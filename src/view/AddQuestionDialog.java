package view;

import model.Question;

import javax.swing.*;
import java.awt.*;

public class AddQuestionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextField idField;
    private JTextField questionField;

    private JTextField aField;
    private JTextField bField;
    private JTextField cField;
    private JTextField dField;

    private JComboBox<String> difficultyBox;   // Easy/Medium/Hard/Expert
    private JComboBox<String> correctBox;      // A/B/C/D

    private final QuestionTableModel model;

    public AddQuestionDialog(Frame owner, QuestionTableModel model) {
        super(owner, "Add Question", true);
        this.model = model;

        setSize(560, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        idField = new JTextField(String.valueOf(model.getNextId()));
        idField.setEditable(false);

        questionField = new JTextField();

        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Expert"});
        correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        aField = new JTextField();
        bField = new JTextField();
        cField = new JTextField();
        dField = new JTextField();

        form.add(new JLabel("ID:"));
        form.add(idField);

        form.add(new JLabel("Question:"));
        form.add(questionField);

        form.add(new JLabel("Difficulty:"));
        form.add(difficultyBox);

        form.add(new JLabel("Option A:"));
        form.add(aField);

        form.add(new JLabel("Option B:"));
        form.add(bField);

        form.add(new JLabel("Option C:"));
        form.add(cField);

        form.add(new JLabel("Option D:"));
        form.add(dField);

        form.add(new JLabel("Correct Answer (A/B/C/D):"));
        form.add(correctBox);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        buttons.add(save);
        buttons.add(cancel);

        add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> onSave());
    }

    private void onSave() {

        if (questionField.getText().trim().isEmpty()
                || aField.getText().trim().isEmpty()
                || bField.getText().trim().isEmpty()
                || cField.getText().trim().isEmpty()
                || dField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill Question and all options (A, B, C, D).",
                    "Missing Data",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int id = Integer.parseInt(idField.getText().trim());
        String question = questionField.getText().trim();
        String difficulty = String.valueOf(difficultyBox.getSelectedItem());

        String a = aField.getText().trim();
        String b = bField.getText().trim();
        String c = cField.getText().trim();
        String d = dField.getText().trim();

        String correctLetter = String.valueOf(correctBox.getSelectedItem());

        Question q = new Question(id, question, difficulty, a, b, c, d, correctLetter);
        model.addQuestion(q);

        JOptionPane.showMessageDialog(this, "Question added successfully!");
        dispose();
    }
}
