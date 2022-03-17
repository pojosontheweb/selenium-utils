
package org.monte.media;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.monte.media.math.Rational;
import org.monte.media.util.Methods;


public class Buffer {

    
    public EnumSet<BufferFlag> flags = EnumSet.noneOf(BufferFlag.class);
    
    public static final int NOT_SPECIFIED = -1;
    
    public int track;
    
    public Object header;
    
    public Object data;
    
    public int offset;
    
    public int length;
    
    public Rational sampleDuration;
    
    public Rational timeStamp;
    
    public Format format;
    
    public int sampleCount = 1;
    
    
    public long sequenceNumber;

    
    public void setMetaTo(Buffer that) {
        this.flags = EnumSet.copyOf(that.flags);




        this.track = that.track;
        this.sampleDuration = that.sampleDuration;
        this.timeStamp = that.timeStamp;
        this.format = that.format;
        this.sampleCount = that.sampleCount;
        this.format = that.format;
        this.sequenceNumber=that.sequenceNumber;
    }

    
    public void setDataTo(Buffer that) {
        this.offset = that.offset;
        this.length = that.length;
        this.data = copy(that.data, this.data);
        this.header = copy(that.header, this.header);

    }

    private Object copy(Object from, Object into) {
        if (from instanceof byte[]) {
            byte[] b=(byte[])from;
            if (!(into instanceof byte[]) || ((byte[]) into).length < b.length) {
                into = new byte[b.length];
            }
            System.arraycopy(b, 0, (byte[])into, 0, b.length);
        } else if (from instanceof BufferedImage) {

            BufferedImage img = (BufferedImage) from;
            ColorModel cm = img.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = img.copyData(null);
            into = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        } else if (from instanceof Cloneable) {
            try {
                into=Methods.invoke(from, "clone");
            } catch (NoSuchMethodException ex) {
                into=from;
            }
        } else {


            into = from;
        }
        
        return into;
    }
    
    
    public boolean isFlag(BufferFlag flag) {
        return flags.contains(flag);
    }

    
    public void setFlag(BufferFlag flag) {
        setFlag(flag, true);
    }

    
    public void clearFlag(BufferFlag flag) {
        setFlag(flag, false);
    }

    
    public void setFlag(BufferFlag flag, boolean value) {
        if (value) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }

    
    public void setFlagsTo(BufferFlag... flags) {
        if (flags.length == 0) {
            this.flags = EnumSet.noneOf(BufferFlag.class);
        } else {
            this.flags = EnumSet.copyOf(Arrays.asList(flags));
        }
    }

    
    public void setFlagsTo(EnumSet<BufferFlag> flags) {
        if (flags == null) {
            this.flags = EnumSet.noneOf(BufferFlag.class);
        } else {
            this.flags = EnumSet.copyOf(flags);
        }
    }

    public void clearFlags() {
        flags.clear();
    }
}
