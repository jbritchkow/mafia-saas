import java.util.HashMap;
import java.util.List;

//link thread id to role? Thread id as user id?

public class MafiaGame2 {
    private static final int GAMESTART = 0;
    private static final int ASKEDCODE = 1;
    private static final int GOTCODE = 2;
    private static final int ASKEDNAME = 3;
    private static final int GOTNAME = 4;
    private static final int STEPONE = 5;
    public static final int VOTED = 6;
    public static final int ROUNDN = 7;
    public static final int WAITING = 8;
    public static final int DONEWAIT = 9;
    private static final int MAFIALOOP = 10;
    private static final int POLICELOOP = 11;
    private static final int DOCTORLOOP = 12;
    private static final int CIVILIANLOOP = 13;


    private int state = GAMESTART;
    private String userName = "";
    private String role = "";
    private int id = 0;
    private MafiaSThread thread = null;
    private CloudMafia cloudMafia = null;

    public MafiaGame2(MafiaSThread thread) {
        //cloudMafia = mainThread;
        this.thread = thread;
    }

    public String getRole() {
        return role;
    }
    public int getPlayerId() {
        return id;
    }

    //private int code =0;
    public String processGame(String userInput) {
        //enter proper connection string
        //DatabaseHelper databaseHelper = new DatabaseHelper("something");
        //userName="";
        String gameOutput = "";
        //role="Civilian";
        //if(userInput!=null||userInput.equals("Game over")|| userInput.equals("exit"))gameOutput="Game over";
        if (state == GAMESTART) {
            return processGameStart(userInput);
        } else if (state == ASKEDCODE) {
            return processAskedCode(userInput);
        } else if (state == GOTCODE) {
            return processGotCode(userInput);
        } else if (state == ASKEDNAME) {
            return processAskedName(userInput);
        } else if (state == WAITING) {
            return processWaiting(userInput);
        } else if (state == DONEWAIT) {
            return processDoneWait(userInput);
        } else if (state == MAFIALOOP) {
            return processMafiaInput(userInput);
        } else if (state == POLICELOOP) {
            return processPoliceInput(userInput);
        } else if (state == DOCTORLOOP) {
            return processDoctorInput(userInput);
        } else if (state == CIVILIANLOOP) {
            return processCivilianInput(userInput);
       // } else if (state == GOTNAME) {
           // return processGotName(userInput);
        } else if (state == STEPONE) {
            return processStepOne(userInput);
        } else if (state == VOTED) {
            return processVoted(userInput);
        } else if (state == ROUNDN) {
            return processRoundN(userInput);
        }
        return "State broken";
    }

    private String processGameStart(String userInput) {
        state = ASKEDCODE;

        return "Welcome to SaaS Mafia! Please enter your game code.";
    }

