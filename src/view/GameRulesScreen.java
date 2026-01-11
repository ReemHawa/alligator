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
        }        Image scaledImage = originalIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
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

        JLabel sub = new JLabel("learn how to play before you start", SwingConstants.CENTER);
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
        
        
        //520, 430, 220, 35

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
                "1. Your goal: Open all the safe squares on your board without hitting a mine.\n\n" +
                "2. Hidden squares: At the start, all squares are hidden. Click a square to reveal it.\n\n" +
                "3. What happens when you click a square:\n" +
                "   • If it’s empty, it opens automatically, and all empty neighbors open too (cascade).\n" +
                "   • If the square is a mine, you lose one life.\n" +
                "   • If it’s safe, a number appears (1–8).\n\n" +
                "4. What the numbers mean: A number shows how many mines touch that square.\n" +
                "   Use these numbers to decide which squares are safe to open."
        );
    }

    private void setExpandedRules() {
        txt.setText(
                 "1. Your goal: Open all the safe squares on your board without hitting a mine.\n\n" +
                 "2. Hidden squares: At the start, all squares are hidden. Click a square to reveal it.\n\n" +
                 "3. What happens when you click a square:\n" +
                 "   • If it’s empty, it opens automatically, and all empty neighbors open too (cascade).\n" +
                 "   • If the square is a mine, you lose one life.\n" +
                 "   • If it’s safe, a number appears (1–8).\n\n" +
                 "4. What the numbers mean: A number shows how many mines touch that square.\n" +
                 "   Use these numbers to decide which squares are safe to open.\n\n"+    
                 "5. Flags: If you think a square contains a mine, mark it with a flag.\n" +
                 "   This helps you avoid clicking it by mistake.\n\n"+
                 "6. Special squares:Your game includes two special types of squares:\n" +
                 "   • Question Square – Works like an empty square and can be activated once,\n" +
                 "    After activition, it becomes a USED. Activation costs points(depends on difficulty.\n"+
                 "   • Surprise Square – Gives a good or bad effect (50–50 chance).\n" +
                 "   Both special squares cost points to activate.\n\n"+
                 "7. Cascade (auto-opening): When you open an empty square,\n" +
                 "   all neighboring empty squares open automatically\n"+
                 "   until the area is surrounded by numbered squares.\n\n"+
                 "8. Winning the game: You win when all safe squares on the board are opened."
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
