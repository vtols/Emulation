package emulation.i8080.hardware;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SimpleRam implements Memory {
    private int size;
    private ByteBuffer ram;

    public SimpleRam(int size) {
        this.size = size;
        ram = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public byte read(int address) {
        return ram.get(address);
    }

    @Override
    public short readLong(int address) {
        return ram.getShort(address);
    }

    @Override
    public void write(int address, byte value) {
        ram.put(address, value);
    }

    @Override
    public void writeLong(int address, short value) {
        ram.putShort(value);
    }

    public void loadBuffer(ByteBuffer buffer, int offset) {
        buffer.position(0);
        ram.position(offset);
        ram.put(buffer);
    }
}
