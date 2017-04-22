import java.net.*;
import java.io.*;

public class MafiaGame {
    private static final int GAMESTART = 0;
    private static final int ASKEDCODE = 1;
    private static final int ASKEDNAME = 2;
    private static final int GOTNAME = 3;
    private static final int STEPONE=4;

    private int state =GAMESTART;

    //private int code =0;
    public String processGame(String userInput){
        String gameOutput = null;
        if(state==GAMESTART){
            gameOutput="Welcome to SaaS Mafia! Please enter your name.";
        }
        else if (state == ASKEDCODE){
            if(Integer.parseInt(userInput)==CloudMafia.code){
                //TODO: SANITIZE INPUT
                gameOutput="You have entered game " +CloudMafia.code + "!";
            }
            else{
                gameOutput="That is not a valid game.";
                state = GAMESTART;
            }
        }
        else if (state ==ASKEDNAME){
            if(userInput!=""){
                //TODO: ADD NAME TO ARRAY/DATABASE
                //TODO: ASSIGN ROLE
                //TODO: SANITIZE INPUT
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