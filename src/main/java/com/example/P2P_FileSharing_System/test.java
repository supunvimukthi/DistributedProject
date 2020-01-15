package com.example.P2P_FileSharing_System;

import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Scanner;


public class test {
    public static void main(String args[]) {

        DownloadApplication x=new DownloadApplication();
        x.startServer();

        boolean indexed = false;
        Node n = null;

        while (true) {

            Scanner scanner = new Scanner(new InputStreamReader(System.in));
            System.out.println("Welcome Client . Please Select whichever You Want");
            String choice="";

            if(n !=null&&n.downloadable){
                System.out.println("1 - Register to BS\n" + "2 - Search Files\n" + "3 - Configure BS IP\n" +
                        "4 - Check Performance\n"+ "5 - Check Routing Table\n6 - Check FileList\n7 - " +
                        "Reset Performance Stats\n8 - Unregister node from the network\n9 - Download File\n ");
                choice = scanner.nextLine();
            }else {
                System.out.println("1 - Register to BS\n" + "2 - Search Files\n" + "3 - Configure BS IP\n" +
                        "4 - Check Performance\n"+ "5 - Check Routing Table\n6 - Check FileList\n7 - " +
                        "Reset Performance Stats\n8 - Unregister node from the network\n");
                choice = scanner.nextLine();
            }

            if (choice.equals("1")) {
                if (indexed && n != null) {
                    System.out.println("You have Already Registered in the Network. \nIf You want to Unregister and Register Again Enter 0 Else Enter 1");
                    String choice1 = scanner.nextLine();
                    if (choice1.equals("0")) {
                        n.UNREG();
                        n = null;
                    }
                } else {
                    System.out.println("Please enter the IP of the server in this format XXX.XXX.XXX.XXX");
                    String ip = scanner.nextLine();
                    System.out.println("Please Enter the Port Number of which the Node is going to listen to");
                    int port = Integer.parseInt(scanner.nextLine());
                    System.out.println("Please Enter a Username for this node");
                    String username = scanner.nextLine();
                    System.out.println("Adding the Node to the P2P Network..........\n");
                    n = new Node(ip, port, username);
                    String result=n.REG();
                    if (result.equals("9999")){
                        System.out.println("Failed ! , There is Some Error in the command\n");
                        n=null;
                    }else if (result.equals("9998")){
                        System.out.println("Failed ! , You are already Registered in the BS, Unregister First\n");
                        n=null;
                    }
                    else if (result.equals("9997")){
                        System.out.println("Failed, Registered to Another user, Try a different IP and Port\n");
                        n=null;
                    }else if (result.equals("9996")){
                        System.out.println("Failed, Can’t register. BS full.\n");
                        n=null;
                    }else if (result.equals("ERROR")){
                        System.out.println("Error in Joining Some Nodes. Try Registering Again\n");
                        n=null;
                    }else if (result.equals("SOCKET")){
                        System.out.println("Error in Given port. Try again with another Port\n");
                        n=null;
                    }
                }
                indexed = true;
            }else if(choice.equals("2")){
                if(n!=null){
                    System.out.println("Welcome to the File Searching Wizard. \nEnter the name of the File you want to Search: ");
                    String filename = scanner.nextLine();
                    if(filename.equals("")){
                        System.out.println("You can't Search for Empty String. Enter a valid File Name: ");
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
            } else if(choice.equals("8")){
                if(n!=null){
                    n.UNREG();
                    n=null;
                }else{
                    System.out.println("Please register in the network first\n");
                }
            }
            else if(choice.equals("9")){
                if(n!=null){
                    int i=1;
                    String [] fileNameList=new String[n.fileDownloadUrls.size()+2];
                    System.out.println("Choose the file you want to download");
                    for (Enumeration k = n.fileDownloadUrls.keys(); k.hasMoreElements();) {
                        String fileN=k.nextElement().toString();
                        System.out.println(i+ " " + fileN+" - "+n.fileDownloadUrls.get(fileN));
                        fileNameList[i]=fileN;
                        i++;
                    }
                    String fileNo = scanner.nextLine();
                    String fileName=fileNameList[Integer.parseInt(fileNo)];
                    n.downloadFile(n.fileDownloadUrls.get(fileName).toString(),fileName);
                }else{
                    System.out.println("Please register in the network first\n");
                }
            }
        }
    }

}

