package emulation.i8080.hardware.vesta;

import java.util.HashMap;
import java.util.Map;

class VestaKeyMap {
    private static Map<Integer, Integer> kmap = new HashMap<>();

    static String[] keyCaptions = {
            "7", "6", "5", "4", "3", "2", "1", "0",
            "?", ";", ":", ".", "-", ",", "9", "8",
            "B", "A", "@", "▵", "~", "}", ":", "{",
            "J", "I", "H", "G", "F", "E", "D", "C",
            "R", "Q", "P", "O", "N", "M", "L", "K",
            "Z", "Y", "X", "W", "V", "U", "T", "S",
            "F3", "F2", "F1", "○", "АЛФ", "ГРАФ", "УПР", "РГ",
            "⏎", "СЕЛ", "ВШ", "СТОП", "↹", "ESC", "F5", "F4",
            "▷", "▽", "△", "◁", "DEL", "INS", "CLS", "␣",
            " ", "⇱", "⇲", "⇒", "⇐", "МЕНЮ", "⇘", "⇖"
    };

    static {
        kmap.put(10, 0x77);
        kmap.put(8, 0x75);
        kmap.put(18, 0x63);
        kmap.put(32, 0x80);

        kmap.put(44, 0x12);
        kmap.put(45, 0x13);
        kmap.put(46, 0x14);

        kmap.put(48, 0x00);
        kmap.put(49, 0x01);
        kmap.put(50, 0x02);
        kmap.put(51, 0x03);
        kmap.put(52, 0x04);
        kmap.put(53, 0x05);
        kmap.put(54, 0x06);
        kmap.put(55, 0x07);
        kmap.put(56, 0x10);
        kmap.put(57, 0x11);

        kmap.put(65, 0x26);
        kmap.put(66, 0x27);
        kmap.put(67, 0x30);
        kmap.put(68, 0x31);
        kmap.put(69, 0x32);
        kmap.put(70, 0x33);
        kmap.put(71, 0x34);
        kmap.put(72, 0x35);
        kmap.put(73, 0x36);
        kmap.put(74, 0x37);
        kmap.put(75, 0x40);
        kmap.put(76, 0x41);
        kmap.put(77, 0x42);
        kmap.put(78, 0x43);
        kmap.put(79, 0x44);
        kmap.put(80, 0x45);
        kmap.put(81, 0x46);
        kmap.put(82, 0x47);
        kmap.put(83, 0x50);
        kmap.put(84, 0x51);
        kmap.put(85, 0x52);
        kmap.put(86, 0x53);
        kmap.put(87, 0x54);
        kmap.put(88, 0x55);
        kmap.put(89, 0x56);
        kmap.put(90, 0x57);

        kmap.put(112, 0x65);
        kmap.put(113, 0x66);
        kmap.put(114, 0x67);
        kmap.put(115, 0x70);
        kmap.put(116, 0x71);

        kmap.put(17, 0x64);
        kmap.put(16, 0x60);

        kmap.put(37, 0x84);
        kmap.put(38, 0x85);
        kmap.put(39, 0x87);
        kmap.put(40, 0x86);

        kmap.put(127, 0x83);
    }

    static int translate(int code) {
        if (kmap.containsKey(code))
            return kmap.get(code);
        return -1;
    }
}
