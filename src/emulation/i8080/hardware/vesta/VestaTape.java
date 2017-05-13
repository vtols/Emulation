package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VestaTape {
    byte[] tape;
    int p = 0;

    private int[] casSignature = {
            //0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x1F, 0xA6, 0xDE, 0xBA, 0xCC, 0x13, 0x7D, 0x74
    };

    List<Integer> signal = new ArrayList<>();

    List<Integer> zero = Arrays.asList(0, 0, 1, 1);
    List<Integer> one = Arrays.asList(0, 1, 0, 1);

    public VestaTape(byte[] casData) {
        tape = casData;
        generateSignal();
    }

    void generateSignal() {
        //generateSilence(1024);
        generateHeader(1024);
        for (int i = 8; i < tape.length; ) {
            boolean match = true;
            for (int j = 0; j < casSignature.length; j++) {
                if ((tape[i + j] & 0xff) != casSignature[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                //generateSilence(1024);
                generateHeader(1024);
                i += 8;
            } else {
                signal.addAll(zero);
                for (int j = 0; j < 8; j++) {
                    int x = (tape[i] >> j) & 1;
                    signal.addAll(x == 0 ? zero : one);
                }
                signal.addAll(one);
                signal.addAll(one);
                i++;
            }
        }
    }

    void generateHeader(int count) {
        for (int i = 0; i < count; i++) {
            signal.addAll(one);
        }
    }

    void generateSilence(int count) {
        for (int i = 0; i < count; i++) {
            signal.add(0);
        }
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
            return (byte) (signal.get(p++) << 7);
        }
    }
}
