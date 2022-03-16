
package org.monte.media.ilbm;


public abstract class ColorCycle implements Cloneable {

    
    protected int rate;
    
    protected int timeScale;
    
    protected boolean isActive;

    
    protected boolean isBlended;

    public ColorCycle(int rate, int timeScale, boolean isActive) {
        this.rate = rate;
        this.timeScale = timeScale;
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }
    public int getRate() {
        return rate;
    }

    public int getTimeScale() {
        return timeScale;
    }

    
    public boolean isBlended() {
        return isBlended;
    }
    
    public void setBlended(boolean newValue) {
        isBlended=newValue;
    }


    public abstract void doCycle(int[] rgbs, long time);

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            InternalError error = new InternalError();
            error.initCause(ex);
            throw error;
        }
    }
}
