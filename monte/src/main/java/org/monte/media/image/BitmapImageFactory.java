

package org.monte.media.image;

import org.monte.media.iff.MC68000OutputStream;
import org.monte.media.iff.MutableIFFChunk;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import org.monte.media.ilbm.HAMColorModel;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Hashtable;


public class BitmapImageFactory {
    
    
    private BitmapImageFactory() {
    }
    
    
    public static BufferedImage toBufferedImage(BitmapImage bm) {
        BufferedImage image = null;
        Hashtable properties = new Hashtable() ;

        
        bm.convertToChunky();
        switch (bm.getPixelType()) {
            case BitmapImage.BYTE_PIXEL : {
                
                image = new BufferedImage(bm.getWidth(), bm.getHeight(),
                        BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) bm.getChunkyColorModel());
                WritableRaster ras = image.getRaster();
                byte[] pixels = ((DataBufferByte) ras.getDataBuffer()).getData();
                System.arraycopy(bm.getBytePixels(), 0, pixels, 0, bm.getBytePixels().length);
                break;
            }
            case BitmapImage.INT_PIXEL : {
                WritableRaster ras = Raster.createPackedRaster(
                        DataBuffer.TYPE_INT, bm.getWidth(), bm.getHeight(),
                        3, 8, new Point());
                image = new BufferedImage(bm.getChunkyColorModel(), ras, false,
                        properties);
                int[] pixels = ((DataBufferInt) ras.getDataBuffer()).getData();
                System.arraycopy(bm.getIntPixels(), 0, pixels, 0, bm.getIntPixels().length);
                break;
            }
        }
        
        return image;
    }
    public static Image toMemoryImage(BitmapImage bm) {
        bm.convertToChunky();
        switch (bm.getPixelType()) {
            case BitmapImage.BYTE_PIXEL : {
                
                MemoryImageSource mis = new MemoryImageSource(
                        bm.getWidth(), bm.getHeight(), bm.getChunkyColorModel(),
                        bm.getBytePixels().clone(), 0, bm.getWidth());
                return Toolkit.getDefaultToolkit().createImage(mis);
            }
            case BitmapImage.INT_PIXEL : {
                MemoryImageSource mis = new MemoryImageSource(
                        bm.getWidth(), bm.getHeight(), bm.getChunkyColorModel(),
                        bm.getIntPixels().clone(), 0, bm.getWidth());
                return Toolkit.getDefaultToolkit().createImage(mis);
            }
        }
        
        return null;
    }

    public static BitmapImage toBitmapImage(MemoryImageSource mis) {
        return null;
    }
    public static BitmapImage toBitmapImage(ColorCyclingMemoryImageSource mis) {
        return null;
    }
    public static BitmapImage toBitmapImage(BufferedImage mis) {
        return null;
    }
    
    public static void write(BitmapImage bm, File f) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            write(bm, out);
        } finally {
            out.close();
        }
    }
    public static void write(BitmapImage bm, OutputStream out) throws IOException {
        MutableIFFChunk form = new MutableIFFChunk("FORM", "ILBM");
        
        ByteArrayOutputStream buf;
        MC68000OutputStream struct;
        

        
        
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        struct.writeUWORD(bm.getWidth());
        struct.writeUWORD(bm.getHeight());
        struct.writeWORD(0);
        struct.writeWORD(0);
        struct.writeUBYTE(bm.getDepth());
        struct.writeUBYTE(0);
        struct.writeUBYTE(1);
        struct.writeUBYTE(0);
        struct.writeUWORD(0);
        struct.writeUBYTE(10);
        struct.writeUBYTE(11);
        struct.writeWORD(bm.getWidth());
        struct.writeWORD(bm.getHeight());
        struct.close();
        form.add(new MutableIFFChunk("BMHD", buf.toByteArray()));
        
        
        ColorModel cm = bm.getPlanarColorModel();
        
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        int viewMode = 0;
        if (cm instanceof HAMColorModel) {
            viewMode |= 0x00800;
        }
        struct.writeULONG(viewMode);
        struct.close();
        form.add(new MutableIFFChunk("CAMG", buf.toByteArray()));
        
        
        if (cm instanceof HAMColorModel) {
            HAMColorModel hcm = (HAMColorModel) cm;
            struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
            byte[] r = new byte[hcm.getMapSize()];
            byte[] g = new byte[hcm.getMapSize()];
            byte[] b = new byte[hcm.getMapSize()];
            hcm.getReds(r);
            hcm.getGreens(g);
            hcm.getBlues(b);
            for (int i=0; i <r.length; i++) {
                struct.writeUBYTE(r[i]);
                struct.writeUBYTE(g[i]);
                struct.writeUBYTE(b[i]);
            }
            struct.close();
            form.add(new MutableIFFChunk("CMAP", buf.toByteArray()));
        }

        
        
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        for (int y=0, height=bm.getHeight(); y < height; y++) {
            for (int d=0, depth=bm.getDepth(); d < depth; d++) {
                struct.writeByteRun1(bm.getBitmap(), y * bm.getScanlineStride() + d * bm.getBitplaneStride(), bm.getWidth() / 8);
            }
        }
   
        form.add(new MutableIFFChunk("BODY", buf.toByteArray()));
        
        MC68000OutputStream mout = new MC68000OutputStream(out);
        form.write(mout);
        mout.flush();
    }
}
