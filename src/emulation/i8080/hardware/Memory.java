package emulation.i8080.hardware;

public interface Memory {
    int size();

    byte read(int address);
    short readLong(int address);

    void write(int address, byte value);
    void writeLong(int address, short value);
}
