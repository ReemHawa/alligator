package controller;

import model.DifficultyLevel;
import model.QuestionOutcome;
import model.board;
import model.game;
import view.HomeScreen;
import view.gameView;
import model.CSVHandler;
import model.gameHistory;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

public class gameController {

    private static final Logger LOG = Logger.getLogger(gameController.class.getName());

    private game model;
    private gameView view;
    private HomeScreen homeScreen;

    private final boolean[] flagMode = new boolean[] { false, false };
    private static final Color CORRECT_BG = new Color(210, 245, 210); // ירוק בהיר
    private static final Color WRONG_BG   = new Color(245, 215, 215); // אדום בהיר

    private static final Color LOCKED_BG  = new Color(245, 245, 245); // אפור עדין (אופציונלי)

    

    private boolean historySaved = false;

    private boolean[][] floodVisited;
    private final java.util.HashSet<Integer> usedQuestionIds = new java.util.HashSet<>();

    // Question system
    private List<model.Question> gameQuestions;

    // =========================
    // ✅ NEW: FLAGS LIMIT SYSTEM
    // =========================
    private int maxFlagsPerPlayer = 0;
    private final int[] remainingFlags = new int[]{0, 0};

    public gameController() {
        model = new game();
        loadQuestionsForGame();
        view = new gameView(this, model);

        initFlagsLimitAndUI();

        for (int i = 0; i < 2; i++) {
            model.getBoard(i).addObserver(view.getBoardView(i));
        }
    }

    public gameController(DifficultyLevel level, String p1, String p2, HomeScreen home) {
        this.homeScreen = home;
        model = new game(level, p1, p2);
        loadQuestionsForGame();
        view = new gameView(this, model);

        initFlagsLimitAndUI();

        for (int i = 0; i < 2; i++) {
            model.getBoard(i).addObserver(view.getBoardView(i));
        }
    }

    // ✅ computes half-of-mines and sets per-player remaining counters
    private void initFlagsLimitAndUI() {
        int mines = model.getBoard(0).getMinesNum();   // same mines per board
        maxFlagsPerPlayer = mines ;                 // your rule: half of mines

        remainingFlags[0] = maxFlagsPerPlayer;
        remainingFlags[1] = maxFlagsPerPlayer;

        if (view != null) {
            view.updateRemainingFlags(0, remainingFlags[0]);
            view.updateRemainingFlags(1, remainingFlags[1]);
        }
    }

    public int getMaxFlagsPerPlayer() {
        return maxFlagsPerPlayer;
    }

    public int getRemainingFlags(int playerIndex) {
        return remainingFlags[playerIndex];
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

    public void restartSameGame() {
        DifficultyLevel level = model.getLevel();
        String p1 = model.getPlayer1Name();
        String p2 = model.getPlayer2Name();
        HomeScreen home = this.homeScreen;
        historySaved = false;

        if (view != null) {
            view.dispose();
        }

        gameController newController = new gameController(level, p1, p2, home);
        newController.startGame();
    }

    private boolean isPlayersTurnOnBoard(int boardIndex) {
        return boardIndex == model.getCurrentPlayer();
    }

    // ====================================================
    // ================= CELL CLICK ========================
    // ====================================================
    public void handleCellClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            return;
        }

        board b = model.getBoard(boardIndex);

        if (b.isFlagged(row, col)) {
            view.showRemoveFlagMessage();
            return;
        }

        if (flagMode[boardIndex]) {
            handleFlagClick(boardIndex, row, col);
            return;
        }

        // ================= QUESTION CELL =================
        if (b.isQuestion(row, col)) {
            handleSpecialCellTemplate(
                    boardIndex,
                    row,
                    col,
                    b.isQuestionRevealed(row, col),
                    b.isQuestionUsed(row, col),
                    () -> {
                        b.revealQuestion(row, col);
                        view.revealQuestion(boardIndex, row, col);
                    },
                    () -> activateQuestionCell(boardIndex, row, col),
                    "question"
            );
            return;
        }

