
package org.monte.media.avi;

import java.util.EnumSet;
import org.monte.media.math.Rational;
import org.monte.media.Format;
import org.monte.media.Codec;
import org.monte.media.Buffer;
import org.monte.media.MovieWriter;
import org.monte.media.Registry;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.riff.RIFFParser;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;
import javax.imageio.stream.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import org.monte.media.BufferFlag;
import static org.monte.media.BufferFlag.*;


public class AVIWriter extends AVIOutputStream implements MovieWriter {

    public final static Format AVI = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
    public final static Format VIDEO_RAW = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_DIB, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_JPEG = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_MJPG, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_PNG = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_PNG, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_RLE = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_RLE, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_SCREEN_CAPTURE = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);


    public AVIWriter(File file) throws IOException {
        super(file);
    }


    public AVIWriter(ImageOutputStream out) throws IOException {
        super(out);
    }

    @Override
    public Format getFileFormat() throws IOException {
        return AVI;
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }


    @Override
    public Rational getDuration(int track) {
        Track tr = tracks.get(track);
        long duration = getMediaDuration(track);
        return new Rational(duration * tr.scale, tr.rate);
    }


    @Override
    public int addTrack(Format format) throws IOException {
        if (format.get(MediaTypeKey) == MediaType.VIDEO) {
            return addVideoTrack(format);
        } else {
            return addAudioTrack(format);
        }
    }


    private int addVideoTrack(Format vf) throws IOException {
        if (!vf.containsKey(EncodingKey)) {
            throw new IllegalArgumentException("EncodingKey missing in " + vf);
        }
        if (!vf.containsKey(FrameRateKey)) {
            throw new IllegalArgumentException("FrameRateKey missing in " + vf);
        }
        if (!vf.containsKey(WidthKey)) {
            throw new IllegalArgumentException("WidthKey missing in " + vf);
        }
        if (!vf.containsKey(HeightKey)) {
            throw new IllegalArgumentException("HeightKey missing in " + vf);
        }
        if (!vf.containsKey(DepthKey)) {
            throw new IllegalArgumentException("DepthKey missing in " + vf);
        }
        int tr = addVideoTrack(vf.get(EncodingKey),
                vf.get(FrameRateKey).getDenominator(), vf.get(FrameRateKey).getNumerator(),
                vf.get(WidthKey), vf.get(HeightKey), vf.get(DepthKey),
                vf.get(FrameRateKey).floor(1).intValue());
        setCompressionQuality(tr, vf.get(QualityKey, 1.0f));
        return tr;
    }


    private int addAudioTrack(Format format) throws IOException {
        int waveFormatTag = 0x0001;


        long timeScale = 1;
        long sampleRate = format.get(SampleRateKey, new Rational(41000, 0)).longValue();
        int numberOfChannels = format.get(ChannelsKey, 1);
        int sampleSizeInBits = format.get(SampleSizeInBitsKey, 16);
        boolean isCompressed = false;
        int frameDuration = 1;
        int frameSize = format.get(FrameSizeKey, (sampleSizeInBits + 7) / 8 * numberOfChannels);


        String enc = format.get(EncodingKey);
        if (enc == null) {
            waveFormatTag = 0x0001;
        } else if (enc.equals(ENCODING_ALAW)) {
            waveFormatTag = 0x0001;
        } else if (enc.equals(ENCODING_PCM_SIGNED)) {
            waveFormatTag = 0x0001;
        } else if (enc.equals(ENCODING_PCM_UNSIGNED)) {
            waveFormatTag = 0x0001;
        } else if (enc.equals(ENCODING_ULAW)) {
            waveFormatTag = 0x0001;
        } else if (enc.equals(ENCODING_MP3)) {
            waveFormatTag = 0x0001;
        } else {
            waveFormatTag = RIFFParser.stringToID(format.get(EncodingKey)) & 0xffff;
        }

        return addAudioTrack(waveFormatTag,
                timeScale, sampleRate,
                numberOfChannels, sampleSizeInBits,
                isCompressed,
                frameDuration, frameSize);
    }


    public Codec getCodec(int track) {
        return tracks.get(track).codec;
    }


    public void setCodec(int track, Codec codec) {
        tracks.get(track).codec = codec;
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }


    public void write(int track, BufferedImage image, long duration) throws IOException {
        ensureStarted();

        VideoTrack vt = (VideoTrack) tracks.get(track);
        if (vt.codec == null) {
            createCodec(track);
        }
        if (vt.codec == null) {
            throw new UnsupportedOperationException("No codec for this format: " + vt.format);
        }


        Format fmt = vt.format;
        if (fmt.get(WidthKey) != image.getWidth() || fmt.get(HeightKey) != image.getHeight()) {
            throw new IllegalArgumentException("Dimensions of image[" + vt.samples.size()
                    + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                    + ") differs from video format of track: " + fmt);
        }


        {
            if (vt.outputBuffer == null) {
                vt.outputBuffer = new Buffer();
            }

            boolean isKeyframe = vt.syncInterval == 0 ? false : vt.samples.size() % vt.syncInterval == 0;

            Buffer inputBuffer = new Buffer();
            inputBuffer.flags = (isKeyframe) ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);
            inputBuffer.data = image;
            vt.codec.process(inputBuffer, vt.outputBuffer);
            if (vt.outputBuffer.flags.contains(DISCARD)) {
                return;
            }


            isKeyframe = vt.outputBuffer.flags.contains(KEYFRAME);
            boolean paletteChange = writePalette(track, image, isKeyframe);
            writeSample(track, (byte[]) vt.outputBuffer.data, vt.outputBuffer.offset, vt.outputBuffer.length, isKeyframe && !paletteChange);

        }
    }


    @Override
    public void write(int track, Buffer buf) throws IOException {
        ensureStarted();
        if (buf.flags.contains(DISCARD)) {
            return;
        }

        Track tr = tracks.get(track);

        boolean isKeyframe = buf.flags.contains(KEYFRAME);
        if (buf.data instanceof BufferedImage) {
            if (tr.syncInterval != 0) {
                isKeyframe = buf.flags.contains(KEYFRAME) | (tr.samples.size() % tr.syncInterval == 0);
            }
        }

        boolean paletteChange = false;
        if (buf.data instanceof BufferedImage && tr instanceof VideoTrack) {
            paletteChange = writePalette(track, (BufferedImage) buf.data, isKeyframe);
        } else if (buf.header instanceof IndexColorModel) {
            paletteChange = writePalette(track, (IndexColorModel) buf.header, isKeyframe);
        }

        {
            if (buf.format == null) {
                throw new IllegalArgumentException("Buffer.format must not be null");
            }
            if (buf.format.matchesWithout(tr.format, FrameRateKey) && buf.data instanceof byte[]) {
                writeSamples(track, buf.sampleCount, (byte[]) buf.data, buf.offset, buf.length,
                        buf.isFlag(KEYFRAME) && !paletteChange);
                return;
            }





            if (tr.codec == null) {
                createCodec(track);
                if (tr.codec == null) {
                    throw new UnsupportedOperationException("No codec for this format " + tr.format);
                }
            }

            if (tr.outputBuffer == null) {
                tr.outputBuffer = new Buffer();
            }
            Buffer outBuf = tr.outputBuffer;
            if (tr.codec.process(buf, outBuf) != Codec.CODEC_OK) {
                throw new IOException("Codec failed or could not encode the sample in a single step.");
            }
            if (outBuf.isFlag(DISCARD)) {
                return;
            }
            writeSamples(track, outBuf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length,
                    isKeyframe && !paletteChange);
        }
    }

    private boolean writePalette(int track, BufferedImage image, boolean isKeyframe) throws IOException {
        if ((image.getColorModel() instanceof IndexColorModel)) {
            return writePalette(track, (IndexColorModel) image.getColorModel(), isKeyframe);
        }
        return false;
    }

    private boolean writePalette(int track, IndexColorModel imgPalette, boolean isKeyframe) throws IOException {
        ensureStarted();

        VideoTrack vt = (VideoTrack) tracks.get(track);
        int imgDepth = vt.bitCount;
        ByteArrayImageOutputStream tmp = null;
        boolean paletteChange = false;
        switch (imgDepth) {
            case 4: {

                int[] imgRGBs = new int[16];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[16];
                if (vt.previousPalette == null) {
                    vt.previousPalette = vt.palette;
                }
                vt.previousPalette.getRGBs(previousRGBs);
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    vt.previousPalette = imgPalette;

                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;

                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first);
                    tmp.writeByte(last - first + 1);
                    tmp.writeShort(0);

                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff);
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff);
                        tmp.writeByte(imgRGBs[i] & 0xff);
                        tmp.writeByte(0);
                    }

                }
                break;
            }
            case 8: {

                int[] imgRGBs = new int[256];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[256];
                if (vt.previousPalette != null) {
                    vt.previousPalette.getRGBs(previousRGBs);
                }
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    vt.previousPalette = imgPalette;

                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;

                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first);
                    tmp.writeByte(last - first + 1);
                    tmp.writeShort(0);
                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff);
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff);
                        tmp.writeByte(imgRGBs[i] & 0xff);
                        tmp.writeByte(0);
                    }
                }

                break;
            }
        }
        if (tmp != null) {
            tmp.close();
            writePalette(track, tmp.toByteArray(), 0, (int) tmp.length(), isKeyframe);
        }
        return paletteChange;
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getEncoder(fmt.prepend(MimeTypeKey, MIME_AVI));
    }

    private void createCodec(int track) {
        Track tr = tracks.get(track);
        Format fmt = tr.format;
        tr.codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
        if (tr.codec != null) {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                tr.codec.setInputFormat(fmt.prepend(
                        EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                        QualityKey, getCompressionQuality(track),
                        MimeTypeKey, MIME_AVI,
                        DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support format " + fmt + ". codec=" + tr.codec);
                }
            } else {
                tr.codec.setInputFormat(null);
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                        QualityKey, getCompressionQuality(track),
                        MimeTypeKey, MIME_AVI,
                        DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec " + tr.codec + " does not support format. " + fmt);
                }
            }
        }
    }

    public boolean isVFRSupported() {
        return false;
    }

    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).samples.isEmpty();
    }
}
