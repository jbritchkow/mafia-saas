import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class MafiaSThread extends Thread {
    private Socket socket = null;
    private final Semaphore sendOutput = new Semaphore(1, true);
    private PrintWriter out = null;
    private MafiaGame2 mg;
    public MafiaSThread(Socket socket) {
        super("MafiaSThread");
        this.socket = socket;
    }

    public void socketOutput(String message) {
        try {
            sendOutput.acquire();
        } catch (InterruptedException e) {
            System.out.println("Failed to send message");
            return;
        }
        if (out == null) {
            System.out.println("Socket not setup");
            return;
        }
        out.println(message);
        sendOutput.release();
    }

    public String getRole() {
        return mg.getRole();
    }
    public int getPlayerId() {
        return mg.getPlayerId();
    }
    public void endVoting() {
        mg.endVoting();
        socketOutput("Voting is finished. Press enter to continue.");
    }
    public void endMafiaVoting() {
        mg.endMafiaVoting();
        socketOutput("Pretend to be a civilian for now.");
    }

    public void run() {

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ) {
            out = new PrintWriter(socket.getOutputStream(), true);
            String inputLine, outputLine;
            mg = new MafiaGame2(this);//service class
            outputLine = mg.processGame(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                Timer timer = new Timer();
                timer.schedule(new DelayedMessageTask("Processing input, please wait"), 1000);
                outputLine = mg.processGame(inputLine);
                timer.cancel();
                socketOutput(outputLine);
                while (in.ready()) { in.skip(1);}
                if (outputLine.equals("Game over"))
                    break;
            }
            out.close();
            out = null;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class DelayedMessageTask extends TimerTask {
        private String message;
        public DelayedMessageTask(String message) {
            this.message = message;
        }
        public void run() {
            socketOutput(message);
        }
    }
}