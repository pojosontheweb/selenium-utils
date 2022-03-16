
package org.monte.media.tiff;


public class FileSegment {

    private long offset;
    private long length;

    public FileSegment(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getOffset() {
        return offset;
    }
}
