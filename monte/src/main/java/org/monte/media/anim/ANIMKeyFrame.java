
package org.monte.media.anim;

import org.monte.media.image.BitmapImage;
import org.monte.media.iff.IFFParser;
import org.monte.media.ParseException;


public class ANIMKeyFrame
        extends ANIMFrame {

    private int compression;
    protected final static int VDAT_ID = IFFParser.stringToID("VDAT");

    public ANIMKeyFrame() {
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }


    public void setCompression(int compression) {
        this.compression = compression;
    }

    @Override
    public void decode(BitmapImage bitmap, ANIMMovieTrack track) {
        switch (compression) {

            case ANIMMovieTrack.CMP_BYTE_RUN_1:
                unpackByteRun1(data, bitmap.getBitmap());
                break;
            case ANIMMovieTrack.CMP_VERTICAL:
                unpackVertical(data, bitmap);
                break;
            case ANIMMovieTrack.CMP_NONE:
            default:
                System.arraycopy(data, 0, bitmap.getBitmap(), 0, data.length);
                break;
        }
    }


    public static int unpackByteRun1(byte[] in, byte[] out) {
        int iOut = 0;
        int iIn = 0;
        int n = 0;
        byte copyByte;

        try {
            while (iOut < out.length) {
                n = in[iIn++];
                if (n >= 0) {
                    n = n + 1;
                    System.arraycopy(in, iIn, out, iOut, n);
                    iOut += n;
                    iIn += n;
                } else {
                    if (n != -128) {
                        copyByte = in[iIn++];
                        for (; n < 1; n++) {
                            out[iOut++] = copyByte;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("ANIMKeyFrame.unpackByteRun1(): " + e);
            System.out.println("  Plane-Index: " + iOut + " Plane size:" + out.length);
            System.out.println("  Buffer-Index: " + iIn + " Buffer size:" + in.length);
            System.out.println("  Command: " + n);
        }
        return iOut;
    }

    public void unpackVertical(byte[] in, BitmapImage bm)
             {
        byte[] out = bm.getBitmap();
        int iIn = 0;
        int endOfData = 0;
        int bmhdWidth = bm.getWidth();
        int bmhdHeight = bm.getHeight();
        int bmhdNbPlanes = bm.getDepth();
        byte buf[] = new byte[bmhdWidth * bmhdHeight / 8];
        int scanlineStride = bm.getScanlineStride();
        int columnCount = (bmhdWidth / 8) * bmhdHeight;
        int columnStride = bmhdHeight * 2;


        try {
            for (int p = 0; p < bmhdNbPlanes; p++) {


                int iBuf = 0;
                iIn = endOfData;


                int id = (in[iIn++] & 0xff) << 24 | (in[iIn++] & 0xff) << 16 | (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                if (id != VDAT_ID) {
                    throw new ParseException("Illegal VDAT chunk ID:" + IFFParser.idToString(id) + " at " + (iIn - 4));
                }
                long length = (in[iIn++] & 0xffL) << 24 | (in[iIn++] & 0xffL) << 16 | (in[iIn++] & 0xffL) << 8 | (in[iIn++] & 0xffL);
                if (iIn + length > in.length) {
                    throw new ParseException("Illegal VDAT chunk length:" + length + " at " + (iIn - 4));
                }
                endOfData += length + 8;





                int cnt = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                int iCmd = iIn;
                iIn = iIn + cnt - 2;
                try {

                    for (int i = cnt - 2; i > 0 && iIn < endOfData; i--) {
                        int cmd = in[iCmd++];
                        if (cmd == 0) {


                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            for (n *= 2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        } else if (cmd == 1) {


                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            byte dhigh = in[iIn++];
                            byte dlow = in[iIn++];
                            for (; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else if (cmd >= 2) {

                            byte dhigh = in[iIn++];
                            byte dlow = in[iIn++];
                            for (int n = cmd; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else {

                            for (int n = cmd * -2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("IndexOutOfBounds in bitplane " + p);
                    e.printStackTrace();
                }


                int bitplaneOffset = bm.getBitplaneStride() * p;
                for (int xBuf = 0, xOut = 0; xBuf < columnCount; xBuf += columnStride, xOut += 2) {
                    for (int yBuf = 0, yOut = bitplaneOffset; yBuf < columnStride; yBuf += 2, yOut += scanlineStride) {
                        out[xOut + yOut] = buf[xBuf + yBuf];
                        out[xOut + 1 + yOut] = buf[xBuf + 1 + yBuf];
                    }
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();

        }
    }

}
