
package org.monte.media.tiff;

import java.util.ArrayList;


public class TIFFDirectory extends TIFFNode {
    
    private TagSet tagSet;
    
    private int index;


    
    private IFD ifd;
    
    private ArrayList<FileSegment> fileSegments;
    
    private long offset;
    
    private long length;

    
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index) {
        super(tag);
        this.tagSet=tagSet;
        this.index=index;
    }
    
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, IFD ifd, IFDEntry parentEntry, ArrayList<FileSegment> fileSegments) {
        this(tagSet, tag, index);
        this.ifd=ifd;
        this.ifdEntry=parentEntry;
        this.fileSegments=fileSegments;
    }
    
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, IFD ifd, IFDEntry parentEntry, FileSegment fileSegment) {
        this(tagSet, tag, index);
        this.ifd=ifd;
        this.ifdEntry=parentEntry;
        this.fileSegments=new ArrayList<FileSegment>();
        fileSegments.add(fileSegment);
    }

    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, long offset, long length, FileSegment fileSegment) {
        this(tagSet, tag, index);
        this.offset=offset;
        this.length=length;
        this.fileSegments=new ArrayList<FileSegment>();
        fileSegments.add(fileSegment);
    }
    public TIFFDirectory(TagSet tagSet, TIFFTag tag, int index, long offset, long length, ArrayList<FileSegment> fileSegments) {
        this(tagSet, tag, index);
        this.offset=offset;
        this.length=length;
        this.fileSegments=fileSegments;
    }

    
    public IFD getIFD() {
        return ifd;
    }

    
    public TagSet getTagSet() {
        return tagSet;
    }

    public String getName() {
        return tagSet==null?null: tagSet.getName();
    }

    public int getIndex() {
        return index;
    }
    public int getCount() {
        return getChildren().size();
    }

    public long getOffset() {
        return ifd!=null?ifd.getOffset():offset;
    }
    public long getLength() {
        return ifd!=null?ifd.getLength():length;
    }

    
    public ArrayList<FileSegment> getFileSegments() {
        return fileSegments;
    }

    
    public TIFFField getField(TIFFTag tag) {
        for (TIFFNode node:getChildren()) {
            if (node instanceof TIFFField) {
                if (node.getTag()==tag) {
                    return (TIFFField) node;
                }
            }
        }
        return null;
    }
    
    public Object getData(TIFFTag tag) {
        TIFFField field=getField(tag);
        return field==null?null:field.getData();
    }

    @Override
    public String toString() {
        return "TIFFDirectory "+tagSet;
    }

}
