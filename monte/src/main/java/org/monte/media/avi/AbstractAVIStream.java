
package org.monte.media.avi;

import org.monte.media.riff.RIFFChunk;
import java.util.Map;
import org.monte.media.Buffer;
import org.monte.media.Codec;
import org.monte.media.Format;
import org.monte.media.io.SubImageOutputStream;
import java.awt.Dimension;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.stream.ImageOutputStream;
import static org.monte.media.VideoFormatKeys.*;


public abstract class AbstractAVIStream {



    protected final static int RIFF_ID =0x52494646;
    protected final static int AVI_ID = 0x41564920;
    protected final static int AVIX_ID = 0x41564958;
    protected final static int LIST_ID = 0x4c495354;
    protected final static int MOVI_ID = 0x6d6f7669;
    protected final static int HDRL_ID = 0x6864726c;
    protected final static int AVIH_ID = 0x61766968;
    protected final static int STRL_ID = 0x7374726c;
    protected final static int STRH_ID = 0x73747268;
    protected final static int STRN_ID = 0x7374726e;
    protected final static int STRF_ID = 0x73747266;
    protected final static int STRD_ID = 0x73747264;
    protected final static int IDX1_ID = 0x69647831;
    protected final static int REC_ID = 0x72656320;
    protected final static int CHUNK_SUBTYPE_MASK = 0xffff;
    protected final static int PC_ID = 0x00007063;
    protected final static int DB_ID = 0x00006462;
    protected final static int DC_ID = 0x00006463;
    protected final static int WB_ID = 0x00007762;


    public final static int AVIH_FLAG_HAS_INDEX = 0x00000010;

    public final static int AVIH_FLAG_MUST_USE_INDEX = 0x00000020;

    public final static int AVIH_FLAG_IS_INTERLEAVED = 0x00000100;

    public final static int AVIH_FLAG_TRUST_CK_TYPE = 0x00000800;

    public final static int AVIH_FLAG_WAS_CAPTURE_FILE = 0x00010000;

    public final static int AVIH_FLAG_COPYRIGHTED = 0x00020000;

    public final static int STRH_FLAG_DISABLED = 0x00000001;

    public final static int STRH_FLAG_VIDEO_PALETTE_CHANGES = 0x00010000;

    protected ImageOutputStream out;

    protected long streamOffset;


    public static enum AVIMediaType {

        AUDIO("auds"),
        MIDI("mids"),
        TEXT("txts"),
        VIDEO("vids")
        ;
        protected final String fccType;

        @Override
        public String toString() {
            return fccType;
        }

        AVIMediaType(String fourCC) {
            this.fccType = fourCC;
        }
    }

    protected ArrayList<Track> tracks = new ArrayList<Track>();


