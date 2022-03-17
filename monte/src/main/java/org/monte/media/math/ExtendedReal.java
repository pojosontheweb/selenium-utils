
package org.monte.media.math;

public class ExtendedReal
extends Number {
    private boolean negSign;

    private int exponent;

    private long mantissa;


    public final static ExtendedReal MAX_VALUE = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0x7f,    (byte)0xfe, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );

    public final static ExtendedReal MIN_VALUE = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xfe, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );
    public final static ExtendedReal NaN = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );
    public final static ExtendedReal NEGATIVE_INFINITY = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}
    );
    public final static ExtendedReal POSITIVE_INFINITY = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0x7f,    (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}
    );

    public ExtendedReal(byte[] bits) {
        negSign = (bits[0] & 0x80) != 0;
        exponent = (bits[0] & 0x7f) << 8 | (bits[1] & 0xff);
        mantissa =
            (bits[2] & 0xffL) << 56 |
            (bits[3] & 0xffL) << 48 |
            (bits[4] & 0xffL) << 40 |
            (bits[5] & 0xffL) << 32 |
            (bits[6] & 0xffL) << 24 |
            (bits[7] & 0xffL) << 16 |
            (bits[8] & 0xffL) << 8  |
            (bits[9] & 0xffL) << 0;
    }

    public ExtendedReal(double d) {
        if (Double.isNaN(d)) {
            negSign = NaN.negSign;
            exponent = NaN.exponent;
            mantissa = NaN.mantissa;

        } else if (Double.isInfinite(d)) {
            if (d < 0.0) {
                negSign = NEGATIVE_INFINITY.negSign;
                exponent = NEGATIVE_INFINITY.exponent;
                mantissa = NEGATIVE_INFINITY.mantissa;
            } else {
                negSign = POSITIVE_INFINITY.negSign;
                exponent = POSITIVE_INFINITY.exponent;
                mantissa = POSITIVE_INFINITY.mantissa;
            }
        } else if (d == +0.0) {
            // nothing to do
        } else if (d == -0.0) {
            negSign = true;

        } else {
            long longBits = Double.doubleToLongBits(d);

            negSign = (longBits & 0x8000000000000000L) != 0L;
            exponent = ((int) (longBits & 0x7ff0000000000000L) >>> 52) - 1023 + 16383;
            mantissa = 0x8000000000000000L | (longBits & 0x000fffffffffffffL) << 11;
        }
    }

    public boolean isNaN() {
        return exponent == 0x7ffff && (mantissa & 0x7fffffffffffffffL) != 0;
    }

    public boolean isInfinite() {
        return exponent == 0x7ffff && mantissa == 0x7fffffffffffffffL;
    }

    public double doubleValue() {
        if (isNaN()) {
            return Double.NaN;
        }


        long longBits = 0;
        // biased exponent

        int biasedExponent = exponent - 16383 + 1023;
        if (biasedExponent > 2047) {
            // overflow
            return (negSign) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        if (biasedExponent < 0 || (mantissa & 0x8000000000000000L) == 0L) {
            // underflow
            return 0.0;
        }

        // negSign
        if (negSign) {
            longBits = 0x8000000000000000L;
        }

        longBits = longBits | (((long) biasedExponent) << 52);
        longBits = longBits | ((mantissa & 0x7fffffffffffffffL) >>> 11);
        return Double.longBitsToDouble(longBits);
    }
    public float floatValue() {
        return (float) doubleValue();
    }
    public int intValue() {
        return (int) doubleValue();
    }
    public long longValue() {
        return (long) doubleValue();
    }

    public int hashCode() {
        long bits = Double.doubleToLongBits(doubleValue());
        return (int)(bits ^ (bits >>> 32));
    }

    public boolean equals(Object obj) {
        return (obj != null)
            && (obj instanceof ExtendedReal)
            && (equals((ExtendedReal) obj));
    }

    public boolean equals(ExtendedReal obj) {
        return negSign == obj.negSign && exponent == obj.exponent && mantissa == obj.mantissa;
    }

    public String toString() {
        return Double.toString(doubleValue());
    }

}
