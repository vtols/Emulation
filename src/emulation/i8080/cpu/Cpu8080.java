package emulation.i8080.cpu;

import emulation.i8080.hardware.Memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static emulation.i8080.cpu.Instruction.*;

public class Cpu8080 {
    private static final int REG_A = 0;
    private static final int REG_F = 1;
    private static final int REG_B = 2;
    private static final int REG_C = 3;
    private static final int REG_D = 4;
    private static final int REG_E = 5;
    private static final int REG_H = 6;
    private static final int REG_L = 7;

    private static final int REG_SP = 8;
    private static final int REG_PC = 10;

    private static final int REG_PSW = REG_A;

    private static final int[] R_MAP = {
            REG_B, REG_C, REG_D, REG_E, REG_H, REG_L, 0, REG_A
    };

    private static final int[] D_MAP = {
            REG_B, REG_D, REG_H, REG_SP
    };

    private static final int[] Q_MAP =  {
            REG_B, REG_D, REG_H, REG_PSW
    };

    private static final int MEM_REF = 6;

    private static final int FLAG_C = 0x01;
    private static final int FLAG_P = 0x04;
    private static final int FLAG_A = 0x10;
    private static final int FLAG_Z = 0x40;
    private static final int FLAG_S = 0x80;

    private enum Arithmetic {
        OP_ADD, OP_SUB, OP_AND, OP_OR, OP_XOR
    }

    private static final int[] PARITY = new int[256];

    static {
        fillParity();
    }

    private static void fillParity() {
        for (int i = 0; i < 256; i++) {
            int x = 1, s = i;
            while (s > 0) {
                s &= (s - 1);
                x ^= 1;
            }
            PARITY[i] = x;
        }
    }

    private Memory memory;
    private ByteBuffer regs = ByteBuffer.allocate(0xC).order(ByteOrder.BIG_ENDIAN);
    private Port[] ports = new Port[256];
    private boolean running = true, interruptions = false;
    private byte interruption = 0;
    private int count = 0;

    public Cpu8080(Memory memory) {
        this.memory = memory;
        reset();
    }

    public void setPort(int portNumber, Port port) {
        ports[portNumber] = port;
    }

    public String disassemble(int address) {
        int pos = address & 0xFFFF;
        int code = memory.read(pos++) & 0xFF;
        char[] template = mnemonics[code].toCharArray();
        StringBuilder format = new StringBuilder();
        for (int i = 0; i < template.length; i++) {
            if (template[i] != '#')
                format.append(template[i]);
            else {
                i++;
                switch (template[i]) {
                    case 'd':
                        format.append(String.format("%02XH", memory.read(pos++)));
                        break;
                    case 'D':
                    case 'A':
                        format.append(String.format("%04XH", memory.readLong(pos)));
                        pos += 2;
                        break;
                    default:
                        format.append("??");
                        break;
                }
            }
        }
        return format.toString();
    }

    private void reset() {
        for (int i = 0; i < 12; i++)
            putRegister(i, (byte) 0x00);
        putRegister(REG_F, (byte) 0x02);
    }

    private int leftPart(int code) {
        return (code >> 3) & 0x07;
    }

    private int rightPart(int code) {
        return code & 0x07;
    }

    private int registerPairPart(int code) {
        return (code >> 4) & 0x03;
    }

    private void setFlag(int flag, int value) {
        byte f = getRegister(REG_F);
        putRegister(REG_F, (byte) (f & ~flag | (-value & flag)));
    }

    private boolean getFlag(int flag) {
        byte f = getRegister(REG_F);
        return (f & flag) != 0;
    }

    private void putRegister(int reg, byte value) {
        regs.put(reg, value);
    }

    private byte getRegister(int reg) {
        return regs.get(reg);
    }

    private short getRegisterLong(int reg) {
        return regs.getShort(reg);
    }

    private int toAddress(short value) {
        return (int) value & 0xFFFF;
    }

