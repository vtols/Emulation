package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

public class VestaTape {
    byte[] tape;
    int p = 0, bp = 0, bx = 0, s = 0, k = 0;
    boolean header = true;
    private int[][] bxs = {
            {0, 0, 1, 1},
            {0, 1, 0, 1}
    };
    private int[] casSignature = {
            0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x1F, 0xA6, 0xDE, 0xBA, 0xCC, 0x13, 0x7D, 0x74
    };

    public VestaTape(byte[] casData) {
        tape = casData;
    }

    public Port getTapePort() {
        return new VestaTapePort();
    }

    private class VestaTapePort implements Port {
        @Override
        public void write(byte value) {

        }

        @Override
        public byte read() {
            int out;
            if (header) {
                if (p % 2 == 0)
                    out = 0;
                else
                    out = 1;
                p++;
                if (p == 4096) {
                    header = false;
                    p = 8;
                }
            } else {
                int currentBit;
                if (bp == 0) {
                    currentBit = 0;
                } else if (bp > 8) {
                    currentBit = 1;
                } else
                    currentBit = (tape[p] >> (bp - 1)) & 1;
                out = bxs[currentBit][bx];
                bx++;
                if (bx == 4) {
                    bx = 0;
                    bp++;
                }
                if (bp == 11) {
                    bp = 0;
                    p++;
                    k++;
                }
                if (bp == 9 && bx == 1 && k == 15) {
                    p++;
                    bp = 0;
                    bx = 0;
                    k = 0;
                }
            }
            return (byte) (out << 7);
        }
    }
}
