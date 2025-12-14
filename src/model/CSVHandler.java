package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {

    private final String filePath;

    public CSVHandler(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads all questions from the CSV file.
     * Expected CSV Format (NO ID column):
     * question,correctAnswer,wrongAnswer1,wrongAnswer2,wrongAnswer3,difficultyLevel,gameLevel
     */
    public List<Question> readQuestions() {

        List<Question> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isHeader = true;
            int idCounter = 1;

            while ((line = br.readLine()) != null) {

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] p = parseCSVLine(line);

                if (p.length < 7) {
                    System.out.println("Invalid row: " + line);
                    continue;
                }

                // Auto-generate ID
                Question q = new Question(
                        idCounter++,
                        p[0],  // question text
                        p[1],  // correct
                        p[2],  // wrong 1
                        p[3],  // wrong 2
                        p[4],  // wrong 3
                        p[5],  // difficulty
                        p[6]   // game level
                );

                list.add(q);
            }

            System.out.println("Loaded " + list.size() + " questions from " + filePath);

        } catch (FileNotFoundException e) {
            System.out.println("CSV not found. It will be created when saving.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    /**
     * SAVE QUESTIONS BACK TO CSV
     */
    public void writeQuestions(List<Question> list) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            // Write header
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

            System.out.println("Saved " + list.size() + " questions to CSV.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * CSV Parser that supports quotes, commas, and cleans fields.
     */
    private String[] parseCSVLine(String line) {

        List<String> result = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
                sb.append(c); // keep quotes for cleaning step
                continue;
            }

            if (c == ',' && !insideQuotes) {
                result.add(cleanField(sb.toString()));
                sb = new StringBuilder();
                continue;
            }

            sb.append(c);
        }

        // last field
        result.add(cleanField(sb.toString()));

        return result.toArray(new String[0]);
    }

    /**
     * Removes wrapping quotes and cleans escaped quotes
     */
    private String cleanField(String field) {
        field = field.trim();

        // Remove leading and trailing quotes
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            field = field.substring(1, field.length() - 1);
        }

        // Replace doubled quotes ("") → "
        field = field.replace("\"\"", "\"");

        // Remove weird Excel quotes like ” “
        field = field.replace("”", "\"").replace("“", "\"");

        return field;
    }

    private String escape(String s) {
        return s.replace("\"", "\"\""); // escape double quotes
    }
}
