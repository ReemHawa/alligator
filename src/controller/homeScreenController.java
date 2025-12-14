package controller;

import view.HomeScreen;
import view.GameRulesScreen;

public class homeScreenController {

    private HomeScreen view;

    public homeScreenController(HomeScreen view) {
        this.view = view;

        view.getBtnStartNewGame().addActionListener(e -> openGameRules());
        view.getBtnViewHistory().addActionListener(e -> openHistory());
        view.getBtnViewQuestions().addActionListener(e -> openQuestions());
    }

    private void openGameRules() {
        GameRulesScreen rules = new GameRulesScreen(view);
        rules.setVisible(true);
        view.setVisible(false);
    }

    private void openHistory() {
        gameHistoryController historyController = new gameHistoryController();
        historyController.showHistoryCard();
    }

    private void openQuestions() {
    	 new QuestionsManagementController(view);
    }
}
