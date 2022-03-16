
package org.monte.media.anim;

import java.util.Map;
import java.util.HashMap;
import org.monte.media.image.BitmapImage;
import org.monte.media.io.SeekableByteArrayOutputStream;
import org.monte.media.iff.IFFOutputStream;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.stream.FileImageOutputStream;
import static java.lang.Math.*;

public class ANIMOutputStream {

    public final static int MONITOR_ID_MASK = 0xffff1000;

    public final static int DEFAULT_MONITOR_ID = 0x00000000;

    public final static int NTSC_MONITOR_ID = 0x00011000;

    public final static int PAL_MONITOR_ID = 0x00021000;

    public final static int MULTISCAN_MONITOR_ID = 0x00031000;

    public final static int A2024_MONITOR_ID = 0x00041000;

    public final static int PROTO_MONITOR_ID = 0x00051000;

    public final static int EURO72_MONITOR_ID = 0x00061000;

    public final static int EURO36_MONITOR_ID = 0x00071000;

    public final static int SUPER72_MONITOR_ID = 0x00081000;

   public final static int DBLNTSC_MONITOR_ID = 0x00091000;

    public final static int DBLPAL_MONITOR_ID = 0x00001000;


    public final static int MODE_MASK = 0x00000880;

    public final static int HAM_MODE = 0x00000800;

    public final static int EHB_MODE = 0x00000080;


    private int jiffies = 60;

    private int camg;
    private boolean debug = false;
    private IFFOutputStream out = null;

    protected int frameCount = 0;

    protected int absTime = 0;

    private BitmapImage oddPrev;

    private BitmapImage evenPrev;

    private BitmapImage firstFrame;

    private int firstWrapupDuration = 1;

    private int secondWrapupDuration = 1;

    private long numberOfFramesOffset = -1;


    private static enum States {

        REALIZED, STARTED, FINISHED, CLOSED;
    }

    private States state = States.REALIZED;

    public ANIMOutputStream(File file) throws IOException {
        out = new IFFOutputStream(new FileImageOutputStream(file));
    }


    public void setJiffies(int newValue) {
        this.jiffies = newValue;
    }


    public int getJiffies() {
        return this.jiffies;
    }


    public void setCAMG(int newValue) {
        this.camg = newValue;
        AmigaDisplayInfo info=AmigaDisplayInfo.getInfo(newValue);
        if (info!=null)this.jiffies=info.fps;
    }


