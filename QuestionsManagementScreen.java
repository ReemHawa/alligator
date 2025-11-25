package View;

import Model.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuestionsManagementScreen extends JFrame {

    private JTable questionsTable;
    private JButton backButton;
    private JButton addQuestionButton;
    private QuestionTableModel tableModel;

    public QuestionsManagementScreen(List<Question> questions) {
        setTitle("Questions Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // פנל הרקע
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        backButton = new JButton("← Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));

        addQuestionButton = new JButton("+ Add Question");
        addQuestionButton.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel titleLabel = new JLabel("Questions Management", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(addQuestionButton, BorderLayout.EAST);

        bg.add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new QuestionTableModel(questions);
        questionsTable = new JTable(tableModel);
        questionsTable.setRowHeight(28);

        // ליישר X, ✎ ומספר למרכז
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        questionsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Delete
        questionsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // edit
        questionsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Number

      
        JScrollPane scrollPane = new JScrollPane(questionsTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        bg.add(centerPanel, BorderLayout.CENTER);

        // ===== כפתור "Add Question" =====
        addQuestionButton.addActionListener(e -> {
            AddQuestionDialog dialog = new AddQuestionDialog(
                    QuestionsManagementScreen.this, tableModel);
            dialog.setVisible(true);
        });
    
 // ✅ טיפול בלחיצה על X (מחיקה) ועל ✎ (עריכה)
    questionsTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = questionsTable.rowAtPoint(e.getPoint());
            int col = questionsTable.columnAtPoint(e.getPoint());
            if (row < 0 || col < 0) return;

            if (col == 0) { // Delete column
                Question q = tableModel.getQuestionAt(row);
                int confirm = JOptionPane.showConfirmDialog(
                        QuestionsManagementScreen.this,
                        "Delete question #" + q.getQuestionID() + "?",
                        "Confirm delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeQuestionAt(row);
                }

            } else if (col == 1) { // Edit column
                // מסמנים את השורה כעריכה
                tableModel.setEditableRow(row);

                JOptionPane.showMessageDialog(
                        QuestionsManagementScreen.this,
                        "Now you can edit all fields of question #" +
                                tableModel.getQuestionAt(row).getQuestionID() +
                                ".\nClick any cell in this row (Number, Question, answers, levels) and change the value."
                );
            }
        }
    });
}

    // ===== Getters =====
    public JTable getQuestionsTable() {
        return questionsTable;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public QuestionTableModel getTableModel() {
        return tableModel;
    }

    // ===== פנל הרקע =====
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            // הקובץ homeBackground.png נמצא בחבילה View
            backgroundImage = new ImageIcon(
                    BackgroundPanel.class.getResource("homeBackground.png")
            ).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0,
                    getWidth(), getHeight(), this);
        }
    }

    
}
