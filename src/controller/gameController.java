package controller;

import model.DifficultyLevel;
import model.Question;
import model.board;
import model.game;
import model.QuestionOutcome;
import view.HomeScreen;
import view.gameView;
import model.CSVHandler;
import model.gameHistory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class gameController {

    private static final Logger LOG = Logger.getLogger(gameController.class.getName());

    private game model;
    private gameView view;
    private HomeScreen homeScreen;

    private final boolean[] flagMode = new boolean[] { false, false };

    private boolean historySaved = false;

    private boolean[][] floodVisited;

    // Question system
    private List<model.Question> gameQuestions;

    public gameController() {
        model = new game();
        loadQuestionsForGame();
        view = new gameView(this, model);

        // register observer
        for (int i = 0; i < 2; i++) {
            model.getBoard(i).addObserver(view.getBoardView(i));
        }
    }

    public gameController(DifficultyLevel level, String p1, String p2, HomeScreen home) {
        this.homeScreen = home;
        model = new game(level, p1, p2);
        loadQuestionsForGame();
        view = new gameView(this, model);

        // register observer
        for (int i = 0; i < 2; i++) {
            model.getBoard(i).addObserver(view.getBoardView(i));
        }
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
        /* // ===== DEBUG: count & reveal question cells =====
            for (int bi = 0; bi < 2; bi++) {

                board b = model.getBoard(bi);

                for (int r = 0; r < b.getRows(); r++) {
                    for (int c = 0; c < b.getCols(); c++) {

                        if (b.isQuestionCell(r, c)) {

                            // optional visual debug
                            b.revealQuestion(r, c);
                            view.revealQuestion(bi, r, c);
                        }
                    }
                }
            }
            // ===============================================*/
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

    // this is template method pattern. i took the common logic that both que and surprise cells follow ( first click, used check,..)
    // and pit it in one fixed method. the parts that change ( how cells reveled and activated ) . this method uses the same logic for diff special
    // by passing the changing steps as parameters.

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

        if (!isRevealed) {
            revealStep.run();
            if (model.isGameOver()) return;
            model.switchTurn();
            view.setActiveBoard(model.getCurrentPlayer());
            return;
        }

        activateStep.run();
        if (model.isGameOver()) return;

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

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

        if (b.isRevealed(row, col) && !b.isQuestion(row, col) && !b.isSurprise(row, col)) {
            showAlreadyRevealedCellMessage();
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

        // ================= NORMAL CELL =================
        //b.setRevealed(row, col);

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
            //////--before observer pattern--//////
            // int count = b.getSurroundingMines(row, col);
            // view.revealSafeCell(boardIndex, row, col, count);

            //////--after observer pattern--//////
            b.openSafeCell(boardIndex, row, col); // model notifies boardView
            int count = b.getSurroundingMines(row, col);
            model.addToScore(+1);
            view.updateScore(model.getScore());
            String msg = model.getMotivationManager()
                              .onGoodMove(model.getCurrentPlayer());
            view.showMotivationMessage(msg);
           /* if (count == 0) {
                floodVisited = new boolean[model.getBoard(boardIndex).getRows() ]
                [model.getBoard(boardIndex).getCols() ];
                floodReveal(boardIndex, row, col);
            }*/
            if (count == 0) {
                floodVisited = new boolean[b.getRows()][b.getCols()];
                floodReveal(boardIndex, row, col);
            }
        }

        if (model.isGameOver()) return;

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

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
            JOptionPane.showMessageDialog(view, "This question is already USED.");
            return;
        }

        int activationCost = getActivationCost();
        model.addToScore(-activationCost);

        model.Question q = getRandomUnusedQuestionForGame();

        if (q == null) {
            JOptionPane.showMessageDialog(view, "No questions available.");
            return;
        }

        Boolean correctObj = showTimedQuestionDialog(q, activationCost, 30);
        boolean correct = Boolean.TRUE.equals(correctObj);

        StringBuilder resultMsg = new StringBuilder();

        resultMsg.append("Question level: ")
                 .append(q.getDifficultyLevel().toUpperCase())
                 .append("\n\n");

        if (correct) {
            resultMsg.append("✅ Correct answer!\n\n");
        } else {
            resultMsg.append("❌ Wrong answer!\n");
            resultMsg.append("Correct answer was:\n")
                     .append(q.getCorrectAnswer())
                     .append("\n\n");
        }

        QuestionOutcome outcome =
                model.getQuestionOutcomeFromTable(correct, q.getDifficultyLevel());

        resultMsg.append("Result:\n")
                 .append(outcome.message);

        model.addToScore(outcome.pointsDelta);

        int overflowPenalty = 0;

        if (outcome.livesDelta > 0) {
            overflowPenalty = model.addLifeIfPossibleOrReturnPenalty(outcome.livesDelta);
        }
        else if (outcome.livesDelta < 0) {
            for (int i = 0; i < -outcome.livesDelta; i++) {
                model.loseLife();
            }
        }

        if (outcome.revealOneMine) {
            revealOneMineAuto(boardIndex);
        }
        if (outcome.reveal3x3Random) {
            revealRandom3x3(boardIndex);
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
            model.addToScore(-overflowPenalty);

            JOptionPane.showMessageDialog(
                    view,
                    "You already have 10 lives (MAX).\n"
                    + "Score reduced by -" + overflowPenalty
                    + " due to life reward overflow.",
                    "Max Lives Reached",
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
                if (b.isMine(nr, nc)) continue;

                if (b.isQuestionCell(nr, nc)) {

                    if (!b.isQuestionRevealed(nr, nc)) {
                        b.revealQuestion(nr, nc);
                        view.revealQuestion(boardIndex, nr, nc);
                    }

                    floodReveal(boardIndex, nr, nc);
                    continue;
                }

                if (b.isSurprise(nr, nc)) {

                    if (!b.isSurpriseRevealed(nr, nc)) {
                        b.revealSurprise(nr, nc);
                        view.revealSurprise(boardIndex, nr, nc);
                    }

                    floodReveal(boardIndex, nr, nc);
                    continue;
                }

                if (!b.isRevealed(nr, nc)) {

                    b.setRevealed(nr, nc);

                    int count = b.getSurroundingMines(nr, nc);
                    view.revealSafeCell(boardIndex, nr, nc, count);

                    model.addToScore(1);
                    view.updateScore(model.getScore());

                    if (count == 0) {
                        floodReveal(boardIndex, nr, nc);
                    }
                }
            }
        }
    }

    private void showAlreadyRevealedCellMessage() {
        JOptionPane.showMessageDialog(
                view,
                "This cell is already revealed!",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showAlreadyUsedSpecialCellMessage(String what) {
        JOptionPane.showMessageDialog(
                view,
                "This " + what + " was already used!",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public void handleFlagClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            flagMode[boardIndex] = false;
            view.setFlagMode(boardIndex, false);
            return;
        }

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

        if (checkWinForBoard(boardIndex)) return;

        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
    }

    public void toggleFlagMode(int boardIndex) {
        flagMode[boardIndex] = !flagMode[boardIndex];
        view.setFlagMode(boardIndex, flagMode[boardIndex]);
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

        List<model.Question> filtered = new ArrayList<>();

        for (model.Question q : gameQuestions) {
            if (q.getGameLevel().equalsIgnoreCase(model.getLevel().name())) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) return null;

        Collections.shuffle(filtered);
        return filtered.get(0);
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

        view.revealMineAuto(boardIndex, chosen[0], chosen[1]);
    }

    private void revealRandom3x3(int boardIndex) {
        board b = model.getBoard(boardIndex);
        java.util.Random rnd = new java.util.Random();

        int r0 = rnd.nextInt(b.getRows());
        int c0 = rnd.nextInt(b.getCols());

        int sr = Math.max(0, Math.min(b.getRows() - 3, r0 - 1));
        int sc = Math.max(0, Math.min(b.getCols() - 3, c0 - 1));

        for (int r = sr; r < sr + 3; r++) {
            for (int c = sc; c < sc + 3; c++) {
                view.revealHintCell(boardIndex, r, c);
            }
        }
    }

    private Boolean showTimedQuestionDialog(
            model.Question q, int activationCost, int seconds) {

        String[] answers = {
                q.getCorrectAnswer(),
                q.getWrongAnswer1(),
                q.getWrongAnswer2(),
                q.getWrongAnswer3()
        };

        java.util.List<String> list =
                java.util.Arrays.asList(answers);
        java.util.Collections.shuffle(list);

        final JDialog dialog =
                new JDialog(view, "Question (30s)", true);

        dialog.setLayout(new java.awt.BorderLayout(10, 10));

        JLabel timerLabel =
                new JLabel("Time left: " + seconds + "s",
                        SwingConstants.CENTER);

        JTextArea questionArea = new JTextArea(
                "Question Level: " + q.getDifficultyLevel().toUpperCase() +
                "\nGame Level: " + model.getLevel().name() +
                "\n\n" + q.getQuestionText() +
                "\n\nActivation Cost: -" + activationCost
        );

        questionArea.setEditable(false);
        questionArea.setWrapStyleWord(true);
        questionArea.setLineWrap(true);
        questionArea.setOpaque(false);

        JPanel answersPanel =
                new JPanel(new java.awt.GridLayout(2, 2, 10, 10));

        final Boolean[] result = { null };
        final String correct = q.getCorrectAnswer();

        for (String ans : list) {
            JButton btn = new JButton(ans);
            btn.addActionListener(e -> {
                result[0] = ans.equals(correct);
                dialog.dispose();
            });
            answersPanel.add(btn);
        }

        dialog.add(timerLabel, java.awt.BorderLayout.NORTH);
        dialog.add(questionArea, java.awt.BorderLayout.CENTER);
        dialog.add(answersPanel, java.awt.BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(view);

        final int[] remaining = { seconds };
        javax.swing.Timer t =
                new javax.swing.Timer(1000, e -> {
                    remaining[0]--;
                    timerLabel.setText("Time left: " + remaining[0] + "s");
                    if (remaining[0] <= 0) {
                        result[0] = false;
                        dialog.dispose();
                    }
                });
        t.start();

        dialog.setVisible(true);
        return result[0];
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
        System.out.println(" Game history saved to: " + getGameHistoryPath());
    }

    public static String getGameHistoryPath() {

        String baseDir =
                System.getProperty("user.home")
                + File.separator
                + ".minesweeper"
                + File.separator
                + "data";

        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return baseDir + File.separator + "game_history.csv";
    }

    private void loadQuestionsForGame() {

        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/questions_data.csv");

            if (is == null) {
                LOG.severe("questions_data.csv NOT FOUND in JAR!");
                gameQuestions = new ArrayList<>();
                return;
            }

            File tempFile = File.createTempFile("questions_data", ".csv");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }

            CSVHandler csv = new CSVHandler(tempFile.getAbsolutePath());
            gameQuestions = csv.readQuestions();

            System.out.println("✅ GAME LOADED QUESTIONS: " + gameQuestions.size());

            for (Question q : gameQuestions) {
                LOG.fine("→ " + q.getQuestionText());
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed loading questions!", e);
            gameQuestions = new ArrayList<>();
        }
    }
    //////////////////

    public void handleRightClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;

        board b = model.getBoard(boardIndex);

        if (b.isFlagged(row, col)) {
            b.removeFlag(row, col);
            view.removeFlag(boardIndex, row, col);

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
