
package org.monte.media.tiff;

import java.util.HashMap;


public abstract class TagSet {

    private HashMap<Integer, TIFFTag> tagsByNumber = new HashMap<Integer, TIFFTag>();
    private String name;

    public TagSet(String name, TIFFTag[] tags) {
        this.name = name;
        for (TIFFTag tag : tags) {
            tag.setTagSet(this);
            tagsByNumber.put(tag.getNumber(), tag);
        }
    }


    public TIFFTag getTag(int tagNumber) {
        TIFFTag tag=tagsByNumber.get(tagNumber);
        if (tag==null) {
            synchronized (this) {
                tag=tagsByNumber.get(tagNumber);
                if (tag==null) {
                    tag=new TIFFTag("unknown",tagNumber,TIFFTag.ALL_MASK,null);
                    tagsByNumber.put(tagNumber, tag);
                }
            }
        }
        return tag;
    }


    public String getName() {
        return name;
    }
}
