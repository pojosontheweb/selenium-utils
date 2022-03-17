
package org.monte.media.ilbm;

import org.monte.media.image.BitmapImage;
import org.monte.media.iff.IFFOutputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.FileImageOutputStream;


public class ILBMEncoder {

    public ILBMEncoder() {
    }

    public void write(File f, BitmapImage img, int camg) throws IOException {
        IFFOutputStream out = null;
        try {
            out = new IFFOutputStream(new FileImageOutputStream(f));

            out.pushCompositeChunk("FORM", "ILBM");
            writeBMHD(out, img);
            writeCMAP(out, img);
            writeCAMG(out, camg);
            writeBODY(out, img);
            out.popChunk();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


    private void writeBMHD(IFFOutputStream out, BitmapImage img) throws IOException {
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
        out.writeUBYTE(44);
        out.writeUBYTE(52);
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


    private void writeCAMG(IFFOutputStream out, int camg) throws IOException {
        out.pushDataChunk("CAMG");

        out.writeLONG(camg);

        out.popChunk();
    }


    private void writeBODY(IFFOutputStream out, BitmapImage img) throws IOException {
        out.pushDataChunk("BODY");

        int w = img.getWidth()/8;
        int ss=img.getScanlineStride();
        int bs=img.getBitplaneStride();

        int offset=0;

       byte[] data = img.getBitmap();

        for (int y = 0, h = img.getHeight(); y < h; y++) {
            for (int p = 0, d = img.getDepth(); p < d; p++) {
                out.writeByteRun1(data, offset+bs*p, w);
            }
            offset+=ss;
        }

        out.popChunk();
    }
}
