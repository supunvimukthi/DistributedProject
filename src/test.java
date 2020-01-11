import java.io.IOException;

public class test {
    public static void  main(String args[]) {

        Node n=new Node("127.0.0.1",54527,"Node1");
        Node n1=new Node("127.0.0.1",54521,"Node2");
        Node n2=new Node("127.0.0.1",54523,"Node3");
        n.REG();
        n1.REG();
        n2.REG();
//        n.UNREG();
//        n1.UNREG();
//        n2.UNREG(); // NOTE --> TODO : delete object or stop running thread from the jvm (unbind socket from the node)
        // manage disconnected nodes .... maintain availability and partition tolerance
//        n.ECHO();
        n.SEARCH("Modern");
        System.out.println("DONE");
    }
}
