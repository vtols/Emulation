package emulation.i8080.cpu;

import java.nio.ByteBuffer;

import static emulation.i8080.cpu.Instruction.mnemonics;

public class Disassembler {
    int pc = 0;
    ByteBuffer bytes;

    public Disassembler(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public void disassembleAll() {
        pc = 0; //0x2920;
        while (pc < 0x4000) {
            disassembleSingle();
        }
    }

    void disassembleSingle() {
        int offset = pc;
        int code = bytes.get(pc++) & 0xFF;
        char[] template = mnemonics[code].toCharArray();
        StringBuilder format = new StringBuilder();
        for (int i = 0; i < template.length; i++) {
            if (template[i] != '#')
                format.append(template[i]);
            else {
                i++;
                switch (template[i]) {
                    case 'd':
                        format.append(String.format("%02XH", bytes.get(pc)));
                        pc++;
                        break;
                    case 'D':
                    case 'A':
                        format.append(String.format("%04XH", bytes.getShort(pc)));
                        pc += 2;
                        break;
                    default:
                        format.append("??");
                        break;
                }
            }
        }
        System.out.printf("%04X: %-15s\n", offset, format);
    }
}
