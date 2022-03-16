

package org.monte.media;

import java.io.IOException;


public interface Track {
    
    public long getSampleCount();

    public void setPosition(long pos);
    
    public long getPosition();

    
    public void read(Buffer buf) throws IOException;
}
