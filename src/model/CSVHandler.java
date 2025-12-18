package model;

import model.gameHistory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {

    private final String filePath;

    public CSVHandler(String filePath) {
        this.filePath = filePath;
    }

    /* =====================================================
       =============== QUESTIONS HANDLING ==================
       ===================================================== */

    public List<Question> readQuestions() {

        List<Question> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isHeader = true;
            int idCounter = 1;

            while ((line = br.readLine()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] p = parseCSVLine(line);

                if (p.length < 7) continue;

                Question q = new Question(
                        idCounter++,
                        p[0],
                        p[1],
                        p[2],
                        p[3],
                        p[4],
                        p[5],
                        p[6]
                );

                list.add(q);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void writeQuestions(List<Question> list) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

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
            e.printStackTrace();
        }
    }

    /* =====================================================
       ============== GAME HISTORY HANDLING =================
       ===================================================== */

    public void appendGameHistory(gameHistory e) {
        File file = new File(filePath);

        try {
            // make sure parent folders exist
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            boolean newFile = !file.exists();

            System.out.println("✅ Writing game history to: " + file.getAbsolutePath());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                if (newFile || file.length() == 0) {
                    bw.write("date,playerA,playerB,result,time,score,level");
                    bw.newLine();
                }

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
            }

            System.out.println("✅ Appended row: " + e.getDate() + " | " + e.getPlayerA() + " vs " + e.getPlayerB());

        } catch (IOException ex) {
            System.out.println("❌ FAILED to append game history!");
            ex.printStackTrace();
        }
    }

    public List<gameHistory> readGameHistory() {
        List<gameHistory> list = new ArrayList<>();
        File file = new File(filePath);

        System.out.println("📖 Reading game history from: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("⚠ game_history.csv does not exist yet.");
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1); // keep empty fields

                if (p.length < 7) {
                    System.out.println("⚠ Skipping invalid row: " + line);
                    continue;
                }

                String date = p[0].trim();
                String playerA = p[1].trim();
                String playerB = p[2].trim();
                String result = p[3].trim();
                String duration = p[4].trim();

                int score;
                try {
                    score = Integer.parseInt(p[5].trim());
                } catch (NumberFormatException nfe) {
                    System.out.println("⚠ Bad score, defaulting to 0. Row: " + line);
                    score = 0;
                }

                String level = p[6].trim();

                list.add(new gameHistory(date, playerA, playerB, result, duration, score, level));
            }

            System.out.println("✅ Loaded history entries: " + list.size());

        } catch (IOException e) {
            System.out.println("❌ FAILED to read game history!");
            e.printStackTrace();
        }

        return list;
    }

    private String safe(String s) {
        if (s == null) return "";
        // prevent commas breaking CSV
        return s.replace(",", " ");
    }

    /* ===================================================== */

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

    private String escape(String s) {
        return s.replace("\"", "\"\"");
    }
    
    public static void main(String[] args) {
        CSVHandler csv = new CSVHandler("src/data/game_history.csv");
        csv.appendGameHistory(new gameHistory(
                "17/12/2025",
                "testA",
                "testB",
                "won",
                "05:12",
                123,
                "EASY"
        ));

        System.out.println("Now reading back:");
        System.out.println(csv.readGameHistory().size());
    }

}
