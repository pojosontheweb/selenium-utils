
package org.monte.media.io;

import java.io.IOException;
import java.nio.ByteOrder;


public class ByteArrayImageInputStream extends ImageInputStreamImpl2 {
    
    protected byte buf[];

    
    protected int count;

    
    private final int arrayOffset;

    public ByteArrayImageInputStream(byte[] buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    public ByteArrayImageInputStream(byte[] buf, ByteOrder byteOrder) {
        this(buf, 0, buf.length, byteOrder);
    }

    public ByteArrayImageInputStream(byte[] buf, int offset, int length, ByteOrder byteOrder) {
	this.buf = buf;
        this.streamPos = offset;
	this.count = Math.min(offset + length, buf.length);
        this.arrayOffset = offset;
        this.byteOrder = byteOrder;
    }

    
    @Override
    public synchronized int read() {
        flushBits();
	return (streamPos < count) ? (buf[(int)(streamPos++)] & 0xff) : -1;
    }

    
    @Override
    public synchronized int read(byte b[], int off, int len) {
        flushBits();
	if (b == null) {
	    throw new NullPointerException();
	} else if (off < 0 || len < 0 || len > b.length - off) {
	    throw new IndexOutOfBoundsException();
	}
	if (streamPos >= count) {
	    return -1;
	}
	if (streamPos + len > count) {
	    len = (int)(count - streamPos);
	}
	if (len <= 0) {
	    return 0;
	}
	System.arraycopy(buf, (int)streamPos, b, off, len);
	streamPos += len;
	return len;
    }

    
    public synchronized long skip(long n) {
	if (streamPos + n > count) {
	    n = count - streamPos;
	}
	if (n < 0) {
	    return 0;
	}
	streamPos += n;
	return n;
    }

    
    public synchronized int available() {
	return (int)(count - streamPos);
    }



    
    @Override
    public void close() {

    }

    @Override
    public long getStreamPosition() throws IOException {
        checkClosed();
        return streamPos-arrayOffset;
    }
    @Override
    public void seek(long pos) throws IOException {
        checkClosed();
        flushBits();


        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }

        this.streamPos = pos+arrayOffset;
    }

    private void flushBits() {
        bitOffset=0;
    }
    @Override
    public long length() {
        return count-arrayOffset;
    }
}
