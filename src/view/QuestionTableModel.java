package view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Question;

public class QuestionTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnNames = {
            "Delete", "Edit", "Number", "Question",
            "Correct Answer", "Wrong 1", "Wrong 2", "Wrong 3",
            "Question Level", "Game Level"
    };

    // The main questions list (THIS IS THE ONE USED EVERYWHERE)
    private List<Question> questions;

    // The row currently in edit mode (kept for backward compatibility)
    private int editableRow = -1;

    public QuestionTableModel(List<Question> questions) {
        this.questions = new ArrayList<>(questions);
    }

    @Override
    public int getRowCount() {
        return questions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Question q = questions.get(rowIndex);

        switch (columnIndex) {
            case 0: return "✖";     // Delete
            case 1: return "✎";     // Edit
            case 2: return q.getQuestionID();         // ID (Number)
            case 3: return q.getQuestionText();
            case 4: return q.getCorrectAnswer();
            case 5: return q.getWrongAnswer1();
            case 6: return q.getWrongAnswer2();
            case 7: return q.getWrongAnswer3();
            case 8: return q.getDifficultyLevel();
            case 9: return q.getGameLevel();
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Integer.class; // ID column
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {

        // Delete/Edit columns are clickable (controller handles click)
        if (columnIndex == 0 || columnIndex == 1) return true;

        // NEVER allow editing the ID column
        if (columnIndex == 2) return false;

        // Other columns: only if this row is in edit mode
        return rowIndex == editableRow;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null || rowIndex < 0 || rowIndex >= questions.size()) return;

        // hard block: never change the ID even if someone tries
        if (columnIndex == 2) return;

        Question q = questions.get(rowIndex);
        String value = normalize(aValue.toString());

        switch (columnIndex) {
            case 3:
                q.setQuestionText(value);
                break;
            case 4:
                q.setCorrectAnswer(value);
                break;
            case 5:
                q.setWrongAnswer1(value);
                break;
            case 6:
                q.setWrongAnswer2(value);
                break;
            case 7:
                q.setWrongAnswer3(value);
                break;
            case 8:
                q.setDifficultyLevel(value);
                break;
            case 9:
                q.setGameLevel(value);
                break;
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    // ============= UTILITIES =============

    public Question getQuestionAt(int row) {
        return questions.get(row);
    }

    public void removeQuestionAt(int row) {
        if (row < 0 || row >= questions.size()) return;
        questions.remove(row);
        fireTableRowsDeleted(row, row);

        // if deleted the editable row - reset edit mode safely
        if (editableRow == row) editableRow = -1;
        else if (editableRow > row) editableRow--;
    }

    public void addQuestion(Question q) {
        int newIndex = questions.size();
        questions.add(q);
        fireTableRowsInserted(newIndex, newIndex);
    }

    public void setEditableRow(int row) {
        this.editableRow = row;
        if (row >= 0 && row < questions.size()) {
            fireTableRowsUpdated(row, row);
        }
    }

    public void clearEditableRow() {
        int old = this.editableRow;
        this.editableRow = -1;
        if (old >= 0 && old < questions.size()) {
            fireTableRowsUpdated(old, old);
        }
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    // ============= NEW HELPERS FOR EDIT/ADD DIALOGS =============

    /** Returns next available ID (max existing + 1). */
    public int getNextId() {
        int max = 0;
        for (Question q : questions) {
            if (q != null && q.getQuestionID() > max) {
                max = q.getQuestionID();
            }
        }
        return max + 1;
    }

    /** Update all editable fields in one call (keeps the same ID). */
    public void updateQuestionAt(
            int row,
            String questionText,
            String correct,
            String wrong1,
            String wrong2,
            String wrong3,
            String questionLevel,
            String gameLevel
    ) {
        if (row < 0 || row >= questions.size()) return;

        Question q = questions.get(row);

        q.setQuestionText(normalize(questionText));
        q.setCorrectAnswer(normalize(correct));
        q.setWrongAnswer1(normalize(wrong1));
        q.setWrongAnswer2(normalize(wrong2));
        q.setWrongAnswer3(normalize(wrong3));
        q.setDifficultyLevel(normalize(questionLevel));
        q.setGameLevel(normalize(gameLevel));

        fireTableRowsUpdated(row, row);
    }

    /** Keep CSV-friendly (avoid commas breaking columns). */
    private String normalize(String s) {
        if (s == null) return "";
        return s.replace(",", " ").trim();
    }
}
