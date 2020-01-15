import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.Scanner;


public class test {
    public static void main(String args[]) {


        boolean indexed = false;
        Node n = null;
        try {
            URL url = new URL("http://127.0.0.1:80");
            HttpURLConnection con = null;
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int code = con.getResponseCode();
            System.out.println(code);

            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))){
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));
            System.out.println("Welcome new Client.Please select whichever you want");
            System.out.println("1 - Register to BS\n" + "2 - Search Files\n" + "3 - Configure BS IP\n" +
                    "4 - Check Performance\n"+ "5 - Check Routing Table\n6 - Check FileList\n7 - Reset Performance Stats\n");
            String choice = scanner.nextLine();
            if (choice.equals("1")) {
                if (indexed && n != null) {
                    System.out.println("You have already registered in the network. if you want to unregister and register again enter 0 Else enter 1");
                    String choice1 = scanner.nextLine();
                    if (choice1.equals("0")) {
                        n.UNREG();
                        n = null;
                    }
                } else {
                    System.out.println("Please enter the IP of the server in this format XXX.XXX.XXX.XXX");
                    String ip = scanner.nextLine();
                    System.out.println("Please enter the port number of which the server is going to listen to.");
                    int port = Integer.parseInt(scanner.nextLine());
                    System.out.println("Please enter a username");
                    String username = scanner.nextLine();
                    System.out.println("Adding the node to the network..........\n");
                    n = new Node(ip, port, username);
                    String result=n.REG();
                    if (result.equals("9999")){
                        System.out.println("failed, there is some error in the command\n");
                        n=null;
                    }else if (result.equals("9998")){
                        System.out.println("failed, already registered to you, unregister first\n");
                        n=null;
                    }
                    else if (result.equals("9997")){
                        System.out.println("failed, registered to another user, try a different IP and port\n");
                        n=null;
                    }else if (result.equals("9996")){
                        System.out.println("failed, can’t register. BS full.\n");
                        n=null;
                    }else if (result.equals("ERROR")){
                        System.out.println("Error in joining some nodes. try registering again\n");
                        n=null;
                    }else if (result.equals("SOCKET")){
                        System.out.println("Error in given port. Try again with another port\n");
                        n=null;
                    }
                }
                indexed = true;
            }else if(choice.equals("2")){
                if(n!=null){
                    System.out.println("Welcome to the file searching wizard. Enter the name of the file you want to search: ");
                    String filename = scanner.nextLine();
                    if(filename.equals("")){
                        System.out.println("You can't search for empty string.Enter valid file name: ");
                        filename = scanner.nextLine();
                        n.SEARCH(filename);
                    }else{
                        n.SEARCH(filename);
                    }
                }else{
                    System.out.println("Please register in the network first\n");
                }
            }else if(choice.equals("3")){
                    System.out.println("Enter the Bootstrap ServerIP : ");
                    String ipBS = scanner.nextLine();
                    Node.serverIP=ipBS;

            }
            else if(choice.equals("4")){
                if(n!=null){
                    n.showPerformance();
                }else{
                    System.out.println("Please register in the network first\n");
                }
            }
            else if(choice.equals("5")){
                if(n!=null){
                    n.showNodesTable();
                }else{
                    System.out.println("Please register in the network first\n");
                }
            }
            else if(choice.equals("6")){
               if(n!=null){
                   n.showFilesList();
               }else{
                   System.out.println("Please register in the network first\n");
               }

            }
            else if(choice.equals("7")){
                if(n!=null){
                    n.resetStat();
                }else{
                    System.out.println("Please register in the network first\n");
                }

            }


        }


        // TODO : manage disconnected nodes .... maintain availability and partition tolerance
    }
}
