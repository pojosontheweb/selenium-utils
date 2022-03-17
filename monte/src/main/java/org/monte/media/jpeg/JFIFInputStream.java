
package org.monte.media.jpeg;

import java.io.*;
import java.util.*;


public class JFIFInputStream extends FilterInputStream {


    private final HashSet<Integer> standaloneMarkers = new HashSet<Integer>();

    private final HashSet<Integer> doubleSegMarkers = new HashSet<Integer>();


    public static class Segment {


        public final int marker;

        public final long offset;

        public final int length;

        public Segment(int marker, long offset, int length) {
            this.marker = marker;
            this.offset = offset;
            this.length = length;
        }

        public boolean isEntropyCoded() {
            return length == -1;
        }

        @Override
        public String toString() {
            return "Segment marker=0x" + Integer.toHexString(marker) + " offset=" + offset + "=0x" + Long.toHexString(offset);
        }
    }
    private Segment segment;

    private boolean markerFound;
    private int marker = JUNK_MARKER;
    private long offset = 0;
    private boolean isStuffed0xff = false;

    public final static int JUNK_MARKER = -1;

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

    public JFIFInputStream(File f) throws IOException {
       this(new BufferedInputStream(new FileInputStream(f)));
    }

    public JFIFInputStream(InputStream in) {
        super(in);

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
        standaloneMarkers.add(0xffff);
        doubleSegMarkers.add(SOS_MARKER);


        segment = new Segment(-1, 0, -1);
    }


    public Segment getSegment() throws IOException {
        return segment;
    }


    public Segment getNextSegment() throws IOException {


        if (!segment.isEntropyCoded()) {
            markerFound = false;
            do {
                long skipped = in.skip(segment.length - offset + segment.offset);
                if (skipped == -1) {
                    segment = new Segment(0, offset, -1);
                    return null;
                }
                offset += skipped;
            } while (offset < segment.length + segment.offset);

            if (doubleSegMarkers.contains(segment.marker)) {
                segment = new Segment(0, offset, -1);
                return segment;
            }
        }


        while (!markerFound) {
            while (true) {
                int b;
                if (isStuffed0xff) {
                    b = 0xff;
                    isStuffed0xff = false;
                } else {
                    b = read0();
                }
                if (b == -1) {
                    return null;
                }
                if (b == 0xff) {
                    markerFound = true;
                    break;
                }
            }
            int b = read0();
            if (b == -1) {
                return null;
            }
            if (b == 0x00) {
                markerFound = false;
            } else if (b == 0xff) {
                isStuffed0xff = true;
                markerFound = false;
            } else {
                marker = 0xff00 | b;
            }
        }
        markerFound = false;




        if (standaloneMarkers.contains(marker)) {
            segment = new Segment(0xff00 | marker, offset, -1);
        } else {
            int length = (read0() << 8) | read0();
            if (length < 2) {
                throw new IOException("JFIFInputStream found illegal segment length " + length + " after marker " + Integer.toHexString(marker) + " at offset " + offset + ".");
            }
            segment = new Segment(0xff00 | marker, offset, length - 2);
        }
        return segment;
    }

    public long getStreamPosition() {
        return offset;
    }

    private int read0() throws IOException {
        int b = in.read();
        if (b != -1) {
            offset++;
        }
        return b;
    }


    @Override
    public int read() throws IOException {
        if (markerFound) {
            return -1;
        }

        int b;
        if (isStuffed0xff) {
            isStuffed0xff = false;
            b = 0xff;
        } else {
            b = read0();
        }

        if (segment.isEntropyCoded()) {
            if (b == 0xff) {
                b = read0();
                if (b == 0x00) {

                    return 0xff;
                } else if (b == 0xff) {

                    isStuffed0xff = true;
                    return 0xff;
                }
                markerFound = true;
                marker = 0xff00 | b;
                return -1;
            }
        }
        return b;
    }


    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (markerFound) {
            return -1;
        }

        int count = 0;
        if (segment.isEntropyCoded()) {
            for (; count < len; count++) {
                int data = read();
                if (data == -1) {
                    if (count==0) return -1;
                    break;
                }

                b[off + count] = (byte) data;
            }
        } else {
            long available = segment.length - offset + segment.offset;
            if (available <= 0) {
                return -1;
            }
            if (available < len) {
                len = (int) available;
            }
            count = in.read(b, off, len);
            if (count != -1) {
                offset += count;
            }
        }
        return count;
    }


    public final void skipFully(long n) throws IOException {
        long total = 0;
        long cur = 0;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }
        offset+=total;
        if (total < n) {
            throw new EOFException();
        }
    }


    @Override
    public long skip(long n) throws IOException {
        if (markerFound) {
            return -1;
        }

        long count = 0;
        if (segment.isEntropyCoded()) {
            for (; count < n; count++) {
                int data = read();
                if (data == -1) {
                    break;
                }
            }
        } else {
            long available = segment.length - offset + segment.offset;
            if (available < n) {
                n = (int) available;
            }
            count = in.skip(n);
            if (count != -1) {
                offset += count;
            }
        }
        return count;
    }


    @Override
    public synchronized void mark(int readlimit) {

    }


    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("Reset not supported");
    }


    @Override
    public boolean markSupported() {
        return false;
    }
}
