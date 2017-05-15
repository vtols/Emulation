package emulation.i8080;

import emulation.i8080.hardware.vesta.*;
import emulation.util.FileLoader;

public class Main {
    public static void main(String[] args) {
        String romPath = args[0];
        String casPath = args[1];
        String extPath = args[2];
        byte[] rom = FileLoader.load(romPath);
        byte[] cas = FileLoader.load(casPath);
        byte[] ext = FileLoader.load(extPath);

        VestaHardware hardware = new VestaHardware(rom);
        hardware.loadCas(cas);
        hardware.setExtension(1, ext);
        hardware.run();
    }
}
