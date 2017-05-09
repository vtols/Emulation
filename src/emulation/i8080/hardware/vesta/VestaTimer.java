package emulation.i8080.hardware.vesta;

import emulation.i8080.cpu.Cpu8080;

public class VestaTimer {
    private static final int TIMER_DELAY = 20;

    private Cpu8080 cpu;

    public VestaTimer(Cpu8080 cpu) {
        this.cpu = cpu;
    }

    public void enable() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(TIMER_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // RST 7
                cpu.interrupt((byte) 0xFF);
            }
        }).start();
    }
}
