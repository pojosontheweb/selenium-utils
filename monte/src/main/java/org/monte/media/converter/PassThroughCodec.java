
package org.monte.media.converter;

import org.monte.media.AbstractCodec;
import org.monte.media.Buffer;
import org.monte.media.BufferFlag;
import org.monte.media.Format;
import org.monte.media.math.Rational;


public class PassThroughCodec extends AbstractCodec {

    public PassThroughCodec() {
        super(new Format[]{
                    new Format(),
                },
                new Format[]{
                    new Format(),
                });
        name = "Pass Through";
    }

    @Override
    public Format setInputFormat(Format f) {
        Format fNew= super.setInputFormat(f);
        outputFormat=fNew;
        return fNew;
    }



    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.setDataTo(in);
        return CODEC_OK;
    }
}
