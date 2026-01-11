package model.factory;

public class BoardConfig {
    public final int rows;
    public final int cols;
    public final int mines;
    public final int surprises;
    public final int questions;
    public final int startingLives;

    public BoardConfig(int rows, int cols, int mines, int surprises, int questions, int startingLives) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.surprises = surprises;
        this.questions = questions;
        this.startingLives = startingLives;
    }
}
