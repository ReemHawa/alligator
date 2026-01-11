package model.factory;

import model.DifficultyLevel;

public class BoardFactoryProvider {

    public static BoardFactory getFactory(DifficultyLevel level) {
        switch (level) {
            case EASY:
                return new EasyBoardFactory();
            case MEDIUM:
                return new MediumBoardFactory();
            case HARD:
                return new HardBoardFactory();
            default:
                return new EasyBoardFactory();
        }
    }
}