        // ================= SURPRISE CELL =================
        if (b.isSurprise(row, col)) {
            handleSpecialCellTemplate(
                    boardIndex,
                    row,
                    col,
                    b.isSurpriseRevealed(row, col),
                    b.isSurpriseActivated(row, col),
                    () -> {
                        b.revealSurprise(row, col);
                        view.revealSurprise(boardIndex, row, col);
                    },
                    () -> activateSurpriseCell(boardIndex, row, col),
                    "surprise"
            );
            return;
        }

        // ================= NORMAL / MINE CELL =================
        if (b.isRevealed(row, col)) {
            showAlreadyRevealedCellMessage();
            return;
        }

        // ===== MINE =====
        if (b.isMine(row, col)) {

            b.setRevealed(row, col);
            model.loseLife();

            String msg = model.getMotivationManager()
                    .onBadMove(model.getCurrentPlayer());
            view.showMotivationMessage(msg);

            view.revealMineHit(boardIndex, row, col);
            view.updateLives(model.getLivesRemaining());

            if (checkWinForBoard(boardIndex)) return;

            if (model.isGameOver()) {

                saveGameHistory("lost");

                for (int i = 0; i < 2; i++) {
                    view.revealAllMines(i, model.getBoard(i));
                    view.revealAllSurprises(i, model.getBoard(i));
                }

                view.stopTimer();

                int choice = view.showGameOverDialog();
                if (choice == JOptionPane.YES_OPTION) {
                    view.dispose();
                    restartSameGame();
                } else {
                    System.exit(0);
                }
                return;
            }
        }
        // ===== SAFE CELL =====
        else {

            b.openSafeCell(boardIndex, row, col);
            int count = b.getSurroundingMines(row, col);

            model.addToScore(+1);
            view.updateScore(model.getScore());

            String msg = model.getMotivationManager()
                    .onGoodMove(model.getCurrentPlayer());
            view.showMotivationMessage(msg);

            if (count == 0) {
                floodVisited = new boolean[b.getRows()][b.getCols()];
                floodReveal(boardIndex, row, col);
            }
        }

        if (model.isGameOver()) return;

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

    // ====================================================
    // ✅ NEW: FLAG CLICK WITH LIMIT + NO TURN SWITCH
    // ====================================================
    public void handleFlagClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

        board b = model.getBoard(boardIndex);

