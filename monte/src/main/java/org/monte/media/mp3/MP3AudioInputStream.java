
package org.monte.media.mp3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


public class MP3AudioInputStream extends AudioInputStream {

    private MP3ElementaryInputStream in;


    public MP3AudioInputStream(File file) throws IOException {
        this(new BufferedInputStream(new FileInputStream(file)));
    }


    public MP3AudioInputStream(InputStream in) throws IOException {

        super(null, new AudioFormat(MP3ElementaryInputStream.MP3, 44100, 16, 2, 626, 44100f / 1152f, true), -1);
        this.in = new MP3ElementaryInputStream(in);
        if (this.in.getNextFrame() == null) {
            throw new IOException("Stream is not an MP3 elementary stream");
        }
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }


    @Override
    public AudioFormat getFormat() {
        return in.getFormat();
    }


    @Override
    public long getFrameLength() {
        return -1;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }


    @Override
    public int read() throws IOException {
        throw new IOException("cannot read a single byte if frame size > 1");
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (in.getFrame() == null && in.getNextFrame() == null) {
            return -1;
        }
        if (in.getStreamPosition() != in.getFrame().getFrameOffset()) {
            if (in.getNextFrame() == null) {
                return -1;
            }
        }

        int bytesRead = 0;
        int frameSize = in.getFrame().getFrameSize();
        while (len >= frameSize) {
            in.readFully(b, off, frameSize);
            len -= frameSize;
            bytesRead += frameSize;
            off += frameSize;
            if (in.getNextFrame() == null
                    || frameSize != in.getFrame().getFrameSize()) {
                break;
            }
        }

        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public void mark(int readlimit) {

    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
