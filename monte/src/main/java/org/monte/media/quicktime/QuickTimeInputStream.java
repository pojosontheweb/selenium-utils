
package org.monte.media.quicktime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.InflaterInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.io.ImageInputStreamAdapter;


public class QuickTimeInputStream extends AbstractQuickTimeStream {


    private class InputAtom extends Atom {

        private byte[] data;

        public InputAtom(String type, byte[] data) throws IOException {
            super(type, -1);
            this.data = data;
        }

        @Override
        public void finish() throws IOException {

        }

        @Override
        public long size() {
            return data.length;
        }
    }

    protected final ImageInputStream in;

    private boolean isRealized = false;
    static final HashSet<String> compositeAtoms;

    static {
        compositeAtoms = new HashSet<String>();
        compositeAtoms.add("moov");
        compositeAtoms.add("cmov");
        compositeAtoms.add("gmhd");
        compositeAtoms.add("trak");
        compositeAtoms.add("tref");
        compositeAtoms.add("meta");
        compositeAtoms.add("ilst");
        compositeAtoms.add("mdia");
        compositeAtoms.add("minf");
        compositeAtoms.add("udta");
        compositeAtoms.add("stbl");
        compositeAtoms.add("dinf");
        compositeAtoms.add("edts");
        compositeAtoms.add("clip");
        compositeAtoms.add("matt");
        compositeAtoms.add("rmra");
        compositeAtoms.add("rmda");
        compositeAtoms.add("tapt");
        compositeAtoms.add("mvex");
    }


    public QuickTimeInputStream(File file) throws IOException {

        this.in = new FileImageInputStream(file);
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        this.streamOffset = 0;
    }


    public QuickTimeInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        this.streamOffset = in.getStreamPosition();
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    public int getTrackCount() throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getMovieDuration() throws IOException {
        ensureRealized();
        long duration = 0;
        for (Track t : tracks) {
            duration = Math.max(duration, t.getTrackDuration(movieTimeScale));
        }
        return duration;
    }


    public Date getCreationTime() throws IOException {
        ensureRealized();
        return creationTime;
    }


    public Date getModificationTime() throws IOException {
        ensureRealized();
        return modificationTime;
    }


    public double getPreferredRate() throws IOException {
        ensureRealized();
        return preferredRate;
    }


    public double getPreferredVolume() throws IOException {
        ensureRealized();
        return preferredVolume;
    }


    public long getCurrentTime() throws IOException {
        ensureRealized();
        return currentTime;
    }


    public long getPosterTime() throws IOException {
        ensureRealized();
        return posterTime;
    }


    public long getPreviewDuration() throws IOException {
        ensureRealized();
        return previewDuration;
    }


    public long getPreviewTime() throws IOException {
        ensureRealized();
        return previewTime;
    }


    public double[] getMovieTransformationMatrix() throws IOException {
        ensureRealized();
        return movieMatrix.clone();
    }


    public long getMovieTimeScale() throws IOException {
        ensureRealized();
        return movieTimeScale;
    }