        // ✅ if player used all flags -> block
        if (remainingFlags[boardIndex] <= 0) {
            JOptionPane.showMessageDialog(
                    view,
                    "You’ve used all your flags for this level.\n" +
                            "Nice effort! Now focus on revealing safe cells ✨",
                    "No Flags Left",
                    JOptionPane.INFORMATION_MESSAGE
            );
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

        // If already flagged
        if (b.isFlagged(row, col)) {
            showAlreadyFlaggedCellMessage();
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

        // If revealed
        boolean revealedAny =
                b.isRevealed(row, col)
                        || b.isQuestionRevealed(row, col)
                        || b.isSurpriseRevealed(row, col)
                        || b.isQuestionUsed(row, col)
                        || b.isSurpriseActivated(row, col);

        if (revealedAny) {
            showCannotFlagRevealedCellMessage();
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

        // Place flag
        b.placeFlag(row, col);
        view.updateTileFlag(boardIndex, row, col);

        int delta = b.isMine(row, col) ? +1 : -3;
        model.addToScore(delta);
        view.updateScore(model.getScore());

        // ✅ consume 1 flag placement
        remainingFlags[boardIndex]--;
        view.updateRemainingFlags(boardIndex, remainingFlags[boardIndex]);

        // ✅ cute message when last flag is used
        if (remainingFlags[boardIndex] == 0) {
            JOptionPane.showMessageDialog(
                    view,
                    "That was your last flag ✅\n" +
                            "Great scouting! Now it’s all about smart reveals 🧠",
                    "All Flags Used",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        // exit flag mode
        flagMode[boardIndex] = false;
        view.setFlagMode(boardIndex, false);

        if (checkWinForBoard(boardIndex)) return;

        // ✅ IMPORTANT: DO NOT SWITCH TURN when placing a flag
        view.setActiveBoard(model.getCurrentPlayer());
    }

    // ====================================================
    // ✅ NEW: RIGHT CLICK REMOVAL DOES NOT REFUND FLAGS
    // ====================================================
    public void handleRightClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        if (!isPlayersTurnOnBoard(boardIndex)) {
            view.showNotYourTurnMessage();
            return;
        }

        board b = model.getBoard(boardIndex);

        // Only act if the cell is flagged
        if (!b.isFlagged(row, col)) return;

        int remaining = getRemainingFlags(boardIndex);

        String msg =
                "Remove this flag?\n\n"
              + "• This will NOT restore your remaining flags.\n"
              + "• You will still have " + remaining + " flag(s) left to place.\n\n"
              + "Do you want to continue?";

        int choice = JOptionPane.showConfirmDialog(
                view,
                msg,
                "Confirm Flag Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            // player cancelled — keep flag
            JOptionPane.showMessageDialog(
                    view,
                    "All good The flag stays right where it is.",
                    "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // ✅ confirmed → remove flag
        b.removeFlag(row, col);
        view.removeFlag(boardIndex, row, col);

        JOptionPane.showMessageDialog(
                view,
                "Flag removed \n",
                "Flag removed",
                JOptionPane.INFORMATION_MESSAGE
        );

        // ✅ stay on same player's turn
        view.setActiveBoard(model.getCurrentPlayer());
    }


    // ====================================================
    // Flag mode toggle
    // ====================================================
    public void toggleFlagMode(int boardIndex) {

        if (model.isGameOver()) return;

        if (!isPlayersTurnOnBoard(boardIndex)) {
            view.showNotYourTurnMessage();
            return;
        }

        // ✅ optional: if no flags left, don't even enable mode
        if (remainingFlags[boardIndex] <= 0) {
            JOptionPane.showMessageDialog(
                    view,
                    "No flags remaining for you in this level.\n" +
                            "Try revealing cells carefully — you’ve got this 🌟",
                    "No Flags Left",
                    JOptionPane.INFORMATION_MESSAGE
            );
            view.setFlagMode(boardIndex, false);
            flagMode[boardIndex] = false;
            return;
        }

        flagMode[boardIndex] = !flagMode[boardIndex];
        view.setFlagMode(boardIndex, flagMode[boardIndex]);
    }

    // ====================================================
    // ========= Special cell template (your existing) =====
    // ====================================================
    private void handleSpecialCellTemplate(
            int boardIndex,
            int row,
            int col,
            boolean isRevealed,
            boolean isUsed,
            Runnable revealStep,
            Runnable activateStep,
            String typeName
    ) {
        if (isUsed) {
            showAlreadyUsedSpecialCellMessage(typeName);
            return;
        }

        // FIRST CLICK reveal only 
        if (!isRevealed) {
            revealStep.run();
            view.setActiveBoard(model.getCurrentPlayer()); // stay same player
            return;
        }

        //  SECOND CLICK: activate
        activateStep.run();

        if (model.isGameOver()) return;

        
        view.setActiveBoard(model.getCurrentPlayer());
    }

    // ====================================================
    // =============== Your existing logic below ===========
    // ====================================================

    private void activateSurpriseCell(int boardIndex, int row, int col) {
        board b = model.getBoard(boardIndex);

        int cost, reward;
        switch (model.getLevel()) {
            case EASY:   cost = 5;  reward = 8;  break;
            case MEDIUM: cost = 8;  reward = 12; break;
            default:     cost = 12; reward = 16;
        }

        boolean good = b.isGoodSurprise(row, col);
        int rewardPoints = good ? reward : -reward;

        boolean livesFull = (model.getLivesRemaining() >= model.getMaxLives());
        int fullLifePenalty = (good && livesFull) ? cost : 0;

        model.addToScore(-cost);
        model.addToScore(rewardPoints);
        model.addToScore(-fullLifePenalty);

        int lifeDelta;
        if (good) {
            if (!livesFull) {
                model.addLife(1);
                lifeDelta = +1;
            } else {
                lifeDelta = 0;
            }
        } else {
            model.loseLife();
            lifeDelta = -1;
        }

        b.activateSurprise(row, col);
        view.activateSurprise(boardIndex, row, col);

        int netPoints = rewardPoints - cost - fullLifePenalty;

        view.showSurpriseResult(
                good, lifeDelta, rewardPoints,
                cost, fullLifePenalty, netPoints
        );

        view.updateScore(model.getScore());
        view.updateLives(model.getLivesRemaining());

        if (model.isGameOver()) {
            saveGameHistory("lost");

            for (int i = 0; i < 2; i++) {
                view.revealAllMines(i, model.getBoard(i));
                view.revealAllSurprises(i, model.getBoard(i));
            }

            view.stopTimer();

            int choice = view.showGameOverDialog();
            if (choice == JOptionPane.YES_OPTION) {
                view.dispose();
                restartSameGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void activateQuestionCell(int boardIndex, int row, int col) {
        board b = model.getBoard(boardIndex);

        if (b.isQuestionUsed(row, col)) {
            JOptionPane.showMessageDialog(view, "This question was already used.", "Question", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int activationCost = getActivationCost();
        model.addToScore(-activationCost);

        model.Question q = getRandomUnusedQuestionForGame();
        if (q == null) {
            JOptionPane.showMessageDialog(view, "No questions available.", "Question", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Boolean correctObj = showTimedQuestionDialog(q, activationCost, 30);
        boolean correct = Boolean.TRUE.equals(correctObj);

        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("Question difficulty: ")
                .append(q.getDifficultyLevel().toUpperCase())
                .append("\n\n");

        if (correct) {
            resultMsg.append("✅ Correct answer!\n\n");
        } else {
            resultMsg.append("❌ Wrong answer!\n");
            resultMsg.append("Correct answer was:\n")
                    .append(q.getCorrectAnswerText())
                    .append("\n\n");
        }

        QuestionOutcome outcome =
                model.getQuestionOutcomeFromTable(correct, q.getDifficultyLevel());

        resultMsg.append("Result:\n")
                .append(outcome.message);

        int pointsDelta = outcome.pointsDelta;
        if (outcome.reveal3x3Random && correct) {
            pointsDelta = 0;
        }

        model.addToScore(pointsDelta);
        int overflowPenalty = 0;

        if (outcome.livesDelta > 0) {
            overflowPenalty = model.addLifeIfPossibleOrReturnPenalty(outcome.livesDelta);
        } else if (outcome.livesDelta < 0) {
            for (int i = 0; i < -outcome.livesDelta; i++) model.loseLife();
        }

        if (outcome.revealOneMine) revealOneMineAuto(boardIndex);
        if (outcome.reveal3x3Random) {
            revealBonus3x3(boardIndex);
            model.addToScore(10);
        }

        b.markQuestionUsed(row, col);
        view.markQuestionUsed(boardIndex, row, col);

        view.updateScore(model.getScore());
        view.updateLives(model.getLivesRemaining());

        JOptionPane.showMessageDialog(
                view,
                resultMsg.toString(),
                "Question Result",
                JOptionPane.INFORMATION_MESSAGE
        );

        if (overflowPenalty > 0) {
            model.addToScore(+overflowPenalty);

            JOptionPane.showMessageDialog(
                    view,
                    "Max lives reached.\n" +
                            "Score reduced by +" + overflowPenalty + " due to life overflow.",
                    "Max Lives",
                    JOptionPane.WARNING_MESSAGE
            );
            view.updateScore(model.getScore());
        }

        if (model.isGameOver()) {
            saveGameHistory("lost");

            for (int i = 0; i < 2; i++) {
                view.revealAllMines(i, model.getBoard(i));
                view.revealAllSurprises(i, model.getBoard(i));
            }

            view.stopTimer();

            int choice = view.showGameOverDialog();
            if (choice == JOptionPane.YES_OPTION) {
                view.dispose();
                restartSameGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void floodReveal(int boardIndex, int row, int col) {
        board b = model.getBoard(boardIndex);

        if (floodVisited[row][col]) return;
        floodVisited[row][col] = true;

        int rows = b.getRows();
        int cols = b.getCols();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {

                if (dr == 0 && dc == 0) continue;

                int nr = row + dr;
                int nc = col + dc;

                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                // Never open mines in flood
                if (b.isMine(nr, nc)) continue;

                // If already revealed/used/activated (any state) -> skip
                if (isAnyRevealedOrUsed(b, nr, nc)) continue;

                boolean isFlagged  = b.isFlagged(nr, nc);
                boolean isQuestion = b.isQuestionCell(nr, nc);
                boolean isSurprise = b.isSurprise(nr, nc);

                int count = b.getSurroundingMines(nr, nc);

                // ==========================
                // 1) NUMBER cell (count > 0)
                // ==========================
                // Stop the flood here.
                // Reveal ONLY if NOT flagged.
                if (!isQuestion && !isSurprise && count > 0) {
                    if (!isFlagged) {
                        b.setRevealed(nr, nc);
                        view.revealSafeCell(boardIndex, nr, nc, count);

                        model.addToScore(1);
                        view.updateScore(model.getScore());
                    }
                    continue;
                }

                // =========================================
                // 2) EMPTY cell (count == 0, normal cell)
                // =========================================
                // Reveal (if not flagged) + keep going
                if (!isQuestion && !isSurprise && count == 0) {
                    if (!isFlagged) {
                        b.setRevealed(nr, nc);
                        view.revealSafeCell(boardIndex, nr, nc, 0);

                        model.addToScore(1);
                        view.updateScore(model.getScore());
                    }

                    // Keep going even if it's flagged (flag stays)
                    floodReveal(boardIndex, nr, nc);
                    continue;
                }

                // =========================================
                // 3) QUESTION / SURPRISE behave like EMPTY
                // =========================================
                // Reveal icon (if not flagged) + keep going.
                // Do NOT activate them.
                if (isQuestion) {
                    if (!isFlagged) {
                        b.revealQuestion(nr, nc);
                        view.revealQuestion(boardIndex, nr, nc);
                    }
                    // keep spreading (even if flagged)
                    floodReveal(boardIndex, nr, nc);
                    continue;
                }

                if (isSurprise) {
                    if (!isFlagged) {
                        b.revealSurprise(nr, nc);
                        view.revealSurprise(boardIndex, nr, nc);
                    }
                    // keep spreading (even if flagged)
                    floodReveal(boardIndex, nr, nc);
                }
            }
        }
    }

    private boolean isAnyRevealedOrUsed(board b, int r, int c) {
        return b.isRevealed(r, c)
                || b.isQuestionRevealed(r, c)
                || b.isSurpriseRevealed(r, c)
                || b.isQuestionUsed(r, c)
                || b.isSurpriseActivated(r, c);
    }


    private void showAlreadyRevealedCellMessage() {
        JOptionPane.showMessageDialog(
                view,
                "This cell is already revealed.",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showAlreadyUsedSpecialCellMessage(String what) {
        JOptionPane.showMessageDialog(
                view,
                "This " + what + " was already used.",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showCannotFlagRevealedCellMessage() {
        JOptionPane.showMessageDialog(
                view,
                "You can’t place a flag on a revealed cell.",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showAlreadyFlaggedCellMessage() {
        JOptionPane.showMessageDialog(
                view,
                "This cell already has a flag.",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private boolean checkWinForBoard(int boardIndex) {
        if (model.boardFinishedAllMines(boardIndex)) {
            onGameEnd("won");
            view.showWinForBoth(model.getScore());
            return true;
        }
        return false;
    }

    private int getActivationCost() {
        switch (model.getLevel()) {
            case EASY:   return 5;
            case MEDIUM: return 8;
            default:     return 12;
        }
    }

    private model.Question getRandomUnusedQuestionForGame() {
        if (gameQuestions == null || gameQuestions.isEmpty()) return null;

        java.util.List<model.Question> pool = new java.util.ArrayList<>();
        for (model.Question q : gameQuestions) {
            if (!usedQuestionIds.contains(q.getQuestionID())) pool.add(q);
        }

        if (pool.isEmpty()) {
            usedQuestionIds.clear();
            pool.addAll(gameQuestions);
        }

        java.util.Collections.shuffle(pool);
        model.Question chosen = pool.get(0);
        usedQuestionIds.add(chosen.getQuestionID());
        return chosen;
    }

    private void revealOneMineAuto(int boardIndex) {
        board b = model.getBoard(boardIndex);
        java.util.List<int[]> mines = new java.util.ArrayList<>();

        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.isMine(r, c) && !b.isRevealed(r, c) && !b.isFlagged(r, c)) {
                    mines.add(new int[]{r, c});
                }
            }
        }

        if (mines.isEmpty()) return;

        java.util.Collections.shuffle(mines);
        int[] chosen = mines.get(0);

        int r = chosen[0];
        int c = chosen[1];

        b.setRevealed(r, c);
        view.revealMineAuto(boardIndex, r, c);
    }

    private void revealBonus3x3(int boardIndex) {
        board b = model.getBoard(boardIndex);

        int bestSr = 0, bestSc = 0;
        int bestRevealedMines = Integer.MAX_VALUE;
        int bestRevealedCells = Integer.MAX_VALUE;

        for (int sr = 0; sr <= b.getRows() - 3; sr++) {
            for (int sc = 0; sc <= b.getCols() - 3; sc++) {

                int[] stats = b.get3x3RevealStats(sr, sc);
                int revealedCells = stats[0];
                int revealedMines = stats[1];

                if (revealedMines < bestRevealedMines ||
                        (revealedMines == bestRevealedMines && revealedCells < bestRevealedCells)) {

                    bestRevealedMines = revealedMines;
                    bestRevealedCells = revealedCells;
                    bestSr = sr;
                    bestSc = sc;
                }
            }
        }

        for (int r = bestSr; r < bestSr + 3; r++) {
            for (int c = bestSc; c < bestSc + 3; c++) {

                if (b.isRevealed(r, c)) continue;

                if (b.isQuestionCell(r, c)) {
                    if (!b.isQuestionRevealed(r, c)) {
                        b.revealQuestion(r, c);
                        view.revealQuestion(boardIndex, r, c);
                    }
                    continue;
                }

                if (b.isSurprise(r, c)) {
                    if (!b.isSurpriseRevealed(r, c)) {
                        b.revealSurprise(r, c);
                        view.revealSurprise(boardIndex, r, c);
                    }
                    continue;
                }

                b.setRevealed(r, c);

                if (b.isMine(r, c)) {
                    view.revealMineAuto(boardIndex, r, c);
                } else {
                    int count = b.getSurroundingMines(r, c);
                    view.revealSafeCell(boardIndex, r, c, count);
                }
            }
        }
    }

    // ===== Keep your showTimedQuestionDialog(...) exactly as you already have =====
    // (Not repeated here to avoid making this message insanely long)
    private Boolean showTimedQuestionDialog(model.Question q, int activationCost, int seconds) {

        java.util.List<String> options = q.getAllAnswersShuffled();
        

        final JDialog dialog = new JDialog(view, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new java.awt.Color(0, 0, 0, 0));

        final Boolean[] result = { null };

        // ===== Overlay (dim whole window) =====
        JPanel overlay = new JPanel(new GridBagLayout()) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(0, 0, 0, 160)); // dim
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        // ===== Card wrapper (rounded white glass) =====
        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // shadow
                g2.setColor(new java.awt.Color(0, 0, 0, 70));
                g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 26, 26);

                // glass card
                g2.setColor(new java.awt.Color(255, 255, 255, 235));
                g2.fillRoundRect(0, 0, getWidth() - 20, getHeight() - 20, 26, 26);

                // border
                g2.setColor(new java.awt.Color(255, 255, 255, 200));
                g2.drawRoundRect(0, 0, getWidth() - 21, getHeight() - 21, 26, 26);

                g2.dispose();
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        cardWrapper.setPreferredSize(new Dimension(880, 440));

        // ===== Top bar (title + timer) =====
        JLabel title = new JLabel("Question", SwingConstants.LEFT);
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setForeground(new java.awt.Color(60, 60, 60));

        JLabel timerLabel = new JLabel("", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        timerLabel.setForeground(new java.awt.Color(60, 60, 60));
        
        JLabel closeBtn = new JLabel("×"); // \u00D7
        closeBtn.setFont(new Font("Arial", Font.BOLD, 22));

        
        /*JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("Verdana", Font.BOLD, 18));
        closeBtn.setForeground(new Color(60, 60, 60));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));*/

        // קצת ריווח כדי שיהיה נעים ללחיצה
        closeBtn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                result[0] = false;   // נחשב כמו timeout
                dialog.dispose();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setForeground(new Color(20, 20, 20)); // hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setForeground(new Color(60, 60, 60));
            }
        });

     // right side: timer + X
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTop.setOpaque(false);
        rightTop.add(timerLabel);
        rightTop.add(closeBtn); // או closeBtn אם את משתמשת JButton

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);


        // ===== Meta line (level + activation cost) =====
        JLabel meta = new JLabel(
                "Level: " + model.getLevel().name() + "     |     Activation Cost: -" + activationCost,
                SwingConstants.LEFT
        );
        meta.setFont(new Font("Verdana", Font.PLAIN, 14));
        meta.setForeground(new java.awt.Color(90, 90, 90));

        // ===== Question text =====
        JTextArea questionArea = new JTextArea();
        questionArea.setText(q.getQuestionText());
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setOpaque(false);
        questionArea.setFont(new Font("Verdana", Font.PLAIN, 18));
        questionArea.setForeground(new java.awt.Color(40, 40, 40));
        questionArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // ===== Answers (2x2) =====
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 14, 14));
        answersPanel.setOpaque(false);

        java.util.List<JButton> answerButtons = new java.util.ArrayList<>();

        for (String ans : options) {
            JButton btn = new JButton(ans);
            styleAnswerButton(btn);

            answerButtons.add(btn);

            btn.addActionListener(e -> {
            	

                boolean isCorrect = q.isCorrect(ans);
                result[0] = isCorrect;

                /* נועלים את כל הכפתורים
                for (JButton btt : answerButtons) {
                    btt.setEnabled(false);
                }
*/
                // צובעים את הנלחץ
                setAnswerFeedbackColor(btn, isCorrect ? CORRECT_BG : WRONG_BG);

                // אם טעה - נסמן גם את התשובה הנכונה בירוק
                if (!isCorrect) {
                    String correctText = q.getCorrectAnswerText();
                    for (JButton btt : answerButtons) {
                        if (stripHtml(btt.getText()).equals(correctText)) {
                            setAnswerFeedbackColor(btt, CORRECT_BG);
                            break;
                        }
                    }
                }

                // השהייה קצרה ואז סגירה
                javax.swing.Timer closeDelay = new javax.swing.Timer(650, ev -> dialog.dispose());
                closeDelay.setRepeats(false);
                closeDelay.start();
            });

            answersPanel.add(btn);
        }


        // ===== Layout inside card =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(topBar);
        center.add(Box.createVerticalStrut(6));
        center.add(meta);
        center.add(Box.createVerticalStrut(8));
        center.add(questionArea);
        center.add(Box.createVerticalStrut(10));
        center.add(answersPanel);

        cardWrapper.add(center, BorderLayout.CENTER);
        overlay.add(cardWrapper, new GridBagConstraints());

        dialog.setContentPane(overlay);
        dialog.pack();
        dialog.setLocationRelativeTo(view);
        
        final Point[] mouseDown = { null };

        cardWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseDown[0] = e.getPoint();
            }
        });

        cardWrapper.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (mouseDown[0] == null) return;
                Point curr = e.getLocationOnScreen();
                dialog.setLocation(curr.x - mouseDown[0].x, curr.y - mouseDown[0].y);
            }
        });


        // ===== Timer logic =====
        final int[] remaining = { seconds };
        timerLabel.setText("Time left: " + remaining[0] + "s");

        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            remaining[0]--;
            timerLabel.setText("Time left: " + remaining[0] + "s");
            if (remaining[0] <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                result[0] = false;
                dialog.dispose();
            }
        });
        t.start();

        // ensure timer stops when dialog closes
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (t.isRunning()) t.stop();
            }
        });

        dialog.setVisible(true);
        return result[0];

        // <-- paste your existing method body here exactly (unchanged)
       // return false;

    }


//
    private void setAnswerFeedbackColor(JButton btn, Color bg) {
        btn.putClientProperty("feedbackBg", bg);
        btn.repaint();
    }

    private String stripHtml(String s) {
        if (s == null) return "";
        return s.replaceAll("\\<.*?\\>", "").trim();
    }


    private void onGameEnd(String result) {
        if (model.isGameOver()) return;

        model.setGameOver(true);
        saveGameHistory(result);

        if (view != null) {
            view.stopTimer();

            for (int i = 0; i < 2; i++) {
                view.revealAllMines(i, model.getBoard(i));
                view.revealAllSurprises(i, model.getBoard(i));
            }
        }
    }

    private void styleAnswerButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.setFont(new Font("Verdana", Font.BOLD, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.BLACK);

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setVerticalAlignment(SwingConstants.CENTER);

        // padding inside
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ✅ allow HTML wrap (width you want)
        String raw = btn.getText();
        btn.setText("<html><div style='width:320px;'>" + escapeHtml(raw) + "</div></html>");

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hover = b.getModel().isRollover();
                boolean press = b.getModel().isPressed();

                Color forced = (Color) b.getClientProperty("feedbackBg");
                Color bg;

                if (forced != null) {
                    bg = forced;
                } else {
                    bg = hover ? new Color(240, 240, 240) : new Color(255, 255, 255);
                    if (press) bg = new Color(230, 230, 230);
                }


                // shadow
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRoundRect(4, 6, c.getWidth() - 8, c.getHeight() - 10, 18, 18);

                // body
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth() - 8, c.getHeight() - 8, 18, 18);

                // border
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, c.getWidth() - 9, c.getHeight() - 9, 18, 18);

