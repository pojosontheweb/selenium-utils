
package org.monte.media;


public interface StateModel {
  
  public void addStateListener(StateListener listener);

  
  public void removeStateListener(StateListener listener);
  
  
  public int getState();
}
