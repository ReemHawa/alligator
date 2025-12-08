package model;

public class game {

    private board[] boards;
    private int currentPlayer = 0;   // 0 = Player A, 1 = Player B

    private int score = 0;

    private final int maxLives = 10;
    private int livesRemaining = maxLives;
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

        boards = new board[2];
        boards[0] = new board(rows, cols, mines);
        boards[1] = new board(rows, cols, mines);
    }

   
    public game(DifficultyLevel level, String p1, String p2) {
        this.level = level;
        this.player1Name = p1;
        this.player2Name = p2;

        int rows = 0;
        int cols = 0;
        int mines = 0;

        switch (level) {
            case EASY:
                rows = 9; cols = 9; mines = 10;
                break;

            case MEDIUM:
                rows = 13; cols = 13; mines = 26;
                break;

            case HARD:
                rows = 16; cols = 16; mines = 44;
                break;
        }

        boards = new board[2];
        boards[0] = new board(rows, cols, mines);
        boards[1] = new board(rows, cols, mines);
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

    public void addToScore(int delta) {
        score += delta;
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
