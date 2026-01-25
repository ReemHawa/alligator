package view;

import controller.gameController;
import model.board;
import model.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class gameView extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(gameView.class.getName());

    private JPanel motivationOverlay;
    private JLabel motivationOverlayLabel;
    private javax.swing.Timer motivationTimer;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;
    private JLabel scoreLabel;

    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    private int elapsedSeconds = 0;

    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private JButton btnExit;
    private JButton btnPause;

    private final ImageIcon pauseIcon;
    private final ImageIcon resumeIcon;

    private boolean paused = false;

    private JPanel boardsPanel;
    private JPanel lifePanel;
    private JPanel topPanel;
    private JPanel timerPanel;
    private JPanel bottomPanel;

    private JPanel pauseOverlay;
    private JButton resumeOverlayBtn;
    private JButton newGameOverlayBtn;

    private final gameController controller;
    private final game model;

    private static final String BG_PATH = "/images/background.jpeg";
    private static final String HEART_FULL = "/images/live.png";
    private static final String HEART_EMPTY = "/images/Llive.png";
    private static final String PAUSE_ICON  = "/images/Pause.png";
    private static final String RESUME_ICON = "/images/Resume.png";

    private JLabel motivationLabel;

    private boolean clickHintDialogOpen = false;
    private long lastClickHintMs = 0;

    private javax.swing.Timer resizeDebounce;

    public gameView(gameController controller, game model) {

        this.controller = controller;
        this.model = model;

        fullheart = loadIcon(HEART_FULL, 32, 32);
        emptyheart = loadIcon(HEART_EMPTY, 32, 32);
        pauseIcon  = loadIcon(PAUSE_ICON, 40, 40);
        resumeIcon = loadIcon(RESUME_ICON, 40, 40);

        setTitle("Dual Minesweeper Boards");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel(BG_PATH);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        final JLabel speaker = SpeakerIcon.createSpeakerLabel();
        final int iconSize = 40;
        final int marginLeft = 10;
        final int marginBottom = 10;

        getLayeredPane().setLayout(null);
        getLayeredPane().add(speaker, JLayeredPane.PALETTE_LAYER);

        btnExit = new JButton("Exit");
        btnExit.setFocusable(false);
        btnExit.addActionListener(e -> controller.exitToHome());

        btnPause = new JButton(pauseIcon);
        btnPause.setFocusable(false);
        btnPause.setBorderPainted(false);
        btnPause.setContentAreaFilled(false);
        btnPause.setOpaque(false);
        btnPause.addActionListener(e -> togglePause());

        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);
        topPanel.add(btnExit);
        topPanel.add(btnPause);

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        msgPanel.setOpaque(false);
        msgPanel.add(msgLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(topPanel, BorderLayout.NORTH);

        timerLabel = new JLabel("Timer: 00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerPanel = new JPanel();
        timerPanel.setOpaque(false);
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.add(timerLabel);
        timerPanel.add(Box.createVerticalStrut(6));

        boardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        boardsPanel.setOpaque(false);

        int initialTileSize = 30;
        boardViews[0] = new boardView(0, model.getPlayer1Name(), controller, initialTileSize);
        boardViews[1] = new boardView(1, model.getPlayer2Name(), controller, initialTileSize);

        boardsPanel.add(boardViews[0]);
        boardsPanel.add(boardViews[1]);

        updateRemainingFlags(0, controller.getRemainingFlags(0));
        updateRemainingFlags(1, controller.getRemainingFlags(1));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(timerPanel, BorderLayout.NORTH);
        centerPanel.add(boardsPanel, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        lifePanel.setOpaque(false);

        JPanel lifeWrapper = new JPanel(new BorderLayout());
        lifeWrapper.setOpaque(false);
        lifeWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        lifeWrapper.add(lifePanel, BorderLayout.CENTER);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            lifePanel.add(heartLabels[i]);
        }
        updateLives(model.getLivesRemaining());

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scorePanel.add(scoreLabel);

        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        msgPanel.setPreferredSize(new Dimension(0, 0));
        msgPanel.setMinimumSize(new Dimension(0, 0));

        bottomPanel.add(msgPanel, BorderLayout.NORTH);
        bottomPanel.add(lifeWrapper, BorderLayout.CENTER);
        bottomPanel.add(scorePanel, BorderLayout.EAST);

        content.add(bottomPanel, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setLocationRelativeTo(null);

        motivationOverlayLabel = new JLabel("", SwingConstants.CENTER);
        motivationOverlayLabel.setForeground(new Color(212, 175, 55));
        motivationOverlayLabel.setFont(new Font("Serif", Font.BOLD, 52));
        motivationOverlayLabel.setVisible(false);

        motivationOverlay = new JPanel(new GridBagLayout());
        motivationOverlay.setOpaque(false);
        motivationOverlay.add(motivationOverlayLabel);
        motivationOverlay.setVisible(false);
        getLayeredPane().add(motivationOverlay, JLayeredPane.POPUP_LAYER);

        buildPauseOverlay();
        resumeOverlayBtn.addActionListener(e -> resumeGame());
        newGameOverlayBtn.addActionListener(e -> controller.exitToHome());

        setVisible(true);

        installSafeHintClicks(root, content, centerPanel, timerPanel, bottomPanel, topPanel);

        SwingUtilities.invokeLater(() -> {
            applyBestTileSize();
            refreshLayeredLayout(speaker, iconSize, marginLeft, marginBottom);
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                refreshLayeredLayout(speaker, iconSize, marginLeft, marginBottom);
                debounceTileResize();
            }
        });

        setActiveBoard(0);
        startTimer();
        this.motivationLabel = msgLabel;
    }

    private void debounceTileResize() {
        if (resizeDebounce != null && resizeDebounce.isRunning()) resizeDebounce.stop();
        resizeDebounce = new javax.swing.Timer(140, e -> applyBestTileSize());
        resizeDebounce.setRepeats(false);
        resizeDebounce.start();
    }

    private void applyBestTileSize() {
        int best = computeTileSizeToFit();
        boardViews[0].setTileSize(best);
        boardViews[1].setTileSize(best);
        boardsPanel.revalidate();
        boardsPanel.repaint();
    }

    private int computeTileSizeToFit() {

        int availW = boardsPanel.getWidth();
        int availH = boardsPanel.getHeight();

        if (availW <= 0 || availH <= 0) {
            Dimension d = getContentPane().getSize();
            availW = Math.max(800, d.width);
            availH = Math.max(600, d.height);
        }

        int rows = model.getBoard(0).getRows();
        int cols = model.getBoard(0).getCols();

        int gapBetweenBoards = 40;

        Dimension outer0 = boardViews[0].getBoardOuterSize();
        int perBoardExtraW = outer0.width - (cols * 1 + (cols - 1) * 2); // baseline for scaling
        int perBoardExtraH = outer0.height - (rows * 1 + (rows - 1) * 2);

        int maxBoardW = (availW - gapBetweenBoards) / 2;
        int maxBoardH = availH;

        int denomW = cols + (cols - 1) * 2;
        int denomH = rows + (rows - 1) * 2;

        int tileByW = (maxBoardW - perBoardExtraW) / cols;
        int tileByH = (maxBoardH - perBoardExtraH) / rows;

        int tile = Math.min(tileByW, tileByH);

        tile = Math.max(tile, 18);
        tile = Math.min(tile, 46);

        return tile;
    }

    private void refreshLayeredLayout(JLabel speaker, int iconSize, int marginLeft, int marginBottom) {
        JLayeredPane lp = getLayeredPane();
        if (lp.getWidth() <= 0 || lp.getHeight() <= 0) return;

        speaker.setBounds(
                marginLeft,
                lp.getHeight() - iconSize - marginBottom,
                iconSize,
                iconSize
        );

        if (motivationOverlay != null) motivationOverlay.setBounds(0, 0, lp.getWidth(), lp.getHeight());
        if (pauseOverlay != null) pauseOverlay.setBounds(0, 0, lp.getWidth(), lp.getHeight());
    }

    public void showMotivationMessage(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;

        motivationOverlayLabel.setText(msg);
        motivationOverlayLabel.setVisible(true);
        motivationOverlay.setVisible(true);

        if (motivationTimer != null && motivationTimer.isRunning()) {
            motivationTimer.stop();
        }

        motivationTimer = new javax.swing.Timer(2500, e -> {
            motivationOverlayLabel.setVisible(false);
            motivationOverlay.setVisible(false);
        });
        motivationTimer.setRepeats(false);
        motivationTimer.start();
    }

    public void startTimer() {
        stopTimer();
        elapsedSeconds = 0;
        timerLabel.setText("Timer: 00:00");

        gameTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Timer: " + formatTime(elapsedSeconds));
        });
        gameTimer.start();
    }

    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    public void setActiveBoard(int playerIndex) {
        boardViews[0].setActive(playerIndex == 0);
        boardViews[1].setActive(playerIndex == 1);
    }

    public void updateLives(int livesRemaining) {
        for (int i = 0; i < heartLabels.length; i++) {
            heartLabels[i].setIcon(i < livesRemaining ? fullheart : emptyheart);
        }
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void revealSafeCell(int boardIndex, int row, int col, int count) {
        boardViews[boardIndex].revealSafeCell(row, col, count);
    }

    public void revealMineHit(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealMineHit(row, col);
    }

    public void revealAllMines(int boardIndex, board model) {
        boardViews[boardIndex].revealAllMines(model);
    }

    public void revealAllSurprises(int boardIndex, board model) {
        boardViews[boardIndex].revealAllSurprises(model);
    }

    public void updateTileFlag(int boardIndex, int row, int col) {
        boardViews[boardIndex].setFlagAtCell(row, col);
    }

    public void setFlagMode(int boardIndex, boolean on) {
        boardViews[boardIndex].setFlagMode(on);
    }

    public void setFlagAtCell(int boardIndex, int row, int col) {
        boardViews[boardIndex].setFlagAtCell(row, col);
    }

    public void showNotYourTurnMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Illegal move: it's not your turn!",
                "Not your turn",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void revealSurprise(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealSurprise(row, col);
    }

    public void activateSurprise(int boardIndex, int row, int col) {
        boardViews[boardIndex].activateSurprise(row, col);
    }

    public void removeFlag(int boardIndex, int row, int col) {
        boardViews[boardIndex].removeFlag(row, col);
    }

    public void showRemoveFlagMessage() {
        JOptionPane.showMessageDialog(
                this,
                "The selected cell is currently flagged.\nPlease remove the flag using a right-click before revealing it.",
                "Invalid Action",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void revealMineAuto(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealMineAuto(row, col);
    }

    public void revealHintCell(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealHintCell(row, col);
    }

    public void revealQuestion(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealQuestion(row, col);
    }

    public void markQuestionUsed(int boardIndex, int row, int col) {
        boardViews[boardIndex].markQuestionUsed(row, col);
    }

    public void showSurpriseResult(boolean good,
                                  int lifeDelta,
                                  int rewardPoints,
                                  int activationCost,
                                  int fullLifePenalty,
                                  int netPoints) {

        StringBuilder msg = new StringBuilder();

        if (good) {
            msg.append("Lucky you!\nGood surprise!\n");
            msg.append("Reward: +").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");

            if (lifeDelta == 1) {
                msg.append("Lives: +1\n");
            } else {
                msg.append("Lives were full -> extra cost: +")
                        .append(fullLifePenalty)
                        .append(" points\n");
            }
        } else {
            msg.append("Oops!\nBad surprise!\n");
            msg.append("Effect: ").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");
            msg.append("Lives: -1\n");
        }

        msg.append("\nNet change: ").append(netPoints >= 0 ? "+" : "").append(netPoints).append(" points");

        JOptionPane.showMessageDialog(this, msg.toString(), "Surprise!", JOptionPane.INFORMATION_MESSAGE);
    }

  
    public void showWinForBoth(int finalScore) {
        stopTimer();
        int choice = showWinDialog(finalScore);

        
        if (choice == JOptionPane.YES_OPTION) {
            controller.exitToHome();
        } else {
            controller.exitToHome();
        }
    }

    private void buildPauseOverlay() {

        pauseOverlay = new JPanel(new GridBagLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 140));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pauseOverlay.setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));

        JLabel title = new JLabel("PAUSE", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 34));
        title.setForeground(new Color(230, 140, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        resumeOverlayBtn = new JButton("RESUME");
        newGameOverlayBtn = new JButton("NEW GAME");

        Dimension btnSize = new Dimension(180, 35);
        resumeOverlayBtn.setMaximumSize(btnSize);
        newGameOverlayBtn.setMaximumSize(btnSize);

        resumeOverlayBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameOverlayBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(15));
        box.add(resumeOverlayBtn);
        box.add(Box.createVerticalStrut(10));
        box.add(newGameOverlayBtn);

        pauseOverlay.add(box);
        pauseOverlay.setVisible(false);

        getLayeredPane().add(pauseOverlay, JLayeredPane.MODAL_LAYER);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));

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
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image bg;

        BackgroundPanel(String path) {
            try {
                bg = new ImageIcon(BackgroundPanel.class.getResource(path)).getImage();
            } catch (Exception e) {
                bg = null;
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void togglePause() {
        if (!paused) pauseGame();
        else resumeGame();
    }

    private void pauseGame() {
        paused = true;
        btnPause.setIcon(resumeIcon);

        if (gameTimer != null) gameTimer.stop();

        setBoardsEnabled(false);
        btnPause.setEnabled(true);
        btnExit.setEnabled(true);

        if (pauseOverlay != null) pauseOverlay.setVisible(true);
    }

    private void resumeGame() {
        paused = false;
        btnPause.setIcon(pauseIcon);

        if (gameTimer != null) gameTimer.start();

        setBoardsEnabled(true);

        if (pauseOverlay != null) pauseOverlay.setVisible(false);
    }

    private void setBoardsEnabled(boolean enabled) {
        for (boardView bv : boardViews) {
            if (bv != null) bv.setPaused(!enabled);
        }
    }

    public void updateRemainingFlags(int boardIndex, int remaining) {
        if (boardViews[boardIndex] != null) {
            boardViews[boardIndex].setRemainingFlags(remaining);
        }
    }

    public boardView getBoardView(int i) {
        if (i < 0 || i >= boardViews.length) return null;
        return boardViews[i];
    }

    public String getFormattedElapsedTime() {
        return formatTime(elapsedSeconds);
    }

    public int showGameOverDialog() {

        final int[] choice = { JOptionPane.NO_OPTION };

        String p1 = model.getPlayer1Name();
        String p2 = model.getPlayer2Name();
        String level = capitalize(model.getLevel().name().toLowerCase());
        int score = model.getScore();
        int lives = model.getLivesRemaining();
        String time = getFormattedElapsedTime();

        final JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        dialog.setSize(this.getSize());
        try {
            dialog.setLocation(this.getLocationOnScreen());
        } catch (Exception ex) {
            dialog.setLocationRelativeTo(this);
        }

        JPanel overlay = new JPanel(new GridBagLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.dispose();
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.add(card, BorderLayout.CENTER);
        cardWrapper.setPreferredSize(new Dimension(520, 320));

        Font titleFont = new Font("Serif", Font.BOLD, 34);
        Font mainFont  = new Font("Serif", Font.PLAIN, 16);
        Font smallFont = new Font("Serif", Font.PLAIN, 14);

        JLabel title = new JLabel("Game Over!", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(titleFont);
        title.setForeground(new Color(60, 60, 60));

        JLabel names = new JLabel(p1 + " & " + p2, SwingConstants.CENTER);
        names.setAlignmentX(Component.CENTER_ALIGNMENT);
        names.setFont(mainFont);
        names.setForeground(new Color(70, 70, 70));

        JLabel lvl = new JLabel("Level: " + level, SwingConstants.CENTER);
        lvl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lvl.setFont(smallFont);
        lvl.setForeground(new Color(90, 90, 90));

        JLabel result = new JLabel("You lost. Give it another shot!", SwingConstants.CENTER);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setFont(smallFont);
        result.setForeground(new Color(90, 90, 90));

        JLabel stats = new JLabel("Score: " + score + "  Time: " + time, SwingConstants.CENTER);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats.setFont(smallFont);
        stats.setForeground(new Color(90, 90, 90));

        JLabel livesLbl = new JLabel("Remaining lives: " + lives, SwingConstants.CENTER);
        livesLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        livesLbl.setFont(smallFont);
        livesLbl.setForeground(new Color(90, 90, 90));

        JButton tryAgain = new JButton("Try Again");
        JButton exit = new JButton("Exit");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btns.setOpaque(false);
        btns.add(tryAgain);
        btns.add(exit);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(names);
        card.add(Box.createVerticalStrut(10));
        card.add(lvl);
        card.add(Box.createVerticalStrut(6));
        card.add(result);
        card.add(Box.createVerticalStrut(12));
        card.add(stats);
        card.add(Box.createVerticalStrut(6));
        card.add(livesLbl);
        card.add(Box.createVerticalStrut(18));
        card.add(btns);

        tryAgain.addActionListener(e -> {
            choice[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        exit.addActionListener(e -> {
            choice[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        overlay.add(cardWrapper, new GridBagConstraints());
        dialog.setContentPane(overlay);

        dialog.validate();
        dialog.repaint();
        dialog.setVisible(true);

        return choice[0];
    }

    // ====== ADDED: WIN dialog that looks exactly like the lose dialog, only text differs ======
    private int showWinDialog(int finalScore) {

        final int[] choice = { JOptionPane.NO_OPTION };

        String p1 = model.getPlayer1Name();
        String p2 = model.getPlayer2Name();
        String level = capitalize(model.getLevel().name().toLowerCase());
        int score = finalScore;
        int lives = model.getLivesRemaining();
        String time = getFormattedElapsedTime();

        final JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        dialog.setSize(this.getSize());
        try {
            dialog.setLocation(this.getLocationOnScreen());
        } catch (Exception ex) {
            dialog.setLocationRelativeTo(this);
        }

        JPanel overlay = new JPanel(new GridBagLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.dispose();
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.add(card, BorderLayout.CENTER);
        cardWrapper.setPreferredSize(new Dimension(520, 320));

        Font titleFont = new Font("Serif", Font.BOLD, 34);
        Font mainFont  = new Font("Serif", Font.PLAIN, 16);
        Font smallFont = new Font("Serif", Font.PLAIN, 14);

        JLabel title = new JLabel("Victory!", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(titleFont);
        title.setForeground(new Color(60, 60, 60));

        JLabel names = new JLabel(p1 + " & " + p2, SwingConstants.CENTER);
        names.setAlignmentX(Component.CENTER_ALIGNMENT);
        names.setFont(mainFont);
        names.setForeground(new Color(70, 70, 70));

        JLabel lvl = new JLabel("Level: " + level, SwingConstants.CENTER);
        lvl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lvl.setFont(smallFont);
        lvl.setForeground(new Color(90, 90, 90));

        JLabel result = new JLabel("You cleared both boards. Nice work!", SwingConstants.CENTER);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setFont(smallFont);
        result.setForeground(new Color(90, 90, 90));

        JLabel stats = new JLabel("Score: " + score + "  Time: " + time, SwingConstants.CENTER);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats.setFont(smallFont);
        stats.setForeground(new Color(90, 90, 90));

        JLabel livesLbl = new JLabel("Remaining lives: " + lives, SwingConstants.CENTER);
        livesLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        livesLbl.setFont(smallFont);
        livesLbl.setForeground(new Color(90, 90, 90));

        JButton playAgain = new JButton("Play Again");
        JButton exit = new JButton("Exit");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btns.setOpaque(false);
        btns.add(playAgain);
        btns.add(exit);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(names);
        card.add(Box.createVerticalStrut(10));
        card.add(lvl);
        card.add(Box.createVerticalStrut(6));
        card.add(result);
        card.add(Box.createVerticalStrut(12));
        card.add(stats);
        card.add(Box.createVerticalStrut(6));
        card.add(livesLbl);
        card.add(Box.createVerticalStrut(18));
        card.add(btns);

        playAgain.addActionListener(e -> {
            choice[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        exit.addActionListener(e -> {
            choice[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        overlay.add(cardWrapper, new GridBagConstraints());
        dialog.setContentPane(overlay);

        dialog.validate();
        dialog.repaint();
        dialog.setVisible(true);

        return choice[0];
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private long lastHintMs = 0;

    private void showClickHintNonModal() {
        long now = System.currentTimeMillis();
        if (now - lastHintMs < 800) return; // throttle
        lastHintMs = now;

        showMotivationMessage("Please click on your board to play.");
    }

    private void installSafeHintClicks(JComponent... targets) {
        java.awt.event.MouseAdapter hint = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {

                if (paused) return;

                Component src = (Component) e.getSource();

                // ignore clicks on Exit / Pause
                if (SwingUtilities.isDescendingFrom(src, btnExit)) return;
                if (SwingUtilities.isDescendingFrom(src, btnPause)) return;

                // convert click to frame coordinates
                Point pInFrame = SwingUtilities.convertPoint(
                        src,
                        e.getPoint(),
                        gameView.this
                );

                int active = model.getCurrentPlayer();
                int other  = (active == 0) ? 1 : 0;

                // inside active board → normal gameplay
                if (isInside(boardViews[active], pInFrame)) return;

                // inside other board → not your turn
                if (isInside(boardViews[other], pInFrame)) {
                    showNotYourTurnMessage();
                    return;
                }

                // background click → non-modal hint
                showClickYourBoardWarning();

            }
        };

        for (JComponent t : targets) {
            if (t != null) t.addMouseListener(hint);
        }
    }

    private boolean isInside(Component comp, Point pInFrame) {
        if (comp == null || !comp.isShowing()) return false;
        Point p = SwingUtilities.convertPoint(this, pInFrame, comp);
        return p.x >= 0 && p.y >= 0 && p.x < comp.getWidth() && p.y < comp.getHeight();
    }

    public void showClickYourBoardWarning() {
        long now = System.currentTimeMillis();

        if (clickHintDialogOpen) return;
        if (now - lastClickHintMs < 800) return; // throttle

        lastClickHintMs = now;
        clickHintDialogOpen = true;

        try {
            JOptionPane.showMessageDialog(
                    this,
                    "Please click on your board to play.",
                    "Invalid Action",
                    JOptionPane.WARNING_MESSAGE
            );
        } finally {
            clickHintDialogOpen = false;
        }
    }
}
