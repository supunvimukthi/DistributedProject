import java.io.IOException;
import java.net.*;
import java.util.*;

public class Node extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private String ip;
    private int port;
    private String username;
    private String s;
    private byte[] buf;
    List<Double> totalhops = new ArrayList<>();
    List<Double> latencyList = new ArrayList<>();
    List<NeighbourNode> connectedNodes = new ArrayList<NeighbourNode>(); // all the connected peer nodes

    // complete file list
    List<String> allFilesList = new ArrayList<>(Arrays.asList("Adventures of Tintin", "Jack and Jill", "Glee",
            "The Vampire Diarie", "King Arthur", "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight",
            "Windows 8", "Mission Impossible", "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010",
            "Happy Feet", "Modern Family", "American Idol", "Hacking for Dummies"));

    List<String> fileList = new ArrayList<>(); // files assigned to this node
    private int nodesLeft = 0; // remaining #no of connected peer nodes
    private int noReceived = 0;
    private int noForwarded = 0;
    private int noResponded = 0;
    public static String serverIP = "127.0.0.1";
    private long queryStartTime;
    private long queryEndTime;

    /* TODO : handle node disconnections from the network, partition tolerance
     * TODO : create UI for node initialization and file sharing
     * TODO : create REST API for downloading file
     * */

    public Node(String ip, int port, String username) {
        this.username = username;
        Collections.shuffle(allFilesList); // shuffle the complete file list to randomly assign 3 files to each node
        for (int i = 0; i < 3; i++) {
            fileList.add(allFilesList.get(i));
        }
        try {
            address = InetAddress.getByName(ip);
            socket = new DatagramSocket(port, address);
            this.port = port;
            this.ip = ip;
        } catch (UnknownHostException e) {
//            System.out.println(e);
//            e.printStackTrace();
        } catch (SocketException e) {
//            System.out.println(e);
//            System.out.println("Port is already in use");
//            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public String getIp() {
        return this.ip;
    }

    public String getUsername() {
        return this.username;
    }

    public int getPort() {
        return this.port;
    }


    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                socket.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength()); // message received
