
package org.monte.media.tiff;

import org.monte.media.math.Rational;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStream;


public class TIFFInputStream extends InputStream {

    
    private ByteOrder byteOrder;
    
    private long firstIFDOffset;
    
    private ImageInputStream in;

    public TIFFInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        readHeader();
    }

    
    public TIFFInputStream(ImageInputStream in, ByteOrder byteOrder, long firstIFDOffset) {
        this.in = in;
        this.byteOrder = byteOrder;
        this.firstIFDOffset = firstIFDOffset;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder newValue) {
        byteOrder = newValue;
    }

    public long getFirstIFDOffset() {
        return firstIFDOffset;
    }

    
    public IFD readIFD(long offset) throws IOException {
        return readIFD(offset, true, false);
    }

    
    public IFD readIFD(long offset, boolean hasNextOffset, boolean isFirstIFD) throws IOException {
        if ((offset % 1) != 0) {
            throw new IOException("IFD does not start at word boundary");
        }
        if (offset == 0 && !isFirstIFD) {
            return null;
        }
        in.seek(offset);
        int numEntries = readSHORT();
        IFD ifd = new IFD(offset, hasNextOffset);
        for (int i = 0; i < numEntries; i++) {
            long entryOffset = in.getStreamPosition();
            int tag = readSHORT();
            int type = readSHORT();
            long count = readLONG();
            long valueOffset = readSLONG();
            if (count == 0) {
                throw new IOException("IFDEntry " + i + " of " + numEntries + " has count 0 in TIFF stream at offset 0x" + Long.toHexString(offset));

            }
            ifd.add(new IFDEntry(tag, type, count, valueOffset, entryOffset));
        }
        if (hasNextOffset) {
            ifd.setNextOffset(readSLONG());
            if ((ifd.getNextOffset() % 1) != 0) {
                throw new IOException("next IFD does not start at word boundary");
            }
        }

        return ifd;
    }

    
    public String readASCII(long offset, long length) throws IOException {
        in.seek(offset);
        return readASCII(length);
    }

    private String readASCII(long length) throws IOException {
        byte[] buf = new byte[(int) length];
        readFully(buf);
        if (buf[(int) length - 1] != 0) {
            throw new IOException("String does not end with NUL byte.");
        }
        return new String(buf, 0, (int) length - 1, "ASCII");
    }

    private void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);

    }

    private void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException("EOF after " + n + " bytes (needed " + len + " bytes)");
            }
            n += count;
        }
    }

    
    public long readLONG(long offset) throws IOException {
        in.seek(offset);
        return readLONG();
    }

    
    public long[] readLONG(long offset, long count) throws IOException {
        in.seek(offset);
        long[] longs = new long[(int) count];
        for (int i = 0; i < count; i++) {
            longs[i] = readLONG();
        }
        return longs;
    }

    
    public int[] readSHORT(long offset, long count) throws IOException {
        in.seek(offset);
        int[] shorts = new int[(int) count];
        for (int i = 0; i < count; i++) {
            shorts[i] = readSHORT();
        }
        return shorts;
    }

    
    public short[] readSSHORT(long offset, long count) throws IOException {
        in.seek(offset);
        short[] shorts = new short[(int) count];
        for (int i = 0; i < count; i++) {
            shorts[i] = readSSHORT();
        }
        return shorts;
    }

    
    public Rational readRATIONAL(long offset) throws IOException {
        in.seek(offset);
        long num = readLONG();
        long denom = readLONG();
        return new Rational(num, denom);
    }

    
    public Rational readSRATIONAL(long offset) throws IOException {
        in.seek(offset);
        int num = readSLONG();
        int denom = readSLONG();
        return new Rational(num, denom);
    }

    
    public Rational[] readRATIONAL(long offset, long count) throws IOException {
        in.seek(offset);
        Rational[] r = new Rational[(int) count];
        for (int i = 0; i < count; i++) {
            r[i] = new Rational(readLONG(), readLONG());
        }
        return r;
    }

    
    public Rational[] readSRATIONAL(long offset, long count) throws IOException {
        in.seek(offset);
        Rational[] r = new Rational[(int) count];
        for (int i = 0; i < count; i++) {
            r[i] = new Rational(readSLONG(), readSLONG());
        }
        return r;
    }

    
    private short readSSHORT() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b0 == -1 || b1 == -1) {
            throw new EOFException();
        }

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return (short) ((b1 << 8) | b0);
        } else {
            return (short) ((b0 << 8) | b1);
        }
    }

    
    private int readSHORT() throws IOException {
        return readSSHORT() & 0xffff;
    }

    
    private int readSLONG() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b0 == -1 || b1 == -1 || b1 == -1 || b2 == -1) {
            throw new EOFException();
        }

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
        } else {
            return ((b0 << 24) | (b1 << 16) | (b2 << 8) | b3);
        }
    }

    
    private long readLONG() throws IOException {
        return readSLONG() & 0xffffffffL;
    }

    
    private void readHeader() throws IOException {
        in.seek(0);
        byteOrder = ByteOrder.BIG_ENDIAN;
        int byteOrder = readSHORT();
        switch (byteOrder) {
            case 0x4949:
                this.byteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            case 0x4d4d:
                this.byteOrder = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new IOException("Image File Header illegal byte order value 0x" + Integer.toHexString(byteOrder));
        }

        int magic = readSHORT();
        if (magic != 42) {
            throw new IOException("Image File Header illegal magic value 0x" + Integer.toHexString(magic));
        }

        firstIFDOffset = readSLONG();
        if ((firstIFDOffset & 1) == 1) {
            throw new IOException("Image File Header IFD must be on a word boundary 0x" + Long.toHexString(firstIFDOffset));
        }
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(long offset, byte b[], int off, int len) throws IOException {
        in.seek(offset);
        return in.read(b, off, len);
    }
}
