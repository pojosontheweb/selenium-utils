
package org.monte.media;

import java.util.ArrayList;

public abstract class AbstractCodec implements Codec {

    protected Format[] inputFormats;
    protected Format[] outputFormats;
    protected Format inputFormat;
    protected Format outputFormat;
    protected String name="unnamed codec";

    public AbstractCodec(Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        this.inputFormats = supportedInputFormats;
        this.outputFormats = supportedOutputFormats;
    }
    public AbstractCodec(Format[] supportedInputOutputFormats) {
        this.inputFormats = supportedInputOutputFormats;
        this.outputFormats = supportedInputOutputFormats;
    }

    @Override
    public Format[] getInputFormats() {
        return inputFormats.clone();
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        ArrayList<Format>of=new ArrayList<Format>(outputFormats.length);
        for (Format f:outputFormats) {
            of.add(input==null?f:f.append(input));
        }
        return of.toArray(new Format[of.size()]);
    }

    @Override
    public Format setInputFormat(Format f) {
        if (f!=null)
        for (Format sf : getInputFormats()) {
            if (sf.matches(f)) {
                this.inputFormat = sf.append(f);
                return inputFormat;
            }
        }
        this.inputFormat=null;
        return null;
    }

    @Override
    public Format setOutputFormat(Format f) {
        for (Format sf : getOutputFormats(f)) {
            if (sf.matches(f)) {
                this.outputFormat = f;
                return sf;
            }
        }
        this.outputFormat=null;
        return null;
    }

    @Override
    public Format getInputFormat() {
        return inputFormat;
    }

    @Override
    public Format getOutputFormat() {
        return outputFormat;
    }

    @Override
    public String getName() {
        return name;
    }

    
    @Override
    public void reset() {

    }

    @Override
    public String toString() {
        String className=getClass().getName();
        int p=className.lastIndexOf('.');
        return className.substring(p+1)+"{" + "inputFormat=" + inputFormat + ", outputFormat=" + outputFormat+'}';
    }
    
    
}
