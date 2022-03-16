
package org.monte.media.jpeg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Stack;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;


public class JFIFOutputStream extends OutputStream {


    private final HashSet<Integer> standaloneMarkers = new HashSet<Integer>();

    private final HashSet<Integer> doubleSegMarkers = new HashSet<Integer>();

    public final static int SOI_MARKER = 0xffd8;

    public final static int EOI_MARKER = 0xffd9;

    public final static int TEM_MARKER = 0xff01;

    public final static int SOS_MARKER = 0xffda;

    public final static int APP1_MARKER = 0xffe1;

    public final static int APP2_MARKER = 0xffe2;

    public final static int JPG0_MARKER = 0xfff0;
    public final static int JPG1_MARKER = 0xfff1;
    public final static int JPG2_MARKER = 0xfff2;
    public final static int JPG3_MARKER = 0xfff3;
    public final static int JPG4_MARKER = 0xfff4;
    public final static int JPG5_MARKER = 0xfff5;
    public final static int JPG6_MARKER = 0xfff6;
    public final static int JPG7_MARKER = 0xfff7;
    public final static int JPG8_MARKER = 0xfff8;
    public final static int JPG9_MARKER = 0xfff9;
    public final static int JPGA_MARKER = 0xfffA;
    public final static int JPGB_MARKER = 0xfffB;
    public final static int JPGC_MARKER = 0xfffC;
    public final static int JPGD_MARKER = 0xfffD;

    public final static int SOF0_MARKER = 0xffc0;
    public final static int SOF1_MARKER = 0xffc1;
    public final static int SOF2_MARKER = 0xffc2;
    public final static int SOF3_MARKER = 0xffc3;

    public final static int SOF5_MARKER = 0xffc5;
    public final static int SOF6_MARKER = 0xffc6;
    public final static int SOF7_MARKER = 0xffc7;

    public final static int SOF9_MARKER = 0xffc9;
    public final static int SOFA_MARKER = 0xffcA;
    public final static int SOFB_MARKER = 0xffcB;

    public final static int SOFD_MARKER = 0xffcD;
    public final static int SOFE_MARKER = 0xffcE;
    public final static int SOFF_MARKER = 0xffcF;

    public final static int RST0_MARKER = 0xffd0;
    public final static int RST1_MARKER = 0xffd1;
    public final static int RST2_MARKER = 0xffd2;
    public final static int RST3_MARKER = 0xffd3;
    public final static int RST4_MARKER = 0xffd4;
    public final static int RST5_MARKER = 0xffd5;
    public final static int RST6_MARKER = 0xffd6;
    public final static int RST7_MARKER = 0xffd7;
    private ImageOutputStream out;
    private long streamOffset;
    private Stack<Segment> stack = new Stack<Segment>();

    public JFIFOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        out.setByteOrder(ByteOrder.BIG_ENDIAN);
        streamOffset = out.getStreamPosition();

        for (int i = RST0_MARKER; i <= RST7_MARKER; i++) {
            standaloneMarkers.add(i);
        }
        standaloneMarkers.add(SOI_MARKER);
        standaloneMarkers.add(EOI_MARKER);
        standaloneMarkers.add(TEM_MARKER);
        standaloneMarkers.add(JPG0_MARKER);
        standaloneMarkers.add(JPG1_MARKER);
        standaloneMarkers.add(JPG2_MARKER);
        standaloneMarkers.add(JPG3_MARKER);
        standaloneMarkers.add(JPG4_MARKER);
        standaloneMarkers.add(JPG5_MARKER);
        standaloneMarkers.add(JPG6_MARKER);
        standaloneMarkers.add(JPG7_MARKER);
        standaloneMarkers.add(JPG8_MARKER);
        standaloneMarkers.add(JPG9_MARKER);
        standaloneMarkers.add(JPGA_MARKER);
        standaloneMarkers.add(JPGB_MARKER);
        standaloneMarkers.add(JPGC_MARKER);
        standaloneMarkers.add(JPGD_MARKER);



        standaloneMarkers.add(0);

        doubleSegMarkers.add(SOS_MARKER);

    }

    public JFIFOutputStream(File imgFile) throws IOException {
        this(new FileImageOutputStream(imgFile));
    }


    public long getStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }


    public void seek(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    public void pushSegment(int marker) throws IOException {
        stack.push(new Segment(marker));
    }

    public void popSegment() throws IOException {
        Segment seg = stack.pop();
        seg.finish();
    }


    public long getSegmentOffset() throws IOException {
        if (stack.peek() == null) {
            return -1;
        } else {
            return stack.peek().offset;
        }
    }


    public long getSegmentLength() throws IOException {
        if (stack.peek() == null) {
            return -1;
        } else {
            return getStreamPosition() - stack.peek().offset - 2;
        }
    }

    public void finish() throws IOException {
        while (!stack.empty()) {
            popSegment();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            finish();
        } finally {
            out.close();
        }
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (stack.size() == 0 || standaloneMarkers.contains(stack.peek().marker)) {
            writeStuffed(b, off, len);
        } else {
            writeNonstuffed(b, off, len);
        }
    }


    @Override
    public void write(int b) throws IOException {
        if (stack.size() == 0 || standaloneMarkers.contains(stack.peek().marker)) {
            writeStuffed(b);
        } else {
            writeNonstuffed(b);
        }
    }


    private void writeNonstuffed(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }


    private void writeNonstuffed(int b) throws IOException {
        out.write(b);
    }


    private void writeStuffed(byte[] b, int off, int len) throws IOException {
        int n = off + len;
        for (int i = off; i < n; i++) {
            if (b[i] == -1) {
                out.write(b, off, i - off + 1);
                out.write(0);
                off = i + 1;
            }
        }
        if (n - off > 0) {
            out.write(b, off, n - off);
        }
    }


    private void writeStuffed(int b) throws IOException {
        out.write(0xff);
        if (b == 0xff) {
            out.write(0);
        }
    }


    private class Segment {


        protected int marker;

        protected long offset;
        protected boolean finished;


        public Segment(int marker) throws IOException {
            this.marker = marker;
            if (marker != 0) {
                out.writeShort(marker);
                offset = getStreamPosition();
                if (!standaloneMarkers.contains(marker)) {
                    out.writeShort(0);
                }
            }
        }


        public void finish() throws IOException {
            if (!finished) {
                if (!standaloneMarkers.contains(marker)) {
                    long size = getStreamPosition() - offset;
                    if (size > 0xffffL) {
                        throw new IOException("Segment \"" + marker + "\" is too large: " + size);
                    }

                    long pointer = getStreamPosition();
                    seek(offset);
                    out.writeShort((short) size);

                    seek(pointer);
                }

                finished = true;
            }
        }
    }
}
