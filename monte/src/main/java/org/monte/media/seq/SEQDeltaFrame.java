
package org.monte.media.seq;

import org.monte.media.image.BitmapImage;
import java.util.Arrays;


public class SEQDeltaFrame
        extends SEQFrame {

    private int leftBound, topBound, rightBound, bottomBound;
    public final static int
            OP_Copy = 0,
            OP_XOR = 1;
    public final static int
            SM_UNCOMPRESSED = 0,
            SM_COMPRESSED = 1;
    private final static int
            ENCODING_COPY_UNCOMPRESSED = (OP_Copy << 1) | SM_UNCOMPRESSED,
            ENCODING_COPY_COMPRESSED = (OP_Copy << 1) | SM_COMPRESSED,
            ENCODING_XOR_UNCOMPRESSED = (OP_XOR << 1) | SM_UNCOMPRESSED,
            ENCODING_XOR_COMPRESSED = (OP_XOR << 1) | SM_COMPRESSED;
    
    private boolean isWarningPrinted = false;

    public SEQDeltaFrame() {
    }

    private int getEncoding() {
        return (getOperation() << 1) | getStorageMethod();
    }

    @Override
    public void decode(BitmapImage bitmap, SEQMovieTrack track) {
        switch (getEncoding()) {
            case ENCODING_COPY_UNCOMPRESSED:
                decodeCopyUncompressed(bitmap, track);
                break;
            case ENCODING_COPY_COMPRESSED:
                decodeCopyCompressed(bitmap, track);
                break;
            case ENCODING_XOR_UNCOMPRESSED:
                decodeXORUncompressed(bitmap, track);
                break;
            case ENCODING_XOR_COMPRESSED:
                decodeXORCompressed(bitmap, track);
                break;
            default:
                throw new InternalError("Unsupported encoding." + getEncoding());
        }
    }

    private void decodeCopyUncompressed(BitmapImage bitmap, SEQMovieTrack track) {
    }

    
    private void decodeCopyCompressed(BitmapImage bitmap, SEQMovieTrack track) {
        int di = 0;
        byte[] screen = bitmap.getBitmap();
        Arrays.fill(screen, (byte) 0);

        int bStride = bitmap.getBitplaneStride();
        int sStride = bitmap.getScanlineStride();
        int x = leftBound;
        int y = topBound;
        int shift = x & 0x7;
        int b = 0;
        int si = y * sStride + x / 8;
        int width = bitmap.getWidth();

        if (shift == 0) {
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff)));
                if ((op & 0x8000) == 0) {

                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    for (int i = 0; i < op; i++) {
                        screen[si] = d1;
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {

                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        screen[si] = d1;
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        } else {
            int invShift = 8 - shift;
            int mask = (0xff << shift) & 0xff;
            int invMask = (0xff << invShift) & 0xff;
            int xorInvMask = 0xff >>> shift;
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff)));
                if ((op & 0x8000) == 0) {

                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    byte d3 = (byte) (d2 << invShift);
                    d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                    d1 = (byte) ((d1 & 0xff) >>> shift);
                    for (int i = 0; i < op; i++) {
                        screen[si] = (byte) ((screen[si] & invMask) | d1);
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | d3);
                            }
                        }

                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {

                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        byte d3 = (byte) (d2 << invShift);
                        d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                        d1 = (byte) ((d1 & 0xff) >>> shift);
                        screen[si] = (byte) ((screen[si] & invMask) | d1);
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | d3);
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        }
    }

    private void decodeXORUncompressed(BitmapImage bitmap, SEQMovieTrack track) {
    }

    private void decodeXORCompressed(BitmapImage bitmap, SEQMovieTrack track) {
        int di = 0;
        byte[] screen = bitmap.getBitmap();
        int bStride = bitmap.getBitplaneStride();
        int sStride = bitmap.getScanlineStride();
        int x = leftBound;
        int y = topBound;
        int shift = x & 0x7;
        int b = 0;
        int si = y * sStride + x / 8;
        int width = bitmap.getWidth();

        if (shift == 0) {
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff)));
                if ((op & 0x8000) == 0) {

                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    for (int i = 0; i < op; i++) {
                        screen[si] ^= d1;
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {

                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        screen[si] ^= d1;
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        } else {
            int invShift = 8 - shift;
            int mask = (0xff << shift) & 0xff;
            int xorMask = 0xff ^ mask;
            int invMask = (0xff << invShift) & 0xff;
            int xorInvMask = 0xff >>> shift;
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff)));
                if ((op & 0x8000) == 0) {

                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    byte d3 = (byte) (d2 << invShift);
                    d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                    d1 = (byte) ((d1 & 0xff) >>> shift);
                    for (int i = 0; i < op; i++) {
                        screen[si] = (byte) ((screen[si] & invMask) | ((screen[si] & xorInvMask) ^ d1));
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | ((screen[si + 2] & invMask) ^ d3));
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {

                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        byte d3 = (byte) (d2 << invShift);
                        d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                        d1 = (byte) ((d1 & 0xff) >>> shift);

                        screen[si] = (byte) ((screen[si] & invMask) | ((screen[si] & xorInvMask) ^ d1));
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | ((screen[si + 2] & invMask) ^ d3));
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        }
    }

    public void setBounds(int x, int y, int w, int h) {
        leftBound = x;
        topBound = y;
        rightBound = x + w;
        bottomBound = y + h;
    }

    @Override
    public int getTopBound(SEQMovieTrack track) {
        return topBound;
    }

    @Override
    public int getBottomBound(SEQMovieTrack track) {
        return bottomBound;
    }

    @Override
    public int getLeftBound(SEQMovieTrack track) {
        return leftBound;
    }

    @Override
    public int getRightBound(SEQMovieTrack track) {
        return rightBound;
    }

    
    @Override
    public boolean isBidirectional() {
        return getOperation() == OP_XOR;
    }
}
