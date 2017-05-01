import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class CloudMafia {
public static int code;
public static Semaphore mutex;
public static Semaphore multithread;
public static int userid;
public static DatabaseHelper dbHelper;
public static int threadcount;
public static int gameOverCondition;
public static int here;
public static int here2;
public static int [] votes;
public static int livingAbilities;
//public static int threadCount;
private static final Object lock = new Object();//not a real lock
private static long checkTime;
//private static long startTime;
//private static long finishTime;
    private static ArrayList<MafiaSThread> threads;

public static boolean timeCheck(){

    synchronized(lock){
        try {
            //finishTime=System.currentTimeMillis();
            //long checkTime=finishTime-startTime;
            System.out.println(checkTime);
            if(checkTime>30000) {
                lock.wait(500);
                lock.notify();
                return false;
            }
        }
        catch(InterruptedException e){
            System.out.println("interrupted main thread");
        }
    }
    return true;
}
public static boolean timeCheckgame(){
    System.out.println("timecheck =" +timeCheck());
    if(multithread!=null){
        return timeCheck();
    }
    return true;
}
    public static void main(String args[]){
    code =15323;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch(Exception e) { return; }
        dbHelper = new DatabaseHelper("jdbc:mysql://mafia.curimo31kkeg.us-west-2.rds.amazonaws.com:3306/mafia?user=aslee1&password=secretpassword987");
        dbHelper.deleteGame(code);
        /*
        hardcoded the code for now. I don't think we should need to worry about
        randomizing it too much? In any case, used an int because ints
        are easy to compare and easy to randomize.
        */

        here=here2=0;
        gameOverCondition=0;
        mutex= new Semaphore(1,true);
        //mutex.release();
        userid=1;
        livingAbilities=3;
        System.out.println(code+"");
        boolean listening=true;
        long startTime = System.currentTimeMillis();
        // Run some code;
        long finishTime =0;
        //long checkTime;
        int portNumber = 4444;//Random port, can customize later. prev: Integer.parseInt(args[0]);
        threads = new ArrayList<MafiaSThread>();
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                MafiaSThread thread = new MafiaSThread(serverSocket.accept());
                thread.start();
                threads.add(thread);
                finishTime=System.currentTimeMillis();
                checkTime=finishTime-startTime;
                listening=timeCheck();
                System.out.println(listening);

                for (MafiaSThread t : threads) {
                    new AsyncMessage(t, "There are now " + threads.size() + " players in the game.").start();
                }
            }
            votes=new int [userid+threadcount+1];
            for(int i=0;i<votes.length;i++){
                votes[i]=0;
            }
            multithread=new Semaphore(threadcount,true);
            multithread.release(threadcount);
            //while(true){
           //     if(here==threadcount){
            ///        System.out.println("It'll work");
           //     }
           // }
            //if(here==threadcount){
              //  multithread.release(threadcount);
            //}
            //Could check per thread, if state equals, then wait...?
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    private static ArrayList<MafiaSThread> getMafiaThreads() {
        ArrayList<MafiaSThread> list = new ArrayList<MafiaSThread>();
        for (MafiaSThread thread : threads) {
            if (thread.getRole().equals("Mafia") && !thread.getState().equals(DatabaseHelper.States.DEAD.toString())) {
                String state = dbHelper.getPlayerState(code, thread.getPlayerId());
                if (!DatabaseHelper.States.DEAD.equalsState(state))
                    list.add(thread);
            }
        }
        return list;
    }

    private static HashMap<Integer, Integer> mafiaChoices = null;
    public static void mafiaChat(int mafiaId, int targetId) {
        if (mafiaChoices == null)
            mafiaChoices = new HashMap<Integer, Integer>();

        ArrayList<MafiaSThread> mafiaThreads = getMafiaThreads();
        boolean chosenCondition = false;

        mafiaChoices.put(mafiaId, targetId);

        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (Integer id : mafiaChoices.values()) {
            counts.put(id, (counts.containsKey(id) ? counts.get(id) + 1 : 1));
        }
        String message = "";
        int chosen = 0;
        for (Integer id : counts.keySet()) {
            if (counts.get(id) == mafiaThreads.size()) {
                chosenCondition = true;
                chosen = id;
                break;
            }
            String name = dbHelper.getPlayerName(code, id);
            message += name + " has " + counts.get(id) + " vote(s)\n";
        }
        if (mafiaChoices.size() < mafiaThreads.size())
            message += (mafiaThreads.size() - mafiaChoices.size()) + " mafia have not voted\n";
        if (chosenCondition) {
            message += "The mafia has chosen " + dbHelper.getPlayerName(code, chosen) + " to suffer.";
            dbHelper.assignStateToPlayer(code, chosen, DatabaseHelper.States.MARKED.toString());
        }

        for (MafiaSThread thread : mafiaThreads) {
            new AsyncMessage(thread, message).start();
            if (chosenCondition) {
                thread.endMafiaVoting();
            }
        }
        if (chosenCondition) {
            finishAbilitiesStage();
            mafiaChoices = null;
        }
    }

    private static int abilitiesCounter = 0;
    public static void finishAbilitiesStage() {
        while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                System.out.println("Sorry, interrupt");
            }
        }
        abilitiesCounter ++;
        if (abilitiesCounter == livingAbilities) {
            abilitiesCounter = 0;
            for (MafiaSThread thread : threads) {
                thread.endVoting();
            }
        }
        CloudMafia.mutex.release();
    }

    private static boolean hasProcessedMafiaAttack = false;
    private static boolean hasProcessedVotingStage = false;
    public static boolean hasSendVotingMessages = false;
    public static void resetTurnCounters() {
        hasProcessedMafiaAttack = false;
        hasProcessedVotingStage = false;
        hasSendVotingMessages = false;
    }

    public static void processMafiaAttack() {
        while (!mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                //bad
            }
        }
        if (hasProcessedMafiaAttack) {
            mutex.release();
            return;
        } else  {
            hasProcessedMafiaAttack = true;
            mutex.release();

            //process mafia attack
            HashMap<Integer, String> savedPlayers = dbHelper.getPlayersWithState(code, DatabaseHelper.States.HEALED.toString());
            HashMap<Integer, String> markedPlayers = dbHelper.getPlayersWithState(code, DatabaseHelper.States.MARKED.toString());
            String message = "";

            if (markedPlayers.size() == 0 && savedPlayers.size() > 0) {
                message = "The mafia tried to get " + savedPlayers.values().toArray()[0] + ", but he was saved by the doctor.";
            } else if (markedPlayers.size() > 0) {
                message = "The mafia got " + markedPlayers.values().toArray()[0];
            } else {
                message = "Something weird happened";
            }

            for (MafiaSThread thread : threads) {
                new AsyncMessage(thread, message).start();
            }
            dbHelper.resetPlayerStatesForNextTurn(code);
        }
    }

    public static void processVotingState(int playerId) {
        while (!mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                //bad
            }
        }
        if (hasProcessedVotingStage) {
            mutex.release();
            return;
        } else  {
            hasProcessedVotingStage = true;
            mutex.release();

            HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
            String gameOutput = "";

            gameOutput = "You voted that " + map.get(playerId) + " was in the mafia!";
            CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, playerId, "DEAD");
            if(CloudMafia.dbHelper.getPlayerRole(CloudMafia.code, playerId).equals("Mafia")){
                CloudMafia.gameOverCondition++;
                gameOutput+=" And you were right!";
            }
            else gameOutput+=" Unfortunately, they were not.";

            for (MafiaSThread thread : threads) {
                new AsyncMessage(thread, gameOutput).start();
            }

            CloudMafia.hasSendVotingMessages = true;
        }
    }
}