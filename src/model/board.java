package model;

import java.util.Random;

public class board {

    private final int rows;
    private final int cols;
    private final int minesNum;

    private final boolean[][] mines;
    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private final int[][] surroundingMines;
    private final cellType[][] type;

    // ===== SURPRISE =====
    private boolean[][] surprise;
    private boolean[][] goodSurprise;
    private boolean[][] surpriseActivated;

    /* ======================================================
       ✅ OLD CONSTRUCTOR (REQUIRED BY game.java)
       ====================================================== */
    public board(int rows, int cols, int minesNum) {
        this(rows, cols, minesNum, 0);   // delegate
    }

    /* ======================================================
       ✅ NEW CONSTRUCTOR (WITH SURPRISES)
       ====================================================== */
    public board(int rows, int cols, int minesNum, int surpriseCount) {
        this.rows = rows;
        this.cols = cols;
        this.minesNum = minesNum;

        mines = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        surroundingMines = new int[rows][cols];
        type = new cellType[rows][cols];

        surprise = new boolean[rows][cols];
        goodSurprise = new boolean[rows][cols];
        surpriseActivated = new boolean[rows][cols];

        placeMines();
        placeSurprises(surpriseCount);
        computeSurroundingMines();
        assignBaseTypes();
    }

    /* ======================================================
       MINES
       ====================================================== */
    private void placeMines() {
        Random rnd = new Random();
        int placed = 0;

        while (placed < minesNum) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);

            if (!mines[r][c]) {
                mines[r][c] = true;
                placed++;
            }
        }
    }

    /* ======================================================
       SURPRISES
       ====================================================== */
    private void placeSurprises(int count) {
        Random rnd = new Random();
        int placed = 0;

        while (placed < count) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);

            if (!mines[r][c] && !surprise[r][c]) {
                surprise[r][c] = true;
                goodSurprise[r][c] = placed < count / 2; // 50/50
                placed++;
            }
        }
    }

    public boolean isSurprise(int r, int c) {
        return surprise[r][c];
    }

    public boolean isGoodSurprise(int r, int c) {
        return goodSurprise[r][c];
    }

    public boolean isSurpriseActivated(int r, int c) {
        return surpriseActivated[r][c];
    }

    public void activateSurprise(int r, int c) {
        surpriseActivated[r][c] = true;
    }
    
    public void setGoodSurprise(int r, int c, boolean good) {
        goodSurprise[r][c] = good;
    }


    /* ======================================================
       CELL TYPES
       ====================================================== */
    private void assignBaseTypes() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) type[r][c] = cellType.mine;
                else if (surroundingMines[r][c] == 0) type[r][c] = cellType.empty;
                else type[r][c] = cellType.number;
            }
        }
    }

    private void computeSurroundingMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (mines[r][c]) continue;

                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr;
                        int nc = c + dc;
                        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                        if (mines[nr][nc]) count++;
                    }
                }
                surroundingMines[r][c] = count;
            }
        }
    }

    /* ======================================================
       FLAGS
       ====================================================== */
    public boolean placeFlag(int r, int c) {
        if (revealed[r][c] || flagged[r][c]) return false;
        flagged[r][c] = true;
        return true;
    }

    public boolean isFlagged(int r, int c) {
        return flagged[r][c];
    }

    /* ======================================================
       STATE
       ====================================================== */
    public boolean isMine(int r, int c) { return mines[r][c]; }
    public boolean isRevealed(int r, int c) { return revealed[r][c]; }
    public void setRevealed(int r, int c) { revealed[r][c] = true; }
    public int getSurroundingMines(int r, int c) { return surroundingMines[r][c]; }
    public cellType getType(int r, int c) { return type[r][c]; }

    /* ======================================================
       REQUIRED BY game.java
       ====================================================== */
    public int getMinesNum() {
        return minesNum;
    }

    public int getRevealedOrFlaggedMinesCount() {
        int count = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (mines[r][c] && (revealed[r][c] || flagged[r][c]))
                    count++;
        return count;
    }

    /* ======================================================
       GETTERS
       ====================================================== */
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
