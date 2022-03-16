
package org.monte.media.mpo;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


public class MPOImageReaderSpi extends ImageReaderSpi {

    public MPOImageReaderSpi() {
        super("Werner Randelshofer",
                "1.0",
                new String[]{"MPO"},
                new String[]{"mpo"},
                new String[]{"image/mpo"},
                "org.monte.media.mpo.MPOImageReader",
                new Class[]{ImageInputStream.class},
                null,
                false,
                null,
                null,
                null,
                null,
                false,
                 "com_sun_media_imageio_plugins_tiff_image_1.0",
                null,
                null,
                null
                );
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (source instanceof ImageInputStream) {
            ImageInputStream in = (ImageInputStream) source;
            in.mark();


            if (in.readShort() != -40) {
                in.reset();
                return false;
            }
            in.reset();
            return true;
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new MPOImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "MPO Image Reader";
    }
}
