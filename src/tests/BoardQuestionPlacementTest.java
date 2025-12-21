package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.board;
import model.cellType;

public class BoardQuestionPlacementTest {

    private int countQuestionCells(board b) {
        int cnt = 0;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.getType(r, c) == cellType.question) cnt++;
            }
        }
        return cnt;
    }

    @Test
    void testPlaceQuestions_AllPlaced_WhenNoMines() {
        board b = new board(6, 6, 0, 0, 7);

        int actual = countQuestionCells(b);
        assertEquals(7, actual);

        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.getType(r, c) == cellType.question) {
                    assertFalse(b.isMine(r, c));
                    assertEquals(0, b.getSurroundingMines(r, c));
                }
            }
        }
    }

    @Test
    void testQuestionsNeverOnNumbersOrMines() {
        board b = new board(9, 9, 10, 0, 6);

        int placed = countQuestionCells(b);
        assertTrue(placed <= 6);

        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.getType(r, c) == cellType.question) {
                    assertFalse(b.isMine(r, c));
                    assertEquals(0, b.getSurroundingMines(r, c));
                }
            }
        }
    }

    @Test
    void testRevealQuestionMarksRevealed() {
        board b = new board(5, 5, 0, 0, 3);

        int qr = -1, qc = -1;
        outer:
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.isQuestionCell(r, c)) {
                    qr = r; qc = c;
                    break outer;
                }
            }
        }

        assertTrue(qr != -1);

        b.revealQuestion(qr, qc);

        assertTrue(b.isQuestionRevealed(qr, qc));
        assertTrue(b.isRevealed(qr, qc));
    }

    @Test
    void testQuestionUsedFlow() {
        board b = new board(5, 5, 0, 0, 1);

        int qr = -1, qc = -1;
        outer:
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.isQuestionCell(r, c)) {
                    qr = r; qc = c;
                    break outer;
                }
            }
        }

        assertFalse(b.isQuestionUsed(qr, qc));
        b.markQuestionUsed(qr, qc);
        assertTrue(b.isQuestionUsed(qr, qc));
    }
}