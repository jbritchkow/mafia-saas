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
public static int here;
public static int here2;
public static int [] votes;
//public static int threadCount;
private static final Object lock = new Object();//not a real lock
private static long checkTime;
//private static long startTime;
//private static long finishTime;

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

        mutex= new Semaphore(1,true);
        //mutex.release();
        userid=20;
        System.out.println(code+"");
        boolean listening=true;
        long startTime = System.currentTimeMillis();
        // Run some code;
        long finishTime =0;
        //long checkTime;
        int portNumber = 4444;//Random port, can customize later. prev: Integer.parseInt(args[0]);
        ArrayList<MafiaSThread> threads = new ArrayList<MafiaSThread>();
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

}