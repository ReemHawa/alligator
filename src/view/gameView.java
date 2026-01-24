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

    // ===== Motivation Overlay (CENTER, GOLD, BIG) =====
    private JPanel motivationOverlay;
    private JLabel motivationOverlayLabel;
    private javax.swing.Timer motivationTimer;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;
    private JLabel scoreLabel;

    // elapsed timer (existing)
    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    private int elapsedSeconds = 0;
 /* ===== TURN TIMER =====
    private javax.swing.Timer turnTimer;
    private int turnSecondsLeft = 0;
    private JLabel turnTimerLabel;
*/
    
    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private JButton btnExit;

    private JButton btnPause;

    private final ImageIcon pauseIcon;
    private final ImageIcon resumeIcon;
  

    private boolean paused = false;
 // ===== Pause Overlay =====
    private JPanel pauseOverlay;
    private JButton resumeOverlayBtn;
    private JButton newGameOverlayBtn;

 // references for restart & dialogs
    private final gameController controller;
    private final game model;

    private static final String BG_PATH = "/images/background.jpeg";
    private static final String HEART_FULL = "/images/live.png";
    private static final String HEART_EMPTY = "/images/Llive.png";
    private static final String PAUSE_ICON  = "/images/Pause.png";
    private static final String RESUME_ICON = "/images/Resume.png";

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
      /*  turnTimerLabel = new JLabel("Time left for your turn: ");
        turnTimerLabel.setForeground(Color.WHITE);
        turnTimerLabel.setFont(new Font("Verdana", Font.BOLD, 14));

     */
        // ===== speaker icon =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        
        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 5;

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
               	JLayeredPane lp = getLayeredPane();
            	speaker.setBounds(
            	        marginLeft,
            	        lp.getHeight() - iconSize - marginBottom,
            	        iconSize,
            	        iconSize
            	);
            	if (motivationOverlay != null) {
            	    motivationOverlay.setBounds(0, 0, lp.getWidth(), lp.getHeight());
            	}
            	if (pauseOverlay != null) {
            	    pauseOverlay.setBounds(0, 0, getWidth(), getHeight());
            	}

            }
        });

       
     // ===== top left exit =====
        btnExit = new JButton("Exit");
        btnExit.setFocusable(false);
        btnExit.addActionListener(e -> controller.exitToHome());

        // NEW: pause button
        btnPause = new JButton(pauseIcon);
        btnPause.setFocusable(false);
        btnPause.setBorderPainted(false);
        btnPause.setContentAreaFilled(false);
        btnPause.setOpaque(false);
        btnPause.addActionListener(e -> togglePause());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);
        topPanel.add(btnExit);

        // ===== message area (bottom) =====
        JLabel msgLabel = new JLabel(" ");
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        topPanel.add(btnPause);   // NEW

        
        motivationLabel = new JLabel(" ");
        motivationLabel.setForeground(Color.WHITE);
        motivationLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        msgPanel.setOpaque(false);
        msgPanel.add(msgLabel);
       // msgPanel.add(motivationLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(topPanel, BorderLayout.NORTH);

        // ===== elapsed timer label =====
        timerLabel = new JLabel("Timer: 00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

       
        JPanel timerPanel = new JPanel();
        timerPanel.setOpaque(false);
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.add(timerLabel);
        timerPanel.add(Box.createVerticalStrut(6));

        JPanel boardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        boardsPanel.setOpaque(false);

        int tileSize = computeTileSizeForBoards();

        boardViews[0] = new boardView(0, model.getPlayer1Name(), controller, tileSize);
        boardViews[1] = new boardView(1, model.getPlayer2Name(), controller, tileSize);

        boardsPanel.add(boardViews[0]);
        boardsPanel.add(boardViews[1]);
        
        //flags counter
        
        updateRemainingFlags(0, controller.getRemainingFlags(0));
        updateRemainingFlags(1, controller.getRemainingFlags(1));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(timerPanel, BorderLayout.NORTH);
        centerPanel.add(boardsPanel, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        // ===== hearts =====
        JPanel lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        lifePanel.setOpaque(false);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            lifePanel.add(heartLabels[i]);
        }
        updateLives(model.getLivesRemaining());

        // ===== score =====
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scorePanel.add(scoreLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        bottomPanel.add(msgPanel, BorderLayout.NORTH);
        bottomPanel.add(lifePanel, BorderLayout.CENTER);
        bottomPanel.add(scorePanel, BorderLayout.EAST);

        content.add(bottomPanel, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
        getLayeredPane().setLayout(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLocationRelativeTo(null);

        // ===== big motivation overlay label (CENTER) =====
        motivationOverlayLabel = new JLabel("", SwingConstants.CENTER);
        motivationOverlayLabel.setForeground(new Color(212, 175, 55));
        motivationOverlayLabel.setFont(new Font("Serif", Font.BOLD, 52));
        motivationOverlayLabel.setVisible(false);
       /* // ===== Set content pane =====
        setContentPane(root);
        getLayeredPane().setLayout(null);*/
   
        motivationOverlay = new JPanel(new GridBagLayout());
        motivationOverlay.setOpaque(false);
        motivationOverlay.add(motivationOverlayLabel);
        motivationOverlay.setVisible(false);

        getLayeredPane().add(motivationOverlay, JLayeredPane.POPUP_LAYER);
        getLayeredPane().add(speaker, JLayeredPane.PALETTE_LAYER);

        setVisible(true);
        //pause
        buildPauseOverlay();

        resumeOverlayBtn.addActionListener(e -> resumeGame());

        newGameOverlayBtn.addActionListener(e -> {
            controller.exitToHome(); 
            // או controller.startNewGame(); אם יש אצלכם
        });


        SwingUtilities.invokeLater(() -> {
            speaker.setBounds(
                    marginLeft,
                    getHeight() - iconSize - marginBottom,
                    iconSize,
                    iconSize
            );

            motivationOverlay.setBounds(0, 0, getWidth(), getHeight());
        });

        setActiveBoard(0);
        startTimer();

        // keep old API call working (controller uses it)
        this.motivationLabel = msgLabel;
    }

    // this keeps compatibility with your old code that calls motivationLabel (bottom line)
    private JLabel motivationLabel;

    // ===================== Motivation overlay =====================
    public void showMotivationMessage(String msg) {
        if (msg == null || msg.isBlank()) return;

        // ❌ לא מציגים למטה כדי שלא יהיה כפול
        // if (motivationLabel != null) motivationLabel.setText(msg);

        // ✅ מציגים רק overlay הגדול
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


    // ===================== elapsed timer =====================
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

    

    // ===================== API used by controller =====================
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
            msg.append("🎉 Lucky you!\nGood surprise!\n");
            msg.append("Reward: +").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");

            if (lifeDelta == 1) {
                msg.append("Lives: +1 ❤️\n");
            }  else {
                msg.append("Lives were full → extra cost: +")
                .append(fullLifePenalty)
                .append(" points (for the extra life)\n");
    }
        } else {
            msg.append("😬 Oops!\nBad surprise!\n");
            msg.append("Effect: ").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");
            msg.append("Lives: -1 ❤️\n");
        }

        msg.append("\nNet change: ").append(netPoints >= 0 ? "+" : "").append(netPoints).append(" points");

        JOptionPane.showMessageDialog(this, msg.toString(), "Surprise!", JOptionPane.INFORMATION_MESSAGE);
    }
    
  


    // ===================== dialogs =====================
    public int showGameOverDialog() {

        final int[] choice = { JOptionPane.NO_OPTION };

        String p1 = model.getPlayer1Name();
        String p2 = model.getPlayer2Name();
        String level = capitalize(model.getLevel().name().toLowerCase());
        int score = model.getScore();
        String time = getFormattedElapsedTime();
        int lives = model.getLivesRemaining();

        // ===== FULL-SCREEN OVER THE GAME WINDOW =====
        final JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Make dialog cover the whole gameView window
        dialog.setSize(this.getSize());
        try {
            dialog.setLocation(this.getLocationOnScreen());
        } catch (Exception ex) {
            dialog.setLocationRelativeTo(this);
        }

        // ===== overlay panel (dims entire window) =====
        JPanel overlay = new JPanel(new GridBagLayout()) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // full dim background
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        // ===== white center card (rounded) =====
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            /**
			 * 
			 */
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

        // Force the card size like your screenshot
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

        JLabel result = new JLabel("You lost ☹ Give it another shot!", SwingConstants.CENTER);
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

        Dimension btnSize = new Dimension(110, 26);
        tryAgain.setPreferredSize(btnSize);
        exit.setPreferredSize(btnSize);

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

        // IMPORTANT: re-pack after preferred sizes are set
        dialog.validate();
        dialog.repaint();

        dialog.setVisible(true);
        return choice[0];
    }


    public void showWinForBoth(int finalScore) {
        stopTimer();
        JOptionPane.showMessageDialog(this, "You win! Final Score: " + finalScore, "Victory",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    //pause
    private void buildPauseOverlay() {

        pauseOverlay = new JPanel(new GridBagLayout()) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 140)); // כהות לכל המסך
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

    
    

    // ===================== util =====================
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getFormattedElapsedTime() {
        return formatTime(elapsedSeconds);
    }
    
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            var url = getClass().getResource(path);
            if (url == null)
                return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));

            BufferedImage img = ImageIO.read(url);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
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

  
    private int computeTileSizeForBoards() {

        Dimension window = getContentPane().getSize();

        if (window.width == 0 || window.height == 0) {
            window = Toolkit.getDefaultToolkit().getScreenSize();
        }

        int reservedWidth = 260;
        int reservedHeight = 320;

        int rows = model.getBoard(0).getRows();
        int cols = model.getBoard(0).getCols();

        int maxBoardWidth = (window.width - reservedWidth) / 2;
        int maxBoardHeight = window.height - reservedHeight;

        int tileByWidth = maxBoardWidth / cols;
        int tileByHeight = maxBoardHeight / rows;

        int tile = Math.min(tileByWidth, tileByHeight);

        tile = Math.max(tile, 22);
        tile = Math.min(tile, 46);

        return tile;
    }

    public boardView getBoardView(int i) {
        return boardViews[i];
    }
    //Pasue
    private void togglePause() {
        if (!paused) {
            pauseGame();
        } else {
            resumeGame();
        }
    }
    private void pauseGame() {
        paused = true;
        btnPause.setIcon(resumeIcon);

        // 1) pause timer (בלי לאפס זמן)
        if (gameTimer != null) gameTimer.stop();

        // 2) לחסום לחיצות על הלוחות
        setBoardsEnabled(false);
        btnPause.setEnabled(true);
        btnExit.setEnabled(true);

        // ✅ NEW: show pause overlay
        if (pauseOverlay != null) pauseOverlay.setVisible(true);
    }

    private void resumeGame() {
        paused = false;
        btnPause.setIcon(pauseIcon);

        // 1) resume timer
        if (gameTimer != null) gameTimer.start();

        // 2) להחזיר לחיצות ללוחות
        setBoardsEnabled(true);

        // ✅ NEW: hide pause overlay
        if (pauseOverlay != null) pauseOverlay.setVisible(false);
    }

    private void setBoardsEnabled(boolean enabled) {
        for (int i = 0; i < boardViews.length; i++) {
            if (boardViews[i] != null) {
               // boardViews[i].setInteractionEnabled(enabled);
            	boardViews[i].setPaused(!enabled);
            }
        }
    }
    
    public void updateRemainingFlags(int boardIndex, int remaining) {
        if (boardViews[boardIndex] != null) {
            boardViews[boardIndex].setRemainingFlags(remaining);
        }
    }



  /*  public void startTurnTimer(int seconds, Runnable onTimeout) {
        stopTurnTimer();
        turnSecondsLeft = seconds;

        if (turnTimerLabel != null) {
            turnTimerLabel.setText("Time left for your turn: " + turnSecondsLeft + "s");
        }

        turnTimer = new javax.swing.Timer(1000, e -> {
            turnSecondsLeft--;

            if (turnTimerLabel != null) {
                turnTimerLabel.setText("Time left for your turn: " + turnSecondsLeft + "s");
            }

            if (turnSecondsLeft <= 0) {
                stopTurnTimer();
                onTimeout.run();
            }
        });

        turnTimer.start();
    }

    public void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }
   
    */
   
}