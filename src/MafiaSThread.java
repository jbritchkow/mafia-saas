import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class MafiaSThread extends Thread {
    private Socket mafiaConnectionSocket = null;
    private final Semaphore sendOutput = new Semaphore(1, true);
    private PrintWriter out = null;
    private MafiaGame2 mg;
    public MafiaSThread(Socket incomingSocket) {
        super("MafiaSThread");
        this.mafiaConnectionSocket = incomingSocket;
    }

    public void socketOutput(String message) {
        try {
            sendOutput.acquire();
        } catch (InterruptedException e) {
            System.out.println("Failed to send message");
            return;
        }
        if (out == null) {
            System.out.println("PrintWriter messed up. Attempting to fix.");
            try {
                out = new PrintWriter(mafiaConnectionSocket.getOutputStream(), true);
            } catch (Exception e) { System.out.println("Could not recreate PrintWriter for " + mg.getPlayerId()); }
            sendOutput.release();
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
                                mafiaConnectionSocket.getInputStream()));
        ) {
            out = new PrintWriter(mafiaConnectionSocket.getOutputStream(), true);
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
            mafiaConnectionSocket.close();
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