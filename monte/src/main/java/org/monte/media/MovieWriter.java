
package org.monte.media;

import org.monte.media.math.Rational;
import java.io.IOException;


public interface MovieWriter extends Multiplexer {

    public Format getFileFormat() throws IOException;


    public int addTrack(Format format) throws IOException;


    public Format getFormat(int track);


    public int getTrackCount();


    @Override
    public void write(int track, Buffer buf) throws IOException;


    @Override
    public void close() throws IOException;


    public boolean isDataLimitReached();


    public Rational getDuration(int track);

    public boolean isEmpty(int track);
}
