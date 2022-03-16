
package org.monte.media.jmf.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;



public class MJPGImageReaderSpi extends ImageReaderSpi {

    public MJPGImageReaderSpi() {
        super("Werner Randelshofer",
                "1.0",
                new String[]{"MJPG"},
                new String[]{"mjpg"},
                new String[]{"image/mjpg"},
                "org.monte.media.jmf.renderer.video.MJPGImageReader",
                new Class[]{ImageInputStream.class,InputStream.class,byte[].class,javax.media.Buffer.class},
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
        return new MJPGImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "MJPG Image Reader";
    }
}
