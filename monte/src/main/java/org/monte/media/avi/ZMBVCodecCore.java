
package org.monte.media.avi;


import java.util.zip.InflaterInputStream;
import java.io.IOException;
import org.monte.media.io.AppendableByteArrayInputStream;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.UncachedImageInputStream;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStream;
import static java.lang.Math.*;

public class ZMBVCodecCore {

    public final static int VIDEOMODE_NONE = 0;
    public final static int VIDEOMODE_1_BIT_PALETTIZED = 1;
    public final static int VIDEOMODE_2_BIT_PALETTIZED = 2;
    public final static int VIDEOMODE_4_BIT_PALETTIZED = 3;
    public final static int VIDEOMODE_8_BIT_PALETTIZED = 4;
    public final static int VIDEOMODE_15_BIT_BGR = 5;
    public final static int VIDEOMODE_16_BIT_BGR = 6;
    public final static int VIDEOMODE_24_BIT_BGR = 7;
    public final static int VIDEOMODE_32_BIT_BGR = 8;
    public final static int COMPRESSION_NONE = 0;
    public final static int COMPRESSION_ZLIB = 1;
    private int majorVersion;
    private int minorVersion;
    private int compressionType;
    private int videoFormat;
    private int blockWidth, blockHeight;
    private InflaterInputStream inflaterInputStream;
    private AppendableByteArrayInputStream byteArrayInputStream;
    private int[] palette;
    private byte[] blockDataBuf;
    private byte[] blockHeaderBuf;

    public boolean decode(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.out.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return false;
            }

            if (isKeyframe) {
                // => Key frame
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return isKeyframe;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.out.println(" NO COMPRESSION");
                    return isKeyframe;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return isKeyframe;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    decode8to32(in, outDat, prevDat, flags, width, height);
                    break;

                case VIDEOMODE_15_BIT_BGR:
                    decode15to32(in, outDat, prevDat, flags, width, height);
                    break;
                case VIDEOMODE_16_BIT_BGR:
                    decode16to32(in, outDat, prevDat, flags, width, height);
                    break;
                case VIDEOMODE_32_BIT_BGR:
                    decode32to32(in, outDat, prevDat, flags, width, height);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe;
    }

