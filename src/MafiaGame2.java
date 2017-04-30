import java.util.HashMap;

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

    public String getRole() {
        return role;
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
        } else if (state == GOTNAME) {
            return processGotName(userInput);
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
        //TODO: SANITIZE INPUT
        int loccode = Integer.parseInt(userInput);//error forced me to do this
        if (loccode == CloudMafia.code) {
            while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                try {
                    Thread.sleep(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    return "Sorry, interrupt";
                }
            }
            //acquired mutex
            CloudMafia.threadcount++;

            CloudMafia.mutex.release();
            System.out.println(CloudMafia.threadcount + "");
            state = GOTCODE;
            return "You have entered game " + CloudMafia.code + "! Hit enter to get started!";
        } else {
            state = ASKEDCODE;
            return "That is not a valid game. Please reenter your game code.";
        }
    }

    private String processGotCode(String userInput) {
        state = ASKEDNAME;
        return "Please enter your name!";
    }

    private String processAskedName(String userInput) {
        if (!userInput.equals("")) {
            //TODO: SANITIZE INPUT
            userName = userInput;
            state = WAITING;
            return "Hello, " + userInput + "! Press enter to wait while others join the game.";
        } else {
            return "Please enter your name!";
        }
    }

    private String processWaiting(String userInput) {
        System.out.println("in waiting");

        if (CloudMafia.timeCheck()) {
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
        while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        id = CloudMafia.userid++;
        boolean database = CloudMafia.dbHelper.checkNameAndAddPlayerToGame(CloudMafia.code, id, userName);
        int n = 0;
        while (!database) {
            n++;
            database = CloudMafia.dbHelper.checkNameAndAddPlayerToGame(CloudMafia.code, id, userName + "" + n);
        }
        if (n > 0) {
            gameOutput = "Sorry, that name was taken. You are now " + userName + "" + n + ".";
            userName = userName + "" + n;
        }
        database = false;
        if(id==1){
            while(!database) database = CloudMafia.dbHelper.assignRoleToPlayer(CloudMafia.code, id, "Police");
            role="Police";
            //CloudMafia.mutex.release();
            state = POLICELOOP;
        }
        else if (id==2){
            while(!database) database = CloudMafia.dbHelper.assignRoleToPlayer(CloudMafia.code, id, "Doctor");
            role="Doctor";
            //CloudMafia.mutex.release();
            state = DOCTORLOOP;
        }
        else if(id==3||id==4){
            while(!database) database = CloudMafia.dbHelper.assignRoleToPlayer(CloudMafia.code, id, "Mafia");
            role="Mafia";
            //CloudMafia.mutex.release();
            state = MAFIALOOP;
        }
        else {
            while (!database) database = CloudMafia.dbHelper.assignRoleToPlayer(CloudMafia.code, id, "Civilian");
            role="Civilian";
            //CloudMafia.mutex.release();
            state = CIVILIANLOOP;
        }


        //TODO: ASSIGN ROLE
        CloudMafia.here++;
        CloudMafia.mutex.release();

        while (CloudMafia.here != CloudMafia.threadcount) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        System.out.println(userName + "here pre database" + CloudMafia.here);
        gameOutput += "You are a " + role + ".";

        //First step of this method complete: All data in the database. Now for second step.
        while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        if (!role.equals("Civilian")) {
            HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
            for (int i = 0; i <= CloudMafia.userid; i++) {
                if (map.get(i) != null)
                    gameOutput += " " + map.get(i) + "; ";

            }
            if (role.equals("Police")) {
                gameOutput += " Take a guess. Who is in the mafia? Enter their username.";
            } else if (role.equals("Doctor")) {
                gameOutput += " Quick, choose someone to save! Enter their username.";
            } else if (role.equals("Mafia")) {
                gameOutput += " Whose turn is it to die? Enter their username.";
                CloudMafia.here2++;
                System.out.println("Mafia increment here2: "+ CloudMafia.here2);
            }
            // System.out.println("released mutex "+userName);

        }
        CloudMafia.here2++;
        CloudMafia.mutex.release();
        if (role.equals("Civilian")) {
            gameOutput += " What is your quest?";

                /*while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
                    try {
                        Thread.sleep(500);
                        //this.wait(500);//milliseconds
                    } catch (InterruptedException interrupt) {
                        System.out.println("Sorry, interrupt");
                    }
                }
                CloudMafia.here2++;
                CloudMafia.mutex.release();*/
            //System.out.println("Civilian increment here2: "+CloudMafia.here2);
        }

        //}
        //System.out.println(CloudMafia.multithread.availablePermits()+" permits avail check2 "+userName + "here2 "+CloudMafia.here2);
     /*   while (CloudMafia.here2 != CloudMafia.threadcount) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        */
        //state = GOTNAME;
        //DEAD CODE
        return gameOutput;

    }

    private String processMafiaInput(String userInput) {

        HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
        for (int i = 0; i <= CloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    //if(CloudMafia.gameOverCondition<2) { //probably going to need mutex here if keep this in
                     //   String playerStatus=CloudMafia.dbHelper.getPlayerState(CloudMafia.code,i);
                     //   if(playerStatus.equals("LIVING"))
                     //       CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, i,"MARKED");
                    //}
                    //else {
                        CloudMafia.mafiaChat(id, i);
                    //}
                }
            }
        }
        return "Sent vote";
    }

    private String processPoliceInput(String userInput) {
        String gameOutput = "";
        boolean foundPlayer = false;
        HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);

        //TODO: Compare user input to database
        //TODO: What do if user inputs bad input ie a name that is not a name?
        for (int i = 0; i <= CloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    String playerRole = CloudMafia.dbHelper.getPlayerRole(CloudMafia.code, i);
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
            CloudMafia.finishAbilitiesStage();
        }
        return gameOutput;
    }

    private String processDoctorInput(String userInput) {
        boolean foundPlayer = false;
        HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
        for (int i = 0; i <= CloudMafia.userid; i++) {
            if (map.get(i) != null) {
                if (userInput.equals(map.get(i))) {
                    CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, i,"HEALED");
                    foundPlayer = true;
                }
            }
        }
        if (!foundPlayer) {
            return "That is not a name of a player. Type the name of a player";
        } else {
            state = CIVILIANLOOP;

            CloudMafia.finishAbilitiesStage();
            return "You have healed a player.\nClick enter to answer random questions.";
        }
    }

    private String processCivilianInput(String userInput) {

        return "I want to ask you a random question.";
    }

    public void endVoting() {
        /*while(CloudMafia.here2 != CloudMafia.threadcount) {
            try {
                Thread.sleep(200);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                System.out.println ("Sorry, interrupt");
            }
        }*/
        state = STEPONE;
    }
    public void endMafiaVoting() {
        state = CIVILIANLOOP;
    }

    private String processGotName(String userInput) {
        String gameOutput = "";

        while (!CloudMafia.mutex.tryAcquire()) {
            try {
                Thread.sleep(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        if (userInput != "" && !role.equals("Civilian")) {
            HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);

            if (role.equals("Police")) {
                //TODO: Compare user input to database
                //TODO: What do if user inputs bad input ie a name that is not a name?
                for (int i = 0; i <= CloudMafia.userid; i++) {
                    if (map.get(i) != null) {
                        if (userInput.equals(map.get(i))) {
                            String playerRole = CloudMafia.dbHelper.getPlayerRole(CloudMafia.code, i);
                            if (playerRole == "Mafia") {
                                gameOutput = "Don't tell anyone I said this, but... yeah, " + userInput + " is in the mafia.";
                            }
                        } else {
                            gameOutput = "Sorry, that player is not in the mafia.";
                        }
                    }
                }
            }
            if (role.equals("Doctor")) {
                for (int i = 0; i <= CloudMafia.userid; i++) {
                    if (map.get(i) != null) {
                        if (userInput.equals(map.get(i))) {
                            CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, i,"HEALED");
                        }
                    }
                }

                //TODO: see police
            }
            if (role.equals("Mafia")) {
                if(CloudMafia.gameOverCondition==0) {
                    //TODO: find other mafia member
                    //TODO: mafia communicate and choose someone to kill
                }

                for (int i = 0; i <= CloudMafia.userid; i++) {
                    if (map.get(i) != null) {
                        if (userInput.equals(map.get(i))) {
                            String playerState=CloudMafia.dbHelper.getPlayerState(CloudMafia.code,i);
                            if(playerState.equals("LIVING"))
                            CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, i,"MARKED");
                        }
                    }
                }
                //we can always back down to one mafia member if this turns out to be too much
            }


        }
        CloudMafia.here--;
        CloudMafia.mutex.release();
        System.out.println("fave color here: " + CloudMafia.here + " " + userName);
        if (role.equals("Civilian")) {
            gameOutput = "What is your favorite color?";
        }
        while (CloudMafia.here != 0) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        state = STEPONE;
        return gameOutput;
    }

    private String processStepOne(String userInput) {
        String gameOutput = "";
        while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }

        CloudMafia.here--;
        CloudMafia.mutex.release();
        System.out.println("who is pre here: " + CloudMafia.here + " " + userName);
        gameOutput = ("So. Who is in the mafia?");
        while (CloudMafia.here != 0) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }

        System.out.println("who is post here2: " + CloudMafia.here + " " + userName);
        state = VOTED;
        return gameOutput;
    }

    private String processVoted(String userInput) {
        String gameOutput = "";
        while (!CloudMafia.mutex.tryAcquire()) { //readwritelock?
            try {
                Thread.sleep(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
        if (userInput != "") {
            //TODO: sanitize input

            for (int i = 0; i <= CloudMafia.userid; i++) {
                if (map.get(i) != null)
                    if (userInput.equals(map.get(i))) {
                        CloudMafia.votes[i] += 1;
                        System.out.println("in vote loop" + CloudMafia.votes[i] + "" + i);
                    }

            }
            //TODO: sum votes across threads
        }

        CloudMafia.here2--;
        CloudMafia.mutex.release();


        //Wait until all threads have finished this step to display results!!!
        System.out.println("looks like here: " + CloudMafia.here2 + " " + userName);
        while (CloudMafia.here2 != 0) {
            try {
                Thread.sleep(500);
                //this.wait(500);//milliseconds
            } catch (InterruptedException interrupt) {
                gameOutput = ("Sorry, interrupt");
            }
        }
        int max = 0;
        int mafia = 0;
        for (int i = 0; i < CloudMafia.votes.length; i++) {
            if (CloudMafia.votes[i] > max) {
                max = CloudMafia.votes[i];
                mafia = i;
            }
        }
        gameOutput = "You voted that " + map.get(mafia) + " was in the mafia!";
        CloudMafia.dbHelper.assignStateToPlayer(CloudMafia.code, mafia, "DEAD");
        if(CloudMafia.dbHelper.getPlayerRole(CloudMafia.code, mafia).equals("Mafia")){
            CloudMafia.gameOverCondition++;
            gameOutput+=" And you were right!";
        }
        else gameOutput+=" Unfortunately, they were not.";
        // TODO: Display results; show if sucessful or not
        //TODO: If all mafia dead, gameOutput=Game Over
        //TODO: else, set state back to roundn and restart
        //done: if this thread is dead, ouput=game over

        //gameOutput="Looks like the mafia is still out there! Hit enter to start another round.";
        //CloudMafia.multithread.release();
        state = ROUNDN;
        return gameOutput;
    }

    private String processRoundN(String userInput) {
        boolean reset =false;
        while(!reset) reset=CloudMafia.dbHelper.resetPlayerStatesForNextTurn(CloudMafia.code);
        String playerstate =CloudMafia.dbHelper.getPlayerState(CloudMafia.code, id);
        if(playerstate.equals("LIVING")&&CloudMafia.gameOverCondition<2) {
            String gameOutput = "";
            while (!CloudMafia.mutex.tryAcquire()) {
                try {
                    this.wait(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
            HashMap<Integer, String> map = CloudMafia.dbHelper.getLivingPlayers(CloudMafia.code);
            for (int i = 0; i <= CloudMafia.userid; i++) {
                if (map.get(i) != null)
                    gameOutput += " " + map.get(i) + "; ";

            }
            //acquired mutex
            if (role.equals("Police")) {
                gameOutput = "Take a guess. Who is in the mafia?";
            } else if (role.equals("Doctor")) {
                gameOutput = "Quick, choose someone to save!";
            } else if (role.equals("Mafia")) {
                gameOutput = "Whose turn is it to die?";
            }
            CloudMafia.here++;
            CloudMafia.mutex.release();
            //released mutex. Now another thread can access database.

            if (role.equals("Civilian")) {
                gameOutput = "What is your favorite food?";//hardcoded for now. fix later.
            }

            while (CloudMafia.here != CloudMafia.threadcount) {
                try {
                    Thread.sleep(500);
                    //this.wait(500);//milliseconds
                } catch (InterruptedException interrupt) {
                    gameOutput = ("Sorry, interrupt");
                }
            }
            //if(CloudMafia.multithread.availablePermits()==0){
            //  CloudMafia.multithread.release();
            //}
            state = GOTNAME;
            return gameOutput;
        }
        else{
            return "Game Over";
        }
    }
}