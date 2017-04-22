import java.net.*;
import java.io.*;

public class MafiaGame {
    private static final int GAMESTART = 0;
    private static final int ASKEDNAME = 1;
    private static final int GOTNAME = 2;

    public String processGame(String userInput){
        String gameOutput = null;
        if(state==GAMESTART){
            gameOutput="Welcome to SaaS Mafia! Please enter your name.";
        }
        else if (state == ASKEDNAME){
            if(userInput!=""){
                //TODO: ADD NAME TO ARRAY/DATABASE
                //TODO: ASSIGN ROLE
                //TODO: SANITIZE INPUT
                state = GOTNAME;
            }
            else{
                state==GAMESTART;
            }
        }
    }
}