    private int getAddress(int reg) {
        return toAddress(getRegisterLong(reg));
    }

    private void putRegisterLong(int reg, short value) {
        regs.putShort(reg, value);
    }

    private byte fetchBy(int reg) {
        return memory.read(getAddress(reg));
    }

    private short fetchLongBy(int reg) {
        return memory.readLong(getAddress(reg));
    }

    private byte fetchAt(short valueAddress) {
        return memory.read(toAddress(valueAddress));
    }

    private short fetchLongAt(short valueAddress) {
        return memory.readLong(toAddress(valueAddress));
    }

    private void storeBy(int reg, byte value) {
        memory.write(getAddress(reg), value);
    }

    private void storeLongBy(int reg, short value) {
        memory.writeLong(getAddress(reg), value);
    }

    private void storeAt(short valueAddress, byte value) {
        memory.write(toAddress(valueAddress), value);
    }

    private void storeLongAt(short valueAddress, short value) {
        memory.writeLong(toAddress(valueAddress), value);
    }

    private void putRegMem(int ref, byte value) {
        if (ref == MEM_REF)
            storeBy(REG_H, value);
        else
            putRegister(R_MAP[ref], value);
    }

    private byte getRegMem(int ref) {
        if (ref == MEM_REF)
            return fetchBy(REG_H);
        else
            return getRegister(R_MAP[ref]);
    }

    private void putPair(int ref, short value) {
        putRegisterLong(D_MAP[ref], value);
    }

    private void updateRegisterLong(int reg, int increment) {
        putRegisterLong(reg, (short) (getRegisterLong(reg) + increment));
    }

    private byte getByPc() {
        byte res = fetchBy(REG_PC);
        updateRegisterLong(REG_PC, +1);
        return res;
    }

    private short getLongByPc() {
        short res = fetchLongBy(REG_PC);
        updateRegisterLong(REG_PC, +2);
        return res;
    }

    private void pushRegisterLong(int reg) {
        short value = getRegisterLong(reg);
        updateRegisterLong(REG_SP, -2);
        storeLongBy(REG_SP, value);
    }

    private void popRegisterLong(int reg) {
        short value = fetchLongBy(REG_SP);
        updateRegisterLong(REG_SP, +2);
        putRegisterLong(reg, value);
    }

    private void out(int to, byte value) {
        to &= 0xFF;
        if (ports[to] != null)
            ports[to].write(value);
    }

    private byte in(int from) {
        from &= 0xFF;
        if (ports[from] != null)
            return ports[from].read();
        return 0;
    }

    public void run(short start) {
        putRegisterLong(REG_PC, start);
        while (running) {
            runSingle();
        }
    }

    public void runSingle() {
        int intCode;
        int oldPc = getRegisterLong(REG_PC);

        if (!interruptions)
            interruption = 0;

        if (interruptions && interruption != 0) {
            intCode = interruption;
            interruption = 0;
            interruptions = false;
            execute(intCode & 0xFF, oldPc);
        } else {
            execute(getByPc() & 0xFF, oldPc);
        }
    }

    public void interrupt(byte code) {
        interruption = code;
    }

    private int doArithmetics(Arithmetic type, int a, int b, int carry, int flags) {
        int t;
        switch (type) {
            case OP_ADD:
                t = a + b + carry;
                break;
            case OP_SUB:
                t = a - b - carry;
                break;
            case OP_AND:
                t = a & b;
                break;
            case OP_OR:
                t = a | b;
                break;
            case OP_XOR:
                t = a ^ b;
                break;
            default:
                throw new IllegalStateException();
        }

        if ((flags & FLAG_C) != 0) {
            if (type == Arithmetic.OP_ADD || type == Arithmetic.OP_SUB)
                setFlag(FLAG_C, ((a ^ b ^ (t >> 1)) >>> 8) & 1);
            else
                setFlag(FLAG_C, 0);
        }
        if ((flags & FLAG_A) != 0)
            setFlag(FLAG_A, ((a ^ b ^ t) >> 4) & 1);
        if ((flags & FLAG_P) != 0)
            setFlag(FLAG_P, PARITY[t & 0xFF]);
        if ((flags & FLAG_Z) != 0)
            setFlag(FLAG_Z, t == 0 ? 1 : 0);
        if ((flags & FLAG_S) != 0)
            setFlag(FLAG_S, (t & 0x80) >>> 7);
        return t;
    }

