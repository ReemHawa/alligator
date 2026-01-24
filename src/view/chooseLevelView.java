package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class chooseLevelView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnEasy, btnMedium, btnHard, btnNext, btnBack;
    private GameRulesScreen rulesScreen;
    private HomeScreen homeScreen;
    private String selectedLevel = null;

    private JPanel descPanel;
    private JTextArea descText;

    private Color defaultButtonColor;
    private Color defaultTextColor;
    private final Color hoverColor = new Color(220, 220, 220);
    private final Color levelSelectedColor = new Color(173, 216, 230);

    public chooseLevelView(GameRulesScreen rulesScreen, HomeScreen homeScreen) {
        this.rulesScreen = rulesScreen;
        this.homeScreen = homeScreen;

        setTitle("MineSweeper - Choose Level");
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
        gbcBack.anchor = GridBagConstraints.NORTHEAST;
        gbcBack.insets = new Insets(18, 18, 10, 18);
        bg.add(btnBack, gbcBack);

        btnBack.addActionListener(e -> {
            if (rulesScreen != null) rulesScreen.setVisible(true);
            dispose();
        });

        // ===== CENTER content =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Choose your level", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        center.add(title);
        center.add(Box.createVerticalStrut(26));

        defaultButtonColor = UIManager.getColor("Button.background");
        defaultTextColor   = UIManager.getColor("Button.foreground");

        JPanel levelRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        levelRow.setOpaque(false);

        btnEasy   = createLevelButton("Easy");
        btnMedium = createLevelButton("Medium");
        btnHard   = createLevelButton("Hard");

        levelRow.add(btnEasy);
        levelRow.add(btnMedium);
        levelRow.add(btnHard);

        center.add(levelRow);
        center.add(Box.createVerticalStrut(18));

        // description panel
        descPanel = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        descPanel.setOpaque(false);
        descPanel.setPreferredSize(new Dimension(680, 95));
        descPanel.setMaximumSize(new Dimension(900, 120));

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

        center.add(descPanel);
        center.add(Box.createVerticalStrut(18));

        btnNext = new JButton("Next");
        btnNext.setFont(new Font("Serif", Font.BOLD, 16));
        btnNext.setEnabled(false);
        btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNext.setPreferredSize(new Dimension(200, 45));
        btnNext.setMaximumSize(new Dimension(200, 45));
        center.add(btnNext);

        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 1;
        gbcCenter.weightx = 1;
        gbcCenter.weighty = 1;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        gbcCenter.insets = new Insets(0, 25, 25, 25);
        bg.add(center, gbcCenter);

        // ===== Speaker bottom-left =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        speaker.setPreferredSize(new Dimension(40, 40));

        GridBagConstraints gbcSpeaker = new GridBagConstraints();
        gbcSpeaker.gridx = 0;
        gbcSpeaker.gridy = 2;
        gbcSpeaker.weightx = 1;
        gbcSpeaker.anchor = GridBagConstraints.SOUTHWEST;
        gbcSpeaker.insets = new Insets(0, 10, 8, 0);
        bg.add(speaker, gbcSpeaker);

        addLevelBehaviour(btnEasy, "EASY");
        addLevelBehaviour(btnMedium, "MEDIUM");
        addLevelBehaviour(btnHard, "HARD");

        btnNext.addActionListener(e -> {
            if (selectedLevel == null) {
                JOptionPane.showMessageDialog(this, "Please choose a level first.");
                return;
            }
            PlayersNamesScreen namesScreen = new PlayersNamesScreen(selectedLevel, this, homeScreen);
            namesScreen.setVisible(true);
            setVisible(false);
        });

        setMinimumSize(new Dimension(800, 550));
        setSize(950, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createLevelButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 18));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBackground(defaultButtonColor);
        b.setForeground(defaultTextColor);
        b.setPreferredSize(new Dimension(140, 60));
        return b;
    }

    private void addLevelBehaviour(JButton button, String levelKey) {
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                descPanel.setVisible(true);
                showDescription(levelKey);
                if (!levelKey.equals(selectedLevel)) button.setBackground(hoverColor);
            }
            @Override public void mouseExited(MouseEvent e) {
                descPanel.setVisible(false);
                descText.setText("");
                if (levelKey.equals(selectedLevel)) applySelectedStyle(button);
                else {
                    button.setBackground(defaultButtonColor);
                    button.setForeground(defaultTextColor);
                }
            }
        });
        button.addActionListener(e -> selectLevel(levelKey));
    }

    private void selectLevel(String level) {
        selectedLevel = level;

        btnEasy.setBackground(defaultButtonColor);
        btnMedium.setBackground(defaultButtonColor);
        btnHard.setBackground(defaultButtonColor);

        switch (level) {
        case "EASY":
            applySelectedStyle(btnEasy);
            break;
        case "MEDIUM":
            applySelectedStyle(btnMedium);
            break;
        case "HARD":
            applySelectedStyle(btnHard);
            break;
    }

        btnNext.setEnabled(true);
    }

    private void applySelectedStyle(JButton button) {
        button.setBackground(levelSelectedColor);
        button.setForeground(defaultTextColor);
    }

    private void showDescription(String level) {
    	switch (level) {
        case "EASY":
            descText.setText("A 9×9 board with 10 mines.\nGood for beginners or a calm start.");
            break;
        case "MEDIUM":
            descText.setText("A 13×13 board with 26 mines.\nBalanced difficulty – not too easy and not too hard.");
            break;
        case "HARD":
            descText.setText("A 16×16 board with 44 mines.\nMade for players who want a real challenge.");
            break;
    }

    }

    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image bg;

        public BackgroundPanel() {
            try { bg = new ImageIcon(getClass().getResource("/images/background.jpeg")).getImage(); }
            catch (Exception e) { bg = new ImageIcon("src/images/background.jpeg").getImage(); }
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
