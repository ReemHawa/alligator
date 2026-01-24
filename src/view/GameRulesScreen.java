package view;

import javax.swing.*;
import java.awt.*;

public class GameRulesScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnBack;
    private JButton btnPickLevel;
    private JButton btnToggleRules;

    private JTextArea txt;
    private JScrollPane scroll;

    private boolean expanded = false;

    // for resizing card nicely
    private RoundedPanel rulesCard;

    public GameRulesScreen(HomeScreen homeScreen) {
        setTitle("MineSweeper - Game Rules");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ===== TOP-RIGHT Back =====
        btnBack = new JButton("\u2190 Go Back");
        GridBagConstraints gbcBack = new GridBagConstraints();
        gbcBack.gridx = 0;
        gbcBack.gridy = 0;
        gbcBack.weightx = 1;
        gbcBack.weighty = 0;
        gbcBack.anchor = GridBagConstraints.NORTHEAST;
        gbcBack.insets = new Insets(18, 18, 10, 18);
        bg.add(btnBack, gbcBack);

        btnBack.addActionListener(e -> {
            homeScreen.setVisible(true);
            dispose();
        });

        // ===== CENTER content =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Game Rules", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));

        JLabel sub = new JLabel("Learn how to play before you start", SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font("Serif", Font.PLAIN, 16));

        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(sub);
        center.add(Box.createVerticalStrut(18));

        // ===== Rules card (glass) =====
        rulesCard = new RoundedPanel(22, new Color(210, 222, 222, 230));
        rulesCard.setLayout(new BorderLayout(10, 10));
        rulesCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

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

        btnToggleRules = new JButton("Read more rules...");
        btnToggleRules.setFocusPainted(false);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(btnToggleRules);

        rulesCard.add(scroll, BorderLayout.CENTER);
        rulesCard.add(bottomRow, BorderLayout.SOUTH);

        // initial size (collapsed)
        applyCardSize(false);

        center.add(rulesCard);

        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 1;
        gbcCenter.weightx = 1;
        gbcCenter.weighty = 1;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        gbcCenter.insets = new Insets(0, 25, 10, 25);
        bg.add(center, gbcCenter);

        btnToggleRules.addActionListener(e -> toggleRules());

        // ===== BOTTOM-RIGHT Pick Level =====
        btnPickLevel = new JButton("Got it! Let's pick a level");

        GridBagConstraints gbcPick = new GridBagConstraints();
        gbcPick.gridx = 0;
        gbcPick.gridy = 2;
        gbcPick.weightx = 1;
        gbcPick.weighty = 0;
        gbcPick.anchor = GridBagConstraints.SOUTHEAST;
        gbcPick.insets = new Insets(0, 18, 18, 18);
        bg.add(btnPickLevel, gbcPick);

        btnPickLevel.addActionListener(e -> {
            chooseLevelView levelScreen = new chooseLevelView(this, homeScreen);
            levelScreen.setVisible(true);
            setVisible(false);
        });

        // ===== BOTTOM-LEFT Speaker =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        speaker.setPreferredSize(new Dimension(40, 40));

        GridBagConstraints gbcSpeaker = new GridBagConstraints();
        gbcSpeaker.gridx = 0;
        gbcSpeaker.gridy = 3;
        gbcSpeaker.weightx = 1;
        gbcSpeaker.weighty = 0;
        gbcSpeaker.anchor = GridBagConstraints.SOUTHWEST;
        gbcSpeaker.insets = new Insets(0, 10, 8, 0);
        bg.add(speaker, gbcSpeaker);

        setMinimumSize(new Dimension(800, 550));
        setSize(950, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void toggleRules() {
        expanded = !expanded;

        if (expanded) {
            btnToggleRules.setText("Read less rules.");
            setExpandedRules();
        } else {
            btnToggleRules.setText("Read more rules...");
            setCollapsedRules();
        }

        applyCardSize(expanded);

        rulesCard.revalidate();
        rulesCard.repaint();
        revalidate();
        repaint();
    }

    // ✅ just change preferred sizes, no setBounds()
    private void applyCardSize(boolean expanded) {
        if (expanded) {
            rulesCard.setPreferredSize(new Dimension(720, 360));
            rulesCard.setMaximumSize(new Dimension(980, 420));
        } else {
            rulesCard.setPreferredSize(new Dimension(720, 290));
            rulesCard.setMaximumSize(new Dimension(980, 340));
        }
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
        txt.setCaretPosition(0);
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
                "     - First click reveals it.\n" +
                "     - Second click activates it (only once).\n" +
                "     - Activation costs points and opens a timed question.\n\n" +
                "   • Surprise Square:\n" +
                "     - First click reveals it.\n" +
                "     - Second click activates it (only once).\n" +
                "     - Activation costs points and gives a random good/bad effect.\n\n" +
                "8. Cascade (auto-opening):\n" +
                "   When you open an empty square, neighboring empty squares open automatically.\n\n" +
                "9. Winning:\n" +
                "   You win when all safe squares are revealed."
        );
        txt.setCaretPosition(0);
    }

    // ===== background scales automatically =====
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
