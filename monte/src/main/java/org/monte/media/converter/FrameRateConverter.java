
package org.monte.media.converter;

import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.Format;
import java.util.ArrayList;
import org.monte.media.math.Rational;
import java.awt.image.BufferedImage;
import static java.lang.Math.*;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;


public class FrameRateConverter extends AbstractVideoCodec {

    private Rational inputTime;
    private Rational outputTime;

    public FrameRateConverter() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO,
                            EncodingKey,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class),
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO,
                            EncodingKey,ENCODING_BUFFERED_IMAGE,
                            DataClassKey, BufferedImage.class,
                            FixedFrameRateKey,false),
                });
        name = "Frame Rate Converter";
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        Format forceVFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,false);

        ArrayList<Format> of = new ArrayList<Format>(outputFormats.length);
        for (Format f : outputFormats) {
            of.add(forceVFR.append(f.append(input)));
        }
        return of.toArray(new Format[of.size()]);
    }

    @Override
    public Format setOutputFormat(Format f) {
        Format forceFFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,true);
        Format forceVFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,false);

        for (Format sf : getOutputFormats(f)) {
            if (sf.matches(f)
                    || forceFFR.append(sf).matches(f)
                    || forceVFR.append(sf).matches(f)) {
                this.outputFormat = forceVFR.append(f);
                return sf;
            }
        }
        this.outputFormat = null;
        return null;
    }

    @Override
    public void reset() {
        inputTime = null;
        outputTime = null;
    }

    @Override
    public int process(Buffer in, Buffer out) {

        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }


        if (inputTime == null) {
            inputTime = new Rational(0, 1);
            outputTime = new Rational(0, 1);
        }


        Format vf = (Format) outputFormat;
        inputTime = inputTime.add(in.sampleDuration);
        Rational outputDuration = inputTime.subtract(outputTime);
        long jiffies = vf.get(FrameRateKey).getNumerator();
        outputDuration = outputDuration.round(jiffies);
        long outputMediaDuration = (int) (outputDuration.getNumerator() * jiffies / outputDuration.getDenominator());
        long remainder = outputMediaDuration % vf.get(FrameRateKey).getDenominator();
        outputDuration = new Rational(outputMediaDuration, jiffies);


        if (outputDuration.isLessOrEqualZero()) {
            out.setFlag(DISCARD, true);
        out.sampleDuration = outputDuration;

            return CODEC_OK;
        }


        out.format = outputFormat;
        out.setDataTo(in);
        out.timeStamp = outputTime;
        out.sampleDuration = outputDuration;
        outputTime = outputTime.add(outputDuration);




        return CODEC_OK;
    }
}
