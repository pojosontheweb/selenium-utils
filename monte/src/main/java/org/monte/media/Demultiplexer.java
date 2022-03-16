

package org.monte.media;

import java.io.IOException;


public interface Demultiplexer {

    public Track[] getTracks();


    public void close() throws IOException;
}
