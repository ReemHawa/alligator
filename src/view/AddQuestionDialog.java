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
        styleTextBox(idField);

        questionField = new JTextField();
        styleTextBox(questionField);

        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Expert"});
        styleComboBox(difficultyBox);

        correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        styleComboBox(correctBox);

        aField = new JTextField();
        styleTextBox(aField);

        bField = new JTextField();
        styleTextBox(bField);

        cField = new JTextField();
        styleTextBox(cField);

        dField = new JTextField();
        styleTextBox(dField);

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

        styleButton(save);
        styleButton(cancel);

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

    // ===== STYLE HELPERS ONLY =====
    private void styleButton(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBackground(new Color(255, 255, 255, 235));
        b.setForeground(Color.BLACK);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 14, 8, 14));

        final Color normalBg = b.getBackground();
        final Color hoverBg = new Color(
                normalBg.getRed(),
                normalBg.getGreen(),
                normalBg.getBlue(),
                Math.min(255, normalBg.getAlpha() + 20)
        );

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hoverBg); b.repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { b.setBackground(normalBg); b.repaint(); }
        });

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton btn = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 16;

                g2.setColor(btn.getBackground());
                g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), arc, arc);

                g2.setColor(new Color(200, 200, 200, 220));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, btn.getWidth() - 3, btn.getHeight() - 3, arc, arc);

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private void styleTextBox(JTextField f) {
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        f.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 14;

                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(0, 0, f.getWidth(), f.getHeight(), arc, arc);

                g2.setColor(new Color(200, 200, 200, 220));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, f.getWidth() - 3, f.getHeight() - 3, arc, arc);

                g2.dispose();
                super.paintSafely(g);
            }
        });
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(new Color(255, 255, 255, 235));
        cb.setOpaque(true);
        cb.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200, 220)));
    }
}
