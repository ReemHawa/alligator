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

        JLabel title = new JLabel("Game Rules", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setBounds(200, 40, 400, 40);
        bg.add(title);

        JLabel sub = new JLabel("Learn how to play before you start", SwingConstants.CENTER);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font("Serif", Font.PLAIN, 16));
        sub.setBounds(220, 80, 360, 30);
        bg.add(sub);

        rulesPanel = new JPanel();
        rulesPanel.setLayout(null);
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

        btnToggleRules = new JButton("Read more rules...");
        btnToggleRules.setBounds(20, 225, 180, 30);
        rulesPanel.add(btnToggleRules);

        btnToggleRules.addActionListener(e -> toggleRules());

        btnBack = new JButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            homeScreen.setVisible(true);
            dispose();
        });

        btnPickLevel = new JButton("Got it! Let's pick a level");
        btnPickLevel.setBounds(520, 460, 220, 35);
        bg.add(btnPickLevel);

        btnPickLevel.addActionListener(e -> {
            chooseLevelView levelScreen = new chooseLevelView(this, homeScreen);
            levelScreen.setVisible(true);
            setVisible(false);
        });

        setVisible(true);
    }

    private void setCollapsedRules() {
        txt.setText(
                "1. Your goal: Reveal all safe squares on your board without hitting mines.\n\n" +
                "2. Turns: Only the active player can play on their board.\n\n" +
                "3. Clicking a square:\n" +
                "   • Empty square → opens and triggers a cascade (auto-open).\n" +
                "   • Number square (1–8) → shows how many mines touch it.\n" +
                "   • Mine → you lose one life.\n\n" +
                "4. Flags: You can place flags to mark suspected mines.\n" +
                "   Flags are limited, and the remaining number is shown on screen.\n\n" +
                "5. Questions: Question squares can be revealed, then activated.\n" +
                "   Activating a question is timed and costs points."
        );
    }

    private void setExpandedRules() {
        txt.setText(
                "1. Your goal: Reveal all safe squares on your board without hitting mines.\n\n" +
                "2. Turns:\n" +
                "   • Only the active player can play on their board.\n" +
                "   • Normal reveals usually end your turn.\n\n" +
                "3. What happens when you click a square:\n" +
                "   • Empty square → opens and triggers a cascade (auto-open).\n" +
                "   • Number square (1–8) → shows how many mines touch it.\n" +
                "   • Mine → you lose one life.\n\n" +
                "4. Numbers meaning:\n" +
                "   A number shows how many mines touch that square.\n" +
                "   Use numbers to deduce safe squares.\n\n" +
                "5. Flags (Limited):\n" +
                "   • Use flags to mark suspected mines.\n" +
                "   • Each player has a limited number of flags (shown as “flags left”).\n" +
                "   • Placing a flag uses one from your remaining limit.\n\n" +
                "6. Flag mode:\n" +
                "   • You place flags only when flag mode is enabled (by clicking the flag icon).\n" +
                "   • Clicking a cell while in flag mode places a flag instead of revealing.\n\n" +
                "7. Special squares:\n" +
                "   • Question Square:\n" +
                "     - First click reveals it (like an empty square).\n" +
                "     - Second click activates it (only once).\n" +
                "     - Activation costs points and opens a timed question.\n" +
                "     - If time runs out, it counts as a wrong answer.\n\n" +
                "   • Surprise Square:\n" +
                "     - First click reveals it (like an empty square).\n" +
                "     - Second click activates it (only once).\n" +
                "     - Activation costs points and gives a random good/bad effect.\n\n" +
                "8. Cascade (auto-opening):\n" +
                "   When you open an empty square, neighboring empty squares open automatically\n" +
                "   until the area reaches numbered squares.\n\n" +
                "9. Winning:\n" +
                "   You win when all safe squares on a board are revealed (no safe squares remain)."
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

        rulesPanel.revalidate();
        rulesPanel.repaint();
    }
}
