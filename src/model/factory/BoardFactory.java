package model.factory;

import model.board;

public abstract class BoardFactory {

    // Factory Method: creates a board with the config of the specific difficulty
    public board createBoard() {
        BoardConfig cfg = getConfig();
        return new board(cfg.rows, cfg.cols, cfg.mines, cfg.surprises, cfg.questions);
    }

    // Each concrete factory must provide its own config (difficulty settings)
    protected abstract BoardConfig getConfig();

    // Helper for the game to set livesRemaining
    public int getStartingLives() {
        return getConfig().startingLives;
    }
}
