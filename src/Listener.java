import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Listener {
    private static HashMap<Integer, CloudMafia> gameThreads = null;
    private static ArrayList<MafiaSThread> playerThreads = null;
    public static DatabaseHelper dbHelper = null;

    public static void main(String args[]) {
        int portNumber = 4444;
        gameThreads = new HashMap<>();
        playerThreads = new ArrayList<>();
        dbHelper = new DatabaseHelper("jdbc:mysql://mafia.curimo31kkeg.us-west-2.rds.amazonaws.com:3306/mafia?user=jbritchkow&password=secretpassword987");

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                MafiaSThread thread = new MafiaSThread(serverSocket.accept());
                thread.start();
                playerThreads.add(thread);
            }
        } catch (IOException e) {
            System.out.println("Socket listener failed");
        }
    }

    public static CloudMafia playerJoinGame(int gameId, MafiaSThread playerThread) {
        if (gameThreads.containsKey(gameId) && !gameThreads.get(gameId).start) {
            gameThreads.get(gameId).addThread(playerThread);
            return gameThreads.get(gameId);
        } else
            return null;
    }
    public static int playerCreateGame(MafiaSThread playerThread) {
        Random rand = new Random();
        int r = rand.nextInt(90000) + 10000;
        while (dbHelper.isActiveGame(r))
            r = rand.nextInt(90000)+10000;
        dbHelper.resetGame(r);
        CloudMafia gameThread = new CloudMafia(r);
        gameThreads.put(r, gameThread);
        return r;
    }
}
