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
        // placeholders must be treated as empty
        String p1 = view.isPlayerAPlaceholder() ? "" : view.getPlayerAName();
        String p2 = view.isPlayerBPlaceholder() ? "" : view.getPlayerBName();

        String err1 = validateName(p1);
        String err2 = validateName(p2);

        view.setNameError(0, err1);
        view.setNameError(1, err2);

        view.setPlayEnabled(err1 == null && err2 == null);
    }

    public void onPlayClicked() {
        String p1 = view.isPlayerAPlaceholder() ? "" : view.getPlayerAName();
        String p2 = view.isPlayerBPlaceholder() ? "" : view.getPlayerBName();

        String err1 = validateName(p1);
        String err2 = validateName(p2);

        view.setNameError(0, err1);
        view.setNameError(1, err2);

        if (err1 != null || err2 != null) {
            view.setPlayEnabled(false);
            return;
        }

        DifficultyLevel diff = DifficultyLevel.valueOf(level);

        gameController gc = new gameController(diff, p1, p2, homeScreen);
        gc.startGame();

        view.setVisible(false);
    }

    // ✅ NEW validation: returns null if valid, otherwise returns a message
    private String validateName(String name) {
        if (name == null) return "Name is required.";

        name = name.trim();
        if (name.isEmpty()) return "Name is required.";
        if (name.length() < 4) return "Name must be at least 4 characters.";
        if (name.length() > 14) return "Name must be at most 14 characters.";

        // letters only (no spaces, numbers, symbols)
        if (!name.matches("^[A-Za-z]+$")) {
            return "Letters only (A–Z). No spaces, numbers, or symbols.";
        }

        return null;
    }

    // ✅ COMPATIBILITY: if other code still calls isValidName(...)
    public boolean isValidName(String name) {
        return validateName(name) == null;
    }
}
