
package org.monte.media.jpeg;

import org.monte.media.io.ByteArrayImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;


public class JPEGImageIO {


    private JPEGImageIO() {
    }


    public static BufferedImage read(InputStream in) throws IOException {
        return read(in, true);
    }
    public static BufferedImage read(InputStream in, boolean inverseYCCKColors) throws IOException {




        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] b = new byte[512];
        for (int count = in.read(b); count != -1; count = in.read(b)) {
            buf.write(b, 0, count);
        }
        byte[] byteArray = buf.toByteArray();




        int samplePrecision = 0;
        int numberOfLines = 0;
        int numberOfSamplesPerLine = 0;
        int numberOfComponentsInFrame = 0;
        int app14AdobeColorTransform = 0;
        ByteArrayOutputStream app2ICCProfile = new ByteArrayOutputStream();



        JFIFInputStream fifi = new JFIFInputStream(new ByteArrayInputStream(byteArray));
        for (JFIFInputStream.Segment seg = fifi.getNextSegment(); seg != null; seg = fifi.getNextSegment()) {
            if (0xffc0 <= seg.marker && seg.marker <= 0xffc3 ||
                    0xffc5 <= seg.marker && seg.marker <= 0xffc7 ||
                    0xffc9 <= seg.marker && seg.marker <= 0xffcb ||
                    0xffcd <= seg.marker && seg.marker <= 0xffcf) {

                DataInputStream dis = new DataInputStream(fifi);
                samplePrecision = dis.readUnsignedByte();
                numberOfLines = dis.readUnsignedShort();
                numberOfSamplesPerLine = dis.readUnsignedShort();
                numberOfComponentsInFrame = dis.readUnsignedByte();




                break;

            } else if (seg.marker == 0xffe2) {

                if (seg.length >= 26) {
                    DataInputStream dis = new DataInputStream(fifi);

                    if (dis.readLong() == 0x4943435f50524f46L && dis.readInt() == 0x494c4500) {

                        dis.skipBytes(2);



                        for (int count = dis.read(b); count != -1; count = dis.read(b)) {
                            app2ICCProfile.write(b, 0, count);
                        }
                    }
                }
            } else if (seg.marker == 0xffee) {

                if (seg.length == 12) {
                    DataInputStream dis = new DataInputStream(fifi);

                    if (dis.readInt() == 0x41646f62L && dis.readUnsignedShort() == 0x6500) {
                        int version = dis.readUnsignedByte();
                        int app14Flags0 = dis.readUnsignedShort();
                        int app14Flags1 = dis.readUnsignedShort();
                        app14AdobeColorTransform = dis.readUnsignedByte();
                    }
                }
            }
        }



        BufferedImage img = null;
        if (numberOfComponentsInFrame != 4) {

            img = readImageFromYUVorGray(new ByteArrayInputStream(byteArray));
        } else if (numberOfComponentsInFrame == 4) {


            ICC_Profile profile = null;
            if (app2ICCProfile.size() > 0) {
            try {
                profile = ICC_Profile.getInstance(new ByteArrayInputStream(app2ICCProfile.toByteArray()));
            } catch (Throwable ex) {

                ex.printStackTrace();
            }
            }


            if (profile == null) {
                profile = ICC_Profile.getInstance(JPEGImageIO.class.getResourceAsStream("Generic CMYK Profile.icc"));
            }

            switch (app14AdobeColorTransform) {
                case 0:
                default:

                    img = readRGBImageFromCMYK(new ByteArrayInputStream(byteArray), profile);
                    break;
                case 1:
                    throw new IOException("YCbCr not supported");
                case 2:



                    if (inverseYCCKColors) {
                    img = readRGBImageFromInvertedYCCK(new ByteArrayInputStream(byteArray), profile);
                    } else {
                    img = readRGBImageFromYCCK(new ByteArrayInputStream(byteArray), profile);
                    }
                    break;
            }
        }

