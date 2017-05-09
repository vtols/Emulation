package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class VestaVideo {
    private int mask;
    private int startAddress;
    private byte[] ram;
    private int mode = 0;
    private BufferedImage screenBuffer;
    private VestaKeyboard kb;

    public VestaVideo(byte[] ram, VestaKeyboard kb) {
        this.ram = ram;
        this.kb = kb;

        resetBuffer();

        javax.swing.SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void resetBuffer() {
        switch (mode) {
            case 0:
                screenBuffer = new BufferedImage(6 * 40, 8 * 24, BufferedImage.TYPE_INT_RGB);
                break;
            case 1:
                screenBuffer = new BufferedImage(8 * 32, 8 * 24, BufferedImage.TYPE_INT_RGB);
                break;
            case 2:
                screenBuffer = new BufferedImage(256, 191, BufferedImage.TYPE_INT_RGB);
                break;
        }
        Graphics g = screenBuffer.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, 256, 192);
    }

    private void updateScreen() {
        if (mode != 1)
            return;
        for (int cx = 0; cx < 32; cx++) {
            for (int cy = 0; cy < 24; cy++) {
                int pos = cy * 64 + cx;
                int c = ram[startAddress + pos] & 0xFF;
                drawCode(cx * 8, cy * 8, c, color(c));
            }
        }
    }

    private void drawCodes() {
        for (int c = 0; c < 256; c++) {
            int y = (c >> 4) * 8;
            int x = (c & 0x0F) * 8;
            drawCode(x + 20, y + 20, c, color(c));
        }
    }

    private int[] colors = {Color.black.getRGB(), Color.white.getRGB()};

    private int color(int c) {
        int off = startAddress + 0x400 + (c >> 3);
        return (ram[off] & 0xF0) != 0 ? 1 : 0;
    }

    private void drawCode(int x, int y, int c, int color) {
        int coff = startAddress + 0x800 + c * 8;
        for (int sy = 0; sy < 8; sy++) {
            for (int sx = 0; sx < 8; sx++) {
                int v = (ram[coff + sy] >> sx) & 1;
                if (v == 0)
                    screenBuffer.setRGB(x + 7 - sx, y + sy, colors[0]);
                else
                    screenBuffer.setRGB(x + 7 - sx, y + sy, colors[color]);
            }
        }
    }

    private void createAndShowGUI() {

        JFrame frame = new JFrame("Vesta PC8000");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ScreenPanel panel = new ScreenPanel();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.addKeyListener(panel);
        panel.setFocusable(true);

        JPanel keysPanel = new JPanel();
        keysPanel.setLayout(new GridLayout(10, 8));
        keysPanel.addKeyListener(panel);

        int z = 0;
        for (int i = 0; i < 10; i++)
            for (int j = 7; j >=0 ; j--) {
                int k = (i << 4) | j;
                int id = z++;
                JButton key = new JButton(VestaKeyMap.keyCaptions[id]);
                key.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        panel.requestFocusInWindow();
                        kb.setKey((byte) k);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        kb.unsetKey((byte) k);
                    }
                });
                keysPanel.add(key);
            }

        frame.getContentPane().setLayout(new GridLayout(1, 2));
        frame.getContentPane().add(panel);
        //frame.getContentPane().add(keysPanel);

        frame.pack();
        frame.setVisible(true);

        Timer t = new Timer(10, e -> {
            updateScreen();
            panel.repaint();
        });
        t.start();
    }

    private class ScreenPanel extends JPanel implements KeyListener {
        public void paintComponent(Graphics g) {
            g.drawImage(screenBuffer, 0, 0, getWidth(), getHeight(), null);
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            changeKey(e, true);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            changeKey(e, false);
        }

        private void changeKey(KeyEvent e, boolean set) {
            int k = VestaKeyMap.translate(e.getKeyCode());
            if (k >= 0) {
                if (set)
                    kb.setKey((byte) k);
                else
                    kb.unsetKey((byte) k);
            }
        }
    }

    public VideoModePort getModePort() {
        return new VideoModePort();
    }

    private class VideoModePort implements Port {
        @Override
        public void write(byte value) {
            mask = value & 0xFF;
            startAddress = (value & 0xC0) << 8;

            if ((value & 0x10) != 0) {
                mode = 2;
            } else {
                mode = (value >> 5) & 1;
            }
            resetBuffer();
            System.out.printf("Set video mode %d at address %08X\n",
                    mode, startAddress);
        }

        @Override
        public byte read() {
            return (byte) mask;
        }
    }
}
