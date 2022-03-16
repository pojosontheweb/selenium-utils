
package org.monte.media.iff;

import java.io.*;


public class MC68000InputStream
extends FilterInputStream {
    private long scan_, mark_;


    public MC68000InputStream(InputStream in)
    { super(in); }


    public int readUBYTE()
    throws IOException {
        int b0 = in.read();
        if (b0 == -1) {
            throw new EOFException();
        }
        scan_ += 1;

        return b0 & 0xff;
    }

    public short readWORD()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b1 == -1) {
            throw new EOFException();
        }
        scan_ += 2;

        return (short) (((b0 & 0xff) << 8) | (b1 & 0xff));
    }

    public int readUWORD()
    throws IOException {
        return readWORD() & 0xffff;
    }

    public int readLONG()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b3 == -1) {
            throw new EOFException();
        }
        scan_ += 4;

        return ((b0 & 0xff) << 24)
        | ((b1 & 0xff) << 16)
        | ((b2 & 0xff) << 8)
        | (b3 & 0xff);
    }


    public long readULONG()
    throws IOException {
        return (long)(readLONG()) & 0x00ffffffff;
    }


    public void align()
    throws IOException {
        if (scan_ % 2 == 1) {
            in.skip(1);
            scan_++;
        }
    }


    public long getScan()
    { return scan_; }


    public int read()
    throws IOException {
        int data = in.read();
        scan_++;
        return data;
    }

    public int readFully(byte[] b,int offset, int length)
    throws IOException {
        return read(b, offset, length);
    }

    public int read(byte[] b,int offset, int length)
    throws IOException {
        int count = 0;
        while (count < length) {
            count += in.read(b,offset+count,length-count);
        }
        scan_ += count;
        return count;
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
        mark_ = scan_;
    }

    public void reset()
    throws IOException {
        in.reset();
        scan_ = mark_;
    }

    public long skip(long n)
    throws IOException {
        long skipped = in.skip(n);
        scan_ += skipped;
        return skipped;
    }

    public void skipFully(long n)
    throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        if (cur == 0) throw new EOFException();
        scan_ += total;
    }


    public static int unpackByteRun1(byte[] in, byte[] out)
            throws IOException {
        int iOut = 0;
        int iIn = 0;
        int n = 0;
        byte copyByte;

        try {
            while (iOut < out.length) {
                n = in[iIn++];
                if (n >= 0) {
                    n = n + 1;
                    System.arraycopy(in, iIn, out, iOut, n);
                    iOut += n;
                    iIn += n;
                } else {
                    if (n != -128) {
                        copyByte = in[iIn++];
                        for (; n < 1; n++) {
                            out[iOut++] = copyByte;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("PBMDecoder.unpackByteRun1(): " + e);
            System.out.println("  Plane-Index: " + iOut + " Plane size:" + out.length);
            System.out.println("  Buffer-Index: " + iIn + " Buffer size:" + in.length);
            System.out.println("  Command: " + n);
        }
        return iOut;
    }
}
