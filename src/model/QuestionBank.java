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
    private static final String CSV_RESOURCE_PATH = "/data/Questions.csv";

    private final List<Question> questions = new ArrayList<>();
    private final Random rnd = new Random();

    public QuestionBank() {
        load();
    }

    private void load() {
        try (InputStream is = getClass().getResourceAsStream(CSV_RESOURCE_PATH)) {
            if (is == null) {
                LOG.severe("Questions.csv not found at " + CSV_RESOURCE_PATH);
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;

                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;

                    String[] p = line.split(",", -1);
                    if (p.length < 8) continue;

                    int id;
                    try { id = Integer.parseInt(p[0].trim()); }
                    catch (Exception ex) { continue; }

                    Question q = new Question(
                            id,
                            p[1].trim(), // Question
                            p[2].trim(), // Difficulty
                            p[3].trim(), // A
                            p[4].trim(), // B
                            p[5].trim(), // C
                            p[6].trim(), // D
                            p[7].trim()  // Correct letter
                    );

                    questions.add(q);
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed loading questions from QuestionBank", e);
        }
    }

    /** New: random question from whole bank (no game level). */
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(rnd.nextInt(questions.size()));
    }
}