                g2.dispose();

                // ✅ IMPORTANT: let Swing paint the HTML text (wrap!)
                super.paint(g, c);
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                Dimension d = super.getPreferredSize(c); // HTML decides height
                d.width = Math.max(d.width, 360);
                d.height = Math.max(d.height, 70);
                return d;
            }
        });

        btn.setRolloverEnabled(true);
    }

    //  helper: prevent HTML breaking if answer contains < >
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }


    private void saveGameHistory(String result) {
        if (historySaved) return;
        historySaved = true;

        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String playerA = model.getPlayer1Name();
        String playerB = model.getPlayer2Name();

        String duration = view != null
                ? view.getFormattedElapsedTime()
                : "00:00";

        int score = model.getScore();
        String level = model.getLevel().name();

        gameHistory entry = new gameHistory(
                date,
                playerA,
                playerB,
                result,
                duration,
                score,
                level
        );

        CSVHandler csv = new CSVHandler(getGameHistoryPath());
        csv.appendGameHistory(entry);
        System.out.println("Game history saved to: " + getGameHistoryPath());
    }

    public static String getGameHistoryPath() {
        String baseDir =
                System.getProperty("user.home")
                        + File.separator
                        + "DualMinesweeper"
                        + ".minesweeper"
                        + File.separator
                        + "data";

        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();

        return baseDir + File.separator + "game_history.csv";
    }

    private void loadQuestionsForGame() {
        try {
            CSVHandler csv = new CSVHandler("ignored");
            gameQuestions = csv.readQuestions();
            System.out.println("✅ GAME LOADED QUESTIONS: " + gameQuestions.size());
            System.out.println("✅ QUESTIONS PATH: " + CSVHandler.getWritableQuestionsFile().getAbsolutePath());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed loading questions!", e);
            gameQuestions = new ArrayList<>();
        }
    }

    public void exitToHome() {
        if (view != null) {
            view.stopTimer();
            view.dispose();
        }

        if (homeScreen != null) {
            homeScreen.setVisible(true);
        } else {
            new HomeScreen().setVisible(true);
        }
    }
}
