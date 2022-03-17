
package org.monte.media.quicktime;

import java.io.*;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.imageio.stream.ImageOutputStreamImpl;


public class DataAtomOutputStream extends FilterOutputStream {

    ImageOutputStreamImpl impl;
    protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();

    protected long written;

    public DataAtomOutputStream(OutputStream out) {
        super(out);
    }


    public void writeType(String s) throws IOException {
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            out.write(s.getBytes("ASCII"), 0, 4);
            incCount(4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }


    public final void writeByte(int v) throws IOException {
        out.write(v);
        incCount(1);
    }


    @Override
    public synchronized void write(byte b[], int off, int len)
            throws IOException {
        out.write(b, off, len);
        incCount(len);
    }


    @Override
    public synchronized void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }


    public void writeInt(int v) throws IOException {
        out.write((v >>> 24) & 0xff);
        out.write((v >>> 16) & 0xff);
        out.write((v >>> 8) & 0xff);
        out.write((v >>> 0) & 0xff);
        incCount(4);
    }


    public void writeUInt(long v) throws IOException {
        out.write((int) ((v >>> 24) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 0) & 0xff));
        incCount(4);
    }


    public void writeShort(int v) throws IOException {
        out.write((int) ((v >> 8) & 0xff));
        out.write((int) ((v >>> 0) & 0xff));
        incCount(2);
    }


    public void writeBCD2(int v) throws IOException {
        out.write(((v % 100 / 10) << 4) | (v % 10));
        incCount(1);
    }


    public void writeBCD4(int v) throws IOException {
        out.write(((v % 10000 / 1000) << 4) | (v % 1000 / 100));
        out.write(((v % 100 / 10) << 4) | (v % 10));
        incCount(2);
    }


    public void writeMacTimestamp(Date date) throws IOException {
        long millis = date.getTime();
        long qtMillis = millis - MAC_TIMESTAMP_EPOCH;
        long qtSeconds = qtMillis / 1000;
        writeUInt(qtSeconds);
    }


    public void writeFixed16D16(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) Math.floor(v);
        int fractionPart = (int) ((v - wholePart) * 65536);
        int t = (wholePart << 16) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeInt(t);
    }


    public void writeFixed2D30(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) v;
        int fractionPart = (int) ((v - wholePart) * 1073741824);
        int t = (wholePart << 30) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeInt(t);
    }


    public void writeFixed8D8(double f) throws IOException {
        double v = (f >= 0) ? f : -f;

        int wholePart = (int) v;
        int fractionPart = (int) ((v - wholePart) * 256);
        int t = (wholePart << 8) + fractionPart;

        if (f < 0) {
            t = t - 1;
        }
        writeUShort(t);
    }


    public void writePString(String s) throws IOException {
        if (s.length() > 0xffff) {
            throw new IllegalArgumentException("String too long for PString");
        }
        if (s.length() != 0 && s.length() < 256) {
            out.write(s.length());
        } else {
            out.write(0);
            writeShort(s.length());
        }
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }
        incCount(1 + s.length());
    }


    public void writePString(String s, int length) throws IOException {
        if (s.length() > length) {
            throw new IllegalArgumentException("String too long for PString of length " + length);
        }
        if (s.length() != 0 && s.length() < 256) {
            out.write(s.length());
        } else {
            out.write(0);
            writeShort(s.length());
        }
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }


        for (int i = 1 + s.length(); i < length; i++) {
            out.write(0);
        }

        incCount(length);
    }

    public void writeLong(long v) throws IOException {
        out.write((int) (v >>> 56) & 0xff);
        out.write((int) (v >>> 48) & 0xff);
        out.write((int) (v >>> 40) & 0xff);
        out.write((int) (v >>> 32) & 0xff);
        out.write((int) (v >>> 24) & 0xff);
        out.write((int) (v >>> 16) & 0xff);
        out.write((int) (v >>> 8) & 0xff);
        out.write((int) (v >>> 0) & 0xff);
        incCount(8);
    }

    public void writeUShort(int v) throws IOException {
        out.write((int) ((v >> 8) & 0xff));
        out.write((int) ((v >>> 0) & 0xff));
        incCount(2);
    }


    protected void incCount(int value) {
        long temp = written + value;
        if (temp < 0) {
            temp = Long.MAX_VALUE;
        }
        written = temp;
    }

    public void writeShorts(short[] s, int off, int len) throws IOException {

        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }

        byte[] b = new byte[len * 2];
        int boff = 0;
        for (int i = 0; i < len; i++) {
            short v = s[off + i];
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v >>> 0);
        }

        write(b, 0, len * 2);
    }

    public void writeInts(int[] i, int off, int len) throws IOException {

        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 4];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            b[boff++] = (byte) (v >>> 24);
            b[boff++] = (byte) (v >>> 16);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v >>> 0);
        }

        write(b, 0, len * 4);
    }
    private byte[] byteBuf = new byte[3];

    public void writeInt24(int v) throws IOException {
        byteBuf[0] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[2] = (byte) (v >>> 0);
        write(byteBuf, 0, 3);
    }

    public void writeInts24(int[] i, int off, int len) throws IOException {

        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = new byte[len * 3];
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];

            b[boff++] = (byte) (v >>> 16);
            b[boff++] = (byte) (v >>> 8);
            b[boff++] = (byte) (v >>> 0);
        }

        write(b, 0, len * 3);
    }


    public final long size() {
        return written;
    }
}