    protected long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }


    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }


    protected static class Sample {

        int chunkType;

        long offset;

        long length;

        int duration;

        boolean isKeyframe;
        long timeStamp;

        Sample header;


        public Sample(int chunkId, int duration, long offset, long length, boolean isSync) {
            this.chunkType = chunkId;
            this.duration = duration;
            this.offset = offset;
            this.length = length;
            this.isKeyframe = isSync;
        }
    }

    protected abstract class Track {

        protected Format format;
        // Common metadata
        protected ArrayList<Sample> samples;
        protected int syncInterval = 30;
        protected int twoCC;
        //
        // AVISTREAMHEADER structure
        // -------------------------
        protected final AVIMediaType mediaType;
        //protected String fccType;
        protected int fccHandler;
        protected int flags;
        protected int priority = 0;
        protected int language = 0;
        protected long initialFrames = 0;
        protected long scale = 1;
        protected long rate = 30;
        protected long startTime = 0;
        protected long length;
        protected int quality = -1;
        int frameLeft;
        int frameTop;
        int frameRight;
        int frameBottom;
        // --------------------------------
        // End of AVISTREAMHEADER structure
        protected FixedSizeDataChunk strhChunk;
        protected FixedSizeDataChunk strfChunk;
        protected String name;
        protected Codec codec;
        protected Buffer outputBuffer;
        protected Buffer inputBuffer;
        protected long readIndex = 0;
        protected ArrayList<RIFFChunk> extraHeaders;

        public Track(int trackIndex, AVIMediaType mediaType, int fourCC) {
            this.mediaType = mediaType;
            twoCC = (('0'+trackIndex/10)<<24) | (('0'+trackIndex%10)<<16);

            this.fccHandler = fourCC;
            this.samples = new ArrayList<Sample>();
            this.extraHeaders = new ArrayList<RIFFChunk>();
        }

        public abstract long getSTRFChunkSize();

        public abstract int getSampleChunkFourCC(boolean isSync);

        public void addSample(Sample s) {
            if (!samples.isEmpty()) {
                s.timeStamp = samples.get(samples.size() - 1).timeStamp + samples.get(samples.size() - 1).duration;
            }
            samples.add(s);
            length++;
        }
    }

    protected class VideoTrack extends Track {
        // Video metadata

        protected float videoQuality = 0.97f;
        protected IndexColorModel palette;
        protected IndexColorModel previousPalette;
        protected Object previousData;
        //protected Rectangle rcFrame;
        // BITMAPINFOHEADER structure
        int width;
        int height;
        int planes;
        int bitCount;
        String compression;
        long sizeImage;
        long xPelsPerMeter;
        long yPelsPerMeter;
        long clrUsed;
        long clrImportant;
        private int sampleChunkFourCC;

        public VideoTrack(int trackIndex, int fourCC, Format videoFormat) {
            super(trackIndex, AVIMediaType.VIDEO, fourCC);
            this.format = videoFormat;
            sampleChunkFourCC = videoFormat != null && videoFormat.get(EncodingKey).equals(ENCODING_AVI_DIB) ? twoCC | DB_ID : twoCC | DC_ID;
        }

        @Override
        public long getSTRFChunkSize() {
            return palette == null ? 40 : 40 + palette.getMapSize() * 4;

        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            return sampleChunkFourCC;
        }
    }

    protected class AudioTrack extends Track {

        // WAVEFORMATEX Structure
        protected int wFormatTag;
        protected int channels;
        protected long samplesPerSec;
        protected long avgBytesPerSec;
        protected int blockAlign;
        protected int bitsPerSample;
        protected final static int WAVE_FORMAT_PCM = 0x0001;
        protected final static int WAVE_FORMAT_ADPCM = 0x0002;
        protected final static int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
        protected final static int WAVE_FORMAT_IBM_CVSD = 0x0005;
        protected final static int WAVE_FORMAT_ALAW = 0x0006;
        protected final static int WAVE_FORMAT_MULAW = 0x0007;
        protected final static int WAVE_FORMAT_OKI_ADPCM = 0x0010;
        protected final static int WAVE_FORMAT_DVI_ADPCM = 0x0011;
        protected final static int WAVE_FORMAT_IMA_ADPCM = WAVE_FORMAT_DVI_ADPCM;
        protected final static int WAVE_FORMAT_MEDIASPACE_ADPCM = 0x0012;
        protected final static int WAVE_FORMAT_SIERRA_ADPCM = 0x0013;
        protected final static int WAVE_FORMAT_G723_ADPCM = 0x0014;
        protected final static int WAVE_FORMAT_DIGISTD = 0x0015;
        protected final static int WAVE_FORMAT_DIGIFIX = 0x0016;
        protected final static int WAVE_FORMAT_DIALOGIC_OKI_ADPCM = 0x0017;
        protected final static int WAVE_FORMAT_MEDIAVISION_ADPCM = 0x0018;
        protected final static int WAVE_FORMAT_YAMAHA_ADPCM = 0x0020;
        protected final static int WAVE_FORMAT_SONARC = 0x0021;
        protected final static int WAVE_FORMAT_DSPGROUP_TRUESPEECH = 0x0022;
        protected final static int WAVE_FORMAT_ECHOSC1 = 0x0023;
        protected final static int WAVE_FORMAT_AUDIOFILE_AF36 = 0x0024;
        protected final static int WAVE_FORMAT_APTX = 0x0025;
        protected final static int WAVE_FORMAT_AUDIOFILE_AF10 = 0x0026;
        protected final static int WAVE_FORMAT_DOLBY_AC2 = 0x0030;
        protected final static int WAVE_FORMAT_GSM610 = 0x0031;
        protected final static int WAVE_FORMAT_MSNAUDIO = 0x0032;
        protected final static int WAVE_FORMAT_ANTEX_ADPCME = 0x0033;
        protected final static int WAVE_FORMAT_CONTROL_RES_VQLPC = 0x0034;
        protected final static int WAVE_FORMAT_DIGIREAL = 0x0035;
        protected final static int WAVE_FORMAT_DIGIADPCM = 0x0036;
        protected final static int WAVE_FORMAT_CONTROL_RES_CR10 = 0x0037;
        protected final static int WAVE_FORMAT_NMS_VBXADPCM = 0x0038;
        protected final static int WAVE_FORMAT_CS_IMAADPCM = 0x0039;
        protected final static int WAVE_FORMAT_ECHOSC3 = 0x003A;
        protected final static int WAVE_FORMAT_ROCKWELL_ADPCM = 0x003B;
        protected final static int WAVE_FORMAT_ROCKWELL_DIGITALK = 0x003C;
        protected final static int WAVE_FORMAT_XEBEC = 0x003D;
        protected final static int WAVE_FORMAT_G721_ADPCM = 0x0040;
        protected final static int WAVE_FORMAT_G728_CELP = 0x0041;
        protected final static int WAVE_FORMAT_MPEG = 0x0050;
        protected final static int WAVE_FORMAT_MPEGLAYER3 = 0x0055;
        protected final static int WAVE_FORMAT_CIRRUS = 0x0060;
        protected final static int WAVE_FORMAT_ESPCM = 0x0061;
        protected final static int WAVE_FORMAT_VOXWARE = 0x0062;
        protected final static int WAVE_FORMAT_CANOPUS_ATRAC = 0x0063;
        protected final static int WAVE_FORMAT_G726_ADPCM = 0x0064;
        protected final static int WAVE_FORMAT_G722_ADPCM = 0x0065;
        protected final static int WAVE_FORMAT_DSAT = 0x0066;
        protected final static int WAVE_FORMAT_DSAT_DISPLAY = 0x0067;
        protected final static int WAVE_FORMAT_SOFTSOUND = 0x0080;
        protected final static int WAVE_FORMAT_RHETOREX_ADPCM = 0x0100;
        protected final static int WAVE_FORMAT_CREATIVE_ADPCM = 0x0200;
        protected final static int WAVE_FORMAT_CREATIVE_FASTSPEECH8 = 0x0202;
        protected final static int WAVE_FORMAT_CREATIVE_FASTSPEECH10 = 0x0203;
        protected final static int WAVE_FORMAT_QUARTERDECK = 0x0220;
        protected final static int WAVE_FORMAT_FM_TOWNS_SND = 0x0300;
        protected final static int WAVE_FORMAT_BTV_DIGITAL = 0x0400;
        protected final static int WAVE_FORMAT_OLIGSM = 0x1000;
        protected final static int WAVE_FORMAT_OLIADPCM = 0x1001;
        protected final static int WAVE_FORMAT_OLICELP = 0x1002;
        protected final static int WAVE_FORMAT_OLISBC = 0x1003;
        protected final static int WAVE_FORMAT_OLIOPR = 0x1004;
        protected final static int WAVE_FORMAT_LH_CODEC = 0x1100;
        protected final static int WAVE_FORMAT_NORRIS = 0x1400;
        protected final static int WAVE_FORMAT_DEVELOPMENT = 0xFFFF;
        private int sampleChunkFourCC;

        public AudioTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.AUDIO, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            return 18;

        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            return sampleChunkFourCC;
        }
    }

    protected abstract class Chunk {

        protected int chunkType;
        protected long offset;

        public Chunk(int chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getRelativeStreamPosition();
        }

        public abstract void finish() throws IOException;

        public abstract long size();
    }

    protected class CompositeChunk extends Chunk {

        protected int compositeType;
        protected LinkedList<Chunk> children;
        protected boolean finished;

        public CompositeChunk(int compositeType, int chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;
            //out.write
            out.writeLong(0); // make room for the chunk header
            out.writeInt(0); // make room for the chunk header
            children = new LinkedList<Chunk>();
        }

        public void add(Chunk child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
            }
            children.add(child);
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeChunk \"" + chunkType + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(compositeType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                out.writeInt((int) (size() - 8));
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                for (Chunk child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }
                finished = true;
            }
        }

        @Override
        public long size() {
            long length = 12;
            for (Chunk child : children) {
                length += child.size() + child.size() % 2;
            }
            return length;
        }
    }

    protected class DataChunk extends Chunk {

        //protected SubImageOutputStream data;
        protected boolean finished;
        private long finishedSize;

        public DataChunk(int name) throws IOException {
            this(name, -1);
        }

        public DataChunk(int name, long dataSize) throws IOException {
            super(name);
            /*
             data = new SubImageOutputStream(out, ByteOrder.LITTLE_ENDIAN, false);
             data.writeInt(typeToInt(chunkType));
             data.writeInt((int)Math.max(0, dataSize)); */
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) Math.max(0, dataSize));
            finishedSize = dataSize == -1 ? -1 : dataSize + 8;
        }

        public ImageOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataChunk is finished");
            }
            //return data;
            return out;
        }

        public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (finishedSize == -1) {
                    finishedSize = size();

                    if (finishedSize > 0xffffffffL) {
                        throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size());
                    }

                    seekRelative(offset + 4);
                    out.writeInt((int) (finishedSize - 8));
                    seekRelative(offset + finishedSize);
                } else {
                    if (size() != finishedSize) {
                        throw new IOException("DataChunk \"" + chunkType + "\" actual size differs from given size: actual size:" + size() + " given size:" + finishedSize);
                    }
                }
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }


                //data.dispose();
                //data = null;
                finished = true;
            }
        }

        @Override
        public long size() {
            if (finished) {
                return finishedSize;
            }

            try {
                //               return data.length();
                return out.getStreamPosition() - offset;
            } catch (IOException ex) {
                InternalError ie = new InternalError("IOException");
                ie.initCause(ex);
                throw ie;
            }
        }
    }

    protected class FixedSizeDataChunk extends Chunk {

        protected boolean finished;
        protected long fixedSize;

        public FixedSizeDataChunk(int chunkType, long fixedSize) throws IOException {
            super(chunkType);
            this.fixedSize = fixedSize;
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) fixedSize);

            // Fill fixed size with nulls
            byte[] buf = new byte[(int) Math.min(512, fixedSize)];
            long written = 0;
            while (written < fixedSize) {
                out.write(buf, 0, (int) Math.min(buf.length, fixedSize - written));
                written += Math.min(buf.length, fixedSize - written);
            }
            if (fixedSize % 2 == 1) {
                out.writeByte(0); // write pad byte
            }
            seekToStartOfData();
        }

        public ImageOutputStream getOutputStream() {
            /*if (finished) {
             throw new IllegalStateException("DataChunk is finished");
             }*/
            return out;
        }

        public long getOffset() {
            return offset;
        }

        public void seekToStartOfData() throws IOException {
            seekRelative(offset + 8);

        }

        public void seekToEndOfChunk() throws IOException {
            seekRelative(offset + 8 + fixedSize + fixedSize % 2);
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                finished = true;
            }
        }

        @Override
        public long size() {
            return 8 + fixedSize;
        }
    }

    protected class MidiTrack extends Track {

        private final int sampleChunkFourCC;

        public MidiTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.MIDI, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected class TextTrack extends Track {

        private final int sampleChunkFourCC;

        public TextTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.TEXT, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected static class MainHeader {

        protected long microSecPerFrame;
        protected long maxBytesPerSec;
        protected long paddingGranularity;
        protected int flags;
        protected long totalFrames;
        protected long initialFrames;
        protected long streams;
        protected long suggestedBufferSize;
        protected Dimension size;
    }

    protected static int typeToInt(String str) {
        int value = ((str.charAt(0) & 0xff) << 24) | ((str.charAt(1) & 0xff) << 16) | ((str.charAt(2) & 0xff) << 8) | (str.charAt(3) & 0xff);
        return value;
    }

    protected static String intToType(int id) {
        char[] b=new char[4];

            b[0] = (char) ((id >>> 24) & 0xff);
            b[1] = (char) ((id >>> 16) & 0xff);
            b[2] = (char) ((id >>> 8) & 0xff);
            b[3] = (char) (id & 0xff);
            return String.valueOf(b);
    }

    protected static boolean isFlagSet(int flag, int mask) {
        return (flag & mask) == mask;
    }
}
