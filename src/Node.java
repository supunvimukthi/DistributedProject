import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.StringTokenizer;

public class Node extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private String ip;
    private int port;
    private String username;
    String s;
    private byte[] buf;

//    public Node(int port, String username){
//
//        this.port = port;
//        this.username = username;
//        try {
//            this.ip = InetAddress.getByName("localhost").toString();
//            socket = new DatagramSocket(port,InetAddress.getByName("localhost"));
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }

    public Node(String username) {
        this.username = username;
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
            this.port = socket.getLocalPort();
            this.ip = address.toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
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
                s = new String(data, 0, incoming.getLength());
                System.out.println(s);
                //echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();
//                String reply = "REGOK ";
                if (command.equals("REG")) {
                    String reply = "REGOK ";

                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();
                    if (nodes.size() == 0) {
                        reply += "0";
                        nodes.add(new Neighbour(ip, port, username));
                    } else {
                        boolean isOkay = true;
                        for (int i = 0; i < nodes.size(); i++) {
                            if (nodes.get(i).getPort() == port) {
                                if (nodes.get(i).getUsername().equals(username)) {
                                    reply += "9998";
                                } else {
                                    reply += "9997";
                                }
                                isOkay = false;
                            }
                        }
                        if (isOkay) {
                            if (nodes.size() == 1) {
                                reply += "1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
                            } else if (nodes.size() == 2) {
                                reply += "2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort();
                            } else {
                                Random r = new Random();
                                int Low = 0;
                                int High = nodes.size();
                                int random_1 = r.nextInt(High - Low) + Low;
                                int random_2 = r.nextInt(High - Low) + Low;
                                while (random_1 == random_2) {
                                    random_2 = r.nextInt(High - Low) + Low;
                                }
                                echo(random_1 + " " + random_2);
                                reply += "2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " + nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
                            }
                            nodes.add(new Neighbour(ip, port, username));
                        }
                    }

                    reply = String.format("%04d", reply.length() + 5) + " " + reply;
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    socket.send(dpReply);
                } else if (command.equals("UNREG")) {
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();
                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getPort() == port) {
                            nodes.remove(i);
                            String reply = "0012 UNROK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                            socket.send(dpReply);
                        }
                    }
                } else if (command.equals("ECHO")) {
                    for (int i = 0; i < nodes.size(); i++) {
                        echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort() + " " + nodes.get(i).getUsername());
                    }
                    String reply = "0012 ECHOK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    socket.send(dpReply);
                }
//                reply = String.format("%04d", reply.length() + 5) + " " + reply;
//
//                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
//                sock.send(dpReply);
            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
//            System.out.println("X");

    }

    public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 55555);
        try {
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String received = new String(
                packet.getData(), 0, packet.getLength());
        System.out.println("TEST " + received);
        return received;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

    public void close() {
        socket.close();
    }
}
