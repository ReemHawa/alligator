package view;

import controller.gameController;
import model.board;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class boardView extends JPanel {
	
	private static final long serialVersionUID = 1L;

    private final JButton[][] buttons = new JButton[board.rows][board.cols];
    private final JPanel borderPanel = new JPanel(new GridBagLayout());
    private final ArrayList<ImageIcon> stoneIcons = new ArrayList<>();
    private final ImageIcon[][] stoneForCell = new ImageIcon[board.rows][board.cols];

    private final Font numberFont = new Font("Verdana", Font.BOLD, 20);

    private static final int TILE_SIZE = 40;
    private static final String STONES_FOLDER = "src/stones/";

    private final gameController controller;

    public boardView(int boardIndex, String title, gameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Verdana", Font.BOLD, 26));
        add(label, BorderLayout.NORTH);

        borderPanel.setOpaque(false);
        JPanel boardPanel = new JPanel(new GridLayout(board.rows, board.cols, 2, 2));
        boardPanel.setOpaque(false);

        loadStoneIcons();

        Random r = new Random();
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                stoneForCell[i][j] = stoneIcons.get(r.nextInt(stoneIcons.size()));
            }
        }

        for (int row = 0; row < board.rows; row++) {
            for (int col = 0; col < board.cols; col++) {
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

                btn.addActionListener(e -> controller.handleCellClick(boardIndex, rIdx, cIdx));

                buttons[row][col] = btn;
                boardPanel.add(btn);
            }
        }

        borderPanel.add(boardPanel);
        add(borderPanel, BorderLayout.CENTER);

        setActive(false);
    }

    // ---------------- PUBLIC METHODS FOR Controller ----------------

    public void setActive(boolean active) {
        Color borderColor = active ? new Color(255, 165, 0) : new Color(150, 150, 150);
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 4, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public void revealSafeCell(int row, int col, int count) {
        JButton btn = buttons[row][col];
        if (!btn.isEnabled()) return;
        btn.setEnabled(false);
        btn.setIcon(null);

        if (count == 0) {
           btn.setText("");
           return;
        }
         btn.setText(String.valueOf(count));
            btn.setFont(numberFont);
            btn.setForeground(getNumberColor(count));
    }

    public void revealMineHit(int row, int col) {
        JButton btn = buttons[row][col];
        btn.setEnabled(false);
        btn.setIcon(null);
        btn.setText("💥");
        btn.setForeground(Color.RED);
        btn.setFont(numberFont);
    }

    public void revealAllMines(board model) {
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                JButton btn = buttons[r][c];
                btn.setEnabled(false);
                if (model.isMine(r, c)) {
                    btn.setIcon(null);
                    btn.setText("💣");
                    btn.setForeground(Color.RED);
                    btn.setFont(numberFont);
                }
            }
        }
    }

    // ---------------- HELPER METHODS ----------------

    private void loadStoneIcons() {
        File folder = new File(STONES_FOLDER);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".png")) {
                    stoneIcons.add(loadAndScaleIcon(f.getPath(), TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private ImageIcon loadAndScaleIcon(String path, int w, int h) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private Color getNumberColor(int n) {
        switch (n) {
            case 1: return new Color(52, 152, 219);
            case 2: return new Color(46, 204, 113);
            case 3: return new Color(231, 76, 60);
            case 4: return new Color(155, 89, 182);
            case 5: return new Color(241, 196, 15);
            case 6: return new Color(26, 188, 156);
            case 7: return new Color(236, 240, 241);
            case 8: return new Color(127, 140, 141);
            default: return Color.WHITE;
        }
    }

	public gameController getController() {
		return controller;
	}
}
