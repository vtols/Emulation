package emulation.i8080;

import emulation.i8080.cpu.Cpu8080;
import emulation.i8080.hardware.vesta.*;
import emulation.util.FileLoader;

public class Main {
    public static void main(String[] args) {
        String romPath = args[0];
        String casPath = args[1];
        int startPc = Integer.parseInt(args[2]);
        byte[] rom = FileLoader.load(romPath);
        byte[] cas = FileLoader.load(casPath);

        VestaMemory vsmem = new VestaMemory(rom);
        VestaKeyboard kb = new VestaKeyboard();
        VestaVideo video = new VestaVideo(vsmem.getRam(), kb);
        VestaTape tape = new VestaTape(cas);
        Cpu8080 c = new Cpu8080(vsmem);
        VestaTimer tm = new VestaTimer(c);
        c.setPort(0x80, vsmem.getModePort());
        c.setPort(0x81, kb.getKeyboardModePort());
        c.setPort(0x82, kb.getKeyboardPort());
        c.setPort(0x84, video.getModePort());
        c.setPort(0x8d, tape.getTapePort());

        tm.enable();
        c.run((short) startPc);
    }
}
