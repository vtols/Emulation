package emulation.i8080;

import emulation.i8080.cpu.Cpu8080;
import emulation.i8080.hardware.vesta.VestaMemory;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) {
        String romPath = args[0];
        int startPc = Integer.parseInt(args[1]);
        byte[] rom;
        try {
            RandomAccessFile raf  = new RandomAccessFile(romPath, "r");
            rom = new byte[(int) raf.length()];
            raf.readFully(rom);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        VestaMemory vsmem = new VestaMemory(rom);
        Cpu8080 c = new Cpu8080(vsmem);
        c.setPort(0x80, vsmem.getModePort());
        c.run((short) startPc);
    }
}
