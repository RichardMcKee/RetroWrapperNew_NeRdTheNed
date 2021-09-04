package com.zero.retrowrapper.emulator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ByteUtils {
    private ByteUtils() {
        // As this is a helper class, there should be no reason to instantiate an instance of it.
    }

    public static String readString(DataInputStream dis) throws IOException {
        final int len = dis.readUnsignedShort();
        System.out.println(len);
        final byte[] bytes = new byte[len];
        dis.read(bytes);
        return new String(bytes);
    }

    public static byte[] readFully(InputStream is) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];
        int read = 0;
        int buffered = 0;

        while ((read = is.read(buffer)) > -1) {
            bos.write(buffer, 0, read);
            buffered += read;

            if (buffered > (1024 * 1024)) {
                bos.flush();
                buffered = 0;
            }
        }

        return bos.toByteArray();
    }

    public static String readLine(InputStream dis) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true) {
            final int b = dis.read();

            if (b == 0x0a) {
                break;
            }

            bos.write(b);
        }

        return new String(bos.toByteArray());
    }
}
