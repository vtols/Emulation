package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VestaVideo {
    private int mask;
    private int startAddress;
    private byte[] ram;
    private int mode = 0;
    private BufferedImage screenBuffer;

    public VestaVideo(byte[] ram) {
        this.ram = ram;

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
                int pos = cy * 32 + cx;
                drawCode(cx * 8, cy * 8, ram[startAddress + pos] & 0xFF);
            }
        }
    }

    private void drawCodes() {
        for (int c = 0; c < 256; c++) {
            int y = (c >> 4) * 8;
            int x = (c & 0x0F) * 8;
            drawCode(x + 20, y + 20, c);
        }
    }

    private void drawCode(int x, int y, int c) {
        int coff = startAddress + 0x800 + c * 8;
        for (int sy = 0; sy < 8; sy++) {
            for (int sx = 0; sx < 8; sx++) {
                int v = (ram[coff + sy] >> sx) & 1;
                if (v == 0)
                    screenBuffer.setRGB(x + 7 - sx, y + sy, Color.black.getRGB());
                else
                    screenBuffer.setRGB(x + 7 - sx, y + sy, Color.white.getRGB());
            }
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Vesta PC8000");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ScreenPanel panel = new ScreenPanel();
        frame.getContentPane().add(panel);

        frame.pack();
        frame.setSize(1024, 768);
        frame.setVisible(true);

        Timer t = new Timer(10, e -> {
            updateScreen();
            panel.repaint();
        });
        t.start();
    }

    private class ScreenPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.drawImage(screenBuffer, 0, 0, 1024, 768, null);
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
            System.out.printf("Set video mode %d at %08X\n",
                    mode, startAddress);
        }

        @Override
        public byte read() {
            return (byte) mask;
        }
    }
}
