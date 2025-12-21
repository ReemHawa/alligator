package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {

    private final String filePath;
    
    public static final String GAME_HISTORY_PATH =
            System.getProperty("user.home")
            + File.separator
            + ".minesweeper"
            + File.separator
            + "game_history.csv";

    public CSVHandler(String filePath) {
        this.filePath = filePath;
    }

    /* =====================================================
       =============== QUESTIONS HANDLING ==================
       ===================================================== */

    public List<Question> readQuestions() {

        List<Question> list = new ArrayList<>();

        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("data/questions_data.csv")) {

            if (is == null) {
                System.out.println("❌ questions_data.csv NOT FOUND in JAR");
                return list;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isHeader = true;
            int id = 1;

            while ((line = br.readLine()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1);
                if (p.length < 7) continue;

                list.add(new Question(
                        id++,
                        p[0].trim(),
                        p[1].trim(),
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        p[5].trim(),
                        p[6].trim()
                ));
            }

            System.out.println("✅ Loaded questions: " + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public void writeQuestions(List<Question> list) {

        File file = new File(filePath);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            bw.write("question,correctAnswer,wrongAnswer1,wrongAnswer2,wrongAnswer3,difficultyLevel,gameLevel");
            bw.newLine();

            for (Question q : list) {
                String line = String.format(
                        "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
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

        } catch (IOException e) {
            System.out.println("❌ FAILED writing questions!");
            e.printStackTrace();
        }
    }

    /* =====================================================
       ============== GAME HISTORY HANDLING =================
       ===================================================== */

    public void appendGameHistory(gameHistory e) {

        ensureGameHistoryExists();   // ⭐ ADD THIS LINE

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
            ex.printStackTrace();
        }
    }


    public List<gameHistory> readGameHistory() {

        ensureGameHistoryExists();   // ⭐ ADD THIS LINE

        List<gameHistory> list = new ArrayList<>();
        File file = new File(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {

                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

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
            e.printStackTrace();
        }

        return list;
    }


    /* ===================================================== */

    private String safe(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }

    private String[] parseCSVLine(String line) {

        List<String> result = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
                continue;
            }
            if (c == ',' && !insideQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        result.add(sb.toString().trim());
        return result.toArray(new String[0]);
    }
    
    private void ensureGameHistoryExists() {

        try {
            File file = new File(filePath);

            if (file.exists()) return; // already copied

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            // load template from JAR
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/game_history.csv");

            if (is == null) {
                // fallback: create header manually
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write("date,playerA,playerB,result,time,score,level");
                    bw.newLine();
                }
                return;
            }

            // copy resource → disk
            try (FileOutputStream fos = new FileOutputStream(file)) {
                is.transferTo(fos);
            }

            System.out.println("✅ game_history.csv copied to disk: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
