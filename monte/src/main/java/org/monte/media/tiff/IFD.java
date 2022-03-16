
package org.monte.media.tiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IFD {

    
    private long offset;

    
    private boolean hasNextOffset;

    
    private long nextOffset;

    
    private ArrayList<IFDEntry> entries;

    public IFD(long offset, boolean hasNextOffset) {
        this.offset = offset;
        this.hasNextOffset=hasNextOffset;
        this.entries = new ArrayList<IFDEntry>();
    }

    
    public long getOffset() {
        return offset;
    }

    
     void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }

    
    public long getNextOffset() {
        return (hasNextOffset)?this.nextOffset:0;
    }

    public boolean hasNextOffset() {
        return hasNextOffset;
    }

    
    public int getCount() {
        return entries.size();
    }

    
    public IFDEntry get(int index) {
        return null;
    }

    
     void add(IFDEntry entry) {
        entries.add(entry);
    }

    
    public List<IFDEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("IFD offset:");
        buf.append(offset);
        buf.append(", numEntries:");
        buf.append(entries.size());
        buf.append(", next:");
        buf.append(nextOffset);

        for (IFDEntry e : entries) {
            buf.append("\n  ");
            buf.append(e);
        }

        return buf.toString();
    }

    
    public long getLength() {
        return getCount()*12+(hasNextOffset?4:0);
    }

}
