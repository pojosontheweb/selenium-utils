

package org.monte.media.io;

import java.io.FilterInputStream;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;


public class ImageInputStreamAdapter extends FilterInputStream {
    private ImageInputStream iis;
    public ImageInputStreamAdapter(ImageInputStream iis) {
        super(null);
        this.iis=iis;
    }


    @Override
    public int read() throws IOException {
	return iis.read();
    }


    @Override
    public int read(byte b[], int off, int len) throws IOException {
	return iis.read(b, off, len);
    }


    @Override
    public long skip(long n) throws IOException {
	return iis.skipBytes(n);
    }


    @Override
    public int available() throws IOException {
	return  (iis.isCached()) ?
            (int)Math.min(Integer.MAX_VALUE, iis.length() - iis.getStreamPosition()) :
            0;
    }


    @Override
    public void close() throws IOException {
	iis.close();
    }


    @Override
    public synchronized void mark(int readlimit) {
	iis.mark();
    }


    @Override
    public synchronized void reset() throws IOException {
	iis.reset();
    }


    @Override
    public boolean markSupported() {
	return true;
    }

}
