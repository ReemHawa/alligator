package controller;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import model.Question;
import view.HomeScreen;
import view.QuestionTableModel;
import view.QuestionsManagementScreen;

public class QuestionsManagementController {

    private static final Logger LOG = Logger.getLogger(QuestionsManagementController.class.getName());

    private QuestionsManagementScreen screen;
    private HomeScreen homeScreen;

    private static final String CSV_RESOURCE_PATH = "/data/questionsDATA.csv";

    // gameplay cache
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

        return new File(dir, "questionsDATA.csv");
    }

    public QuestionsManagementController(HomeScreen homeScreen) {
        this.homeScreen = homeScreen;

        List<Question> questions = loadQuestionsFromCSV();
        LOG.fine("Questions loaded: " + questions.size());

        screen = new QuestionsManagementScreen(questions);
        initListeners();

        screen.setVisible(true);
        homeScreen.setVisible(false);
    }

    private List<Question> loadQuestionsFromCSV() {
        ensureQuestionsFileExists();

        List<Question> list = new ArrayList<>();
        File file = getWritableQuestionsFile();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1);
                if (p.length < 8) continue;

                int id = safeParseInt(p[0].trim(), 0);
                if (id == 0) continue;

                list.add(new Question(
                        id,
                        p[1].trim(), // Question
                        p[2].trim(), // Difficulty
                        p[3].trim(), // A
                        p[4].trim(), // B
                        p[5].trim(), // C
                        p[6].trim(), // D
                        p[7].trim()  // Correct letter
                ));
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load questions from CSV", e);
        }

        return list;
    }

    private void initListeners() {

        screen.getBackButton().addActionListener(e -> goBackHome());

        screen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goBackHome();
            }
        });

        screen.getQuestionsTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                JTable table = screen.getQuestionsTable();
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());

                if (viewRow < 0 || viewCol < 0) return;

                // ✅ convert view row -> model row (important when sorting/filtering)
                int modelRow = table.convertRowIndexToModel(viewRow);

                QuestionTableModel model = screen.getTableModel();

                if (viewCol == 0) { // DELETE
                    Question q = model.getQuestionAt(modelRow);

                    int confirm = JOptionPane.showConfirmDialog(
                            screen,
                            "Delete question #" + q.getQuestionID() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {

                        // commit editing if any
                        if (table.isEditing()) {
                            var editor = table.getCellEditor();
                            if (editor != null) editor.stopCellEditing();
                        }

                        model.removeQuestionAt(modelRow);
                        saveToCSV();
                    }

                } else if (viewCol == 1) { // EDIT TOGGLE

                    // commit last edit if user was typing
                    if (table.isEditing()) {
                        var editor = table.getCellEditor();
                        if (editor != null) editor.stopCellEditing();
                    }

                    // If already editing this row -> finish + save + confirmation
                    if (model.isRowInEditMode(modelRow)) {

                        model.finishEdit();
                        saveToCSV();

                        JOptionPane.showMessageDialog(
                                screen,
                                "Changes saved successfully ✅",
                                "Edit Completed",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                    } else {
                        // Start editing this row (and stop editing any other)
                        model.startEditRow(modelRow);

                        JOptionPane.showMessageDialog(
                                screen,
                                "Edit mode enabled for this row.\n" +
                                "Edit cells directly, then click ✎ again to save.",
                                "Edit Mode",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // focus first editable cell (Question column = 3)
                        table.requestFocus();
                        table.changeSelection(viewRow, 3, false, false);
                    }
                }
            }
        });
    }

    private void saveToCSV() {
        ensureQuestionsFileExists();
        File file = getWritableQuestionsFile();

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            bw.write("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            bw.newLine();

            List<Question> list = screen.getTableModel().getAllQuestions();

            for (Question q : list) {
                String line = String.join(",",
                        String.valueOf(q.getQuestionID()),
                        escape(q.getQuestionText()),
                        escape(q.getDifficultyLevel()),
                        escape(q.getOptionA()),
                        escape(q.getOptionB()),
                        escape(q.getOptionC()),
                        escape(q.getOptionD()),
                        escape(q.getCorrectLetter())
                );
                bw.write(line);
                bw.newLine();
            }

            cachedQuestionsForGame = null; // clear cache after save
            LOG.fine("Questions saved + cache cleared");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save questions to CSV", e);
            JOptionPane.showMessageDialog(
                    screen,
                    "Failed to save questions file.\nCheck permissions and try again.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace(",", " ");
    }

    // =========================
    // GAMEPLAY API (static)
    // =========================

    public static List<Question> loadQuestionsForGame() {

        if (cachedQuestionsForGame != null) return cachedQuestionsForGame;

        ensureQuestionsFileExists();
        File file = getWritableQuestionsFile();

        List<Question> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1);
                if (p.length < 8) continue;

                int id = safeParseInt(p[0].trim(), 0);
                if (id == 0) continue;

                list.add(new Question(
                        id,
                        p[1].trim(), // Question
                        p[2].trim(), // Difficulty
                        p[3].trim(), // A
                        p[4].trim(), // B
                        p[5].trim(), // C
                        p[6].trim(), // D
                        p[7].trim()  // Correct letter
                ));
            }

            LOG.fine("Gameplay questions loaded from disk");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load gameplay questions from disk", e);
        }

        cachedQuestionsForGame = list;
        return list;
    }

    public static Question getRandomQuestionAny() {
        List<Question> all = loadQuestionsForGame();
        if (all.isEmpty()) return null;
        return all.get(RNG.nextInt(all.size()));
    }

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

        if (chosen == null) return false;
        return q.isCorrect(chosen.toString());
    }

    private void goBackHome() {
        // commit edits before save
        JTable t = screen.getQuestionsTable();
        if (t.isEditing()) {
            var ed = t.getCellEditor();
            if (ed != null) ed.stopCellEditing();
        }
        screen.getTableModel().finishEdit();

        saveToCSV();

        if (screen != null) screen.dispose();

        if (homeScreen != null) {
            homeScreen.setVisible(true);
            homeScreen.toFront();
            homeScreen.requestFocus();
        } else {
            new HomeScreen().setVisible(true);
        }
    }

    private static void ensureQuestionsFileExists() {

        File file = getWritableQuestionsFile();
        if (file.exists()) return;

        try (InputStream is =
                     QuestionsManagementController.class.getResourceAsStream(CSV_RESOURCE_PATH)) {

            if (is == null) {
                LOG.severe("Questions CSV missing in resources: " + CSV_RESOURCE_PATH);
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                is.transferTo(fos);
            }

            LOG.fine("questionsDATA.csv copied to writable folder");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to ensure questions file exists", e);
        }
    }

    private static int safeParseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
