package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import controller.PlayersNamesController;

public class PlayersNamesScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField txtPlayerA, txtPlayerB;
    private JButton btnPlay, btnBack;
    private JLabel errA, errB;

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
            dispose();
            levelScreen.setVisible(true);
        });

        // ===== CENTER form =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Enter your names:", SwingConstants.CENTER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Both names must be 4–14 letters (A–Z).", SwingConstants.CENTER);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setFont(new Font("Serif", Font.BOLD, 16));
        lblSub.setForeground(Color.WHITE);

        center.add(lblTitle);
        center.add(Box.createVerticalStrut(6));
        center.add(lblSub);
        center.add(Box.createVerticalStrut(25));

        txtPlayerA = new JTextField(PH_A);
        styleField(txtPlayerA);
        center.add(wrapField(txtPlayerA, 320, 48));

        errA = errorLabel();
        center.add(errA);
        center.add(Box.createVerticalStrut(10));

        txtPlayerB = new JTextField(PH_B);
        styleField(txtPlayerB);
        center.add(wrapField(txtPlayerB, 320, 48));

        errB = errorLabel();
        center.add(errB);
        center.add(Box.createVerticalStrut(22));

        btnPlay = new JButton("Let's Play");
        btnPlay.setEnabled(false);
        btnPlay.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlay.setPreferredSize(new Dimension(220, 45));
        btnPlay.setMaximumSize(new Dimension(220, 45));
        center.add(btnPlay);

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

        // ===== placeholder behavior + validation hook =====
        attachPlaceholder(txtPlayerA, PH_A);
        attachPlaceholder(txtPlayerB, PH_B);

        controller = new PlayersNamesController(this, level, homeScreen);

        KeyAdapter validator = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { controller.onInputChanged(); }
        };
        txtPlayerA.addKeyListener(validator);
        txtPlayerB.addKeyListener(validator);

        btnPlay.addActionListener(e -> controller.onPlayClicked());

        controller.onInputChanged();

        setMinimumSize(new Dimension(800, 550));
        setSize(950, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Arial", Font.PLAIN, 18));
        f.setForeground(new Color(180, 180, 180));
    }

    private JPanel wrapField(JComponent c, int w, int h) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        c.setPreferredSize(new Dimension(w, h));
        c.setMaximumSize(new Dimension(w, h));
        p.add(c);
        return p;
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(new Color(255, 120, 120));
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void attachPlaceholder(JTextField field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(180, 180, 180));
                }
                if (controller != null) controller.onInputChanged();
            }
        });
    }

    // ===== controller API =====
    public String getLevel() { return level; }
    public String getPlayerAName() { return txtPlayerA.getText().trim(); }
    public String getPlayerBName() { return txtPlayerB.getText().trim(); }
    public boolean isPlayerAPlaceholder() { return txtPlayerA.getText().equals(PH_A); }
    public boolean isPlayerBPlaceholder() { return txtPlayerB.getText().equals(PH_B); }

    public void setPlayEnabled(boolean enabled) {
        btnPlay.setEnabled(enabled);
        btnPlay.setBackground(enabled ? new JButton().getBackground() : new Color(200, 200, 200));
    }

    public void setNameError(int index, String message) {
        JLabel target = (index == 0) ? errA : errB;
        target.setText(message == null ? " " : message);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid Name", JOptionPane.WARNING_MESSAGE);
    }

    public HomeScreen getHomeScreen() { return homeScreen; }

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
