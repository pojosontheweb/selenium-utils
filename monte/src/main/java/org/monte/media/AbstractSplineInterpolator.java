
package org.monte.media;

import java.awt.geom.Point2D.Float;
import java.util.Comparator;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.util.Arrays;
import static java.lang.Math.*;


public abstract class AbstractSplineInterpolator extends Interpolator {

    
    private LengthItem[] fractions;

    private static class LengthItem {

        public float x, y, t;

        public LengthItem(float x, float y, float t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }

        public LengthItem(Float p, float t) {
            this.x = p.x;
            this.y = p.y;
            this.t = t;
        }

        @Override
        public String toString() {
            return "LengthItem{" + "x=" + x + ", y=" + y + ", t=" + t + '}';
        }
        
        
    }

    private static class FractionComparator implements Comparator<LengthItem> {

        @Override
        public int compare(LengthItem o1, LengthItem o2) {
            if (o1.x > o2.x) {
                return 1;
            } else if (o1.x < o2.x) {
                return -1;
            }
            return 0;
        }
    }
    private static FractionComparator fractionComparator = new FractionComparator();

 public AbstractSplineInterpolator() {
        this(0f, 1f);
    }
    
    public AbstractSplineInterpolator(long timespan) {
        this(0f, 1f, timespan);
    }
    
    public AbstractSplineInterpolator(boolean reverse) {
        this((reverse) ? 1f : 0f, (reverse) ? 0f : 1f);
    }
    
    public AbstractSplineInterpolator(float startValue, float endValue) {
        this(startValue, endValue, 1000);
    }
    
    public AbstractSplineInterpolator(float startValue, float endValue, long timespan) {
        super(startValue,endValue,timespan);
    }    

    
    protected void updateFractions(int N) {
        fractions = new LengthItem[N];
        Float p = new Float();
        for (int i = 0; i < N; i++) {
            float t = (float) i / (N - 1);
            fractions[i] = new LengthItem(getXY(t, p), t);
        }
    }

    
    @Override
    public final float getFraction(float t) {
        LengthItem p1 = new LengthItem(t, 0f, t);
        LengthItem p2 = new LengthItem(t, 0f, t);
        int index = Arrays.binarySearch(fractions, p1, fractionComparator);
        if (index >= 0) {
            return fractions[index].y;
        }


        index = -1 - index;
        if (index == fractions.length) {
            return fractions[fractions.length - 1].y;
        }
        if (index == 0) {
            return fractions[0].y;
        }

        p1 = fractions[max(0, index - 1)];
        p2 = fractions[min(fractions.length - 1, index)];
        float weight = (p2.x - t) / (p2.x - p1.x);
        float s = p1.t * weight + p2.t * (1 - weight);
        return getY(s);
    }

    
    protected abstract Float getXY(float t, Float p);

    
    protected abstract float getY(float t);

    
    @Override
    protected void update(float fraction) {
    }
}
