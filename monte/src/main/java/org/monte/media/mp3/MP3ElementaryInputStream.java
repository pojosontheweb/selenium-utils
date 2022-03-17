
package org.monte.media.mp3;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import javax.sound.sampled.AudioFormat;


public class MP3ElementaryInputStream extends FilterInputStream {


    public final static AudioFormat.Encoding MP3 = new AudioFormat.Encoding("MP3");
    private Frame frame;
    private long pos;
    private final static int[][] BIT_RATES = {







        {-1, -1, -1, -1, -1},
        {32, 32, 32, 32, 8},
        {64, 48, 40, 48, 16},
        {96, 56, 48, 56, 24},
        {128, 64, 56, 64, 32},
        {160, 80, 64, 80, 40},
        {192, 96, 80, 96, 48},
        {224, 112, 96, 112, 56},
        {256, 128, 112, 128, 64},
        {288, 160, 128, 144, 80},
        {320, 192, 160, 160, 96},
        {352, 224, 192, 176, 112},
        {384, 256, 224, 192, 128},
        {416, 320, 256, 224, 144},
        {448, 384, 320, 256, 160},
        {-2, -2, -2, -2, -2},
    };
    private final static int[][] SAMPLE_RATES = {





        {44100, 22050, 11025},
        {48000, 24000, 12000},
        {32000, 16000, 8000},
        {-1, -1, -1},
    };


    public static class Frame {


        private int header;

        private int crc;

        private int bodySize;

        private long bodyOffset;


        public Frame(int header) {
            this.header = header;
        }


        public int getHeaderCode() {
            return header;

        }


        public int getVersion() {
            switch (getVersionCode()) {
                case 0:
                    return 25;
                case 2:
                    return 2;
                case 3:
                    return 1;
                default:
                    return -1;
            }
        }


        public int getVersionCode() {
            return (header >>> 19) & 3;
        }


        public int getLayer() {
            switch (getLayerCode()) {
                case 1:
                    return 3;
                case 2:
                    return 2;
                case 3:
                    return 1;
                default:
                    return -1;
            }
        }


        public int getLayerCode() {
            return (header >>> 17) & 3;
        }


        public int getBitRate() {
            if (getVersion() < 0 || getLayer() < 0) {
                return -1;
            }
            int v = getVersion() == 1 ? 0 : 3;
            int l = getVersion() == 1 ? getLayer() - 1 : (getLayer() == 1 ? 0 : 1);
            return BIT_RATES[getBitRateCode()][v + l];
        }


        public int getBitRateCode() {
            return (header >>> 12) & 15;
        }


        public boolean hasCRC() {
            return ((header >>> 16) & 1) == 0;
        }


        public int getCRC() {
            return crc;
        }

        public boolean hasPadding() {
            return ((header >>> 9) & 1) == 1;
        }


        public int getSampleRate() {
            if (getVersion() < 0 || getLayer() < 0) {
                return -1;
            }
            int v = getVersion() == 25 ? 2 : getVersion() - 1;
            return SAMPLE_RATES[getSampleRateCode()][v];
        }


        public int getSampleRateCode() {
            return (header >>> 10) & 3;
        }


        public int getSampleCount() {
            if (getLayer() < 0) {
                return -1;
            }
            return (getLayer() == 1 ? 192 : 576) * getChannelCount();
        }


        public int getChannelCount() {
            return getChannelModeCode() == 3 ? 1 : 2;
        }


        public int getSampleSize() {
            return 16;
        }


        public int getChannelModeCode() {
            return (header >>> 6) & 3;
        }


        public byte[] headerToByteArray() {
            byte[] data = new byte[hasCRC() ? 6 : 4];
            headerToByteArray(data, 0);
            return data;
        }


        public int headerToByteArray(byte[] data, int offset) {
            if (data.length - offset < getHeaderSize()) {
                throw new IllegalArgumentException("data array is too small");
            }
            data[offset + 0] = (byte) (header >>> 24);
            data[offset + 1] = (byte) (header >>> 16);
            data[offset + 2] = (byte) (header >>> 8);
            data[offset + 3] = (byte) (header >>> 0);
            if (hasCRC()) {
                data[offset + 4] = (byte) (crc >>> 8);
                data[offset + 5] = (byte) (crc >>> 0);
            }
            return getHeaderSize();
        }


        public void writeHeader(OutputStream out) throws IOException {
            out.write((header >>> 24));
            out.write((header >>> 16));
            out.write((header >>> 8));
            out.write((header >>> 0));
            if (hasCRC()) {
                out.write((crc >>> 8));
                out.write((crc >>> 0));
            }
        }


        public long getFrameOffset() {
            return getBodyOffset() - getHeaderSize();
        }


        public int getFrameSize() {
            return getHeaderSize() + getBodySize();
        }


        public long getHeaderOffset() {
            return getFrameOffset();
        }


        public int getHeaderSize() {
            return hasCRC() ? 6 : 4;
        }


        public long getSideInfoOffset() {
            return bodyOffset;
        }


