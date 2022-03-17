
package org.monte.media.pbm;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import org.monte.media.iff.*;
import org.monte.media.ilbm.CRNGColorCycle;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.DRNGColorCycle;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;


import java.io.*;
import java.util.*;
import java.awt.image.*;
import java.net.URL;


public class PBMDecoder implements IFFVisitor {



    protected final static int PBM_ID = IFFParser.stringToID("PBM ");
    protected final static int BMHD_ID = IFFParser.stringToID("BMHD");
    protected final static int CMAP_ID = IFFParser.stringToID("CMAP");
    protected final static int CRNG_ID = IFFParser.stringToID("CRNG");
    protected final static int DRNG_ID = IFFParser.stringToID("DRNG");
    protected final static int BODY_ID = IFFParser.stringToID("BODY");
    private final static int AUTH_ID = IFFParser.stringToID("AUTH");
    private final static int ANNO_ID = IFFParser.stringToID("ANNO");
    private final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");

    protected final static int MSK_NONE = 0,
            MSK_HAS_MASK = 1,
            MSK_HAS_TRANSPARENT_COLOR = 2,
            MSK_LASSO = 3;

    protected final static int CMP_NONE = 0,
            CMP_BYTE_RUN_1 = 1;


    protected InputStream inputStream;

    protected URL location;

    protected ArrayList<ColorCyclingMemoryImageSource> sources;

    protected Hashtable properties;


    protected int bmhdWidth, bmhdHeight;

    protected int bmhdXPosition, bmhdYPosition;

    protected int bmhdNbPlanes;
    protected int bmhdMasking;
    protected int bmhdCompression;

    protected int bmhdTransparentColor;

    protected int bmhdXAspect, bmhdYAspect;

    protected int bmhdPageWidth, bmhdPageHeight;

    protected ColorModel cmapColorModel;

    protected ColorCyclingMemoryImageSource memoryImageSource;


    public PBMDecoder(InputStream in) {
        inputStream = in;
    }

    public PBMDecoder(URL location) {
        this.location = location;
    }


    public ArrayList<ColorCyclingMemoryImageSource> produce()
            throws IOException {
        InputStream in = null;
        sources = new ArrayList<ColorCyclingMemoryImageSource>();
        boolean mustCloseStream;
        if (inputStream != null) {
            in = inputStream;
            mustCloseStream = false;
        } else {
            in = location.openStream();
            mustCloseStream = true;
        }
        try {

            IFFParser iff = new IFFParser();
            registerChunks(iff);
            iff.parse(in, this);
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (AbortException e) {
            e.printStackTrace();
        } finally {
             if (mustCloseStream) {
                in.close();
            }
        }
        return sources;
    }

    public void registerChunks(IFFParser iff) {
        iff.declareGroupChunk(PBM_ID, IFFParser.ID_FORM);
        iff.declarePropertyChunk(PBM_ID, BMHD_ID);
        iff.declarePropertyChunk(PBM_ID, CMAP_ID);
        iff.declareDataChunk(PBM_ID, BODY_ID);
        iff.declareCollectionChunk(PBM_ID, ANNO_ID);
        iff.declareCollectionChunk(PBM_ID, COPYRIGHT_ID);
        iff.declareCollectionChunk(PBM_ID, AUTH_ID);
        iff.declareCollectionChunk(PBM_ID, CRNG_ID);
        iff.declareCollectionChunk(PBM_ID, DRNG_ID);
    }

    @Override
    public void enterGroup(IFFChunk chunk) {
    }

    @Override
    public void leaveGroup(IFFChunk chunk) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitChunk(IFFChunk group, IFFChunk chunk)
            throws ParseException, AbortException {
        decodeBMHD(group.getPropertyChunk(BMHD_ID));
        decodeCMAP(group.getPropertyChunk(CMAP_ID));
        decodeBODY(chunk);

        double aspect = (double) bmhdXAspect / (double) bmhdYAspect;
        if (bmhdXAspect == 0 || bmhdYAspect == 0) {
            aspect = 1d;
        }
        Hashtable props = memoryImageSource.getProperties();

        props.put("aspect", new Double(aspect));
        String s = "Indexed Colors";
        props.put("screenMode", s);
        props.put("nbPlanes", "" + bmhdNbPlanes + (((bmhdMasking & MSK_HAS_MASK) != 0) ? "+mask" : ""));

        StringBuffer comment = new StringBuffer();
        IFFChunk[] chunks = group.getCollectionChunks(ANNO_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append(new String(chunks[i].getData()));
        }
        chunks = group.getCollectionChunks(AUTH_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append("Author: ");
            comment.append(new String(chunks[i].getData()));
        }
        chunks = group.getCollectionChunks(COPYRIGHT_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append("© ");
            comment.append(new String(chunks[i].getData()));
        }
        if (comment.length() > 0) {
            props.put("comment", comment.toString());
        }


        IFFChunk[] crngChunks = group.getCollectionChunks(CRNG_ID);
        IFFChunk[] drngChunks = group.getCollectionChunks(DRNG_ID);
        int activeCycles = 0;
        int j = 0, k = 0;
        for (int i = 0, n = crngChunks.length + drngChunks.length; i < n; i++) {
            if (j < crngChunks.length && (k >= drngChunks.length || crngChunks[j].getScan() < drngChunks[k].getScan())) {

                ColorCycle cc = decodeCRNG(crngChunks[j]);
                memoryImageSource.addColorCycle(cc);
                if (cc.isActive()) {
                    activeCycles++;
                }
                j++;
            } else {

                ColorCycle cc = decodeDRNG(drngChunks[k]);
                memoryImageSource.addColorCycle(cc);
                if (cc.isActive()) {
                    activeCycles++;
                }
                k++;
            }
        }
        if (activeCycles > 0) {
            memoryImageSource.setAnimated(true);
            props.put("colorCycling", activeCycles);
        }

        sources.add(memoryImageSource);
    }


    protected void decodeBMHD(IFFChunk chunk)
            throws ParseException {
        if (chunk == null) {
            throw new ParseException("no BMHD -> no Picture");
        }
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
            bmhdWidth = in.readUWORD();
            bmhdHeight = in.readUWORD();
            bmhdXPosition = in.readWORD();
            bmhdYPosition = in.readWORD();
            bmhdNbPlanes = in.readUBYTE();
            bmhdMasking = in.readUBYTE();
            bmhdCompression = in.readUBYTE();
            in.skip(1);
            bmhdTransparentColor = in.readUWORD();
            bmhdXAspect = in.readUBYTE();
            bmhdYAspect = in.readUBYTE();
            bmhdPageWidth = in.readWORD();
            bmhdPageHeight = in.readWORD();
            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }

    protected void decodeCMAP(IFFChunk chunk)
            throws ParseException {
        byte[] red;
        byte[] green;
        byte[] blue;
        byte[] alpha;
        int size = 0;
        int colorsToRead = 0;

        size = ((bmhdMasking & MSK_HAS_MASK) != 0) ? 2 << bmhdNbPlanes : 1 << bmhdNbPlanes;
        colorsToRead = Math.min(size, (int) chunk.getSize() / 3);


        red = new byte[size];
        green = new byte[size];
        blue = new byte[size];

        byte[] data = chunk.getData();
        int j = 0;
        for (int i = 0; i < colorsToRead; i++) {
            red[i] = data[j++];
            green[i] = data[j++];
            blue[i] = data[j++];
        }

        int transparentColorIndex = ((bmhdMasking & MSK_HAS_TRANSPARENT_COLOR) != 0) ? bmhdTransparentColor : -1;

        if ((bmhdMasking & MSK_HAS_MASK) != 0) {
            System.arraycopy(red, 0, red, red.length / 2, red.length / 2);
            System.arraycopy(green, 0, green, green.length / 2, green.length / 2);
            System.arraycopy(blue, 0, blue, blue.length / 2, blue.length / 2);
            alpha = new byte[red.length];
            for (int i = 0, n = red.length / 2; i < n; i++) {
                alpha[i] = (byte) 0xff;
            }
            cmapColorModel = new IndexColorModel(8, red.length, red, green, blue, alpha);
        } else {
            cmapColorModel = new IndexColorModel(8, red.length, red, green, blue, transparentColorIndex);
        }
    }


    protected ColorCycle decodeCRNG(IFFChunk chunk)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int pad1 = in.readUWORD();
            int rate = in.readUWORD();
            int flags = in.readUWORD();
            int low = in.readUBYTE();
            int high = in.readUBYTE();

            cc = new CRNGColorCycle(rate, 273, low, high,
                    (flags & 1) != 0 && rate > 36 && high > low,
                    (flags & 2) != 0, false);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }


