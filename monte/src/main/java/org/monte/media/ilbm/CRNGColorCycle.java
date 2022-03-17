
package org.monte.media.ilbm;


public class CRNGColorCycle extends ColorCycle {


    private int low;

    private int high;

    private boolean isReverse;

    private boolean isEHB;

    public CRNGColorCycle(int rate, int timeScale, int low, int high, boolean isActive, boolean isReverse, boolean isEHB) {
        super(rate, timeScale, isActive);
        this.low = low;
        this.high = high;
        this.isReverse = isReverse;
        this.isEHB = isEHB;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    @Override
    public void doCycle(int[] rgbs, long time) {
        if (isBlended) {
            doBlendedCycle(rgbs, time);
        } else {
            doHardCycle(rgbs,time);
        }
    }

    public void doBlendedCycle(int[] rgbs, long time) {
        if (isActive) {
            doHardCycle(rgbs, time);
            double blendf =  Math.IEEEremainder((time * rate / timeScale / 1000f), high - low + 1);
            blendf = blendf - Math.floor(blendf);
            int blend =  255-(int)(blendf*255);
            if (isReverse) {
                {
                    blend=255-blend;
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = ((((rgbs[i]&0xff)*blend+(rgbs[i - 1]&0xff)*(255-blend))>>8)&0xff)
                                |((((rgbs[i]&0xff00)*blend+(rgbs[i - 1]&0xff00)*(255-blend))>>8)&0xff00)
                                |((((rgbs[i]&0xff0000)*blend+(rgbs[i - 1]&0xff0000)*(255-blend))>>8)&0xff0000);
                    }
                    rgbs[low] =  ((((rgbs[low]&0xff)*blend+(tmp&0xff)*(255-blend))>>8)&0xff)
                                |((((rgbs[low]&0xff00)*blend+(tmp&0xff00)*(255-blend))>>8)&0xff00)
                                |((((rgbs[low]&0xff0000)*blend+(tmp&0xff0000)*(255-blend))>>8)&0xff0000);;
                }
                if (isEHB) {

                }
            } else {
                {
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = ((((rgbs[i]&0xff)*blend+(rgbs[i - 1]&0xff)*(255-blend))>>8)&0xff)
                                |((((rgbs[i]&0xff00)*blend+(rgbs[i - 1]&0xff00)*(255-blend))>>8)&0xff00)
                                |((((rgbs[i]&0xff0000)*blend+(rgbs[i - 1]&0xff0000)*(255-blend))>>8)&0xff0000);
                    }
                    rgbs[low] =  ((((rgbs[low]&0xff)*blend+(tmp&0xff)*(255-blend))>>8)&0xff)
                                |((((rgbs[low]&0xff00)*blend+(tmp&0xff00)*(255-blend))>>8)&0xff00)
                                |((((rgbs[low]&0xff0000)*blend+(tmp&0xff0000)*(255-blend))>>8)&0xff0000);;
                }
                if (isEHB) {

                }
            }
        }
    }

    public void doHardCycle(int[] rgbs, long time) {
        if (isActive) {

            int shift = (int) ((time * rate / timeScale / 1000) % (high - low + 1));
            if (isReverse) {
                for (int j = 0; j < shift; j++) {
                    int tmp = rgbs[low];
                    for (int i = low; i < high; i++) {
                        rgbs[i] = rgbs[i + 1];
                    }
                    rgbs[high] = tmp;
                }
                if (isEHB) {
                    for (int j = 0; j < shift; j++) {
                        int tmp = rgbs[low + 32];
                        for (int i = low + 32; i < high + 32; i++) {
                            rgbs[i] = rgbs[i + 1];
                        }
                        rgbs[high + 32] = tmp;
                    }
                }
            } else {
                for (int j = 0; j < shift; j++) {
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = rgbs[i - 1];
                    }
                    rgbs[low] = tmp;
                }
                if (isEHB) {
                    for (int j = 0; j < shift; j++) {
                        int tmp = rgbs[high + 32];
                        for (int i = high + 32; i > low + 32; i--) {
                            rgbs[i] = rgbs[i - 1];
                        }
                        rgbs[low + 32] = tmp;
                    }
                }
            }
        }
    }
}
