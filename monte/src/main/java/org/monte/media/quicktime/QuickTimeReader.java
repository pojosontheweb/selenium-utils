
package org.monte.media.quicktime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.MovieReader;
import org.monte.media.math.Rational;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import org.monte.media.Codec;
import org.monte.media.Registry;
import static org.monte.media.BufferFlag.*;


public class QuickTimeReader extends QuickTimeInputStream implements MovieReader {

      public final static Format QUICKTIME = new Format(MediaTypeKey,MediaType.FILE,MimeTypeKey,MIME_QUICKTIME);

    public QuickTimeReader(File file) throws IOException {
        super(file);
    }


    public QuickTimeReader(ImageInputStream in) throws IOException {
        super(in);
    }

    @Override
    public long timeToSample(int track, Rational seconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational sampleToTime(int track, long sample) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Format getFileFormat() throws IOException {
        return QUICKTIME;
    }

    @Override
    public Format getFormat(int track) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).sampleCount;
    }

    public BufferedImage read(int track, BufferedImage img) throws IOException {
        AbstractQuickTimeStream.Track tr = tracks.get(track);
        if (tr.inputBuffer == null) {
            tr.inputBuffer = new Buffer();
        }
        if (tr.codec == null) {
            createCodec(tr);
        }
        Buffer buf = new Buffer();
        buf.data = img;
        do {
            read(track, tr.inputBuffer);

            tr.codec.process(tr.inputBuffer, buf);
        } while (buf.isFlag(DISCARD) && !buf.isFlag(END_OF_MEDIA));

        if (tr.inputBuffer.isFlag(END_OF_MEDIA)) {
            return null;
        }

        return (BufferedImage) buf.data;
    }

    @Override
    public void read(int track, Buffer buffer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int nextTrack() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMovieReadTime(Rational newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getDuration() throws IOException {
        return new Rational(getMovieDuration(), getMovieTimeScale());
    }
    @Override
    public Rational getDuration(int track) throws IOException {
        ensureRealized();
        Track tr = tracks.get(track);

        Rational trackDuration = new Rational(tr.mediaDuration,tr.mediaTimeScale);
        return trackDuration;
    }

    @Override
    public int findTrack(int fromTrack, Format format) throws IOException {
        for (int i=fromTrack,n=getTrackCount();i<n;i++) {
            if (getFormat(i).matches(format)) {
                return i;
            }
        }
        return -1;
    }
    private void createCodec(AbstractQuickTimeStream.Track tr) {
        Format fmt = tr.format;
        Codec codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
        if (codec == null) {
            throw new UnsupportedOperationException("Track " + tr + " no codec found for format " + fmt);
        } else {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                if (null == codec.setInputFormat(fmt)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support input format " + fmt + ". codec=" + codec);
                }
                Format outFormat = fmt.prepend(MediaTypeKey, MediaType.VIDEO,
                        MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class);
                if (null == codec.setOutputFormat(outFormat)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support output format " + outFormat + ". codec=" + codec);
                }
            }
        }

        tr.codec = codec;
    }

    private Codec createCodec(Format fmt) {
        Codec[] codecs = Registry.getInstance().getDecoders(fmt.prepend(MimeTypeKey, MIME_AVI));
        return codecs.length == 0 ? null : codecs[0];
    }
}
