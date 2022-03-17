
package org.monte.media.avi;

import org.monte.media.AbortException;
import org.monte.media.Format;
import org.monte.media.ParseException;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.math.Rational;
import org.monte.media.riff.RIFFChunk;
import org.monte.media.riff.RIFFParser;
import org.monte.media.riff.RIFFVisitor;
import java.awt.Dimension;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;


public class AVIInputStream extends AbstractAVIStream {


    protected final ImageInputStream in;

    private boolean isRealized = false;
    protected MainHeader mainHeader;
    protected ArrayList<Sample> idx1 = new ArrayList<Sample>();
    private long moviOffset = 0;


    public AVIInputStream(File file) throws IOException {

        this.in = new FileImageInputStream(file);
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.streamOffset = 0;
    }


    public AVIInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        this.streamOffset = in.getStreamPosition();
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }


    protected void ensureRealized() throws IOException {
        if (!isRealized) {
            isRealized = true;
            readAllMetadata();
        }
        if (mainHeader == null) {
            throw new IOException("AVI main header missing.");
        }
    }


    public int getHeaderFlags() throws IOException {
        ensureRealized();
        return mainHeader.flags;
    }

    public Dimension getVideoDimension() throws IOException {
        ensureRealized();
        return (Dimension) mainHeader.size.clone();
    }

    public int getTrackCount() throws IOException {
        ensureRealized();
        return tracks.size();
    }


    public long getMicroSecPerFrame() throws IOException {
        ensureRealized();
        return mainHeader.microSecPerFrame;
    }


    public long getTimeScale(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).scale;
    }


    public long getStartTime(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).startTime;
    }


    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).samples.size();
    }


    public String getName(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).name;
    }


    public byte[] getExtraHeader(int track, String fourcc) throws IOException {
        ensureRealized();
        int id = RIFFParser.stringToID(fourcc);
        for (RIFFChunk c : tracks.get(track).extraHeaders) {
            if (c.getID() == id) {
                return c.getData();
            }
        }
        return null;
    }


    public String[] getExtraHeaderFourCCs(int track) throws IOException {
        Track tr = tracks.get(track);
        String[] fourccs = new String[tr.extraHeaders.size()];
        for (int i = 0; i < fourccs.length; i++) {
            fourccs[i] = RIFFParser.idToString(tr.extraHeaders.get(i).getID());
        }
        return fourccs;
    }


    protected void readAllMetadata() throws IOException {
        in.seek(streamOffset);
        final RIFFParser p = new RIFFParser();


        try {
            RIFFVisitor v = new RIFFVisitor() {
                private Track currentTrack;

                @Override
                public boolean enteringGroup(RIFFChunk group) {

                    if (group.getType() == MOVI_ID) {
                        moviOffset = group.getScan() + 8;
                    }

                    if (group.getType() == MOVI_ID && group.getID() == LIST_ID) {
                        if (mainHeader != null
                                && (mainHeader.flags & AVIH_FLAG_HAS_INDEX) != 0
                                && p.getStreamOffset() == 0) {


                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public void enterGroup(RIFFChunk group) throws ParseException, AbortException {

                }

                @Override
                public void leaveGroup(RIFFChunk group) throws ParseException, AbortException {

                    if (group.getType() == HDRL_ID) {
                        currentTrack = null;
                    }
                }

                @Override
                public void visitChunk(RIFFChunk group, RIFFChunk chunk) throws ParseException, AbortException {
                    try {


                        switch (chunk.getType()) {
                            case HDRL_ID:
                                switch (chunk.getID()) {
                                    case AVIH_ID:
                                        mainHeader = readAVIH(chunk.getData());
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case STRL_ID:




                                switch (chunk.getID()) {
                                    case STRH_ID:
                                        currentTrack = readSTRH(chunk.getData());
                                        tracks.add(currentTrack);
                                        break;
                                    case STRF_ID:
                                        switch (currentTrack.mediaType) {
                                            case AUDIO:
                                                readAudioSTRF((AudioTrack) currentTrack, chunk.getData());
                                                break;
                                            case VIDEO:
                                                readVideoSTRF((VideoTrack) currentTrack, chunk.getData());
                                                break;
                                            default:
                                                throw new ParseException("Unsupported media type:" + currentTrack.mediaType);
                                        }
                                        break;
                                    case STRN_ID:
                                        readSTRN(currentTrack, chunk.getData());
                                        break;
                                    default:
                                        currentTrack.extraHeaders.add(chunk);
                                        break;
                                }
                                break;
                            case AVI_ID:
                                switch (chunk.getID()) {
                                    case IDX1_ID:
                                        if (isFlagSet(mainHeader.flags, AVIH_FLAG_HAS_INDEX)) {
                                            readIDX1(tracks, idx1, chunk.getData());
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case MOVI_ID:

                            case REC_ID: {
                                int chunkIdInt = chunk.getID();
                                int id = chunkIdInt;
                                int track = (((chunkIdInt >> 24) & 0xff) - '0') * 10 + (((chunkIdInt >>> 16) & 0xff) - '0');
                                if (track >= 0 && track < tracks.size()) {
                                    Track tr=tracks.get(track);
                                    Sample s = new Sample(id, (id & 0xffff) == PC_ID ? 0 : 1, chunk.getScan(), chunk.getSize(), false);

                                    if (tr.format.get(MediaTypeKey)==MediaType.AUDIO) {
                                        s.duration=(int)(s.length/(tr.format.get(FrameSizeKey)*tr.format.get(ChannelsKey)));
                                    }

                                    s.isKeyframe=tr.samples.isEmpty()||(id & 0xffff) == WB_ID||(id & 0xffff) == DB_ID;
                                    if (tr.samples.size()>0) {
                                        Sample lastSample=tr.samples.get(tr.samples.size()-1);
                                        s.timeStamp = lastSample.timeStamp+lastSample.duration;
                                    }
                                    tr.length=s.timeStamp+s.duration;
                                    idx1.add(s);
                                    tr.samples.add(s);

                                }
                            }
                            break;
                            default:
                                break;
                        }

                    } catch (IOException ex) {
                        throw new ParseException("Error parsing " + RIFFParser.idToString(group.getID()) + "." + RIFFParser.idToString(chunk.getID()), ex);
                    }
                }
            };


            int count = 0;
            while (true) {
                long offset = p.parse(in, v);
                p.setStreamOffset(offset);
                count++;
            }
        } catch (EOFException ex) {

        } catch (ParseException ex) {
            throw new IOException("Error Parsing AVI stream", ex);
        } catch (AbortException ex) {
            throw new IOException("Parsing aborted", ex);
        }
    }


    private MainHeader readAVIH(byte[] data) throws IOException, ParseException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);
        MainHeader mh = new MainHeader();
        mh.microSecPerFrame = in.readUnsignedInt();
        mh.maxBytesPerSec = in.readUnsignedInt();
        mh.paddingGranularity = in.readUnsignedInt();
        mh.flags = in.readInt();
        mh.totalFrames = in.readUnsignedInt();
        mh.initialFrames = in.readUnsignedInt();
        mh.streams = in.readUnsignedInt();
        mh.suggestedBufferSize = in.readUnsignedInt();

        mh.size = new Dimension(in.readInt(), in.readInt());
        return mh;
    }



    private Track readSTRH(byte[] data) throws IOException, ParseException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);
        Track tr = null;

        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        String type = intToType(in.readInt());
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int handler = in.readInt();

        if (type.equals(AVIMediaType.AUDIO.fccType)) {
            tr = new AudioTrack(tracks.size(), handler);
        } else if (type.equals(AVIMediaType.VIDEO.fccType)) {
            tr = new VideoTrack(tracks.size(), handler, null);
        } else if (type.equals(AVIMediaType.MIDI.fccType)) {
            tr = new MidiTrack(tracks.size(), handler);
        } else if (type.equals(AVIMediaType.TEXT.fccType)) {
            tr = new TextTrack(tracks.size(), handler);
        } else {
            throw new ParseException("Unknown track type " + type);
        }

        tr.fccHandler = handler;

        tr.flags = in.readInt();
        tr.priority = in.readUnsignedShort();
        tr.language = in.readUnsignedShort();
        tr.initialFrames = in.readUnsignedInt();
        tr.scale = in.readUnsignedInt();
        tr.rate = in.readUnsignedInt();
        tr.startTime = in.readUnsignedInt();
        tr.length = in.readUnsignedInt();
         in.readUnsignedInt();
        tr.quality = in.readInt();
         in.readUnsignedInt();
        tr.frameLeft = in.readShort();
        tr.frameTop = in.readShort();
        tr.frameRight = in.readShort();
        tr.frameBottom = in.readShort();

        return tr;
    }


    private void readSTRN(Track tr, byte[] data) throws IOException {
        tr.name = new String(data, 0, data.length - 1, "ASCII");
    }


    private void readVideoSTRF(VideoTrack tr, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);


        long structSize = in.readUnsignedInt();
        tr.width = in.readInt();
        tr.height = in.readInt();
        tr.planes = in.readUnsignedShort();
        tr.bitCount = in.readUnsignedShort();
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        tr.compression = intToType(in.readInt());
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long imageSizeInBytes = in.readUnsignedInt();
        tr.xPelsPerMeter = in.readUnsignedInt();
        tr.yPelsPerMeter = in.readUnsignedInt();
        tr.clrUsed = in.readUnsignedInt();
        tr.clrImportant = in.readUnsignedInt();
        if (tr.bitCount == 0) {
            tr.bitCount = (int) (imageSizeInBytes / tr.width / tr.height * 8);
        }

        tr.format = new Format(MimeTypeKey, MIME_AVI,
                MediaTypeKey, MediaType.VIDEO,
                EncodingKey, tr.compression,
                DataClassKey, byte[].class,
                WidthKey, tr.width,
                HeightKey, tr.height,
                DepthKey, tr.bitCount,
                PixelAspectRatioKey, new Rational(1, 1),
                FrameRateKey, new Rational(tr.rate, tr.scale),
                FixedFrameRateKey, true);
    }


    private void readAudioSTRF(AudioTrack tr, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);

        String formatTag = RIFFParser.idToString(in.readUnsignedShort());
        tr.channels = in.readUnsignedShort();
        tr.samplesPerSec = in.readUnsignedInt();
        tr.avgBytesPerSec = in.readUnsignedInt();
        tr.blockAlign = in.readUnsignedShort();
        tr.bitsPerSample = in.readUnsignedShort();
        if (data.length > 16) {
            long cbSize = in.readUnsignedShort();

        }

        tr.format = new Format(MimeTypeKey, MIME_AVI,
                MediaTypeKey, MediaType.AUDIO,
                EncodingKey, formatTag,
                SampleRateKey, Rational.valueOf(tr.samplesPerSec),
                SampleSizeInBitsKey, tr.bitsPerSample,
                ChannelsKey, tr.channels,
                FrameSizeKey, tr.blockAlign,
                FrameRateKey, new Rational(tr.samplesPerSec, 1),
                SignedKey, tr.bitsPerSample != 8,
                ByteOrderKey, ByteOrder.LITTLE_ENDIAN);

    }


    private void readIDX1(ArrayList<Track> tracks, ArrayList<Sample> idx1, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);

        long[] trReadTimeStamp = new long[tracks.size()];

        Sample paletteChange = null;
        while (in.getStreamPosition() < data.length) {
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            int chunkIdInt = in.readInt();
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            int chunkId = chunkIdInt;
            int track = (((chunkIdInt>>>24) & 0xff) - '0') * 10 + (((chunkIdInt >>> 16) & 0xff) - '0');
            if (track < 0 || track > 99 || track > tracks.size()) {
                throw new IOException("Illegal chunkId in IDX1:" + chunkId);
            }
            int flags = in.readInt();
            long offset = in.readUnsignedInt();
            long size = in.readUnsignedInt();
            Track tr = tracks.get(track);
            int duration = ((flags & 0x100) != 0) ? 0 : 1;
            if (tr.mediaType == AVIMediaType.AUDIO) {
                Format af = tr.format;
                duration = (int) (size * duration / af.get(FrameSizeKey));
                flags |= 0x10;
            }
            Sample s = new Sample(chunkId, duration, offset + moviOffset, size, (flags & 0x10) != 0);
            s.timeStamp = trReadTimeStamp[track];
            idx1.add(s);
            trReadTimeStamp[track] += duration;



            if ((s.chunkType & CHUNK_SUBTYPE_MASK) == PC_ID) {
                paletteChange = s;
            } else {
                if (paletteChange != null) {
                    s.header = paletteChange;
                }
                tr.samples.add(s);
            }
        }

        for (Track tr : tracks) {
            tr.readIndex = 0;
        }
    }

    public void close() throws IOException {
        in.close();
        for (Track tr : tracks) {
            tr.samples.clear();
        }
        tracks.clear();
    }
}
