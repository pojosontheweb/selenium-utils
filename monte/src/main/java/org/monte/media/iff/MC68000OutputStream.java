
package org.monte.media.iff;

import java.io.*;
import java.util.Arrays;


public class MC68000OutputStream extends FilterOutputStream {

    protected long written;


    public MC68000OutputStream(OutputStream out) {
        super(out);
    }

    public void writeLONG(int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(4);
    }

    public void writeULONG(long v) throws IOException {
        out.write((int) ((v >>> 24) & 0xFF));
        out.write((int) ((v >>> 16) & 0xFF));
        out.write((int) ((v >>> 8) & 0xFF));
        out.write((int) ((v >>> 0) & 0xFF));
        incCount(4);
    }

    public void writeWORD(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    public void writeUWORD(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    public void writeUBYTE(int v) throws IOException {
        out.write((v >>> 0) & 0xFF);
        incCount(1);
    }


    public void writeByteRun1(byte[] data) throws IOException {
        writeByteRun1(data, 0, data.length);
    }

    public void writeByteRun1(byte[] data, int offset, int length) throws IOException {
        int end = offset + length;


        int literalOffset = offset;
        int i;
        for (i = offset; i < end; i++) {

            byte b = data[i];


            int repeatCount = i + 1;
            for (; repeatCount < end; repeatCount++) {
                if (data[repeatCount] != b) {
                    break;
                }
            }
            repeatCount = repeatCount - i;

            if (repeatCount == 1) {

                if (i - literalOffset > 127) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                    literalOffset = i;
                }



            } else if (repeatCount == 2
                    && literalOffset < i && i - literalOffset < 127) {
                i++;
            } else {

                if (literalOffset < i) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                }

                i += repeatCount - 1;
                literalOffset = i + 1;


                for (; repeatCount > 128; repeatCount -= 128) {
                    write(-127);
                    write(b);
                }
                write(-repeatCount + 1);
                write(b);
            }
        }


        if (literalOffset < end) {
            write(i - literalOffset - 1);
            write(data, literalOffset, i - literalOffset);
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        incCount(len);
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


    public final long size() {
        return written;
    }


    public void clearCount() {
        written = 0;
    }

    protected void incCount(int value) {
        long temp = written + value;
        if (temp < 0) {
            temp = Long.MAX_VALUE;
        }
        written = temp;
    }
}
