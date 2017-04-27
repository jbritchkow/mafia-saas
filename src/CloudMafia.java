import java.io.IOException;
import java.net.*;
import java.util.concurrent.Semaphore;

public class CloudMafia {
public static int code;
public static Semaphore mutex;
public static Semaphore multithread;
public static int userid;
public static DatabaseHelper dbHelper;
public static int threadcount;
//public static int threadCount;
private static final Object lock = new Object();//not a real lock
private static long checkTime;

public static boolean timeCheck(){
    System.out.print(checkTime);
    synchronized(lock){
        try {
            if(checkTime>30000) {
                lock.wait(5000);
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
    /*try {
        lock.wait(5000);
    }
    catch(InterruptedException e){
        System.out.println("interrupted timeCheckgame");
    }
    */
    if(multithread!=null){
        return timeCheck();
    }
    return true;
}
    public static void main(String args[]){
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch(Exception e) { return; }
        dbHelper = new DatabaseHelper("jdbc:mysql://localhost/mafia?user=root&password=password");
        /*
        hardcoded the code for now. I don't think we should need to worry about
        randomizing it too much? In any case, used an int because ints
        are easy to compare and easy to randomize.
        */
        code =15323;
        mutex= new Semaphore(1,true);
        userid=0;
        System.out.println(code+"");
        boolean listening=true;
        long startTime = System.currentTimeMillis();
        // Run some code;
        long stopTime =0;
        //long checkTime;
        int portNumber = 444;//Random port, can customize later, probably want localhost for testing. Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new MafiaSThread(serverSocket.accept()).start();
                stopTime=System.currentTimeMillis();
                checkTime=stopTime-startTime;
                listening=timeCheck();
                System.out.println(listening);

            }
            multithread=new Semaphore(threadcount,true);
            //Could check per thread, if state equals, then wait...?
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

}