    private int doArithmetics(Arithmetic type, int a, int b, int flags) {
        return doArithmetics(type, a, b, 0, flags);
    }

    private void execute(int code, int savePc) {
        int src, dst;
        int s, t, p;
        short j;

        Instruction inst = decoded[code];


        switch (inst) {
            case NOP:
                break;
            case MOV:
                dst = leftPart(code);
                src = rightPart(code);

                if (src != dst)
                    putRegMem(dst, getRegMem(src));
                break;
            case JMP:
                j = getLongByPc();
                putRegisterLong(REG_PC, j);
                break;
            case JZ:
                j = getLongByPc();
                if (getFlag(FLAG_Z))
                    putRegisterLong(REG_PC, j);
                break;
            case JNZ:
                j = getLongByPc();
                if (!getFlag(FLAG_Z))
                    putRegisterLong(REG_PC, j);
                break;
            case JC:
                j = getLongByPc();
                if (getFlag(FLAG_C))
                    putRegisterLong(REG_PC, j);
                break;
            case JNC:
                j = getLongByPc();
                if (!getFlag(FLAG_C))
                    putRegisterLong(REG_PC, j);
                break;
            case JPE:
                j = getLongByPc();
                if (getFlag(FLAG_P))
                    putRegisterLong(REG_PC, j);
                break;
            case JPO:
                j = getLongByPc();
                if (!getFlag(FLAG_P))
                    putRegisterLong(REG_PC, j);
                break;
            case JP:
                j = getLongByPc();
                if (!getFlag(FLAG_S))
                    putRegisterLong(REG_PC, j);
                break;
            case JM:
                j = getLongByPc();
                if (getFlag(FLAG_S))
                    putRegisterLong(REG_PC, j);
                break;
            case LXI:
                putPair(registerPairPart(code), getLongByPc());
                break;
            case MVI:
                putRegMem(leftPart(code), getByPc());
                break;
            case PCHL:
                putRegisterLong(REG_PC, getRegisterLong(REG_H));
                break;
            case SPHL:
                putRegisterLong(REG_SP, getRegisterLong(REG_H));
                break;
            case LHLD:
                putRegisterLong(REG_H, fetchLongAt(getLongByPc()));
                break;
            case SHLD:
                storeLongAt(getLongByPc(), getRegisterLong(REG_H));
                break;
            case CALL:
                j = getLongByPc();
                pushRegisterLong(REG_PC);
                putRegisterLong(REG_PC, j);
                break;
            case CC:
                j = getLongByPc();
                if (getFlag(FLAG_C)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CPO:
                j = getLongByPc();
                if (!getFlag(FLAG_P)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CPE:
                j = getLongByPc();
                if (getFlag(FLAG_P)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CM:
                j = getLongByPc();
                if (getFlag(FLAG_S)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CP:
                j = getLongByPc();
                if (!getFlag(FLAG_S)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CZ:
                j = getLongByPc();
                if (getFlag(FLAG_Z)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CNZ:
                j = getLongByPc();
                if (!getFlag(FLAG_Z)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case CNC:
                j = getLongByPc();
                if (!getFlag(FLAG_C)) {
                    pushRegisterLong(REG_PC);
                    putRegisterLong(REG_PC, j);
                }
                break;
            case RET:
                popRegisterLong(REG_PC);
                break;
            case RZ:
                if (getFlag(FLAG_Z))
                    popRegisterLong(REG_PC);
                break;
            case RNZ:
                if (!getFlag(FLAG_Z))
                    popRegisterLong(REG_PC);
                break;
            case RP:
                if (!getFlag(FLAG_S))
                    popRegisterLong(REG_PC);
                break;
            case RM:
                if (getFlag(FLAG_S))
                    popRegisterLong(REG_PC);
                break;
            case RPE:
                if (getFlag(FLAG_P))
                    popRegisterLong(REG_PC);
                break;
            case RPO:
                if (!getFlag(FLAG_P))
                    popRegisterLong(REG_PC);
                break;
            case RC:
                if (getFlag(FLAG_C))
                    popRegisterLong(REG_PC);
                break;
            case RNC:
                if (!getFlag(FLAG_C))
                    popRegisterLong(REG_PC);
                break;
            case LDAX:
                putRegister(REG_A, fetchBy(D_MAP[registerPairPart(code)]));
                break;
            case INX:
                updateRegisterLong(D_MAP[registerPairPart(code)], +1);
                break;
            case DCX:
                updateRegisterLong(D_MAP[registerPairPart(code)], -1);
                break;
            case PUSH:
                pushRegisterLong(Q_MAP[registerPairPart(code)]);
                break;
            case POP:
                popRegisterLong(Q_MAP[registerPairPart(code)]);
                break;
            case XCHG:
                t = getRegisterLong(REG_H);
                s = getRegisterLong(REG_D);
                putRegisterLong(REG_H, (short) s);
                putRegisterLong(REG_D, (short) t);
                break;
            case XTHL:
                t = getRegisterLong(REG_H);
                s = fetchLongBy(REG_SP);
                putRegisterLong(REG_H, (short) s);
                storeLongBy(REG_SP, (short) t);
                break;
            case RLC:
                s = getRegister(REG_A) & 0xFF;
                t = s & 0x80;
                putRegister(REG_A, (byte) ((s << 1) | (t >>> 7)));
                setFlag(FLAG_C, t >>> 7);
                break;
            case RRC:
                s = getRegister(REG_A) & 0xFF;
                t = s & 0x01;
                putRegister(REG_A, (byte) ((s >>> 1) | (t << 7)));
                setFlag(FLAG_C, t);
                break;
            case RAL:
                s = getRegister(REG_A) & 0xFF;
                t = s & 0x80;
                putRegister(REG_A, (byte) ((s << 1) | (getFlag(FLAG_C) ? 1 : 0)));
                setFlag(FLAG_C, t >>> 7);
                break;
            case RAR:
                s = getRegister(REG_A) & 0xFF;
                t = s & 0x01;
                putRegister(REG_A, (byte) ((s >>> 1) | ((getFlag(FLAG_C) ? 1 : 0) << 7)));
                setFlag(FLAG_C, t);
                break;
            case CMA:
                putRegister(REG_A, (byte) ~getRegister(REG_A));
                break;
            case DCR:
                src = leftPart(code);
                t = doArithmetics(Arithmetic.OP_SUB, getRegMem(src), 1,
                        FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegMem(src, (byte) t);
                break;
            case INR:
                src = leftPart(code);
                t = doArithmetics(Arithmetic.OP_ADD, getRegMem(src), 1,
                        FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegMem(src, (byte) t);
                break;
            case CPI:
                doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                break;
            case ADI:
                t = doArithmetics(Arithmetic.OP_ADD,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case ACI:
                t = doArithmetics(Arithmetic.OP_ADD,
                        getRegister(REG_A), getByPc(), (getFlag(FLAG_C) ? 1 : 0),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case SUI:
                t = doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case SBI:
                t = doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getByPc(), (getFlag(FLAG_C) ? 1 : 0),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case ANI:
                t = doArithmetics(Arithmetic.OP_AND,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case ORI:
                t = doArithmetics(Arithmetic.OP_OR,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case XRI:
                t = doArithmetics(Arithmetic.OP_XOR,
                        getRegister(REG_A), getByPc(),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case ADD:
                t = doArithmetics(Arithmetic.OP_ADD,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case ADC:
                t = doArithmetics(Arithmetic.OP_ADD,
                        getRegister(REG_A), getRegMem(rightPart(code)), (getFlag(FLAG_C) ? 1 : 0),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case SUB:
                t = doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case SBB:
                t = doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getRegMem(rightPart(code)), (getFlag(FLAG_C) ? 1 : 0),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                putRegister(REG_A, (byte) t);
                break;
            case ANA:
                t = doArithmetics(Arithmetic.OP_AND,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case ORA:
                t = doArithmetics(Arithmetic.OP_OR,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case XRA:
                t = doArithmetics(Arithmetic.OP_XOR,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P);
                putRegister(REG_A, (byte) t);
                break;
            case CMP:
                doArithmetics(Arithmetic.OP_SUB,
                        getRegister(REG_A), getRegMem(rightPart(code)),
                        FLAG_C | FLAG_Z | FLAG_S | FLAG_P | FLAG_A);
                break;
            case LDA:
                putRegister(REG_A, fetchAt(getLongByPc()));
                break;
            case STA:
                storeAt(getLongByPc(), getRegister(REG_A));
                break;
            case STAX:
                storeBy((short) ((code & 0x10) == 0 ? REG_B : REG_D), getRegister(REG_A));
                break;
            case DAD:
                s = getRegisterLong(REG_H);
                t = getRegisterLong(D_MAP[registerPairPart(code)]);
                p = s + t;
                setFlag(FLAG_C, ((s ^ t ^ (p >> 1)) >>> 16) & 1);
                putRegisterLong(REG_H, (short) p);
                break;
            case STC:
                setFlag(FLAG_C, 1);
                break;
            case CMC:
                setFlag(FLAG_C, getFlag(FLAG_C) ? 0 : 1);
                break;
            case DAA:
                s = getRegister(REG_A);
                if ((s & 0x0F) > 0x09 || getFlag(FLAG_A)) {
                    t = s + 0x06;
                    if (((s ^ t) & 0x10) != 0)
                        setFlag(FLAG_A, 1);
                    s = t;
                }
                if ((s & 0xF0) > 0x90 || getFlag(FLAG_C)) {
                    t = s + 0x60;
                    if (((s ^ t) & 0x100) != 0)
                        setFlag(FLAG_C, 1);
                    s = t;
                }
                putRegister(REG_A, (byte) s);
                break;
            case OUT:
                out(getByPc(), getRegister(REG_A));
                break;
            case IN:
                putRegister(REG_A, in(getByPc()));
                break;
            case RST:
                j = (short) (code & 0x38);
                pushRegisterLong(REG_PC);
                putRegisterLong(REG_PC, j);
                break;
            case DI:
                interruptions = false;
                break;
            case EI:
                interruptions = true;
                break;
            case HLT:
                running = false;
                break;
            default:
                throw new UnsupportedOperationException(
                        inst.toString() + " " + String.format("0x%02X", code) +
                                " after " + count + "\n" + registerString());
        }
        count++;
    }

    public String registerString() {
        String regs = "AF=" + String.format("%04x", getRegisterLong(REG_A)) + " " +
                "BC=" + String.format("%04x", getRegisterLong(REG_B)) + " " +
                "DE=" + String.format("%04x", getRegisterLong(REG_D)) + " " +
                "HL=" + String.format("%04x", getRegisterLong(REG_H)) + " " +
                "PC=" + String.format("%04x", getRegisterLong(REG_PC)) + " " +
                "SP=" + String.format("%04x", getRegisterLong(REG_SP)) + " ";
        char[] flags = "c.p.a.zs".toCharArray();
        short f = getRegister(REG_F);
        for (int i = 0; i < 8; i++)
            if ((f & (1 << i)) != 0)
                flags[i] = Character.toUpperCase(flags[i]);
        return regs + "{" + String.valueOf(flags) + "}" + (interruptions ? " EI" : " DI");
    }
}
