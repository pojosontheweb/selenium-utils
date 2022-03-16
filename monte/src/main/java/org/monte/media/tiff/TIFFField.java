
package org.monte.media.tiff;

import java.awt.image.BufferedImage;


public class TIFFField extends TIFFNode {


    private Object data;

    public TIFFField(TIFFTag tag, Object data) {
        super(tag);
        this.data = data;
    }

    public TIFFField(TIFFTag tag, Object data, IFDEntry entry) {
        super(tag);
        this.data = data;
        this.ifdEntry = entry;
    }


    public String getDescription() {
        return getTag().getDescription(getData());
    }

    public IFDDataType getType() {
        if (ifdEntry != null) {
            return IFDDataType.valueOf(ifdEntry.getTypeNumber());
        } else {
            return getTag().getType(data);
        }
    }

    public long getCount() {
        if (ifdEntry != null) {
            return ifdEntry.getCount();
        } else if (data instanceof Object[]) {
            return ((Object[]) data).length;
        } else {
            return 1;
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (data==null) return super.toString();
        return "TIFFField "+tag+"="+ data.toString();
    }
}
