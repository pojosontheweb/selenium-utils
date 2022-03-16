
package org.monte.media.quicktime;

import java.awt.image.ColorModel;
import java.util.Arrays;
import org.monte.media.Format;
import org.monte.media.io.ImageOutputStreamAdapter;
import org.monte.media.math.Rational;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.stream.*;
import static java.lang.Math.*;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;


public class QuickTimeOutputStream extends AbstractQuickTimeStream {


    public QuickTimeOutputStream(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        this.out = new FileImageOutputStream(file);
        this.streamOffset = 0;
        init();
    }


    public QuickTimeOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        this.streamOffset = out.getStreamPosition();
        init();
    }

    private void init() {
        creationTime = new Date();
        modificationTime = new Date();
    }


    public void setMovieTimeScale(long timeScale) {
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        this.movieTimeScale = timeScale;
    }


    public long getMovieTimeScale() {
        return movieTimeScale;
    }


    public long getMediaTimeScale(int track) {
        return tracks.get(track).mediaTimeScale;
    }


    public long getMediaDuration(int track) {
        return tracks.get(track).mediaDuration;
    }


    public long getUneditedTrackDuration(int track) {
        Track t = tracks.get(track);
        return t.mediaDuration * t.mediaTimeScale / movieTimeScale;
    }


    public long getTrackDuration(int track) {
        return tracks.get(track).getTrackDuration(movieTimeScale);
    }


    public long getMovieDuration() {
        long duration = 0;
        for (Track t : tracks) {
            duration = Math.max(duration, t.getTrackDuration(movieTimeScale));
        }
        return duration;
    }


    public void setVideoColorTable(int track, ColorModel icm) {
        if (icm instanceof IndexColorModel) {
            VideoTrack t = (VideoTrack) tracks.get(track);
            t.videoColorTable = (IndexColorModel) icm;
        }
    }


    public IndexColorModel getVideoColorTable(int track) {
        VideoTrack t = (VideoTrack) tracks.get(track);
        return t.videoColorTable;
    }


    public void setEditList(int track, Edit[] editList) {
        if (editList != null && editList.length > 0 && editList[editList.length - 1].mediaTime == -1) {
            throw new IllegalArgumentException("Edit list must not end with empty edit.");
        }
        tracks.get(track).editList = editList;
    }


    public int addVideoTrack(String compressionType, String compressorName, long timeScale, int width, int height, int depth, int syncInterval) throws IOException {
        ensureStarted();
        if (compressionType == null || compressionType.length() != 4) {
            throw new IllegalArgumentException("compressionType must be 4 characters long:" + compressionType);
        }
        if (compressorName == null||compressorName.length() < 1 || compressorName.length() > 32) {
            throw new IllegalArgumentException("compressorName must be between 1 and 32 characters long:" + (compressorName == null ? "null" : "\"" + compressorName + "\""));
        }
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be greater than 0, width:" + width + " height:" + height);
        }

        VideoTrack t = new VideoTrack();
        t.mediaCompressionType = compressionType;
        t.mediaCompressorName = compressorName;
        t.mediaTimeScale = timeScale;
        t.width = width;
        t.height = height;
        t.videoDepth = depth;
        t.syncInterval = syncInterval;
        t.format = new Format(
                MediaTypeKey, MediaType.VIDEO,
                MimeTypeKey, MIME_QUICKTIME,
                EncodingKey, compressionType,
                CompressorNameKey, compressorName,
                DataClassKey, byte[].class,
                WidthKey, width, HeightKey, height, DepthKey, depth,
                FrameRateKey, new Rational(timeScale, 1));
        tracks.add(t);
        return tracks.size() - 1;
    }


    public int addAudioTrack(String compressionType,
            long timeScale, double sampleRate,
            int numberOfChannels, int sampleSizeInBits,
            boolean isCompressed,
            int frameDuration, int frameSize, boolean signed, ByteOrder byteOrder) throws IOException {
        ensureStarted();
        if (compressionType == null || compressionType.length() != 4) {
            throw new IllegalArgumentException("audioFormat must be 4 characters long:" + compressionType);
        }
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        if (timeScale != (int) Math.floor(sampleRate)) {
            throw new IllegalArgumentException("timeScale: " + timeScale + " must match integer portion of sampleRate: " + sampleRate);
        }
        if (numberOfChannels != 1 && numberOfChannels != 2) {
            throw new IllegalArgumentException("numberOfChannels must be 1 or 2: " + numberOfChannels);
        }
        if (sampleSizeInBits != 8 && sampleSizeInBits != 16) {
            throw new IllegalArgumentException("sampleSize must be 8 or 16: " + numberOfChannels);
        }

        AudioTrack t = new AudioTrack();
        t.mediaCompressionType = compressionType;
        t.mediaTimeScale = timeScale;
        t.soundSampleRate = sampleRate;
        t.soundCompressionId = isCompressed ? -2 : -1;
        t.soundNumberOfChannels = numberOfChannels;
        t.soundSampleSize = sampleSizeInBits;
        t.soundSamplesPerPacket = frameDuration;
        if (isCompressed) {
            t.soundBytesPerPacket = frameSize;
            t.soundBytesPerFrame = frameSize * numberOfChannels;
        } else {
            t.soundBytesPerPacket = frameSize / numberOfChannels;
            t.soundBytesPerFrame = frameSize;
        }
        t.soundBytesPerSample = sampleSizeInBits / 8;

        t.format = new Format(
                MediaTypeKey, MediaType.AUDIO,
                MimeTypeKey, MIME_QUICKTIME,
                EncodingKey, compressionType,
                SampleRateKey, Rational.valueOf(sampleRate),
                SampleSizeInBitsKey, sampleSizeInBits,
                ChannelsKey, numberOfChannels,
                FrameSizeKey, frameSize,
                SampleRateKey, Rational.valueOf(sampleRate),
                SignedKey, signed,
                ByteOrderKey, byteOrder);
        tracks.add(t);
        return tracks.size() - 1;
    }


    public void setCompressionQuality(int track, float newValue) {
        VideoTrack vt = (VideoTrack) tracks.get(track);
        vt.videoQuality = newValue;
    }


    public float getCompressionQuality(int track) {
        return ((VideoTrack) tracks.get(track)).videoQuality;
    }


    public void setSyncInterval(int track, int i) {
        ((VideoTrack) tracks.get(track)).syncInterval = i;
    }


    public int getSyncInterval(int track) {
        return ((VideoTrack) tracks.get(track)).syncInterval;
    }


    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }


    public Date getCreationTime() {
        return creationTime;
    }


    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }


    public Date getModificationTime() {
        return modificationTime;
    }


    public double getPreferredRate() {
        return preferredRate;
    }


    public void setPreferredRate(double preferredRate) {
        this.preferredRate = preferredRate;
    }


    public double getPreferredVolume() {
        return preferredVolume;
    }


    public void setPreferredVolume(double preferredVolume) {
        this.preferredVolume = preferredVolume;
    }


    public long getCurrentTime() {
        return currentTime;
    }


    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }


    public long getPosterTime() {
        return posterTime;
    }


    public void setPosterTime(long posterTime) {
        this.posterTime = posterTime;
    }


    public long getPreviewDuration() {
        return previewDuration;
    }


    public void setPreviewDuration(long previewDuration) {
        this.previewDuration = previewDuration;
    }


    public long getPreviewTime() {
        return previewTime;
    }


    public void setPreviewTime(long previewTime) {
        this.previewTime = previewTime;
    }


    public long getSelectionDuration() {
        return selectionDuration;
    }


    public void setSelectionDuration(long selectionDuration) {
        this.selectionDuration = selectionDuration;
    }


    public long getSelectionTime() {
        return selectionTime;
    }


    public void setSelectionTime(long selectionTime) {
        this.selectionTime = selectionTime;
    }


    public void setMovieTransformationMatrix(double[] matrix) {
        if (matrix.length != 9) {
            throw new IllegalArgumentException("matrix must have 9 elements, matrix.length=" + matrix.length);
        }

        System.arraycopy(matrix, 0, movieMatrix, 0, 9);
    }


    public double[] getMovieTransformationMatrix() {
        return movieMatrix.clone();
    }


    public void setTransformationMatrix(int track, double[] matrix) {
        if (matrix.length != 9) {
            throw new IllegalArgumentException("matrix must have 9 elements, matrix.length=" + matrix.length);
        }

        System.arraycopy(matrix, 0, tracks.get(track).matrix, 0, 9);
    }


    public double[] getTransformationMatrix(int track) {
        return tracks.get(track).matrix.clone();
    }


    protected void ensureStarted() throws IOException {
        ensureOpen();
        if (state == States.FINISHED) {
            throw new IOException("Can not write into finished movie.");
        }
        if (state != States.STARTED) {
            writeProlog();
            mdatAtom = new WideDataAtom("mdat");
            state = States.STARTED;
        }
    }


    public void writeSample(int track, File file, long duration, boolean isSync) throws IOException {
        ensureStarted();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeSample(track, in, duration, isSync);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }


    public void writeSample(int track, InputStream in, long duration, boolean isSync) throws IOException {
        ensureStarted();
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        Track t = tracks.get(track);
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        OutputStream mdatOut = mdatAtom.getOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) != -1) {
            mdatOut.write(buf, 0, len);
        }
        long length = getRelativeStreamPosition() - offset;
        t.addSample(new Sample(duration, offset, length), 1, isSync);
    }


    public void writeSample(int track, byte[] data, long duration, boolean isSync) throws IOException {
        writeSample(track, data, 0, data.length, duration, isSync);
    }


    public void writeSample(int track, byte[] data, int off, int len, long duration, boolean isSync) throws IOException {
        ensureStarted();
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        Track t = tracks.get(track);
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        OutputStream mdatOut = mdatAtom.getOutputStream();
        mdatOut.write(data, off, len);
        t.addSample(new Sample(duration, offset, len), 1, isSync);
    }


    public void writeSamples(int track, int sampleCount, byte[] data, long sampleDuration, boolean isSync) throws IOException {
        writeSamples(track, sampleCount, data, 0, data.length, sampleDuration, isSync);
    }


    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration) throws IOException {
        writeSamples(track, sampleCount, data, off, len, sampleDuration, true);
    }


    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync) throws IOException {
        ensureStarted();
        if (sampleDuration <= 0) {
            throw new IllegalArgumentException("sampleDuration must be greater 0, sampleDuration=" + sampleDuration + " track=" + track);
        }
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("sampleCount must be greater 0, sampleCount=" + sampleCount + " track=" + track);
        }
        if (len % sampleCount != 0) {
            throw new IllegalArgumentException("len must be divisable by sampleCount len=" + len + " sampleCount=" + sampleCount + " track=" + track);
        }
        Track t = tracks.get(track);
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        OutputStream mdatOut = mdatAtom.getOutputStream();
        mdatOut.write(data, off, len);


        int sampleLength = len / sampleCount;
        Sample first = new Sample(sampleDuration, offset, sampleLength);
        Sample last = new Sample(sampleDuration, offset + sampleLength * (sampleCount - 1), sampleLength);
        t.addChunk(new Chunk(first, last, sampleCount, 1), isSync);
    }


    public boolean isDataLimitReached() {
        try {
            long maxMediaDuration = 0;
            for (Track t : tracks) {
                maxMediaDuration = max(t.mediaDuration, maxMediaDuration);
            }

            return getRelativeStreamPosition() > (long) (1L << 61)
                    || maxMediaDuration > 1L << 61;
        } catch (IOException ex) {
            return true;
        }
    }


    public void close() throws IOException {
        try {
            if (state == States.STARTED) {
                finish();
            }
        } finally {
            if (state != States.CLOSED) {
                out.close();
                state = States.CLOSED;
            }
        }
    }


    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            for (int i = 0, n = tracks.size(); i < n; i++) {
            }
            mdatAtom.finish();
            writeEpilog();
            state = States.FINISHED;

        }
    }


    protected void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }


    private void writeProlog() throws IOException {

        DataAtom ftypAtom = new DataAtom("ftyp");
        DataAtomOutputStream d = ftypAtom.getOutputStream();
        d.writeType("qt  ");
        d.writeBCD4(2005);
        d.writeBCD2(3);
        d.writeBCD2(0);
        d.writeType("qt  ");
        d.writeInt(0);
        d.writeInt(0);
        d.writeInt(0);
        ftypAtom.finish();
    }

    private void writeEpilog() throws IOException {
        long duration = getMovieDuration();

        DataAtom leaf;


        moovAtom = new CompositeAtom("moov");


        leaf = new DataAtom("mvhd");
        moovAtom.add(leaf);
        DataAtomOutputStream d = leaf.getOutputStream();
        d.writeByte(0);


        d.writeByte(0);
        d.writeByte(0);
        d.writeByte(0);


        d.writeMacTimestamp(creationTime);





        d.writeMacTimestamp(modificationTime);





        d.writeUInt(movieTimeScale);





        d.writeUInt(duration);





        d.writeFixed16D16(preferredRate);



        d.writeFixed8D8(preferredVolume);



        d.write(new byte[10]);


        d.writeFixed16D16(movieMatrix[0]);
        d.writeFixed16D16(movieMatrix[1]);
        d.writeFixed2D30(movieMatrix[2]);
        d.writeFixed16D16(movieMatrix[3]);
        d.writeFixed16D16(movieMatrix[4]);
        d.writeFixed2D30(movieMatrix[5]);
        d.writeFixed16D16(movieMatrix[6]);
        d.writeFixed16D16(movieMatrix[7]);
        d.writeFixed2D30(movieMatrix[8]);






        d.writeUInt(previewTime);


        d.writeUInt(previewDuration);


        d.writeUInt(posterTime);


        d.writeUInt(selectionTime);


        d.writeUInt(selectionDuration);


        d.writeUInt(currentTime);


        d.writeUInt(tracks.size() + 1);




        for (int i = 0, n = tracks.size(); i < n; i++) {
            Track t = tracks.get(i);

            t.writeTrackAtoms(i, moovAtom, modificationTime);
        }

        moovAtom.finish();
    }


    public void toWebOptimizedMovie(File outputFile, boolean compressHeader) throws IOException {
        finish();
        long originalMdatOffset = mdatAtom.getOffset();
        CompositeAtom originalMoovAtom = moovAtom;
        mdatOffset = 0;

        ImageOutputStream originalOut = out;
        try {
            out = null;

            if (compressHeader) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int maxIteration = 5;
                long compressionHeadersSize = 40 + 8;
                long headerSize = 0;
                long freeSize = 0;
                while (true) {
                    mdatOffset = compressionHeadersSize + headerSize + freeSize;
                    buf.reset();
                    DeflaterOutputStream deflater = new DeflaterOutputStream(buf);
                    out = new MemoryCacheImageOutputStream(deflater);
                    writeEpilog();
                    out.close();
                    deflater.close();

                    if (buf.size() > headerSize + freeSize && --maxIteration > 0) {
                        if (headerSize != 0) {
                            freeSize = Math.max(freeSize, buf.size() - headerSize - freeSize);
                        }
                        headerSize = buf.size();
                    } else {
                        freeSize = headerSize + freeSize - buf.size();
                        headerSize = buf.size();
                        break;
                    }
                }

                if (maxIteration < 0 || buf.size() == 0) {
                    compressHeader = false;
                    System.err.println("WARNING QuickTimeWriter failed to compress header.");
                } else {
                    out = new FileImageOutputStream(outputFile);
                    writeProlog();


                    DataAtomOutputStream daos = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                    daos.writeUInt(headerSize + 40);
                    daos.writeType("moov");

                    daos.writeUInt(headerSize + 32);
                    daos.writeType("cmov");

                    daos.writeUInt(12);
                    daos.writeType("dcom");
                    daos.writeType("zlib");

                    daos.writeUInt(headerSize + 12);
                    daos.writeType("cmvd");
                    daos.writeUInt(originalMoovAtom.size());

                    daos.write(buf.toByteArray());


                    daos.writeUInt(freeSize + 8);
                    daos.writeType("free");
                    for (int i = 0; i < freeSize; i++) {
                        daos.write(0);
                    }
                }

            }
            if (!compressHeader) {
                out = new FileImageOutputStream(outputFile);
                mdatOffset = moovAtom.size();
                writeProlog();
                writeEpilog();
            }


            byte[] buf = new byte[4096];
            originalOut.seek((originalMdatOffset));
            for (long count = 0, n = mdatAtom.size(); count < n;) {
                int read = originalOut.read(buf, 0, (int) Math.min(buf.length, n - count));
                out.write(buf, 0, read);
                count += read;
            }
            out.close();
        } finally {
            mdatOffset = 0;
            moovAtom = originalMoovAtom;
            out = originalOut;
        }
    }
}
