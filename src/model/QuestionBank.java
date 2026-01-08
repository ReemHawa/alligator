package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionBank {

    private static final Logger LOG = Logger.getLogger(QuestionBank.class.getName());

    private static final String CSV_RESOURCE_PATH = "/data/questions_data.csv";
    private final List<Question> questions = new ArrayList<>();
    private final Random rnd = new Random();

    public QuestionBank() {
        load();
    }

    private void load() {
        try (InputStream is = getClass().getResourceAsStream(CSV_RESOURCE_PATH)) {
            if (is == null) {
                LOG.severe("questions_data.csv not found at " + CSV_RESOURCE_PATH);
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;
                int id = 1;

                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    String[] p = line.split(",");
                    if (p.length < 7) continue;

                    Question q = new Question(
                            id++,
                            p[0].trim(),
                            p[1].trim(),
                            p[2].trim(),
                            p[3].trim(),
                            p[4].trim(),
                            p[5].trim(),
                            p[6].trim()
                    );
                    questions.add(q);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed loading questions from QuestionBank", e);
        }
    }

    public Question getRandomQuestionForGameLevel(DifficultyLevel level) {
        if (questions.isEmpty()) return null;

        String lvl = level.name(); // EASY / MEDIUM / HARD

        List<Question> filtered = new ArrayList<>();
        for (Question q : questions) {
            if (q.getGameLevel() != null
                    && q.getGameLevel().trim().equalsIgnoreCase(lvl)) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) {
            // fallback: return any question
            return questions.get(rnd.nextInt(questions.size()));
        }

        return filtered.get(rnd.nextInt(filtered.size()));
    }
}
