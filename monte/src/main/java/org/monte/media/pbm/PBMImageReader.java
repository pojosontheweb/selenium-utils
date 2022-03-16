
package org.monte.media.pbm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.ilbm.*;
import org.monte.media.io.ImageInputStreamAdapter;


public class PBMImageReader extends ImageReader {

    private ArrayList<ColorCyclingMemoryImageSource> images = null;

    public PBMImageReader(PBMImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        if (allowSearch && images == null) {
            readImages();
        }
        return images.size();
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readImages();
        return images.get(imageIndex).getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readImages();
        return images.get(imageIndex).getHeight();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readImages();
        ColorCyclingMemoryImageSource iip = images.get(imageIndex);

        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(iip.getColorModel(),
                iip.getColorModel().createCompatibleSampleModel(iip.getWidth(), iip.getHeight())));
        return l.iterator();
    }


    @Override
    public float getAspectRatio(int imageIndex) throws IOException {
        readImages();
        ColorCyclingMemoryImageSource mis=images.get(imageIndex);
        float ratio = (float) getWidth(imageIndex) / getHeight(imageIndex);
        if (mis.getProperties().containsKey("aspect")) {
            ratio*=(Double)mis.getProperties().get("aspect");
        }
        return ratio;
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
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        readImages();

        return images.get(imageIndex).toBufferedImage();
    }

    private void readImages() throws IOException {
        ImageInputStream in = (ImageInputStream) getInput();
        if (images == null) {
            in.seek(0);
            PBMDecoder d = new PBMDecoder(new ImageInputStreamAdapter(in));
            images = d.produce();
        }
    }
}
