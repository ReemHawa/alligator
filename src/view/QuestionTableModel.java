package view;

import model.Question;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class QuestionTableModel extends AbstractTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String[] columnNames = {
            "Delete", "Edit", "Number", "Question",
            "Correct Answer", "Wrong Answer1", "Wrong Answer2", "Wrong Answer3",
            "Question Level", "Game Level"
    };

    // The main questions list (THIS IS THE ONE USED EVERYWHERE)
    private List<Question> questions;

    // The row currently in edit mode
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
            case 2: return q.getQuestionID();
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
        // Only allow editing in the selected row, and not delete/edit columns
        return rowIndex == editableRow && columnIndex >= 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null || rowIndex < 0 || rowIndex >= questions.size()) return;

        Question q = questions.get(rowIndex);
        String value = aValue.toString();

        switch (columnIndex) {
            case 2: // Question Number
                try {
                    q.setQuestionID(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {}
                break;
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
    }

    public void addQuestion(Question q) {
        int newIndex = questions.size();
        questions.add(q);
        fireTableRowsInserted(newIndex, newIndex);
    }

    public void setEditableRow(int row) {
        this.editableRow = row;
        fireTableRowsUpdated(row, row);
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }
}
