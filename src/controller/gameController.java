package controller;

import model.board;
import model.game;
import view.gameView;

import javax.swing.*;

public class gameController {

    private game model;
    private gameView view;

    public gameController() {
        model = new game();
        view = new gameView(this, model);
    }

    // called by BoardView buttons
    public void handleCellClick(int boardIndex, int row, int col) {
        if (model.isGameOver()) return;

        if (boardIndex != model.getCurrentPlayer()) {
            view.notYourTurn();
            return;
        }

        board board = model.getBoard(boardIndex);

        if (board.isRevealed(row, col)) return; // already opened

        // mark revealed in model
        board.setRevealed(row, col);

        if (board.isMine(row, col)) {
            // mine hit
            model.loseLife();
            view.revealMineHit(boardIndex, row, col);
            view.updateLives(model.getLivesRemaining());

            if (model.isGameOver()) {
                // reveal all mines on both boards
                view.revealAllMines(0, model.getBoard(0));
                view.revealAllMines(1, model.getBoard(1));

                int choice = view.gameOverDialog();
                if (choice == JOptionPane.YES_OPTION) {
                    // restart
                    view.dispose();
                    new gameController();
                } else {
                    System.exit(0);
                }
                return;
            }

        } else {
            int count = board.getSurroundingMines(row, col);
            view.revealSafeCell(boardIndex, row, col, count);
            if (count == 0) {
                floodReveal(boardIndex, row, col);
            }
        }

        // switch turn after each valid move
        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

    // recursive reveal of empty neighbors
    private void floodReveal(int boardIndex, int row, int col) {
        board board = model.getBoard(boardIndex);

        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int nr = row + di;
                int nc = col + dj;
                if (di == 0 && dj == 0) continue;
                if (nr < 0 || nr >= board.rows || nc < 0 || nc >= board.cols) continue;

                if (!board.isRevealed(nr, nc) && !board.isMine(nr, nc)) {
                    board.setRevealed(nr, nc);
                    int count = board.getSurroundingMines(nr, nc);
                    view.revealSafeCell(boardIndex, nr, nc, count);
                    if (count == 0) {
                        floodReveal(boardIndex, nr, nc);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(gameController::new);
    }
}
