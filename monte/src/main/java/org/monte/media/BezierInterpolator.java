
package org.monte.media;

import java.awt.geom.Point2D;
import java.util.Arrays;


public class BezierInterpolator extends AbstractSplineInterpolator {


    private double[] controlPoints;


    public BezierInterpolator(float x1, float y1, float x2, float y2) {
        this(x1, y1, x2, y2, 1000);

    }


    public BezierInterpolator(float x1, float y1, float x2, float y2, long timespan) {
        this(x1, y1, x2, y2, false, timespan);
    }


    public BezierInterpolator(float x1, float y1, float x2, float y2, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);

        if (x1 < 0 || x1 > 1.0f
                || y1 < 0 || y1 > 1.0f
                || x2 < 0 || x2 > 1.0f
                || y2 < 0 || y2 > 1.0f) {
            throw new IllegalArgumentException("Control points must be in "
                    + "the range [0, 1]:");
        }

        controlPoints=new double[4*2];
        controlPoints[0] = 0;
        controlPoints[1] = 0;
        controlPoints[2] = x1;
        controlPoints[3] = y1;
        controlPoints[4] = x2;
        controlPoints[5] = y2;
        controlPoints[6] = 1;
        controlPoints[7] = 1;

        updateFractions(100);
    }


    public BezierInterpolator(double[][] controlPoints) {
        this(controlPoints,false,1000);


    }

    public BezierInterpolator(double[][] controlPoints, boolean reverse, long timespan) {
        super((reverse) ? 1f : 0f, (reverse) ? 0f : 1f, timespan);
        this.controlPoints=new double[controlPoints.length*2];
        for (int i=0;i<controlPoints.length;i++){
            this.controlPoints[i*2]=controlPoints[i][0];
            this.controlPoints[i*2+1]=controlPoints[i][1];
        }
        updateFractions(100);
    }


    @Override
    public Point2D.Float getXY(float t, Point2D.Float xy) {
        if (xy==null)xy=new Point2D.Float(0,0);

        double[] p = controlPoints.clone();

        for (int i = p.length/2-1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                p[j*2+0] = (1 - t) * p[j*2+0] + t * p[(j+1)*2+0];
                p[j*2+1] = (1 - t) * p[j*2+1] + t * p[(j + 1)*2+1];
            }
        }

        xy.setLocation(p[0],p[1]);
        return xy;
    }

    @Override
    public float getY(float t) {
        double[] p = controlPoints.clone();

        for (int i = p.length/2-1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                p[j*2+0] = (1 - t) * p[j*2+0] + t * p[(j + 1)*2+0];
                p[j*2+1] = (1 - t) * p[j*2+1] + t * p[(j + 1)*2+1];
            }
        }
        return (float)p[1];
    }
}
