
package org.monte.media.avi;

import org.monte.media.io.ImageOutputStreamAdapter;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import javax.imageio.*;
import javax.imageio.stream.*;


public class AVIOutputStreamOLD {


    private ImageOutputStream out;

    private long streamOffset;

    private Object previousData;


    public static enum AVIVideoFormat {

        RAW, RLE, JPG, PNG;
    }

    private AVIVideoFormat videoFormat;

    private float quality = 0.9f;

    private Date creationTime;

    private int imgWidth = -1;

    private int imgHeight = -1;

    private int imgDepth = 24;

    private IndexColorModel palette;
    private IndexColorModel previousPalette;

    private RunLengthCodec encoder;

    private int timeScale = 1;

    private int frameRate = 30;

    private int syncInterval = 30;


    private static enum States {

        STARTED, FINISHED, CLOSED;
    }

    private States state = States.FINISHED;


    private static class Sample {

        String chunkType;

        long offset;

        long length;

        int duration;

        boolean isSync;


        public Sample(String chunkId, int duration, long offset, long length, boolean isSync) {
            this.chunkType = chunkId;
            this.duration = duration;
            this.offset = offset;
            this.length = length;
            this.isSync = isSync;
        }
    }

    private LinkedList<Sample> videoFrames;

    private CompositeChunk aviChunk;

    private CompositeChunk moviChunk;

    FixedSizeDataChunk avihChunk;

    FixedSizeDataChunk strhChunk;

    FixedSizeDataChunk strfChunk;


    private abstract class Chunk {


        protected String chunkType;

        protected long offset;


        public Chunk(String chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getRelativeStreamPosition();
        }


        public abstract void finish() throws IOException;


        public abstract long size();
    }


    private class CompositeChunk extends Chunk {


        protected String compositeType;
        private LinkedList<Chunk> children;
        private boolean finished;


        public CompositeChunk(String compositeType, String chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;

            out.writeLong(0);
            out.writeInt(0);
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

                DataChunkOutputStream headerData = new DataChunkOutputStream(new ImageOutputStreamAdapter(out),false);
                headerData.writeType(compositeType);
                headerData.writeUInt(size() - 8);
                headerData.writeType(chunkType);
                for (Chunk child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                if (size() % 2 == 1) {
                    out.writeByte(0);
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


    private class DataChunk extends Chunk {

        private DataChunkOutputStream data;
        private boolean finished;


        public DataChunk(String name) throws IOException {
            super(name);
            out.writeLong(0);
            data = new DataChunkOutputStream(new ImageOutputStreamAdapter(out), false);
        }

        public DataChunkOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataChunk is finished");
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
System.out.println("sizeBefore:"+sizeBefore);
                if (size() > 0xffffffffL) {
                    throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                DataChunkOutputStream headerData = new DataChunkOutputStream(new ImageOutputStreamAdapter(out),false);
                headerData.writeType(chunkType);
                headerData.writeUInt(size() - 8);
                seekRelative(pointer);
                if (size() % 2 == 1) {
                    out.writeByte(0);
                }
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


    private class FixedSizeDataChunk extends Chunk {

        private DataChunkOutputStream data;
        private boolean finished;
        private long fixedSize;


        public FixedSizeDataChunk(String chunkType, long fixedSize) throws IOException {
            super(chunkType);
            this.fixedSize = fixedSize;
            data = new DataChunkOutputStream(new ImageOutputStreamAdapter(out),false);
            data.writeType(chunkType);
            data.writeUInt(fixedSize);
            data.clearCount();


            byte[] buf = new byte[(int) Math.min(512, fixedSize)];
            long written = 0;
            while (written < fixedSize) {
                data.write(buf, 0, (int) Math.min(buf.length, fixedSize - written));
                written += Math.min(buf.length, fixedSize - written);
            }
            if (fixedSize % 2 == 1) {
                out.writeByte(0);
            }
            seekToStartOfData();
        }

        public DataChunkOutputStream getOutputStream() {

            return data;
        }


        public long getOffset() {
            return offset;
        }

        public void seekToStartOfData() throws IOException {
            seekRelative(offset + 8);
            data.clearCount();
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


    public AVIOutputStreamOLD(File file, AVIVideoFormat format) throws IOException {
        this(file,format,24);
    }

    public AVIOutputStreamOLD(File file, AVIVideoFormat format, int bitsPerPixel) throws IOException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }

        if (file.exists()) {
            file.delete();
        }
        this.out = new FileImageOutputStream(file);
        this.streamOffset = 0;
        this.videoFormat = format;
        this.videoFrames = new LinkedList<Sample>();
        this.imgDepth = bitsPerPixel;
        if (imgDepth == 4) {
            byte[] gray = new byte[16];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) ((i << 4) | i);
            }
            palette = new IndexColorModel(4, 16, gray, gray, gray);
        } else if (imgDepth == 8) {
            byte[] gray = new byte[256];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) i;
            }
            palette = new IndexColorModel(8, 256, gray, gray, gray);
        }

    }


    public AVIOutputStreamOLD(ImageOutputStream out, AVIVideoFormat format) throws IOException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        this.out = out;
        this.streamOffset = out.getStreamPosition();
        this.videoFormat = format;
        this.videoFrames = new LinkedList<Sample>();
    }


