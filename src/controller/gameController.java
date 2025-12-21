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

public class gameController {

    private game model;
    private gameView view;
    private HomeScreen homeScreen;

    private final boolean[] flagMode = new boolean[] { false, false };
    
 // Question system
   // private final model.QuestionBank questionBank = new model.QuestionBank();
    
    private List<model.Question> gameQuestions;

    public gameController() {
        model = new game();
        loadQuestionsForGame();
        view = new gameView(this, model);
    }

    public gameController(DifficultyLevel level, String p1, String p2, HomeScreen home) {
        this.homeScreen = home;
        model = new game(level, p1, p2);
        loadQuestionsForGame();
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

        if (view != null) {
            view.dispose();
        }

        gameController newController = new gameController(level, p1, p2, home);
        newController.startGame();
    }


    public void handleCellClick(int boardIndex, int row, int col) {

        if (model.isGameOver()) return;
        
        // Only current player can play, and only on their own board
        if (boardIndex != model.getCurrentPlayer()) {
            view.showNotYourTurnMessage();
            return;
        }
        board b = model.getBoard(boardIndex);
        
     
        // ================================================

        // block if flagged
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

      // if (b.isRevealed(row, col)) return;
        
     // ================= QUESTION CELL =================
        if (b.isQuestion(row, col)) {

            // -------- FIRST CLICK → REVEAL QUESTION --------
            if (!b.isQuestionRevealed(row, col)) {
                b.revealQuestion(row, col);
                view.revealQuestion(boardIndex, row, col);

                model.switchTurn();
                view.setActiveBoard(model.getCurrentPlayer());
                return;
            }

            // -------- SECOND CLICK → ACTIVATE QUESTION --------
            if (!b.isQuestionUsed(row, col)) {
                handleQuestionCell(boardIndex, row, col);
                return;
            }

            return;
        }



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

            String msg = model.getMotivationManager().onBadMove(model.getCurrentPlayer());
            view.showMotivationMessage(msg);
            
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
                   // new gameController();
                    restartSameGame();
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
            
            String msg = model.getMotivationManager().onGoodMove(model.getCurrentPlayer());
            view.showMotivationMessage(msg);


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

                    // cascade continues through surprise
                    continue;
                }
                
             // ================= QUESTION CELL =================
                if (b.isQuestionCell(nr, nc)) {

                    if (!b.isQuestionRevealed(nr, nc)) {
                        b.revealQuestion(nr, nc);
                        view.revealQuestion(boardIndex, nr, nc);
                    }

                    continue; // DO NOT activate
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
    
    
    // question cell 
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

    
    public void handleQuestionCell(int boardIndex, int row, int col) {

        board b = model.getBoard(boardIndex);

        // ================= FIRST CLICK =================
        // Reveal question only
        if (!b.isQuestionRevealed(row, col)) {
            b.revealQuestion(row, col);
            view.revealQuestion(boardIndex, row, col);

            model.switchTurn();
            view.setActiveBoard(model.getCurrentPlayer());
            return;
        }

        // ================= ALREADY USED =================
        if (b.isQuestionUsed(row, col)) {
            JOptionPane.showMessageDialog(view, "This question is already USED.");
            return;
        }

        // ================= ACTIVATION COST =================
        int activationCost = getActivationCost();
        model.addToScore(-activationCost);

        // ================= GET UNUSED QUESTION =================
       // model.Question q = model.getUnusedRandomQuestion(questionBank);
        model.Question q = getRandomUnusedQuestionForGame();

        if (q == null) {
            JOptionPane.showMessageDialog(view, "No questions available.");
            return;
        }

        // ================= SHOW QUESTION =================
        Boolean correctObj = showTimedQuestionDialog(q, activationCost, 30);
        boolean correct = Boolean.TRUE.equals(correctObj);

        // ================= BUILD RESULT MESSAGE =================
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

        // ================= GET OUTCOME =================
        QuestionOutcome outcome =
                model.getQuestionOutcomeFromTable(correct, q.getDifficultyLevel());

        resultMsg.append("Result:\n")
                 .append(outcome.message);

        // ================= APPLY POINTS (ALWAYS) =================
        model.addToScore(outcome.pointsDelta);

        // ================= APPLY LIVES =================
        int overflowPenalty = 0;

        if (outcome.livesDelta > 0) {
            overflowPenalty = model.addLifeIfPossibleOrReturnPenalty(outcome.livesDelta);
        }
        else if (outcome.livesDelta < 0) {
            for (int i = 0; i < -outcome.livesDelta; i++) {
                model.loseLife();
            }
        }

        // ================= APPLY REVEALS =================
        if (outcome.revealOneMine) {
            revealOneMineAuto(boardIndex);
        }
        if (outcome.reveal3x3Random) {
            revealRandom3x3(boardIndex);
        }

        // ================= MARK QUESTION USED =================
        b.markQuestionUsed(row, col);
        view.markQuestionUsed(boardIndex, row, col);

        // ================= UPDATE UI =================
        view.updateScore(model.getScore());
        view.updateLives(model.getLivesRemaining());

        // ================= SHOW RESULT MESSAGE =================
        JOptionPane.showMessageDialog(
                view,
                resultMsg.toString(),
                "Question Result",
                JOptionPane.INFORMATION_MESSAGE
        );

        // ================= LIFE OVERFLOW MESSAGE =================
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

        // ================= SWITCH TURN =================
        model.switchTurn();
        view.setActiveBoard(model.getCurrentPlayer());
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
    
    private void loadQuestionsForGame() {

        try {
            // 1️⃣ Load CSV from JAR resources
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/questions_data.csv");

            if (is == null) {
                System.out.println("❌ questions_data.csv NOT FOUND in JAR!");
                gameQuestions = new ArrayList<>();
                return;
            }

            // 2️⃣ Copy resource to a temporary file (CSVHandler expects a file path)
            File tempFile = File.createTempFile("questions_data", ".csv");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }

            // 3️⃣ Read questions normally
            CSVHandler csv = new CSVHandler(tempFile.getAbsolutePath());
            gameQuestions = csv.readQuestions();

            // 4️⃣ Debug output
            System.out.println("✅ GAME LOADED QUESTIONS: " + gameQuestions.size());

            for (Question q : gameQuestions) {
                System.out.println(" → " + q.getQuestionText());
            }

        } catch (Exception e) {
            System.out.println("❌ Failed loading questions!");
            e.printStackTrace();
            gameQuestions = new ArrayList<>();
        }
    }






    public void exitToHome() {
        if (view != null) view.stopTimer();

        view.dispose();
        if (homeScreen != null) homeScreen.setVisible(true);
        else new HomeScreen().setVisible(true);
    }
    
}