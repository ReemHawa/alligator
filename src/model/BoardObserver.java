package model;

public interface BoardObserver {
    void cellOpened(int boardIndex, int row, int col, int count);
}
