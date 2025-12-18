package controller;

import model.CSVHandler;
import model.gameHistory;
import view.gameHistoryView;

import javax.swing.*;
import java.util.List;

public class gameHistoryController {

    private final List<gameHistory> historyEntries;

    public gameHistoryController() {
        CSVHandler csv = new CSVHandler("src/data/game_history.csv");
        historyEntries = csv.readGameHistory();
    }

    // return one game by index
    public gameHistory getEntry(int index) {
        return historyEntries.get(index);
    }

    public int getNumberOfEntries() {
        return historyEntries.size();
    }

    // show history screen
    public void showHistoryCard() {
        new gameHistoryView(this);
    }

    // small main to test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new gameHistoryController().showHistoryCard();
        });
    }
}

