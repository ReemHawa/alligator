package controller;

import model.DifficultyLevel;
import view.PlayersNamesScreen;
import view.HomeScreen;

public class PlayersNamesController {

    private final PlayersNamesScreen view;
    private final String level;
    private final HomeScreen homeScreen;

    public PlayersNamesController(PlayersNamesScreen view, String level, HomeScreen homeScreen) {
        this.view = view;
        this.level = level;
        this.homeScreen = homeScreen;
    }

    public void onInputChanged() {
        String p1 = view.getPlayerAName();
        String p2 = view.getPlayerBName();

        boolean valid =
                isValidName(p1) &&
                isValidName(p2);

        view.setPlayEnabled(valid);
    }

    public void onPlayClicked() {
        String p1 = view.getPlayerAName();
        String p2 = view.getPlayerBName();

        if (!isValidName(p1) || !isValidName(p2)) {
            view.showError(
                "Names must contain at least 4 letters.\n" +
                "Letters only — no numbers, spaces, or symbols."
            );
            return;
        }

        DifficultyLevel diff = DifficultyLevel.valueOf(level);
        
        /// to start the game 

        gameController gc = new gameController(diff, p1, p2, homeScreen);
        gc.startGame();

        view.setVisible(false);
    }
      /// the names must have only letters and each name must has at least 4 letterss
    public static boolean isValidName(String name) {
        if (name == null) return false;
        if (name.length() < 4) return false;
        if (name.contains(" ")) return false;
        return true;
    }

}
