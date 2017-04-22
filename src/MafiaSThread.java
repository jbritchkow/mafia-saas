import java.net.*;
import java.io.*;

public class MafiaSThread extends Thread {
    private Socket socket = null;
    public MafiaSThread(Socket socket) {
        super("MafiaSThread");
        this.socket = socket;
    }

    public void run() {

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ) {
            String inputLine, outputLine;
            MafiaGame mg = new MafiaGame();//service class
            outputLine = mg.processGame(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = mg.processGame(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Game over"))
                    break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}