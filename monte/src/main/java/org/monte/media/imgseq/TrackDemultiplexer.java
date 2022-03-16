
package org.monte.media.imgseq;

import org.monte.media.Demultiplexer;
import org.monte.media.Track;
import java.io.IOException;


public class TrackDemultiplexer implements Demultiplexer {

    private Track[] tracks;

    public TrackDemultiplexer(Track[] tracks) {
        this.tracks = tracks.clone();
    }

    @Override
    public Track[] getTracks() {
        return tracks.clone();
    }

    @Override
    public void close() throws IOException {
    }
}
