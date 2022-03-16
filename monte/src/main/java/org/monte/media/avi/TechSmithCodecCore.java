
package org.monte.media.avi;

import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import org.monte.media.io.UncachedImageInputStream;
import java.io.ByteArrayInputStream;
import org.monte.media.AbstractVideoCodecCore;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import static java.lang.Math.*;

public class TechSmithCodecCore extends AbstractVideoCodecCore {

    private ByteArrayImageOutputStream temp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
    private byte[] temp2;
    private int[] palette;

    public TechSmithCodecCore() {
        reset();
    }

    public void reset() {
        palette = null;
    }

    public int[] getPalette() {
        if (palette == null) {
            palette = new int[256];
            // initalize palette with grayscale colors
            for (int i = 0; i < palette.length; i++) {
                palette[i] = (i) | (i << 8) | (i << 16);
            }
        }
        return palette;
    }

    public void decodePalette(byte[] inDat, int off, int len) throws IOException {
        getPalette();
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inDat, off, len, ByteOrder.LITTLE_ENDIAN);
        int firstEntry = in.readUnsignedByte();
        int numEntries = in.readUnsignedByte();
        if (numEntries == 0) {
            numEntries = 256;
        }
        int flags = in.readUnsignedShort();
        if (firstEntry + numEntries > 256) {
            throw new IOException("Illegal headers in pc chunk. firstEntry=" + firstEntry + ", numEntries=" + numEntries);
        }
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < numEntries; i++) {
            int rgbf = in.readInt();
            palette[i + firstEntry] = rgbf >> 8;
        }
    }

    public boolean decode8(byte[] inDat, int off, int length, byte[] outDat, byte[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: // delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        default: // literal run
                            in.readFully(outDat, xy, opcode);
                            xy += opcode;
                            if ((opcode & 1) == 1) {
                                int pad = in.readByte() & 0xff;
                                if (pad != 0) {
                                    throw new IOException("Illegal pad byte, pad=0x" + Integer.toHexString(pad));
                                }
                            }
                            break;
                    }
                } else {
                    // repetition
                    byte v = in.readByte();
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    public boolean decode8(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }
        if (temp2 == null || temp2.length < 255) {
            temp2 = new byte[255];
        }
        getPalette();

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: { // delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: { // literal run
                            in.readFully(temp2, 0, opcode);
                            for (int i = 0; i < opcode; i++) {
                                outDat[xy + i] = palette[temp2[i] & 0xff];
                            }
                            xy += opcode;
                            if ((opcode & 1) == 1) {
                                int pad = in.readByte() & 0xff;
                                if (pad != 0) {
                                    throw new IOException("Illegal pad byte, pad=0x" + Integer.toHexString(pad));
                                }
                            }
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = palette[in.readUnsignedByte()];
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    public boolean decode24(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: {// delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: {// literal run
                            readInts24LE(in, outDat, xy, opcode);
                            xy += opcode;
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = readInt24LE(in);
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }
    public boolean decode16(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            if (outDat!=prevDat) {
                System.arraycopy(prevDat,0,outDat,0,width*height);
            }
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)), ByteOrder.LITTLE_ENDIAN);

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: {// delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: {// literal run
                            readRGBs555to24(in, outDat, xy, opcode);
                            xy += opcode;
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = readRGB555to24(in);
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    public void encodeDelta8(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }


            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            temp.write(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            temp.write(data, xy - literalCount, literalRun);
                            if ((literalRun & 1) == 1) {
                                temp.write(0); // pad byte
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        temp.write(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    temp.write(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    temp.write(data, xy - literalCount, literalRun);
                    if ((literalRun & 1) == 1) {
                        temp.write(0); // pad byte
                    }
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap


        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    public void encodeDelta8to24(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }


            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            for (int i = xy - literalCount, end = xy - literalCount + literalRun; i < end; i++) {
                                writeInt24LE(temp, palette[data[i]&0xff]);
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        writeInt24LE(temp, palette[v&0xff]);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    writeInt24LE(temp, palette[data[xy - literalCount]]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    for (int i = xy - literalCount, end = xy - literalCount + literalRun; i < end; i++) {
                        writeInt24LE(temp, palette[data[i]&0xff]);
                    }
                    /*
                    temp.write(data, xy - literalCount, literalRun);
                    if (literalRun & 1 == 1) {
                    temp.write(0); // pad byte
                    }*/
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap


        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    public void encodeSameDelta8(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    public void encodeSameDelta24(OutputStream out, int[] data, int[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    public void encodeSameDelta16(OutputStream out, short[] data, short[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    public void encodeKey8(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        temp.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                temp.write(data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            temp.write(data, xy - literalCount, literalCount);
                            if ((literalCount & 1) == 1) {
                                temp.write(0); // pad byte
                            }
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    temp.write(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        temp.write(data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    temp.write(data, xy - literalCount, literalCount);
                    if ((literalCount & 1) == 1) {
                        temp.write(0); // pad byte
                    }
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    public void encodeKey8to24(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        for (int i = xy - literalCount + 1, end = xy + 1; i < end; i++) {
                            writeInt24LE(temp, palette[data[i]&0xff]);
                        }
                        //temp.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            for (int i = xy - literalCount, end = xy; i < end; i++) {
                                writeInt24LE(temp, palette[data[i]&0xff]);
                            }
                            //temp.write(data, xy - literalCount, literalCount);
                            //if ((literalCount & 1) == 1) {
                            //    temp.write(0); // pad byte
                            //}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    writeInt24LE(temp, palette[v&0xff]);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    for (int i = xy - literalCount, end = xy; i < end; i++) {
                        writeInt24LE(temp, palette[data[i]&0xff]);
                    }
                    //temp.write(data, xy - literalCount, literalCount);
                    //if ((literalCount & 1) == 1) {
                    //    temp.write(0); // pad byte
                    //}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    public void encodeDelta16(OutputStream out, short[] data, short[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {


        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                short v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            temp.writeShort(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            temp.writeShorts(data, xy - literalCount, literalRun);
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x02); // Skip OP-code
                            temp.write(min(255, skipCount)); // horizontal skip
                            temp.write(0); // vertical skip
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        temp.writeShort(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    temp.writeShort(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0); // Escape code
                    temp.write(literalRun); // Literal OP-code
                    temp.writeShorts(data, xy - literalCount, literalRun);
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap OP-code

        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    public void encodeKey24(OutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                int v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        writeInts24LE(temp, data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                writeInt24LE(temp, data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            writeInts24LE(temp, data, xy - literalCount, literalCount);
                            ///if (literalCount & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    writeInt24LE(temp, v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        writeInt24LE(temp, data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    writeInts24LE(temp, data, xy - literalCount, literalCount);
                    ///if (literalCount & 1 == 1) {
                    ///    temp.write(0); // pad byte
                    ///}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    public void encodeDelta24(OutputStream out, int[] data, int[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        ScanlineLoop:
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                int v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            writeInt24LE(temp, data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0);
                            temp.write(literalRun); // Literal OP-code
                            writeInts24LE(temp, data, xy - literalCount, literalRun);
                            ///if (literalRun & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0);
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        writeInt24LE(temp, v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    writeInt24LE(temp, data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    writeInts24LE(temp, data, xy - literalCount, literalRun);
                    ///if (literalRun & 1 == 1) {
                    ///   temp.write(0); // pad byte
                    ///}
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap

        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    public void encodeKey16(OutputStream out, short[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                short v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0); // Escape code
                        temp.write(literalCount); // Literal OP-code
                        temp.writeShorts(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                temp.writeShort(data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            temp.writeShorts(data, xy - literalCount, literalCount);
                            ///if (literalCount & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    temp.writeShort(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        temp.writeShort(data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    temp.writeShorts(data, xy - literalCount, literalCount);
                    ///if (literalCount & 1 == 1) {
                    ///    temp.write(0); // pad byte
                    ///}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    public void setPalette(byte[] redValues, byte[] greenValues, byte[] blueValues) {
        if (palette==null)palette=new int[256];
        for (int i=0;i<256;i++) {
            palette[i]=((redValues[i]&0xff)<<16)
                    |((greenValues[i]&0xff)<<8)
                    |((blueValues[i]&0xff)<<0);
        }
    }
}
