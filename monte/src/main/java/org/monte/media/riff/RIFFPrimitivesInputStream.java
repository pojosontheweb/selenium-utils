
package org.monte.media.riff;

import java.io.*;


public class RIFFPrimitivesInputStream extends FilterInputStream {
    private long scan, mark;
    
    
    public RIFFPrimitivesInputStream(InputStream in) {
        super(in);
    }
    
    
    public int readUBYTE()
    throws IOException {
        int b0 = in.read();
        
        if (b0 == -1) {
            throw new EOFException();
        }

        scan += 1;
        return b0 & 0xff;
    }
    
    public short readWORD()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        
        if (b1 == -1) {
            throw new EOFException();
        }
        scan += 2;
        
        return (short) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
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
        scan += 4;
        
        return ((b0&0xff) << 0) +
        ((b1&0xff) << 8) +
        ((b2&0xff) << 16) +
        ((b3&0xff) << 24);
    }
    
    
    public int readFourCC()
    throws IOException {
        int b3 = in.read();
        int b2 = in.read();
        int b1 = in.read();
        int b0 = in.read();

        if (b0 == -1) {
            throw new EOFException();
        }
        scan += 4;

        return ((b0&0xff) << 0) +
        ((b1&0xff) << 8) +
        ((b2&0xff) << 16) +
        ((b3&0xff) << 24);
    }
    
    public String readFourCCString()
    throws IOException {
        byte[] buf = new byte[4];
        readFully(buf, 0, 4);

        return new String(buf, "ASCII");
    }
    
    
    public long readULONG()
    throws IOException {
        return (long)(readLONG()) & 0x00ffffffff;
    }
    
    
    public void align()
    throws IOException {
        if (scan % 2 == 1) {
            in.skip(1);
            scan++;
        }
    }
    
    
    public long getScan()
    { return scan; }
    
    
    public int read()
    throws IOException {
        int data = in.read();
        if (data != -1) scan++;
        return data;
    }
    
    public int readFully(byte[] b,int offset, int length)
    throws IOException {
        int count = read(b, offset, length);
        if (count != length) {
            throw new EOFException("readFully for "+length+" bytes, unexpected EOF after "+count+" bytes.");
            }

        return count;
    }
    
    public int read(byte[] b,int offset, int length)
    throws IOException {
        int count = 0;
        while (count < length) {
            int result = in.read(b,offset+count,length-count);
            if (result == -1) break;
            count += result;
        }
        scan += count;
        return count;
    }
    
    public void mark(int readlimit) {
        in.mark(readlimit);
        mark = scan;
    }
    
    public void reset()
    throws IOException {
        in.reset();
        scan = mark;
    }
    
    public long skip(long n)
    throws IOException {
        long skipped = in.skip(n);
        scan += skipped;
        return skipped;
    }
    
    public void skipFully(long n)
    throws IOException {
        if (n==0) return;

        int total = 0;
        int cur = 0;
        
        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        if (cur == 0) throw new EOFException();
        scan += total;
    }
}
