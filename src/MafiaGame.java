import java.net.*;
import java.io.*;

//link thread id to role? Thread id as user id?

public class MafiaGame {
    private static final int GAMESTART = 0;
    private static final int ASKEDCODE = 1;
    private static final int GOTCODE = 2;
    private static final int ASKEDNAME = 3;
    private static final int GOTNAME = 4;
    private static final int STEPONE=5;
    public static final int VOTED =6;
    public static final int ROUNDN=7;
    public static final int WAITING=8;
    public static final int DONEWAIT=9;

    private int state =GAMESTART;

    //private int code =0;
    public String processGame(String userInput){
        String userName="";
        String gameOutput = "";
        String role="Civilian";
        //if(userInput!=null||userInput.equals("Game over")|| userInput.equals("exit"))gameOutput="Game over";
        if(state==GAMESTART){
            gameOutput="Welcome to SaaS Mafia! Please enter your game code.";
            state=ASKEDCODE;
        }
        else if (state == ASKEDCODE){
            //TODO: SANITIZE INPUT
            int loccode = Integer.parseInt(userInput);//error forced me to do this
            if(loccode==CloudMafia.code){

                gameOutput="You have entered game " +CloudMafia.code + "! Hit enter to get started!";
                state = GOTCODE;
            }
            else{
                gameOutput="That is not a valid game.";
                state = GAMESTART;
            }
        }
        else if (state == GOTCODE){
            gameOutput="Please enter your name!";
                state=ASKEDNAME;

        }
        else if (state ==ASKEDNAME) {
            if (userInput != "") {
                //TODO: SANITIZE INPUT
                gameOutput = "Hello, " + userInput + "! Press enter to wait while the game loads.";
                userName=userInput;
                while(!CloudMafia.mutex.tryAcquire()){ //readwritelock?
                    try {
                        this.wait(2000);//milliseconds
                    }
                    catch(InterruptedException interrupt){
                        gameOutput=("Sorry, interrupt");
                    }
                }
                //acquired mutex
                CloudMafia.threadcount++;
                CloudMafia.mutex.release();
                state=WAITING;
            }
            else{
                state=GOTCODE;
            }
        }
        else if(state==WAITING) {
            while (!CloudMafia.timeCheck()) { //checks to see if time has passed. spins.
            }
            state=DONEWAIT;
        }
        else if (state==DONEWAIT){
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }

                while(!CloudMafia.mutex.tryAcquire()){ //readwritelock?
                try {
                    this.wait(2000);//milliseconds
                }
                catch(InterruptedException interrupt){
                    gameOutput=("Sorry, interrupt");
                }
            }
            //acquired mutex

            CloudMafia.userid++;
            // TODO: ASSIGN ROLE
            //TODO: ADD NAME, USERID, ROLE TO ARRAY/DATABASE
            //threadid as userid????? How else to attach thread/user?
            gameOutput+=" You are a " + role + ".";
            if(role.equals("Police")){
                gameOutput+=" Take a guess. Who is in the mafia?";
            }
            else if(role.equals("Doctor")){
                gameOutput+=" Quick, choose someone to save!";
            }
            else if(role.equals("Mafia")){
                gameOutput+=" Whose turn is it to die?";
            }

            CloudMafia.mutex.release();
            //released mutex. Now another thread can access database.
            //CloudMafia.threadCount++
                if(role.equals("Civilian")){//hardcoded for now. fix later.
                gameOutput+=" What is your quest?";
                }
                state = GOTNAME;
            //}
            if(CloudMafia.multithread.availablePermits()==0){
                CloudMafia.multithread.release(CloudMafia.threadcount);
            }
        }


        else if (state ==GOTNAME){
           // Object obj=new Object();
           // try {
           //     obj.wait();//waits until notifyAll, can be used to wait for all to reach same place
           // }
           // catch(InterruptedException interrupt){
            //    gameOutput=("Sorry, interrupt");
           // }
            if(userInput!=""&&!role.equals("Civilian")) {
                while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        this.wait(2000);//milliseconds
                    } catch (InterruptedException interrupt) {
                        gameOutput = ("Sorry, interrupt");
                    }
                }
                if(role.equals("Police")){
                    //TODO: Compare user input to database
                    //TODO: What do if user inputs bad input ie a name that is not a name?
                    // TODO: Write results to database
                }
                if(role.equals("Doctor")){
                    //TODO: see police
                }
                if(role.equals("Mafia")){
                    //TODO: find other mafia member
                    //TODO: mafia communicate and choose someone to kill
                    //we can always back down to one mafia member if this turns out to be too much
                }

                CloudMafia.mutex.release();
            }
           if(role.equals("Civilian")){
                gameOutput="What is your favorite color?";
            }
            //TODO: Wait until stage is over, then unlock and move to next stage
            state =STEPONE;
        }
        else if (state ==STEPONE) {
            gameOutput = ("Who is in the mafia?");
            state = VOTED;
        }
        else if (state == VOTED){
            if(userInput!="") {
                //TODO: sanitize input

                while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        this.wait(2000);//milliseconds
                    } catch (InterruptedException interrupt) {
                        gameOutput = ("Sorry, interrupt");
                    }
                }
                //TODO: sum votes across threads
                //arraylist.add for votes? 2d array with 0s if no votes?
                //TODO: Write results to database
                CloudMafia.mutex.release();

                //TODO: Display results; show if sucessful or not
                //TODO: If all mafia dead, gameOutput=Game Over
                //TODO: else, set state back to gotname and restart
                //TODO: if this thread is dead, ouput=game over
                gameOutput="Looks like the mafia is still out there! Hit enter to start another round.";
                state=GOTNAME;
            }
        }
        else if (state==ROUNDN){

            //acquired mutex
            if(role.equals("Police")){
                gameOutput="Take a guess. Who is in the mafia?";
            }
            else if(role.equals("Doctor")){
                gameOutput="Quick, choose someone to save!";
            }
            else if(role.equals("Mafia")){
                gameOutput="Whose turn is it to die?";
            }


            if(role.equals("Civilian")){
                gameOutput="What is your favorite food?";//hardcoded for now. fix later.
            }
            //released mutex. Now another thread can access database.
            //CloudMafia.threadCount++;
            state=GOTNAME;
        }
        return gameOutput;
    }
}