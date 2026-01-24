package view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.Question;

public class QuestionTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnNames = {
            "Delete", "Edit",
            "ID", "Question", "Difficulty",
            "A", "B", "C", "D",
            "Correct"
    };

    private final List<Question> questions;
    private int editableRow = -1; // model-row index, -1 = none

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
            case 0: return "✖"; // Delete
            case 1: return "✎"; // Edit
            case 2: return q.getQuestionID();
            case 3: return q.getQuestionText();
            case 4: return q.getDifficultyLevel();
            case 5: return q.getOptionA();
            case 6: return q.getOptionB();
            case 7: return q.getOptionC();
            case 8: return q.getOptionD();
            case 9: return q.getCorrectLetter();
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Integer.class; // ID
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Delete/Edit are clickable
        if (columnIndex == 0 || columnIndex == 1) return true;

        // Never edit ID
        if (columnIndex == 2) return false;

        // Only in edit mode for that row
        return rowIndex == editableRow;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null || rowIndex < 0 || rowIndex >= questions.size()) return;
        if (columnIndex == 2) return; // never edit ID

        Question q = questions.get(rowIndex);
        String value = normalize(aValue.toString());

        switch (columnIndex) {
            case 3: q.setQuestionText(value); break;
            case 4: q.setDifficultyLevel(value); break;
            case 5: q.setOptionA(value); break;
            case 6: q.setOptionB(value); break;
            case 7: q.setOptionC(value); break;
            case 8: q.setOptionD(value); break;
            case 9: q.setCorrectLetter(value); break;
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    // =========================
    // ✅ Edit Mode Helpers
    // =========================
    public void startEditRow(int row) {
        int old = this.editableRow;
        this.editableRow = row;

        if (old >= 0 && old < questions.size()) fireTableRowsUpdated(old, old);
        if (row >= 0 && row < questions.size()) fireTableRowsUpdated(row, row);
    }

    public void finishEdit() {
        int old = this.editableRow;
        this.editableRow = -1;
        if (old >= 0 && old < questions.size()) fireTableRowsUpdated(old, old);
    }

    public boolean isRowInEditMode(int row) {
        return row == editableRow;
    }

    public int getEditableRow() {
        return editableRow;
    }

    // ===== Utilities =====
    public Question getQuestionAt(int row) {
        return questions.get(row);
    }

    public void removeQuestionAt(int row) {
        if (row < 0 || row >= questions.size()) return;

        questions.remove(row);
        fireTableRowsDeleted(row, row);

        if (editableRow == row) editableRow = -1;
        else if (editableRow > row) editableRow--;
    }

    public void addQuestion(Question q) {
        int newIndex = questions.size();
        questions.add(q);
        fireTableRowsInserted(newIndex, newIndex);
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public int getNextId() {
        int max = 0;
        for (Question q : questions) {
            if (q != null && q.getQuestionID() > max) {
                max = q.getQuestionID();
            }
        }
        return max + 1;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replace(",", " ").trim();
    }
}
