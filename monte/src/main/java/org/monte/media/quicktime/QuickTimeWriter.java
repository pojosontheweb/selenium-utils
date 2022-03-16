
package org.monte.media.quicktime;

import org.monte.media.Registry;
import org.monte.media.Format;
import org.monte.media.Codec;
import org.monte.media.Buffer;
import org.monte.media.MovieWriter;
import org.monte.media.math.Rational;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.*;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.BufferFlag.*;

public class QuickTimeWriter extends QuickTimeOutputStream implements MovieWriter {

    public final static Format QUICKTIME = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME);
    public final static Format VIDEO_RAW = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_RAW,//
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_ANIMATION = new Format(
            MediaTypeKey, MediaType.VIDEO, //
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_ANIMATION, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_ANIMATION);
    public final static Format VIDEO_JPEG = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_JPEG, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG);
    public final static Format VIDEO_PNG = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_PNG, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_PNG);

    public QuickTimeWriter(File file) throws IOException {
        super(file);
    }

    public QuickTimeWriter(ImageOutputStream out) throws IOException {
        super(out);
    }

    @Override
    public Format getFileFormat() throws IOException {
        return QUICKTIME;
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }

    @Override
    public int addTrack(Format fmt) throws IOException {
        if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
            int t= addVideoTrack(fmt.get(EncodingKey),
                    fmt.get(CompressorNameKey,fmt.get(EncodingKey)),
                    Math.min(6000,fmt.get(FrameRateKey).getNumerator() * fmt.get(FrameRateKey).getDenominator()),
                    fmt.get(WidthKey), fmt.get(HeightKey), fmt.get(DepthKey),
                    (int) fmt.get(FrameRateKey).getDenominator());
            setCompressionQuality(t,fmt.get(QualityKey,1.0f));
            return t;
        } else if (fmt.get(MediaTypeKey) == MediaType.AUDIO) {
            // fill in unspecified values
            int sampleSizeInBits = fmt.get(SampleSizeInBitsKey, 16);
            ByteOrder bo = fmt.get(ByteOrderKey, ByteOrder.BIG_ENDIAN);
            boolean signed = fmt.get(SignedKey, true);
            String encoding = fmt.get(EncodingKey, null);
            Rational frameRate = fmt.get(FrameRateKey, fmt.get(SampleRateKey));
            int channels = fmt.get(ChannelsKey, 1);
            int frameSize = fmt.get(FrameSizeKey, (sampleSizeInBits + 7) / 8 * sampleSizeInBits);
            if (encoding == null||encoding.length()!=4) {
                if (signed) {
                    encoding = bo == ByteOrder.BIG_ENDIAN ? "twos" : "sowt";
                } else {
                    encoding = "raw ";
                }
            }

            return addAudioTrack(encoding,
                    fmt.get(SampleRateKey).longValue(),
                    fmt.get(SampleRateKey).doubleValue(),
                    channels,
                    sampleSizeInBits,
                    false, // FIXME - We should support compressed formats
                    fmt.get(SampleRateKey).divide(frameRate).intValue(),
                    frameSize,
                    signed,
                    bo);
            //return addAudioTrack(AudioFormatKeys.toAudioFormat(fmt)); // FIXME Add direct support for AudioFormat
        } else {
            throw new IOException("Unsupported media type:" + fmt.get(MediaTypeKey));
        }
    }

    @Deprecated
    public int addVideoTrack(Format format, long timeScale, int width, int height) throws IOException {
        return addVideoTrack(format.get(EncodingKey), format.get(CompressorNameKey), timeScale, width, height, 24, 30);
    }

    @Deprecated
    public int addVideoTrack(Format format, int width, int height, int depth, int syncInterval) throws IOException {
        return addVideoTrack(format.get(EncodingKey), format.get(CompressorNameKey), format.get(FrameRateKey).getDenominator() * format.get(FrameRateKey).getNumerator(), width, height, depth, syncInterval);
    }

    @Deprecated
    public int addAudioTrack(javax.sound.sampled.AudioFormat format) throws IOException {
        ensureStarted();
        String qtAudioFormat;
        double sampleRate = format.getSampleRate();
        long timeScale = (int) Math.floor(sampleRate);
        int sampleSizeInBits = format.getSampleSizeInBits();
        int numberOfChannels = format.getChannels();
        ByteOrder byteOrder = format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        int frameDuration = (int) (format.getSampleRate() / format.getFrameRate());
        int frameSize = format.getFrameSize();
        boolean isCompressed = format.getProperty("vbr") != null && ((Boolean) format.getProperty("vbr")).booleanValue();
        boolean signed = false;
        javax.sound.sampled.AudioFormat.Encoding enc = format.getEncoding();
        if (enc.equals(javax.sound.sampled.AudioFormat.Encoding.ALAW)) {
            qtAudioFormat = "alaw";
            if (sampleSizeInBits != 8) {
                throw new IllegalArgumentException("Sample size of 8 for ALAW required:" + sampleSizeInBits);
            }
        } else if (javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.equals(enc)) {
            switch (sampleSizeInBits) {
                case 8:// Requires conversion to PCM_UNSIGNED!
                    qtAudioFormat = "raw ";
                    break;
                case 16:
                    qtAudioFormat = (byteOrder == ByteOrder.BIG_ENDIAN) ? "twos" : "sowt";
                    break;
                case 24:
                    qtAudioFormat = "in24";
                    break;
                case 32:
                    qtAudioFormat = "in32";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported sample size for PCM_SIGNED:" + sampleSizeInBits);
            }
        } else if (javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED.equals(enc)) {
            switch (sampleSizeInBits) {
                case 8:
                    qtAudioFormat = "raw ";
                    break;
                case 16:// Requires conversion to PCM_SIGNED!
                    qtAudioFormat = (byteOrder == ByteOrder.BIG_ENDIAN) ? "twos" : "sowt";
                    break;
                case 24:// Requires conversion to PCM_SIGNED!
                    qtAudioFormat = "in24";
                    break;
                case 32:// Requires conversion to PCM_SIGNED!
                    qtAudioFormat = "in32";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported sample size for PCM_UNSIGNED:" + sampleSizeInBits);
            }
        } else if (javax.sound.sampled.AudioFormat.Encoding.ULAW.equals(enc)) {
            if (sampleSizeInBits != 8) {
                throw new IllegalArgumentException("Sample size of 8 for ULAW required:" + sampleSizeInBits);
            }
            qtAudioFormat = "ulaw";
        } else if ("MP3".equals(enc == null ? null : enc.toString())) {
            qtAudioFormat = ".mp3";
        } else {
            qtAudioFormat = format.getEncoding().toString();
            if (qtAudioFormat == null || qtAudioFormat.length() != 4) {
                throw new IllegalArgumentException("Unsupported encoding:" + format.getEncoding());
            }
        }

        return addAudioTrack(qtAudioFormat, timeScale, sampleRate,
                numberOfChannels, sampleSizeInBits,
                isCompressed, frameDuration, frameSize, signed, byteOrder);
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    @Override
    public Rational getDuration(int track) {
        Track tr = tracks.get(track);
        return new Rational(tr.mediaDuration, tr.mediaTimeScale);
    }

    private Codec createCodec(Format fmt) {
        Codec[] codecs = Registry.getInstance().getEncoders(fmt.prepend(MimeTypeKey, MIME_QUICKTIME));
        Codec c= codecs.length == 0 ? null : codecs[0];
        return c;
    }

    private void createCodec(int track) {
        Track tr=tracks.get(track);
        Format fmt = tr.format;
        tr.codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
        if (tr.codec != null) {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                Format vf = (Format) fmt;
                tr.codec.setInputFormat(fmt.prepend(
                        MimeTypeKey, MIME_JAVA, EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));

                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(
                        QualityKey, getCompressionQuality(track),
                        MimeTypeKey, MIME_QUICKTIME,
                        DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Input format not supported:" + fmt);
                }
                //tr.codec.setQuality(tr.videoQuality);
            } else {
                Format vf = (Format) fmt;
                tr.codec.setInputFormat(fmt.prepend(
                        MimeTypeKey, MIME_JAVA, EncodingKey, fmt.containsKey(SignedKey) && fmt.get(SignedKey) ? ENCODING_PCM_SIGNED : ENCODING_PCM_UNSIGNED,
                        DataClassKey, byte[].class));
                if (tr.codec.setOutputFormat(fmt) == null) {
                    throw new UnsupportedOperationException("Codec output format not supported:" + fmt + " codec:" + tr.codec);
                } else {
                    tr.format = tr.codec.getOutputFormat();
                }
                //tr.codec.setQuality(tr.dwQuality);
            }
        }
    }

    public Codec getCodec(int track) {
        return tracks.get(track).codec;
    }

    public void setCodec(int track, Codec codec) {
        tracks.get(track).codec = codec;
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        ensureStarted();
        Track tr = tracks.get(track);

        // Encode sample data
        {
            if (tr.outputBuffer == null) {
                tr.outputBuffer = new Buffer();
                tr.outputBuffer.format = tr.format;
            }
            Buffer outBuf;
            if (tr.format.matchesWithout(buf.format,FrameRateKey)) {
                outBuf = buf;
            } else {
                outBuf = tr.outputBuffer;
                boolean isSync = tr.syncInterval == 0 ? false : tr.sampleCount % tr.syncInterval == 0;
                buf.setFlag(KEYFRAME, isSync);
                if (tr.codec == null) {
                    createCodec(track);
                    if (tr.codec == null) {
                        throw new UnsupportedOperationException("No codec for this format " + tr.format);
                    }
                }

                tr.codec.process(buf, outBuf);
            }
            if (outBuf.isFlag(DISCARD)||outBuf.sampleCount==0) {
                return;
            }

            // Compute sample sampleDuration in media time scale
            Rational sampleDuration;
            if (tr.inputTime == null) {
                tr.inputTime = new Rational(0, 1);
                tr.writeTime = new Rational(0, 1);
            }
            tr.inputTime = tr.inputTime.add(outBuf.sampleDuration.multiply(outBuf.sampleCount));
            Rational exactSampleDuration = tr.inputTime.subtract(tr.writeTime);
            sampleDuration = exactSampleDuration.floor(tr.mediaTimeScale);
            if (sampleDuration.compareTo(new Rational(0, 1)) <= 0) {
                sampleDuration = new Rational(1, tr.mediaTimeScale);
            }
            tr.writeTime = tr.writeTime.add(sampleDuration);
            long sampleDurationInMediaTS = sampleDuration.getNumerator() * (tr.mediaTimeScale / sampleDuration.getDenominator());

            writeSamples(track, buf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length,
                    sampleDurationInMediaTS / buf.sampleCount, outBuf.isFlag(KEYFRAME));
        }
    }

    public void write(int track, BufferedImage image, long duration) throws IOException {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater 0.");
        }
        VideoTrack vt = (VideoTrack) tracks.get(track); // throws index out of bounds exception if illegal track index
        if (vt.mediaType != MediaType.VIDEO) {
            throw new IllegalArgumentException("Track " + track + " is not a video track");
        }
        if (vt.codec == null) {
            createCodec(track);
        }
        if (vt.codec == null) {
            throw new UnsupportedOperationException("No codec for this format: " + vt.format);
        }
        ensureStarted();

        // Get the dimensions of the first image
        if (vt.width == -1) {
            vt.width = image.getWidth();
            vt.height = image.getHeight();
        } else {
            // The dimension of the image must match the dimension of the video track
            if (vt.width != image.getWidth() || vt.height != image.getHeight()) {
                throw new IllegalArgumentException("Dimensions of frame[" + tracks.get(track).getSampleCount()
                        + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                        + ") differs from video dimension (width="
                        + vt.width + ", height=" + vt.height + ") in track " + track + ".");
            }
        }

        // Encode pixel data
        {

            if (vt.outputBuffer == null) {
                vt.outputBuffer = new Buffer();
            }

            boolean isSync = vt.syncInterval == 0 ? false : vt.sampleCount % vt.syncInterval == 0;

            Buffer inputBuffer = new Buffer();
            inputBuffer.setFlag(KEYFRAME, isSync);
            inputBuffer.data = image;
            vt.codec.process(inputBuffer, vt.outputBuffer);
            if (vt.outputBuffer.isFlag(DISCARD)) {
                return;
            }

            isSync = vt.outputBuffer.isFlag(KEYFRAME);

            long offset = getRelativeStreamPosition();
            OutputStream mdatOut = mdatAtom.getOutputStream();
            mdatOut.write((byte[]) vt.outputBuffer.data, vt.outputBuffer.offset, vt.outputBuffer.length);

            long length = getRelativeStreamPosition() - offset;
            vt.addSample(new Sample(duration, offset, length), 1, isSync);
        }
    }

    @Deprecated
    public void write(int track, byte[] data, int off, int len, long duration, boolean isSync) throws IOException {
        writeSamples(track, 1, data, off, len, duration, isSync);
    }

    @Deprecated
    public void write(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync) throws IOException {
        Track tr = tracks.get(track);
        if (tr.codec == null) {
            writeSamples(track, sampleCount, data, off, len, sampleDuration, isSync);
        } else {
            if (tr.outputBuffer == null) {
                tr.outputBuffer = new Buffer();
            }
            if (tr.inputBuffer == null) {
                tr.inputBuffer = new Buffer();
            }
            Buffer outb = tr.outputBuffer;
            Buffer inb = tr.inputBuffer;
            inb.data = data;
            inb.offset = off;
            inb.length = len;
            inb.sampleDuration = new Rational(sampleDuration, tr.mediaTimeScale);
            inb.sampleCount = sampleCount;
            inb.setFlag(KEYFRAME, isSync);
            tr.codec.process(inb, outb);
            if (!outb.isFlag(DISCARD)) {
                writeSample(track, (byte[]) outb.data, outb.offset, outb.length, outb.sampleCount, outb.isFlag(KEYFRAME));
            }
        }
    }

    public boolean isVFRSupported() {
        return true;
    }

    @Override
    public boolean isDataLimitReached() {
        return super.isDataLimitReached();
    }
    @Override
    public boolean isEmpty(int track) {
       return tracks.get(track).isEmpty();
    }
}
