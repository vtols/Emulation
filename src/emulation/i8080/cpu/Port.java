package emulation.i8080.cpu;

public interface Port {
    void write(byte value);
    byte read();
}
