package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import controller.PlayersNamesController;

public class PlayersNamesScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField txtPlayerA;
    private JTextField txtPlayerB;
    private JButton btnPlay;
    private JButton btnBack;

    private String level;
    private chooseLevelView levelScreen;
    private HomeScreen homeScreen;
    private PlayersNamesController controller;

    private static final String PH_A = "Enter first player name";
    private static final String PH_B = "Enter second player name";

    public PlayersNamesScreen(String level, chooseLevelView levelScreen, HomeScreen homeScreen) {
        this.level = level;
        this.levelScreen = levelScreen;
        this.homeScreen = homeScreen;

        setTitle("MineSweeper Players");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== Speaker icon (bottom-left) =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        bg.add(speaker);

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 5;

        speaker.setBounds(
                marginLeft,
                bg.getHeight() - iconSize - marginBottom,
                iconSize,
                iconSize
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setLocation(
                        marginLeft,
                        bg.getHeight() - iconSize - marginBottom
                );
            }
        });

        JLabel lblTitle = new JLabel("Enter your names:");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(200, 120, 400, 40);
        bg.add(lblTitle);

        JLabel lblSub = new JLabel("You Must Fill In Both Names To Start The Game.");
        lblSub.setFont(new Font("Serif", Font.BOLD, 16));
        lblSub.setForeground(Color.WHITE);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(160, 155, 480, 30);
        bg.add(lblSub);

        txtPlayerA = new JTextField(PH_A);
        txtPlayerA.setFont(new Font("Arial", Font.PLAIN, 18));
        txtPlayerA.setForeground(new Color(180, 180, 180));
        txtPlayerA.setBounds(260, 220, 280, 48);
        bg.add(txtPlayerA);

        txtPlayerA.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtPlayerA.getText().equals(PH_A)) {
                    txtPlayerA.setText("");
                    txtPlayerA.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPlayerA.getText().trim().isEmpty()) {
                    txtPlayerA.setText(PH_A);
                    txtPlayerA.setForeground(new Color(180, 180, 180));
                }
                if (controller != null) controller.onInputChanged();
            }
        });

        txtPlayerB = new JTextField(PH_B);
        txtPlayerB.setFont(new Font("Arial", Font.PLAIN, 18));
        txtPlayerB.setForeground(new Color(180, 180, 180));
        txtPlayerB.setBounds(260, 280, 280, 48);
        bg.add(txtPlayerB);

        txtPlayerB.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtPlayerB.getText().equals(PH_B)) {
                    txtPlayerB.setText("");
                    txtPlayerB.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPlayerB.getText().trim().isEmpty()) {
                    txtPlayerB.setText(PH_B);
                    txtPlayerB.setForeground(new Color(180, 180, 180));
                }
                if (controller != null) controller.onInputChanged();
            }
        });

        // ✅ Styled like Home page (white glass) - SAME SIZE & PLACE
        btnPlay = new HomeGlassButton("Let's Play");
        btnPlay.setBounds(300, 350, 200, 45);
        btnPlay.setEnabled(false);
        bg.add(btnPlay);

        // ✅ Styled like Home page (white glass) - SAME SIZE & PLACE
        btnBack = new HomeGlassButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            this.dispose();
            levelScreen.setVisible(true);
        });

        controller = new PlayersNamesController(this, level, homeScreen);

        KeyAdapter validator = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                controller.onInputChanged();
            }
        };
        txtPlayerA.addKeyListener(validator);
        txtPlayerB.addKeyListener(validator);

        btnPlay.addActionListener(e -> controller.onPlayClicked());

        // Initial check
        controller.onInputChanged();

        setVisible(true);
    }

    // ================= Controller Methods =================

    public String getLevel() {
        return level;
    }

    public String getPlayerAName() {
        return txtPlayerA.getText().trim();
    }

    public String getPlayerBName() {
        return txtPlayerB.getText().trim();
    }

    public boolean isPlayerAPlaceholder() {
        return txtPlayerA.getText().equals(PH_A);
    }

    public boolean isPlayerBPlaceholder() {
        return txtPlayerB.getText().equals(PH_B);
    }

    public void setPlayEnabled(boolean enabled) {
        btnPlay.setEnabled(enabled);
        // keep the same glass style; just make text slightly transparent when disabled
        btnPlay.setForeground(enabled ? Color.BLACK : new Color(0, 0, 0, 120));
        btnPlay.repaint();
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public HomeScreen getHomeScreen() {
        return homeScreen;
    }

    // ==========================================================
    // SAME STYLE AS YOUR ORIGINAL WHITE "GO BACK" BUTTON
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
            setFont(new Font("Serif", Font.BOLD, 16));
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

            // small hover overlay
            if (getModel().isRollover() && isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================================
    // Background
    // ==========================================================
    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image bg;

        public BackgroundPanel() {
            try {
                bg = new ImageIcon(getClass().getResource("/images/background.jpeg")).getImage();
            } catch (Exception e) {
                bg = new ImageIcon("src/images/background.jpeg").getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