    public int getCAMG() {
        return this.camg;
    }


    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }


    private void ensureStarted() throws IOException {
        ensureOpen();
        if (state == States.FINISHED) {
            throw new IOException("Can not write into finished movie.");
        }
        if (state != States.STARTED) {
            writeProlog();
            state = States.STARTED;
        }
    }


    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            writeEpilog();
            out.finish();
            state = States.FINISHED;
        }
    }


    public void close() throws IOException {
        try {
            if (state == States.STARTED) {
                finish();
            }
        } finally {
            if (state != States.CLOSED) {
                out.close();
                state = States.CLOSED;
            }
        }
    }

    private void writeProlog() throws IOException {
        out.pushCompositeChunk("FORM", "ANIM");
    }

    private void writeEpilog() throws IOException {
        if (frameCount > 0) {

            writeDeltaFrame(firstFrame, firstWrapupDuration);
            writeDeltaFrame(firstFrame, secondWrapupDuration);
        }
        out.popChunk();
        if (numberOfFramesOffset!=-1) {
            long pos=out.getStreamPosition();
            out.seek(numberOfFramesOffset);
            out.writeUWORD(max(0,frameCount-2));
            out.seek(pos);
        }

    }

    public void writeFrame(BitmapImage image, int duration) throws IOException {
        ensureStarted();
        if (frameCount == 0) {
            writeFirstFrame(image, duration);
        } else {
            writeDeltaFrame(image, duration);
        }
    }

    private void writeFirstFrame(BitmapImage img, int duration) throws IOException {

        out.pushCompositeChunk("FORM", "ILBM");
        writeBMHD(out, img);
        writeCMAP(out, img);

        writeANHD(out, img.getWidth(), img.getHeight(), 0, absTime, duration);
        writeCAMG(out, camg);
        writeBODY(out, img);
        out.popChunk();

        firstFrame = new BitmapImage(img.getWidth(), img.getHeight(), img.getDepth(), img.getPlanarColorModel());
        oddPrev = new BitmapImage(img.getWidth(), img.getHeight(), img.getDepth(), img.getPlanarColorModel());
        evenPrev = new BitmapImage(img.getWidth(), img.getHeight(), img.getDepth(), img.getPlanarColorModel());

        System.arraycopy(img.getBitmap(), 0, firstFrame.getBitmap(), 0, img.getBitmap().length);
        System.arraycopy(img.getBitmap(), 0, oddPrev.getBitmap(), 0, img.getBitmap().length);
        System.arraycopy(img.getBitmap(), 0, evenPrev.getBitmap(), 0, img.getBitmap().length);

        absTime += duration;
        firstWrapupDuration = secondWrapupDuration = duration;
        frameCount++;
    }

    private void writeDeltaFrame(BitmapImage img, int duration) throws IOException {
        BitmapImage prev = (frameCount & 1) == 0 ? evenPrev : oddPrev;
        BitmapImage immPrev = (frameCount & 1) == 0 ? oddPrev : evenPrev;

        out.pushCompositeChunk("FORM", "ILBM");
        writeANHD(out, img.getWidth(), img.getHeight(), 0x5, absTime, duration);
        writeCMAP(out, img, immPrev);
        writeDLTA(out, img, prev);
        out.popChunk();

        System.arraycopy(img.getBitmap(), 0, prev.getBitmap(), 0, prev.getBitmap().length);
        prev.setPlanarColorModel(img.getPlanarColorModel());


        absTime += duration;
        firstWrapupDuration = secondWrapupDuration = duration;
        frameCount++;
    }

    public long getMovieTime() {return absTime;}


    private void writeBMHD(IFFOutputStream out, BitmapImage img) throws IOException {
        AmigaDisplayInfo info=AmigaDisplayInfo.getInfo(camg);
        if (info==null)info=AmigaDisplayInfo.getInfo(AmigaDisplayInfo.DEFAULT_MONITOR_ID);

        out.pushDataChunk("BMHD");
        out.writeUWORD(img.getWidth());
        out.writeUWORD(img.getHeight());
        out.writeWORD(0);
        out.writeWORD(0);
        out.writeUBYTE(img.getDepth());
        out.writeUBYTE(0);
        out.writeUBYTE(1);
        out.writeUBYTE(0);
        out.writeUWORD(0);
        out.writeUBYTE(info.resolutionX);
        out.writeUBYTE(info.resolutionY);
        out.writeUWORD(img.getWidth());
        out.writeUWORD(img.getHeight());
        out.popChunk();
    }


    private void writeCMAP(IFFOutputStream out, BitmapImage img) throws IOException {
        out.pushDataChunk("CMAP");

        IndexColorModel cm = (IndexColorModel) img.getPlanarColorModel();
        for (int i = 0, n = cm.getMapSize(); i < n; ++i) {
            out.writeUBYTE(cm.getRed(i));
            out.writeUBYTE(cm.getGreen(i));
            out.writeUBYTE(cm.getBlue(i));
        }

        out.popChunk();
    }


    private void writeCMAP(IFFOutputStream out, BitmapImage img, BitmapImage prev) throws IOException {
        IndexColorModel cm = (IndexColorModel) img.getPlanarColorModel();
        IndexColorModel prevCm = (IndexColorModel) prev.getPlanarColorModel();

        boolean equals = true;
        for (int i = 0, n = cm.getMapSize(); i < n; ++i) {
            if (cm.getRGB(i) != prevCm.getRGB(i)) {
                equals = false;
                break;
            }
        }
        if (!equals) {
            writeCMAP(out, img);
        }
    }


    private void writeCAMG(IFFOutputStream out, int camg) throws IOException {
        out.pushDataChunk("CAMG");
        out.writeLONG(camg);
        out.popChunk();
    }


    private void writeDPAN(IFFOutputStream out) throws IOException {
        out.pushDataChunk("DPAN");
        out.writeUWORD(4);
        numberOfFramesOffset = out.getStreamPosition();
        out.writeUWORD(-1);
        out.writeULONG(0);
        out.popChunk();
    }


    private void writeBODY(IFFOutputStream out, BitmapImage img) throws IOException {
        out.pushDataChunk("BODY");
        int widthInBytes = (img.getWidth() + 7) / 8;
        int ss = img.getScanlineStride();
        int bs = img.getBitplaneStride();
        int offset = 0;
        byte[] data = img.getBitmap();

        for (int y = 0, h = img.getHeight(); y < h; y++) {
            for (int p = 0, d = img.getDepth(); p < d; p++) {
                out.writeByteRun1(data, offset + bs * p, widthInBytes);
            }
            offset += ss;
        }
        out.popChunk();
    }


    private void writeDLTA(IFFOutputStream out, BitmapImage img, BitmapImage prev) throws IOException {
        out.pushDataChunk("DLTA");

        int height = img.getHeight();
        int widthInBytes = (img.getWidth() + 7) / 8;
        int ss = img.getScanlineStride();
        int bs = img.getBitplaneStride();
        int offset = 0;
        byte[] data = img.getBitmap();

        byte[] prevData = prev.getBitmap();
        SeekableByteArrayOutputStream buf = new SeekableByteArrayOutputStream();


        byte[][] planes = new byte[16][0];


        int depth = img.getDepth();

        for (int p = 0; p < depth; ++p) {
            buf.reset();


            for (int column = 0; column < widthInBytes; ++column) {
                writeByteVertical(buf, data, prevData, bs * p + column, height, ss);
            }

            planes[p] = buf.toByteArray();

            if (planes[p].length == widthInBytes) {

                planes[p] = new byte[0];
            }
        }


        int[] pPointers = new int[16];



        for (int p = 0; p < depth; ++p) {
            if (planes[p].length == 0) {
                pPointers[p] = 0;
            } else {
                pPointers[p] = 16 * 4;
                for (int q = 0; q < p; ++q) {
                    if (Arrays.equals(planes[q], planes[p])) {
                        pPointers[p] = pPointers[q];
                        planes[p] = new byte[0];
                        break;
                    }
                    pPointers[p] += planes[q].length;
                }
            }
        }



        for (int p = 0; p < pPointers.length; ++p) {
            out.writeULONG(pPointers[p]);
        }

        for (int p = 0; p
                < planes.length;
                ++p) {
            out.write(planes[p]);
        }
        out.popChunk();
    }


    private void writeByteVertical(SeekableByteArrayOutputStream out, byte[] data, byte[] prev, int offset, int length, int step) throws IOException {
        int opCount = 0;


        long opCountPos = out.getStreamPosition();
        out.write(0);


        int literalOffset = 0;

        int i;
        for (i = 0; i < length; i++) {

            int skipCount = i;

            for (; skipCount < length; skipCount++) {
                if (data[offset + skipCount * step] != prev[offset + skipCount * step]) {
                    break;
                }
            }
            skipCount = skipCount - i;


            if (skipCount + i == length) {
                break;
            }

            if (skipCount > 0 && literalOffset == i
                    || skipCount > 1) {

                if (literalOffset < i) {
                    opCount++;
                    out.write(0x80 | (i - literalOffset));
                    for (int j = literalOffset; j < i; j++) {
                        out.write(data[offset + j * step]);
                    }
                }


                i += skipCount - 1;
                literalOffset = i + 1;
                for (; skipCount > 127; skipCount -= 127) {
                    opCount++;
                    out.write(127);
                }
                opCount++;
                out.write(skipCount);
            } else {

                byte b = data[offset + i * step];


                int repeatCount = i + 1;
                for (; repeatCount < length; repeatCount++) {
                    if (data[offset + repeatCount * step] != b) {
                        break;
                    }
                }
                repeatCount = repeatCount - i;

                if (repeatCount == 1) {

                    if (i - literalOffset > 126) {
                        opCount++;
                        out.write(0x80 | (i - literalOffset));
                        for (int j = literalOffset; j < i; j++) {
                            out.write(data[offset + j * step]);
                        }
                        literalOffset = i;
                    }

                } else if (repeatCount < 4
                        && literalOffset < i && i - literalOffset < 126) {
                    i++;
                } else {

                    if (literalOffset < i) {
                        opCount++;
                        out.write(0x80 | (i - literalOffset));
                        for (int j = literalOffset; j < i; j++) {
                            out.write(data[offset + j * step]);
                        }
                    }

                    i += repeatCount - 1;
                    literalOffset = i + 1;


                    for (; repeatCount
                            > 255; repeatCount -= 255) {
                        opCount++;
                        out.write(0);
                        out.write(255);
                        out.write(b);
                    }
                    opCount++;
                    out.write(0);
                    out.write(repeatCount);
                    out.write(b);
                }
            }
        }


        if (literalOffset < i) {
            opCount++;
            out.write(0x80 | (i - literalOffset));
            for (int j = literalOffset; j < i; j++) {
                out.write(data[offset + j * step]);
            }
        }


        long pos = out.getStreamPosition();
        out.seek(opCountPos);
        out.write(opCount);
        out.seek(pos);
    }

    private void writeANHD(IFFOutputStream out, int width, int height, int compressionMode, int absTime, int relTime) throws IOException {
        out.pushDataChunk("ANHD");

        out.writeUBYTE(compressionMode);
        out.writeUBYTE(0);
        out.writeUWORD(width);
        out.writeUWORD(height);
        out.writeUWORD(0);
        out.writeUWORD(0);
        out.writeULONG(absTime);
        out.writeULONG(relTime);
        out.writeUBYTE(0);
        out.writeUBYTE(0);
        out.writeULONG(0); // bits
        out.writeULONG(0); // pad
        out.writeULONG(0);
        out.writeULONG(0);
        out.writeULONG(0);

        out.popChunk();
    }
}
