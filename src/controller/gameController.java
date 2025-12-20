package controller;

import model.DifficultyLevel;
import model.board;
import model.game;
import view.HomeScreen;
import view.gameView;
import model.CSVHandler;
import model.gameHistory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            // timer is already started in gameView constructor
        }
    }

    public void handleCellClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        board b = model.getBoard(boardIndex);
        
        if (b.isFlagged(row, col)) {
            view.showRemoveFlagMessage();
            return;
        }

        // ===== FLAG MODE =====
        if (flagMode[boardIndex]) {
            handleFlagClick(boardIndex, row, col);
            return;
        }

        // ===== TURN CHECK =====
        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            return;
        }

        // ===== ALREADY REVEALED =====

        if (b.isRevealed(row, col)) return;

        // =====================================================
        // ================= SURPRISE LOGIC ====================
        // =====================================================
        if (b.isSurprise(row, col)) {

            // -------- FIRST CLICK → REVEAL SURPRISE ONLY --------
            if (!b.isSurpriseRevealed(row, col)) {
                b.revealSurprise(row, col);
                view.revealSurprise(boardIndex, row, col);

                // turn switches immediately
                model.switchTurn();
                view.setActiveBoard(model.getCurrentPlayer());
                return;
            }

            // -------- SECOND CLICK → ACTIVATE SURPRISE --------
            if (!b.isSurpriseActivated(row, col)) {

                int cost, reward;
                switch (model.getLevel()) {
                    case EASY:   cost = 5;  reward = 8;  break;
                    case MEDIUM: cost = 8;  reward = 12; break;
                    default:     cost = 12; reward = 16;
                }

                boolean good = b.isGoodSurprise(row, col);

                // points from surprise itself
                int rewardPoints = good ? reward : -reward;

                // extra penalty if good surprise but lives already full
                boolean livesFull = (model.getLivesRemaining() >= model.getMaxLives());
                int fullLifePenalty = (good && livesFull) ? cost : 0;

                // ===== Apply score exactly as rules =====
                model.addToScore(-cost);            // activation cost
                model.addToScore(rewardPoints);     // reward (+) or bad (-)
                model.addToScore(-fullLifePenalty); // extra cost if lives were full

                // ===== Apply life change =====
                int lifeDelta;
                if (good) {
                    if (!livesFull) {
                        model.addLife(1);
                        lifeDelta = +1;
                    } else {
                        // life stays same, paid penalty instead
                        lifeDelta = 0;
                    }
                } else {
                    model.loseLife();
                    lifeDelta = -1;
                }

                b.activateSurprise(row, col);
                view.activateSurprise(boardIndex, row, col);

                int netPoints = rewardPoints - cost - fullLifePenalty;

                // ✅ updated message (see view change below)
                view.showSurpriseResult(good, lifeDelta, rewardPoints, cost, fullLifePenalty, netPoints);

                view.updateScore(model.getScore());
                view.updateLives(model.getLivesRemaining());

                model.switchTurn();
                view.setActiveBoard(model.getCurrentPlayer());
                return;
            }
        }

        // =====================================================
        // ================= NORMAL CELL =======================
        // =====================================================
        b.setRevealed(row, col);

        // ===== MINE =====
        if (b.isMine(row, col)) {

            model.loseLife();
            view.revealMineHit(boardIndex, row, col);
            view.updateLives(model.getLivesRemaining());
            checkWinForBoard(boardIndex);

            if (model.isGameOver()) {
            	
            	saveGameHistory("lost");
            	
            	for (int i = 0; i < 2; i++) {
            	    view.revealAllMines(i, model.getBoard(i));
            	    view.revealAllSurprises(i, model.getBoard(i));
            	}
               // view.revealAllMines(0, model.getBoard(0));
               // view.revealAllMines(1, model.getBoard(1));
                view.stopTimer();

                // ✅ stop timer before dialog / restart / exit
                view.stopTimer();

                int choice = view.showGameOverDialog();
                if (choice == JOptionPane.YES_OPTION) {
                    view.dispose();
                    new gameController();
                } else {
                    System.exit(0);
                }
            }
        }
        // ===== SAFE CELL =====
        else {
            int count = b.getSurroundingMines(row, col);
            view.revealSafeCell(boardIndex, row, col, count);

            model.addToScore(+1);
            view.updateScore(model.getScore());

            if (count == 0)
                floodReveal(boardIndex, row, col);
        }

        // ===== SWITCH TURN =====
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

                // already revealed → skip
                if (b.isRevealed(nr, nc)) continue;

                // mines are never revealed by cascade
                if (b.isMine(nr, nc)) continue;

                // ================= SURPRISE CELL =================
                if (b.isSurprise(nr, nc)) {

                    // reveal surprise visually, but DO NOT activate
                    if (!b.isSurpriseRevealed(nr, nc)) {
                        b.revealSurprise(nr, nc);
                        view.revealSurprise(boardIndex, nr, nc);
                    }

                    // IMPORTANT: cascade continues through surprise
                    continue;
                }

                // ================= NORMAL SAFE CELL =================
                b.setRevealed(nr, nc);
                int count = b.getSurroundingMines(nr, nc);
                view.revealSafeCell(boardIndex, nr, nc, count);

                model.addToScore(+1);
                view.updateScore(model.getScore());

                if (count == 0) {
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
            
            saveGameHistory("won");
            // gameView will stop timer inside showWinForBoth
            view.showWinForBoth(model.getScore());
        }
    }
    
    private void saveGameHistory(String result) {

        // 1. Date
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // 2. Players
        String playerA = model.getPlayer1Name();
        String playerB = model.getPlayer2Name();

        // 3. Duration (from view timer)
        String duration = view != null
                ? view.getFormattedElapsedTime()
                : "00:00";

        // 4. Score
        int score = model.getScore();

        // 5. Level
        String level = model.getLevel().name();

        // 6. Create history object
        gameHistory entry = new gameHistory(
                date,
                playerA,
                playerB,
                result,
                duration,
                score,
                level
        );

        // 7. Append to CSV
        CSVHandler csv = new CSVHandler("src/data/game_history.csv");
        csv.appendGameHistory(entry);
    }
    
    public void handleRightClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        board b = model.getBoard(boardIndex);

        // Right click removes flag and switches turn
        if (b.isFlagged(row, col)) {
            b.removeFlag(row, col);
            view.removeFlag(boardIndex, row, col);

            // switch turn
            model.switchTurn();
            view.setActiveBoard(model.getCurrentPlayer());
        }
    }




    public void exitToHome() {
        if (view != null) view.stopTimer();

        view.dispose();
        if (homeScreen != null) homeScreen.setVisible(true);
        else new HomeScreen().setVisible(true);
    }

}
