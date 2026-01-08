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
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

import model.DifficultyLevel;
import model.Question;
import view.HomeScreen;
import view.QuestionTableModel;
import view.QuestionsManagementScreen;

import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionsManagementController {

    private static final Logger LOG = Logger.getLogger(QuestionsManagementController.class.getName());

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

    private static File getWritableQuestionsFile() {
        String baseDir = System.getProperty("user.home")
                + File.separator
                + "DualMinesweeper"
                + File.separator
                + "data";

        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();

        return new File(dir, "questions_data.csv");
    }


    // ------------ MAIN CONSTRUCTOR USED BY HomeScreen ------------
    public QuestionsManagementController(HomeScreen homeScreen) {
        this.homeScreen = homeScreen;

        // Read questions from CSV (כמו במסך היסטוריה)
        questions = loadQuestionsFromCSV();
        LOG.fine("Questions loaded: " + questions.size());

        // Open UI screen
        screen = new QuestionsManagementScreen(questions);

        // Setup listeners and saving logic
        initListeners();

        screen.setVisible(true);
        homeScreen.setVisible(false);
    }



    // ------------ LOAD FROM CSV (like gameHistoryController) ------------
    private List<Question> loadQuestionsFromCSV() {

        ensureQuestionsFileExists();

        List<Question> list = new ArrayList<>();
        File file = getWritableQuestionsFile();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new java.io.FileInputStream(file),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            int idCounter = 1;

            while ((line = reader.readLine()) != null) {

                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                list.add(new Question(
                        idCounter++,
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim(),
                        parts[6].trim()
                ));
            }

            LOG.fine("Questions loaded from writable file");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load questions from CSV", e);
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

        ensureQuestionsFileExists();
        File file = getWritableQuestionsFile();

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(file, false))) {

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

            // 🔥 VERY IMPORTANT
            cachedQuestionsForGame = null;

            LOG.fine("Questions SAVED and cache cleared");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save questions to CSV", e);
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

        if (cachedQuestionsForGame != null)
            return cachedQuestionsForGame;

        ensureQuestionsFileExists();
        File file = getWritableQuestionsFile();

        List<Question> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new java.io.FileInputStream(file),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            int idCounter = 1;

            while ((line = reader.readLine()) != null) {

                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                list.add(new Question(
                        idCounter++,
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim(),
                        parts[6].trim()
                ));
            }

            LOG.fine("Gameplay questions loaded from disk");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load gameplay questions from disk", e);
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


    private static void ensureQuestionsFileExists() {

        File file = getWritableQuestionsFile();
        if (file.exists()) return;

        try (InputStream is =
                QuestionsManagementController.class
                        .getResourceAsStream(CSV_RESOURCE_PATH)) {

            if (is == null) {
                LOG.severe("questions_data.csv missing in JAR");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                is.transferTo(fos);
            }

            LOG.fine("questions_data.csv copied to writable folder");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to ensure questions file exists", e);
        }
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
