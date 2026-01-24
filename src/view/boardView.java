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

    private JButton[][] buttons;
    private ImageIcon[][] stoneForCell;

    // wrapper for the grid (the orange/gray border should be tight around this)
    private final JPanel boardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final JPanel boardHolder = new JPanel(new GridBagLayout()); // prevents stretching

    private final ArrayList<ImageIcon> stoneIcons = new ArrayList<>();
    private final Font numberFont = new Font("Verdana", Font.BOLD, 20);

    private static int TILE_SIZE = 40;
    private boolean flagMode = false;

    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private ImageIcon surpriseIcon;
    private ImageIcon openedSurpriseIcon;
    private ImageIcon questionIcon;
    private ImageIcon questionUsedIcon;

    // header elements
    private JLabel nameLabel;
    private JLabel flagIconLabel;          // clickable
    private JLabel flagsRemainingLabel;    // not clickable

    private final int rows;
    private final int cols;
    private final int myBoardIndex;

    private Cursor flagCursor;
    private boolean paused = false;
    
    private boolean warnedOneFlagLeft = false;


    private String originalTitle = "";

    private static final String FLAG = "/images/flag.png";
    private static final String STONES_FOLDER = "/stones/";
    private static final String MINE = "/images/mine.png";
    private static final String SURPRISE = "/images/surprise.png";
    private static final String OPENED_SURPRISE = "/images/openedsurprise.png";
    private static final String QUESTION = "/images/question.png";
    private static final String QUESTION_USED = "/images/usedQ.png";

    public boardView(int boardIndex, String title, gameController controller, int tileSize) {

        board model = controller.getModel().getBoard(boardIndex);

        this.rows = model.getRows();
        this.cols = model.getCols();
        TILE_SIZE = tileSize;

        this.myBoardIndex = boardIndex;
        this.originalTitle = (title == null) ? "" : title;

        buttons = new JButton[rows][cols];
        stoneForCell = new ImageIcon[rows][cols];

        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== icons =====
        mineIcon = loadIcon(MINE, TILE_SIZE, TILE_SIZE);
        surpriseIcon = loadIcon(SURPRISE, TILE_SIZE - 6, TILE_SIZE - 6);
        openedSurpriseIcon = loadIcon(OPENED_SURPRISE, TILE_SIZE - 6, TILE_SIZE - 6);
        questionIcon = loadIcon(QUESTION, TILE_SIZE - 6, TILE_SIZE - 6);
        questionUsedIcon = loadIcon(QUESTION_USED, TILE_SIZE - 6, TILE_SIZE - 6);

        // flag icon + cursor
        flagIcon = loadIcon(FLAG, 32, 32);
        Toolkit tk = Toolkit.getDefaultToolkit();
        flagCursor = tk.createCustomCursor(flagIcon.getImage(), new Point(1, 1), "flagCursor");

        // ===== TOP BAR (painted, no artifacts) =====
        nameLabel = new JLabel("");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        flagIconLabel = new JLabel(loadIcon(FLAG, 18, 18));
        flagIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        flagIconLabel.setToolTipText("Toggle flag mode");

        flagsRemainingLabel = new JLabel("— left");
        flagsRemainingLabel.setForeground(new Color(210, 210, 210));
        flagsRemainingLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        flagsRemainingLabel.setOpaque(false); // badge paints background

        // badge that paints its own rounded background
        JPanel flagBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // rounded dark chip
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        flagBadge.setOpaque(false);
        flagBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        // ✅ IMPORTANT: add components into the badge (you forgot this)
        flagBadge.add(flagIconLabel);
        flagBadge.add(flagsRemainingLabel);

        // top bar panel that also paints background (no artifacts)
        JPanel topBar = new JPanel(new BorderLayout()) {
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

        add(topBar, BorderLayout.NORTH);

        // ✅ ONLY clicking the FLAG ICON toggles flag mode
        flagIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (paused) return;
                controller.toggleFlagMode(boardIndex);
            }
        });

        // ===== stones =====
        loadStoneIcons();
        Random r = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                stoneForCell[i][j] = stoneIcons.get(r.nextInt(stoneIcons.size()));
            }
        }

        // ===== board grid =====
        JPanel boardPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        boardPanel.setOpaque(false);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                final int rIdx = row;
                final int cIdx = col;

                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setIcon(stoneForCell[row][col]);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);

                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {

                        if (paused) return;

                        // RIGHT CLICK
                        if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(boardIndex, rIdx, cIdx);
                            return;
                        }

                        // LEFT CLICK
                        if (SwingUtilities.isLeftMouseButton(e)) {

                            // block clicking on non-active board
                            if (controller.getModel().getCurrentPlayer() != myBoardIndex) {
                                return;
                            }

                            if (flagMode) controller.handleFlagClick(boardIndex, rIdx, cIdx);
                            else controller.handleCellClick(boardIndex, rIdx, cIdx);
                        }
                    }
                });

                buttons[row][col] = btn;
                boardPanel.add(btn);
            }
        }

        // wrapper keeps border tight around grid
        boardWrapper.setOpaque(false);
        boardWrapper.add(boardPanel);

        int boardW = cols * TILE_SIZE + (cols - 1) * 2;
        int boardH = rows * TILE_SIZE + (rows - 1) * 2;
        boardWrapper.setPreferredSize(new Dimension(boardW + 16, boardH + 16));

        boardHolder.setOpaque(false);
        boardHolder.add(boardWrapper);

        add(boardHolder, BorderLayout.CENTER);

        // name ellipsis (also on resize)
        SwingUtilities.invokeLater(this::updateNameEllipsis);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateNameEllipsis();
            }
        });

        setActive(false);
    }

    // called by controller / gameView
    public void setRemainingFlags(int remaining) {
        flagsRemainingLabel.setText(remaining + " left");
        flagsRemainingLabel.revalidate();
        flagsRemainingLabel.repaint();

        //  warn only once when it becomes 1
        if (remaining == 1 && !warnedOneFlagLeft) {
            warnedOneFlagLeft = true;

            String playerName = originalTitle; // your board title / player name
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    playerName + ", you have only 1 flag remaining!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        //  if flags go back above 1 , allow warning again later
        if (remaining > 1) {
            warnedOneFlagLeft = false;
        }
    }


    public void setActive(boolean active) {
        Color borderColor = active ? new Color(255, 165, 0) : new Color(150, 150, 150);

        boardWrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 4, true),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
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
        if (btn.getIcon() == null && btn.getText() != null) return;

        btn.setIcon(null);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);

        if (count == 0) {
            btn.setText("");
            return;
        }

        btn.setText(String.valueOf(count));
        btn.setFont(numberFont);
        btn.setForeground(getNumberColor(count));
    }

    public void revealSurprise(int r, int c) {
        buttons[r][c].setIcon(surpriseIcon);
    }

    public void activateSurprise(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(openedSurpriseIcon);
        btn.setText(null);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

    public void revealMineHit(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(mineIcon);
        btn.setText(null);

        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }

        btn.setBorder(null);
        btn.setContentAreaFilled(false);
    }

    public void revealAllMines(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = buttons[r][c];
                if (model.isMine(r, c)) {
                    btn.setIcon(mineIcon);
                    btn.setText(null);
                    btn.setBorder(null);
                    btn.setContentAreaFilled(false);
                }
            }
        }
    }

    public void removeFlag(int row, int col) {
        buttons[row][col].setIcon(stoneForCell[row][col]);
    }

    public void revealQuestion(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(questionIcon);
        btn.setText(null);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

    public void markQuestionUsed(int r, int c) {
        JButton btn = buttons[r][c];
        btn.setIcon(questionUsedIcon);
        btn.setText(null);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

    public void revealMineAuto(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(mineIcon);
        btn.setText(null);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
    }

    public void revealHintCell(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setIcon(null);
        btn.setText("");
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
    }

    public void revealAllSurprises(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (model.isSurprise(r, c)) {

                    if (model.isSurpriseActivated(r, c)) {
                        buttons[r][c].setIcon(openedSurpriseIcon);
                    } else {
                        buttons[r][c].setIcon(surpriseIcon);
                    }

                    buttons[r][c].setText(null);
                    buttons[r][c].setBorder(null);
                    buttons[r][c].setContentAreaFilled(false);

                    for (java.awt.event.ActionListener al : buttons[r][c].getActionListeners()) {
                        buttons[r][c].removeActionListener(al);
                    }
                }
            }
        }
    }

    private void loadStoneIcons() {
        try {
            for (int i = 1; i <= 20; i++) {
                String path = STONES_FOLDER + "stone" + i + ".png";
                java.net.URL url = getClass().getResource(path);
                if (url != null) stoneIcons.add(loadIcon(path, TILE_SIZE, TILE_SIZE));
            }
        } catch (Exception ignored) {}
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

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // ===== name ellipsis =====
    private void updateNameEllipsis() {
        int boardW = cols * TILE_SIZE + (cols - 1) * 2;
        int maxNameWidth = Math.max(80, boardW - 140); // leave space for badge
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
}
