

package org.monte.media.quicktime;

import org.monte.media.Buffer;
import org.monte.media.Multiplexer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageOutputStream;


public class QuickTimeMultiplexer extends QuickTimeWriter implements Multiplexer {
 public QuickTimeMultiplexer(File file) throws IOException {

super(file);

    }

    
    public QuickTimeMultiplexer(ImageOutputStream out) throws IOException {
        super(out);
    }


}
