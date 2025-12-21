package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import model.game;
import model.DifficultyLevel;

public class LivesTest {

    @Test
    void testEasyLivesInitialization() {
        game g = new game(DifficultyLevel.EASY, "A", "B");
        assertEquals(10, g.getLivesRemaining());
        assertFalse(g.isGameOver());
    }

    @Test
    void testMediumLivesInitialization() {
        game g = new game(DifficultyLevel.MEDIUM, "A", "B");
        assertEquals(8, g.getLivesRemaining());
        assertFalse(g.isGameOver());
    }

    @Test
    void testHardLivesInitialization() {
        game g = new game(DifficultyLevel.HARD, "A", "B");
        assertEquals(6, g.getLivesRemaining());
        assertFalse(g.isGameOver());
    }

    @Test
    void testLoseLifeLastLifeGameOver() {
        game g = new game(DifficultyLevel.HARD, "A", "B"); // starts with 6

        for (int i = 0; i < 5; i++) {
            g.loseLife();
        }
        assertEquals(1, g.getLivesRemaining());
        assertFalse(g.isGameOver());

        g.loseLife();
        assertEquals(0, g.getLivesRemaining());
        assertTrue(g.isGameOver());
    }
}
