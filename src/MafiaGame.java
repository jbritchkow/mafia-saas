import java.net.*;
import java.io.*;

public class MafiaGame {
    private static final int GAMESTART = 0;
    private static final int ASKEDCODE = 1;
    private static final int GOTCODE = 2;
    private static final int ASKEDNAME = 3;
    private static final int GOTNAME = 4;
    private static final int STEPONE=5;

    private int state =GAMESTART;

    //private int code =0;
    public String processGame(String userInput){
        String gameOutput = null;
        //if(userInput!=null||userInput.equals("Game over")|| userInput.equals("exit"))gameOutput="Game over";
        if(state==GAMESTART){
            gameOutput="Welcome to SaaS Mafia! Please enter your game code.";
            state=ASKEDCODE;
        }
        else if (state == ASKEDCODE){
            int loccode = Integer.parseInt(userInput);//error forced me to do this
            if(loccode==CloudMafia.code){
                //TODO: SANITIZE INPUT
                gameOutput="You have entered game " +CloudMafia.code + "!";
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
                        this.wait(2000);//miliseconds
                    }
                    catch(InterruptedException interrupt){
                        gameOutput=("Sorry, interrupt");
                    }
                }
                //acquired mutex

                CloudMafia.userid++;
                // TODO: ASSIGN ROLE
                //TODO: ADD NAME, USERID, ROLE TO ARRAY/DATABASE

                CloudMafia.mutex.release();
                //released mutex. Now another thread can access database.
                try {
                    this.wait();//waits until notifyAll, can be used to wait for all to reach same place
                }
                catch(InterruptedException interrupt){
                    gameOutput=("Sorry, interrupt");
                }
                // TODO: Wait for all to join>semaphore with size = # joiners? Possible?
                state = GOTNAME;
            }
            else{
                state=GAMESTART;
            }
        }
        else if (state ==GOTNAME){
            //TODO: Mafia kill, police guess, doctor heals
            //TODO: Civilians chill
            //TODO: Wait until stage is over, then unlock and move to next stage
            state =STEPONE;
        }
        else if (state ==STEPONE){
            //TODO: voting
            //TODO: Display results; show if sucessful or not
            //TODO: If all mafia dead, gameOutput=Game Over
            //TODO: else, set state back to gotname and restart
        }
        return gameOutput;
    }
}