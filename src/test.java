import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class test {
    public static void  main(String args[]) throws IOException {
//        new BootstrapServer().start();
        Node n=new Node("Node1");
        Node n1=new Node("Node2");
        n.start();
        String echo1 = n1.sendEcho("0038 REG "+n1.getIp()+" "+n1.getPort()+" "+n1.getUsername());
        System.out.println(echo1);
        String echo = n.sendEcho("0038 REG "+n.getIp()+" "+n.getPort()+" "+n.getUsername());
        System.out.println(echo);

//        assertEquals("hello server", echo);
        echo = n.sendEcho("server is working");
//        assertFalse(echo.equals("hello server"));


    }
}
