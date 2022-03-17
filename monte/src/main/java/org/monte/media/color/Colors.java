
package org.monte.media.color;

import java.awt.image.IndexColorModel;
import static java.lang.Math.*;


public class Colors {

    
    private Colors() {
    }

    
    public static IndexColorModel createMacColors() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];


        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    r[index] = (byte) (255 - 51 * i);
                    g[index] = (byte) (255 - 51 * j);
                    b[index] = (byte) (255 - 51 * k);
                    index++;
                }
            }
        }

        index--;


        byte[] ramp = {(byte) 238, (byte) 221, (byte) 187, (byte) 170, (byte) 136, (byte) 119, 85, 68, 34, 17};
        for (int i = 0; i < 10; i++) {
            r[index] = ramp[i];
            g[index] = (byte) (0);
            b[index] = (byte) (0);
            index++;
        }

        for (int j = 0; j < 10; j++) {
            r[index] = (byte) (0);
            g[index] = ramp[j];
            b[index] = (byte) (0);
            index++;
        }

        for (int k = 0; k < 10; k++) {
            r[index] = (byte) (0);
            g[index] = (byte) (0);
            b[index] = ramp[k];
            index++;
        }

        for (int ijk = 0; ijk < 10; ijk++) {
            r[index] = ramp[ijk];
            g[index] = ramp[ijk];
            b[index] = ramp[ijk];
            index++;
        }


        

        IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);
        return icm;
    }

    private static void RGBtoYCC(float[] rgb, float[] ycc) {
        float R = rgb[0];
        float G = rgb[1];
        float B = rgb[2];
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        float V = R - Y;
        float U = B - Y;
        float Cb = (U / 2f) + 0.5f;
        float Cr = (V / 1.6f) + 0.5f;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    private static void YCCtoRGB(float[] ycc, float[] rgb) {
        float Y = ycc[0];
        float Cb = ycc[1];
        float Cr = ycc[2];
        float U = (Cb - 0.5f) * 2f;
        float V = (Cr - 0.5f) * 1.6f;
        float R = V + Y;
        float B = U + Y;
        float G = (Y - 0.3f * R - 0.1f * B) / 0.6f;
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    
    private static void RGB8toYCC16(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    
    private static void RGB8toYCC16(int rgb, int[] ycc) {
        int R = (rgb & 0xff0000) >>> 16;
        int G = (rgb & 0xff00) >>> 8;
        int B = rgb & 0xff;
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    
    private static void YCC16toRGB8(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = min(255, max(0, (V + Y) / 256));
        int B = min(255, max(0, (U + Y) / 256));
        int G = min(255, max(0, (Y - 77 * R - 26 * B) / 153));
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    
    private static void YCC8toRGB8(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];





        int R = (1000 * Y + 1402 * (Cr - 128)) / 1000;
        int G = (100000 * Y - 34414 * (Cb - 128) - 71414 * (Cr - 128)) / 100000;
        int B = (1000 * Y + 1772 * (Cb - 128)) / 1000;
        rgb[0] = min(255, max(0, R));
        rgb[1] = min(255, max(0, G));
        rgb[2] = min(255, max(0, B));
    }

    
    private static void RGB8toYCC8(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];





        int Y = (299 * R + 587 * G + 114 * B) / 1000;
        int Cb = (-1687 * R - 3313 * G + 5000 * B) / 10000 + 128;
        int Cr = (5000 * R - 4187 * G - 813 * B) / 10000 + 128;
        ycc[0] = min(255, max(0, Y));
        ycc[1] = min(255, max(0, Cb));
        ycc[2] = min(255, max(0, Cr));
    }
}
