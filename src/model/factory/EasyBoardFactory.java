package model.factory;

public class EasyBoardFactory extends BoardFactory {

    @Override
    protected BoardConfig getConfig() {
        return new BoardConfig(
                9,   // rows
                9,   // cols
                10,  // mines
                2,   // surprises
                6,   // questions
                10   // startingLives
        );
    }
}
