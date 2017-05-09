package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

import java.util.Arrays;

public class VestaKeyboard {
    private static final int LINE_COUNT = 10;

    private int lineIndex = 0;
    private byte[] matrixLines = new byte[LINE_COUNT];

    public VestaKeyboard() {
        Arrays.fill(matrixLines, (byte) 0xFF);
    }

    /* Hi byte - line, low - position in line */
    public void setKey(byte code) {
        matrixLines[(code >> 4) & 0xF] &= ~(1 << (code & 0xF));
    }

    public void unsetKey(byte code) {
        matrixLines[(code >> 4) & 0xF] |= 1 << (code & 0xF);
    }

    public KeyboardModePort getKeyboardModePort() {
        return new KeyboardModePort();
    }

    public KeyboardPort getKeyboardPort() {
        return new KeyboardPort();
    }

    private class KeyboardModePort implements Port {
        @Override
        public void write(byte value) {
        }

        @Override
        public byte read() {
            return matrixLines[lineIndex];
        }
    }

    private class KeyboardPort implements Port {
        @Override
        public void write(byte value) {
            lineIndex = value;
        }

        @Override
        public byte read() {
            return 0;
        }
    }
}
