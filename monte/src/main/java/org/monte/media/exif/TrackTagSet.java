

package org.monte.media.exif;

import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;


public class TrackTagSet extends TagSet {
    private static TrackTagSet instance;

    public static TrackTagSet getInstance() {
        if (instance==null) {
            instance=new TrackTagSet();
        }
        return instance;
    }



    private TrackTagSet() {
        super("Image",new TIFFTag[0]);
    }

}
