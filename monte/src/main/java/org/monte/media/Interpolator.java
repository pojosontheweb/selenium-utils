

package org.monte.media;


public abstract class Interpolator {
    private float startValue;
    private float endValue;
    private long startTime;
    private long timespan;
    private boolean isFinished;
    
    
    public Interpolator() {
        this(0f, 1f);
    }
    
    public Interpolator(long timespan) {
        this(0f, 1f, timespan);
    }
    
    public Interpolator(boolean reverse) {
        this((reverse) ? 1f : 0f, (reverse) ? 0f : 1f);
    }
    
    public Interpolator(float startValue, float endValue) {
        this(startValue, endValue, 1000);
    }
    
    public Interpolator(float startValue, float endValue, long timespan) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.timespan = timespan;
    }
    
    
    protected abstract void update(float fraction);
    
    
    protected float getFraction(float linearFraction) {
        return linearFraction;
    }
    
    
    public boolean replaces(Interpolator that) {
        return false;
    }
    
    public void initialize(long currentTimeMillis) {
        startTime = currentTimeMillis;
        update(getFraction(startValue));
    }
    
    
    public boolean isElapsed(long currentTimeMillis) {
        return timespan <= currentTimeMillis - startTime;
    }

    
    public void interpolate(long currentTimeMillis) {
        long elapsed = Math.min(timespan, currentTimeMillis - startTime);
        float weight = elapsed / (float) timespan;
        update(getFraction(startValue * (1 - weight) + endValue * weight));
    }
    
    
    public void finish(long currentTimeMillis) {
        if (! isFinished) {
        update(getFraction(endValue));
        isFinished = true;
        
        synchronized(this) {
            notifyAll();
        }
        }
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public boolean isSequential(Interpolator that) {
        return false;
    }
    
    public void setTimespan(long t) {
        this.timespan = t;
    }    
}
