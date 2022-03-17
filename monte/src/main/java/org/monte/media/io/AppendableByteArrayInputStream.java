
package org.monte.media.io;

import java.io.ByteArrayInputStream;
import java.util.zip.Adler32;


public class AppendableByteArrayInputStream extends ByteArrayInputStream {

    public AppendableByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);

    }

    public AppendableByteArrayInputStream(byte[] buf) {
        super(buf);

    }

    @Override
    public synchronized int read() {
        int b = super.read();
        
        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {

        int count = super.read(b, off, len);
        
        return count;
    }

    
    public void appendBuffer(byte[] buf, int offset, int length, boolean discard) {
        if (discard) {
            if (this.buf.length >= count - pos + length) {

                System.arraycopy(this.buf, pos, this.buf, 0, count - pos);
                System.arraycopy(buf, offset, this.buf, count - pos, length);
                this.count = count - pos + length;
                this.pos = 0;
                this.mark = 0;
            } else {

                byte[] newBuf = new byte[(count - pos + length + 31) / 32 * 32];
                System.arraycopy(this.buf, pos, newBuf, 0, count - pos);
                System.arraycopy(buf, offset, newBuf, count - pos, length);
                this.buf = newBuf;
                this.count = count - pos + length;
                this.pos = 0;
                this.mark = 0;
            }
        } else {
            if (this.buf.length >= count + length) {

                System.arraycopy(buf, offset, this.buf, count, length);
                this.count = count + length;
            } else {

                byte[] newBuf = new byte[(this.buf.length + length + 31) / 32 * 32];
                System.arraycopy(this.buf, 0, newBuf, 0, count);
                System.arraycopy(buf, offset, newBuf, count, length);
                this.buf = newBuf;
                this.count = count + length;
            }
        }


    }

    
    public void setBuffer(byte[] buf, int offset, int length) {
        if (this.buf.length >= length) {

            System.arraycopy(buf, offset, this.buf, 0, length);
            this.count = length;
            this.pos = 0;
            this.mark = 0;
        } else {

            this.buf = null;
            this.buf = new byte[(length + 31) & ~31];
            System.arraycopy(buf, offset, this.buf, 0, length);
            this.count = length;
            this.pos = 0;
            this.mark = 0;

        }
    }

    public static void main(String[] args) {
        byte[] b = new byte[5];
        int count = 0;
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        AppendableByteArrayInputStream in = new AppendableByteArrayInputStream(b);

        for (int j = 0; j < 3; j++) {
            System.out.println(in.read());
        }

        b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        in.appendBuffer(b, 0, b.length, true);
        for (int j = 0; j < 3; j++) {
            System.out.println(in.read());
        }
        b = new byte[6];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        in.appendBuffer(b, 0, b.length, true);

        for (int d = in.read(); d >= 0; d = in.read()) {
            System.out.println(d);
        }
    }
}