    public void setTimeScale(int newValue) {
        if (newValue <= 0) {
            throw new IllegalArgumentException("timeScale must be greater 0");
        }
        this.timeScale = newValue;
    }


    public int getTimeScale() {
        return timeScale;
    }


    public void setFrameRate(int newValue) {
        if (newValue <= 0) {
            throw new IllegalArgumentException("frameDuration must be greater 0");
        }
        if (state == States.STARTED) {
            throw new IllegalStateException("frameDuration must be set before the first frame is written");
        }
        this.frameRate = newValue;
    }


    public int getFrameRate() {
        return frameRate;
    }


    public void setPalette(IndexColorModel palette) {
        this.palette = palette;
    }


    public void setVideoCompressionQuality(float newValue) {
        this.quality = newValue;
    }


    public float getVideoCompressionQuality() {
        return quality;
    }


    public void setVideoDimension(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be greater zero.");
        }
        this.imgWidth = width;
        this.imgHeight = height;
    }


    public Dimension getVideoDimension() {
        if (imgWidth < 1 || imgHeight < 1) {
            return null;
        }
        return new Dimension(imgWidth, imgHeight);
    }


    private void ensureStarted() throws IOException {
        if (state != States.STARTED) {
            creationTime = new Date();
            writeProlog();
            state = States.STARTED;
        }
    }


