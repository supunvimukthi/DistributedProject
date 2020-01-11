/*
* model for the connected peer nodes for any given node.
* */
public class NeighbourNode {
        private String ip;
        private int port;


        public NeighbourNode(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        public String getIp(){
            return this.ip;
        }

        public int getPort(){
            return this.port;
        }
}
