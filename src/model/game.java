package model;

public class game {

	private final board[] boards = new board[2];
    private int currentPlayer = 0;  // 0 => Player A / 1 => Player B

    private final int maxLives = 10;
    private int livesRemaining = maxLives;
    private boolean gameOver = false;

    public game() {
        boards[0] = new board();
        boards[1] = new board();
    }

    public board getBoard(int index) {
        return boards[index];
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchTurn() {
        currentPlayer = 1 - currentPlayer;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public int getLivesRemaining() {
        return livesRemaining;
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

}
