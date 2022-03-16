
package org.monte.media.ilbm;

import java.awt.image.DirectColorModel;


public class HAMColorModel extends DirectColorModel {

    public final static int
            HAM6 = 6,
            HAM8 = 8;

    protected int HAMType;
    protected int map_size;
    protected boolean opaque;
    protected int[] rgb;


    public HAMColorModel(int aHAMType,int size,byte r[],byte g[],byte b[], boolean isOCS) {
        super(24,0x00ff0000,0x0000ff00,0x000000ff);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }
        HAMType = aHAMType;
        if (isOCS) {
            byte[] r8 = new byte[size];
            byte[] g8 = new byte[size];
            byte[] b8 = new byte[size];
            for (int i=0; i < size; i++) {
                r8[i] = (byte) (((r[i] & 0xf) << 4) | (r[i] & 0xf));
                g8[i] = (byte) (((g[i] & 0xf) << 4) | (g[i] & 0xf));
                b8[i] = (byte) (((b[i] & 0xf) << 4) | (b[i] & 0xf));
            }
            setRGBs(size,r8,g8,b8,null);
        } else {
            setRGBs(size,r,g,b,null);
        }
    }


    public HAMColorModel(int aHAMType,int size,int rgb[], boolean isOCS) {
        super(24,0x00ff0000,0x0000ff00,0x000000ff);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }

        HAMType = aHAMType;
        if (isOCS) {
            byte[] r = new byte[rgb.length];
            byte[] g = new byte[rgb.length];
            byte[] b = new byte[rgb.length];
            for (int i=0; i < rgb.length; i++) {
                r[i] = (byte) (((rgb[i] & 0xf00) >>> 8) |
                        (rgb[i] & 0xf00) >>> 4);
                g[i] = (byte) (((rgb[i] & 0xf0) >>> 4) |
                        (rgb[i] & 0xf0));
                b[i] = (byte) (((rgb[i] & 0xf) ) |
                        (rgb[i] & 0xf) << 4);
            }
            setRGBs(size,r,g,b,null);
        } else {
            byte[] r = new byte[size];
            byte[] g = new byte[size];
            byte[] b = new byte[size];
            for (int i=0; i < size; i++) {
                r[i] = (byte) ((rgb[i] & 0xff0000) >>> 16);
                g[i] = (byte) ((rgb[i] & 0xff00) >>> 8);
                b[i] = (byte) (rgb[i] & 0xff);
            }
            setRGBs(size,r,g,b,null);
        }
    }


    public int getHAMType() {
        return HAMType;
    }


    public int getDepth() {
        return HAMType;
    }


    protected void setRGBs(int size, byte r[], byte g[], byte b[], byte a[]) {
        if (size > 256) {
            throw new ArrayIndexOutOfBoundsException();
        }
        map_size = size;
        rgb = new int[256];
        int alpha = 0xff;
        opaque = true;
        for (int i = 0; i < size; i++) {
            if (a != null) {
                alpha = (a[i] & 0xff);
                if (alpha != 0xff) {
                    opaque = false;
                }
            }
            rgb[i] = (alpha << 24)
            | ((r[i] & 0xff) << 16)
            | ((g[i] & 0xff) << 8)
            | (b[i] & 0xff);
        }
    }


    final public void getReds(byte r[]) {
        for (int i = 0; i < map_size; i++) {
            r[i] = (byte) (rgb[i] >> 16);
        }
    }


    final public void getGreens(byte g[]) {
        for (int i = 0; i < map_size; i++) {
            g[i] = (byte) (rgb[i] >> 8);
        }
    }


    final public void getBlues(byte b[]) {
        for (int i = 0; i < map_size; i++) {
            b[i] = (byte) rgb[i];
        }
    }

    final public void getRGBs(int rgbs[]) {
        for (int i = 0; i < map_size; i++) {
            rgbs[i] = rgb[i];
        }
    }

    final public int getMapSize() {
        return map_size;
    }
}
