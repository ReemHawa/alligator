package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class chooseLevelView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnEasy;
    private JButton btnMedium;
    private JButton btnHard;
    private JButton btnNext;
    private JButton btnBack;

    private GameRulesScreen rulesScreen;
    private HomeScreen homeScreen;

    private String selectedLevel = null;

    private JPanel descPanel;
    private JTextArea descText;

    private final Color hoverColor = new Color(220, 220, 220, 255);
    private final Color levelSelectedColor = new Color(173, 216, 230, 255);

    public chooseLevelView(GameRulesScreen rulesScreen, HomeScreen homeScreen) {
        this.rulesScreen = rulesScreen;
        this.homeScreen = homeScreen;

        setTitle("MineSweeper - Choose Level");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        ImageIcon originalIcon;

        try {
            originalIcon = new ImageIcon(getClass().getResource("/images/background.jpeg"));
        } catch (Exception e) {
            originalIcon = new ImageIcon("src/images/background.jpeg");
        }

        Image scaledImage = originalIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel bg = new JLabel(scaledIcon);
        bg.setBounds(0, 0, 800, 550);
        bg.setLayout(null);
        add(bg);

        // ===== Speaker icon (bottom-left) =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        getLayeredPane().add(speaker, JLayeredPane.POPUP_LAYER);

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 40;

        speaker.setBounds(
                marginLeft,
                getHeight() - iconSize - marginBottom,
                iconSize,
                iconSize
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setLocation(
                        marginLeft,
                        getHeight() - iconSize - marginBottom
                );
            }
        });

        JLabel title = new JLabel("choose your level", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setBounds(200, 60, 400, 40);
        bg.add(title);

        // ===== Description panel (unchanged) =====
        descPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        descPanel.setLayout(new BorderLayout());
        descPanel.setOpaque(false);
        descPanel.setBounds(80, 290, 640, 90);
        bg.add(descPanel);

        descText = new JTextArea();
        descText.setEditable(false);
        descText.setOpaque(false);
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        descText.setFont(new Font("Serif", Font.BOLD, 16));
        descText.setForeground(Color.WHITE);
        descText.setMargin(new Insets(10, 20, 10, 20));
        descPanel.add(descText, BorderLayout.CENTER);

        descPanel.setVisible(false);

        // ===== Level buttons (same size & position as you had) =====
        btnEasy = new LevelGlassButton("Easy");
        btnMedium = new LevelGlassButton("Medium");
        btnHard = new LevelGlassButton("Hard");

        btnEasy.setBounds(200, 210, 120, 60);
        btnMedium.setBounds(350, 210, 120, 60);
        btnHard.setBounds(500, 210, 120, 60);

        bg.add(btnEasy);
        bg.add(btnMedium);
        bg.add(btnHard);

        addLevelBehaviour(btnEasy, "EASY");
        addLevelBehaviour(btnMedium, "MEDIUM");
        addLevelBehaviour(btnHard, "HARD");

        // ===== Next button (same size & position) =====
        btnNext = new HomeGlassButton("Next");
        btnNext.setFont(new Font("Serif", Font.BOLD, 16));
        btnNext.setBounds(320, 400, 160, 45);
        btnNext.setEnabled(false);
        bg.add(btnNext);

        // ===== Back button (same size & position) =====
        btnBack = new HomeGlassButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            if (rulesScreen != null) {
                rulesScreen.setVisible(true);
            }
            dispose();
        });

        btnNext.addActionListener(e -> {
            if (selectedLevel == null) {
                JOptionPane.showMessageDialog(this, "Please choose a level first.");
                return;
            }

            PlayersNamesScreen namesScreen =
                    new PlayersNamesScreen(selectedLevel, this, homeScreen);
            namesScreen.setVisible(true);
            this.setVisible(false);
        });
    }

    // ==========================================================
    // Level hover + select behavior (keeps your logic)
    // ==========================================================
    private void addLevelBehaviour(JButton button, String levelKey) {

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                descPanel.setVisible(true);
                showDescription(levelKey);

                if (!levelKey.equals(selectedLevel) && button instanceof LevelGlassButton) {
                    ((LevelGlassButton) button).setMode(LevelGlassButton.Mode.HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                descPanel.setVisible(false);
                descText.setText("");

                if (levelKey.equals(selectedLevel) && button instanceof LevelGlassButton) {
                    ((LevelGlassButton) button).setMode(LevelGlassButton.Mode.SELECTED);
                } else if (button instanceof LevelGlassButton) {
                    ((LevelGlassButton) button).setMode(LevelGlassButton.Mode.NORMAL);
                }
            }
        });

        button.addActionListener(e -> selectLevel(levelKey));
    }

    private void selectLevel(String level) {
        selectedLevel = level;

        // reset all to normal
        if (btnEasy instanceof LevelGlassButton) ((LevelGlassButton) btnEasy).setMode(LevelGlassButton.Mode.NORMAL);
        if (btnMedium instanceof LevelGlassButton) ((LevelGlassButton) btnMedium).setMode(LevelGlassButton.Mode.NORMAL);
        if (btnHard instanceof LevelGlassButton) ((LevelGlassButton) btnHard).setMode(LevelGlassButton.Mode.NORMAL);

        switch (level) {
            case "EASY":
                ((LevelGlassButton) btnEasy).setMode(LevelGlassButton.Mode.SELECTED);
                break;
            case "MEDIUM":
                ((LevelGlassButton) btnMedium).setMode(LevelGlassButton.Mode.SELECTED);
                break;
            case "HARD":
                ((LevelGlassButton) btnHard).setMode(LevelGlassButton.Mode.SELECTED);
                break;
        }

        btnNext.setEnabled(
                "EASY".equals(level) ||
                "MEDIUM".equals(level) ||
                "HARD".equals(level)
        );
    }

    private void showDescription(String level) {
        switch (level) {
            case "EASY":
                descText.setText(
                        "A 9×9 board with 10 mines.\n" +
                        "Good for beginners or for a calm start.\n" +
                        "You get 50 seconds per turn, so pick your move wisely."
                );
                break;
            case "MEDIUM":
                descText.setText(
                        "A 13×13 board with 26 mines.\n" +
                        "Balanced difficulty- not too easy and not too hard.\n" +
                        "You get 40 seconds per turn to make your move, stay focused."
                );
                break;
            case "HARD":
                descText.setText(
                        "A 16×16 board with 44 mines.\n" +
                        "Made for players who want a real challenge.\n" +
                        "You get 20 seconds per turn, so act fast."
                );
                break;
        }
    }

    // ==========================================================
    // Home page glass button style (your ORIGINAL white Go Back)
    // ==========================================================
    private static class HomeGlassButton extends JButton {
        private static final long serialVersionUID = 1L;

        public HomeGlassButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.BLACK);
            setFont(new Font("Serif", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;

            // background
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // border
            g2.setColor(new Color(200, 200, 200, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================================
    // Level buttons: same home style, plus hover/selected colors
    // (keeps your original UX: hoverColor + levelSelectedColor)
    // ==========================================================
    private static class LevelGlassButton extends HomeGlassButton {
        private static final long serialVersionUID = 1L;

        enum Mode { NORMAL, HOVER, SELECTED }
        private Mode mode = Mode.NORMAL;

        public LevelGlassButton(String text) {
            super(text);
            setFont(new Font("Serif", Font.BOLD, 18));
        }

        public void setMode(Mode mode) {
            this.mode = mode;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;

            // base background = original home button style
            Color bgColor = new Color(255, 255, 255, 235);

            // overlay for hover/selected (your original colors)
            if (mode == Mode.HOVER) {
                bgColor = new Color(220, 220, 220, 235); // hoverColor
            } else if (mode == Mode.SELECTED) {
                bgColor = new Color(173, 216, 230, 235); // levelSelectedColor
            }

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // border
            g2.setColor(new Color(200, 200, 200, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
