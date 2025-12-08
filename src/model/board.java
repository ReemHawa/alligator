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

    public board(int rows, int cols, int minesNum) {
        this.rows = rows;
        this.cols = cols;
        this.minesNum = minesNum;

        mines = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        surroundingMines = new int[rows][cols];
        type = new cellType[rows][cols];

        placeMines();
        computeSurroundingMines();
        assignBaseTypes();
    }

 
    public board() {
        this.rows = 10;
        this.cols = 10;
        this.minesNum = 10;

        mines = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        surroundingMines = new int[rows][cols];
        type = new cellType[rows][cols];

        placeMines();
        computeSurroundingMines();
        assignBaseTypes();
    }

    

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

    private void assignBaseTypes() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c]) {
                    type[r][c] = cellType.mine;
                } else if (surroundingMines[r][c] == 0) {
                    type[r][c] = cellType.empty;
                } else {
                    type[r][c] = cellType.number;
                }
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

    // flag

    public boolean placeFlag(int r, int c) {
        if (revealed[r][c] || flagged[r][c]) return false;
        flagged[r][c] = true;
        return true;
    }

    public boolean isFlagged(int r, int c) {
        return flagged[r][c];
    }

    // state

    public boolean isMine(int r, int c) { 
    	return mines[r][c]; 
    	}
    public boolean isRevealed(int r, int c) { 
    	return revealed[r][c]; 
    	}
    public void setRevealed(int r, int c) { 
    	revealed[r][c] = true; 
    	}
    public int getSurroundingMines(int r, int c) {
    	return surroundingMines[r][c]; 
    	}
    public cellType getType(int r, int c) { 
    	return type[r][c]; 
    	}

    public int getRevealedOrFlaggedMinesCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mines[r][c] && (revealed[r][c] || flagged[r][c])) {
                    count++;
                }
            }
        }
        return count;
    }

    // getters

    public int getRows() { 
    	return rows;
    	}
    public int getCols() {
    	return cols; 
    	}
    public int getMinesNum() { 
    	return minesNum; 
    	}

    public boolean[][] getMinesMatrix() { 
    	return mines; 
    	}
}
