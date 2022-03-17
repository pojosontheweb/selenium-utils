
package org.monte.media.anim;

import org.monte.media.Demultiplexer;
import org.monte.media.Track;
import java.io.File;
import java.io.IOException;


public class ANIMDemultiplexer extends ANIMReader implements Demultiplexer {

    private Track[] tracks;

    public ANIMDemultiplexer(File file) throws IOException {
        super(file);
    }

    @Override
    public Track[] getTracks() {
        if (tracks == null) {
            tracks = new Track[]{new ANIMTrack(this)};
        }
        return tracks.clone();
    }
}
