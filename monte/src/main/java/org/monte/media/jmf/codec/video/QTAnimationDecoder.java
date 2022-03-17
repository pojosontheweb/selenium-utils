
package org.monte.media.jmf.codec.video;

import org.monte.media.quicktime.AnimationCodec;
import java.awt.image.BufferedImage;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;


public class QTAnimationDecoder extends AbstractVideoDecoder {

    @Override
    protected Format[] getMatchingOutputFormats(Format input) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int process(Buffer input, Buffer output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
