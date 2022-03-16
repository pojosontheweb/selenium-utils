
package org.monte.media.quicktime;

import java.io.UnsupportedEncodingException;
import org.monte.media.Buffer;
import org.monte.media.Codec;
import org.monte.media.Format;
import org.monte.media.io.ImageOutputStreamAdapter;
import org.monte.media.math.Rational;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import javax.imageio.stream.ImageOutputStream;
import static org.monte.media.FormatKeys.*;


public class AbstractQuickTimeStream {

    
    protected ImageOutputStream out;
    
    protected long streamOffset;
    
    protected WideDataAtom mdatAtom;
    
    protected long mdatOffset;
    
    protected CompositeAtom moovAtom;
    
    protected Date creationTime;
    
    protected Date modificationTime;
    
    protected long movieTimeScale = 600;
    
    protected double preferredRate = 1d;
    
    protected double preferredVolume = 1d;
    
    protected long previewTime = 0;
    
    protected long previewDuration = 0;
    
    protected long posterTime = 0;
    
    protected long selectionTime = 0;
    
    protected long selectionDuration = 0;
    
    protected long currentTime = 0;
    
    protected ArrayList<Track> tracks = new ArrayList<Track>();
    
    protected double[] movieMatrix = {1, 0, 0, 0, 1, 0, 0, 0, 1};

    
    protected static enum States {

        REALIZED, STARTED, FINISHED, CLOSED;
    }
    
    protected States state = States.REALIZED;

    
    protected long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    
    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    protected static int typeToInt(String str) {
        int value = ((str.charAt(0) & 0xff) << 24) |
                ((str.charAt(1) & 0xff) << 16) |
                ((str.charAt(2) & 0xff) << 8) |
                (str.charAt(3) & 0xff);
        return value;
    }

    protected static String intToType(int id) {
        char[] b = new char[4];

        b[0] = (char) ((id >>> 24) & 0xff);
        b[1] = (char) ((id >>> 16) & 0xff);
        b[2] = (char) ((id >>> 8) & 0xff);
        b[3] = (char) (id & 0xff);
        return String.valueOf(b);
    }

    
    protected abstract class Atom {

        
        protected String type;
        
        protected long offset;

        
        public Atom(String type, long offset) {
            this.type = type;
            this.offset = offset;
        }

        
        public abstract void finish() throws IOException;

        
        public abstract long size();
    }

    
    protected class CompositeAtom extends DataAtom {

        protected LinkedList<Atom> children;

        
        public CompositeAtom(String type) throws IOException {
            super(type);
            children = new LinkedList<Atom>();
        }