    public boolean decode(byte[] inDat, int off, int length, byte[] outDat, byte[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.out.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return false;
            }

            if (isKeyframe) {
                // => Key frame
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return isKeyframe;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.out.println(" NO COMPRESSION");
                    return isKeyframe;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return isKeyframe;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    decode8to8(in, outDat, prevDat, flags, width, height);
                    break;
                /*
                case VIDEOMODE_15_BIT_BGR:
                decode15BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                case VIDEOMODE_16_BIT_BGR:
                decode16BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                case VIDEOMODE_32_BIT_BGR:
                decode32BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                 * */
                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe;
    }

    public int decode(byte[] inDat, int off, int length, Object[] outDatHolder, Object[] prevDatHolder, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        int depth = 0;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.err.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return 0;
            }

            if (isKeyframe) {
                // => Key frame
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return 0;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.err.println(" NO COMPRESSION");
                    return 0;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return 0;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    depth = 8;
                    if (!(outDatHolder[0] instanceof byte[])) {
                        outDatHolder[0] = new byte[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof byte[])) {
                        prevDatHolder[0] = new byte[width * height];
                    }
                    decode8to8(in, (byte[]) outDatHolder[0], (byte[]) prevDatHolder[0], flags, width, height);
                    break;

                case VIDEOMODE_15_BIT_BGR:
                    depth = 15;
                    if (!(outDatHolder[0] instanceof short[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof short[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode15to15(in, (short[]) outDatHolder[0], (short[]) prevDatHolder[0], flags, width, height);
                    break;
                case VIDEOMODE_16_BIT_BGR:
                    depth = 16;
                    if (!(outDatHolder[0] instanceof short[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof short[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode16to16(in, (short[]) outDatHolder[0], (short[]) prevDatHolder[0], flags, width, height);
                    break;
                case VIDEOMODE_32_BIT_BGR:
                    depth = 32;
                    if (!(outDatHolder[0] instanceof int[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof int[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode32to32(in, (int[]) outDatHolder[0], (int[]) prevDatHolder[0], flags, width, height);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe ? -depth : depth;
    }

    private void decode8to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;
        boolean isPaletteChange = (flags & 2) != 0;

        // palette each entry contains a 32-bit entry constisting of:
        // {palette index, red, green, blue}.
        if (palette == null) {
            palette = new int[256];
        }
        int blockSize = blockWidth * blockHeight;

        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize)) {
            blockDataBuf = new byte[max(3, blockSize)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Read palette
            for (int i = 0; i < 256; i++) {
                in.readFully(buf, 0, 3);
                palette[i] = ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16) | (i << 24);
            }

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                outDat[i] = palette[in.readUnsignedByte()];
            }

        } else {
            // => Delta frame.

            // Optionally update palette
            if (isPaletteChange) {
                for (int i = 0; i < 256; i++) {
                    in.readFully(buf, 0, 3);
                    palette[i] ^= ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16);
                }
            }

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = palette[0];
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = palette[prevDat[px + py * width] >>> 24];
                                    } else {
                                        outDat[iout++] = palette[0];
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int paletteIndex = buf[iblock++] & 0xff;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    paletteIndex ^= prevDat[px + py * width] >>> 24;
                                }
                                outDat[iout] = palette[paletteIndex];
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode8to8(ImageInputStream in, byte[] outDat, byte[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;
        boolean isPaletteChange = (flags & 2) != 0;

        // palette each entry contains a 32-bit entry constisting of:
        // {palette index, red, green, blue}.
        if (palette == null) {
            palette = new int[256];
        }
        int blockSize = blockWidth * blockHeight;

        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize)) {
            blockDataBuf = new byte[max(3, blockSize)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Read palette
            for (int i = 0; i < 256; i++) {
                in.readFully(buf, 0, 3);
                palette[i] = ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16) | (i << 24);
            }

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                outDat[i] = in.readByte();
            }

        } else {
            // => Delta frame.

            // Optionally update palette
            if (isPaletteChange) {
                for (int i = 0; i < 256; i++) {
                    in.readFully(buf, 0, 3);
                    palette[i] ^= ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16);
                }
            }

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                byte paletteIndex = buf[iblock++];
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    paletteIndex ^= prevDat[px + py * width];
                                }
                                outDat[iout] = paletteIndex;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode15to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = ((bgr & (0x1f << 5)) << 6) | ((bgr & (0x1c << 5)) << 1)//green
                        | ((bgr & (0x1f << 10)) << 9) | ((bgr & (0x1c << 10)) << 4) // red
                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                        ;
            }

        } else {
            // => Delta frame.

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            //System.out.println("blockHeaderSize=" + blockHeaderSize + " blockSize x,y=" + blockWidth + "," + blockHeight);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                int rgb = ((bgr & (0x1f << 5)) << 6) | ((bgr & (0x1c << 5)) << 1)//green
                                        | ((bgr & (0x1f << 10)) << 9) | ((bgr & (0x1c << 10)) << 4) // red
                                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                        ;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void decode15to15(ImageInputStream in, short[] outDat, short[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = (short) bgr;
            }

        } else {
            // => Delta frame.

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            //System.out.println("blockHeaderSize=" + blockHeaderSize + " blockSize x,y=" + blockWidth + "," + blockHeight);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    bgr ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void decode16to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = ((bgr & (0x3f << 5)) << 5) | ((bgr & (0x30 << 5)) >> 1)//green
                        | ((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                        ;
            }

        } else {
            // => Delta frame.

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    outDat[iout] = prevDat[px + py * width];
                                } else {
                                    outDat[iout] = 0;
                                }
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                int rgb = ((bgr & (0x3f << 5)) << 5) | ((bgr & (0x30 << 5)) >> 1)//green
                                        | ((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                        ;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode16to16(ImageInputStream in, short[] outDat, short[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = (short) bgr;
            }

        } else {
            // => Delta frame.

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    outDat[iout] = prevDat[px + py * width];
                                } else {
                                    outDat[iout] = 0;
                                }
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    bgr ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode32to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 4)) {
            blockDataBuf = new byte[max(3, blockSize * 4)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame.

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readInt();
                outDat[i] = bgr;
                /*((bgr & (0x3f << 5))<<5)| ((bgr & (0x30 << 5)) >>1)//green
                |((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                |((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                ; */
            }

        } else {
            // => Delta frame.

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int rgb;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb = prevDat[px + py * width];
                                } else {
                                    rgb = 0;
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 4);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock] & 0xff)) | ((buf[iblock + 1] & 0xff) << 8) | ((buf[iblock + 2] & 0xff) << 16) | ((buf[iblock + 3] & 0xff) << 24);
                                iblock += 4;
                                int rgb = bgr;
                                /*((bgr & (0x3f << 5))<<5)| ((bgr & (0x30 << 5)) >>1)//green
                                |((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                                |((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                ; */
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
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
}