    public long getMediaTimeScale(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).mediaTimeScale;
    }


    public long getMediaDuration(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).mediaDuration;
    }


    public double[] getTransformationMatrix(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).matrix.clone();
    }


    protected void ensureRealized() throws IOException {
        if (!isRealized) {
            isRealized = true;
            readAllMetadata();
        }
    }

    private void readAllMetadata() throws IOException {
        long remainingSize = in.length();
        if (remainingSize == -1) {
            remainingSize = Long.MAX_VALUE;
        }

        in.seek(0);
        readAllMetadata(new DataAtomInputStream(new ImageInputStreamAdapter(in)), remainingSize, new HashMap<String, InputAtom>(), null);
    }

    private void readAllMetadata(DataAtomInputStream in, long remainingSize, HashMap<String, InputAtom> atoms, String path) throws IOException {
        long pos = 0;

        InputAtom atom;

        while (remainingSize > 0) {
            long size = in.readInt() & 0xffffffffL;
            int headerSize = 8;
            if (size == 0) {


                size = remainingSize;


                if (headerSize + 4 <= remainingSize) {
                    in.skipBytes(4);
                    headerSize += 4;
                }

            } else if (size == 1) {

                headerSize = 16;
                size = in.readLong();
            }
            String type;
            long atomSize = size;
            if (size > remainingSize) {

                size = remainingSize;
            }

            if (size - headerSize >= 0) {
                type = intToType(in.readInt());

            } else {
                type = "";
            }
            remainingSize -= size;



            if (type.equals("stsd")) {

            }

            if (compositeAtoms.contains(type) && size - headerSize >= 8) {
                if (type.equals("trak")) {
                    atoms.clear();
                    readAllMetadata(in, size - headerSize, atoms, path == null ? type : path + "." + type);
                    parseTrack(atoms);
                } else {
                    readAllMetadata(in, size - headerSize, atoms, path == null ? type : path + "." + type);
                }
            } else {
                byte[] data;
                if (type.equals("mdat")) {
                    data = new byte[0];
                    long skipped = 0;
                    while (skipped < size - headerSize) {
                        long skipValue = in.skipBytes(size - headerSize - skipped);
                        if (skipValue > 0) {
                            skipped += skipValue;
                        } else {
                            throw new IOException("unable to skip");
                        }
                    }
                } else {
                    if (size < headerSize) {

                        data = new byte[0];
                    } else {
                        data = new byte[(int) (size - headerSize)];
                        in.readFully(data);
                    }
                }
                atom = new InputAtom(type, data);
                atoms.put(path == null ? type : path + "." + type, atom);

                if (type.equals("cmvd")) {


                    try {
                        InputStream in2 = new InflaterInputStream(new ByteArrayInputStream(data, 4, data.length - 4));
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in2.read()) != -1) {
                            out.write(b);
                        }
                        in2.close();
                        out.close();

                        byte[] decompressed = out.toByteArray();
                        readAllMetadata(new DataAtomInputStream(
                                new ByteArrayInputStream(decompressed)),
                                decompressed.length,
                                atoms, path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (type.equals("mvhd")) {


                    parseMovieHeader(data);
                }
            }
            pos += size;
        }
    }

    public void close() throws IOException {
        in.close();
    }


    private void parseMovieHeader(byte[] data) throws IOException {

        DataAtomInputStream i = new DataAtomInputStream(new ByteArrayInputStream(data));
        int version = i.readByte();
        i.skipBytes(3);
        creationTime = i.readMacTimestamp();
        modificationTime = i.readMacTimestamp();
        movieTimeScale = i.readUInt();
        long movieDuration = i.readUInt();
        preferredRate = i.readFixed16D16();
        preferredVolume = i.readFixed8D8();
        i.skipBytes(10);



        movieMatrix[0] = i.readFixed16D16();
        movieMatrix[1] = i.readFixed16D16();
        movieMatrix[2] = i.readFixed2D30();
        movieMatrix[3] = i.readFixed16D16();
        movieMatrix[4] = i.readFixed16D16();
        movieMatrix[5] = i.readFixed2D30();
        movieMatrix[6] = i.readFixed16D16();
        movieMatrix[7] = i.readFixed16D16();
        movieMatrix[8] = i.readFixed2D30();
        previewTime = i.readUInt();
        previewDuration = i.readUInt();
        posterTime = i.readUInt();
        selectionTime = i.readUInt();
        selectionDuration = i.readUInt();
        currentTime = i.readUInt();
        long nextTrackId = i.readUInt();
    }


    private void parseTrack(HashMap<String, InputAtom> atoms) throws IOException {
        for (String p : atoms.keySet()) {
            System.out.println("QuickTimeInputStream " + p);
        }
        HashMap<String, Object> hdlrMap = parseHdlr(atoms.get("moov.trak.mdia.hdlr").data);
        String trackType = (String) hdlrMap.get("componentSubtype");
        Track t;
        if ("vide".equals(trackType)) {
            t = new VideoTrack();
        } else if ("soun".equals(trackType)) {
            t = new AudioTrack();
        } else {
            throw new IOException("Unsupported track type: " + trackType);
        }

        parseTkhd(t, atoms.get("moov.trak.tkhd").data);
        if (atoms.get("moov.trak.edts") != null) {
            parseEdts(t, atoms.get("moov.trak.edts").data);
        }
        if (atoms.get("moov.trak.mdhd") != null) {
            parseMdhd(t, atoms.get("moov.trak.mdhd").data);
        }

        if ("vide".equals(trackType)) {
            parseVideoTrack((VideoTrack) t, atoms);
        } else if ("soun".equals(trackType)) {
            parseAudioTrack((AudioTrack) t, atoms);
        } else {
            throw new IOException("Unsupported track type: " + trackType);
        }
        tracks.add(t);
    }

    private void parseVideoTrack(VideoTrack t, HashMap<String, InputAtom> atoms) throws IOException {
    }

    private void parseAudioTrack(AudioTrack t, HashMap<String, InputAtom> atoms) throws IOException {
    }


    private void parseTkhd(Track t, byte[] data) throws IOException {

        DataAtomInputStream dain = new DataAtomInputStream(new ByteArrayInputStream(data));

        int version = dain.readByte();
        dain.skipBytes(2);

        int trackHeaderFlags = dain.readUByte();
        Date creationTime = dain.readMacTimestamp();
        Date modificationTime = dain.readMacTimestamp();
        int trackId = dain.readInt();
        dain.skipBytes(4);
        long duration = dain.readUInt();
        dain.skipBytes(8);
        int layer = dain.readUShort();
        int alternateGroup = dain.readUShort();
        double volume = dain.readFixed8D8();
        dain.skipBytes(2);




        t.matrix[0] = dain.readFixed16D16();
        t.matrix[1] = dain.readFixed16D16();
        t.matrix[2] = dain.readFixed2D30();
        t.matrix[3] = dain.readFixed16D16();
        t.matrix[4] = dain.readFixed16D16();
        t.matrix[5] = dain.readFixed2D30();
        t.matrix[6] = dain.readFixed16D16();
        t.matrix[7] = dain.readFixed16D16();
        t.matrix[8] = dain.readFixed2D30();

        t.width = dain.readFixed16D16();
        t.height = dain.readFixed16D16();
    }


    private HashMap<String, Object> parseHdlr(byte[] data) throws IOException {

        DataAtomInputStream i = new DataAtomInputStream(new ByteArrayInputStream(data));

        int version = i.readByte();
        int flags = (i.readUShort() << 8) | (i.readUByte());
        String componentType = i.readType();
        String componentSubtype = i.readType();
        String componentManufactureer = i.readType();
        int componentFlags = i.readInt();
        int componentFlagsMask = i.readInt();
        String componentName = i.readPString();

        HashMap<String, Object> m = new HashMap<String, Object>();
        m.put("componentSubtype", componentSubtype);
        return m;
    }


    private void parseEdts(Track t, byte[] data) throws IOException {



        DataAtomInputStream dain = new DataAtomInputStream(new ByteArrayInputStream(data));

        int version = dain.readByte();
        int flags = (dain.readUShort() << 8) | (dain.readUByte());
        int numberOfEntries = dain.readInt();
        t.editList = new Edit[numberOfEntries];
        for (int i = 0; i < numberOfEntries; i++) {
            Edit edit = new Edit(dain.readInt(), dain.readInt(), dain.readFixed16D16());
            t.editList[i] = edit;
        }
    }


    private void parseMdhd(Track t, byte[] data) throws IOException {


        DataAtomInputStream dain = new DataAtomInputStream(new ByteArrayInputStream(data));

        int version = dain.readByte();
        int flags = (dain.readUShort() << 8) | (dain.readUByte());
        Date creationTime = dain.readMacTimestamp();
        Date modificationTime = dain.readMacTimestamp();
        t.mediaTimeScale = dain.readUInt();
        t.mediaDuration = dain.readUInt();
        short language = dain.readShort();
        short quality = dain.readShort();

    }
}