    public void writeFrame(BufferedImage image) throws IOException {
        ensureOpen();
        ensureStarted();


        if (imgWidth == -1) {
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();
        } else {

            if (imgWidth != image.getWidth() || imgHeight != image.getHeight()) {
                throw new IllegalArgumentException("Dimensions of image[" + videoFrames.size()
                        + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                        + ") differs from image[0] (width="
                        + imgWidth + ", height=" + imgHeight);
            }
        }

        DataChunk videoFrameChunk;
        long offset = getRelativeStreamPosition();
        boolean isSync = true;
        switch (videoFormat) {
            case RAW: {
                switch (imgDepth) {
                    case 4: {
                        IndexColorModel imgPalette = (IndexColorModel) image.getColorModel();
                        int[] imgRGBs = new int[16];
                        imgPalette.getRGBs(imgRGBs);
                        int[] previousRGBs = new int[16];
                        if (previousPalette == null) {
                            previousPalette = palette;
                        }
                        previousPalette.getRGBs(previousRGBs);
                        if (!Arrays.equals(imgRGBs, previousRGBs)) {
                            previousPalette = imgPalette;
                            DataChunk paletteChangeChunk = new DataChunk("00pc");

                            int first = 0;
                            int last = imgPalette.getMapSize() - 1;

                            DataChunkOutputStream pOut = paletteChangeChunk.getOutputStream();
                            pOut.writeByte(first);
                            pOut.writeByte(last - first + 1);
                            pOut.writeShort(0);

                            for (int i = first; i <= last; i++) {
                                pOut.writeByte((imgRGBs[i] >>> 16) & 0xff);
                                pOut.writeByte((imgRGBs[i] >>> 8) & 0xff);
                                pOut.writeByte(imgRGBs[i] & 0xff);
                                pOut.writeByte(0);
                            }

                            moviChunk.add(paletteChangeChunk);
                            paletteChangeChunk.finish();
                            long length = getRelativeStreamPosition() - offset;
                            videoFrames.add(new Sample(paletteChangeChunk.chunkType, 0, offset, length - 8, false));
                            offset = getRelativeStreamPosition();
                        }

                        videoFrameChunk = new DataChunk("00db");
                        byte[] rgb8 = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        byte[] rgb4 = new byte[imgWidth / 2];
                        for (int y = (imgHeight - 1) * imgWidth; y >= 0; y -= imgWidth) {
                            for (int x = 0, xx = 0, n = imgWidth; x < n; x += 2, ++xx) {
                                rgb4[xx] = (byte) (((rgb8[y + x] & 0xf) << 4) | (rgb8[y + x + 1] & 0xf));
                            }
                            videoFrameChunk.getOutputStream().write(rgb4);
                        }
                        break;
                    }
                    case 8: {
                        IndexColorModel imgPalette = (IndexColorModel) image.getColorModel();
                        int[] imgRGBs = new int[256];
                        imgPalette.getRGBs(imgRGBs);
                        int[] previousRGBs = new int[256];
                        if (previousPalette == null) {
                            previousPalette = palette;
                        }
                        previousPalette.getRGBs(previousRGBs);
                        if (!Arrays.equals(imgRGBs, previousRGBs)) {
                            previousPalette = imgPalette;
                            DataChunk paletteChangeChunk = new DataChunk("00pc");

                            int first = 0;
                            int last = imgPalette.getMapSize() - 1;

                            DataChunkOutputStream pOut = paletteChangeChunk.getOutputStream();
                            pOut.writeByte(first);
                            pOut.writeByte(last - first + 1);
                            pOut.writeShort(0);

                            for (int i = first; i <= last; i++) {
                                pOut.writeByte((imgRGBs[i] >>> 16) & 0xff);
                                pOut.writeByte((imgRGBs[i] >>> 8) & 0xff);
                                pOut.writeByte(imgRGBs[i] & 0xff);
                                pOut.writeByte(0);
                            }

                            moviChunk.add(paletteChangeChunk);
                            paletteChangeChunk.finish();
                            long length = getRelativeStreamPosition() - offset;
                            videoFrames.add(new Sample(paletteChangeChunk.chunkType, 0, offset, length - 8, false));
                            offset = getRelativeStreamPosition();
                        }

                        videoFrameChunk = new DataChunk("00db");
                        byte[] rgb8 = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        for (int y = (imgHeight - 1) * imgWidth; y >= 0; y -= imgWidth) {
                            videoFrameChunk.getOutputStream().write(rgb8, y, imgWidth);
                        }
                        break;
                    }
                    default: {
                        videoFrameChunk = new DataChunk("00db");
                        WritableRaster raster = image.getRaster();
                        int[] raw = new int[imgWidth * 3];
                        byte[] bytes = new byte[imgWidth * 3];
                        for (int y = imgHeight - 1; y >= 0; --y) {
                            raster.getPixels(0, y, imgWidth, 1, raw);
                            for (int x = 0, n = imgWidth * 3; x < n; x += 3) {
                                bytes[x + 2] = (byte) raw[x];
                                bytes[x + 1] = (byte) raw[x + 1];
                                bytes[x] = (byte) raw[x + 2];
                            }
                            videoFrameChunk.getOutputStream().write(bytes);
                        }
                        break;
                    }
                }
                break;
            }
            case RLE: {
                if (encoder == null) {
                    encoder = new RunLengthCodec();
                }

                isSync = videoFrames.size() % syncInterval == 0;

                switch (imgDepth) {
                    case 4: {
                        throw new UnsupportedOperationException("RLE 4-bit not implemented.");
                    }
                    case 8: {
                        IndexColorModel imgPalette = (IndexColorModel) image.getColorModel();
                        int[] imgRGBs = new int[256];
                        imgPalette.getRGBs(imgRGBs);
                        int[] previousRGBs = new int[256];
                        if (previousPalette == null) {
                            previousPalette = palette;
                        }
                        previousPalette.getRGBs(previousRGBs);
                        if (!Arrays.equals(imgRGBs, previousRGBs)) {
                            isSync = true;

                            previousPalette = imgPalette;
                            DataChunk paletteChangeChunk = new DataChunk("00pc");

                            int first = 0;
                            int last = imgPalette.getMapSize() - 1;

                            DataChunkOutputStream pOut = paletteChangeChunk.getOutputStream();
                            pOut.writeByte(first);
                            pOut.writeByte(last - first + 1);
                            pOut.writeShort(0);

                            for (int i = first; i <= last; i++) {
                                pOut.writeByte((imgRGBs[i] >>> 16) & 0xff);
                                pOut.writeByte((imgRGBs[i] >>> 8) & 0xff);
                                pOut.writeByte(imgRGBs[i] & 0xff);
                                pOut.writeByte(0);
                            }

                            moviChunk.add(paletteChangeChunk);
                            paletteChangeChunk.finish();
                            long length = getRelativeStreamPosition() - offset;
                            videoFrames.add(new Sample(paletteChangeChunk.chunkType, 0, offset, length - 8, false));
                            offset = getRelativeStreamPosition();
                        }

                        videoFrameChunk = new DataChunk("00dc");
                        byte[] rgb8 = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        if (isSync) {
                            encoder.writeKey8(videoFrameChunk.getOutputStream(), rgb8, 0, imgWidth, imgWidth, imgHeight);
                        } else {
                            encoder.writeDelta8(videoFrameChunk.getOutputStream(), rgb8, (byte[]) previousData, 0, imgWidth, imgWidth, imgHeight);
                        }
                        if (previousData == null) {
                            previousData = new byte[rgb8.length];
                        }
                        System.arraycopy(rgb8, 0, previousData, 0, rgb8.length);
                        break;
                    }
                    default: {
                        throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit video.");
                    }
                }
                break;
            }
            case JPG: {
                videoFrameChunk = new DataChunk("00dc");
                ImageWriter iw = (ImageWriter) ImageIO.getImageWritersByMIMEType("image/jpeg").next();
                ImageWriteParam iwParam = iw.getDefaultWriteParam();
                iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwParam.setCompressionQuality(quality);
                MemoryCacheImageOutputStream imgOut = new MemoryCacheImageOutputStream(videoFrameChunk.getOutputStream());
                iw.setOutput(imgOut);
                IIOImage img = new IIOImage(image, null, null);
                iw.write(null, img, iwParam);
                iw.dispose();
                break;
            }
            case PNG:
            default: {
                videoFrameChunk = new DataChunk("00dc");
                ImageWriter iw = (ImageWriter) ImageIO.getImageWritersByMIMEType("image/png").next();
                ImageWriteParam iwParam = iw.getDefaultWriteParam();
                MemoryCacheImageOutputStream imgOut = new MemoryCacheImageOutputStream(videoFrameChunk.getOutputStream());
                iw.setOutput(imgOut);
                IIOImage img = new IIOImage(image, null, null);
                iw.write(null, img, iwParam);
                iw.dispose();
                break;
            }
        }
        long length = getRelativeStreamPosition() - offset;
        moviChunk.add(videoFrameChunk);
        videoFrameChunk.finish();

        videoFrames.add(new Sample(videoFrameChunk.chunkType, frameRate, offset, length - 8, isSync));
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }


    public void writeFrame(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeFrame(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }


    public void writeFrame(InputStream in) throws IOException {
        ensureOpen();
        ensureStarted();

        DataChunk videoFrameChunk = new DataChunk(
                videoFormat == AVIVideoFormat.RAW ? "00db" : "00dc");
        moviChunk.add(videoFrameChunk);
        OutputStream mdatOut = videoFrameChunk.getOutputStream();
        long offset = getRelativeStreamPosition();
        byte[] buf = new byte[512];
        int len;
        while ((len = in.read(buf)) != -1) {
            mdatOut.write(buf, 0, len);
        }
        long length = getRelativeStreamPosition() - offset;
        videoFrameChunk.finish();
        videoFrames.add(new Sample(videoFrameChunk.chunkType, frameRate, offset, length - 8, true));
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
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
            if (imgWidth == -1 || imgHeight == -1) {
                throw new IllegalStateException("image width and height must be specified");
            }

            moviChunk.finish();
            writeEpilog();
            state = States.FINISHED;
            imgWidth = imgHeight = -1;
        }
    }


    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }


    private long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }


    private void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    private void writeProlog() throws IOException {












        aviChunk = new CompositeChunk("RIFF", "AVI ");
        CompositeChunk hdrlChunk = new CompositeChunk("LIST", "hdrl");


        aviChunk.add(hdrlChunk);
        avihChunk = new FixedSizeDataChunk("avih", 56);
        avihChunk.seekToEndOfChunk();
        hdrlChunk.add(avihChunk);

        CompositeChunk strlChunk = new CompositeChunk("LIST", "strl");
        hdrlChunk.add(strlChunk);


        strhChunk = new FixedSizeDataChunk("strh", 56);
        strhChunk.seekToEndOfChunk();
        strlChunk.add(strhChunk);
        strfChunk = new FixedSizeDataChunk("strf", palette == null ? 40 : 40 + palette.getMapSize() * 4);
        strfChunk.seekToEndOfChunk();
        strlChunk.add(strfChunk);

        moviChunk = new CompositeChunk("LIST", "movi");
        aviChunk.add(moviChunk);


    }

    private void writeEpilog() throws IOException {

        int duration = 0;
        for (Sample s : videoFrames) {
            duration += s.duration;
        }
        long bufferSize = 0;
        for (Sample s : videoFrames) {
            if (s.length > bufferSize) {
                bufferSize = s.length;
            }
        }


        DataChunkOutputStream d;


        DataChunk idx1Chunk = new DataChunk("idx1");
        aviChunk.add(idx1Chunk);
        d = idx1Chunk.getOutputStream();
        long moviListOffset = moviChunk.offset + 8;

        for (Sample f : videoFrames) {

            d.writeType(f.chunkType);










            d.writeUInt((f.chunkType.endsWith("pc") ? 0x100 : 0x0)
                    | (f.isSync ? 0x10 : 0x0));










            d.writeUInt(f.offset - moviListOffset);





            d.writeUInt(f.length);

        }
        idx1Chunk.finish();


        avihChunk.seekToStartOfData();
        d = avihChunk.getOutputStream();

        d.writeUInt((1000000L * (long) timeScale) / (long) frameRate);



        d.writeUInt(0);





        d.writeUInt(0);



        d.writeUInt(0x10);






















        d.writeUInt(videoFrames.size());


        d.writeUInt(0);












        d.writeUInt(1);



        d.writeUInt(bufferSize);








        d.writeUInt(imgWidth);


        d.writeUInt(imgHeight);


        d.writeUInt(0);
        d.writeUInt(0);
        d.writeUInt(0);
        d.writeUInt(0);



        strhChunk.seekToStartOfData();
        d = strhChunk.getOutputStream();
        d.writeType("vids");










        switch (videoFormat) {
            case RAW:
                d.writeType("DIB ");
                break;
            case RLE:
                d.writeType("RLE ");
                break;
            case JPG:
                d.writeType("MJPG");
                break;
            case PNG:
            default:
                d.writeType("png ");
                break;
        }





        if (imgDepth <= 8) {
            d.writeUInt(0x00010000);
        } else {
            d.writeUInt(0);
        }














        d.writeUShort(0);




        d.writeUShort(0);


        d.writeUInt(0);







        d.writeUInt(timeScale);






        d.writeUInt(frameRate);


        d.writeUInt(0);





        d.writeUInt(videoFrames.size());



        d.writeUInt(bufferSize);





        d.writeInt(-1);






        d.writeUInt(0);










        d.writeUShort(0);
        d.writeUShort(0);
        d.writeUShort(imgWidth);
        d.writeUShort(imgHeight);










        strfChunk.seekToStartOfData();
        d = strfChunk.getOutputStream();
        d.writeUInt(40);




        d.writeInt(imgWidth);


        d.writeInt(imgHeight);













        d.writeShort(1);



        d.writeShort(imgDepth);





        switch (videoFormat) {
            case RAW:
            default:
                d.writeInt(0);
                break;
            case RLE:
                if (imgDepth == 8) {
                    d.writeInt(1);
                } else if (imgDepth == 4) {
                    d.writeInt(2);
                } else {
                    throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit images");
                }
                break;
            case JPG:
                d.writeType("MJPG");
                break;
            case PNG:
                d.writeType("png ");
                break;
        }



















        switch (videoFormat) {
            case RAW:
                d.writeInt(0);
                break;
            case RLE:
            case JPG:
            case PNG:
            default:
                if (imgDepth == 4) {
                    d.writeInt(imgWidth * imgHeight / 2);
                } else {
                    int bytesPerPixel = Math.max(1, imgDepth / 8);
                    d.writeInt(imgWidth * imgHeight * bytesPerPixel);
                }
                break;
        }



        d.writeInt(0);



        d.writeInt(0);



        d.writeInt(palette == null ? 0 : palette.getMapSize());



        d.writeInt(0);




        if (palette != null) {
            for (int i = 0, n = palette.getMapSize(); i < n; ++i) {

                d.write(palette.getBlue(i));
                d.write(palette.getGreen(i));
                d.write(palette.getRed(i));
                d.write(0);
            }
        }



        aviChunk.finish();
    }
}
