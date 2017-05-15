package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Cpu8080;

public class VestaHardware {
    VestaMemory vsmem;
    VestaKeyboard keyboard;
    VestaVideo video;
    VestaTape tape;
    Cpu8080 cpu;
    VestaTimer timer;

    public VestaHardware(byte[] rom) {
        vsmem = new VestaMemory(rom);
        cpu = new Cpu8080(vsmem);
        keyboard = new VestaKeyboard();
        video = new VestaVideo(vsmem.getRam(), keyboard);
        timer = new VestaTimer(cpu);


        cpu.setPort(0x80, vsmem.getModePort());
        cpu.setPort(0x81, keyboard.getKeyboardModePort());
        cpu.setPort(0x82, keyboard.getKeyboardPort());
        cpu.setPort(0x84, video.getModePort());
        cpu.setPort(0x88, video.getColorModePort());

        cpu.setPort(0x90, video.getCharacterBufferPort());
        cpu.setPort(0x91, video.getCharacterGeneratorPort());
    }

    public void loadCas(byte[] cas) {
        tape = new VestaTape(cas);
        cpu.setPort(0x8d, tape.getTapePort());
    }

    public void setExtension(int id, byte[] data) {
        vsmem.setExtension(id, data);
    }

    public void run() {
        timer.enable();
        cpu.run((short) 0);
    }
}
