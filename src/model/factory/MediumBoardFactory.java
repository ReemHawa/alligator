package model.factory;

public class MediumBoardFactory extends BoardFactory {

    @Override
    protected BoardConfig getConfig() {
        return new BoardConfig(
                13,  // rows
                13,  // cols
                26,  // mines
                3,   // surprises
                7,   // questions
                8    // startingLives
        );
    }
}
