
package org.monte.media.anim;

import org.monte.media.Buffer;
import org.monte.media.Multiplexer;
import org.monte.media.image.BitmapImage;
import org.monte.media.math.Rational;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;
import static org.monte.media.BufferFlag.*;


public class ANIMMultiplexer extends ANIMOutputStream implements Multiplexer {

    protected Rational inputTime;

    public ANIMMultiplexer(File file) throws IOException {
        super(file);
    }

    @Override
    public void write(int trackIndex, Buffer buf) throws IOException {
        if (!buf.isFlag(DISCARD)) {




            long jiffies = getJiffies();

            if (inputTime == null) {
                inputTime = new Rational(0, 1);
            }
            inputTime=inputTime.add(buf.sampleDuration.multiply(buf.sampleCount));
            
            Rational outputTime = new Rational(getMovieTime(), jiffies);
            Rational outputDuration = inputTime.subtract(outputTime);


            outputDuration = outputDuration.round(jiffies);
            int outputMediaDuration =max(1,(int)( outputDuration.getNumerator() *jiffies/outputDuration.getDenominator()));

            outputTime=
            outputTime.add(new Rational(outputMediaDuration,jiffies));


            writeFrame((BitmapImage) buf.data, (int) outputMediaDuration);
        }
    }
}
