package controller;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import model.DifficultyLevel;
import model.Question;
import view.HomeScreen;
import view.QuestionTableModel;
import view.QuestionsManagementScreen;

public class QuestionsManagementController {

    private QuestionsManagementScreen screen;
    private HomeScreen homeScreen;
    private List<Question> questions;

    // קריאה מה־classpath (כמו game_history.csv)
    private static final String CSV_RESOURCE_PATH = "/data/questions_data.csv";
    
    // כתיבה חזרה לקובץ בפרויקט (שיהיה בתיקייה src/data)
    private static final String CSV_WRITE_PATH   = "src/data/questions_data.csv";

    // ===========================
    // ✅ ADDED: cache for gameplay
    // ===========================
    private static List<Question> cachedQuestionsForGame = null;
    private static final Random RNG = new Random();

    // ------------ MAIN CONSTRUCTOR USED BY HomeScreen ------------
    public QuestionsManagementController(HomeScreen homeScreen) {
        this.homeScreen = homeScreen;

        // Read questions from CSV (כמו במסך היסטוריה)
        questions = loadQuestionsFromCSV();
        System.out.println("Questions loaded: " + questions.size());

        // Open UI screen
        screen = new QuestionsManagementScreen(questions);

        // Setup listeners and saving logic
        initListeners();

        screen.setVisible(true);
        homeScreen.setVisible(false);
    }
    
    

    // ------------ LOAD FROM CSV (like gameHistoryController) ------------
    private List<Question> loadQuestionsFromCSV() {
        List<Question> list = new ArrayList<>();

        try (InputStream is = getClass().getResourceAsStream(CSV_RESOURCE_PATH)) {

            if (is == null) {
                System.out.println("⚠ questions_data.csv not found at " + CSV_RESOURCE_PATH);
                return list;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                boolean firstLine = true;
                int idCounter = 1;

                while ((line = reader.readLine()) != null) {

                    // שורה ראשונה – כותרת
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    // פיצול לפי פסיק
                    String[] parts = line.split(",");

                    // חייבים 7 עמודות
                    if (parts.length < 7) continue;

                    String questionText = parts[0].trim();
                    String correct      = parts[1].trim();
                    String wrong1       = parts[2].trim();
                    String wrong2       = parts[3].trim();
                    String wrong3       = parts[4].trim();
                    String difficulty   = parts[5].trim();
                    String gameLevel    = parts[6].trim();

                    Question q = new Question(
                            idCounter++,
                            questionText,
                            correct,
                            wrong1,
                            wrong2,
                            wrong3,
                            difficulty,
                            gameLevel
                    );

                    list.add(q);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠ Failed to load questions from CSV.");
        }

        return list;
    }

    // ------------ EVENT LISTENERS ------------
    private void initListeners() {

        // BACK BUTTON
        screen.getBackButton().addActionListener(e -> {
            saveToCSV();
            screen.dispose();
            homeScreen.setVisible(true);
        });

        // SAVE on close (click X)
        screen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveToCSV();
            }
        });

        // TABLE EVENTS (delete / edit)
        screen.getQuestionsTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                int row = screen.getQuestionsTable().rowAtPoint(e.getPoint());
                int col = screen.getQuestionsTable().columnAtPoint(e.getPoint());

                QuestionTableModel model = screen.getTableModel();

                if (row < 0) return;

