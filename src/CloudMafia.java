import java.io.IOException;
import java.net.*;

public class CloudMafia {
public static int code;

    public static void main(String args[]){
        /*
        hardcoded the code for now. I don't think we should need to worry about
        randomizing it too much? In any case, used an int because ints
        are easy to compare and easy to randomize.
        */
        code =15323;
        System.out.println(code+"");
        boolean listening=true;

        int portNumber = 4444;//Random port, can customize later, probably want localhost for testing. Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new MafiaSThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

}