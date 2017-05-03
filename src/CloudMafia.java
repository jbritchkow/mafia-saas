import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class CloudMafia {
public int code;
public Semaphore mutex;
public Semaphore multithread;
public int userid;
public DatabaseHelper dbHelper;
public int threadcount;
public int gameOverCondition;
public int here;
public int here2;
public int here3;
public int here4;
public int [] votes;
public int livingAbilities;
public String checkthis;
//public static int threadCount;
private final Object lock = new Object();//not a real lock
private long checkTime;
public boolean start = false;
//private static long startTime;
//private static long finishTime;
    private ArrayList<MafiaSThread> threads;

public boolean timeCheck(){

    synchronized(lock){
        try {
            //finishTime=System.currentTimeMillis();
            //long checkTime=finishTime-startTime;
            System.out.println(checkTime);
           //if(checkTime>30000) {
            if(start) {

                lock.wait(500);
                lock.notify();
                return false;
                //}
            }
        }
        catch(InterruptedException e){
            System.out.println("interrupted main thread");
        }
    }
    return true;
}
public boolean timeCheckgame(String thing){
    System.out.println("timecheck =" +timeCheck());
    System.out.println("thing: "+thing);
    if(checkthis.equals(thing)){
        while (!mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);//milliseconds
            } catch (InterruptedException interrupt) {
            }
        }
        if (!start) {
            startGame();
            start = true;
        }
        start=true;
        mutex.release();
        return false;
    }
    return true;
}
    public CloudMafia(int gameCode){
    checkthis = "startgame";
    //checkthis="0";
    code =gameCode;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch(Exception e) { return; }
        dbHelper = new DatabaseHelper("jdbc:mysql://mafia.curimo31kkeg.us-west-2.rds.amazonaws.com:3306/mafia?user=jbritchkow&password=secretpassword987");
        //dbHelper.resetGame(code);
        /*
        hardcoded the code for now. I don't think we should need to worry about
        randomizing it too much? In any case, used an int because ints
        are easy to compare and easy to randomize.
        */

        here=here2=here3=here4=0;
        gameOverCondition=0;
        mutex= new Semaphore(1,true);
        //mutex.release();
        userid=0;
        livingAbilities=3;
        System.out.println(code+"");
        threads = new ArrayList<MafiaSThread>();
    }

    private void startGame() {
        votes=new int [userid+threadcount+1];
        for(int i=0;i<votes.length;i++){
            votes[i]=0;
        }
        multithread=new Semaphore(threadcount,true);
        multithread.release(threadcount);
    }

    public void addThread(MafiaSThread thread) {
        threads.add(thread);
        for (MafiaSThread t : threads) {
            new AsyncMessage(t, "There are now " + threads.size() + " players in the game.").start();
        }
    }

    private ArrayList<MafiaSThread> getMafiaThreads() {
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

    private HashMap<Integer, Integer> mafiaChoices = null;
    public void mafiaChat(int mafiaId, int targetId) {
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

    private int abilitiesCounter = 0;
    public void finishAbilitiesStage() {
        while (!this.mutex.tryAcquire()) { //readwritelock?
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
        this.mutex.release();
    }

    private boolean hasProcessedMafiaAttack = false;
    private boolean hasProcessedVotingStage = false;
    public boolean hasSendVotingMessages = false;
    public void resetTurnCounters() {
        hasProcessedMafiaAttack = false;
        hasProcessedVotingStage = false;
        hasSendVotingMessages = false;
        here3 = 0;
    }

    public void processMafiaAttack() {
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
                message = "The mafia tried to get " + savedPlayers.values().toArray()[0] + ", but he was saved by the doctor.  Press enter to continue.";
            } else if (markedPlayers.size() > 0) {
                message = "The mafia got " + markedPlayers.values().toArray()[0] + ". Press enter to continue.";
            } else {
                message = "Something weird happened";
            }

            for (MafiaSThread thread : threads) {
                new AsyncMessage(thread, message).start();
            }
            dbHelper.resetPlayerStatesForNextTurn(code);
        }
    }

    public void processVotingState(int playerId) {
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

            HashMap<Integer, String> map = this.dbHelper.getLivingPlayers(this.code);
            String gameOutput = "";

            gameOutput = "The group voted that " + map.get(playerId) + " was in the mafia!";
            this.dbHelper.assignStateToPlayer(this.code, playerId, "DEAD");
            if(this.dbHelper.getPlayerRole(this.code, playerId).equals("Mafia")){
                this.gameOverCondition++;
                gameOutput+=" And you were right!";
            }
            else gameOutput+=" Unfortunately, they were not.";
            gameOutput += " Press enter to continue.";
            for (MafiaSThread thread : threads) {
                new AsyncMessage(thread, gameOutput).start();
            }

            this.hasSendVotingMessages = true;
        }
    }
}