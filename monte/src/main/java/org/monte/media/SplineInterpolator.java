
package org.monte.media;

import java.awt.geom.Point2D.Float;
import java.util.Comparator;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.util.Arrays;
import static java.lang.Math.*;


public class SplineInterpolator extends AbstractSplineInterpolator {

    
    private float x1, y1, x2, y2;

    
    public SplineInterpolator(float x1, float y1, float x2, float y2) {
        this(x1, y1, x2, y2, 1000);

    }

    
    public SplineInterpolator(float x1, float y1, float x2, float y2, long timespan) {
        this(x1, y1, x2, y2, false, timespan);
    }

    
    public SplineInterpolator(float x1, float y1, float x2, float y2, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);

        if (x1 < 0 || x1 > 1.0f
                || y1 < 0 || y1 > 1.0f
                || x2 < 0 || x2 > 1.0f
                || y2 < 0 || y2 > 1.0f) {
            throw new IllegalArgumentException("Control points must be in "
                    + "the range [0, 1]:");
        }

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        updateFractions(100);
    }


    
    @Override
    public Float getXY(float t, Float p) {
        if (p == null) {
            p = new Float();
        }
        float invT = (1 - t);
        float b1 = 3 * t * (invT * invT);
        float b2 = 3 * (t * t) * invT;
        float b3 = t * t * t;
        p.setLocation((b1 * x1) + (b2 * x2) + b3, (b1 * y1) + (b2 * y2) + b3);
        return p;
    }
    
    @Override
    public float getY(float t) {
        float invT = (1 - t);
        float b1 = 3 * t * (invT * invT);
        float b2 = 3 * (t * t) * invT;
        float b3 = t * t * t;
        return (b1 * y1) + (b2 * y2) + b3;
    }
}
