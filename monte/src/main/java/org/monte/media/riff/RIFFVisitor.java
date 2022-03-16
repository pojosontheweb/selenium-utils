
package org.monte.media.riff;

import org.monte.media.AbortException;
import org.monte.media.ParseException;


public interface RIFFVisitor {
    
    public boolean enteringGroup(RIFFChunk group);
    
    
    public void enterGroup(RIFFChunk group)
    throws ParseException, AbortException;
    
    
    public void leaveGroup(RIFFChunk group)
    throws ParseException, AbortException;
    
    
    public void visitChunk(RIFFChunk group, RIFFChunk chunk)
    throws ParseException, AbortException;
}
