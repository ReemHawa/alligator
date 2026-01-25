package view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import model.Question;

public class QuestionsManagementScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTable questionsTable;
    private JButton backButton;
    private JButton addQuestionButton;
    private QuestionTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> difficultyFilter;
    private JButton resetFiltersButton;
    private JLabel counterLabel;

    private TableRowSorter<QuestionTableModel> sorter;

    public QuestionsManagementScreen(List<Question> questions) {
        setTitle("Questions Management");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // ✅ allow maximize + resizing
        setResizable(true);
        setMinimumSize(new Dimension(1100, 650));
        setSize(1400, 800);
        setLocationRelativeTo(null);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // --- TOP BAR ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        backButton = new JButton("← Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        styleButton(backButton);

        addQuestionButton = new JButton("+ Add Question");
        addQuestionButton.setFont(new Font("Arial", Font.BOLD, 14));
        addQuestionButton.setFocusPainted(false);
        styleButton(addQuestionButton);

        JLabel titleLabel = new JLabel("Questions Management", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(addQuestionButton, BorderLayout.EAST);

        // --- TOOLBAR (Search + Difficulty + Counter) ---
        JPanel toolsPanel = new JPanel(new BorderLayout());
        toolsPanel.setOpaque(false);
        toolsPanel.setBorder(new EmptyBorder(8, 40, 10, 40));

        JPanel leftTools = new JPanel();
        leftTools.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(Color.WHITE);
        searchLbl.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setToolTipText("Search across question & answers...");
        styleTextBox(searchField);

        leftTools.add(searchLbl);
        leftTools.add(searchField);
        leftTools.add(Box.createHorizontalStrut(18));

        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setForeground(Color.WHITE);
        diffLbl.setFont(new Font("Arial", Font.BOLD, 14));

        difficultyFilter = new JComboBox<>(new String[] { "All", "Easy", "Medium", "Hard", "Expert" });
        difficultyFilter.setFont(new Font("Arial", Font.PLAIN, 14));
        styleComboBox(difficultyFilter);

        leftTools.add(diffLbl);
        leftTools.add(difficultyFilter);

        resetFiltersButton = new JButton("Reset");
        resetFiltersButton.setFont(new Font("Arial", Font.BOLD, 13));
        resetFiltersButton.setFocusPainted(false);
        styleButton(resetFiltersButton);

        leftTools.add(Box.createHorizontalStrut(12));
        leftTools.add(resetFiltersButton);

        counterLabel = new JLabel(" ");
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setFont(new Font("Arial", Font.BOLD, 14));
        counterLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        toolsPanel.add(leftTools, BorderLayout.WEST);
        toolsPanel.add(counterLabel, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(topPanel, BorderLayout.NORTH);
        topWrapper.add(toolsPanel, BorderLayout.SOUTH);

        bg.add(topWrapper, BorderLayout.NORTH);

        // --- TABLE ---
        tableModel = new QuestionTableModel(questions);

        questionsTable = new JTable(tableModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);

                if (c instanceof JTextArea) {
                    int prefH = c.getPreferredSize().height;
                    if (getRowHeight(row) < prefH) {
                        setRowHeight(row, prefH);
                    }
                }
                return c;
            }
        };

        questionsTable.setFillsViewportHeight(true);
        questionsTable.setRowHeight(42);

        //  No auto resize -> horizontal scroll if needed
        questionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        sorter = new TableRowSorter<>(tableModel);
        questionsTable.setRowSorter(sorter);

        applyColumnWidths();
        enableWrapForLongText();

        JTableHeader header = questionsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setDefaultRenderer(new HeaderRenderer());

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        questionsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(1).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(2).setCellRenderer(center);
        questionsTable.getColumnModel().getColumn(9).setCellRenderer(center);

        questionsTable.setShowHorizontalLines(true);
        questionsTable.setShowVerticalLines(true);
        questionsTable.setGridColor(new Color(255, 255, 255, 50));

        JScrollPane scrollPane = new JScrollPane(questionsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 40, 40, 40));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        bg.add(centerPanel, BorderLayout.CENTER);

        // Add Question dialog
        addQuestionButton.addActionListener(e -> {
            AddQuestionDialog dialog = new AddQuestionDialog(this, tableModel);
            dialog.setVisible(true);
            applyFilters();
        });

        wireSearchAndFilters();
        updateCounter();

        //  On resize/maximize -> recalc heights
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcAllRowHeights();
            }
        });

        setVisible(true);
    }

    //  ONLY button styles helper (rounded/glass like HomeScreen)
    private void styleButton(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        if (b.getBackground() == null || b.getBackground().equals(new JButton().getBackground())) {
            b.setBackground(new Color(255, 255, 255, 235));
        }
        if (b.getForeground() == null || b.getForeground().equals(new JButton().getForeground())) {
            b.setForeground(Color.BLACK);
        }

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(10, 18, 10, 18));

        final Color normalBg = b.getBackground();
        final Color hoverBg = new Color(
                normalBg.getRed(),
                normalBg.getGreen(),
                normalBg.getBlue(),
                Math.min(255, normalBg.getAlpha() + 18)
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

                int arc = 18;

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

    //  ONLY style for search field (rounded glass)
    private void styleTextBox(JTextField f) {
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        f.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 16;

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

    //  ONLY style for difficulty dropdown (glass + rounded)
    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(new Color(255, 255, 255, 235));
        cb.setOpaque(true);
        cb.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200, 220)));

        cb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                return l;
            }
        });
    }

    // ===== PUBLIC GETTERS =====
    public JTable getQuestionsTable() { return questionsTable; }
    public JButton getBackButton() { return backButton; }
    public QuestionTableModel getTableModel() { return tableModel; }

    // ===== Filters/Search =====
    private void wireSearchAndFilters() {

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        difficultyFilter.addActionListener(e -> applyFilters());

        resetFiltersButton.addActionListener(e -> {
            searchField.setText("");
            difficultyFilter.setSelectedIndex(0);
            applyFilters();
        });
    }

    private void applyFilters() {
        final String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        final String diff = String.valueOf(difficultyFilter.getSelectedItem());

        final int DIFFICULTY_COL = 4; // model column index

        RowFilter<QuestionTableModel, Integer> rf = new RowFilter<QuestionTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends QuestionTableModel, ? extends Integer> entry) {

                if (!text.isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object v = entry.getValue(i);
                        if (v != null && v.toString().toLowerCase().contains(text)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return false;
                }

                if (!"All".equalsIgnoreCase(diff)) {
                    Object v = entry.getValue(DIFFICULTY_COL);
                    if (v == null || !v.toString().equalsIgnoreCase(diff)) return false;
                }

                return true;
            }
        };

        sorter.setRowFilter(rf);
        updateCounter();
        recalcAllRowHeights();
    }

    private void updateCounter() {
        int total = tableModel.getRowCount();
        int shown = questionsTable.getRowCount();
        counterLabel.setText("Showing: " + shown + " / " + total);
    }

    //  ensures full visibility after changes
    private void recalcAllRowHeights() {
        for (int row = 0; row < questionsTable.getRowCount(); row++) {
            int maxH = 42; // minimum
            for (int col : new int[]{3, 5, 6, 7, 8}) { // Question + A/B/C/D (VIEW cols)
                Component c = questionsTable.prepareRenderer(
                        questionsTable.getCellRenderer(row, col), row, col
                );
                maxH = Math.max(maxH, c.getPreferredSize().height);
            }
            questionsTable.setRowHeight(row, maxH);
        }
    }

    // ===== Renderers =====
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 14));
            setOpaque(true);
            setBackground(new Color(0, 0, 0, 150));
        }
    }

    private void applyColumnWidths() {
        // Delete, Edit, ID, Question, Difficulty, A, B, C, D, Correct
        questionsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        questionsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        questionsTable.getColumnModel().getColumn(2).setPreferredWidth(55);

        questionsTable.getColumnModel().getColumn(3).setPreferredWidth(420); // Question
        questionsTable.getColumnModel().getColumn(4).setPreferredWidth(110); // Difficulty

        questionsTable.getColumnModel().getColumn(5).setPreferredWidth(320); // A
        questionsTable.getColumnModel().getColumn(6).setPreferredWidth(320); // B
        questionsTable.getColumnModel().getColumn(7).setPreferredWidth(320); // C
        questionsTable.getColumnModel().getColumn(8).setPreferredWidth(320); // D

        questionsTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // Correct
    }

    private void enableWrapForLongText() {
        int[] wrapCols = {3, 5, 6, 7, 8}; // Question + A/B/C/D
        WrapCellRenderer wrap = new WrapCellRenderer();

        for (int c : wrapCols) {
            questionsTable.getColumnModel().getColumn(c).setCellRenderer(wrap);
        }
    }

    private static class WrapCellRenderer extends JTextArea implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        WrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(new Font("Arial", Font.PLAIN, 13));
            setBorder(new EmptyBorder(6, 8, 6, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            setText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(new Color(180, 200, 230));
                setForeground(Color.BLACK);
            } else {
                setBackground(new Color(255, 255, 255, 220));
                setForeground(Color.BLACK);
            }

            //  set width so preferred height is correct
            int width = table.getColumnModel().getColumn(column).getWidth();
            setSize(new Dimension(width, Short.MAX_VALUE));

            return this;
        }
    }

    // ===== Background Panel  =====
    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Image img;

        public BackgroundPanel() {
            URL url = getClass().getResource("/images/background.jpeg");
            if (url != null) {
                img = new ImageIcon(url).getImage();
            } else {
                img = new ImageIcon("src/images/background.jpeg").getImage(); // IDE fallback
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}