//                echo(s);
                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (command.equals("JOIN")) {  //listens for any JOIN requests from peer nodes
                    String reply = "JOINOK ";
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    reply += "0";
                    connectedNodes.add(new NeighbourNode(ip, port));
                    reply = String.format("%04d", reply.length() + 5) + " " + reply;
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    socket.send(dpReply);
                } else if (command.equals("LEAVE")) { // listens for any LEAVE request from connected peer nodes
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    for (int i = 0; i < connectedNodes.size(); i++) { // TODO : handle disconnections from the network
                        if (connectedNodes.get(i).getPort() == port && connectedNodes.get(i).getIp().equals(ip)) {
                            connectedNodes.remove(i);
                            String reply = "0014 LEAVEOK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                            socket.send(dpReply);
                        }
                    }
                } else if (command.equals("SER")) {  // listens for SEARCH queries from the connected nodes
                    noReceived += 1;
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String filename = st.nextToken();
                    int hops = Integer.parseInt(st.nextToken());
                    List<Integer> found = new ArrayList<>();
                    // check for the files in the node that matches the query file
                    for (int i = 0; i < fileList.size(); i++) {
                        String[] words = fileList.get(i).split(" ");
                        for (int j = 0; j < words.length; j++) {
                            if (words[j].equals(filename)) {
                                found.add(i);
                            }
                        }
                    }
                    if (found.size() == 0) { // if no matching files are found pass the message to all connected peer nodes
                        if (hops < 10) {// TODO : decide on hops threshold
                            String reply = "SER " + ip + " " + port + " " + filename + " " + (hops + 1);
                            reply = String.format("%04d", reply.length() + 5) + " " + reply;
                            for (int i = 0; i < connectedNodes.size(); i++) {
                                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, InetAddress.getByName(connectedNodes.get(i).getIp()), connectedNodes.get(i).getPort());
                                socket.send(dpReply);
                                noForwarded += 1;
                            }
                        }
                    } else {  // if the file is found in this node pass it to the requested node
                        String reply = "SEROK " + found.size() + " " + this.ip + " " + this.port + " " + (hops + 1) + " ";
                        for (int i = 0; i < found.size(); i++) {
                            reply += String.join("_", fileList.get(found.get(i)).split(" ")) + " ";
                        }
                        reply = String.format("%04d", reply.length() + 5) + " " + reply;
                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, InetAddress.getByName(ip), port);
                        socket.send(dpReply);
                        noResponded += 1;
                    }
                } else if (command.equals("UNROK")) { // handles unregister responses by leaving from all the nodes
                    String noNodes = st.nextToken();
                    if (noNodes.equals("0")) {
                        echo("Successfully Unregistered from the BS");
                        LEAVE();
                    } else {
                        echo("Error in unregistering from BS");
                    }
                    if (nodesLeft == connectedNodes.size()) { // if the node has left all the connected peer nodes, close the connection
                        echo("Closing the connection");
                        this.close();
                    }
                } else if (command.equals("LEAVEOK")) { // handles leave responses.
                    String result = st.nextToken();
                    if (result.equals("0")) {
                        echo("Successfully LEFT");
                        nodesLeft += 1;
                    } else {
                        echo("Error in Leaving from one node");
                    }
                    if (nodesLeft == connectedNodes.size()) { // if the node has left all the connected peer nodes, close the connection
                        echo("Closing the connection");
                        this.close();
                    }

                } else if (command.equals("SEROK")) {
                    queryEndTime = System.currentTimeMillis();
                    int filecount = Integer.parseInt(st.nextToken());
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String hops = st.nextToken();
                    String files = "";
                    for (int i = 0; i < filecount; i++) {
                        String filename = st.nextToken();
                        files += filename + " ";
                    }
                    float latency = (queryEndTime - queryStartTime) / 1000F;
                    latencyList.add(Double.parseDouble(latency + ""));
                    totalhops.add(Double.parseDouble(hops));
                    echo(filecount + " files found in the " + ip + ":" + port + " node with " + hops + " hops. File Name: " + files + " : " + latency);

                    // TODO: handle implementation of downloading the file

                } else if (command.equals("SHOW_ROUTE")) {
                    showNodesTable();
                } else if (command.equals("SHOW_FILES")) {
                    showFilesList();
                } else { // handles commands that does not match to any of the above commands
                    String received = new String(incoming.getData(), 0, incoming.getLength());
                    echo("Error in the command: " + received);
                }
            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    /*
     * register node on the bootstrap Server and initiate join to the peer nodes
     * */
    public String REG() {  // change ip address of the sending packet
        if (socket != null) {

            String result = "";
            String msg = "REG " + this.ip + " " + this.port + " " + this.username;
            msg = String.format("%04d", msg.length() + 5) + " " + msg;
            buf = msg.getBytes();
            try {
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverIP), 55555);

                socket.send(packet);

                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                socket.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());
                //            echo(s);
                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();
                //            System.out.println(command.length());
                if (command.equals("REGOK")) {
                    String noNodes = st.nextToken();
                    if (noNodes.equals("0")) { // handles joining to peer nodes according to the architecture of the number of nodes currently in the network
                        echo("Successfully Registered to BS - 1st Node in the P2P DS");
                    } else if (noNodes.equals("1")) {
                        String ip = st.nextToken();
                        int port = Integer.parseInt(st.nextToken());
                        connectedNodes.add(new NeighbourNode(ip, port));
                    } else if (noNodes.equals("2")) {
                        String ip = st.nextToken();
                        int port = Integer.parseInt(st.nextToken());
                        connectedNodes.add(new NeighbourNode(ip, port));
                        ip = st.nextToken();
                        port = Integer.parseInt(st.nextToken());
                        connectedNodes.add(new NeighbourNode(ip, port));
                    } else {
                        return noNodes;
                    }
                    showFilesList();
                    result = JOIN(); // after registering with BS, join to the nodes provided by the BS
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            return "SOCKET";
        }
    }

    /*
     * unregistering nodes from bootstrap server initiating leave from connected peer nodes
     * */
    public void UNREG() {
        String msg = "UNREG " + this.ip + " " + this.port + " " + this.username;
        msg = String.format("%04d", msg.length() + 5) + " " + msg;
        buf = msg.getBytes();
        try {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverIP), 55555);

            socket.send(packet); // initialise the unregistering process and handles the response through the thread by listening through the socket

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * join the connected nodes with the peer nodes provided by the bootstrap server
     * */
    public String JOIN() { // implement the method for join and leave by sending data packets to nodes in the routing graph
        String result = "";
        try {
            int successCount = 0;
            for (int i = 0; i < connectedNodes.size(); i++) {
                String reply = "JOIN ";
                reply += this.ip + " " + this.port;
                reply = String.format("%04d", reply.length() + 5) + " " + reply;
                buf = reply.getBytes();
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length, InetAddress.getByName(connectedNodes.get(i).getIp()), connectedNodes.get(i).getPort());
                socket.send(packet); // sending joing requests to all the nodes provided by the BS
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                socket.receive(incoming);


                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());
//                echo(s);
                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (command.equals("JOINOK")) { // handles the join response
                    String resultCode = st.nextToken();
                    if (resultCode.equals("0")) {
                        echo(this.ip + ":" + this.port + " (" + this.username + ") successfully JOINED to " + connectedNodes.get(i).getIp() + ":" + connectedNodes.get(i).getPort());
                        successCount += 1;
                    } else {
                        echo("Error in Joining to one node");
                    }
                }


            }
            /*
             * check whether join is successfull with all the provided nodes from the BS.
             * if yes start listening through the socket
             * if no show error message  ---> TODO : try reconnecting
             * */
            if (successCount == connectedNodes.size()) {
                showNodesTable();
                this.start();
                echo(this.ip + ":" + this.port + " " + this.username + " successfully REGISTERED on BS and JOINED to all nodes provided by the BS");
                result = "SUCCESS";
            } else {
                echo("ERROR ERROR ERROR");
                result = "ERROR";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * Leaving the connected peer nodes after unregistering with the bootstrap server
     * */
    public void LEAVE() {
        try {
            for (int i = 0; i < connectedNodes.size(); i++) {
                String reply = "LEAVE ";
                reply += this.ip + " " + this.port;
                reply = String.format("%04d", reply.length() + 5) + " " + reply;
                buf = reply.getBytes();
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length, InetAddress.getByName(connectedNodes.get(i).getIp()), connectedNodes.get(i).getPort());
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Searching for a file in the peer to peer network by flooding the query message through the network
     * first message is passed to the connected nodes of the querying nodes.
     * then if the file is not found message is passed continuously through connected peer nodes until the required file is found
     * */
    public void SEARCH(String query) { //change the implementation
        try {
            String reply = "SER " + this.ip + " " + this.port + " " + query + " " + 0;
            reply = String.format("%04d", reply.length() + 5) + " " + reply;
            buf = reply.getBytes();
            for (int i = 0; i < connectedNodes.size(); i++) {
                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                        InetAddress.getByName(connectedNodes.get(i).getIp()), connectedNodes.get(i).getPort());
                queryStartTime = System.currentTimeMillis();
                socket.send(dpReply);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * used to list out all the nodes in the network by sending a request to the bootstrap server --- all registered nodes in the BS
     * */
    public void ECHO() {
        String msg = "0007 ECHO";
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 55555);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void echo(String msg) {
        System.out.println(msg);
    }

    public void close() {
        socket.close();
    }

    /*
    * Display a list of connected nodes to this node - Routing table
    * */
    public void showNodesTable() {
        System.out.println("\nConnected nodes of the " + this.username + " of " + this.ip + ":" + this.port);
        String leftAlignFormat = "| %-15s | %-6d |%n";

        System.out.format("+-----------------+--------+%n");
        System.out.format("| Ip Address      | Port   |%n");
        System.out.format("+-----------------+--------+%n");
        for (int i = 0; i < connectedNodes.size(); i++) {
            System.out.format(leftAlignFormat, connectedNodes.get(i).getIp(), connectedNodes.get(i).getPort());
        }
        System.out.format("+-----------------+--------+%n");
    }

    /*
    * Show the list of files assigned to this node.
    * */
    public void showFilesList() {
        System.out.println("\nList of files in the " + this.username + " of " + this.ip + ":" + this.port);
        String leftAlignFormat = "| %-21s    |%n";

        System.out.format("+-----------------+--------+%n");
        System.out.format("|       File Names         |%n");
        System.out.format("+-----------------+--------+%n");
        for (int i = 0; i < fileList.size(); i++) {
            System.out.format(leftAlignFormat, fileList.get(i));
        }
        System.out.format("+-----------------+--------+%n");
    }

    /*
    * resetting all the performance stats of the node to start testing with different node structure
    * */
    public void resetStat(){
        noReceived=0;
        noForwarded=0;
        noResponded=0;
        totalhops=new ArrayList<>();
        latencyList=new ArrayList<>();
    }

    /*
    * showing all the performance stats related to this node
    * */
    public void showPerformance() {
        System.out.println("\nPerformance of " + this.username + " of " + this.ip + ":" + this.port + "\n");
        String Format = "| %-20s |   %-10d  |%n";
        System.out.format("+----------------------+----------------+%n");
        System.out.format(Format, "Received messages", noReceived);
        System.out.format(Format, "Forwarded messages", noForwarded);
        System.out.format(Format, "Answered messages", noResponded);
        System.out.format(Format, "Node degree", connectedNodes.size());
        System.out.format("+----------------------+----------------+%n\n");
        String leftAlignFormat = "| %-15s |    %2.0f    |   %2.0f     |  %-2.2f   |   %2.2f    |%n";
        String leftFormat = "| %-15s |   %2.3f  |  %2.3f   | %-2.3f    |  %2.3f    |%n";

        System.out.format("+-----------------+----------+----------+----------+-----------+%n");
        System.out.format("| Property        |  Min     |  Max     |  Avg     |    SD     |%n");
        System.out.format("+-----------------+----------+----------+----------+-----------+%n");

        System.out.format(leftAlignFormat, "Hops", Collections.min(totalhops), Collections.max(totalhops), avg(totalhops), sd(totalhops));
        System.out.format(leftFormat, "Latency", Collections.min(latencyList), Collections.max(latencyList), avg(latencyList), sd(latencyList));

        System.out.format("+-----------------+----------+----------+----------+-----------+%n");

    }

    /*
    * function to calculate the Standard deviation of a given list: hops or latency
    * */
    public double sd(List<Double> table) {
        double mean = avg(table);
        double temp = 0;

        for (int i = 0; i < table.size(); i++) {
            double val = table.get(i);
            double squrDiffToMean = Math.pow(val - mean, 2);
            temp += squrDiffToMean;
        }
        double meanOfDiffs = (double) temp / (double) (table.size());
        return Math.sqrt(meanOfDiffs);
    }

    /*
    * function to calculate the avaerage of the given list: hops or latency
    * */
    public double avg(List<Double> table) {
        int total = 0;

        for (int i = 0; i < table.size(); i++) {
            double currentNum = table.get(i);
            total += currentNum;
        }
        return (double) total / (double) table.size();
    }
}
