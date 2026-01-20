package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVHandler {

    private static final Logger LOG = Logger.getLogger(CSVHandler.class.getName());

    private final String filePath;

    public CSVHandler(String filePath) {
        this.filePath = filePath;
    }

    // ===== Writable questions file (same location you already use) =====
    private static File getWritableQuestionsFile() {
        String baseDir = System.getProperty("user.home")
                + File.separator
                + ".minesweeper"
                + File.separator
                + "data";

        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();

        return new File(dir, "Questions.csv");
    }

    /* =====================================================
       =============== QUESTIONS HANDLING ==================
       ===================================================== */

    private static void ensureQuestionsFileExists() {
        File outFile = getWritableQuestionsFile();
        if (outFile.exists()) return;

        try (InputStream is = CSVHandler.class
                .getClassLoader()
                .getResourceAsStream("data/Questions.csv")) {

            if (is == null) {
                LOG.severe("Questions.csv missing in JAR");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                is.transferTo(fos);
            }

            LOG.fine("Questions.csv copied to writable location");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to ensure questions file exists", e);
        }
    }

    /**
     * Reads questions from writable Questions.csv.
     * Expected columns:
     * ID,Question,Difficulty,A,B,C,D,Correct Answer
     */
    public List<Question> readQuestions() {

        ensureQuestionsFileExists();

        List<Question> list = new ArrayList<>();
        File file = getWritableQuestionsFile();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)
        )) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {

                if (isHeader) { 
                    isHeader = false; 
                    continue; 
                }

                if (line.trim().isEmpty()) continue;

                // ✅ parse with quotes support (+ comma/semicolon support)
                String[] p = splitCsvLineFlexible(line);

                // must have 8 columns
                if (p.length < 8) continue;

                // ✅ skip empty rows like: ,,,,,,,
                boolean allEmpty = true;
                for (String s : p) {
                    if (s != null && !s.trim().isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) continue;

                int id;
                try {
                    id = Integer.parseInt(p[0].trim());
                } catch (Exception ex) {
                    continue; // bad id row
                }

                Question q = new Question(
                        id,
                        p[1].trim(), // Question
                        p[2].trim(), // Difficulty
                        p[3].trim(), // A
                        p[4].trim(), // B
                        p[5].trim(), // C
                        p[6].trim(), // D
                        p[7].trim()  // Correct Answer letter
                );

                list.add(q);
            }

            LOG.fine("Loaded questions from writable file");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to read questions", e);
        }

        return list;
    }

    private String[] parseCsvLine(String line, char delimiter) {
        List<String> out = new ArrayList<>();
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
            } else if (ch == delimiter && !inQuotes) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString().trim());

        // ניקוי גרשיים/אפוסטרוף מיותרים (אם יש)
        for (int i = 0; i < out.size(); i++) {
            out.set(i, stripWrappingQuotes(out.get(i)));
        }

        return out.toArray(new String[0]);
    }

    private String stripWrappingQuotes(String s) {
        if (s == null) return "";
        s = s.trim();

        // remove wrapping "..."
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }

        // לפעמים מופיע ' בתחילת מחרוזת (Excel/העתקה) -> ננקה אם זה רק עטיפה
        if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
            s = s.substring(1, s.length() - 1);
        }

        return s;
    }

    private String[] splitCsvLineFlexible(String line) {
        // נסה פסיק
        String[] comma = parseCsvLine(line, ',');
        if (comma.length >= 8) return comma;

        // נסה נקודה-פסיק
        return parseCsvLine(line, ';');
    }

    
   /* private String[] splitCsvLineFlexible(String line) {
        // קודם ננסה פסיק
        String[] comma = splitByDelimiter(line, ",");
        if (comma.length >= 8) return comma;

        // אם לא הצליח - ננסה נקודה פסיק (CSV של אקסל בהרבה מחשבים)
        String[] semicolon = splitByDelimiter(line, ";");
        return semicolon;
    }*/

   /* private String[] splitByDelimiter(String line, String delimiter) {
        // split delimiter not inside quotes
        String regex = java.util.regex.Pattern.quote(delimiter)
                + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        String[] parts = line.split(regex, -1);

        for (int i = 0; i < parts.length; i++) {
            parts[i] = unquote(parts[i].trim());
        }
        return parts;
    }

    private String unquote(String s) {
        if (s == null) return "";
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }

*/
    /**
     * Writes questions to writable Questions.csv with NEW format.
     */
    public void writeQuestions(List<Question> list) {

        ensureQuestionsFileExists();
        File file = getWritableQuestionsFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

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

            LOG.fine("Questions.csv SAVED (new format)");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to write questions", e);
        }
    }

    /* =====================================================
       ============== GAME HISTORY HANDLING =================
       ===================================================== */

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

              //  String[] p = line.split(",", -1);
                String[] p = splitCsvLineFlexible(line);

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

    /* ===================================================== */

   /* private String[] splitCsvLine(String line) {
        // Split by commas that are NOT inside quotes
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        // remove wrapping quotes + unescape double quotes
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unquote(parts[i].trim());
        }
        return parts;
    }
    */
    
    
    
   /* private String clean(String s) {
        if (s == null) return "";
        s = s.trim();

        // remove wrapping quotes
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }

        return s.replace("\"\"", "\"");
    }

    */
    
    private String safe(String s) {
        return s == null ? "" : s.replace(",", " ");
    }
    private String escape(String s) {
        if (s == null) return "\"\"";
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void ensureGameHistoryExists() {
        try {
            File file = new File(filePath);

            if (file.exists()) return;

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/game_history.csv");

            if (is == null) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write("date,playerA,playerB,result,time,score,level");
                    bw.newLine();
                }
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                is.transferTo(fos);
            }

            LOG.fine("game_history.csv copied to disk: " + filePath);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to ensure game history exists", e);
        }
    }
}
