/*
 * @(#)AbstractVideoDecoder.java 
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.jmf.codec.video;

import java.util.Arrays;
import org.monte.media.jmf.codec.AbstractCodec;
import javax.media.Format;
import javax.media.format.VideoFormat;

/**
 * {@code AbstractVideoDecoder}.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractVideoDecoder.java 304 2013-01-03 07:45:40Z werner $
 */
public abstract class AbstractVideoDecoder extends AbstractCodec {

    protected VideoFormat[] defaultOutputFormats=new VideoFormat[0];
    protected VideoFormat[] supportedInputFormats=new VideoFormat[0];
    protected VideoFormat inputFormat;
    protected VideoFormat outputFormat;
 
   
    
    
    @Override
    public Format[] getSupportedInputFormats() {
        return supportedInputFormats.clone();
    }

    @Override
    public Format[] getSupportedOutputFormats(Format input) {
        if (input==null) {
            return defaultOutputFormats.clone();
        }
        
        Format[] sop=getMatchingOutputFormats(input);
        return sop;
    }

    protected abstract Format[] getMatchingOutputFormats(Format input);

    @Override
    public Format setInputFormat(Format format) {
        inputFormat = (VideoFormat) format;
        return inputFormat;
    }

    @Override
    public Format setOutputFormat(Format format) {
        Format[] ops=getSupportedOutputFormats(inputFormat);
        
        outputFormat=null;
        for (Format f:ops) {
            if (f.matches(format)) {
                outputFormat=(VideoFormat)f;
                break;
            }
        }
        return outputFormat;
    }

    protected VideoFormat getInputFormat() {
        return inputFormat;
    }

    protected VideoFormat getOutputFormat() {
        return outputFormat;
    }

}