    protected ColorCycle decodeDRNG(IFFChunk chunk)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int min = in.readUBYTE();
            int max = in.readUBYTE();
            int rate = in.readUWORD();
            int flags = in.readUWORD();
            int ntrue = in.readUBYTE();
            int nregs = in.readUBYTE();
            DRNGColorCycle.Cell[] cells = new DRNGColorCycle.Cell[ntrue + nregs];

            for (int i = 0; i < ntrue; i++) {
                int cell = in.readUBYTE();
                int rgb = (in.readUBYTE() << 16) | (in.readUBYTE() << 8) | in.readUBYTE();
                cells[i] = new DRNGColorCycle.DColorCell(cell, rgb);
            }
            for (int i = 0; i < nregs; i++) {
                int cell = in.readUBYTE();
                int index = in.readUBYTE();
                cells[i + ntrue] = new DRNGColorCycle.DIndexCell(cell, index);
            }


            cc = new DRNGColorCycle(rate, 273, min, max,
                    (flags & 1) != 0 && rate > 36 && min <= max && ntrue + nregs > 1,
                    false, cells);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }

    protected void decodeBODY(IFFChunk chunk)
            throws ParseException {
        int pixmapWidth = (bmhdWidth % 2 == 1) ? bmhdWidth + 1 : bmhdWidth;
        byte[] pixels = new byte[pixmapWidth * bmhdHeight];

        byte[] data = chunk.getData();

        switch (bmhdCompression) {
            case CMP_NONE:
                System.arraycopy(data, 0, pixels, 0, data.length);
                break;
            case CMP_BYTE_RUN_1:
                unpackByteRun1(data, pixels);
                break;
            default:
                throw new ParseException("unknown compression method: " + bmhdCompression);
        }

        Hashtable props = new Hashtable();
        if ((bmhdMasking & MSK_HAS_MASK) != 0) {

            System.out.println("PBMDecoder Images with Mask not supported");
            memoryImageSource = new ColorCyclingMemoryImageSource(bmhdWidth, bmhdHeight, cmapColorModel, pixels, 0, pixmapWidth, props);
        } else {
            memoryImageSource = new ColorCyclingMemoryImageSource(bmhdWidth, bmhdHeight, cmapColorModel, pixels, 0, pixmapWidth, props);
        }
    }


    public static int unpackByteRun1(byte[] in, byte[] out)
            throws ParseException {
        try {
            return MC68000InputStream.unpackByteRun1(in, out);
        } catch (IOException ex) {
            ParseException e = new ParseException("couldn't decompress body");
            e.initCause(ex);
            throw e;
        }
    }
}
