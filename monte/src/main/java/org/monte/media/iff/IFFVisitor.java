
package org.monte.media.iff;

import org.monte.media.AbortException;
import org.monte.media.ParseException;


public interface IFFVisitor
  {
  public void enterGroup(IFFChunk group)
  throws ParseException, AbortException;

  public void leaveGroup(IFFChunk group)
  throws ParseException, AbortException;

  public void visitChunk(IFFChunk group, IFFChunk chunk)
  throws ParseException, AbortException;
  }