        public void add(Atom child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
            }
            children.add(child);
        }

        
        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                headerData.writeInt((int) size());
                headerData.writeType(type);
                for (Atom child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                finished = true;
            }
        }

        @Override
        public long size() {
            long length = 8 + data.size();
            for (Atom child : children) {
                length += child.size();
            }
            return length;
        }
    }

    
    protected class DataAtom extends Atom {

        protected DataAtomOutputStream data;
        protected boolean finished;

        
        public DataAtom(String type) throws IOException {
            super(type, getRelativeStreamPosition());
            out.writeLong(0);
            data = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
        }

        public DataAtomOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataAtom is finished");
            }
            return data;
        }

        
        public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long sizeBefore = size();

                if (size() > 0xffffffffL) {
                    throw new IOException("DataAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                headerData.writeUInt(size());
                headerData.writeType(type);
                seekRelative(pointer);
                finished = true;
                long sizeAfter = size();
                if (sizeBefore != sizeAfter) {
                    System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
                }
            }
        }

        @Override
        public long size() {
            return 8 + data.size();
        }
    }

    
    protected class WideDataAtom extends Atom {

        protected DataAtomOutputStream data;
        protected boolean finished;

        
        public WideDataAtom(String type) throws IOException {
            super(type, getRelativeStreamPosition());
            out.writeLong(0);
            out.writeLong(0);
            data = new DataAtomOutputStream(new ImageOutputStreamAdapter(out)) {
                @Override
                public void flush() throws IOException {

                }
            };
        }

        public DataAtomOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("Atom is finished");
            }
            return data;
        }

        
        public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                long finishedSize = size();
                if (finishedSize <= 0xffffffffL) {
                    headerData.writeUInt(8);
                    headerData.writeType("wide");
                    headerData.writeUInt(finishedSize - 8);
                    headerData.writeType(type);
                } else {
                    headerData.writeInt(1);
                    headerData.writeType(type);
                    headerData.writeLong(finishedSize - 8);
                }

                seekRelative(pointer);
                finished = true;
            }
        }

        @Override
        public long size() {
            return 16 + data.size();
        }
    }

    
    protected abstract static class Group {

        protected Sample firstSample;
        protected Sample lastSample;
        protected long sampleCount;
        protected final static long maxSampleCount = Integer.MAX_VALUE;

        protected Group(Sample firstSample) {
            this.firstSample = this.lastSample = firstSample;
            sampleCount = 1;
        }

        protected Group(Sample firstSample, Sample lastSample, long sampleCount) {
            this.firstSample = firstSample;
            this.lastSample = lastSample;
            this.sampleCount = sampleCount;
            if (sampleCount > maxSampleCount) {
                throw new IllegalArgumentException("Capacity exceeded");
            }
        }

        protected Group(Group group) {
            this.firstSample = group.firstSample;
            this.lastSample = group.lastSample;
            sampleCount = group.sampleCount;
        }

        
        protected boolean maybeAddSample(Sample sample) {
            if (sampleCount < maxSampleCount) {
                lastSample = sample;
                sampleCount++;
                return true;
            }
            return false;
        }

        
        protected boolean maybeAddChunk(Chunk chunk) {
            if (sampleCount + chunk.sampleCount <= maxSampleCount) {
                lastSample = chunk.lastSample;
                sampleCount += chunk.sampleCount;
                return true;
            }
            return false;
        }

        public long getSampleCount() {
            return sampleCount;
        }
    }

    
    protected static class Sample {

        
        long offset;
        
        long length;
        
        long duration;

        
        public Sample(long duration, long offset, long length) {
            this.duration = duration;
            this.offset = offset;
            this.length = length;
        }
    }

    
    protected static class TimeToSampleGroup extends Group {

        public TimeToSampleGroup(Sample firstSample) {
            super(firstSample);
        }

        public TimeToSampleGroup(Group group) {
            super(group);
        }

        
        @Override
        public boolean maybeAddSample(Sample sample) {
            if (firstSample.duration == sample.duration) {
                return super.maybeAddSample(sample);
            }
            return false;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.duration == chunk.firstSample.duration) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        
        public long getSampleDuration() {
            return firstSample.duration;
        }
    }

    
    protected static class SampleSizeGroup extends Group {

        public SampleSizeGroup(Sample firstSample) {
            super(firstSample);
        }

        public SampleSizeGroup(Group group) {
            super(group);
        }

        
        @Override
        public boolean maybeAddSample(Sample sample) {
            if (firstSample.length == sample.length) {
                return super.maybeAddSample(sample);
            }
            return false;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.length == chunk.firstSample.length) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        
        public long getSampleLength() {
            return firstSample.length;
        }
    }

    
    protected static class Chunk extends Group {

        protected int sampleDescriptionId;

        
        public Chunk(Sample firstSample, int sampleDescriptionId) {
            super(firstSample);
            this.sampleDescriptionId = sampleDescriptionId;
        }

        
        public Chunk(Sample firstSample, Sample lastSample, int sampleCount, int sampleDescriptionId) {
            super(firstSample, lastSample, sampleCount);
            this.sampleDescriptionId = sampleDescriptionId;
        }

        
        public boolean maybeAddSample(Sample sample, int sampleDescriptionId) {
            if (sampleDescriptionId == this.sampleDescriptionId
                    && lastSample.offset + lastSample.length == sample.offset) {
                return super.maybeAddSample(sample);
            }
            return false;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (sampleDescriptionId == chunk.sampleDescriptionId
                    && lastSample.offset + lastSample.length == chunk.firstSample.offset) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        
        public long getChunkOffset() {
            return firstSample.offset;
        }
    }

    
    protected abstract class Track {


        
        protected final MediaType mediaType;
        
        protected Format format;
        
        protected long mediaTimeScale = 600;
        
        protected String mediaCompressionType;
        
        protected String mediaCompressorName;
        
        protected ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        
        protected ArrayList<TimeToSampleGroup> timeToSamples = new ArrayList<TimeToSampleGroup>();
        
        protected ArrayList<SampleSizeGroup> sampleSizes = new ArrayList<SampleSizeGroup>();
        
        protected ArrayList<Long> syncSamples = null;
        
        protected long sampleCount = 0;
        
        protected long mediaDuration = 0;
        
        protected Edit[] editList;
        
        protected int syncInterval;
        
        protected Codec codec;
        protected Buffer outputBuffer;
        protected Buffer inputBuffer;
        
        protected Rational inputTime;
        
        protected Rational writeTime;
        
        protected double[] matrix = {
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
        };
        protected double width, height;

        public Track(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public void addSample(Sample sample, int sampleDescriptionId, boolean isSyncSample) {
            mediaDuration += sample.duration;
            sampleCount++;



            if (isSyncSample) {
                if (syncSamples != null) {
                    syncSamples.add(sampleCount);
                }
            } else {
                if (syncSamples == null) {
                    syncSamples = new ArrayList<Long>();
                    for (long i = 1; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            }


            if (timeToSamples.isEmpty()
                    || !timeToSamples.get(timeToSamples.size() - 1).maybeAddSample(sample)) {
                timeToSamples.add(new TimeToSampleGroup(sample));
            }
            if (sampleSizes.isEmpty()
                    || !sampleSizes.get(sampleSizes.size() - 1).maybeAddSample(sample)) {
                sampleSizes.add(new SampleSizeGroup(sample));
            }
            if (chunks.isEmpty()
                    || !chunks.get(chunks.size() - 1).maybeAddSample(sample, sampleDescriptionId)) {
                chunks.add(new Chunk(sample, sampleDescriptionId));
            }
        }

        public void addChunk(Chunk chunk, boolean isSyncSample) {
            mediaDuration += chunk.firstSample.duration * chunk.sampleCount;
            sampleCount += chunk.sampleCount;



            if (isSyncSample) {
                if (syncSamples != null) {
                    for (long i = sampleCount - chunk.sampleCount; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            } else {
                if (syncSamples == null) {
                    syncSamples = new ArrayList<Long>();
                    for (long i = 1; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            }


            if (timeToSamples.isEmpty()
                    || !timeToSamples.get(timeToSamples.size() - 1).maybeAddChunk(chunk)) {
                timeToSamples.add(new TimeToSampleGroup(chunk));
            }
            if (sampleSizes.isEmpty()
                    || !sampleSizes.get(sampleSizes.size() - 1).maybeAddChunk(chunk)) {
                sampleSizes.add(new SampleSizeGroup(chunk));
            }
            if (chunks.isEmpty()
                    || !chunks.get(chunks.size() - 1).maybeAddChunk(chunk)) {
                chunks.add(chunk);
            }
        }

        public boolean isEmpty() {
            return sampleCount == 0;
        }

        public long getSampleCount() {
            return sampleCount;
        }

        
        public long getTrackDuration(long movieTimeScale) {
            if (editList == null || editList.length == 0) {
                return mediaDuration * movieTimeScale / mediaTimeScale;
            } else {
                long duration = 0;
                for (int i = 0; i < editList.length; ++i) {
                    duration += editList[i].trackDuration;
                }
                return duration;
            }
        }

         
        protected void writeTrackAtoms(int trackIndex, CompositeAtom moovAtom, Date modificationTime) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;

            
            CompositeAtom trakAtom = new CompositeAtom("trak");
            moovAtom.add(trakAtom);

            
            leaf = new DataAtom("tkhd");
            trakAtom.add(leaf);
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);

            d.write(0xf);
















            d.writeMacTimestamp(creationTime);





            d.writeMacTimestamp(modificationTime);





            d.writeInt(trackIndex + 1);



            d.writeInt(0);


            d.writeUInt(getTrackDuration(movieTimeScale));







            d.writeLong(0);


            d.writeShort(0);





            d.writeShort(0);






            d.writeFixed8D8(mediaType == MediaType.AUDIO ? 1 : 0);



            d.writeShort(0);


            d.writeFixed16D16(matrix[0]);
            d.writeFixed16D16(matrix[1]);
            d.writeFixed2D30(matrix[2]);
            d.writeFixed16D16(matrix[3]);
            d.writeFixed16D16(matrix[4]);
            d.writeFixed2D30(matrix[5]);
            d.writeFixed16D16(matrix[6]);
            d.writeFixed16D16(matrix[7]);
            d.writeFixed2D30(matrix[8]);




            d.writeFixed16D16(mediaType == MediaType.VIDEO ? ((VideoTrack) this).width : 0);


            d.writeFixed16D16(mediaType == MediaType.VIDEO ? ((VideoTrack) this).height : 0);


            
            CompositeAtom edtsAtom = new CompositeAtom("edts");
            trakAtom.add(edtsAtom);

            
            
            leaf = new DataAtom("elst");
            edtsAtom.add(leaf);
            d = leaf.getOutputStream();

            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);

            Edit[] elist = editList;
            if (elist == null || elist.length == 0) {
                d.writeUInt(1);
                d.writeUInt(getTrackDuration(movieTimeScale));
                d.writeUInt(0);
                d.writeFixed16D16(1);
            } else {
                d.writeUInt(elist.length);
                for (int i = 0; i < elist.length; ++i) {
                    d.writeUInt(elist[i].trackDuration);
                    d.writeUInt(elist[i].mediaTime);
                    d.writeUInt(elist[i].mediaRate);
                }
            }


            
            CompositeAtom mdiaAtom = new CompositeAtom("mdia");
            trakAtom.add(mdiaAtom);

            
            leaf = new DataAtom("mdhd");
            mdiaAtom.add(leaf);
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeMacTimestamp(creationTime);





            d.writeMacTimestamp(modificationTime);





            d.writeUInt(mediaTimeScale);




            d.writeUInt(mediaDuration);


            d.writeShort(0);




            d.writeShort(0);



            
            leaf = new DataAtom("hdlr");
            mdiaAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeType("mhlr");




            d.writeType(mediaType == MediaType.VIDEO ? "vide" : "soun");







            if (mediaType == MediaType.AUDIO) {
                d.writeType("appl");
            } else {
                d.writeUInt(0);
            }



            d.writeUInt(mediaType == MediaType.AUDIO ? 268435456L : 0);


            d.writeUInt(mediaType == MediaType.AUDIO ? 65941 : 0);


            d.writePString(mediaType == MediaType.AUDIO ? "Apple Sound Media Handler" : "");




            
            writeMediaInformationAtoms(mdiaAtom);
        }

        protected void writeMediaInformationAtoms(CompositeAtom mdiaAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;
            
            CompositeAtom minfAtom = new CompositeAtom("minf");
            mdiaAtom.add(minfAtom);

            
            writeMediaInformationHeaderAtom(minfAtom);


            



            leaf = new DataAtom("hdlr");
            minfAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeType("dhlr");




            d.writeType("alis");






            if (mediaType == MediaType.AUDIO) {
                d.writeType("appl");
            } else {
                d.writeUInt(0);
            }



            d.writeUInt(mediaType == MediaType.AUDIO ? 268435457L : 0);


            d.writeInt(mediaType == MediaType.AUDIO ? 65967 : 0);


            d.writePString("Apple Alias Data Handler");




            
            CompositeAtom dinfAtom = new CompositeAtom("dinf");
            minfAtom.add(dinfAtom);

            


            leaf = new DataAtom("dref");
            dinfAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeInt(1);


            d.writeInt(12);



            d.writeType("alis");




            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0x1);










            
            writeSampleTableAtoms(minfAtom);
        }

        protected abstract void writeMediaInformationHeaderAtom(CompositeAtom minfAtom) throws IOException;

        protected abstract void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException;

        protected void writeSampleTableAtoms(CompositeAtom minfAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;

            
            CompositeAtom stblAtom = new CompositeAtom("stbl");
            minfAtom.add(stblAtom);

            
            writeSampleDescriptionAtom(stblAtom);


            




            leaf = new DataAtom("stts");
            stblAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeUInt(timeToSamples.size());



            for (TimeToSampleGroup tts : timeToSamples) {
                d.writeUInt(tts.getSampleCount());



                d.writeUInt(tts.getSampleDuration());


            }
            



            leaf = new DataAtom("stsc");
            stblAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            int entryCount = 0;
            long previousSampleCount = -1;
            long previousSampleDescriptionId = -1;
            for (Chunk c : chunks) {
                if (c.sampleCount != previousSampleCount
                        || c.sampleDescriptionId != previousSampleDescriptionId) {
                    previousSampleCount = c.sampleCount;
                    previousSampleDescriptionId = c.sampleDescriptionId;
                    entryCount++;
                }
            }

            d.writeInt(entryCount);


            int firstChunk = 1;
            previousSampleCount = -1;
            previousSampleDescriptionId = -1;
            for (Chunk c : chunks) {
                if (c.sampleCount != previousSampleCount
                        || c.sampleDescriptionId != previousSampleDescriptionId) {
                    previousSampleCount = c.sampleCount;
                    previousSampleDescriptionId = c.sampleDescriptionId;

                    d.writeUInt(firstChunk);


                    d.writeUInt(c.sampleCount);


                    d.writeInt(c.sampleDescriptionId);





                }
                firstChunk++;
            }

        
            if (syncSamples != null) {
                leaf = new DataAtom("stss");
                stblAtom.add(leaf);
                
                d = leaf.getOutputStream();
                d.write(0);


                d.write(0);
                d.write(0);
                d.write(0);


                d.writeUInt(syncSamples.size());



                for (Long number : syncSamples) {
                    d.writeUInt(number);


                }
            }


            





            leaf = new DataAtom("stsz");
            stblAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            int sampleUnit = mediaType == MediaType.AUDIO
                    && ((AudioTrack) this).soundCompressionId != -2
                    ? ((AudioTrack) this).soundSampleSize / 8 * ((AudioTrack) this).soundNumberOfChannels
                    : 1;
            if (sampleSizes.size() == 1) {
                d.writeUInt(sampleSizes.get(0).getSampleLength() / sampleUnit);





                d.writeUInt(sampleSizes.get(0).getSampleCount());



            } else {
                d.writeUInt(0);






                long count = 0;
                for (SampleSizeGroup s : sampleSizes) {
                    count += s.sampleCount;
                }
                d.writeUInt(count);



                for (SampleSizeGroup s : sampleSizes) {
                    long sampleSize = s.getSampleLength() / sampleUnit;
                    for (int i = 0; i < s.sampleCount; i++) {
                        d.writeUInt(sampleSize);




                    }
                }
            }

        





            if (chunks.isEmpty() || chunks.get(chunks.size() - 1).getChunkOffset() <= 0xffffffffL) {
                
                leaf = new DataAtom("stco");
                stblAtom.add(leaf);
                
                d = leaf.getOutputStream();
                d.write(0);


                d.write(0);
                d.write(0);
                d.write(0);


                d.writeUInt(chunks.size());


                for (Chunk c : chunks) {
                    d.writeUInt(c.getChunkOffset() + mdatOffset);




                }
            } else {
                
                leaf = new DataAtom("co64");
                stblAtom.add(leaf);
                
                d = leaf.getOutputStream();
                d.write(0);


                d.write(0);
                d.write(0);
                d.write(0);


                d.writeUInt(chunks.size());



                for (Chunk c : chunks) {
                    d.writeLong(c.getChunkOffset());




                }
            }
        }
    }

    protected class VideoTrack extends Track {


        
        protected float videoQuality = 0.97f;
        
        protected int videoDepth = -1;
        
        protected IndexColorModel videoColorTable;

        public VideoTrack() {
            super(MediaType.VIDEO);
        }

        @Override
        protected void writeMediaInformationHeaderAtom(CompositeAtom minfAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;

            
            leaf = new DataAtom("vmhd");
            minfAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0x1);







            d.writeShort(0x40);







            d.writeUShort(0);
            d.writeUShort(0);
            d.writeUShort(0);


        }

        @Override
        protected void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException {
            CompositeAtom leaf;
            DataAtomOutputStream d;

            







            leaf = new CompositeAtom("stsd");
            stblAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeInt(1);



            d.writeInt(86);

            d.writeType(mediaCompressionType);





            d.write(new byte[6]);


            d.writeShort(1);










            d.writeShort(0);




            d.writeShort(0);


            d.writeType("java");




            d.writeInt(0);



            d.writeInt(512);



            d.writeUShort((int) width);



            d.writeUShort((int) height);


            d.writeFixed16D16(72.0);



            d.writeFixed16D16(72.0);



            d.writeInt(0);


            d.writeShort(1);



            d.writePString(mediaCompressorName, 32);



            d.writeShort(videoDepth);







            d.writeShort(videoColorTable == null ? -1 : 0);









            if (videoColorTable != null) {
                writeColorTableAtom(leaf);
            }
        }

        
        protected void writeColorTableAtom(CompositeAtom stblAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;
            leaf = new DataAtom("ctab");
            stblAtom.add(leaf);

            d = leaf.getOutputStream();

            d.writeUInt(0);
            d.writeUShort(0x8000);
            d.writeUShort(videoColorTable.getMapSize() - 1);




            for (int i = 0, n = videoColorTable.getMapSize(); i < n; ++i) {



                d.writeUShort(0);
                d.writeUShort((videoColorTable.getRed(i) << 8) | videoColorTable.getRed(i));
                d.writeUShort((videoColorTable.getGreen(i) << 8) | videoColorTable.getGreen(i));
                d.writeUShort((videoColorTable.getBlue(i) << 8) | videoColorTable.getBlue(i));
            }
        }
    }

    protected class AudioTrack extends Track {


        
        protected int soundNumberOfChannels;
        
        protected int soundSampleSize;
        
        protected int soundCompressionId;
        
        protected long soundSamplesPerPacket;
        
        protected int soundBytesPerPacket;
        
        protected int soundBytesPerFrame;
        
        protected int soundBytesPerSample;
        
        protected double soundSampleRate;
        
        protected byte[] stsdExtensions = new byte[0];

        public AudioTrack() {
            super(MediaType.AUDIO);
        }

        @Override
        protected void writeMediaInformationHeaderAtom(CompositeAtom minfAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;

            
            leaf = new DataAtom("smhd");
            minfAtom.add(leaf);
            
            d = leaf.getOutputStream();
            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeFixed8D8(0);












            d.writeUShort(0);


        }

        @Override
        protected void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException {

            DataAtom leaf;
            DataAtomOutputStream d;

            







            leaf = new DataAtom("stsd");
            stblAtom.add(leaf);
            
            d = leaf.getOutputStream();



            d.write(0);


            d.write(0);
            d.write(0);
            d.write(0);


            d.writeInt(1);





            d.writeUInt(4 + 12 + 20 + 16 + stsdExtensions.length);


            d.writeType(mediaCompressionType);




            d.write(new byte[6]);


            d.writeUShort(1);








            d.writeUShort(1);


            d.writeUShort(0);


            d.writeUInt(0);


            d.writeUShort(soundNumberOfChannels);




            d.writeUShort(soundSampleSize);





            d.writeUShort(soundCompressionId);





            d.writeUShort(0);


            d.writeFixed16D16(soundSampleRate);









            d.writeUInt(soundSamplesPerPacket);








            d.writeUInt(soundBytesPerPacket);










            d.writeUInt(soundBytesPerFrame);







            d.writeUInt(soundBytesPerSample);








            d.write(stsdExtensions);
        }
    }

    
    public static class Edit {

        
        public int trackDuration;
        
        public int mediaTime;
        
        public int mediaRate;

        
        public Edit(int trackDuration, int mediaTime, double mediaRate) {
            if (trackDuration < 0) {
                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);
            }
            if (mediaTime < -1) {
                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);
            }
            if (mediaRate <= 0) {
                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);
            }
            this.trackDuration = trackDuration;
            this.mediaTime = mediaTime;
            this.mediaRate = (int) (mediaRate * (1 << 16));
        }

        
        public Edit(int trackDuration, int mediaTime, int mediaRate) {
            if (trackDuration < 0) {
                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);
            }
            if (mediaTime < -1) {
                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);
            }
            if (mediaRate <= 0) {
                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);
            }
            this.trackDuration = trackDuration;
            this.mediaTime = mediaTime;
            this.mediaRate = mediaRate;
        }
    }
}
