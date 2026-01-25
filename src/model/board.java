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
///// new changes
    // ===== SURPRISE =====
    private boolean[][] surprise;
    private boolean[][] goodSurprise;
    private boolean[][] surpriseActivated;
    private boolean[][] surpriseRevealed;
    
 // ===== QUESTION =====
    private boolean[][] question;
    private boolean[][] questionRevealed;
    private boolean[][] questionUsed;
    
    // =====  OBSERVER PATTERN =====
    private java.util.List<BoardObserver> observers = new java.util.ArrayList<>();

    public void addObserver(BoardObserver o) {
        observers.add(o);
    }
    
    private void notifyCellOpened(int boardIndex, int r, int c, int count) {
        for (BoardObserver o : observers) {
            o.cellOpened(boardIndex, r, c, count);
        }
    }
    
    public void openSafeCell(int boardIndex, int r, int c) {
        if (revealed[r][c]) return;

        revealed[r][c] = true;
        int count = getSurroundingMines(r, c);

        notifyCellOpened(boardIndex, r, c, count);
    }
//====================================================






    /* ======================================================
        OLD CONSTRUCTOR (REQUIRED BY game.java)
       ====================================================== */
    public board(int rows, int cols, int minesNum) {
        this(rows, cols, minesNum, 0);   // delegate
    }

    /* ======================================================
        NEW CONSTRUCTOR (WITH SURPRISES)
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
        surpriseRevealed = new boolean[rows][cols];

        
        question = new boolean[rows][cols];
        questionRevealed = new boolean[rows][cols];
        questionUsed = new boolean[rows][cols];

        placeMines();
        computeSurroundingMines();
        assignBaseTypes();
        placeSurprises(surpriseCount);
    }

    /* ======================================================
     NEW CONSTRUCTOR (WITH SURPRISES + QUESTIONS)
    ====================================================== */
 public board(int rows, int cols, int minesNum, int surpriseCount, int questionCount) {

     this(rows, cols, minesNum, surpriseCount);

     question = new boolean[rows][cols];
     questionRevealed = new boolean[rows][cols];
     questionUsed = new boolean[rows][cols];

     placeQuestions(questionCount);
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

            if (type[r][c] == cellType.empty && !surprise[r][c]) {

                surprise[r][c] = true;
                type[r][c] = cellType.surprise; 
                placed++;
            }
        }
    }

    public boolean isSurprise(int r, int c) {
        return type[r][c] == cellType.surprise;
    }

    
    public boolean isSurpriseRevealed(int r, int c) {
        return surpriseRevealed[r][c];
    }

    public void revealSurprise(int r, int c) {
        surpriseRevealed[r][c] = true;
        revealed[r][c] = true;
    }


    public boolean isGoodSurprise(int r, int c) {
        return goodSurprise[r][c];
    }

    public boolean isSurpriseActivated(int r, int c) {
        return surpriseActivated[r][c];
    }

    public void activateSurprise(int r, int c) {
        surpriseActivated[r][c] = true;
        surpriseRevealed[r][c] = true;
        revealed[r][c] = true;
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
                if (mines[r][c]) {
                    type[r][c] = cellType.mine;
                } else if (surroundingMines[r][c] > 0) {
                    type[r][c] = cellType.number;
                } else {
                    type[r][c] = cellType.empty;
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
    
    public void removeFlag(int r, int c) {
        flagged[r][c] = false;
    }
    
    /* ======================================================
    QUESTIONS
    ====================================================== */
 private void placeQuestions(int count) {

     Random rnd = new Random();
     int placed = 0;
     int attempts = 0;
     int maxAttempts = rows * cols * 50;

     while (placed < count && attempts < maxAttempts) {
         attempts++;

         int r = rnd.nextInt(rows);
         int c = rnd.nextInt(cols);

         if (!mines[r][c]
                 && type[r][c] == cellType.empty
                 && surroundingMines[r][c] == 0
                 && !surprise[r][c]) {

             type[r][c] = cellType.question;

             question[r][c] = true;
             questionRevealed[r][c] = false;
             questionUsed[r][c] = false;

             placed++;
         }
     }
 }
 
 public boolean isQuestion(int r, int c) {
	    return type[r][c] == cellType.question;
	}

	public boolean isQuestionCell(int r, int c) {
	    return type[r][c] == cellType.question;
	}

	public boolean isQuestionRevealed(int r, int c) {
	    return questionRevealed[r][c];
	}

	public void revealQuestion(int r, int c) {
	    questionRevealed[r][c] = true;
	    revealed[r][c] = true;
	}

	public boolean isQuestionUsed(int r, int c) {
	    return questionUsed[r][c];
	}

	public void markQuestionUsed(int r, int c) {
	    questionUsed[r][c] = true;
	}

	//Q BOUNOS:
	// BONUS reveal: mark as revealed without any game logic (no score/life changes)
	public void revealBonusCell(int r, int c) {
	    if (r < 0 || r >= rows || c < 0 || c >= cols) return;
	    if (revealed[r][c]) return;

	    // optional: if it was flagged, remove flag so it won't look inconsistent
	    flagged[r][c] = false;

	    revealed[r][c] = true;

	    // if it's a question/surprise - keep their specific revealed flags consistent
	    if (isQuestionCell(r, c)) {
	        questionRevealed[r][c] = true;
	    }
	    if (isSurprise(r, c)) {
	        surpriseRevealed[r][c] = true;
	    }
	}



    /* ======================================================
       STATE
       ====================================================== */
    public boolean isMine(int r, int c) { return mines[r][c]; }
    public boolean isRevealed(int r, int c) { return revealed[r][c]; }
    public void setRevealed(int r, int c) { revealed[r][c] = true; }
    public int getSurroundingMines(int r, int c) { return surroundingMines[r][c]; }
    public cellType getType(int r, int c) { return type[r][c]; }
    
    //HOW MANY SQ
 // returns int[]{revealedCells, revealedMines} in a 3x3 starting at (sr, sc)
    public int[] get3x3RevealStats(int sr, int sc) {
        int revealedCells = 0;
        int revealedMines = 0;

        for (int r = sr; r < sr + 3; r++) {
            for (int c = sc; c < sc + 3; c++) {
                if (revealed[r][c]) {
                    revealedCells++;
                    if (mines[r][c]) revealedMines++;
                }
            }
        }
        return new int[]{revealedCells, revealedMines};
    }


    
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
    
    public boolean allSafeCellsRevealed() {
        int safeTotal = (rows * cols) - minesNum;

        int safeRevealed = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!mines[r][c] && revealed[r][c]) {
                    safeRevealed++;
                }
            }
        }
        return safeRevealed == safeTotal;
    }

    /* ======================================================
       GETTERS
       ====================================================== */
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}