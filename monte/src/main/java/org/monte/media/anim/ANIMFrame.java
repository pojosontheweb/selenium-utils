
package org.monte.media.anim;

import java.awt.image.ColorModel;
import java.util.*;
import org.monte.media.image.BitmapImage;


public abstract class ANIMFrame {
    protected ColorModel colorModel;
    protected byte[] data;
    private int operation;
    private int mask;






    private long reltime;
    private int interleave;
    private int bits;
    protected final static int

    BadBitsOP_GeneralDelta = 0xffc0,
    BIT_LongData = 1,
    BIT_XOR = 2,
    BIT_OneInfoListForAllPlanes = 4,
    BIT_RLC = 8,
    BIT_Vertical = 16,
    BIT_LongInfoOffsets = 32,

    BadBitsOP_ByteVertical = 0xfff7;


    private ANIMAudioCommand[] audioCommands;


    public void addAudioCommand(ANIMAudioCommand command) {
        if (audioCommands == null) {
            audioCommands = new ANIMAudioCommand[1];
        } else {
            ANIMAudioCommand[] old = audioCommands;
            audioCommands = new ANIMAudioCommand[old.length + 1];
            System.arraycopy(old, 0, audioCommands, 0, old.length);
        }
        audioCommands[audioCommands.length - 1] = command;
    }


    public ANIMAudioCommand[] getAudioCommands() {
        return audioCommands;
    }


    public void cleanUpAudioCommands() {
        if (audioCommands != null && audioCommands.length > 1) {
        int i, j;
        Vector v = new Vector();
        v.addElement(audioCommands[0]);
        for (i=1; i < audioCommands.length; i++) {
            for (j=0; j < i; j++) {
                if ((audioCommands[j].getChannelMask() & audioCommands[i].getChannelMask()) != 0) {
                    break;
                }
            }
            if (j == i) v.addElement(audioCommands[i]);

        }
        audioCommands = new ANIMAudioCommand[v.size()];
        v.copyInto(audioCommands);
        }

    }


    public void setColorModel(ColorModel cm) { colorModel = cm; }
    public ColorModel getColorModel() { return colorModel; }
    public void setData(byte[] data) { this.data = data; }

    public void setOperation(int operation) { this.operation = operation; }
    public void setMask(int mask) { this.mask = mask; }
    public void setWidth(int w) {  }
    public void setHeight(int h) {  }
    public void setX(int x) {  }
    public void setY(int y) {  }
    public void setAbsTime(long abstime) {  }
    public void setRelTime(long reltime) { this.reltime = reltime; }
    public void setInterleave(int interleave) { this.interleave = interleave; }
    public void setBits(int bits) { this.bits = bits; }

    public int getOperation() { return this.operation; }
    public int getBits() { return this.bits; }


    public long getRelTime() { return this.reltime; }
    public int getInterleave() { return this.interleave; }

    public abstract void decode(BitmapImage bitmap, ANIMMovieTrack track);

    public int getTopBound(ANIMMovieTrack track) { return 0; }
    public int getBottomBound(ANIMMovieTrack track) { return track.getHeight()-1; }
    public int getLeftBound(ANIMMovieTrack track) { return 0; }
    public int getRightBound(ANIMMovieTrack track) { return track.getWidth()-1; }


    public boolean isBidirectional() {return true;}
}
