

package org.monte.media.tiff;


public interface ValueFormatter {

    public Object format(Object value);

    public Object prettyFormat(Object value);

    public String descriptionFormat(Object data);
}
