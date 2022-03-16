

package org.monte.media.exif;

import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;


public class ImageTagSet extends TagSet {
    private static ImageTagSet instance;

    public static ImageTagSet getInstance() {
        if (instance==null) {
            instance=new ImageTagSet();
        }
        return instance;
    }



    private ImageTagSet() {
        super("Image",new TIFFTag[0]);
    }

}
