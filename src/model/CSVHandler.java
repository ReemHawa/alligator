package model;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVHandler {

    private static final Logger LOG = Logger.getLogger(CSVHandler.class.getName());

    // ========= ONE consistent runtime folder for ALL PCs =========
    private static final String APP_DIR_NAME = "DualMinesweeper";
    private static final String DATA_DIR_NAME = "data";

    // ========= ONE consistent file name =========
    private static final String QUESTIONS_FILE_NAME = "questionsDATA.csv";
    private static final String QUESTIONS_RESOURCE_PATH = "data/questionsDATA.csv"; // inside src/data/

    private final String filePath; // used for game history only

    public CSVHandler(String filePath) {
        this.filePath = filePath;
    }

    // =========================
    // Paths
    // =========================
    public static File getDataDir() {
        String baseDir = System.getProperty("user.home")
                + File.separator + APP_DIR_NAME
                + File.separator + DATA_DIR_NAME;

        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getWritableQuestionsFile() {
        return new File(getDataDir(), QUESTIONS_FILE_NAME);
    }

    // =========================
    // Ensure Questions file exists (copy from resources once)
    // =========================
    private static void ensureQuestionsFileExists() {
        File outFile = getWritableQuestionsFile();
        if (outFile.exists()) return;

        try (InputStream is = CSVHandler.class.getClassLoader().getResourceAsStream(QUESTIONS_RESOURCE_PATH)) {

            if (is == null) {
                LOG.severe("questionsDATA.csv missing in JAR resources at: " + QUESTIONS_RESOURCE_PATH);
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(outFile, false)) {
                is.transferTo(fos);
            }

            LOG.info("questionsDATA.csv copied to: " + outFile.getAbsolutePath());

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to ensure questions file exists", e);
        }
    }

    /**
     * OPTIONAL: Force reset questions file from the resource (overwrites user file).
     * Use this when you want to "delete everything and re-upload".
     */
    public static void resetQuestionsFromResource() {
        File outFile = getWritableQuestionsFile();

        try (InputStream is = CSVHandler.class.getClassLoader().getResourceAsStream(QUESTIONS_RESOURCE_PATH)) {

            if (is == null) {
                LOG.severe("questionsDATA.csv missing in JAR resources at: " + QUESTIONS_RESOURCE_PATH);
                return;
            }

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(outFile, false)) {
                is.transferTo(fos);
            }

            LOG.info("questionsDATA.csv RESET from resources -> " + outFile.getAbsolutePath());

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to reset questions file", e);
        }
    }

    /**
     * Reads questions from user-writable questionsDATA.csv
     * Columns:
     * ID,Question,Difficulty,A,B,C,D,Correct Answer
     */
    public List<Question> readQuestions() {
        ensureQuestionsFileExists();

        File file = getWritableQuestionsFile();
        System.out.println("✅ QUESTIONS FILE USED = " + file.getAbsolutePath());

        // Try UTF-8 first, then Windows-1252 (Excel on Windows sometimes saves this)
        List<Charset> tries = Arrays.asList(StandardCharsets.UTF_8, Charset.forName("windows-1252"));

        Exception last = null;

        for (Charset cs : tries) {
            List<Question> list = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), cs))) {

                String line;
                boolean isHeader = true;

                while ((line = br.readLine()) != null) {

                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    if (line.trim().isEmpty()) continue;

                    List<String> p = parseCsvLine(line);
                    
                 // ✅ If we got extra columns because of commas inside answers,
                 // fix it by forcing the format to 8 columns:
                 // 0=id, 1=question, 2=difficulty, 3=A, 4=B, 5=C, 6=D (merged), 7=correct (last)
                 if (p.size() > 8) {
                     String correct = p.get(p.size() - 1);              // last token is the correct letter
                     String d = String.join(",", p.subList(6, p.size() - 1)); // merge everything from D until before correct

                     List<String> fixed = new ArrayList<>(8);
                     fixed.add(p.get(0));
                     fixed.add(p.get(1));
                     fixed.add(p.get(2));
                     fixed.add(p.get(3));
                     fixed.add(p.get(4));
                     fixed.add(p.get(5));
                     fixed.add(d);
                     fixed.add(correct);
                     p = fixed;
                 }

                    if (p.size() < 8) continue;

                    // Skip fully empty rows
                    boolean allEmpty = true;
                    for (String s : p) {
                        if (s != null && !s.trim().isEmpty()) { allEmpty = false; break; }
                    }
                    if (allEmpty) continue;

                    int id;
                    try { id = Integer.parseInt(p.get(0).trim()); }
                    catch (Exception ex) { continue; }

                    Question q = new Question(
                            id,
                            p.get(1).trim(), // Question
                            p.get(2).trim(), // Difficulty
                            p.get(3).trim(), // A
                            p.get(4).trim(), // B
                            p.get(5).trim(), // C
                            p.get(6).trim(), // D
                            p.get(7).trim()  // Correct letter
                    );

                    list.add(q);
                }

                System.out.println("✅ CSV charset OK: " + cs + " | loaded=" + list.size());
                return list; // success

            } catch (Exception e) {
                last = e;
                // try next charset
            }
        }

        LOG.log(Level.SEVERE, "Failed to read questions (all charsets). Last error:", last);
        return new ArrayList<>();
    }

    // Proper CSV parser (supports quotes + commas inside quotes)
    private static List<String> parseCsvLine(String line) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // escaped quote ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                res.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        res.add(cur.toString().trim());

        // Strip wrapping quotes if any
        for (int i = 0; i < res.size(); i++) {
            res.set(i, stripWrappingQuotes(res.get(i)));
        }

        return res;
    }

    private static String stripWrappingQuotes(String s) {
        if (s == null) return "";
        s = s.trim();

        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Writes questions back to user-writable questionsDATA.csv
     */
    public void writeQuestions(List<Question> list) {
        ensureQuestionsFileExists();

        File file = getWritableQuestionsFile();
        System.out.println("✅ SAVING QUESTIONS TO = " + file.getAbsolutePath());

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            bw.write("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            bw.newLine();

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

            LOG.info("questionsDATA.csv SAVED");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to write questions", e);
        }
    }

    private String escape(String s) {
        if (s == null) return "\"\"";
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    // =====================================================
    // GAME HISTORY (unchanged idea, but safe writes)
    // =====================================================

    public void appendGameHistory(gameHistory e) {
        ensureGameHistoryExists();

        File file = new File(filePath);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            String line = String.format(
                    "%s,%s,%s,%s,%s,%d,%s",
                    safe(e.getDate()),
                    safe(e.getPlayerA()),
                    safe(e.getPlayerB()),
                    safe(e.getResult()),
                    safe(e.getDuration()),
                    e.getScore(),
                    safe(e.getLevel())
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to append game history", ex);
        }
    }

    public List<gameHistory> readGameHistory() {
        ensureGameHistoryExists();

        List<gameHistory> list = new ArrayList<>();
        File file = new File(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                // simple CSV (history usually has no quotes)
                String[] p = line.split(",", -1);
                if (p.length < 7) continue;

                list.add(new gameHistory(
                        p[0].trim(),
                        p[1].trim(),
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        Integer.parseInt(p[5].trim()),
                        p[6].trim()
                ));
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to read game history", e);
        }

        return list;
    }

    private String safe(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    private void ensureGameHistoryExists() {
        try {
            File file = new File(filePath);

            if (file.exists()) return;

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            // if no resource -> create header
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("date,playerA,playerB,result,time,score,level");
                bw.newLine();
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to ensure game history exists", e);
        }
    }
}
