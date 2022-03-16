
package org.monte.media.image;

import org.monte.media.ilbm.HAMColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.util.zip.Adler32;
import javax.swing.JFrame;

public class BitmapImage
        implements Cloneable {

    private byte[] bitmap;
    private int width;
    private int height;
    private int depth;
    private int bitplaneStride;
    private int scanlineStride;
    private ColorModel planarColorModel;
    private ColorModel preferredChunkyColorModel_;
    private ColorModel currentChunkyColorModel_;
    private ColorModel lastPixelColorModel_;
    private int pixelType;
    public final static int BYTE_PIXEL = 1;
    public final static int INT_PIXEL = 2;
    public final static int SHORT_PIXEL = 2;
    public final static int NO_PIXEL = 0;
    private byte[] bytePixels;
    private int[] intPixels;
    private short[] shortPixels;
    private boolean enforceDirectColors_ = false;

    public void setEnforceDirectColors(boolean b) {
        enforceDirectColors_ = b;
    }

    public boolean isEnforceDirectColors() {
        return enforceDirectColors_;
    }

    public BitmapImage(int width, int height, int depth, ColorModel colorModel) {
        this(width, height, depth, colorModel, true);
    }

    public BitmapImage(int width, int height, int depth, ColorModel colorModel, boolean isInterleaved) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.planarColorModel = colorModel;
        if (isInterleaved) {
            bitplaneStride = (width + 15) / 16 * 2;
            scanlineStride = bitplaneStride * depth;
            bitmap = new byte[scanlineStride * height];
        } else {
            scanlineStride = (width + 15) / 16 * 2;
            bitplaneStride = scanlineStride * depth;
            bitmap = new byte[bitplaneStride * height];
        }
        pixelType = NO_PIXEL;
    }

    public BitmapImage(int width, int height, int depth, ColorModel colorModel, int bitStride, int scanlineStride) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.planarColorModel = colorModel;
        this.bitplaneStride = bitStride;
        this.scanlineStride = scanlineStride;
        if (bitplaneStride < scanlineStride) {
            bitmap = new byte[scanlineStride * height];
        } else {
            bitmap = new byte[bitplaneStride * height];
        }
        pixelType = NO_PIXEL;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public int getScanlineStride() {
        return scanlineStride;
    }

    public int getBitplaneStride() {
        return bitplaneStride;
    }

    public void setPlanarColorModel(ColorModel colorModel) {
        planarColorModel = colorModel;
    }

    public ColorModel getPlanarColorModel() {
        return planarColorModel;
    }

    public void setPreferredChunkyColorModel(ColorModel colorModel) {
        preferredChunkyColorModel_ = colorModel;
    }

    public ColorModel getChunkyColorModel() {
        if (currentChunkyColorModel_ == null) {
            convertToChunky(0, 0, 0, 0);
        }
        return currentChunkyColorModel_;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public byte[] getBytePixels() {
        if (pixelType == BYTE_PIXEL) {
            return bytePixels;
        } else {
            return null;
        }
    }

    public short[] getShortPixels() {
        if (pixelType == BYTE_PIXEL) {
            return shortPixels;
        } else {
            return null;
        }
    }

    public int[] getIntPixels() {
        if (pixelType == INT_PIXEL) {
            return intPixels;
        } else {
            return null;
        }
    }

    public int getPixelType() {
        return pixelType;
    }

    @Override
    public BitmapImage clone() {
        try {
            BitmapImage theClone = (BitmapImage) super.clone();
            theClone.bitmap = (byte[]) bitmap.clone();
            if (getPixelType() == BYTE_PIXEL) {
                theClone.bytePixels = (byte[]) bytePixels.clone();
            }
            if (getPixelType() == INT_PIXEL) {
                theClone.intPixels = (int[]) intPixels.clone();
            }
            return theClone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public int convertToChunky() {
        return convertToChunky(0, 0, getHeight() - 1, getWidth() - 1);
    }

    public int convertToChunky(int top, int left, int bottom, int right) {
        pixelType = NO_PIXEL;

        /* Ensure pre conditions are met. */
        if (top < 0) {
            top = 0;
        }
        if (left < 0) {
            left = 0;
        }
        if (bottom > getHeight() - 1) {
            bottom = getHeight() - 1;
        }
        if (right > getWidth() - 1) {
            right = getWidth() - 1;
        }

        /* */
        if (planarColorModel instanceof HAMColorModel) {
            if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                bytePixels = null;
                shortPixels = null;
                intPixels = new int[getWidth() * getHeight()];
            }
            currentChunkyColorModel_ = planarColorModel;
            if (((HAMColorModel) planarColorModel).getHAMType() == HAMColorModel.HAM6) {
                ham6PlanesToDirectPixels(top, left, bottom, right);
            } else if (((HAMColorModel) planarColorModel).getHAMType() == HAMColorModel.HAM8) {
                ham8PlanesToDirectPixels(top, left, bottom, right);
            } else {
                throw new InternalError("unsupported ham model:" + planarColorModel);
            }
            pixelType = INT_PIXEL;

        } else {
            if (planarColorModel instanceof IndexColorModel) {
                if (enforceDirectColors_ || preferredChunkyColorModel_ instanceof DirectColorModel) {
                    if (preferredChunkyColorModel_ != null && ((DirectColorModel) preferredChunkyColorModel_).getPixelSize() == 16) {
                        if (shortPixels == null || shortPixels.length != getWidth() * getHeight()) {
                            bytePixels = null;
                            intPixels = null;
                            shortPixels = null;
                            shortPixels = new short[getWidth() * getHeight()];
                        }
                        currentChunkyColorModel_ =
                                (preferredChunkyColorModel_ != null && (preferredChunkyColorModel_ instanceof DirectColorModel))
                                ? preferredChunkyColorModel_
                                : new DirectColorModel(16, 0x7c00, 0x3e0, 0x1f);

                        indexPlanesTo555(top, left, bottom, right);
                        pixelType = SHORT_PIXEL;
                    } else {
                        if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                            bytePixels = null;
                            shortPixels = null;
                            intPixels = new int[getWidth() * getHeight()];
                        }

                        currentChunkyColorModel_ =
                                (preferredChunkyColorModel_ != null && (preferredChunkyColorModel_ instanceof DirectColorModel))
                                ? preferredChunkyColorModel_
                                : ColorModel.getRGBdefault();

                        currentChunkyColorModel_ = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
                        indexPlanesToDirectPixels(top, left, bottom, right);
                        pixelType = INT_PIXEL;
                    }
                } else {
                    if (bytePixels == null || bytePixels.length != getWidth() * getHeight()) {
                        intPixels = null;
                        shortPixels = null;
                        bytePixels = new byte[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    indexPlanesToIndexPixels(top, left, bottom, right);
                    pixelType = BYTE_PIXEL;
                }
            } else if (planarColorModel instanceof DirectColorModel) {
                if (((DirectColorModel) planarColorModel).getPixelSize() == 16) {
                    if (shortPixels == null || shortPixels.length != getWidth() * getHeight()) {
                        bytePixels = null;
                        intPixels = null;
                        shortPixels = null;
                        shortPixels = new short[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    directPlanesTo555(top, left, bottom, right);
                    pixelType = SHORT_PIXEL;
                } else {
                    if (intPixels == null || intPixels.length != getWidth() * getHeight()) {
                        bytePixels = null;
                        shortPixels = null;
                        shortPixels = null;
                        intPixels = new int[getWidth() * getHeight()];
                    }
                    currentChunkyColorModel_ = planarColorModel;
                    directPlanesToDirectPixels(top, left, bottom, right);
                    pixelType = INT_PIXEL;
                }
            } else {
                throw new InternalError("unsupported color model:" + planarColorModel);
            }
        }
        return pixelType;
    }

    public void convertFromChunky(BufferedImage image) {
        /* */
        if (planarColorModel instanceof HAMColorModel) {

            throw new UnsupportedOperationException("HAM mode not implemented:"+ planarColorModel);


        } else {
            if (planarColorModel instanceof IndexColorModel) {
                if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                    planarColorModel=image.getColorModel();
                    Raster raster=image.getRaster();
                    int dx=0,dy=0;
                    while (raster.getParent()!=null) {
                        dx+=raster.getMinX();
                        dy+=raster.getMinY();
                        raster=raster.getParent();
                    }
                   DataBufferByte dbuf= ((DataBufferByte)image.getRaster().getDataBuffer());
                  int inScanlineStride=raster.getWidth();
                    byte[] inb=dbuf.getData();

                    if (bytePixels==null||bytePixels.length!=width*height) {
                        bytePixels=new byte[width*height];
                    }

                    for (int y=0;y<height;y++) {
                        System.arraycopy(inb,dx+(y+dy)*inScanlineStride,bytePixels,y*width,width);
                    }
                    indexPixelsToIndexPlanes(0, 0, getHeight() - 1, getWidth() - 1);
                } else {

                throw new UnsupportedOperationException("index color model not implemented:" + planarColorModel);
                }
            } else if (planarColorModel instanceof DirectColorModel) {
                throw new UnsupportedOperationException("index color model not implemented:" + planarColorModel);
            } else {
                throw new UnsupportedOperationException("unsupported color model:" + planarColorModel);
            }
        }
    }

    public void flushPixels() {
        pixelType = NO_PIXEL;
        intPixels = null;
        shortPixels = null;
        bytePixels = null;
    }

    private void indexPlanesToIndexPixels(int top, int left, int bottom, int right) {

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = depth - 8;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        int iBitmap;
        int iScanline;
        int iDepth;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (((bitmap_[iBitmap] << bitShift) & 128) >>> 7);
                }
                iPixel += pixelLineStride;
                }
                 */
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        bytePixels[iPixel++] = (byte) (((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7);
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x & 7;
                iBitmap = iScanline + x >>> 3;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                );
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x & 7;
                iBitmap = iScanline + x >>> 3;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                );
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                int bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                );
                }
                iPixel += pixelLineStride;
                }*/

                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }
                iPixel=0;
                 */
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                | (bitmap_[iBitmap+bitplaneStride6] & bitMask) << 6
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                bytePixels_[iPixel++] = (byte) (
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                );
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                bytePixels_[iPixel++] = (byte) ((
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                | (bitmap_[iBitmap+bitplaneStride5] & bitMask) << 5
                | (bitmap_[iBitmap+bitplaneStride6] & bitMask) << 6
                | (bitmap_[iBitmap+bitplaneStride7] & bitMask) << 7
                ) >>> (7 - iBit));
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                for (iDepth = depth; iDepth > 0; iDepth--) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                //bytePixels_[iPixel++] = (byte)(pixel >>> bitCorrection);
                bytePixels_[iPixel++] = (byte)(pixel);
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        bytePixels[iPixel++] = (byte) (pixel >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
        }
    }
    private void indexPixelsToIndexPlanes(int top, int left, int bottom, int right) {

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = depth - 8;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        int iBitmap;
        int iScanline;
        int iDepth;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        switch (depth) {
            case 1:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        bytePixels[iPixel++] = (byte) (((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7);
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        bytePixels[iPixel++] = (byte) (((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 6:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        bytePixels[iPixel++] = (byte) (((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        int px=bytePixels[iPixel++];
                        b7=(b7<<1)|((px>>>7)&1);
                        b6=(b6<<1)|((px>>>6)&1);
                        b5=(b5<<1)|((px>>>5)&1);
                        b4=(b4<<1)|((px>>>4)&1);
                        b3=(b3<<1)|((px>>>3)&1);
                        b2=(b2<<1)|((px>>>2)&1);
                        b1=(b1<<1)|((px>>>1)&1);
                        b0=(b0<<1)|((px>>>0)&1);

                        if (iBit == 7) {
                             bitmap[iBitmap]=(byte)b0;
                             bitmap[iBitmap + bitplaneStride]=(byte)b1;
                             bitmap[iBitmap + bitplaneStride2]=(byte)b2;
                             bitmap[iBitmap + bitplaneStride3]=(byte)b3;
                             bitmap[iBitmap + bitplaneStride4]=(byte)b4;
                             bitmap[iBitmap + bitplaneStride5]=(byte)b5;
                             bitmap[iBitmap + bitplaneStride6]=(byte)b6;
                             bitmap[iBitmap + bitplaneStride7]=(byte)b7;
                        }
                    }
                    // FIXME - Add special treatment here when width is not a multiple of 8

                    iPixel += pixelLineStride;
                }
                break;

            default:
                if (true) throw new UnsupportedOperationException(depth +" not yet implemented");
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        bytePixels[iPixel++] = (byte) (pixel >>> (7 - iBit));
                    }
                    iPixel += pixelLineStride;
                }
        }
    }
    private void indexPlanesToDirectPixels(int top, int left, int bottom, int right) {
        IndexColorModel colorModel = (IndexColorModel) planarColorModel;
        final int[] clut = new int[colorModel.getMapSize()];
        //colorModel.getRGBs(clut);
        IndexColorModel icm = (IndexColorModel) planarColorModel;
        byte[] reds = new byte[clut.length];
        byte[] greens = new byte[clut.length];
        byte[] blues = new byte[clut.length];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < clut.length; i++) {
            clut[i] = 0xff000000 | (reds[i] & 0xff) << 16 | (greens[i] & 0xff) << 8 | (blues[i] & 0xff);
        }
        if (clut.length < (1 << getDepth())) {
            throw new IndexOutOfBoundsException("Clut must not be smaller than depth");
        }


        /*
        int transparentPixel = colorModel.getTransparentPixel();
        if (transparentPixel != -1) {
        clut[transparentPixel] &= 0x00ffffff;
        }
        }*/

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;


        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(((bitmap_[iBitmap] << bitShift) & 128) >>> 7)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        intPixels[iPixel++] = clut[(((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        intPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                )];
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                intPixels_[iPixel++] = clut[(
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }

                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        intPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;

                for (iDepth = 0; iDepth < depth; iDepth++) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                intPixels_[iPixel++] = clut[(pixel >>> bitCorrection)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        intPixels[iPixel++] =
                                clut[pixel >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
        }
    }

    private void indexPlanesTo555(int top, int left, int bottom, int right) {
        IndexColorModel colorModel = (IndexColorModel) planarColorModel;
        final short[] clut = new short[colorModel.getMapSize()];
        //colorModel.getRGBs(clut);
        IndexColorModel icm = (IndexColorModel) planarColorModel;
        byte[] reds = new byte[clut.length];
        byte[] greens = new byte[clut.length];
        byte[] blues = new byte[clut.length];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < clut.length; i++) {
            clut[i] = (short) ((reds[i] & 0xf8) << 7 | (greens[i] & 0xf8) << 2 | (blues[i] & 0xf8) >> 3);
        }
        if (clut.length < (1 << getDepth())) {
            throw new IndexOutOfBoundsException("Clut must not be smaller than depth");
        }


        /*
        int transparentPixel = colorModel.getTransparentPixel();
        if (transparentPixel != -1) {
        clut[transparentPixel] &= 0x00ffffff;
        }
        }*/

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        //final int bitCorrection = 8 - depth;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        //int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;


        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position

        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        switch (depth) {
            case 1:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(((bitmap_[iBitmap] << bitShift) & 128) >>> 7)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        shortPixels[iPixel++] = clut[(((bitmap[iScanline + (x >>> 3)] << (x & 7)) & 128) >>> 7)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 2:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 3:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 4:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        shortPixels[iPixel++] = clut[((bitmap[iBitmap] & bitMask)
                                | (bitmap[iBitmap + bitplaneStride1] & bitMask) << 1
                                | (bitmap[iBitmap + bitplaneStride2] & bitMask) << 2
                                | (bitmap[iBitmap + bitplaneStride3] & bitMask) << 3) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 5:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                )];
                }
                iPixel += pixelLineStride;
                }*/
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                intPixels_[iPixel++] = clut[(
                (bitmap_[iBitmap] & bitMask)
                | (bitmap_[iBitmap+bitplaneStride1] & bitMask) << 1
                | (bitmap_[iBitmap+bitplaneStride2] & bitMask) << 2
                | (bitmap_[iBitmap+bitplaneStride3] & bitMask) << 3
                | (bitmap_[iBitmap+bitplaneStride4] & bitMask) << 4
                ) >>> (7 - iBit)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }

                break;

            case 6:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 7:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            case 8:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;
                intPixels_[iPixel++] = clut[(
                ((bitmap_[iBitmap] << bitShift) & 128) >>> 7
                | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 6
                | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 5
                | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 4
                | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 3
                | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 2
                | ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 1
                | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128)
                )];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);

                        if (iBit == 0) {
                            b0 = bitmap[iBitmap];
                            b1 = bitmap[iBitmap + bitplaneStride];
                            b2 = bitmap[iBitmap + bitplaneStride2];
                            b3 = bitmap[iBitmap + bitplaneStride3];
                            b4 = bitmap[iBitmap + bitplaneStride4];
                            b5 = bitmap[iBitmap + bitplaneStride5];
                            b6 = bitmap[iBitmap + bitplaneStride6];
                            b7 = bitmap[iBitmap + bitplaneStride7];
                        }
                        shortPixels[iPixel++] = clut[((b0 & bitMask)
                                | (b1 & bitMask) << 1
                                | (b2 & bitMask) << 2
                                | (b3 & bitMask) << 3
                                | (b4 & bitMask) << 4
                                | (b5 & bitMask) << 5
                                | (b6 & bitMask) << 6
                                | (b7 & bitMask) << 7) >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
                break;

            default:
                /*
                for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
                for (x = left; x < right; x++) {
                bitShift = x % 8;
                iBitmap = iScanline + x / 8;

                for (iDepth = 0; iDepth < depth; iDepth++) {
                pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 128);
                iBitmap += bitplaneStride;
                }
                intPixels_[iPixel++] = clut[(pixel >>> bitCorrection)];
                }
                iPixel += pixelLineStride;
                }*/
                for (iScanline = top * scanlineStride + scanlineStride; iScanline <= bottomScanline; iScanline += scanlineStride) {
                    for (x = left; x < right; x++) {
                        iBit = x & 7;
                        bitMask = 128 >>> (iBit);
                        iBitmap = iScanline + (x >>> 3);
                        pixel = 0;
                        for (iDepth = 0; iDepth < depth; iDepth++) {
                            iBitmap -= bitplaneStride;
                            pixel = (pixel << 1) | bitmap[iBitmap] & bitMask;
                        }
                        shortPixels[iPixel++] =
                                clut[pixel >>> (7 - iBit)];
                    }
                    iPixel += pixelLineStride;
                }
        }
    }

    private void directPlanesToDirectPixels(int top, int left, int bottom, int right) {
        /*
        // This section shows the original algorithm.

        final int depth = getDepth();
        final int width = getWidth();
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int iScanline, x, iBitmap, iDepth, bitShift;
        int pixel = 0;
        int iPixel = top * width + left;

        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride)
        {
        for (x = left; x < right; x++)
        {
        bitShift = x % 8 + 16;
        iBitmap = iScanline + x / 8;
        for (iDepth = depth; iDepth > 0; iDepth--)
        {
        pixel = (pixel >>> 1) | ((bitmap_[iBitmap] << bitShift)  & 0x800000);
        iBitmap += bitplaneStride;
        }
        intPixels_[iPixel++] = 0xff000000 | ((pixel >>> 16) & 0xff) + (pixel & 0xff00) + ((pixel << 16) & 0xff0000);
        }
        iPixel += pixelLineStride;
        }
         */
        /*
        // Eliminating the innermost loop increases the performance
        // by 37 percent.

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;
        final int bitplaneStride8 = bitplaneStride * 8;
        final int bitplaneStride9 = bitplaneStride * 9;
        final int bitplaneStride10 = bitplaneStride * 10;
        final int bitplaneStride11 = bitplaneStride * 11;
        final int bitplaneStride12 = bitplaneStride * 12;
        final int bitplaneStride13 = bitplaneStride * 13;
        final int bitplaneStride14 = bitplaneStride * 14;
        final int bitplaneStride15 = bitplaneStride * 15;
        final int bitplaneStride16 = bitplaneStride * 16;
        final int bitplaneStride17 = bitplaneStride * 17;
        final int bitplaneStride18 = bitplaneStride * 18;
        final int bitplaneStride19 = bitplaneStride * 19;
        final int bitplaneStride20 = bitplaneStride * 20;
        final int bitplaneStride21 = bitplaneStride * 21;
        final int bitplaneStride22 = bitplaneStride * 22;
        final int bitplaneStride23 = bitplaneStride * 23;

        int iBitmap = top * scanlineStride + left / 8;

        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride)
        {
        for (x = left; x < right; x++)
        {
        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        intPixels_[iPixel++] = 0xff000000 |
        ((bitmap_[iBitmap] << bitShift) & 128) << 9 |
        ((bitmap_[iBitmap+bitplaneStride] << bitShift) & 128) << 10 |
        ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) << 11 |
        ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) << 12 |
        ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) << 13 |
        ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) << 14 |
        ((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) << 15 |
        ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128) << 16 |
        ((bitmap_[iBitmap+bitplaneStride8] << bitShift) & 128) << 1 |
        ((bitmap_[iBitmap+bitplaneStride9] << bitShift) & 128) << 2 |
        ((bitmap_[iBitmap+bitplaneStride10] << bitShift) & 128) << 3 |
        ((bitmap_[iBitmap+bitplaneStride11] << bitShift) & 128) << 4 |
        ((bitmap_[iBitmap+bitplaneStride12] << bitShift) & 128) << 5 |
        ((bitmap_[iBitmap+bitplaneStride13] << bitShift) & 128) << 6 |
        ((bitmap_[iBitmap+bitplaneStride14] << bitShift) & 128) << 7 |
        ((bitmap_[iBitmap+bitplaneStride15] << bitShift) & 128) << 8 |
        ((bitmap_[iBitmap+bitplaneStride16] << bitShift) & 128) >>> 7 |
        ((bitmap_[iBitmap+bitplaneStride17] << bitShift) & 128) >>> 6 |
        ((bitmap_[iBitmap+bitplaneStride18] << bitShift) & 128) >>> 5 |
        ((bitmap_[iBitmap+bitplaneStride19] << bitShift) & 128) >>> 4 |
        ((bitmap_[iBitmap+bitplaneStride20] << bitShift) & 128) >>> 3 |
        ((bitmap_[iBitmap+bitplaneStride21] << bitShift) & 128) >>> 2 |
        ((bitmap_[iBitmap+bitplaneStride22] << bitShift) & 128) >>> 1 |
        ((bitmap_[iBitmap+bitplaneStride23] << bitShift) & 128)
        ;
        }
        iPixel += pixelLineStride;
        }
         */

        // Eliminating the innermost loop and avoiding unnecessary
        // array accesses improves performance by 56 percent
        // regarding to the original algorithm.

        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;
        final int bitplaneStride8 = bitplaneStride * 8;
        final int bitplaneStride9 = bitplaneStride * 9;
        final int bitplaneStride10 = bitplaneStride * 10;
        final int bitplaneStride11 = bitplaneStride * 11;
        final int bitplaneStride12 = bitplaneStride * 12;
        final int bitplaneStride13 = bitplaneStride * 13;
        final int bitplaneStride14 = bitplaneStride * 14;
        final int bitplaneStride15 = bitplaneStride * 15;
        final int bitplaneStride16 = bitplaneStride * 16;
        final int bitplaneStride17 = bitplaneStride * 17;
        final int bitplaneStride18 = bitplaneStride * 18;
        final int bitplaneStride19 = bitplaneStride * 19;
        final int bitplaneStride20 = bitplaneStride * 20;
        final int bitplaneStride21 = bitplaneStride * 21;
        final int bitplaneStride22 = bitplaneStride * 22;
        final int bitplaneStride23 = bitplaneStride * 23;

        int iBitmap = top * scanlineStride + left / 8;
        int b0 = bitmap[iBitmap];
        int b1 = bitmap[iBitmap + bitplaneStride];
        int b2 = bitmap[iBitmap + bitplaneStride2];
        int b3 = bitmap[iBitmap + bitplaneStride4];
        int b4 = bitmap[iBitmap + bitplaneStride4];
        int b5 = bitmap[iBitmap + bitplaneStride5];
        int b6 = bitmap[iBitmap + bitplaneStride6];
        int b7 = bitmap[iBitmap + bitplaneStride7];
        int b8 = bitmap[iBitmap + bitplaneStride8];
        int b9 = bitmap[iBitmap + bitplaneStride9];
        int b10 = bitmap[iBitmap + bitplaneStride10];
        int b11 = bitmap[iBitmap + bitplaneStride11];
        int b12 = bitmap[iBitmap + bitplaneStride12];
        int b13 = bitmap[iBitmap + bitplaneStride13];
        int b14 = bitmap[iBitmap + bitplaneStride14];
        int b15 = bitmap[iBitmap + bitplaneStride15];
        int b16 = bitmap[iBitmap + bitplaneStride16];
        int b17 = bitmap[iBitmap + bitplaneStride17];
        int b18 = bitmap[iBitmap + bitplaneStride18];
        int b19 = bitmap[iBitmap + bitplaneStride19];
        int b20 = bitmap[iBitmap + bitplaneStride20];
        int b21 = bitmap[iBitmap + bitplaneStride21];
        int b22 = bitmap[iBitmap + bitplaneStride22];
        int b23 = bitmap[iBitmap + bitplaneStride23];
        /*

        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        for (x = left; x < right; x++) {
        iBitmap = iScanline + x / 8;
        bitShift = x % 8;
        if (bitShift == 0) {
        b0 = bitmap_[iBitmap];
        b1 = bitmap_[iBitmap+bitplaneStride];
        b2 = bitmap_[iBitmap+bitplaneStride2];
        b3 = bitmap_[iBitmap+bitplaneStride3];
        b4 = bitmap_[iBitmap+bitplaneStride4];
        b5 = bitmap_[iBitmap+bitplaneStride5];
        b6 = bitmap_[iBitmap+bitplaneStride6];
        b7 = bitmap_[iBitmap+bitplaneStride7];
        b8 = bitmap_[iBitmap+bitplaneStride8];
        b9 = bitmap_[iBitmap+bitplaneStride9];
        b10 = bitmap_[iBitmap+bitplaneStride10];
        b11 = bitmap_[iBitmap+bitplaneStride11];
        b12 = bitmap_[iBitmap+bitplaneStride12];
        b13 = bitmap_[iBitmap+bitplaneStride13];
        b14 = bitmap_[iBitmap+bitplaneStride14];
        b15 = bitmap_[iBitmap+bitplaneStride15];
        b16 = bitmap_[iBitmap+bitplaneStride16];
        b17 = bitmap_[iBitmap+bitplaneStride17];
        b18 = bitmap_[iBitmap+bitplaneStride18];
        b19 = bitmap_[iBitmap+bitplaneStride19];
        b20 = bitmap_[iBitmap+bitplaneStride20];
        b21 = bitmap_[iBitmap+bitplaneStride21];
        b22 = bitmap_[iBitmap+bitplaneStride22];
        b23 = bitmap_[iBitmap+bitplaneStride23];
        }
        intPixels_[iPixel++] =
        0xff000000
        | ((b0 << bitShift) & 128) << 9
        | ((b1 << bitShift) & 128) << 10
        | ((b2 << bitShift) & 128) << 11
        | ((b3 << bitShift) & 128) << 12
        | ((b4 << bitShift) & 128) << 13
        | ((b5 << bitShift) & 128) << 14
        | ((b6 << bitShift) & 128) << 15
        | ((b7 << bitShift) & 128) << 16
        | ((b8 << bitShift) & 128) << 1
        | ((b9 << bitShift) & 128) << 2
        | ((b10 << bitShift) & 128) << 3
        | ((b11 << bitShift) & 128) << 4
        | ((b12 << bitShift) & 128) << 5
        | ((b13 << bitShift) & 128) << 6
        | ((b14 << bitShift) & 128) << 7
        | ((b15 << bitShift) & 128) << 8
        | ((b16 << bitShift) & 128) >>> 7
        | ((b17 << bitShift) & 128) >>> 6
        | ((b18 << bitShift) & 128) >>> 5
        | ((b19 << bitShift) & 128) >>> 4
        | ((b20 << bitShift) & 128) >>> 3
        | ((b21 << bitShift) & 128) >>> 2
        | ((b22 << bitShift) & 128) >>> 1
        | ((b23 << bitShift) & 128)
        ;
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;*/
        int iBit, bitMask;
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);
                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                    b6 = bitmap[iBitmap + bitplaneStride6];
                    b7 = bitmap[iBitmap + bitplaneStride7];
                    b8 = bitmap[iBitmap + bitplaneStride8];
                    b9 = bitmap[iBitmap + bitplaneStride9];
                    b10 = bitmap[iBitmap + bitplaneStride10];
                    b11 = bitmap[iBitmap + bitplaneStride11];
                    b12 = bitmap[iBitmap + bitplaneStride12];
                    b13 = bitmap[iBitmap + bitplaneStride13];
                    b14 = bitmap[iBitmap + bitplaneStride14];
                    b15 = bitmap[iBitmap + bitplaneStride15];
                    b16 = bitmap[iBitmap + bitplaneStride16];
                    b17 = bitmap[iBitmap + bitplaneStride17];
                    b18 = bitmap[iBitmap + bitplaneStride18];
                    b19 = bitmap[iBitmap + bitplaneStride19];
                    b20 = bitmap[iBitmap + bitplaneStride20];
                    b21 = bitmap[iBitmap + bitplaneStride21];
                    b22 = bitmap[iBitmap + bitplaneStride22];
                    b23 = bitmap[iBitmap + bitplaneStride23];
                }

                intPixels[iPixel++] = ((b0 & bitMask) << 16
                        | (b1 & bitMask) << 17
                        | (b2 & bitMask) << 18
                        | (b3 & bitMask) << 19
                        | (b4 & bitMask) << 20
                        | (b5 & bitMask) << 21
                        | (b6 & bitMask) << 22
                        | (b7 & bitMask) << 23
                        | (b8 & bitMask) << 8
                        | (b9 & bitMask) << 9
                        | (b10 & bitMask) << 10
                        | (b11 & bitMask) << 11
                        | (b12 & bitMask) << 12
                        | (b13 & bitMask) << 13
                        | (b14 & bitMask) << 14
                        | (b15 & bitMask) << 15
                        | (b16 & bitMask)
                        | (b17 & bitMask) << 1
                        | (b18 & bitMask) << 2
                        | (b19 & bitMask) << 3
                        | (b20 & bitMask) << 4
                        | (b21 & bitMask) << 5
                        | (b22 & bitMask) << 6
                        | (b23 & bitMask) << 7) >>> (7 - iBit);

            }
            iPixel += pixelLineStride;
        }
    }

    private void ham6PlanesToDirectPixels(int top, int left, int bottom, int right) {
        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int[] HAMColors = new int[((HAMColorModel) planarColorModel).getMapSize()];
        ((HAMColorModel) planarColorModel).getRGBs(HAMColors);
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int lastPixel, iLastPixel = top * width + left - 1;
        int pixel = 0;
        int bitShift;
        int iScanline;
        int iDepth;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        int iBitmap;
        int b0, b1, b2, b3, b4, b5;
        b0 = b1 = b2 = b3 = b4 = b5 = 0;
        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position
        /*

        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        if (left == 0) {
        lastPixel = 0xff000000;
        } else {
        lastPixel = intPixels_[iLastPixel];
        iLastPixel += width;
        }
        for (x = left; x < right; x++) {

        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        pixel =
        ((bitmap_[iBitmap] << bitShift) & 128) >>> 3
        | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 2
        | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 1
        | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128)
        ;

        switch (((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 7
        | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128) >>> 6) {

        case 0: // use indexed color
        intPixels_[iPixel++] = lastPixel = HAMColors[pixel >>> 4];
        break;

        case 1: // modifie blue
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel >>> 4;
        break;

        case 2:  // modify red
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 12 & 0x000f0000;
        break;

        default: // modify green
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 4 & 0x0f00;
        break;
        }
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;
        iLastPixel = top*width + left - 1;
         */
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            if (left == 0) {
                lastPixel = 0xff000000;
            } else {
                lastPixel = intPixels[iLastPixel];
                iLastPixel += width;
            }
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                }
                pixel = ((b0 & bitMask)
                        | (b1 & bitMask) << 1
                        | (b2 & bitMask) << 2
                        | (b3 & bitMask) << 3) >>> (7 - iBit);

                switch (((b4 & bitMask)
                        | (b5 & bitMask) << 1) >>> (7 - iBit)) {

                    case 0: // use indexed color
                        intPixels[iPixel++] = lastPixel = HAMColors[pixel];
                        break;

                    case 1: // modifie blue
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel << 4;
                        break;

                    case 2:  // modify red
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 20;
                        break;

                    default: // modify green
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 12;
                        break;
                }
            }
            iPixel += pixelLineStride;
        }
    }

    private void ham8PlanesToDirectPixels(int top, int left, int bottom, int right) {
        /* Add one to bottom and right to facilitate computations. */
        bottom++;
        right++;

        final int[] HAMColors = new int[((HAMColorModel) planarColorModel).getMapSize()];
        ((HAMColorModel) planarColorModel).getRGBs(HAMColors);
        final int scanlineStride = getScanlineStride();
        final int bitplaneStride = getBitplaneStride();
        final int depth = getDepth();
        final int width = getWidth();
        final int pixelLineStride = width - right + left;
        final int bottomScanline = bottom * scanlineStride;
        int x;
        int iPixel = top * width + left;
        int lastPixel, iLastPixel = top * width + left - 1;
        int pixel = 0;
        int bitShift;
        //int iBitmap;
        int iScanline;
        int iDepth;
        final int bitplaneStride1 = bitplaneStride;
        final int bitplaneStride2 = bitplaneStride * 2;
        final int bitplaneStride3 = bitplaneStride * 3;
        final int bitplaneStride4 = bitplaneStride * 4;
        final int bitplaneStride5 = bitplaneStride * 5;
        final int bitplaneStride6 = bitplaneStride * 6;
        final int bitplaneStride7 = bitplaneStride * 7;

        int iBitmap = top * scanlineStride + left / 8;
        int b0, b1, b2, b3, b4, b5, b6, b7;
        b0 = b1 = b2 = b3 = b4 = b5 = b6 = b7 = 0;

        int iBit; // the index of the bit inside the byte at the current x-position
        int bitMask; // the mask for the bit inside the byte at the current x-position
        /*

        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
        if (left == 0) {
        lastPixel = 0xff000000;
        } else {
        lastPixel = intPixels_[iLastPixel];
        iLastPixel += width;
        }
        for (x = left; x < right; x++) {
        bitShift = x % 8;
        iBitmap = iScanline + x / 8;
        pixel =
        ((bitmap_[iBitmap] << bitShift) & 128) >>> 5
        | ((bitmap_[iBitmap+bitplaneStride1] << bitShift) & 128) >>> 4
        | ((bitmap_[iBitmap+bitplaneStride2] << bitShift) & 128) >>> 3
        | ((bitmap_[iBitmap+bitplaneStride3] << bitShift) & 128) >>> 2
        | ((bitmap_[iBitmap+bitplaneStride4] << bitShift) & 128) >>> 1
        | ((bitmap_[iBitmap+bitplaneStride5] << bitShift) & 128)
        ;

        switch (((bitmap_[iBitmap+bitplaneStride6] << bitShift) & 128) >>> 7
        | ((bitmap_[iBitmap+bitplaneStride7] << bitShift) & 128) >>> 6) {

        case 0: // use indexed color
        intPixels_[iPixel++] = lastPixel = HAMColors[pixel >>> 2];
        break;

        case 1: // modifie blue
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel | pixel >>> 6;
        break;

        case 2: // modify red
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 16 | pixel << 10 & 0x030000;
        break;

        default: // modify green
        intPixels_[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 8 | pixel << 2 & 0x0300;
        break;

        }
        }
        iPixel += pixelLineStride;
        }
        iPixel = 0;
        iLastPixel = top*width + left - 1;
         */
        for (iScanline = top * scanlineStride; iScanline < bottomScanline; iScanline += scanlineStride) {
            if (left == 0) {
                lastPixel = 0xff000000;
            } else {
                lastPixel = intPixels[iLastPixel];
                iLastPixel += width;
            }
            for (x = left; x < right; x++) {
                iBit = x & 7;
                bitMask = 128 >>> (iBit);
                iBitmap = iScanline + (x >>> 3);

                if (iBit == 0) {
                    b0 = bitmap[iBitmap];
                    b1 = bitmap[iBitmap + bitplaneStride];
                    b2 = bitmap[iBitmap + bitplaneStride2];
                    b3 = bitmap[iBitmap + bitplaneStride3];
                    b4 = bitmap[iBitmap + bitplaneStride4];
                    b5 = bitmap[iBitmap + bitplaneStride5];
                    b6 = bitmap[iBitmap + bitplaneStride6];
                    b7 = bitmap[iBitmap + bitplaneStride7];
                }
                pixel = ((b0 & bitMask)
                        | (b1 & bitMask) << 1
                        | (b2 & bitMask) << 2
                        | (b3 & bitMask) << 3
                        | (b4 & bitMask) << 4
                        | (b5 & bitMask) << 5) >>> (7 - iBit);

                switch (((b6 & bitMask)
                        | (b7 & bitMask) << 1) >>> (7 - iBit)) {

                    case 0: // use indexed color
                        intPixels[iPixel++] = lastPixel = HAMColors[pixel];
                        break;

                    case 1: // modifie blue
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffffff00 | pixel << 2 | pixel >>> 4;
                        break;

                    case 2:  // modify red
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xff00ffff | pixel << 18 | (pixel & 0x03) << 16;
                        break;

                    default: // modify green
                        intPixels[iPixel++] = lastPixel = lastPixel & 0xffff00ff | pixel << 10 | (pixel & 0x03) << 8;
                        break;
                }
            }
            iPixel += pixelLineStride;
        }
    }

    public void setIntPixels(int[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        intPixels = buf;
    }

    public void setBytePixels(byte[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        bytePixels = buf;
    }

    public void setShortPixels(short[] buf) {
        if (buf.length != getWidth() * getHeight()) {
            throw new IllegalArgumentException("Illegal size");
        }
        shortPixels = buf;
    }

    private void directPlanesTo555(int top, int left, int bottom, int right) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
