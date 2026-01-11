package view;

import javax.swing.*;
import java.awt.*;

public class GameRulesScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnBack;
    private JButton btnPickLevel;
    private JButton btnToggleRules;
    private JPanel rulesPanel;
    private JScrollPane scroll;
    private JTextArea txt;

    private boolean expanded = false;

    public GameRulesScreen(HomeScreen homeScreen) {

        setTitle("MineSweeper - Game Rules");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        ImageIcon originalIcon;
        try {
            originalIcon = new ImageIcon(
                    getClass().getResource("/images/background.jpeg")
            );
        } catch (Exception e) {
            originalIcon = new ImageIcon("src/images/background.jpeg");
        }

        Image scaledImage =
                originalIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
        JLabel bg = new JLabel(new ImageIcon(scaledImage));
        bg.setBounds(0, 0, 800, 550);
        bg.setLayout(null);
        add(bg);

        // ===== Speaker icon =====
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

        JLabel title = new JLabel("Game Rules", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setBounds(200, 40, 400, 40);
        bg.add(title);

        JLabel sub = new JLabel("learn how to play before you start", SwingConstants.CENTER);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font("Serif", Font.PLAIN, 16));
        sub.setBounds(220, 80, 360, 30);
        bg.add(sub);

        rulesPanel = new JPanel(null);
        rulesPanel.setBackground(new Color(210, 222, 222));
        rulesPanel.setBounds(80, 130, 640, 260);
        bg.add(rulesPanel);

        txt = new JTextArea();
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(new Font("Serif", Font.PLAIN, 15));
        setCollapsedRules();

        scroll = new JScrollPane(txt);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBounds(20, 20, 600, 200);
        rulesPanel.add(scroll);

        // ===== WHITE GLASS BUTTONS =====
        btnToggleRules = new GlassButton("Read more rules...");
        btnToggleRules.setBounds(20, 225, 180, 30);
        rulesPanel.add(btnToggleRules);
        btnToggleRules.addActionListener(e -> toggleRules());

        btnBack = new GlassButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        bg.add(btnBack);
        btnBack.addActionListener(e -> {
            homeScreen.setVisible(true);
            dispose();
        });

        btnPickLevel = new GlassButton("Got it! Let's pick a level");
        btnPickLevel.setBounds(520, 460, 220, 35);
        bg.add(btnPickLevel);
        btnPickLevel.addActionListener(e -> {
            chooseLevelView levelScreen = new chooseLevelView(this, homeScreen);
            levelScreen.setVisible(true);
            setVisible(false);
        });

        setVisible(true);
    }

    // ==========================================================
    // Rules text
    // ==========================================================
    private void setCollapsedRules() {
        txt.setText(
                "1. Your goal: Open all the safe squares on your board without hitting a mine.\n\n" +
                "2. Hidden squares: At the start, all squares are hidden. Click a square to reveal it.\n\n" +
                "3. What happens when you click a square:\n" +
                "   • If it’s empty, it opens automatically.\n" +
                "   • If it’s a mine, you lose a life.\n" +
                "   • If it’s safe, a number appears.\n\n" +
                "4. Numbers show how many mines touch that square."
        );
    }

    private void setExpandedRules() {
        txt.setText(
                "1. Your goal: Open all safe squares without hitting mines.\n\n" +
                "2. Flags help mark suspected mines.\n\n" +
                "3. Question & Surprise squares cost points to activate.\n\n" +
                "4. You win when all safe squares are opened."
        );
    }

    private void toggleRules() {
        if (!expanded) {
            expanded = true;
            btnToggleRules.setText("Read less rules.");
            rulesPanel.setBounds(80, 130, 640, 320);
            scroll.setBounds(20, 20, 600, 240);
            btnToggleRules.setBounds(20, 270, 180, 30);
            setExpandedRules();
        } else {
            expanded = false;
            btnToggleRules.setText("Read more rules...");
            rulesPanel.setBounds(80, 130, 640, 260);
            scroll.setBounds(20, 20, 600, 200);
            btnToggleRules.setBounds(20, 225, 180, 30);
            setCollapsedRules();
        }
        rulesPanel.repaint();
    }

    // ==========================================================
    // EXACT SAME STYLE AS YOUR ORIGINAL "GO BACK" BUTTON
    // ==========================================================
    private static class GlassButton extends JButton {

        private static final long serialVersionUID = 1L;

        public GlassButton(String text) {
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;

            // background (WHITE GLASS)
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
}