        return img;
    }


    public static BufferedImage readImageFromYUVorGray(InputStream in) throws IOException {
        BufferedImage img = (in instanceof ImageInputStream) ? ImageIO.read((ImageInputStream) in) : ImageIO.read(in);
        return img;
    }


    public static BufferedImage readRGBImageFromCMYK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = ImageIO.getImageReadersByFormatName("JPEG").next();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromCMYK(raster, cmykProfile);
        return image;
    }


    public static BufferedImage readRGBImageFromYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = ImageIO.getImageReadersByFormatName("JPEG").next();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromYCCK(raster, cmykProfile);
        return image;
    }


    public static BufferedImage readRGBImageFromInvertedYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = ImageIO.getImageReadersByFormatName("JPEG").next();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        raster = convertInvertedYCCKToCMYK(raster);
        BufferedImage image = createRGBImageFromCMYK(raster, cmykProfile);
        return image;
    }


    public static BufferedImage createRGBImageFromYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        if (cmykProfile != null) {
            ycckRaster = convertYCCKtoCMYK(ycckRaster);
            image = createRGBImageFromCMYK(ycckRaster, cmykProfile);
        } else {
            int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
            int[] rgb = new int[w * h];
            int[] Y = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] Cb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Cr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);


            float vr, vg, vb;
            for (int i = 0, imax = Y.length; i < imax; i++) {
                float k = K[i], y = Y[i], cb = Cb[i], cr = Cr[i];
                vr = y + 1.402f * (cr - 128) - k;
                vg = y - 0.34414f * (cb - 128) - 0.71414f * (cr - 128) - k;
                vb = y + 1.772f * (cb - 128) - k;
                rgb[i] = (0xff & (vr < 0.0f ? 0 : vr > 255.0f ? 0xff : (int) (vr + 0.5f))) << 16 |
                        (0xff & (vg < 0.0f ? 0 : vg > 255.0f ? 0xff : (int) (vg + 0.5f))) << 8 |
                        (0xff & (vb < 0.0f ? 0 : vb > 255.0f ? 0xff : (int) (vb + 0.5f)));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);

            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }


    public static BufferedImage createRGBImageFromInvertedYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        if (cmykProfile != null) {
            ycckRaster = convertInvertedYCCKToCMYK(ycckRaster);
            image = createRGBImageFromCMYK(ycckRaster, cmykProfile);
        } else {
            int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
            int[] rgb = new int[w * h];

            PixelInterleavedSampleModel pix;

            int[] Y = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] Cb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Cr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);
            float vr, vg, vb;
            for (int i = 0, imax = Y.length; i < imax; i++) {
                float k = 255 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];
                vr = y + 1.402f * (cr - 128) - k;
                vg = y - 0.34414f * (cb - 128) - 0.71414f * (cr - 128) - k;
                vb = y + 1.772f * (cb - 128) - k;
                rgb[i] = (0xff & (vr < 0.0f ? 0 : vr > 255.0f ? 0xff : (int) (vr + 0.5f))) << 16 |
                        (0xff & (vg < 0.0f ? 0 : vg > 255.0f ? 0xff : (int) (vg + 0.5f))) << 8 |
                        (0xff & (vb < 0.0f ? 0 : vb > 255.0f ? 0xff : (int) (vb + 0.5f)));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);

            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }


    public static BufferedImage createRGBImageFromCMYK(Raster cmykRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        int w = cmykRaster.getWidth();
        int h = cmykRaster.getHeight();

        if (cmykProfile != null) {
            ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
            image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = image.getRaster();
            ColorSpace rgbCS = image.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
            cmykToRgb.filter(cmykRaster, rgbRaster);
        } else {

            int[] rgb = new int[w * h];

            int[] C = cmykRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] M = cmykRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Y = cmykRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = cmykRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            for (int i = 0, imax = C.length; i < imax; i++) {
                int k = K[i];
                rgb[i] = (255 - Math.min(255, C[i] + k)) << 16 |
                        (255 - Math.min(255, M[i] + k)) << 8 |
                        (255 - Math.min(255, Y[i] + k));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);
            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }



    private final static int SCALEBITS = 16;
    private final static int MAXJSAMPLE = 255;
    private final static int CENTERJSAMPLE = 128;
    private final static int ONE_HALF = 1 << (SCALEBITS - 1);
    private final static int[] Cr_r_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_b_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cr_g_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_g_tab = new int[MAXJSAMPLE + 1];


    private static synchronized void buildYCCtoRGBtable() {
        if (Cr_r_tab[0] == 0) {
            for (int i = 0,   x = -CENTERJSAMPLE; i <= MAXJSAMPLE; i++, x++) {



                Cr_r_tab[i] = (int) ((1.40200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;

                Cb_b_tab[i] = (int) ((1.77200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;

                Cr_g_tab[i] = -(int) (0.71414 * (1 << SCALEBITS) + 0.5) * x;


                Cb_g_tab[i] = -(int) ((0.34414) * (1 << SCALEBITS) + 0.5) * x + ONE_HALF;
            }
        }
    }


    private static Raster convertInvertedYCCKToCMYK(Raster ycckRaster) {
        buildYCCtoRGBtable();

        int w = ycckRaster.getWidth(),   h = ycckRaster.getHeight();
        int[] ycckY = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
        int[] ycckCb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
        int[] ycckCr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
        int[] ycckK = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);
        int[] cmyk = new int[ycckY.length];

        for (int i = 0; i < ycckY.length; i++) {
            int y = 255 - ycckY[i];
            int cb = 255 - ycckCb[i];
            int cr = 255 - ycckCr[i];
            int cmykC,   cmykM,   cmykY;

            cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
            cmykM = MAXJSAMPLE - (y +
                    (Cb_g_tab[cb] + Cr_g_tab[cr] >>
                    SCALEBITS));
            cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);

            cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24 |
                    (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16 |
                    (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8 |
                    255 - ycckK[i];
        }

        Raster cmykRaster = Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
        return cmykRaster;

    }

    private static Raster convertYCCKtoCMYK(Raster ycckRaster) {
        buildYCCtoRGBtable();

        int w = ycckRaster.getWidth(),   h = ycckRaster.getHeight();
        int[] ycckY = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
        int[] ycckCb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
        int[] ycckCr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
        int[] ycckK = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);

        int[] cmyk = new int[ycckY.length];

        for (int i = 0; i < ycckY.length; i++) {
            int y = ycckY[i];
            int cb = ycckCb[i];
            int cr = ycckCr[i];
            int cmykC,   cmykM,   cmykY;

            cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
            cmykM = MAXJSAMPLE - (y +
                    (Cb_g_tab[cb] + Cr_g_tab[cr] >>
                    SCALEBITS));
            cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);

            cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24 |
                    (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16 |
                    (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8 |
                    ycckK[i];
        }

        return Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
    }
}


