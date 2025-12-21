package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;



public class game {

    private board[] boards;
    private int currentPlayer = 0;   // 0 = Player A, 1 = Player B

    private int  score = 0;

    private final int maxLives = 10;
    private int livesRemaining ;
    private boolean gameOver = false;

    private DifficultyLevel level;
    private String  player1Name;
    private String  player2Name;
    
    private MotivationManager motivationManager = new MotivationManager();
    
    private Set<Integer> usedQuestionIds = new HashSet<>();

    public game() {
        // default is easy 9x9 board with 10 mines
        this.level = DifficultyLevel.EASY;
        this.player1Name = "Player A";
        this.player2Name = "Player B";

        int rows = 9;
        int cols = 9;
        int mines = 10;
        
        livesRemaining = maxLives;

        boards = new board[2];
        boards[0] = new board(rows, cols, mines);
        boards[1] = new board(rows, cols, mines);
    }

    public game(DifficultyLevel level, String p1, String p2) {
        this.level = level;
        this.player1Name = p1;
        this.player2Name = p2;

        int rows = 0, cols = 0, mines = 0, surprises = 0;

        switch (level) {
            case EASY:
                rows = 9; cols = 9; mines = 10; surprises = 2; livesRemaining = 10;
                break;
            case MEDIUM:
                rows = 13; cols = 13; mines = 26; surprises = 3; livesRemaining = 8;
                break;
            case HARD:
                rows = 16; cols = 16; mines = 44; surprises = 4; livesRemaining = 6;
                break;
        }

        int questions = getQuestionCount(level);

        boards = new board[2];
        boards[0] = new board(rows, cols, mines, surprises, questions);
        boards[1] = new board(rows, cols, mines, surprises, questions);

        // ✅ IMPORTANT: enforce GLOBAL 50:50 across BOTH boards combined
        assignGlobalGoodBadForAllSurprises();
    }

    
    
