package view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import controller.PlayersNamesController;

public class PlayersNamesScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private  JTextField txtPlayerA;
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
        // it always starts placeholder
        btnPlay = new JButton("Let's Play");
        btnPlay.setBounds(300, 350, 200, 45);
        btnPlay.setEnabled(false);
        btnPlay.setBackground(new Color(200, 200, 200));
        bg.add(btnPlay);

        btnBack = new JButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        btnBack.setFont(new Font("Serif", Font.BOLD, 16));
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

 /// using the methods of the controller

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
        btnPlay.setBackground(enabled ? new JButton().getBackground() : new Color(200, 200, 200));
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public HomeScreen getHomeScreen() {
        return homeScreen;
    }


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