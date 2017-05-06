package emulation.i8080;

import emulation.i8080.cpu.Cpu8080;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Main {
    public static void main(String[] args) {
        String romPath = args[0];
        int startPc = Integer.parseInt(args[1]);
        ByteBuffer rom;
        try {
            RandomAccessFile raf  = new RandomAccessFile(romPath, "r");
            FileChannel ch = raf.getChannel();
            rom = ByteBuffer.allocate((int) ch.size());
            ch.read(rom);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Cpu8080 c = new Cpu8080();
        c.loadRom(rom, startPc);
        c.run((short) startPc);
    }
}
