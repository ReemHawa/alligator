package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

import model.factory.BoardFactory;
import model.factory.BoardFactoryProvider;
////new changes
public class game {

    private board[] boards;
    private int currentPlayer = 0;   // 0 = Player A, 1 = Player B

    private int score = 0;

    private final int maxLives = 10;
    private int livesRemaining;
    private boolean gameOver = false;

    private DifficultyLevel level;
    private String player1Name;
    private String player2Name;

    private MotivationManager motivationManager = new MotivationManager();

    // Question system (optional, only used if you still use QuestionBank somewhere)
    private Set<Integer> usedQuestionIds = new HashSet<>();

    private boolean livesConverted = false;

    // ==========================================================
    //  DEFAULT CONSTRUCTOR (must also build boards!)
    // ==========================================================
    public game() {
        this.level = DifficultyLevel.EASY;
        this.player1Name = "Player A";
        this.player2Name = "Player B";

        //  Factory Method usage
        BoardFactory factory = BoardFactoryProvider.getFactory(this.level);

        // Lives come from difficulty config
        livesRemaining = factory.getStartingLives();

        boards = new board[2];

        //  always use the constructor with QUESTIONS (handled inside factory)
        boards[0] = factory.createBoard();
        boards[1] = factory.createBoard();

        // keep surprises balanced
        assignGlobalGoodBadForAllSurprises();
    }

    // ==========================================================
    //  MAIN GAME CONSTRUCTOR
    // ==========================================================
    public game(DifficultyLevel level, String p1, String p2) {
        this.level = level;
        this.player1Name = p1;
        this.player2Name = p2;

        //  Factory Method usage
        BoardFactory factory = BoardFactoryProvider.getFactory(this.level);

        // Lives come from difficulty config
        livesRemaining = factory.getStartingLives();

        boards = new board[2];

        //  IMPORTANT: always use the constructor with QUESTIONS (handled inside factory)
        boards[0] = factory.createBoard();
        boards[1] = factory.createBoard();

        //  IMPORTANT: enforce GLOBAL 50:50 across BOTH boards combined
        assignGlobalGoodBadForAllSurprises();
    }

    // ==========================================================
    // QUESTIONS COUNT BY GAME LEVEL
    // (kept for compatibility; not used anymore because factory holds it)
    // ==========================================================
    private int getQuestionCount(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 6;
            case MEDIUM: return 7;
            case HARD:   return 11;
            default:     return 6;
        }
    }

    // ==========================================================
    // ACTIVATION COST BY GAME LEVEL
    // ==========================================================
    public int getActivationCost() {
        switch (level) {
            case EASY: return 5;
            case MEDIUM: return 8;
            case HARD:
            default: return 12;
        }
    }

    public int convertRemainingLivesToPointsOnce() {
        if (livesConverted) return 0;

        int bonus = livesRemaining * getActivationCost();
        score += bonus;

        livesRemaining = 0;
        livesConverted = true;

        return bonus;
    }

    // ==========================================================
    // QUESTION OUTCOME TABLE
    // ==========================================================
    public QuestionOutcome getQuestionOutcomeFromTable(boolean correct, String questionDifficulty) {

        Random rnd = new Random();
        boolean fiftyFifty = rnd.nextBoolean();

        switch (level) {

            case EASY:
                if (correct) {
                    switch (questionDifficulty.toLowerCase()) {
                        case "easy":
                            return new QuestionOutcome(+3, +1, false, false,
                                    "Correct! +3 points, +1 life");
                        case "medium":
                  
                            return new QuestionOutcome(
                                    +6,          // pointsDelta
                                    0,           // livesDelta
                                    true,        // revealOneMine ✅ תמיד
                                    false,       // reveal3x3Random
                                    "Correct! +6 points and a mine was revealed"
                            );

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
            q = bank.getRandomQuestion();   // ✅ updated
            safety++;
        } while (q != null && usedQuestionIds.contains(q.getQuestionID()) && safety < 100);

        if (q != null) {
            usedQuestionIds.add(q.getQuestionID());
        }

        return q;
    }

    /**
     * GLOBAL 50:50 for the WHOLE GAME (both boards together).
     * Example MEDIUM: 3 surprises per board -> 6 total -> exactly 3 good + 3 bad.
     */
    private void assignGlobalGoodBadForAllSurprises() {
        List<int[]> positions = new ArrayList<>();

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

        Collections.shuffle(positions, new Random());
        int goodCount = positions.size() / 2;

        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            int bi = pos[0], r = pos[1], c = pos[2];

            boolean good = (i < goodCount);
            boards[bi].setGoodSurprise(r, c, good);
        }
    }

    // ==========================================================
    // GETTERS
    // ==========================================================
    public board getBoard(int index) { return boards[index]; }
    public int getCurrentPlayer() { return currentPlayer; }
    public int getMaxLives() { return maxLives; }
    public int getLivesRemaining() { return livesRemaining; }
    public int getScore() { return score; }
    public DifficultyLevel getLevel() { return level; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }

    // ==========================================================
    // GAME LOGIC
    // ==========================================================
    public void switchTurn() {
        currentPlayer = 1 - currentPlayer;
    }

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

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    // ==========================================================
    // WIN CHECK
    // ==========================================================
    public boolean boardFinishedAllMines(int boardIndex) {
        return boards[boardIndex].allSafeCellsRevealed();
    }


    public MotivationManager getMotivationManager() {
        return motivationManager;
    }

    // ==========================================================
    // LIFE OVERFLOW HELPERS
    // ==========================================================
    public void addLifeWithOverflowToPoints(int amount) {
        int before = livesRemaining;
        livesRemaining = Math.min(maxLives, livesRemaining + amount);

        int gained = livesRemaining - before;
        int overflow = amount - gained;

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
}
