package controller;

import model.CSVHandler;
import model.gameHistory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class gameHistoryController {

    private List<gameHistory> historyEntries = new ArrayList<>();

    public gameHistoryController() {
        reload(); // always load latest
    }

    /** Reread CSV from disk so the table is always up to date */
    public final void reload() {
    	CSVHandler csv = new CSVHandler(gameController.getGameHistoryPath());
    	historyEntries = csv.readGameHistory();

    }

    public gameHistory getEntry(int index) {
        return historyEntries.get(index);
    }

    public int getNumberOfEntries() {
        return historyEntries.size();
    }

    // show history screen (always fresh)
    public void showHistoryCard() {
        reload();
        SwingUtilities.invokeLater(() -> new view.gameHistoryView(this));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new gameHistoryController().showHistoryCard());
    }
}
