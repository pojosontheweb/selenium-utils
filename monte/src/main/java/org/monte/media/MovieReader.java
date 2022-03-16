
package org.monte.media;

import org.monte.media.math.Rational;
import java.io.IOException;


public interface MovieReader {
    
    public int getTrackCount() throws IOException;
    
    
    public int findTrack(int fromTrack, Format format) throws IOException;
    
    
    public Rational getDuration() throws IOException;
    
    public Rational getDuration(int track) throws IOException;
    
   
    public long timeToSample(int track, Rational seconds) throws IOException;
   
    public Rational sampleToTime(int track, long sample) throws IOException;
     
    
    public Format getFileFormat() throws IOException;
    
    
    public Format getFormat(int track) throws IOException;
    
    
    public long getChunkCount(int track) throws IOException;
    
    
    public void read(int track, Buffer buffer) throws IOException;
    

    
    
    public int nextTrack() throws IOException;
    
    public void close() throws IOException;

    
    public void setMovieReadTime(Rational newValue) throws IOException;

    
    public Rational getReadTime(int track) throws IOException;

}
