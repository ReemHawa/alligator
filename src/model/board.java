package model;

import java.util.Random;

public class board {
	
        public static final int rows = 10 ;
		public static final int cols = 10 ;
		public static final int minesNum = 10 ;
		private final boolean[][] mines = new boolean[rows][cols];
	    private final boolean[][] revealed = new boolean[rows][cols];
	    private final int[][] surroundingMines = new int[rows][cols];
	    private final cellType[][] type = new cellType[rows][cols];

	    public board() {
	        placeMines();
	        computeSurroundingMines();
	        assignBaseTypes();
	    }
	    
	    private void assignBaseTypes() {
	    	for (int r=0; r<rows;r++) {
	    		for(int c=0;c<cols;c++) {
	    			if(mines[r][c]) {
	    				type[r][c]=cellType.mine;
	    			}else if (surroundingMines[r][c]==0) {
	    				type[r][c]=cellType.empty;
	    			}else {
	    				type[r][c]=cellType.number;
	    			}
	    		}
	    	}
	    }
	    
	    public cellType getType(int r, int c ) {
	    	return type[r][c];
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

	    private void computeSurroundingMines() {
	        for (int i = 0; i < rows; i++) {
	            for (int j = 0; j < cols; j++) {
	                if (mines[i][j]) continue;

	                int count = 0;
	                for (int di = -1; di <= 1; di++) {
	                    for (int dj = -1; dj <= 1; dj++) {
	                        int ni = i + di;
	                        int nj = j + dj;
	                        if (ni < 0 || ni >= rows || nj < 0 || nj >= cols) continue;
	                        if (mines[ni][nj]) count++;
	                    }
	                }
	                surroundingMines[i][j] = count;
	            }
	        }
	    }

	    // ---- getters / state ----
	    public boolean isMine(int r, int c) {
	        return mines[r][c];
	    }

	    public int getSurroundingMines(int r, int c) {
	        return surroundingMines[r][c];
	    }

	    public boolean isRevealed(int r, int c) {
	        return revealed[r][c];
	    }

	    public void setRevealed(int r, int c) {
	        revealed[r][c] = true;
	    }

	    public boolean[][] getMinesMatrix() {
	        return mines;
	    }

	    public int getRows() { return rows; }
	    public int getCols() { return cols; }

}
