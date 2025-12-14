package view;

import model.Question;
import javax.swing.*;
import java.awt.*;

public class AddQuestionDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField idField, qField, cField, w1Field, w2Field, w3Field, diffField, gameField;
    private final QuestionTableModel model;

    public AddQuestionDialog(Frame owner, QuestionTableModel model) {
        super(owner, "Add Question", true);
        this.model = model;

        setSize(450, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        idField = new JTextField();
        qField = new JTextField();
        cField = new JTextField();
        w1Field = new JTextField();
        w2Field = new JTextField();
        w3Field = new JTextField();
        diffField = new JTextField();
        gameField = new JTextField();

        form.add(new JLabel("ID:")); form.add(idField);
        form.add(new JLabel("Question:")); form.add(qField);
        form.add(new JLabel("Correct:")); form.add(cField);
        form.add(new JLabel("Wrong1:")); form.add(w1Field);
        form.add(new JLabel("Wrong2:")); form.add(w2Field);
        form.add(new JLabel("Wrong3:")); form.add(w3Field);
        form.add(new JLabel("Difficulty:")); form.add(diffField);
        form.add(new JLabel("Game Level:")); form.add(gameField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        bottom.add(save);
        bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());

                Question q = new Question(
                        id,
                        qField.getText(),
                        cField.getText(),
                        w1Field.getText(),
                        w2Field.getText(),
                        w3Field.getText(),
                        diffField.getText(),
                        gameField.getText()
                );

                model.addQuestion(q);
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });
    }
}
