package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import model.Question;

public class QuestionsManagementScreen extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

        // Background panel
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // --- TOP BAR ---
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

        // --- TABLE ---
        tableModel = new QuestionTableModel(questions);
        questionsTable = new JTable(tableModel);
        questionsTable.setRowHeight(28);

        // Center align delete/edit
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        questionsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(1).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(2).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(questionsTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        bg.add(centerPanel, BorderLayout.CENTER);

        // Add question dialog
        addQuestionButton.addActionListener(e -> {
            AddQuestionDialog dialog = new AddQuestionDialog(
                    QuestionsManagementScreen.this,
                    tableModel
            );
            dialog.setVisible(true);
        });
    }

    // ======== REQUIRED METHODS FOR CONTROLLER ========

    public JTable getQuestionsTable() {
        return questionsTable;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public QuestionTableModel getTableModel() {
        return tableModel;
    }

    // ======== Background Panel ========

    private static class BackgroundPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image img;

        public BackgroundPanel() {
            img = new ImageIcon("src/images/background.jpeg").getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