                if (col == 0) {     // DELETE
                    Question q = model.getQuestionAt(row);

                    int confirm = JOptionPane.showConfirmDialog(
                            screen,
                            "Delete question #" + q.getQuestionID() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        model.removeQuestionAt(row);
                        saveToCSV();
                    }

                } else if (col == 1) {   // EDIT
                    model.setEditableRow(row);

                    JOptionPane.showMessageDialog(
                            screen,
                            "Row is now editable.\nEdit values directly in the table.",
                            "Edit Mode",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
    }

    // ------------ SAVE TO CSV (plain FileWriter) ------------
    private void saveToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_WRITE_PATH))) {

            // Header
            bw.write("question,correctAnswer,wrongAnswer1,wrongAnswer2,wrongAnswer3,difficultyLevel,gameLevel");
            bw.newLine();

            List<Question> list = screen.getTableModel().getAllQuestions();

            for (Question q : list) {
                String line = String.join(",",
                        escape(q.getQuestionText()),
                        escape(q.getCorrectAnswer()),
                        escape(q.getWrongAnswer1()),
                        escape(q.getWrongAnswer2()),
                        escape(q.getWrongAnswer3()),
                        escape(q.getDifficultyLevel()),
                        escape(q.getGameLevel())
                );
                bw.write(line);
                bw.newLine();
            }

            System.out.println("✅ Saved " + list.size() + " questions to " + CSV_WRITE_PATH);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠ Failed to save questions CSV.");
        }
    }

    // כדי שלא ישברו פסיקים – הכי פשוט: מחליפים פסיקים ברווח
    private String escape(String value) {
        if (value == null) return "";
        return value.replace(",", " ");
    }

    // =========================================================
    // ✅ ADDED: GAMEPLAY API (static) - no UI dependency
    // =========================================================

    /** Load questions for gameplay once (from resources). */
    public static List<Question> loadQuestionsForGame() {
        if (cachedQuestionsForGame != null) return cachedQuestionsForGame;

        List<Question> list = new ArrayList<>();

        try (InputStream is = QuestionsManagementController.class.getResourceAsStream(CSV_RESOURCE_PATH)) {
            if (is == null) {
                System.out.println("⚠ questions_data.csv not found at " + CSV_RESOURCE_PATH);
                cachedQuestionsForGame = list;
                return list;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                int idCounter = 1;

                while ((line = reader.readLine()) != null) {
                    if (firstLine) { firstLine = false; continue; }

                    String[] parts = line.split(",");
                    if (parts.length < 7) continue;

                    String questionText = parts[0].trim();
                    String correct      = parts[1].trim();
                    String wrong1       = parts[2].trim();
                    String wrong2       = parts[3].trim();
                    String wrong3       = parts[4].trim();
                    String difficulty   = parts[5].trim();
                    String gameLevel    = parts[6].trim();

                    list.add(new Question(
                            idCounter++,
                            questionText,
                            correct,
                            wrong1,
                            wrong2,
                            wrong3,
                            difficulty,
                            gameLevel
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cachedQuestionsForGame = list;
        return list;
    }

    /** Convert DifficultyLevel to CSV string you store (e.g., EASY/MEDIUM/HARD). */
    private static String toCsvGameLevel(DifficultyLevel level) {
        // your CSV currently uses a string column gameLevel
        // keep it simple: compare by name (EASY/MEDIUM/HARD)
        return level.name();
    }

    /** Pick random question that matches current game level. If none found -> any random question. */
    public static Question getRandomQuestionForLevel(DifficultyLevel gameLevel) {
        List<Question> all = loadQuestionsForGame();
        if (all.isEmpty()) return null;

        String wanted = toCsvGameLevel(gameLevel);

        List<Question> filtered = new ArrayList<>();
        for (Question q : all) {
            if (q.getGameLevel() != null && q.getGameLevel().trim().equalsIgnoreCase(wanted)) {
                filtered.add(q);
            }
        }

        List<Question> pool = !filtered.isEmpty() ? filtered : all;
        return pool.get(RNG.nextInt(pool.size()));
    }

    /**
     * Optional: Pick random question by gameLevel AND question difficultyLevel (easy/medium/hard/expert).
     * If no match -> fallback to getRandomQuestionForLevel.
     */
    public static Question getRandomQuestionForLevelAndDifficulty(DifficultyLevel gameLevel, String questionDifficulty) {
        List<Question> all = loadQuestionsForGame();
        if (all.isEmpty()) return null;

        String wantedGame = toCsvGameLevel(gameLevel);
        String wantedDiff = (questionDifficulty == null) ? "" : questionDifficulty.trim();

        List<Question> filtered = new ArrayList<>();
        for (Question q : all) {
            boolean okGame = q.getGameLevel() != null && q.getGameLevel().trim().equalsIgnoreCase(wantedGame);
            boolean okDiff = q.getDifficultyLevel() != null && q.getDifficultyLevel().trim().equalsIgnoreCase(wantedDiff);
            if (okGame && okDiff) filtered.add(q);
        }

        if (!filtered.isEmpty()) {
            return filtered.get(RNG.nextInt(filtered.size()));
        }
        return getRandomQuestionForLevel(gameLevel);
    }

    /**
     * Show a multiple-choice question dialog.
     * @return true if user chose correct answer, false otherwise (also false if canceled).
     */
    public static boolean showQuestionDialog(Component parent, Question q) {
        if (q == null) {
            JOptionPane.showMessageDialog(parent, "No question available.", "Question", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> answers = q.getAllAnswersShuffled();
        Object chosen = JOptionPane.showInputDialog(
                parent,
                q.getQuestionText(),
                "Question",
                JOptionPane.QUESTION_MESSAGE,
                null,
                answers.toArray(),
                answers.get(0)
        );

        if (chosen == null) return false; // canceled
        return q.isCorrect(chosen.toString());
    }
}