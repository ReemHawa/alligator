package view;

import controller.gameController;
import model.board;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class boardView extends JPanel {

    private static final long serialVersionUID = 1L;

    private JButton[][] buttons;
    private ImageIcon[][] stoneForCell;
    private final JPanel borderPanel = new JPanel(new GridBagLayout());
    private final ArrayList<ImageIcon> stoneIcons = new ArrayList<>();

    private final Font numberFont = new Font("Verdana", Font.BOLD, 20);
    private static int TILE_SIZE = 40;

    private boolean flagMode = false;
    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private final JLabel flagButton;

    
    private static final String FLAG = "/images/flag.png";
    private static final String STONES_FOLDER = "/stones/";
    private static final String MINE = "/images/mine.png";
    
    private static final String SURPRISE = "/images/surprise.png";
    private static final String OPENED_SURPRISE = "/images/openedsurprise.png";

    private ImageIcon surpriseIcon;
    private ImageIcon openedSurpriseIcon;



    private final int rows;
    private final int cols;

    public boardView(int boardIndex, String title, gameController controller) {

        board model = controller.getModel().getBoard(boardIndex);
        this.rows = model.getRows();
        this.cols = model.getCols();
        
        if (this.rows == 9 && cols == 9) {   //   Easy level
            TILE_SIZE= 45;                
        }
        if (this.rows == 16 && cols == 16) {   // Hard level
            TILE_SIZE= 32;                
        }
        //med level

        buttons = new JButton[rows][cols];
        stoneForCell = new ImageIcon[rows][cols];

        setLayout(new BorderLayout());
        setOpaque(false);
        
        mineIcon = loadIcon(MINE, TILE_SIZE, TILE_SIZE);
        
        //Load surprise icon
        
        surpriseIcon = loadIcon(SURPRISE, TILE_SIZE - 6, TILE_SIZE - 6);
        openedSurpriseIcon = loadIcon(OPENED_SURPRISE, TILE_SIZE - 6, TILE_SIZE - 6);
        
       




        // Load flag icon from resources
        flagIcon = loadIcon(FLAG, 32, 32);
        flagButton = new JLabel(flagIcon);
        flagButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Verdana", Font.BOLD, 26));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        topPanel.setOpaque(false);
        topPanel.add(label);
        topPanel.add(flagButton);
        add(topPanel, BorderLayout.NORTH);

        // Load stone icons
        loadStoneIcons();
        Random r = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                stoneForCell[i][j] = stoneIcons.get(r.nextInt(stoneIcons.size()));
            }
        }

        borderPanel.setOpaque(false);
        JPanel boardPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        boardPanel.setOpaque(false);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                final int rIdx = row;
                final int cIdx = col;

                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setMargin(new Insets(0, 0, 0, 0));

                btn.setIcon(stoneForCell[row][col]);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);

                btn.addActionListener(e -> {
                    if (flagMode) {
                        controller.handleFlagClick(boardIndex, rIdx, cIdx);
                    } else {
                        controller.handleCellClick(boardIndex, rIdx, cIdx);
                    }
                });

                buttons[row][col] = btn;
                boardPanel.add(btn);
            }
        }

        borderPanel.add(boardPanel);
        add(borderPanel, BorderLayout.CENTER);

        setActive(false);

        flagButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                controller.toggleFlagMode(boardIndex);
            }
        });
    }

    public void setActive(boolean active) {
        Color borderColor = active ? new Color(255, 165, 0) : new Color(150, 150, 150);
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 4, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));
    }

    public void setFlagMode(boolean enabled) {
        this.flagMode = enabled;
    }

    public void setFlagAtCell(int row, int col) {
        JButton btn = buttons[row][col];

        btn.setIcon(flagIcon);
        btn.setText(null);

        // Prevent further clicks without disabling
        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
    }


    public void revealSafeCell(int row, int col, int count) {
        JButton btn = buttons[row][col];

        // Prevent double reveal
        if (btn.getIcon() == null && btn.getText() != null) return;

        // Remove stone icon
        btn.setIcon(null);

        // Disable further clicks WITHOUT disabling the button
        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }

        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);

        if (count == 0) {
            btn.setText("");
            return;
        }

        btn.setText(String.valueOf(count));
        btn.setFont(numberFont);
        btn.setForeground(getNumberColor(count));
    }
    
    public void revealSurprise(int r, int c) {
        buttons[r][c].setIcon(surpriseIcon);
    }

    public void activateSurprise(int r, int c) {
        buttons[r][c].setIcon(openedSurpriseIcon);
    }

    
    public void revealMineHit(int row, int col) {
        JButton btn = buttons[row][col];

        // Remove stone
        btn.setIcon(mineIcon);
        btn.setText(null);

        // Prevent further clicks
        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }

        btn.setBorder(null);
        btn.setContentAreaFilled(false);
    }


    public void revealAllMines(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                JButton btn = buttons[r][c];

                
                for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }

                if (model.isMine(r, c)) {
                    btn.setIcon(mineIcon);
                    btn.setText(null);
                    btn.setBorder(null);
                    btn.setContentAreaFilled(false);
                }
            }
        }
    }



   
    private void loadStoneIcons() {
        try {
            // Try loading 20 stone images automatically
            for (int i = 1; i <= 20; i++) {
                String path = STONES_FOLDER + "stone" + i + ".png";
                java.net.URL url = getClass().getResource(path);

                if (url != null) {
                    stoneIcons.add(loadIcon(path, TILE_SIZE, TILE_SIZE));
                }
            }
        } catch (Exception ignored) {}
    }


    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return emptyIcon(w, h);

            BufferedImage img = ImageIO.read(url);

            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();

            return new ImageIcon(resized);
        } catch (Exception e) {
            return emptyIcon(w, h);
        }
    }


    private ImageIcon emptyIcon(int w, int h) {
        return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
    }
    
    ///////////////////////////////////////////////////////////////////
 // //////////DEBUG ONLY – reveal all surprise cells at game start//////////////
    ///////////////////////////////
    public void debugRevealAllSurprises(board model) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (model.isSurprise(r, c)) {
                    buttons[r][c].setIcon(surpriseIcon);
                }
            }
        }
    }


    private Color getNumberColor(int n) {
        switch (n) {
            case 1: return new Color(52, 152, 219);   // Blue
            case 2: return new Color(46, 204, 113);   // Green
            case 3: return new Color(231, 76, 60);    // Red
            case 4: return new Color(155, 89, 182);   // Purple
            case 5: return new Color(241, 196, 15);   // Yellow
            case 6: return new Color(26, 188, 156);   // Teal
            case 7: return Color.WHITE;
            case 8: return Color.GRAY;
            default: return Color.WHITE;
        }
    }

}