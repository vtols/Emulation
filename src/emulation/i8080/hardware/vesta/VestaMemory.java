package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Port;
import emulation.i8080.hardware.Memory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class VestaMemory implements Memory {
    private static final int MODE_ROM = 0;
    private static final int MODE_EXT1 = 1;
    private static final int MODE_EXT2 = 2;
    private static final int MODE_RAM = 3;

    private static final int MODE_COUNT = 4;
    private static final int RAM_SIZE = 0x10000;

    /* First two bits */
    private static final int REGION_SHIFT = 14;
    private static final int REGION_MASK = 0xC000;

    private int[] modes = new int[MODE_COUNT];
    private byte[] ram = new byte[RAM_SIZE];
    private byte[] rom;

    public VestaMemory(byte[] rom) {
        this.rom = rom;
    }

    @Override
    public int size() {
        return RAM_SIZE;
    }

    @Override
    public byte read(int address) {
        int readMode = modes[(address & REGION_MASK) >> REGION_SHIFT];
        switch (readMode) {
            case MODE_RAM:
                return ram[address];
            case MODE_ROM:
                return rom[address & ~REGION_MASK];
            default:
                return 0;
        }
    }

    @Override
    public short readLong(int address) {
        int lo = read(address) & 0xFF;
        int hi = read(address + 1) & 0xFF;
        return (short) ((hi << 8) | lo);
    }

    @Override
    public void write(int address, byte value) {
        int writeMode = modes[(address & REGION_MASK) >> REGION_SHIFT];
        switch (writeMode) {
            case MODE_RAM:
            case MODE_ROM:
                ram[address] = value;
                break;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void writeLong(int address, short value) {
        byte lo = (byte) (value & 0xFF);
        byte hi = (byte) ((value >> 8) & 0xFF);
        write(address, lo);
        write(address + 1, hi);
    }

    public Port getModePort() {
        return new MemoryModePort();
    }

    private class MemoryModePort implements Port {
        @Override
        public void write(byte value) {
            for (int i = 0; i < MODE_COUNT; i++) {
                modes[i] = (value >> (i * 2)) & 0x03;
            }
        }

        @Override
        public byte read() {
            int value = 0;
            for (int i = 0; i < MODE_COUNT; i++) {
                value |= modes[i] << (i * 2);
            }
            return (byte) value;
        }
    }

    public byte[] getRam() {
        return ram;
    }
}
