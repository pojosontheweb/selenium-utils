
package org.monte.media.seq;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;


public class SEQDecoder {

    private ImageInputStream in;
    
    private int nFrames;
    
    private int speed;
    
    private long[] offsets;

    
    private int resolution = -1;

    
    private int nColors = -1;

    
    private SEQMovieTrack track;


    private boolean enforce8BitColorModel = true;

    
    public SEQDecoder(InputStream in) {
        this.in = new MemoryCacheImageInputStream(in);
        this.in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    
    public SEQDecoder(ImageInputStream in) {
        this.in = in;
        this.in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    
    public void produce(SEQMovieTrack track, boolean loadAudio) throws IOException {
        this.track = track;
        readHeader();
        readOffsets();
        readFrames();
    }

    public void setEnforce8BitColorModel(boolean b) {
        enforce8BitColorModel=b;
    }


    
    private void readHeader() throws IOException {
        int magic = in.readUnsignedShort();
        if (magic != 0xfedb && magic != 0xfedc) {
            throw new IOException("SEQ Header: Invalid magic number 0x" + Integer.toHexString(magic) + ", expected 0xfedb or 0xfedc.");
        }
        int version = in.readUnsignedShort();
        if (version != 0) {
            throw new IOException("SEQ Header: Invalid version " + version + ", expected 0.");
        }
        long numberOfFrames = in.readUnsignedInt();
        if (numberOfFrames > Integer.MAX_VALUE) {
            throw new IOException("SEQ Header: Too many frames " + numberOfFrames + ", expected 0.");
        }
        nFrames = (int) numberOfFrames;
        speed = in.readUnsignedShort();
       
        track.setJiffies(6000);


        int skipped = in.skipBytes(118);
        if (skipped != 118) {
            throw new IOException("SEQ Header: Unexpected EOF.");
        }
    }

    
    private void readOffsets() throws IOException {
        offsets = new long[nFrames];
        for (int i = 0; i < nFrames; i++) {
            offsets[i] = in.readUnsignedInt();
        }
    }

    
    private void readFrames() throws IOException {
        for (int i = 0; i < nFrames; i++) {
            readFrame(i);
        }
    }

    
    private void readFrame(int i) throws IOException {


        int type = in.readUnsignedShort();
        if (type != 0xffff) {
            throw new IOException("Frame Header "+i+": Invalid type "+type+", expected 0xffff.");
        }
        int res = in.readUnsignedShort();
        if (res > 2) {
            throw new IOException("Frame Header "+i+": Illegal resolution "+res+", expected range [0,2].");
        }
        if (resolution == -1) {
            resolution = res;
            switch (res) {
                case  0:
                    track.setWidth(320);
                    track.setHeight(200);
                    track.setNbPlanes(4);
                    nColors = 16;
                    break;
                case 1:
                    track.setWidth(640);
                    track.setHeight(200);
                    track.setNbPlanes(2);
                    nColors = 4;
                    break;
                case 2:
                    track.setWidth(640);
                    track.setHeight(400);
                    track.setNbPlanes(1);
                    nColors = 2;
                    break;
            }
        }
        if (res != resolution) {
            throw new IOException("Frame Header "+i+": Illegal resolution change "+res+", expected "+resolution+".");
        }



        byte[] r = new byte[nColors];
        byte[] g = new byte[nColors];
        byte[] b = new byte[nColors];
        for (int j=0; j<nColors;j++) {
            int clr = in.readUnsignedShort();
                    int red = (clr&0x700)>>8;
                    int green = (clr&0x70)>>4;
                    int blue = (clr&0x7);
                    r[j] = (byte) ((red<<5)|(red<<2)|(red>>>1));
                    g[j] = (byte) ((green<<5)|(green<<2)|(green>>>1));
                    b[j] = (byte) ((blue<<5)|(blue<<2)|(blue>>>1));
        }
       ColorModel cm= new IndexColorModel(enforce8BitColorModel?8:4, nColors, r, g, b);



        if (in.skipBytes(12) != 12) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in filename.");
        }




        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in color animation flag.");
        }

        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in color animation range.");
        }

        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in color animation activation flag.");
        }

        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in color animation speeddir.");
        }

        if (in.skipBytes(2) != 2) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in color animation steps.");
        }



        int xOffset = in.readUnsignedShort();
        int yOffset = in.readUnsignedShort();
        int width = in.readUnsignedShort();
        int height = in.readUnsignedShort();



        int operation = in.readUnsignedByte();
        if (operation > 1) {
            throw new IOException("Frame Header "+i+": Unexpected operation "+operation+", expected range [0,1|.");
        }
        int storageMethod = in.readUnsignedByte();
        if (storageMethod > 1) {
            throw new IOException("Frame Header "+i+": Unexpected storage method "+storageMethod+", expected range [0,1|.");
        }
        long nData = in.readUnsignedInt();
        if (nData > Integer.MAX_VALUE) {
            throw new IOException("Frame Header "+i+": Too much data "+nData+", expected range [0,"+Integer.MAX_VALUE+"|.");
        }


        if (in.skipBytes(60) != 60) {
            throw new IOException("Frame Header "+i+": Unexpected EOF in reserved fields.");
        }
        

        byte[] data = new byte[(int) nData];
        in.readFully(data);

        SEQDeltaFrame f = new SEQDeltaFrame();
        f.setBounds(xOffset, yOffset, width, height);
        f.setOperation(operation);
        f.setStorageMethod(storageMethod);
        f.setData(data);
        f.setColorModel(cm);
        f.setRelTime(speed);
        f.setInterleave(1);
        track.addFrame(f);
    }
}
