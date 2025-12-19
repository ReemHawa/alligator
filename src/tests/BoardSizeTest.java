package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import model.game;
import model.board;
import model.DifficultyLevel;

public class BoardSizeTest {

    @Test
    void testEasyBoardSize() {
        game g = new game(DifficultyLevel.EASY, "A", "B");
        board b = g.getBoard(0);

        assertEquals(9, b.getRows());
        assertEquals(9, b.getCols());
    }

    @Test
    void testMediumBoardSize() {
        game g = new game(DifficultyLevel.MEDIUM, "A", "B");
        board b = g.getBoard(0);

        assertEquals(13, b.getRows());
        assertEquals(13, b.getCols());
    }

    @Test
    void testHardBoardSize() {
        game g = new game(DifficultyLevel.HARD, "A", "B");
        board b = g.getBoard(0);

        assertEquals(16, b.getRows());
        assertEquals(16, b.getCols());
    }
}
