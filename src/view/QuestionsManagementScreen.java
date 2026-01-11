package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;


import model.Question;

public class QuestionsManagementScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTable questionsTable;
    private JButton backButton;
    private JButton addQuestionButton;
    private QuestionTableModel tableModel;

    // NEW UI
    private JTextField searchField;
    private JComboBox<String> questionLevelFilter;
    private JComboBox<String> gameLevelFilter;
    private JButton resetFiltersButton;
    private JLabel counterLabel;

    private TableRowSorter<QuestionTableModel> sorter;

    public QuestionsManagementScreen(List<Question> questions) {
        setTitle("Questions Management");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        //// Background panel (UNCHANGED)
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

        // --- TOOLBAR (Search + Filters + Counter) ---
        JPanel toolsPanel = new JPanel();
        toolsPanel.setOpaque(false);
        toolsPanel.setBorder(new EmptyBorder(8, 40, 10, 40));
        toolsPanel.setLayout(new BorderLayout());

        JPanel leftTools = new JPanel();
        leftTools.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(Color.WHITE);
        searchLbl.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(22);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setToolTipText("Type to search in the table (question / answers / levels)");

        leftTools.add(searchLbl);
        leftTools.add(searchField);

        leftTools.add(Box.createHorizontalStrut(15));

        JLabel qLevelLbl = new JLabel("Question Level:");
        qLevelLbl.setForeground(Color.WHITE);
        qLevelLbl.setFont(new Font("Arial", Font.BOLD, 14));

        questionLevelFilter = new JComboBox<>(new String[] { "All", "Easy", "Medium", "Hard", "Expert" });
        questionLevelFilter.setFont(new Font("Arial", Font.PLAIN, 14));

        leftTools.add(qLevelLbl);
        leftTools.add(questionLevelFilter);

        leftTools.add(Box.createHorizontalStrut(15));

        JLabel gLevelLbl = new JLabel("Game Level:");
        gLevelLbl.setForeground(Color.WHITE);
        gLevelLbl.setFont(new Font("Arial", Font.BOLD, 14));

        gameLevelFilter = new JComboBox<>(new String[] { "All", "Easy", "Medium", "Hard", "Expert" });
        gameLevelFilter.setFont(new Font("Arial", Font.PLAIN, 14));

        leftTools.add(gLevelLbl);
        leftTools.add(gameLevelFilter);

        resetFiltersButton = new JButton("Reset");
        resetFiltersButton.setFont(new Font("Arial", Font.BOLD, 13));
        leftTools.add(Box.createHorizontalStrut(10));
        leftTools.add(resetFiltersButton);

        counterLabel = new JLabel(" ");
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setFont(new Font("Arial", Font.BOLD, 14));
        counterLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        toolsPanel.add(leftTools, BorderLayout.WEST);
        toolsPanel.add(counterLabel, BorderLayout.EAST);

        // wrap top area
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(topPanel, BorderLayout.NORTH);
        topWrapper.add(toolsPanel, BorderLayout.SOUTH);

        bg.add(topWrapper, BorderLayout.NORTH);

        // --- TABLE ---
        tableModel = new QuestionTableModel(questions);
        questionsTable = new JTable(tableModel);
        questionsTable.setRowHeight(34);
        questionsTable.setAutoCreateRowSorter(true); // enable sorting by header click
        
        //edit color
     // ===== Color rows by Question Difficulty =====
        questionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());

          


        // Sorter (for filters + search)
        sorter = new TableRowSorter<>(tableModel);
        questionsTable.setRowSorter(sorter);

        // Header styling
        JTableHeader header = questionsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setOpaque(false);
        header.setDefaultRenderer(new HeaderRenderer());

        // Center align delete/edit/number
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        questionsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(1).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(2).setCellRenderer(center);

        // Striping + tooltips for all cells
       // questionsTable.setDefaultRenderer(Object.class, new StripedTooltipRenderer());

        // Slight borders for table
        questionsTable.setShowHorizontalLines(true);
        questionsTable.setShowVerticalLines(true);
        questionsTable.setGridColor(new Color(255, 255, 255, 50));
        questionsTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(questionsTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 40, 40, 40));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        bg.add(centerPanel, BorderLayout.CENTER);

        // Add question dialog (UNCHANGED)
        addQuestionButton.addActionListener(e -> {
            AddQuestionDialog dialog = new AddQuestionDialog(
                    QuestionsManagementScreen.this,
                    tableModel
            );
            dialog.setVisible(true);

            // After adding -> update counter & filters
            applyFilters();
        });

        // Filters/Search wiring
        wireSearchAndFilters();

        // Initial counter
        updateCounter();
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

    // ======== Search & Filters ========

    private void wireSearchAndFilters() {

        // live search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        questionLevelFilter.addActionListener(e -> applyFilters());
        gameLevelFilter.addActionListener(e -> applyFilters());

        resetFiltersButton.addActionListener(e -> {
            searchField.setText("");
            questionLevelFilter.setSelectedIndex(0);
            gameLevelFilter.setSelectedIndex(0);
            applyFilters();
        });
    }

    private void applyFilters() {
        final String text = searchField.getText() == null ? "" : searchField.getText().trim();
        final String qLevel = String.valueOf(questionLevelFilter.getSelectedItem());
        final String gLevel = String.valueOf(gameLevelFilter.getSelectedItem());

        // Column indexes based on your table:
        // 8 = Question Level, 9 = Game Level
        final int QUESTION_LEVEL_COL = 8;
        final int GAME_LEVEL_COL = 9;

        RowFilter<QuestionTableModel, Integer> rf = new RowFilter<QuestionTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends QuestionTableModel, ? extends Integer> entry) {

                // Search text across all columns
                if (!text.isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object v = entry.getValue(i);
                        if (v != null && v.toString().toLowerCase().contains(text.toLowerCase())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return false;
                }

                // Question level filter
                if (!"All".equalsIgnoreCase(qLevel)) {
                    Object v = entry.getValue(QUESTION_LEVEL_COL);
                    if (v == null || !v.toString().equalsIgnoreCase(qLevel)) return false;
                }

                // Game level filter
                if (!"All".equalsIgnoreCase(gLevel)) {
                    Object v = entry.getValue(GAME_LEVEL_COL);
                    if (v == null || !v.toString().equalsIgnoreCase(gLevel)) return false;
                }

                return true;
            }
        };

        sorter.setRowFilter(rf);
        updateCounter();
    }

    private void updateCounter() {
        int total = tableModel.getRowCount();
        int shown = questionsTable.getRowCount(); // after filter
        counterLabel.setText("Showing: " + shown + " / " + total);
    }

    // ======== Renderers ========

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 14));
            setOpaque(true);
            setBackground(new Color(0, 0, 0, 140));
        }
    }
    /*
    private static class StripedTooltipRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            // tooltip for long text
            if (value != null) {
                String s = value.toString();
                setToolTipText(s.length() > 30 ? s : null);
            } else {
                setToolTipText(null);
            }

            // keep selection color
            if (isSelected) {
                c.setBackground(new Color(180, 200, 230));
                c.setForeground(Color.BLACK);
            } else {

                // IMPORTANT: row is VIEW row (because of sorter). Convert to MODEL row.
                int modelRow = table.convertRowIndexToModel(row);

                // Difficulty column index = 8 in MODEL
                Object diffObj = table.getModel().getValueAt(modelRow, 8);
                String difficulty = diffObj != null ? diffObj.toString().trim() : "";

                Color baseColor;
                switch (difficulty.toLowerCase()) {
                    case "easy":
                        baseColor = new Color(220, 245, 220); // light green
                        break;
                    case "medium":
                        baseColor = new Color(220, 235, 250); // light blue
                        break;
                    case "hard":
                        baseColor = new Color(255, 235, 205); // light orange
                        break;
                    case "expert":
                        baseColor = new Color(255, 220, 220); // light red
                        break;
                    default:
                        baseColor = (row % 2 == 0)
                                ? new Color(255, 255, 255, 215)
                                : new Color(245, 245, 245, 215);
                }

                // Optional: zebra effect on top of base color (very subtle)
                if (row % 2 == 1) {
                    // slightly darken for odd rows
                    baseColor = new Color(
                            Math.max(0, baseColor.getRed() - 8),
                            Math.max(0, baseColor.getGreen() - 8),
                            Math.max(0, baseColor.getBlue() - 8)
                    );
                }

                c.setBackground(baseColor);
                c.setForeground(Color.BLACK);
            }

            // alignment
            if (col == 0 || col == 1 || col == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            return c;
        }
    }
*/

    // ======== Background Panel (UNCHANGED) ========

    private static class BackgroundPanel extends JPanel {
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