    private int getQuestionCount(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 6;
            case MEDIUM: return 7;
            case HARD:   return 11;
            default:     return 6;
        }
    }

    /*
    private int getSurpriseCount(DifficultyLevel level) {
        switch (level) {
            case EASY: return 2;
            case MEDIUM: return 3;
            case HARD: default: return 4;
        }
    }*/
    
    public int getActivationCost() {
        switch (level) {
            case EASY: return 5;
            case MEDIUM: return 8;
            case HARD:
            default: return 12;
        }
    }

    
    public QuestionOutcome getQuestionOutcomeFromTable(boolean correct, String questionDifficulty) {

        Random rnd = new Random();
        boolean fiftyFifty = rnd.nextBoolean(); //returns:true 50% of the time false 50% of the time

        switch (level) {

            // =================================================
            // EASY GAME LEVEL
            // =================================================
            case EASY:

                if (correct) {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return new QuestionOutcome(+3, +1, false, false,
                                    "Correct! +3 points, +1 life");

                        case "medium":
                            return fiftyFifty
                                    ? new QuestionOutcome(+6, 0, false, false,
                                        "Correct! +6 points")
                                    : new QuestionOutcome(0, 0, true, false,
                                        "Correct! A mine was revealed");

                        case "hard":
                            return new QuestionOutcome(+10, 0, false, true,
                                    "Correct! +10 points and 3x3 revealed");

                        case "expert":
                            return new QuestionOutcome(+15, +2, false, false,
                                    "Correct! +15 points, +2 lives");
                    }
                } else {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return fiftyFifty
                                    ? new QuestionOutcome(-3, 0, false, false, "Wrong! -3 points")
                                    : new QuestionOutcome(0, 0, false, false, "Wrong! No effect");

                        case "medium":
                            return fiftyFifty
                                    ? new QuestionOutcome(-6, 0, false, false, "Wrong! -6 points")
                                    : new QuestionOutcome(0, 0, false, false, "Wrong! No effect");

                        case "hard":
                            return new QuestionOutcome(-10, 0, false, false,
                                    "Wrong! -10 points");

                        case "expert":
                            return new QuestionOutcome(-15, -1, false, false,
                                    "Wrong! -15 points, -1 life");
                    }
                }
                break;

            // =================================================
            // MEDIUM GAME LEVEL
            // =================================================
            case MEDIUM:

                if (correct) {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return new QuestionOutcome(+8, +1, false, false,
                                    "Correct! +8 points, +1 life");

                        case "medium":
                            return new QuestionOutcome(+10, +1, false, false,
                                    "Correct! +10 points, +1 life");

                        case "hard":
                            return new QuestionOutcome(+15, +1, false, false,
                                    "Correct! +15 points, +1 life");

                        case "expert":
                            return new QuestionOutcome(+20, +2, false, false,
                                    "Correct! +20 points, +2 lives");
                    }
                } else {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return new QuestionOutcome(-8, 0, false, false,
                                    "Wrong! -8 points");

                        case "medium":
                            return fiftyFifty
                                    ? new QuestionOutcome(-10, -1, false, false,
                                        "Wrong! -10 points, -1 life")
                                    : new QuestionOutcome(0, 0, false, false,
                                        "Wrong! No effect");

                        case "hard":
                            return new QuestionOutcome(-15, -1, false, false,
                                    "Wrong! -15 points, -1 life");

                        case "expert":
                            return fiftyFifty
                                    ? new QuestionOutcome(-20, -1, false, false,
                                        "Wrong! -20 points, -1 life")
                                    : new QuestionOutcome(-20, -2, false, false,
                                        "Wrong! -20 points, -2 lives");
                    }
                }
                break;

            // =================================================
            // HARD GAME LEVEL
            // =================================================
            case HARD:

                if (correct) {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return new QuestionOutcome(+10, +1, false, false,
                                    "Correct! +10 points, +1 life");

                        case "medium":
                            return fiftyFifty
                                    ? new QuestionOutcome(+15, +1, false, false,
                                        "Correct! +15 points, +1 life")
                                    : new QuestionOutcome(+15, +2, false, false,
                                        "Correct! +15 points, +2 lives");

                        case "hard":
                            return new QuestionOutcome(+20, +2, false, false,
                                    "Correct! +20 points, +2 lives");

                        case "expert":
                            return new QuestionOutcome(+40, +3, false, false,
                                    "Correct! +40 points, +3 lives");
                    }
                } else {
                    switch (questionDifficulty.toLowerCase()) {

                        case "easy":
                            return new QuestionOutcome(-10, -1, false, false,
                                    "Wrong! -10 points, -1 life");

                        case "medium":
                            return fiftyFifty
                                    ? new QuestionOutcome(-15, -1, false, false,
                                        "Wrong! -15 points, -1 life")
                                    : new QuestionOutcome(-15, -2, false, false,
                                        "Wrong! -15 points, -2 lives");

                        case "hard":
                            return new QuestionOutcome(-20, -2, false, false,
                                    "Wrong! -20 points, -2 lives");

                        case "expert":
                            return new QuestionOutcome(-40, -3, false, false,
                                    "Wrong! -40 points, -3 lives");
                    }
                }
                break;
        }

        return new QuestionOutcome(0, 0, false, false, "No effect");
    }

    
    public Question getUnusedRandomQuestion(QuestionBank bank) {
        Question q;
        int safety = 0;

        do {
            q = bank.getRandomQuestionForGameLevel(level);
            safety++;
        } while (q != null && usedQuestionIds.contains(q.getQuestionID()) && safety < 100);

        if (q != null) {
            usedQuestionIds.add(q.getQuestionID());
        }

        return q;
    }

   /* 
    private QuestionOutcome randomOrNothing(int pts, int lives, String msg) {
        boolean apply = new Random().nextBoolean();
        return apply
                ? new QuestionOutcome(pts, lives, false, false, msg)
                : new QuestionOutcome(0, 0, false, false, "No effect");
    }*/

    /**
     * GLOBAL 50:50 for the WHOLE GAME (both boards together).
     * Example MEDIUM: 3 surprises per board -> 6 total -> exactly 3 good + 3 bad.
     */
    private void assignGlobalGoodBadForAllSurprises() {
        List<int[]> positions = new ArrayList<>();

        // collect all surprise cells from BOTH boards
        for (int bi = 0; bi < 2; bi++) {
            board b = boards[bi];
            for (int r = 0; r < b.getRows(); r++) {
                for (int c = 0; c < b.getCols(); c++) {
                    if (b.isSurprise(r, c)) {
                        positions.add(new int[]{bi, r, c});
                    }
                }
            }
        }

        // total should be even: EASY 4, MEDIUM 6, HARD 8
        Collections.shuffle(positions, new Random());

        int goodCount = positions.size() / 2; // exactly half good

        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            int bi = pos[0], r = pos[1], c = pos[2];

            boolean good = (i < goodCount);
            boards[bi].setGoodSurprise(r, c, good); // board must expose this
        }
    }

    // getters
    public board getBoard(int index) {
        return boards[index];
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    public int getScore() {
        return score;
    }

    public DifficultyLevel getLevel() {
        return level;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    // game logic
    public void switchTurn() {
        currentPlayer = 1 - currentPlayer;
    }

    // ✅ allow negative score
    public void addToScore(int delta) {
        score += delta;
    }

    public void addLife(int amount) {
        livesRemaining = Math.min(maxLives, livesRemaining + amount);
    }

    public void loseLife() {
        if (livesRemaining > 0) {
            livesRemaining--;
            if (livesRemaining == 0) {
                gameOver = true;
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    // win check
    public boolean boardFinishedAllMines(int boardIndex) {
        board b = boards[boardIndex];
        return b.getRevealedOrFlaggedMinesCount() == b.getMinesNum();
    }

    
    public MotivationManager getMotivationManager() {
        return motivationManager;
    }
    
    public void addLifeWithOverflowToPoints(int amount) {

        int before = livesRemaining;
        livesRemaining = Math.min(maxLives, livesRemaining + amount);

        int gained = livesRemaining - before;
        int overflow = amount - gained;

        // convert overflow lives to points using activation cost
        if (overflow > 0) {
            addToScore(overflow * getActivationCost());
        }
    }
    
    public int addLifeIfPossibleOrReturnPenalty(int livesToAdd) {

        int penalty = 0;

        for (int i = 0; i < livesToAdd; i++) {
            if (livesRemaining < maxLives) {
                livesRemaining++;
            } else {
                penalty += getActivationCost();
            }
        }

        return penalty;
    }



   // public MotivationManager getMotivationManager() {
       //return motivationManager;
   // }


}
