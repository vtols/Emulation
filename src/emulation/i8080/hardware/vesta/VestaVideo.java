package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class VestaVideo {
    private static final int[] textPalette = {
            0x000000,
            0x000000,
            0x00ff00,
            0x7fff7f,
            0x0000ff,
            0x00007f,
            0x00ffff,
            0x7f7fff,

            0xff0000,
            0xff7f7f,
            0xffff00,
            0xffff7f,
            0xff00ff,
            0xff7fff,
            0x7f7f7f,
            0xffffff
    };

    private int mask;
    private int startAddress;
    private byte[] ram;
    private int mode = 0, drawMode;
    private int colorMode = 0;
    private int charBufferOffset = 0x400;
    private int charGenOffset = 0x800;
    private final BufferedImage[] screenBuffer;
    private VestaKeyboard kb;

    public VestaVideo(byte[] ram, VestaKeyboard kb) {
        this.ram = ram;
        this.kb = kb;

        screenBuffer = new BufferedImage[] {
                new BufferedImage(8 * 32, 8 * 24, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(6 * 40, 8 * 24, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(256, 192, BufferedImage.TYPE_INT_RGB)
        };

        resetBuffer();

        javax.swing.SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void resetBuffer() {
        drawMode = mode;
        Graphics g = screenBuffer[drawMode].getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, 256, 192);
    }

    private void updateScreen() {
        switch (drawMode) {
            case 0:
                drawCodes(32, 24, 8, 8);
                break;
            case 1:
                drawCodes(40, 24, 6, 8);
                break;
            case 2:
                drawVideo();
                break;
        }
    }

    private void drawVideo() {
        for (int i = 0; i < 192; i++) {
            for (int j = 0; j < 256; j++) {
                int code = ram[startAddress + 0x1800 + ((i / 8) * 32 + j / 8) % 256] & 0xff;
                int codeY = i % 8;
                int codeX = j % 8;
                int part = i / 64;
                byte graphics = ram[part * 0x800 + code * 8 + codeY];
                int color = ram[0x2000 + part * 0x800 + code * 8 + codeY];
                int fg = (graphics >> (7 - codeX)) & 1;
                if (fg == 0)
                    color >>= 4;
                screenBuffer[drawMode].setRGB(j, i, textPalette[color & 0x0f]);
            }
        }
    }

    private void drawCodes(int tw, int th, int cw, int ch) {
        for (int cx = 0; cx < tw; cx++) {
            for (int cy = 0; cy < th; cy++) {
                int pos = cy * (drawMode == 1 ? 64 : 32) + cx;
                int c = ram[startAddress + charBufferOffset + pos] & 0xFF;
                drawCode(cw, ch, cx * cw, cy * ch, c);
            }
        }
    }

    private int color(int fg, int c) {
        if (drawMode == 1)
            return fg == 0 ? Color.black.getRGB() : Color.white.getRGB();
        int off = startAddress + charBufferOffset + 0x400 + (c >> 3);
        int value = ram[off];
        if (fg == 0)
            value >>= 4;
        return textPalette[value & 0x0f];
    }

    private void drawCode(int w, int h, int x, int y, int c) {
        int charOffset = startAddress + charGenOffset + c * 8;
        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                int v = (ram[charOffset + sy] >> (7 - sx)) & 1;
                screenBuffer[drawMode].setRGB(x + sx, y + sy, color(v, c));
            }
        }
    }

    private void createAndShowGUI() {

        JFrame frame = new JFrame("Vesta PC8000");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ScreenPanel panel = new ScreenPanel();
        panel.setPreferredSize(new Dimension(720, 576));
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
            drawMode = mode;
            updateScreen();
            panel.repaint();
        });
        t.start();
    }

    private class ScreenPanel extends JPanel implements KeyListener {
        public void paintComponent(Graphics g) {
            g.drawImage(screenBuffer[drawMode], 0, 0, getWidth(), getHeight(), null);
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

    public Port getModePort() {
        return new VideoModePort();
    }

    public Port getColorModePort() {
        return new ColorModePort();
    }

    public Port getCharacterBufferPort() {
        return new CharacterBufferPort();
    }

    public Port getCharacterGeneratorPort() {
        return new CharacterGeneratorPort();
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
            System.out.printf("Set video mode %d at address %04X\n",
                    mode, startAddress);
        }

        @Override
        public byte read() {
            return (byte) mask;
        }
    }

    private class ColorModePort implements Port {
        @Override
        public void write(byte value) {
            colorMode = value;
        }

        @Override
        public byte read() {
            return 0;
        }
    }

    private class CharacterBufferPort implements Port {
        @Override
        public void write(byte value) {
            if (mode == 0) {
                charBufferOffset = (value & 0x0F) << 10;
            } else if (mode == 1) {
                charBufferOffset = (value & 0x0E) << 10;
            }
        }

        @Override
        public byte read() {
            return 0;
        }
    }

    private class CharacterGeneratorPort implements Port {
        @Override
        public void write(byte value) {
            charGenOffset = (value & 0x0E) << 10;
        }

        @Override
        public byte read() {
            return 0;
        }
    }
}
