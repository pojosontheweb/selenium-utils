
package org.monte.media.binary;


public interface BinaryModel {

    public long getLength();

    public int getBytes(long off, int len, byte[] target);


    public void close();
}
