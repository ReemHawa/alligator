package controller;

import view.gameHistoryView;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class gameHistoryController {

    // list of all past games
    private final List<GameHistoryEntry> historyEntries = new ArrayList<>();

    public gameHistoryController() {
        // load data from CSV instead of hardcoded values
        loadHistoryFromCSV();
    }

    // ===== LOAD HISTORY FROM CSV FILE =====
    private void loadHistoryFromCSV() {

        try (InputStream is = getClass().getResourceAsStream("/data/game_history.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                // skip header row
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // split by comma
                String[] parts = line.split(",");

                // need exactly 7 columns
                if (parts.length < 7) continue;

                String date    = parts[0].trim();
                String playerA = parts[1].trim();
                String playerB = parts[2].trim();
                String result  = parts[3].trim();
                String duration = parts[4].trim();
                int score      = Integer.parseInt(parts[5].trim());
                String level   = parts[6].trim();

                historyEntries.add(new GameHistoryEntry(
                        date, playerA, playerB, level, result, duration, score
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠ CSV load failed! Check file location or formatting.");

            // fallback if CSV missing
            historyEntries.add(new GameHistoryEntry(
                    "N/A", "N/A", "N/A",
                    "N/A", "N/A", "0 min", 0));
        }
    }

    // return one game by index
    public GameHistoryEntry getEntry(int index) {
        if (index < 0 || index >= historyEntries.size()) {
            throw new IllegalArgumentException("Invalid history index");
        }
        return historyEntries.get(index);
    }

    public int getNumberOfEntries() {
        return historyEntries.size();
    }

    // SHOW THE WHOLE DATA 
    public void showHistoryCard() {
        new gameHistoryView(this);
    }

    // model for one game
    public static class GameHistoryEntry {
        private final String date;
        private final String playerA;
        private final String playerB;
        private final String level;
        private final String result;
        private final String duration;
        private final int score;

        public GameHistoryEntry(String date, String playerA, String playerB,
                                String level, String result,
                                String duration, int score) {
            this.date = date;
            this.playerA = playerA;
            this.playerB = playerB;
            this.level = level;
            this.result = result;
            this.duration = duration;
            this.score = score;
        }

        public String getDate()     { return date; }
        public String getPlayerA()  { return playerA; }
        public String getPlayerB()  { return playerB; }
        public String getLevel()    { return level; }
        public String getResult()   { return result; }
        public String getDuration() { return duration; }
        public int    getScore()    { return score; }
    }

    // small main to test – shows card for FIRST game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gameHistoryController controller = new gameHistoryController();
            controller.showHistoryCard();   
        });
    }
}
