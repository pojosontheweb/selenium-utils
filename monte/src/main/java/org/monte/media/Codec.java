
package org.monte.media;


public interface Codec {


    public final static int CODEC_OK = 0;

    public final static int CODEC_FAILED = 1;

    public final static int CODEC_INPUT_NOT_CONSUMED = 2;

    public final static int CODEC_OUTPUT_NOT_FILLED = 4;


    public Format[] getInputFormats();


    public Format[] getOutputFormats(Format input);


    public Format setInputFormat(Format input);

    public Format getInputFormat();


    public Format setOutputFormat(Format output);

    public Format getOutputFormat();


    public int process(Buffer in, Buffer out);


    public String getName();


    public void reset();
}
