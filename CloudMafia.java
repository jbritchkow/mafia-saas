public class CloudMafia {


    public static void main(String args[]){
        /*
        hardcoded the code for now. I don't think we should need to worry about
        randomizing it too much? In any case, used an int because ints
        are easy to compare and easy to randomize.
        */
        int code =15323;
        System.out.println(code+"");
        boolean listening=true;

        int portNumber = 4444;//Random port, can customize later Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new MafiaSThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
        //main as controller class?
        //sockets
        //if code, launch thread
        //receive names
        //once all threads are launched and names are received, assign roles

    }

}