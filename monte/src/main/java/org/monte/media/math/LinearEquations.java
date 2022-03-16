
package org.monte.media.math;


public class LinearEquations {

    private LinearEquations() {
    }

    ;


    public static double[] solve(double a, double b, double c, double d, double e, double f) {
        System.out.println("["+a+" "+b+";"+c+" "+d+"]\\["+e+";"+f+"]");
        double x = (e * d - b * f) / (a * d - b * c);
        double y = (a * f - e * c) / (a * d - b * c);
        return new double[]{x, y};
    }


    public static double[] solve(double a, double b, double c, double d, double e, double f, double g, double h, double i, double j, double k, double l) {
        double det_abcdefghi=det(a,b,c,d,e,f,g,h,i);
        double x = det(j,b,c,k,e,f,l,h,i)/det_abcdefghi;
        double y = det(a,j,c,d,k,f,g,l,i)/det_abcdefghi;
        double z = det(a,b,j,d,e,k,g,h,l)/det_abcdefghi;
        return new double[]{x, y,z};
    }


    public static double det(double a, double b, double c, double d, double e, double f, double g, double h, double i) {
        return a * e * i
                + b * f * g
                + c * d * h
                - c * e * g
                - b * d * i
                - a * f * h;
    }


    public static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }
}
