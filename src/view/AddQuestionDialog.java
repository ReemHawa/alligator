package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Question;

public class AddQuestionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextField idField, qField, cField, w1Field, w2Field, w3Field;
    private JComboBox<String> diffBox, gameBox;

    private final QuestionTableModel model;

    public AddQuestionDialog(Frame owner, QuestionTableModel model) {
        super(owner, "Add Question", true);
        this.model = model;

        setSize(520, 430);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ID (auto)
        idField = new JTextField(String.valueOf(model.getNextId()));
        idField.setEditable(false);

        qField = new JTextField();
        cField = new JTextField();
        w1Field = new JTextField();
        w2Field = new JTextField();
        w3Field = new JTextField();

        // dropdowns to prevent typos
        diffBox = new JComboBox<>(new String[] {"Easy", "Medium", "Hard", "Expert"});
        gameBox = new JComboBox<>(new String[] {"Easy", "Medium", "Hard"});

        form.add(new JLabel("ID:"));               form.add(idField);
        form.add(new JLabel("Question:"));         form.add(qField);
        form.add(new JLabel("Correct Answer:"));   form.add(cField);
        form.add(new JLabel("Wrong 1:"));          form.add(w1Field);
        form.add(new JLabel("Wrong 2:"));          form.add(w2Field);
        form.add(new JLabel("Wrong 3:"));          form.add(w3Field);
        form.add(new JLabel("Question Level:"));   form.add(diffBox);
        form.add(new JLabel("Game Level:"));       form.add(gameBox);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        bottom.add(save);
        bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            // validation
            String question = qField.getText().trim();
            String correct  = cField.getText().trim();
            String w1       = w1Field.getText().trim();
            String w2       = w2Field.getText().trim();
            String w3       = w3Field.getText().trim();

            if (question.isEmpty() || correct.isEmpty() || w1.isEmpty() || w2.isEmpty() || w3.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // prevent duplicates between answers
            if (correct.equalsIgnoreCase(w1) || correct.equalsIgnoreCase(w2) || correct.equalsIgnoreCase(w3)
                    || w1.equalsIgnoreCase(w2) || w1.equalsIgnoreCase(w3) || w2.equalsIgnoreCase(w3)) {
                JOptionPane.showMessageDialog(this, "Answers must be different from each other.", "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idField.getText());

            Question q = new Question(
                    id,
                    question,
                    correct,
                    w1,
                    w2,
                    w3,
                    diffBox.getSelectedItem().toString(),
                    gameBox.getSelectedItem().toString()
            );

            model.addQuestion(q);
            dispose();
        });
    }
}
