package model.factory;

public class HardBoardFactory extends BoardFactory {

    @Override
    protected BoardConfig getConfig() {
        return new BoardConfig(
                16,  // rows
                16,  // cols
                44,  // mines
                4,   // surprises
                11,  // questions
                6    // startingLives
        );
    }
}
