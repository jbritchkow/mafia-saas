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

    private int state =GAMESTART;

    //private int code =0;
    public String processGame(String userInput){
        String gameOutput = "";
        String role="Civilian";
        //if(userInput!=null||userInput.equals("Game over")|| userInput.equals("exit"))gameOutput="Game over";
        if(state==GAMESTART){
            gameOutput="Welcome to SaaS Mafia! Please enter your game code.";
            state=ASKEDCODE;
        }
        else if (state == ASKEDCODE){
            int loccode = Integer.parseInt(userInput);//error forced me to do this
            if(loccode==CloudMafia.code){
                //TODO: SANITIZE INPUT
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
        else if (state ==ASKEDNAME){
            if(userInput!=""){
                //TODO: SANITIZE INPUT
                gameOutput = "Hello, " + userInput + "!";
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
                    //TODO: find other mafia member
                    //TODO: mafia communicate and choose someone to kill
                    //we can always back down to one mafia member if this turns out to be too much
                }
                else if(role.equals("Civilian")){//hardcoded for now. fix later.
                    gameOutput+=" What is your quest?";
                }
                CloudMafia.mutex.release();
                //released mutex. Now another thread can access database.
                //CloudMafia.threadCount++;

                // Wait on main thread. Maybe not best idea.
                state = GOTNAME;
            }
            else{
                state=GAMESTART;
            }
        }
        else if (state ==GOTNAME){
            Object obj=new Object();
           // try {
           //     obj.wait();//waits until notifyAll, can be used to wait for all to reach same place
           // }
           // catch(InterruptedException interrupt){
            //    gameOutput=("Sorry, interrupt");
           // }
            //gameOutput = "Hello, " + userInput + "!";
            if(userInput!="") {
                while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        this.wait(2000);//milliseconds
                    } catch (InterruptedException interrupt) {
                        gameOutput = ("Sorry, interrupt");
                    }
                }
                //TODO: Mafia kill, police guess, doctor heals
                //TODO: Write this to database
                CloudMafia.mutex.release();
            }
            else if(role.equals("Civilian")){
                gameOutput="What is your favorite color?";
            }
            //TODO: Civilians chill
            //TODO: Wait until stage is over, then unlock and move to next stage
            state =STEPONE;
        }
        else if (state ==STEPONE) {
            gameOutput = ("Who is in the mafia?");
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
            }
        }
        return gameOutput;
    }
}