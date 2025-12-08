package controller;

import model.DifficultyLevel;
import model.board;
import model.game;
import view.HomeScreen;
import view.gameView;

import javax.swing.*;

public class gameController {

    private game model;
    private gameView view;
    private HomeScreen homeScreen;

    private final boolean[] flagMode = new boolean[] { false, false };

    public gameController() {
        model = new game();
        view = new gameView(this, model);
    }

    public gameController(DifficultyLevel level, String p1, String p2, HomeScreen home) {
        this.homeScreen = home;
        model = new game(level, p1, p2);
        view = new gameView(this, model);
    }

    public game getModel() {
        return model;
    }
    
    public gameView getView() {
        return view;
    }
    
    public void startGame() {
        if (view != null) {
            view.setVisible(true);
        }
    }

    public void handleCellClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        if (flagMode[boardIndex]) {
            handleFlagClick(boardIndex, row, col);
            return;
        }

        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            return;
        }

        board b = model.getBoard(boardIndex);
      //  int rows = b.getRows();
      //  int cols = b.getCols();

        if (b.isRevealed(row, col)) return;

        b.setRevealed(row, col);

        if (b.isMine(row, col)) {

            model.loseLife();
            view.revealMineHit(boardIndex, row, col);
            view.updateLives(model.getLivesRemaining());
            checkWinForBoard(boardIndex);

            if (model.isGameOver()) {
                view.revealAllMines(0, model.getBoard(0));
                view.revealAllMines(1, model.getBoard(1));

                int choice = view.showGameOverDialog();
                if (choice == JOptionPane.YES_OPTION) {
                    view.dispose();
                    new gameController();
                } else {
                    System.exit(0);
                }
            }
        } else {

            int count = b.getSurroundingMines(row, col);
            view.revealSafeCell(boardIndex, row, col, count);

            model.addToScore(+1);
            view.updateScore(model.getScore());

            if (count == 0)
                floodReveal(boardIndex, row, col);
        }

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

    private void floodReveal(int boardIndex, int row, int col) {
        board b = model.getBoard(boardIndex);
        int rows = b.getRows();
        int cols = b.getCols();

        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {

                int nr = row + di;
                int nc = col + dj;

                if (di == 0 && dj == 0) continue;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                if (!b.isRevealed(nr, nc) && !b.isMine(nr, nc)) {
                    b.setRevealed(nr, nc);
                    int count = b.getSurroundingMines(nr, nc);
                    view.revealSafeCell(boardIndex, nr, nc, count);

                    model.addToScore(+1);
                    view.updateScore(model.getScore());

                    if (count == 0)
                        floodReveal(boardIndex, nr, nc);
                }
            }
        }
    }

    public void handleFlagClick(int boardIndex, int row, int col) {

        board b = model.getBoard(boardIndex);

        if (b.isRevealed(row, col) || b.isFlagged(row, col)) {
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

        b.placeFlag(row, col);
        view.updateTileFlag(boardIndex, row, col);

        int delta = b.isMine(row, col) ? +1 : -3;
        model.addToScore(delta);
        view.updateScore(model.getScore());

        flagMode[boardIndex] = false;
        view.setFlagMode(boardIndex, false);

        checkWinForBoard(boardIndex);

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

    public void toggleFlagMode(int boardIndex) {
        flagMode[boardIndex] = !flagMode[boardIndex];
        view.setFlagMode(boardIndex, flagMode[boardIndex]);
    }

    private void checkWinForBoard(int boardIndex) {
        if (model.boardFinishedAllMines(boardIndex)) {
            model.setGameOver(true);
            view.showWinForBoth(model.getScore());
        }
    }

    public void exitToHome() {
        view.dispose();
        if (homeScreen != null) homeScreen.setVisible(true);
        else new HomeScreen().setVisible(true);
    }
    
 

}
