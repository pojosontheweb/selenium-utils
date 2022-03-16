
package org.monte.media.exif;

import org.monte.media.tiff.ASCIIValueFormatter;
import org.monte.media.tiff.TagSet;
import org.monte.media.tiff.*;
import static org.monte.media.tiff.TIFFTag.*;


public class InteroperabilityTagSet extends TagSet {

    private static InteroperabilityTagSet instance;

    private InteroperabilityTagSet(TIFFTag[] tags) {
        super("Interoperability", tags);
    }


    public static InteroperabilityTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {
    new TIFFTag("InteroperabilityIndex",0x1,SHORT_MASK),
    new TIFFTag("InteroperabilityVersion",0x2,SHORT_MASK, new ASCIIValueFormatter()),
    new TIFFTag("RelatedImageFileFormat",0x1000,SHORT_MASK),
    new TIFFTag("RelatedImageWidth",0x1001,SHORT_MASK),
    new TIFFTag("RelatedImageLength",0x1002,SHORT_MASK),
};
            instance = new InteroperabilityTagSet(tags);

        }
        return instance;
    }
}
