

package org.monte.media;

import java.io.IOException;


public interface Multiplexer {

    public void write(int track, Buffer buf) throws IOException;


    public void close() throws IOException;
}
