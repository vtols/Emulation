package emulation.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileLoader {

    public static byte[] load(String path) {
        byte[] data;
        try {
            RandomAccessFile raf  = new RandomAccessFile(path, "r");
            data = new byte[(int) raf.length()];
            raf.readFully(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

}
