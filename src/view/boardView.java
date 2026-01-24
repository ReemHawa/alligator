package view;

import controller.gameController;
import model.board;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class boardView extends JPanel implements model.BoardObserver {

    private static final long serialVersionUID = 1L;

    private static final String FLAG = "/images/flag.png";
    private static final String STONES_FOLDER = "/stones/";
    private static final String MINE = "/images/mine.png";
    private static final String SURPRISE = "/images/surprise.png";
    private static final String OPENED_SURPRISE = "/images/openedsurprise.png";
    private static final String QUESTION = "/images/question.png";
    private static final String QUESTION_USED = "/images/usedQ.png";

    private static final int GRID_GAP = 2;

    private static final int OUTER_BORDER_W = 4;
    private static final int INNER_PAD = 6;

    private final int rows;
    private final int cols;
    private final int myBoardIndex;

    private int tileSize;
    private boolean paused = false;
    private boolean flagMode = false;

    private final Font numberFont = new Font("Verdana", Font.BOLD, 20);

    private JButton[][] buttons;
    private ImageIcon[][] stoneForCell;

    private final ArrayList<ImageIcon> stoneIcons = new ArrayList<>();

    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private ImageIcon surpriseIcon;
    private ImageIcon openedSurpriseIcon;
    private ImageIcon questionIcon;
    private ImageIcon questionUsedIcon;

    private Cursor flagCursor;

    private JLabel nameLabel;
    private JLabel flagIconLabel;
    private JLabel flagsRemainingLabel;

    private boolean warnedOneFlagLeft = false;
    private final String originalTitle;

    private final JPanel boardWrapper = new JPanel(new GridBagLayout());
    private final JPanel boardHolder = new JPanel(new GridBagLayout());
    private JPanel boardPanel;

    private int topBarPreferredH = 0;

    private static final class FixedCellButton extends JButton {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Dimension fixed;
        FixedCellButton(Dimension fixed) {
            super();
            this.fixed = fixed;
            setMargin(new Insets(0, 0, 0, 0));
        }
        @Override public Dimension getPreferredSize() { return fixed; }
        @Override public Dimension getMinimumSize()   { return fixed; }
        @Override public Dimension getMaximumSize()   { return fixed; }
    }

    public boardView(int boardIndex, String title, gameController controller, int initialTileSize) {

        board model = controller.getModel().getBoard(boardIndex);

        this.rows = model.getRows();
        this.cols = model.getCols();
        this.tileSize = Math.max(28, initialTileSize);
        this.myBoardIndex = boardIndex;
        this.originalTitle = (title == null) ? "" : title;

        buttons = new JButton[rows][cols];
        stoneForCell = new ImageIcon[rows][cols];

        setLayout(new BorderLayout());
        setOpaque(false);

        rebuildIcons();

        flagIcon = loadIcon(FLAG, 32, 32);
        Toolkit tk = Toolkit.getDefaultToolkit();
        flagCursor = tk.createCustomCursor(flagIcon.getImage(), new Point(1, 1), "flagCursor");

        JPanel topBar = buildTopBar(controller, boardIndex);
        add(topBar, BorderLayout.NORTH);

        SwingUtilities.invokeLater(() -> {
            topBarPreferredH = topBar.getPreferredSize().height;
            updateNameEllipsis();
        });

        loadStoneIcons();
        Random rand = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                stoneForCell[r][c] = stoneIcons.get(rand.nextInt(stoneIcons.size()));
            }
        }

        boardPanel = new JPanel(new GridLayout(rows, cols, GRID_GAP, GRID_GAP));
        boardPanel.setOpaque(false);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                final int rr = r;
                final int cc = c;

                Dimension d = new Dimension(tileSize, tileSize);
                JButton btn = new FixedCellButton(d);
                styleCellButton(btn);
                btn.setIcon(stoneForCell[r][c]);

                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {

                        if (paused) return;

                        // clicking other player's board -> show not your turn
                        if (controller.getModel().getCurrentPlayer() != myBoardIndex) {
                            controller.notifyNotYourTurn();
                            return;
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(boardIndex, rr, cc);
                            return;
                        }

                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (flagMode) controller.handleFlagClick(boardIndex, rr, cc);
                            else controller.handleCellClick(boardIndex, rr, cc);
                        }
                    }
                });


                buttons[r][c] = btn;
                boardPanel.add(btn);
            }
        }

        boardWrapper.setOpaque(false);
        boardWrapper.add(boardPanel, new GridBagConstraints());

        boardHolder.setOpaque(false);
        boardHolder.add(boardWrapper, new GridBagConstraints());

        add(boardHolder, BorderLayout.CENTER);

        setActive(false);
        applyFixedSizesForCurrentTile();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateNameEllipsis();
            }
        });
    }

    private JPanel buildTopBar(gameController controller, int boardIndex) {

        nameLabel = new JLabel("");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        flagIconLabel = new JLabel(loadIcon(FLAG, 18, 18));
        flagIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        flagIconLabel.setToolTipText("Toggle flag mode");

        flagsRemainingLabel = new JLabel("— left");
        flagsRemainingLabel.setForeground(new Color(210, 210, 210));
        flagsRemainingLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        flagsRemainingLabel.setOpaque(false);

        JPanel flagBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        flagBadge.setOpaque(false);
        flagBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        flagBadge.add(flagIconLabel);
        flagBadge.add(flagsRemainingLabel);

        JPanel topBar = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topBar.add(nameLabel, BorderLayout.WEST);
        topBar.add(flagBadge, BorderLayout.EAST);

        flagIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (paused) return;
                controller.toggleFlagMode(boardIndex);
            }
        });

        return topBar;
    }

    private void styleCellButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setFont(numberFont);
    }

    private void rebuildIcons() {
        mineIcon = loadIcon(MINE, tileSize, tileSize);
        surpriseIcon = loadIcon(SURPRISE, tileSize - 6, tileSize - 6);
        openedSurpriseIcon = loadIcon(OPENED_SURPRISE, tileSize - 6, tileSize - 6);
        questionIcon = loadIcon(QUESTION, tileSize - 6, tileSize - 6);
        questionUsedIcon = loadIcon(QUESTION_USED, tileSize - 6, tileSize - 6);
    }

    private void applyFixedSizesForCurrentTile() {

        int gridW = cols * tileSize + (cols - 1) * GRID_GAP ;
        int gridH = rows * tileSize + (rows - 1) * GRID_GAP;

        Dimension gridDim = new Dimension(gridW, gridH);
        boardPanel.setPreferredSize(gridDim);
        boardPanel.setMinimumSize(gridDim);
        boardPanel.setMaximumSize(gridDim);

        int inset = 2 * (OUTER_BORDER_W + INNER_PAD);
        Dimension wrapDim = new Dimension(gridW + inset, gridH + inset);
        boardWrapper.setPreferredSize(wrapDim);
        boardWrapper.setMinimumSize(wrapDim);
        boardWrapper.setMaximumSize(wrapDim);

        revalidate();
        repaint();
    }

    public Dimension getBoardOuterSize() {
        int gridW = cols * tileSize + (cols - 1) * GRID_GAP;
        int gridH = rows * tileSize + (rows - 1) * GRID_GAP;
        int inset = 2 * (OUTER_BORDER_W + INNER_PAD);
        return new Dimension(gridW + inset, gridH + inset);
    }

    public int getTopBarPreferredHeight() {
        return Math.max(0, topBarPreferredH);
    }

    public void setRemainingFlags(int remaining) {
        flagsRemainingLabel.setText(remaining + " left");
        flagsRemainingLabel.revalidate();
        flagsRemainingLabel.repaint();

        if (remaining == 1 && !warnedOneFlagLeft) {
            warnedOneFlagLeft = true;
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    originalTitle + ", you have only 1 flag remaining!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }
        if (remaining > 1) warnedOneFlagLeft = false;
    }

    public void setActive(boolean active) {
        Color borderColor = active ? new Color(255, 165, 0) : new Color(150, 150, 150);

        boardWrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, OUTER_BORDER_W, true),
                BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD)
        ));

        boardWrapper.revalidate();
        boardWrapper.repaint();
    }

    public void setFlagMode(boolean enabled) {
        this.flagMode = enabled;

        Cursor c = enabled ? flagCursor : Cursor.getDefaultCursor();
        setCursor(c);

        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                buttons[r][col].setCursor(c);
            }
        }
    }

    public void setFlagAtCell(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(flagIcon);
        btn.setText(null);
    }

    @Override
    public void cellOpened(int boardIndex, int row, int col, int count) {
        if (boardIndex != myBoardIndex) return;
        revealSafeCell(row, col, count);
    }

    public void revealSafeCell(int row, int col, int count) {
        JButton btn = buttons[row][col];

        btn.setIcon(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);

        if (count == 0) {
            btn.setText("");
            return;
        }

        btn.setText(String.valueOf(count));
        btn.setForeground(getNumberColor(count));
    }

    public void revealSurprise(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(surpriseIcon);
        btn.setText(null);
    }

    public void activateSurprise(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(openedSurpriseIcon);
        btn.setText(null);
    }

    public void revealMineHit(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(mineIcon);
        btn.setText(null);
    }

    public void revealAllMines(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (model.isMine(r, c)) {
                    JButton btn = buttons[r][c];
                    btn.setIcon(mineIcon);
                    btn.setText(null);
                }
            }
        }
    }

    public void removeFlag(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(stoneForCell[row][col]);
        btn.setText(null);
    }

    public void revealQuestion(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(questionIcon);
        btn.setText(null);
    }

    public void markQuestionUsed(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(questionUsedIcon);
        btn.setText(null);
    }

    public void revealMineAuto(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(mineIcon);
        btn.setText(null);
    }

    public void revealHintCell(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(null);
        btn.setText("");
    }

    public void revealAllSurprises(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (model.isSurprise(r, c)) {
                    buttons[r][c].setIcon(model.isSurpriseActivated(r, c) ? openedSurpriseIcon : surpriseIcon);
                    buttons[r][c].setText(null);
                }
            }
        }
    }

    private void loadStoneIcons() {
        stoneIcons.clear();
        for (int i = 1; i <= 20; i++) {
            String path = STONES_FOLDER + "stone" + i + ".png";
            java.net.URL url = getClass().getResource(path);
            if (url != null) stoneIcons.add(loadIcon(path, tileSize, tileSize));
        }
        if (stoneIcons.isEmpty()) stoneIcons.add(emptyIcon(tileSize, tileSize));
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return emptyIcon(w, h);

            BufferedImage img = ImageIO.read(url);

            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();

            return new ImageIcon(resized);
        } catch (Exception e) {
            return emptyIcon(w, h);
        }
    }

    private ImageIcon emptyIcon(int w, int h) {
        return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
    }

    private Color getNumberColor(int n) {
        switch (n) {
            case 1: return new Color(52, 152, 219);
            case 2: return new Color(46, 204, 113);
            case 3: return new Color(231, 76, 60);
            case 4: return new Color(155, 89, 182);
            case 5: return new Color(241, 196, 15);
            case 6: return new Color(26, 188, 156);
            case 7: return Color.WHITE;
            case 8: return Color.GRAY;
            default: return Color.WHITE;
        }
    }

    public void setPaused(boolean paused) { this.paused = paused; }

    private void updateNameEllipsis() {
        int gridW = cols * tileSize + (cols - 1) * GRID_GAP;
        int maxNameWidth = Math.max(80, gridW - 140);
        nameLabel.setText(ellipsize(originalTitle, maxNameWidth, nameLabel.getFont()));
    }

    private String ellipsize(String text, int maxWidthPx, Font font) {
        if (text == null) return "";
        FontMetrics fm = getFontMetrics(font);
        if (fm.stringWidth(text) <= maxWidthPx) return text;

        String dots = "...";
        int dotsW = fm.stringWidth(dots);

        int n = text.length();
        while (n > 0) {
            String candidate = text.substring(0, n).trim();
            if (fm.stringWidth(candidate) + dotsW <= maxWidthPx) return candidate + dots;
            n--;
        }
        return dots;
    }

    public void setTileSize(int newTileSize) {
        newTileSize = Math.max(28, newTileSize);
        if (newTileSize == this.tileSize) return;

        this.tileSize = newTileSize;

        rebuildIcons();
        loadStoneIcons();

        Random rand = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                stoneForCell[r][c] = stoneIcons.get(rand.nextInt(stoneIcons.size()));

                Dimension d = new Dimension(tileSize, tileSize);
                JButton old = buttons[r][c];

                if (old instanceof FixedCellButton) {
                    FixedCellButton fb = (FixedCellButton) old;
                    fb.setIcon(stoneForCell[r][c]);
                } else {
                    old.setIcon(stoneForCell[r][c]);
                }
            }
        }

        applyFixedSizesForCurrentTile();
        updateNameEllipsis();
    }
}
