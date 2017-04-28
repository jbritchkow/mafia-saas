import java.net.*;
import java.io.*;
import java.util.HashMap;

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
    private String userName="";
    private String role="";
    //private int code =0;
    public String processGame(String userInput){
        //enter proper connection string
        //DatabaseHelper databaseHelper = new DatabaseHelper("something");
        //userName="";
        String gameOutput = "";
        role="Civilian";
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
                gameOutput="That is not a valid game. Please reenter your game code.";
                state = ASKEDCODE;
            }
        }
        else if (state == GOTCODE){
            gameOutput="Please enter your name!";
                state=ASKEDNAME;

        }
        else if (state ==ASKEDNAME) {
            if (userInput != "") {
                //TODO: SANITIZE INPUT
                gameOutput = "Hello, " + userInput + "! Press enter to wait while others join the game.";
                userName=userInput;
                while(!CloudMafia.mutex.tryAcquire()){ //readwritelock?
                    try {
                        this.wait(100);//milliseconds
                    }
                    catch(InterruptedException interrupt){
                        gameOutput=("Sorry, interrupt");
                    }
                }
                //acquired mutex
                CloudMafia.threadcount++;
                CloudMafia.mutex.release();
                System.out.println(CloudMafia.threadcount+"");
                state=WAITING;
            }
            else{
                state=GOTCODE;
            }
        }
        else if(state==WAITING) {
            System.out.println("in waiting");

            if (CloudMafia.timeCheckgame()) {
                //checks to see if time has passed. spins.
                gameOutput="Waiting... Press enter after a few seconds to try again.";
                state=WAITING;
            }
            else {
                gameOutput="Looks like the game has started! Hit enter to get going.";
                state = DONEWAIT;
            }
        }
        else if (state==DONEWAIT){
            System.out.println("in donewait");
            System.out.println(CloudMafia.multithread.availablePermits() +" permits start dunwait " + userName);
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail after first get "+userName);
                while(!CloudMafia.mutex.tryAcquire()){ //readwritelock?
                try {
                    this.wait(1000);//milliseconds
                }
                catch(InterruptedException interrupt){
                    gameOutput=("Sorry, interrupt");
                }
            }
            System.out.println(userName+" Got mutex " + CloudMafia.mutex.availablePermits());
            //acquired mutex

            CloudMafia.userid++;

            /*boolean database =databaseHelper.checkNameAndAddPlayerToGame(CloudMafia.code, CloudMafia.userid, userName);
            int n=0;
            while(!database){
                n++;
                database =databaseHelper.checkNameAndAddPlayerToGame(CloudMafia.code, CloudMafia.userid, userName+""+n);

            }
            if(n>0){
                gameOutput="Sorry, that name was taken. You are now "+userName+""+n+".\n";
                userName=userName+""+n;
            }
            */
            //TODO: ASSIGN ROLE
            //threadid as userid????? How else to attach thread/user?
            CloudMafia.here++;
            CloudMafia.mutex.release();
            System.out.println(userName+"released mutex"+CloudMafia.mutex.availablePermits());
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail check1 "+userName + "here"+CloudMafia.here);

            while(CloudMafia.here!=CloudMafia.threadcount){
            }

            //might be a race condition. May need mutex.
            System.out.println(CloudMafia.multithread.availablePermits()+" permits availpostcheck1 "+userName);
            //if(CloudMafia.multithread.availablePermits()==0){
                CloudMafia.multithread.release();
            //}
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail after release1 "+userName);
            gameOutput += "You are a " + role + ".\n";
            //First step of this method complete: All data in the database. Now for second step.
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail after get2 "+userName);
        if(!role.equals("Civilian")) {
            while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                try {
                    this.wait(1000);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
            System.out.println("got mutex "+userName);
            /*HashMap<Integer, String> map =databaseHelper.getLivingPlayers(CloudMafia.code);
            for(int i=0; i<CloudMafia.userid; i++){
                gameOutput+=" "+map.get(i)+"\n";
            }*/
            if (role.equals("Police")) {
                gameOutput += " Take a guess. Who is in the mafia? Enter their username.";
            } else if (role.equals("Doctor")) {
                gameOutput += " Quick, choose someone to save! Enter their username.";
            } else if (role.equals("Mafia")) {
                gameOutput += " Whose turn is it to die? Enter their username.";
            }
            System.out.println("released mutex "+userName);
            CloudMafia.here2++;
            CloudMafia.mutex.release();
        }
            //released mutex. Now another thread can access database.
            //CloudMafia.threadCount++
                if(role.equals("Civilian")){//hardcoded for now. fix later.
                gameOutput+=" What is your quest?";
                }

            //}
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail check2 "+userName);
            while(CloudMafia.here2!=CloudMafia.threadcount){
            }
            //might be a race condition. May need mutex.
            //if(CloudMafia.multithread.availablePermits()==0){
                CloudMafia.multithread.release();
            //}
            System.out.println(CloudMafia.multithread.availablePermits()+" permits avail postrelease2 "+userName);
            state = GOTNAME;
        }


        else if (state ==GOTNAME){
            CloudMafia.here=0;
           // Object obj=new Object();
           // try {
           //     obj.wait();//waits until notifyAll, can be used to wait for all to reach same place
           // }
           // catch(InterruptedException interrupt){
            //    gameOutput=("Sorry, interrupt");
           // }
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }
            if(userInput!=""&&!role.equals("Civilian")) {
                while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        this.wait(1000);//milliseconds
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
            while(CloudMafia.multithread.availablePermits()!=0){

            }
            if(CloudMafia.multithread.availablePermits()==0){
                CloudMafia.multithread.release(CloudMafia.threadcount);
            }
            //TODO: Wait until stage is over, then unlock and move to next stage
            state =STEPONE;
        }
        else if (state ==STEPONE) {
            gameOutput = ("Who is in the mafia?");
            state = VOTED;
        }
        else if (state == VOTED){
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }

            if(userInput!="") {
                //TODO: sanitize input

                while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        this.wait(1000);//milliseconds
                    } catch (InterruptedException interrupt) {
                        gameOutput = ("Sorry, interrupt");
                    }
                }
                //TODO: sum votes across threads
                //arraylist.add for votes? 2d array with 0s if no votes?
                //TODO: Write results to database
                CloudMafia.mutex.release();
                //Wait until all threads have finished this step to display results!!!
                while(CloudMafia.multithread.availablePermits()!=0){
                }
                if(CloudMafia.multithread.availablePermits()==0){
                    CloudMafia.multithread.release(CloudMafia.threadcount);
                }

                //TODO: Display results; show if sucessful or not
                //TODO: If all mafia dead, gameOutput=Game Over
                //TODO: else, set state back to gotname and restart
                //TODO: if this thread is dead, ouput=game over
                gameOutput="Looks like the mafia is still out there! Hit enter to start another round.";
                state=GOTNAME;
            }
        }
        else if (state==ROUNDN){
            try {
                CloudMafia.multithread.acquire();
            }
            catch(InterruptedException e){
                gameOutput="stupid semaphores";
            }

            while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                try {
                    this.wait(1000);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
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
            CloudMafia.mutex.release();
//released mutex. Now another thread can access database.

            if(role.equals("Civilian")){
                gameOutput="What is your favorite food?";//hardcoded for now. fix later.
            }

            while(CloudMafia.multithread.availablePermits()!=0){

            }
            if(CloudMafia.multithread.availablePermits()==0){
                CloudMafia.multithread.release(CloudMafia.threadcount);
            }
            state=GOTNAME;
        }
        return gameOutput;
    }
}