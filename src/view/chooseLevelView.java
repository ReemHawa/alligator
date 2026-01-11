package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//import model.DifficultyLevel;

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
    private Color defaultButtonColor;
    private Color defaultTextColor;
    private final Color hoverColor = new Color(220, 220, 220);
    private final Color levelSelectedColor = new Color(173, 216, 230);

    public chooseLevelView(GameRulesScreen rulesScreen, HomeScreen homeScreen) {
        this.rulesScreen = rulesScreen;
        this.homeScreen  = homeScreen;

        setTitle("MineSweeper - Choose Level");
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
            // Fallback for running from gameController.main
            originalIcon = new ImageIcon("src/images/background.jpeg");
        }
        Image scaledImage =
                originalIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
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

        descPanel = new JPanel() {
           
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

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

        defaultButtonColor = UIManager.getColor("Button.background");
        defaultTextColor   = UIManager.getColor("Button.foreground");

        btnEasy   = createLevelButton("Easy");
        btnMedium = createLevelButton("Medium");
        btnHard   = createLevelButton("Hard");

        btnEasy.setBounds(200, 210, 120, 60);
        btnMedium.setBounds(350, 210, 120, 60);
        btnHard.setBounds(500, 210, 120, 60);

        bg.add(btnEasy);
        bg.add(btnMedium);
        bg.add(btnHard);

        addLevelBehaviour(btnEasy, "EASY");
        addLevelBehaviour(btnMedium, "MEDIUM");
        addLevelBehaviour(btnHard, "HARD");

        btnNext = new JButton("Next");
        btnNext.setFont(new Font("Serif", Font.BOLD, 16));
        btnNext.setBounds(320, 400, 160, 45);
        btnNext.setEnabled(false);
        bg.add(btnNext);

        btnBack = new JButton("← Go Back");
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
                JOptionPane.showMessageDialog(this,
                        "Please choose a level first.");
                return;
            }

            PlayersNamesScreen namesScreen =
                    new PlayersNamesScreen(selectedLevel, this,homeScreen);
            namesScreen.setVisible(true);
            this.setVisible(false);
        });
    }

    private JButton createLevelButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 18));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBackground(defaultButtonColor);
        b.setForeground(defaultTextColor);
        return b;
    }

    private void addLevelBehaviour(JButton button, String levelKey) {

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                descPanel.setVisible(true);
                showDescription(levelKey);

                if (!levelKey.equals(selectedLevel)) {
                    button.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                descPanel.setVisible(false);
                descText.setText("");

                if (levelKey.equals(selectedLevel)) {
                    applySelectedStyle(button);
                } else {
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
        btnEasy.setForeground(defaultTextColor);
        btnMedium.setBackground(defaultButtonColor);
        btnMedium.setForeground(defaultTextColor);
        btnHard.setBackground(defaultButtonColor);
        btnHard.setForeground(defaultTextColor);

        switch (level) {
            case "EASY"   : applySelectedStyle(btnEasy); break; 
            case "MEDIUM" : applySelectedStyle(btnMedium) ; break;
            case "HARD"   : applySelectedStyle(btnHard); break;
        }

        btnNext.setEnabled(
                "EASY".equals(level) ||
                "MEDIUM".equals(level) ||
                "HARD".equals(level)
        );
    }

    private void applySelectedStyle(JButton button) {
        button.setBackground(levelSelectedColor);
        button.setForeground(defaultTextColor);
    }

    private void showDescription(String level) {
        switch (level) {
            case "EASY" : descText.setText(
                    "A 9×9 board with 10 mines.\n" +
                    "Good for beginners or for a calm start.\n" +
                    "You get 50 seconds per turn, so pick your move wisely."
            );
            break;
            case "MEDIUM" : descText.setText(
                    "A 13×13 board with 26 mines.\n" +
                    "Balanced difficulty- not too easy and not too hard.\n" +
                    "You get 40 seconds per turn to make your move, stay focused."
            );
            break;
            case "HARD" : descText.setText(
                    "A 16×16 board with 44 mines.\n" +
                    "Made for players who want a real challenge.\n" +
                    "You get 20 seconds per turn, so act fast."
            );
            break;
        }
    }
}
