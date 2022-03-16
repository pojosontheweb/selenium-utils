
package org.monte.media.pgm;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


public class PGMImageReaderSpi extends ImageReaderSpi {

    public PGMImageReaderSpi() {
        super("Werner Randelshofer",
                "1.0",
                new String[]{"PGM"},
                new String[]{"pgm"},
                new String[]{"image/pgm"},
                "org.monte.media.pgm.PGMImageReader",
                new Class[]{ImageInputStream.class},
                null,
                false,
                null,
                null,
                null,
                null,
                false,
                null,
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


            if (in.readShort() != 0x5035) {
                in.reset();
                return false;
            }

            int b;
            if ((b = in.readUnsignedByte()) != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
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
        return new PGMImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "PGM Image Reader";
    }
}
