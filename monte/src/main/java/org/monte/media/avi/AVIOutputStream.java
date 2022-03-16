
package org.monte.media.avi;

import java.awt.image.ColorModel;
import org.monte.media.riff.RIFFChunk;
import org.monte.media.math.Rational;
import java.util.ArrayList;
import org.monte.media.Format;
import org.monte.media.riff.RIFFParser;
import java.awt.Dimension;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.*;
import static java.lang.Math.*;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;


public class AVIOutputStream extends AbstractAVIStream {

    
    protected static enum States {

        STARTED, FINISHED, CLOSED;
    }
    
    protected States state = States.FINISHED;
    
    protected CompositeChunk aviChunk;
    
    protected CompositeChunk moviChunk;
    
    protected FixedSizeDataChunk avihChunk;
    ArrayList<Sample> idx1 = new ArrayList<Sample>();

    
    public AVIOutputStream(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        this.out = new FileImageOutputStream(file);
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.streamOffset = 0;
    }

    
    public AVIOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        this.streamOffset = out.getStreamPosition();
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    
    public int addVideoTrack(String fccHandler, long scale, long rate, int width, int height, int depth, int syncInterval) throws IOException {
        ensureFinished();
        if (fccHandler == null || fccHandler.length() != 4) {
            throw new IllegalArgumentException("fccHandler must be 4 characters long:" + fccHandler);
        }
        VideoTrack vt = new VideoTrack(tracks.size(), typeToInt(fccHandler),
                new Format(MediaTypeKey, MediaType.VIDEO,
                MimeTypeKey, MIME_AVI,
                EncodingKey, fccHandler,
                DataClassKey, byte[].class,
                WidthKey, width, HeightKey, height, DepthKey, depth,
                FixedFrameRateKey, true,
                FrameRateKey, new Rational(rate, scale)));
        vt.scale = scale;
        vt.rate = rate;
        vt.syncInterval = syncInterval;
        vt.frameLeft = 0;
        vt.frameTop = 0;
        vt.frameRight = width;
        vt.frameBottom = height;
        vt.bitCount = depth;
        vt.planes = 1;

        if (depth == 4) {
            byte[] gray = new byte[16];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) ((i << 4) | i);
            }
            vt.palette = new IndexColorModel(4, 16, gray, gray, gray);
        } else if (depth == 8) {
            byte[] gray = new byte[256];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) i;
            }
            vt.palette = new IndexColorModel(8, 256, gray, gray, gray);
        }

        tracks.add(vt);
        return tracks.size() - 1;
    }

    
    public int addAudioTrack(int waveFormatTag,
            long scale, long rate,
            int numberOfChannels, int sampleSizeInBits,
            boolean isCompressed,
            int frameDuration, int frameSize) throws IOException {
        ensureFinished();

        if (scale < 1 || scale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + scale);
        }
        if (numberOfChannels != 1 && numberOfChannels != 2) {
            throw new IllegalArgumentException("numberOfChannels must be 1 or 2: " + numberOfChannels);
        }
        if (sampleSizeInBits != 8 && sampleSizeInBits != 16) {
            throw new IllegalArgumentException("sampleSize must be 8 or 16: " + numberOfChannels);
        }

        AudioTrack t = new AudioTrack(tracks.size(), typeToInt("\u0000\u0000\u0000\u0000"));
        t.wFormatTag = waveFormatTag;

        float afSampleRate = (float) rate / (float) scale;

        t.format = new Format(MediaTypeKey, MediaType.AUDIO,
                MimeTypeKey, MIME_AVI,
                EncodingKey, RIFFParser.idToString(waveFormatTag),
                SampleRateKey, Rational.valueOf(afSampleRate),
                SampleSizeInBitsKey, sampleSizeInBits,
                ChannelsKey, numberOfChannels,
                FrameSizeKey, frameSize,
                FrameRateKey, Rational.valueOf(afSampleRate),
                SignedKey, sampleSizeInBits != 8,
                ByteOrderKey, ByteOrder.LITTLE_ENDIAN);

        t.scale = scale;
        t.rate = rate;
        t.samplesPerSec = rate / scale;
        t.channels = numberOfChannels;
        t.avgBytesPerSec = t.samplesPerSec * frameSize;
        t.blockAlign = t.channels * sampleSizeInBits / 8;
        t.bitsPerSample = sampleSizeInBits;
        tracks.add(t);
        return tracks.size() - 1;
    }

    
    public void setPalette(int track, ColorModel palette) {
        if (palette instanceof IndexColorModel) {
            ((VideoTrack) tracks.get(track)).palette = (IndexColorModel) palette;
        }
    }

    
    public Dimension getVideoDimension(int track) {
        Track tr = tracks.get(track);
        if (tr instanceof VideoTrack) {
            VideoTrack vt = (VideoTrack) tr;
            Format fmt = vt.format;
            return new Dimension(fmt.get(WidthKey), fmt.get(HeightKey));
        } else {
            return new Dimension(0, 0);
        }
    }

    
    public void putExtraHeader(int track, String fourcc, byte[] data) throws IOException {
        if (state == States.STARTED) {
            throw new IllegalStateException("Stream headers have already been written!");
        }
        Track tr = tracks.get(track);
        int id = RIFFParser.stringToID(fourcc);

        for (int i = tr.extraHeaders.size() - 1; i >= 0; i--) {
            if (tr.extraHeaders.get(i).getID() == id) {
                tr.extraHeaders.remove(i);
            }
        }


        RIFFChunk chunk = new RIFFChunk(STRH_ID, id, data.length, -1);
        chunk.setData(data);
        tr.extraHeaders.add(chunk);
    }

    
    public String[] getExtraHeaderFourCCs(int track) throws IOException {
        Track tr = tracks.get(track);
        String[] fourccs = new String[tr.extraHeaders.size()];
        for (int i = 0; i < fourccs.length; i++) {
            fourccs[i] = RIFFParser.idToString(tr.extraHeaders.get(i).getID());
        }
        return fourccs;
    }

    public void setName(int track, String name) {
        tracks.get(track).name = name;
    }

    
    public void setCompressionQuality(int track, float newValue) {
        VideoTrack vt = (VideoTrack) tracks.get(track);
        vt.videoQuality = newValue;
    }

    
    public float getCompressionQuality(int track) {
        return ((VideoTrack) tracks.get(track)).videoQuality;
    }    
    protected void ensureStarted() throws IOException {
        if (state != States.STARTED) {
            writeProlog();
            state = States.STARTED;
        }
    }

    
    protected void ensureFinished() throws IOException {
        if (state != States.FINISHED) {
            throw new IllegalStateException("Writer is in illegal state for this operation.");
        }
    }

    
    public void writePalette(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        Track tr = tracks.get(track);
        if (!(tr instanceof VideoTrack)) {
            throw new IllegalArgumentException("Error: track " + track + " is not a video track.");
        }
        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.");
        }

        VideoTrack vt = (VideoTrack) tr;
        tr.flags |= STRH_FLAG_VIDEO_PALETTE_CHANGES;

        DataChunk paletteChangeChunk = new DataChunk(vt.twoCC | PC_ID);
        long offset = getRelativeStreamPosition();
        ImageOutputStream pOut = paletteChangeChunk.getOutputStream();
        pOut.write(data, off, len);
        moviChunk.add(paletteChangeChunk);
        paletteChangeChunk.finish();
        long length = getRelativeStreamPosition() - offset;
        Sample s = new Sample(paletteChangeChunk.chunkType, 0, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);

        offset = getRelativeStreamPosition();
    }

    
    public void writeSample(int track, File file, boolean isKeyframe) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeSample(track, in, isKeyframe);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    
    public void writeSample(int track, InputStream in, boolean isKeyframe) throws IOException {
        ensureStarted();

        Track tr = tracks.get(track);

        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.");
        }



        if (isKeyframe && 0 != (tr.flags & STRH_FLAG_VIDEO_PALETTE_CHANGES)) {


            if (tr.samples.size() > 0) {
                Sample s = tr.samples.get(tr.samples.size() - 1);
                if ((s.chunkType & 0xffff) == PC_ID) {
                    s.isKeyframe = true;
                }
            }
            isKeyframe = false;
        }


        DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe));
        moviChunk.add(dc);
        ImageOutputStream mdatOut = dc.getOutputStream();
        long offset = getRelativeStreamPosition();
        byte[] buf = new byte[512];
        int len;
        while ((len = in.read(buf)) != -1) {
            mdatOut.write(buf, 0, len);
        }
        long length = getRelativeStreamPosition() - offset;
        dc.finish();
        Sample s = new Sample(dc.chunkType, 1, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        tr.length++;
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }

    
    public void writeSample(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        ensureStarted();
        Track tr = tracks.get(track);


        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.\nTrack="+track+", "+tr.format);
        }



        if (isKeyframe && 0 != (tr.flags & STRH_FLAG_VIDEO_PALETTE_CHANGES)) {
            throw new IllegalStateException("Only palette changes can be marked as keyframe.\nTrack="+track+", "+tr.format);
        }

        DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe), len);
        moviChunk.add(dc);
        ImageOutputStream mdatOut = dc.getOutputStream();
        long offset = getRelativeStreamPosition();
        mdatOut.write(data, off, len);
        long length = getRelativeStreamPosition() - offset;
        dc.finish();
        Sample s = new Sample(dc.chunkType, 1, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }

    
    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        ensureStarted();
        Track tr = tracks.get(track);
        if (tr.mediaType == AVIMediaType.AUDIO) {
            DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe), len);
            moviChunk.add(dc);
            ImageOutputStream mdatOut = dc.getOutputStream();
            long offset = getRelativeStreamPosition();
            mdatOut.write(data, off, len);
            long length = getRelativeStreamPosition() - offset;
            dc.finish();
            Sample s = new Sample(dc.chunkType, sampleCount, offset, length, isKeyframe | tr.samples.isEmpty());
            tr.addSample(s);
            idx1.add(s);
            tr.length += sampleCount;
            if (getRelativeStreamPosition() > 1L << 32) {
                throw new IOException("AVI file is larger than 4 GB");
            }
        } else {
            for (int i = 0; i < sampleCount; i++) {
                writeSample(track, data, off, len / sampleCount, isKeyframe);
                off += len / sampleCount;
            }
        }
    }

    
    public long getMediaDuration(int track) {
        Track tr = tracks.get(track);
        long duration = tr.startTime;
        if (!tr.samples.isEmpty()) {
            Sample s = tr.samples.get(tr.samples.size() - 1);
            duration += s.timeStamp + s.duration;
        }
        return duration;
    }

    
    public void close() throws IOException {
        if (state == States.STARTED) {
            finish();
        }
        if (state != States.CLOSED) {
            out.close();
            state = States.CLOSED;
        }
    }

    
    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            moviChunk.finish();
            writeEpilog();
            state = States.FINISHED;
        }
    }

    
    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }

    
    public boolean isDataLimitReached() {
        try {
            return getRelativeStreamPosition() > (long) (1.8 * 1024 * 1024 * 1024);
        } catch (IOException ex) {
            return true;
        }
    }

    private void writeProlog() throws IOException {














        aviChunk = new CompositeChunk(RIFF_ID, AVI_ID);
        CompositeChunk hdrlChunk = new CompositeChunk(LIST_ID, HDRL_ID);


        aviChunk.add(hdrlChunk);
        avihChunk = new FixedSizeDataChunk(AVIH_ID, 56);
        avihChunk.seekToEndOfChunk();
        hdrlChunk.add(avihChunk);


        for (Track tr : tracks) {

            CompositeChunk strlChunk = new CompositeChunk(LIST_ID, STRL_ID);
            hdrlChunk.add(strlChunk);

            tr.strhChunk = new FixedSizeDataChunk(STRH_ID, 56);
            tr.strhChunk.seekToEndOfChunk();
            strlChunk.add(tr.strhChunk);

            tr.strfChunk = new FixedSizeDataChunk(STRF_ID, tr.getSTRFChunkSize());
            tr.strfChunk.seekToEndOfChunk();
            strlChunk.add(tr.strfChunk);

            for (RIFFChunk c : tr.extraHeaders) {
                DataChunk d = new DataChunk(c.getID(),
                        c.getSize());
                ImageOutputStream dout = d.getOutputStream();
                dout.write(c.getData());
                d.finish();
                strlChunk.add(d);
            }

            if (tr.name != null) {
                byte[] data = (tr.name + "\u0000").getBytes("ASCII");
                DataChunk d = new DataChunk(STRN_ID,
                        data.length);
                ImageOutputStream dout = d.getOutputStream();
                dout.write(data);
                d.finish();
                strlChunk.add(d);
            }
        }

        moviChunk = new CompositeChunk(LIST_ID, MOVI_ID);
        aviChunk.add(moviChunk);


    }

    private void writeEpilog() throws IOException {

        ImageOutputStream d;

        
        {
            DataChunk idx1Chunk = new DataChunk(IDX1_ID);
            aviChunk.add(idx1Chunk);
            d = idx1Chunk.getOutputStream();
            long moviListOffset = moviChunk.offset + 8 + 8;

            {
                double movieTime = 0;
                int nTracks = tracks.size();
                int[] trackSampleIndex = new int[nTracks];
                long[] trackSampleCount = new long[nTracks];
                for (Sample s : idx1) {
                    d.setByteOrder(ByteOrder.BIG_ENDIAN);
                    d.writeInt(s.chunkType);
                    d.setByteOrder(ByteOrder.LITTLE_ENDIAN);










                    d.writeInt(((s.chunkType & 0xffff) == PC_ID ? 0x100 : 0x0)
                            | (s.isKeyframe ? 0x10 : 0x0));










                    d.writeInt((int) (s.offset - moviListOffset));





                    d.writeInt((int) (s.length));

                }

            }

            idx1Chunk.finish();
        }

        
        {
            avihChunk.seekToStartOfData();
            d = avihChunk.getOutputStream();


            long largestBufferSize = 0;
            long duration = 0;
            for (Track tr : tracks) {
                long trackDuration = 0;
                for (Sample s : tr.samples) {
                    trackDuration += s.duration;
                }
                duration = max(duration, trackDuration);
                for (Sample s : tr.samples) {
                    if (s.length > largestBufferSize) {
                        largestBufferSize = s.length;
                    }
                }
            }




            Track tt = tracks.get(0);

            d.writeInt((int) ((1000000L * tt.scale) / tt.rate));



            d.writeInt((int)largestBufferSize);





            d.writeInt(0);



            d.writeInt(0x10|0x100|0x800);























            
            d.writeInt(tt.samples.size());


            d.writeInt(0);












            d.writeInt(tracks.size());



            d.writeInt((int) largestBufferSize);






            {
                VideoTrack vt = null;
                int width = 0, height = 0;

                for (Track tr : tracks) {
                    width = max(width, max(tr.frameLeft, tr.frameRight));
                    height = max(height, max(tr.frameTop, tr.frameBottom));
                }
                d.writeInt(width);


                d.writeInt(height);

            }
            d.writeInt(0);
            d.writeInt(0);
            d.writeInt(0);
            d.writeInt(0);

        }

        for (Track tr : tracks) {
            
            tr.strhChunk.seekToStartOfData();
            d = tr.strhChunk.getOutputStream();
            d.setByteOrder(ByteOrder.BIG_ENDIAN);
            d.writeInt(typeToInt(tr.mediaType.fccType));
            d.writeInt(tr.fccHandler);
            d.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            d.writeInt(tr.flags);













            d.writeShort(tr.priority);
            d.writeShort(tr.language);
            d.writeInt((int) tr.initialFrames);
            d.writeInt((int) tr.scale);
            d.writeInt((int) tr.rate);
            d.writeInt((int) tr.startTime);
            d.writeInt((int) tr.length);

            long dwSuggestedBufferSize = 0;
            long dwSampleSize = -1;
            for (Sample s : tr.samples) {
                if (s.length > dwSuggestedBufferSize) {
                    dwSuggestedBufferSize = s.length;
                }
                if (dwSampleSize == -1) {
                    dwSampleSize = s.length;
                } else if (dwSampleSize != s.length) {
                    dwSampleSize = 0;
                }
            }
            if (dwSampleSize == -1) {
                dwSampleSize = 0;
            }

            d.writeInt((int) dwSuggestedBufferSize);





            d.writeInt(tr.quality);






            d.writeInt(tr instanceof AudioTrack ? ((AudioTrack) tr).blockAlign : (int) dwSampleSize);










            d.writeShort(tr.frameLeft);
            d.writeShort(tr.frameTop);
            d.writeShort(tr.frameRight);
            d.writeShort(tr.frameBottom);









            if (tr instanceof VideoTrack) {
                VideoTrack vt = (VideoTrack) tr;
                Format vf = tr.format;

                
                tr.strfChunk.seekToStartOfData();
                d = tr.strfChunk.getOutputStream();
                d.writeInt(40);
                d.writeInt(vf.get(WidthKey));
                d.writeInt(vf.get(HeightKey));
                d.writeShort(1);
                d.writeShort(vf.get(DepthKey));

                String enc = vf.get(EncodingKey);
                if (enc.equals(ENCODING_AVI_DIB)) {
                    d.writeInt(0);
                } else if (enc.equals(ENCODING_AVI_RLE)) {
                    if (vf.get(DepthKey) == 8) {
                        d.writeInt(1);
                    } else if (vf.get(DepthKey) == 4) {
                        d.writeInt(2);
                    } else {
                        throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit images");
                    }
                } else {
            d.setByteOrder(ByteOrder.BIG_ENDIAN);
                    d.writeInt(typeToInt(vt.format.get(EncodingKey)));
            d.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                }

                if (enc.equals(ENCODING_AVI_DIB)) {
                    d.writeInt(0);
                } else {
                    if (vf.get(DepthKey) == 4) {
                        d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) / 2);
                    } else {
                        int bytesPerPixel = Math.max(1, vf.get(DepthKey) / 8);
                        d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) * bytesPerPixel);
                    }
                }

                d.writeInt(0);
                d.writeInt(0);

                d.writeInt(vt.palette == null ? 0 : vt.palette.getMapSize());

                d.writeInt(0);

                if (vt.palette != null) {
                    for (int i = 0, n = vt.palette.getMapSize(); i < n; ++i) {
                        
                        d.write(vt.palette.getBlue(i));
                        d.write(vt.palette.getGreen(i));
                        d.write(vt.palette.getRed(i));
                        d.write(0);
                    }
                }
            } else if (tr instanceof AudioTrack) {
                AudioTrack at = (AudioTrack) tr;

                
                tr.strfChunk.seekToStartOfData();
                d = tr.strfChunk.getOutputStream();

                d.writeShort(at.wFormatTag);
                d.writeShort(at.channels);
                d.writeInt((int) at.samplesPerSec);
                d.writeInt((int) at.avgBytesPerSec);
                d.writeShort(at.blockAlign);
                d.writeShort(at.bitsPerSample);

                d.writeShort(0);








            }
        }


        aviChunk.finish();
    }
}
