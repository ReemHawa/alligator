package View;

import Model.Question;

import javax.swing.*;
import java.awt.*;

public class AddQuestionDialog extends JDialog {

    private JTextField idField;
    private JTextField questionField;
    private JTextField correctField;
    private JTextField wrong1Field;
    private JTextField wrong2Field;
    private JTextField wrong3Field;
    private JTextField difficultyField;
    private JTextField gameLevelField;

    private JButton saveButton;
    private JButton cancelButton;

    private QuestionTableModel tableModel;

    public AddQuestionDialog(Frame owner, QuestionTableModel tableModel) {
        super(owner, "Add New Question", true);
        this.tableModel = tableModel;

        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        idField = new JTextField();
        questionField = new JTextField();
        correctField = new JTextField();
        wrong1Field = new JTextField();
        wrong2Field = new JTextField();
        wrong3Field = new JTextField();
        difficultyField = new JTextField(); // למשל: Easy / Medium / Hard / Expert
        gameLevelField = new JTextField();  // למשל: Easy / Medium / Hard

        formPanel.add(new JLabel("Question Number (ID):"));
        formPanel.add(idField);

        formPanel.add(new JLabel("Question Text:"));
        formPanel.add(questionField);

        formPanel.add(new JLabel("Correct Answer:"));
        formPanel.add(correctField);

        formPanel.add(new JLabel("Wrong Answer 1:"));
        formPanel.add(wrong1Field);

        formPanel.add(new JLabel("Wrong Answer 2:"));
        formPanel.add(wrong2Field);

        formPanel.add(new JLabel("Wrong Answer 3:"));
        formPanel.add(wrong3Field);

        formPanel.add(new JLabel("Question's Level:"));
        formPanel.add(difficultyField);

        formPanel.add(new JLabel("Game's Level:"));
        formPanel.add(gameLevelField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        // ===== לוגיקת כפתורים =====
        cancelButton.addActionListener(e -> dispose());

        saveButton.addActionListener(e -> onSave());
    }

    private void onSave() {
        // בדיקות בסיסיות
        String idText = idField.getText().trim();
        String questionText = questionField.getText().trim();
        String correct = correctField.getText().trim();
        String w1 = wrong1Field.getText().trim();
        String w2 = wrong2Field.getText().trim();
        String w3 = wrong3Field.getText().trim();
        String difficulty = difficultyField.getText().trim();
        String gameLevel = gameLevelField.getText().trim();

        if (idText.isEmpty() || questionText.isEmpty() || correct.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ID, Question Text and Correct Answer are required.",
                    "Missing data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "ID must be a number.",
                    "Invalid ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // יצירת אובייקט שאלה חדש
        Question q = new Question(
                id,
                questionText,
                correct,
                w1,
                w2,
                w3,
                difficulty,
                gameLevel
        );

        // הוספה למודל הטבלה
        tableModel.addQuestion(q);

        dispose();
    }
}