        public int getSideInfoSize() {
            return getChannelCount() == 1 ? 17 : 32;
        }


        public long getBodyOffset() {
            return bodyOffset;
        }


        public int getBodySize() {
            return bodySize;
        }


        public int getPaddingSize() {
            if (hasPadding()) {
                return getLayer() == 1 ? 4 : 1;
            }
            return 0;
        }

        private float getFrameRate() {
            return (float) getSampleRate() / getSampleCount();
        }
    }

    public MP3ElementaryInputStream(File file) throws IOException {
        super(new PushbackInputStream(new BufferedInputStream(new FileInputStream(file)), 6));
    }

    public MP3ElementaryInputStream(InputStream in) {
        super(new PushbackInputStream(in, 6));
    }


    public Frame getNextFrame() throws IOException {
        while (frame != null && pos < frame.getBodyOffset() + frame.getBodySize()) {
            long skipped = skip(frame.getBodyOffset() + frame.getBodySize() - pos);
            if (skipped < 0) {
                break;
            }
        }

        while (true) {
            int b = read0();
            if (b == -1) {
                frame = null;
                break;
            } else if (b == 255) {
                int h0 = b;
                int h1 = read0();
                if (h1 != -1 && (h1 & 0xe0) == 0xe0) {
                    int h2 = read0();
                    int h3 = read0();
                    if (h3 != -1) {
                        frame = new Frame((h0 << 24) | (h1 << 16) | (h2 << 8) | h3);
                        if (frame.getBitRate() == -1 || frame.getLayer() == -1 || frame.getSampleRate() == -1) {

                            PushbackInputStream pin = (PushbackInputStream) in;
                            pin.unread(h3);
                            pin.unread(h2);
                            pin.unread(h1);
                            pos -= 3;
                            continue;
                        }

                        int crc0 = -1, crc1 = -1;
                        if (frame.hasCRC()) {
                            crc0 = read0();
                            crc1 = read0();
                            if (crc1 == -1) {
                                throw new EOFException();
                            }
                            frame.crc = (crc0 << 8) | crc1;
                        }
                        frame.bodyOffset = pos;
                        if (frame.getBitRate() <= 0 || frame.getSampleRate() <= 0) {
                            frame.bodySize = 0;
                        } else if (frame.getLayer() == 1) {
                            frame.bodySize = (int) ((12000L * frame.getBitRate() / frame.getSampleRate()) * 4) - frame.getHeaderSize() + frame.getPaddingSize();
                        } else if (frame.getLayer() == 2 || frame.getLayer() == 3) {
                            if (frame.getChannelCount() == 1) {
                                frame.bodySize = (int) (72000L * frame.getBitRate() / (frame.getSampleRate() + frame.getPaddingSize())) - frame.getHeaderSize() + frame.getPaddingSize();
                            } else {
                                frame.bodySize = (int) (144000L * frame.getBitRate() / (frame.getSampleRate() + frame.getPaddingSize())) - frame.getHeaderSize() + frame.getPaddingSize();
                            }
                        }
                        PushbackInputStream pin = (PushbackInputStream) in;
                        if (frame.hasCRC()) {
                            pin.unread(crc1);
                            pin.unread(crc0);
                            pos -= 2;
                        }
                        pin.unread(h3);
                        pin.unread(h2);
                        pin.unread(h1);
                        pin.unread(h0);
                        pos -= 4;
                        assert pos == frame.getFrameOffset() : pos + "!=" + frame.getFrameOffset();
                        break;
                    }
                }
            }
        }
        return frame;
    }


    public Frame getFrame() {
        return frame;
    }


    public AudioFormat getFormat() {
        if (frame == null) {
            return null;
        } else {
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("vbr", true);
            return new AudioFormat(MP3,
                    frame.getSampleRate(), frame.getSampleSize(), frame.getChannelCount(),
                    frame.getFrameSize(), frame.getFrameRate(), true, properties);
        }
    }

    private int read0() throws IOException {
        int b = super.read();
        if (b != -1) {
            pos++;
        }
        return b;
    }


    @Override
    public int read() throws IOException {
        if (frame == null || pos >= frame.getBodyOffset() + frame.getBodySize()) {
            return -1;
        }
        return read0();
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (frame == null) {
            return -1;
        }
        int maxlen = (int) (frame.getBodyOffset() + frame.getBodySize() - pos);
        if (maxlen < 1) {
            return -1;
        }
        len = Math.min(maxlen, len);
        int count = super.read(b, off, len);
        if (count != -1) {
            pos += count;
        }
        return count;
    }


    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }


    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
            pos += count;
        }
    }


    @Override
    public long skip(long n) throws IOException {
        if (frame == null) {
            return -1;
        }
        int maxlen = (int) (frame.getBodyOffset() + frame.getBodySize() - pos);
        if (maxlen < 1) {
            return -1;
        }
        n = Math.min(maxlen, n);
        long skipped = in.skip(n);
        if (skipped > 0) {
            pos += skipped;
        }
        return skipped;
    }


    public long getStreamPosition() {
        return pos;
    }
}