    private String processAskedCode(String userInput) {
        try {
            int loccode = 0;
            if (userInput.equals("new")) {
                int code = Listener.playerCreateGame(this.thread);
                return "Your new game code is " + code + "\nPlease enter your game code.";
            } else {
                loccode = Integer.parseInt(userInput);//error forced me to do this
            }
            if (Listener.dbHelper.isActiveGame(loccode)) {
                //System.out.println(cloudMafia.threadcount + "");
                cloudMafia = Listener.playerJoinGame(loccode, this.thread);
                state = GOTCODE;

                while (!cloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        Thread.sleep(50);//milliseconds
                    }
                    catch (InterruptedException e) {
                        return "Sorry, interrupt";
                    }
                }
                //acquired mutex
                cloudMafia.threadcount++;

                cloudMafia.mutex.release();

                return "You have entered game " + cloudMafia.code + "! Hit enter to get started!";
            } else {
                state = ASKEDCODE;
                return "That is not a valid game. Please reenter your game code.";
            }
        }
        catch (NumberFormatException e){
            state = ASKEDCODE;
            return "That is not a valid game. Please reenter your game code.";
        }
    }

    private String processGotCode(String userInput) {
        state = ASKEDNAME;
        return "Please enter your name!";
    }

    private String processAskedName(String userInput) {
        if(userInput.indexOf(';') != -1){
            return "Names cannot include punctuation.";
        }
        if (!userInput.equals("")) {
            //TODO: SANITIZE INPUT (what does this mean? cant the user put in whatever name they want?)
            if (id == 0)
                id = cloudMafia.userid++;
            boolean database = cloudMafia.dbHelper.checkNameAndAddPlayerToGame(cloudMafia.code, id, userInput);
            if (!database) {
                return "That name is already taken. Please enter your name!";
            }

            userName = userInput;
            state = WAITING;
            return "Hello, " + userInput + "! Press enter to wait while others join the game.";
        } else {
            return "Please enter your name!";
        }
    }

    private String processWaiting(String userInput) {
        System.out.println("in waiting");

        if (cloudMafia.timeCheck()) {
            System.out.println("timecheck "+cloudMafia.timeCheck());
            if(userInput!=""){
                System.out.println("User input: "+userInput);
                cloudMafia.timeCheckgame(userInput);
                System.out.println("time check game: "+cloudMafia.timeCheckgame(userInput));
            }
            //checks to see if time has passed. spins.
            state = WAITING;
            return "Waiting... Hit enter after a few seconds to try again.";
        } else {
            state = DONEWAIT;
            return "Looks like the game has started! Hit enter to get going.";
        }
    }

    private String processDoneWait(String userInput) {
        String gameOutput = "";
        System.out.println("in donewait");
        boolean database = false;
        if(id==0){
            while(!database) database = cloudMafia.dbHelper.assignRoleToPlayer(cloudMafia.code, id, "Police");
            role="Police";
            //cloudMafia.mutex.release();
            state = POLICELOOP;
        }
        else if (id==1){
            while(!database) database = cloudMafia.dbHelper.assignRoleToPlayer(cloudMafia.code, id, "Doctor");
            role="Doctor";
            //cloudMafia.mutex.release();
            state = DOCTORLOOP;
        }
        else if(id==3||id==2){
            while(!database) database = cloudMafia.dbHelper.assignRoleToPlayer(cloudMafia.code, id, "Mafia");
            role="Mafia";
            //cloudMafia.mutex.release();
            state = MAFIALOOP;
        }
        else {
            while (!database) database = cloudMafia.dbHelper.assignRoleToPlayer(cloudMafia.code, id, "Civilian");
            role="Civilian";
            //cloudMafia.mutex.release();
            state = CIVILIANLOOP;
        }


        while (!cloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        cloudMafia.here++;
        cloudMafia.mutex.release();
        cloudMafia.here3=0;
        while (cloudMafia.here < cloudMafia.threadcount) {
            try {
                Thread.sleep(100);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        System.out.println(userName + "here pre database" + cloudMafia.here);
        gameOutput += "You are a " + role + ".";
        HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);

        //First step of this method complete: All data in the database. Now for second step.
        if (!role.equals("Civilian")) {
            for (int i = 0; i <= cloudMafia.userid; i++) {
                if (map.get(i) != null)
                    gameOutput += " " + map.get(i) + "; ";

            }
            if (role.equals("Police")) {
                gameOutput += " Take a guess. Who is in  he mafia? Enter their username.";
            } else if (role.equals("Doctor")) {
                gameOutput += " Quick, choose someone to save! Enter their username.";
            } else if (role.equals("Mafia")) {
                gameOutput += " Whose turn is it to die? Enter their username.";
                //cloudMafia.here2++;
                //System.out.println("Mafia increment here2: "+ cloudMafia.here2);
            }
            // System.out.println("released mutex "+userName);

        }

        if (role.equals("Civilian")) {
            gameOutput += " What is your quest?";
        }

        //}
        //System.out.println(cloudMafia.multithread.availablePermits()+" permits avail check2 "+userName + "here2 "+cloudMafia.here2);
     /*   while (cloudMafia.here2 != cloudMafia.threadcount) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        */
        //state = GOTNAME;
        return gameOutput;

    }

    private String processMafiaInput(String userInput) {
        if(userInput.indexOf(';') != -1){
            return "Names cannot include punctuation.";
        }

        boolean foundPlayer = false;
        HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);
        for (int i = 0; i <= cloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    foundPlayer = true;
                    thread.socketOutput("Sent vote");
                    cloudMafia.mafiaChat(id, i);
                }
            }
        }
        if (!foundPlayer)
            return "Not a valid player name";
        return "";
    }

    private String processPoliceInput(String userInput) {
        if(userInput.indexOf(';') != -1){
            return "Names cannot include punctuation.";
        }

        String gameOutput = "";
        boolean foundPlayer = false;
        HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);

        for (int i = 0; i <= cloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    String playerRole = cloudMafia.dbHelper.getPlayerRole(cloudMafia.code, i);
                    if (playerRole.equals("Mafia")) {
                        gameOutput = "Don't tell anyone I said this, but... yeah, " + userInput + " is in the mafia.";
                    } else {
                        gameOutput = "Sorry, that player is not in the mafia.";
                    }
                    foundPlayer = true;
                }
            }
        }
        if (!foundPlayer) {
            gameOutput = "That is not a name of a player. Type the name of a player";
        } else {
            gameOutput += "\nClick enter to answer random questions.";
            state = CIVILIANLOOP;
            cloudMafia.finishAbilitiesStage();
        }
        return gameOutput;
    }

    private String processDoctorInput(String userInput) {
        if(userInput.indexOf(';') != -1){
            return "Names cannot include punctuation.";
        }

        boolean foundPlayer = false;
        HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);
        for (int i = 0; i <= cloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    cloudMafia.dbHelper.assignStateToPlayer(cloudMafia.code, i,"HEALED");
                    foundPlayer = true;
                }
            }
        }
        if (!foundPlayer) {
            return "That is not a name of a player. Type the name of a player";
        } else {
            state = CIVILIANLOOP;

            cloudMafia.finishAbilitiesStage();
            return "You have healed a player.\nClick enter to answer random questions.";
        }
    }

    private String processCivilianInput(String userInput) {

        return "I want to ask you a random question.";
    }

    public void endVoting() {
        /*while(cloudMafia.here2 != cloudMafia.threadcount) {
            try {
                Thread.sleep(200);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                System.out.println ("Sorry, interrupt");
            }
        }*/
        System.out.println(userName + " is moving to stepone");
        state = STEPONE;
    }
    public void endMafiaVoting() {
        state = CIVILIANLOOP;
    }

    private String processStepOne(String userInput) {
        String gameOutput = "";
        cloudMafia.processMafiaAttack();

        while (!cloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }

        cloudMafia.here2++;
        cloudMafia.mutex.release();
        cloudMafia.here4=0;
        System.out.println("who is pre here2: " + cloudMafia.here + " " + userName);
        while (cloudMafia.here2 < cloudMafia.threadcount) {
            try {
                Thread.sleep(50);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        String playerState = cloudMafia.dbHelper.getPlayerState(cloudMafia.code, id);
        if (DatabaseHelper.States.DEAD.equalsState(playerState)) {
            while (!cloudMafia.mutex.tryAcquire()) {
                try {
                    Thread.sleep(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    System.out.println ("Sorry, interrupt");
                }
            }
            cloudMafia.threadcount--;
            if(role.equals("Doctor")||role.equals("Police")){
                cloudMafia.livingAbilities--;
            }
            cloudMafia.mutex.release();
            return "Game over";
        }
        else {
            HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);
            for (int i = 0; i <= cloudMafia.userid; i++) {
                if (map.get(i) != null)
                    gameOutput += " " + map.get(i) + "; ";

            }
            gameOutput += "So. Who is in the mafia?";
        }

        System.out.println("who is post here2: " + cloudMafia.here + " " + userName);
        state = VOTED;
        return gameOutput;
    }

    private String processVoted(String userInput) {
        if(userInput.indexOf(';') != -1){
            return "Names cannot include punctuation.";
        }

        boolean foundPlayer = false;
        String gameOutput = "";
        HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);

        while (!cloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(50);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        if (!userInput.equals("")) {
            System.out.println(cloudMafia.userid);
            for (int i = 0; i < cloudMafia.userid; i++) {
                System.out.println(""+i+ " has "+cloudMafia.votes[i]);
                if (map.get(i) != null) {
                    if (userInput.equals(map.get(i))) {
                        foundPlayer = true;
                        System.out.println(map.get(i));
                        cloudMafia.votes[i] += 1;
                        System.out.println("in vote loop" + cloudMafia.votes[i] + "" + i);
                    }
                }
            }
        }
        if (!foundPlayer) {
            cloudMafia.mutex.release();
            return "That is not a name of a player. Type the name of a player";
        }


        cloudMafia.here3++;
        cloudMafia.mutex.release();
        cloudMafia.here=0;

        //Wait until all threads have finished this step to display results!!!
        System.out.println("looks like here2: " + cloudMafia.here3 + " " + userName);
        while (cloudMafia.here3 < cloudMafia.threadcount) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        int max = 0;
        int mafia = 0;
        for (int i = 0; i < cloudMafia.votes.length; i++) {
            if (cloudMafia.votes[i] > max) {
                max = cloudMafia.votes[i];
                mafia = i;
            }
        }
        cloudMafia.processVotingState(mafia);
        /*gameOutput = "You voted that " + map.get(mafia) + " was in the mafia!";
        cloudMafia.dbHelper.assignStateToPlayer(cloudMafia.code, mafia, "DEAD");
        if(cloudMafia.dbHelper.getPlayerRole(cloudMafia.code, mafia).equals("Mafia")){
            cloudMafia.gameOverCondition++;
            gameOutput+=" And you were right!";
        }
        else gameOutput+=" Unfortunately, they were not.";*/
        System.out.println("hassend voting messages"+ userName);
        while (!cloudMafia.hasSendVotingMessages) {
            try {
                Thread.sleep(50);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        System.out.println("after send voting msgs "+userName);

        // TODO: Display results; show if sucessful or not
        //TODO: If all mafia dead, gameOutput=Game Over
        //TODO: else, set state back to roundn and restart
        //done: if this thread is dead, ouput=game over

        //gameOutput="Looks like the mafia is still out there! Hit enter to start another round.";
        //cloudMafia.multithread.release();
        state = ROUNDN;
        return gameOutput;
    }

    private String processRoundN(String userInput) {
        System.out.println("in roundn "+userName);
        boolean reset =false;
        for (int i = 0; i < cloudMafia.votes.length; i++) {
            cloudMafia.votes[i]=0;
        }
        while(!reset) reset=cloudMafia.dbHelper.resetPlayerStatesForNextTurn(cloudMafia.code);
        String playerstate =cloudMafia.dbHelper.getPlayerState(cloudMafia.code, id);
        cloudMafia.hasSendVotingMessages=false;
        if(cloudMafia.dbHelper.isMafiaOnlyRemaining(cloudMafia.code)){
            return "Game Over, mafia wins";
        }
        if(playerstate.equals("LIVING")&&cloudMafia.gameOverCondition<2) {
            //From here, same as earlier step.
            String gameOutput = "";
            while (!cloudMafia.mutex.tryAcquire()) {
                try {
                    Thread.sleep(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
            HashMap<Integer, String> map = cloudMafia.dbHelper.getLivingPlayers(cloudMafia.code);
            for (int i = 0; i <= cloudMafia.userid; i++) {
                if (map.get(i) != null) {
                    gameOutput += " " + map.get(i) + "; ";

                }

            }
            cloudMafia.resetTurnCounters();
            //acquired mutex
            if (role.equals("Police")) {
                gameOutput += "Take a guess. Who is in the mafia?";
                state=POLICELOOP;
            } else if (role.equals("Doctor")) {
                gameOutput += "Quick, choose someone to save!";
                state=DOCTORLOOP;
            } else if (role.equals("Mafia")) {
                gameOutput += "Whose turn is it to die?";
                state=MAFIALOOP;
            }
            cloudMafia.here4++;
            cloudMafia.mutex.release();
            cloudMafia.here2=0;

            //released mutex. Now another thread can access database.

            if (role.equals("Civilian")) {
                gameOutput += "What is your favorite food?";//hardcoded for now. fix later.
                state=CIVILIANLOOP;
            }
System.out.println("RoundN here4 "+cloudMafia.here4+ " "+userName);
            while (cloudMafia.here4 < cloudMafia.threadcount) {
                try {
                    Thread.sleep(500);
                    //this.wait(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
            //if(cloudMafia.multithread.availablePermits()==0){
            //  cloudMafia.multithread.release();
            //}
            //state = GOTNAME;
            cloudMafia.here3=0;
            return gameOutput;
        }
        else if (cloudMafia.gameOverCondition == 2){
            while (!cloudMafia.mutex.tryAcquire()) {
                try {
                    Thread.sleep(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    System.out.println ("Sorry, interrupt");
                }
            }
            cloudMafia.threadcount--;
            if(role.equals("Doctor")||role.equals("Police")){
                cloudMafia.livingAbilities--;
            }
            cloudMafia.mutex.release();
            return "Game Over, civilians win!";
        }
        else{
            while (!cloudMafia.mutex.tryAcquire()) {
                try {
                    Thread.sleep(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    System.out.println ("Sorry, interrupt");
                }
            }
            cloudMafia.threadcount--;
            if(role.equals("Doctor")||role.equals("Police")){
                cloudMafia.livingAbilities--;
            }
            cloudMafia.mutex.release();
            return "Game over";
        }
    }
}