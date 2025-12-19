package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class game {

    private board[] boards;
    private int currentPlayer = 0;   // 0 = Player A, 1 = Player B

    private int score = 0;

    private final int maxLives = 10;
    private int livesRemaining ;
    private boolean gameOver = false;

    private DifficultyLevel level;
    private String player1Name;
    private String player2Name;

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
                rows = 13; cols = 13; mines = 26; surprises = 3;livesRemaining = 8;
                break;
            case HARD:
                rows = 16; cols = 16; mines = 44; surprises = 4; livesRemaining = 6;
                break;
        }

        boards = new board[2];
        boards[0] = new board(rows, cols, mines, surprises);
        boards[1] = new board(rows, cols, mines, surprises);

        // ✅ IMPORTANT: enforce GLOBAL 50:50 across BOTH boards combined
        assignGlobalGoodBadForAllSurprises();
    }

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
}
