
package org.monte.media.jmf.jpeg;

import org.monte.media.avi.AVIBMPDIB;
import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.Buffer;


public class MJPGImageReader extends ImageReader {

    private static DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0x0);
    
    private BufferedImage image;

    public MJPGImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readHeader();
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readHeader();
        return image.getHeight();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readHeader();
        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(RGB, RGB.createCompatibleSampleModel(image.getWidth(), image.getHeight())));
        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex > 0) {
            throw new IndexOutOfBoundsException();
        }
        readHeader();

        return image;
    }

    
    private void readHeader() throws IOException {
        if (image == null) {
            ImageReader r = new JPEGImageReader(getOriginatingProvider());
            Object in = getInput();
            if (in instanceof Buffer) {
                Buffer buffer = (Buffer) in;
                in=buffer.getData();
            }

            if (in instanceof byte[]) {
                r.setInput(new MemoryCacheImageInputStream(AVIBMPDIB.prependDHTSeg((byte[]) in)));
            } else if (in instanceof ImageInputStream) {
                r.setInput(AVIBMPDIB.prependDHTSeg((ImageInputStream) in));
            } else {
                r.setInput(AVIBMPDIB.prependDHTSeg((InputStream) in));
            }
            image = r.read(0);
        }
    }
}
