
package org.monte.media.tiff;

import java.io.UnsupportedEncodingException;


public class ASCIIValueFormatter implements ValueFormatter  {


    public ASCIIValueFormatter() {
    }

    @Override
    public Object format(Object value) {
        if (value instanceof byte[]) {
            try {
                return new String((byte[]) value, "ASCII");
            } catch (UnsupportedEncodingException ex) {
                throw new InternalError("ASCII not supported");
            }
            }
        return value;
    }
    @Override
    public Object prettyFormat(Object value) {
        return format(value);
    }

    @Override
    public String descriptionFormat(Object data) {
       return null;
    }
}
