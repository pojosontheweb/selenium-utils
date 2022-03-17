
package org.monte.media;


public interface ColorCyclePlayer extends Player {

    
    public boolean isColorCyclingStarted();

    
    public void setColorCyclingStarted(boolean b);

    
    public boolean isColorCyclingAvailable();

    
    public void setBlendedColorCycling(boolean newValue);

    
    public boolean isBlendedColorCycling();
}
