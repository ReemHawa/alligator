package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;

public class QuestionBank {

    private static final Logger LOG = Logger.getLogger(QuestionBank.class.getName());
    private static final String CSV_RESOURCE_PATH = "/data/questionsDATA.csv";

    private final List<Question> questions = new ArrayList<>();
    private final Random rnd = new Random();

    // same writable location
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

    // copy resource only if first time
    private static void ensureQuestionsFileExists() {
        File file = getWritableQuestionsFile();

        try (InputStream is =
                QuestionBank.class.getResourceAsStream(CSV_RESOURCE_PATH)) {

            if (is == null) {
                LOG.severe("questionsDATA.csv missing in resources");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                is.transferTo(fos);
            }

            LOG.info("Questions.csv synced from resources");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed syncing questions file", e);
        }
    }


    public QuestionBank() {
        load();
    }

    private void load() {
    	
    	
    	
        ensureQuestionsFileExists();

        File file = getWritableQuestionsFile();

        LOG.info("GAME loading questions from: " + file.getAbsolutePath());

        questions.clear();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

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

                questions.add(new Question(
                        id,
                        p[1].trim(),
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        p[5].trim(),
                        p[6].trim(),
                        p[7].trim()
                ));
            }
        LOG.info("USING FILE = " + getWritableQuestionsFile().getAbsolutePath());

            LOG.info("GAME LOADED QUESTIONS: " + questions.size());

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed loading questions from disk", e);
        }
        
        
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(rnd.nextInt(questions.size()));
    }
    
    
}
