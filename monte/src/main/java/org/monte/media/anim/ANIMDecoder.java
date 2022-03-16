
package org.monte.media.anim;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import org.monte.media.iff.*;
import org.monte.media.ilbm.HAMColorModel;
import org.monte.media.eightsvx.EightSVXDecoder;
import org.monte.media.ilbm.CRNGColorCycle;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.DRNGColorCycle;

import java.io.*;
import java.util.*;
import java.awt.image.*;
import java.net.URL;
import java.applet.AudioClip;


public class ANIMDecoder
        implements IFFVisitor {

    private final static int ILBM_ID = IFFParser.stringToID("ILBM");
    private final static int BMHD_ID = IFFParser.stringToID("BMHD");
    private final static int CMAP_ID = IFFParser.stringToID("CMAP");
    private final static int CAMG_ID = IFFParser.stringToID("CAMG");
    private final static int CCRT_ID = IFFParser.stringToID("CCRT");
    private final static int CRNG_ID = IFFParser.stringToID("CRNG");
    private final static int DRNG_ID = IFFParser.stringToID("DRNG");
    private final static int BODY_ID = IFFParser.stringToID("BODY");
    private final static int ANHD_ID = IFFParser.stringToID("ANHD");
    private final static int DLTA_ID = IFFParser.stringToID("DLTA");
    private final static int ANIM_ID = IFFParser.stringToID("ANIM");
    private final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");
    private final static int AUTH_ID = IFFParser.stringToID("AUTH");
    private final static int ANNO_ID = IFFParser.stringToID("ANNO");
    private final static int ANFI_ID = IFFParser.stringToID("ANFI");
    private final static int SCTL_ID = IFFParser.stringToID("SCTL");

    public final static int MONITOR_ID_MASK = 0xffff1000;

    public final static int DEFAULT_MONITOR_ID = 0x00000000;

    public final static int NTSC_MONITOR_ID = 0x00011000;

    public final static int PAL_MONITOR_ID = 0x00021000;

    public final static int MULTISCAN_MONITOR_ID = 0x00031000;

    public final static int A2024_MONITOR_ID = 0x00041000;

    public final static int PROTO_MONITOR_ID = 0x00051000;

    public final static int EURO72_MONITOR_ID = 0x00061000;

    public final static int EURO36_MONITOR_ID = 0x00071000;

    public final static int SUPER72_MONITOR_ID = 0x00081000;

    public final static int DBLNTSC_MONITOR_ID = 0x00091000;

    public final static int DBLPAL_MONITOR_ID = 0x000a1000;
    protected final static int MODE_MASK = 0x00000880;
    protected final static int HAM_MODE = 0x00000800;
    protected final static int EHB_MODE = 0x00000080;

    private InputStream inputStream_;
    private URL location_;
    private Vector sources_;

    private Hashtable properties_;

    private ColorModel cmapColorModel;

    private ANIMMovieTrack track;

    private int animCount;

    private int index;

    private EightSVXDecoder eightSVXDecoder;

    private byte[] previousCMAPdata_;


    private boolean isInANIM;

    private boolean isInILBM;

    private int camg = NTSC_MONITOR_ID;


    public ANIMDecoder(InputStream inputStream) {
        inputStream_ = inputStream;
    }

    public ANIMDecoder(URL location) {
        location_ = location;
    }


    public void produce(ANIMMovieTrack track, int n, boolean loadAudio)
            throws IOException {
        InputStream in = null;
        this.track = track;
        index = n;
        animCount = 0;
        if (inputStream_ != null) {
            in = inputStream_;
        } else {
            in = location_.openStream();
        }
        try {


            IFFParser iff = new IFFParser();
            registerChunks(iff, loadAudio);
            if (loadAudio) {
                eightSVXDecoder = new EightSVXDecoder() {

                    @Override
                    public void addAudioClip(AudioClip clip) {
                        super.addAudioClip(clip);
                        ANIMDecoder.this.track.addAudioClip(clip);
                    }
                };
                eightSVXDecoder.registerChunks(iff);
            }
            iff.parse(in, this);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        } catch (AbortException e) {
            throw new IOException(e.getMessage());
        } finally {
            in.close();
        }
    }

    public void registerChunks(IFFParser iff, boolean loadAudio) {
        iff.declarePropertyChunk(ILBM_ID, BMHD_ID);
        iff.declarePropertyChunk(ILBM_ID, CMAP_ID);
        iff.declarePropertyChunk(ILBM_ID, CAMG_ID);
        iff.declarePropertyChunk(ILBM_ID, ANHD_ID);
        iff.declareCollectionChunk(ILBM_ID, CCRT_ID);
        iff.declareCollectionChunk(ILBM_ID, CRNG_ID);
        iff.declareCollectionChunk(ILBM_ID, DRNG_ID);
        if (loadAudio) {
            iff.declarePropertyChunk(ILBM_ID, ANFI_ID);
            iff.declareCollectionChunk(ILBM_ID, SCTL_ID);
        }
        iff.declareGroupChunk(ANIM_ID, IFFParser.ID_FORM);
        iff.declareGroupChunk(ILBM_ID, IFFParser.ID_FORM);
        iff.declareDataChunk(ILBM_ID, BODY_ID);
        iff.declareDataChunk(ILBM_ID, DLTA_ID);
        iff.declareCollectionChunk(ILBM_ID, AUTH_ID);
        iff.declareCollectionChunk(ILBM_ID, ANNO_ID);
        iff.declareCollectionChunk(ILBM_ID, COPYRIGHT_ID);
    }

    public void enterGroup(IFFChunk chunk) {

        if (chunk.getType() == ANIM_ID) {
            if (animCount++ == index) {
                isInANIM = true;
            }
        } else if (chunk.getType() == ILBM_ID) {
            isInILBM = true;
        }


        if (isInANIM && eightSVXDecoder != null) {
            eightSVXDecoder.enterGroup(chunk);
        }
    }

    public void leaveGroup(IFFChunk chunk) {

        if (isInANIM && eightSVXDecoder != null) {
            eightSVXDecoder.leaveGroup(chunk);
        }


        if (chunk.getType() == ANIM_ID) {
            isInANIM = false;
        }
        if (chunk.getType() == ILBM_ID) {
            isInILBM = false;
        }
    }

    public void visitChunk(IFFChunk group, IFFChunk chunk)
            throws ParseException, AbortException {
        if (Thread.currentThread().isInterrupted()) {
            throw new AbortException();
        }
        if (isInANIM) {

            if (eightSVXDecoder != null) {
                eightSVXDecoder.visitChunk(group, chunk);
            }


            if (group.getType() == ILBM_ID) {

                if (track.getWidth() == 0) {
                    decodeBMHD(group.getPropertyChunk(BMHD_ID), track);
                    decodeCAMG(group.getPropertyChunk(CAMG_ID), track);
                    decodeColorCycling(
                            group.getCollectionChunks(CCRT_ID),
                            group.getCollectionChunks(CRNG_ID),
                            group.getCollectionChunks(DRNG_ID),
                            track);
                    decodeAUTH(group.getCollectionChunks(AUTH_ID), track);
                    decodeANNO(group.getCollectionChunks(ANNO_ID), track);
                    decodeCOPYRIGHT(group.getCollectionChunks(COPYRIGHT_ID), track);
                }
                boolean is4BitsPerChannel = (camg & MONITOR_ID_MASK) == DEFAULT_MONITOR_ID;
                ColorModel cm = decodeCMAP(group.getPropertyChunk(CMAP_ID), track, is4BitsPerChannel);
                if (cm != null) {
                    cmapColorModel = cm;
                }

                if (chunk.getID() == BODY_ID) {
                    decodeBODY(cmapColorModel, group, chunk, track);
                } else if (chunk.getID() == DLTA_ID) {
                    decodeDLTA(cmapColorModel, group, chunk, track);
                }
            }
        } else if (isInILBM) {




            if (track.getWidth() == 0) {
                decodeBMHD(group.getPropertyChunk(BMHD_ID), track);
                decodeCAMG(group.getPropertyChunk(CAMG_ID), track);
                decodeColorCycling(
                        group.getCollectionChunks(CCRT_ID),
                        group.getCollectionChunks(CRNG_ID),
                        group.getCollectionChunks(DRNG_ID),
                        track);
                decodeAUTH(group.getCollectionChunks(AUTH_ID), track);
                decodeANNO(group.getCollectionChunks(ANNO_ID), track);
                decodeCOPYRIGHT(group.getCollectionChunks(COPYRIGHT_ID), track);
            }
            track.setPlayWrapupFrames(true);
            boolean is4BitsPerChannel = (camg & MONITOR_ID_MASK) == DEFAULT_MONITOR_ID;
            ColorModel cm = decodeCMAP(group.getPropertyChunk(CMAP_ID), track, is4BitsPerChannel);
            if (cm != null) {
                cmapColorModel = cm;
            }

            if (chunk.getID() == BODY_ID) {
                decodeBODY(cmapColorModel, group, chunk, track);
            }
        }
    }


    private void decodeBMHD(IFFChunk chunk, ANIMMovieTrack track)
            throws ParseException {
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
            track.setWidth(in.readUWORD());
            track.setHeight(in.readUWORD());
            track.setXPosition(in.readWORD());
            track.setYPosition(in.readWORD());
            track.setNbPlanes(in.readUBYTE());
            track.setMasking(in.readUBYTE());
            track.setCompression(in.readUBYTE());
            in.skip(1);
            track.setTransparentColor(in.readUWORD());
            track.setXAspect(in.readUBYTE());
            track.setYAspect(in.readUBYTE());
            track.setPageWidth(in.readWORD());
            track.setPageHeight(in.readWORD());
            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }


    private void decodeCAMG(IFFChunk chunk, ANIMMovieTrack track)
            throws ParseException {


        if (chunk != null) {
            try {
                MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

                camg = in.readLONG();

                in.close();
            } catch (IOException e) {
                throw new ParseException(e.toString());
            }
        }


        switch (camg & (MODE_MASK | MODE_MASK)) {
            case EHB_MODE:
                track.setScreenMode(ANIMMovieTrack.MODE_EHB);
                break;
            case HAM_MODE:
                if (track.getNbPlanes() == 6) {
                    track.setScreenMode(ANIMMovieTrack.MODE_HAM6);
                } else if (track.getNbPlanes() == 8) {
                    track.setScreenMode(ANIMMovieTrack.MODE_HAM8);
                } else {
                    throw new ParseException("unsupported Ham Mode with " + track.getNbPlanes() + " bitplanes");

                }
                break;
            default:
                if (track.getNbPlanes() <= 8) {
                    track.setScreenMode(ANIMMovieTrack.MODE_INDEXED_COLORS);
                } else {
                    track.setScreenMode(ANIMMovieTrack.MODE_DIRECT_COLORS);
                }
        }


        int camgJiffies;
        switch (camg & MONITOR_ID_MASK) {
            case DEFAULT_MONITOR_ID:
                camgJiffies = 60;
                break;
            case NTSC_MONITOR_ID:
                camgJiffies = 60;
                break;
            case PAL_MONITOR_ID:
                camgJiffies = 50;
                break;
            case MULTISCAN_MONITOR_ID:
                camgJiffies = 58;
                break;
            case A2024_MONITOR_ID:
                camgJiffies = 60;
                break;
            case PROTO_MONITOR_ID:
                camgJiffies = 60;
                break;
            case EURO72_MONITOR_ID:
                camgJiffies = 69;
                break;
            case EURO36_MONITOR_ID:
                camgJiffies = 73;
                break;
            case DBLNTSC_MONITOR_ID:
                camgJiffies = 58;
                break;
            case DBLPAL_MONITOR_ID:
                camgJiffies = 48;
                break;
            case SUPER72_MONITOR_ID:
                camgJiffies = 71;
                break;
            default:
                camgJiffies = 60;
                break;
        }
        track.setJiffies(camgJiffies);
    }


    private ColorModel decodeCMAP(IFFChunk chunk, ANIMMovieTrack track, boolean is4BitsPerChannel)
            throws ParseException {
        byte[] red;
        byte[] green;
        byte[] blue;
        int size = 0;
        int colorsToRead = 0;

        if (chunk == null) {
            return null;
        }

        byte[] cmapData = chunk.getData();
        if (previousCMAPdata_ != null && Arrays.equals(cmapData, previousCMAPdata_)) {
            return null;
        } else {
            previousCMAPdata_ = cmapData;
        }

        switch (track.getScreenMode()) {
            case ANIMMovieTrack.MODE_EHB:
                size = 64;
                colorsToRead = Math.min(32, (int) chunk.getSize() / 3);
                break;
            case ANIMMovieTrack.MODE_HAM6:
            case ANIMMovieTrack.MODE_HAM8:
                size = 1 << (track.getNbPlanes() - 2);
                colorsToRead = Math.min(size, (int) chunk.getSize() / 3);
                break;
            case ANIMMovieTrack.MODE_INDEXED_COLORS:
                size = 1 << (track.getNbPlanes());
                colorsToRead = Math.min(size, (int) chunk.getSize() / 3);
                break;
            case ANIMMovieTrack.MODE_DIRECT_COLORS:
                return new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
        }

        red = new byte[size];
        green = new byte[size];
        blue = new byte[size];

        byte[] data = chunk.getData();
        int j = 0;

        if (is4BitsPerChannel) {
            for (int i = 0; i < colorsToRead; i++) {
                red[i] = (byte) (data[j] & 0xf0 | ((data[j] & 0xf0) >>> 4));
                green[i] = (byte) (data[j + 1] & 0xf0 | ((data[j + 1] & 0xf0) >>> 4));
                blue[i] = (byte) (data[j + 2] & 0xf0 | ((data[j + 2] & 0xf0) >>> 4));
                j += 3;
            }
        } else {

            for (int i = 0; i < colorsToRead; i++) {
                red[i] = data[j++];
                green[i] = data[j++];
                blue[i] = data[j++];
            }
        }

        switch (track.getScreenMode()) {

            case ANIMMovieTrack.MODE_EHB:
                j = 32;
                for (int i = 0; i < 32; i++, j++) {
                    red[j] = (byte) ((red[i] & 255) / 2);
                    green[j] = (byte) ((green[i] & 255) / 2);
                    blue[j] = (byte) ((blue[i] & 255) / 2);
                }


                return new IndexColorModel(8, 64, red, green, blue, -1);


            case ANIMMovieTrack.MODE_HAM6:
                return new HAMColorModel(HAMColorModel.HAM6, 16, red, green, blue, false);

            case ANIMMovieTrack.MODE_HAM8:
                return new HAMColorModel(HAMColorModel.HAM8, 64, red, green, blue, false);

            case ANIMMovieTrack.MODE_INDEXED_COLORS:



                return new IndexColorModel(8, Math.min(red.length, (int) chunk.getSize() / 3), red, green, blue, -1);


            default: {
                throw new ParseException("ScreenMode not supported:" + track.getScreenMode());
            }
        }
    }


    protected void decodeCCRT(IFFChunk chunk, ANIMMovieTrack track)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int direction = in.readWORD();
            int start = in.readUBYTE();
            int end = in.readUBYTE();
            long seconds = in.readULONG();
            long microseconds = in.readULONG();
            int pad = in.readWORD();
            cc = new CRNGColorCycle(1000000 / (int) (seconds * 1000 + microseconds / 1000), 1000, start, end,
                    direction == 1 || direction == -1,
                    direction == 1, track.getScreenMode() == ANIMMovieTrack.MODE_EHB);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        if (cc.isActive()) {
            track.addColorCycle(cc);
        }
    }


    protected void decodeCRNG(IFFChunk chunk, ANIMMovieTrack track)
            throws ParseException {
        try {
            ColorCycle cc;
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int pad1 = in.readUWORD();
            int rate = in.readUWORD();
            int flags = in.readUWORD();
            int low = in.readUBYTE();
            int high = in.readUBYTE();

            cc = new CRNGColorCycle(rate, 273,
                    low, high,
                    (flags & 1) != 0 && rate > 36 && high > low,
                    (flags & 2) != 0,
                    track.getScreenMode() == ANIMMovieTrack.MODE_EHB);

            if (cc.isActive()) {
                track.addColorCycle(cc);
            }

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }


    protected void decodeDRNG(IFFChunk chunk, ANIMMovieTrack track)
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
                    track.getScreenMode() == ANIMMovieTrack.MODE_EHB, cells);
            if (cc.isActive()) {
                track.addColorCycle(cc);
            }

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }


    protected void decodeColorCycling(IFFChunk[] ccrtChunks, IFFChunk[] crngChunks, IFFChunk[] drngChunks, ANIMMovieTrack track) throws ParseException {
        int activeCycles = 0;
        int j = 0, k = 0, l = 0;
        for (int i = 0, n = ccrtChunks.length + crngChunks.length + drngChunks.length; i < n; i++) {
            if (j < crngChunks.length
                    && (k >= drngChunks.length || crngChunks[j].getScan() < drngChunks[k].getScan())
                    && (l >= ccrtChunks.length || crngChunks[j].getScan() < ccrtChunks[l].getScan())) {
                decodeCRNG(crngChunks[j], track);
                j++;
            } else if (k < drngChunks.length
                    && (l >= ccrtChunks.length || drngChunks[k].getScan() < ccrtChunks[l].getScan())) {
                decodeDRNG(drngChunks[k], track);
                k++;
            } else {
                decodeCCRT(ccrtChunks[l], track);
                l++;
            }
        }
        track.setProperty("colorCycling", track.getColorCyclesCount());
    }

    private void decodeBODY(ColorModel colorModel, IFFChunk group, IFFChunk body, ANIMMovieTrack track)
            throws ParseException {
        ANIMKeyFrame frame = new ANIMKeyFrame();
        frame.setColorModel(colorModel);

        decodeANHD(group.getPropertyChunk(ANHD_ID), frame);
        if (group.getPropertyChunk(ANFI_ID) != null) {
            decodeANFI(group.getPropertyChunk(ANFI_ID), frame, track);
        }
        IFFChunk[] sctlChunks = group.getCollectionChunks(SCTL_ID);
        for (int i = 0; i < sctlChunks.length; i++) {
            decodeSCTL(sctlChunks[i], frame, track);
        }
        frame.cleanUpAudioCommands();
        frame.setData(body.getData());
        frame.setCompression(track.getCompression());


        track.addFrame(frame);
    }

    private void decodeDLTA(ColorModel colorModel, IFFChunk group, IFFChunk dlta, ANIMMovieTrack track)
            throws ParseException {
        ANIMDeltaFrame frame = new ANIMDeltaFrame();
        frame.setColorModel(colorModel);

        decodeANHD(group.getPropertyChunk(ANHD_ID), frame);
        if (group.getPropertyChunk(ANFI_ID) != null) {
            decodeANFI(group.getPropertyChunk(ANFI_ID), frame, track);
        }
        IFFChunk[] sctlChunks = group.getCollectionChunks(SCTL_ID);
        for (int i = 0; i < sctlChunks.length; i++) {
            decodeSCTL(sctlChunks[i], frame, track);
        }

        frame.cleanUpAudioCommands();
        frame.setData(dlta.getData());

        track.addFrame(frame);
    }

    private void decodeANHD(IFFChunk chunk, ANIMFrame frame)
            throws ParseException {
        if (chunk != null) {
            try {
                MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
                frame.setOperation(in.readUBYTE());
                frame.setMask(in.readUBYTE());
                frame.setWidth(in.readUWORD());
                frame.setHeight(in.readUWORD());
                frame.setY(in.readUWORD());
                frame.setX(in.readUWORD());
                frame.setAbsTime(in.readULONG());
                frame.setRelTime(in.readULONG());
                frame.setInterleave(in.readUBYTE());
                in.skip(1);
                frame.setBits((int) in.readULONG());
                //    in.skip(16);
                in.close();
            } catch (IOException e) {
                throw new ParseException(e.toString());
            }
        }
    }

    private void decodeANFI(IFFChunk chunk, ANIMFrame frame, ANIMMovieTrack track)
            throws ParseException {
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            for (int i = 0; i < 4; i++) {
                int command = in.readUWORD();
                int frequency = in.readUWORD();
                int sound = in.readUBYTE();
                int channel = in.readUBYTE();
                int repeats = in.readUBYTE();
                if (repeats > 2) {
                    repeats -= 1;
                } else {
                    repeats = 1;
                }
                int volume = in.readUBYTE();
                //if (command == 0x28 || command == 0x81f) {
                if (command != 0) {
                    ANIMAudioCommand audioCommand = new ANIMAudioCommand(ANIMAudioCommand.COMMAND_PLAY_SOUND, volume - 1, sound, repeats, 1 << channel, 0 /*frequency*/, 0);
                    frame.addAudioCommand(audioCommand);
                    audioCommand.prepare(track);
                }
            }
            //int pad = in.readULONG();
            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }

        /*
        if (chunk != null) {
        try {
        MC68000InputStream in = new MC68000InputStream( new ByteArrayInputStream( chunk.getData() ) ) ;
        for (int i = 0; i < 4; i ++) {
        //in.skip(4); // skip pad bytes
        long pad1a = in.readWORD();
        long pad1b = in.readWORD();
        int audioclip = in.readUBYTE();
        //in.skip(3); // skip pad bytes
        int repeatCount = in.readUBYTE();
        int pad3 = in.readWORD();
        if (audioclip != 0) {
        frame.setAudioClip(audioclip - 1, i, repeatCount);
        //            System.out.println("channel:"+i+" clip:"+audioclip+ " pad1a:"+pad1a+" pad1b:"+pad1b +" repeat:"+repeatCount+" pad3:"+pad3);
        }
        }
        }
        catch (IOException e) {
        throw new ParseException(e.toString());
        }
        }*/
    }

    private void decodeSCTL(IFFChunk chunk, ANIMFrame frame, ANIMMovieTrack track)
            throws ParseException {
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
            int command = in.readUBYTE();
            int volume = in.readUBYTE();
            int sound = in.readUWORD();
            int repeats = in.readUWORD();
            int channel = in.readUWORD();
            int frequency = in.readUWORD();
            int flags = in.readUWORD();
            //int pad = in.readULONG();
            in.close();
            ANIMAudioCommand audioCommand = new ANIMAudioCommand(command, volume, sound, repeats, channel, frequency, flags);
            frame.addAudioCommand(audioCommand);
            audioCommand.prepare(track);
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }

    protected void decodeCOPYRIGHT(IFFChunk[] chunks, ANIMMovieTrack track)
            throws ParseException {
        for (int i = 0; i < chunks.length; i++) {
            String copyright = new String(chunks[i].getData());
            appendProperty("copyright", copyright);
            appendProperty("comment", "� " + copyright);
        }
    }

    protected void decodeAUTH(IFFChunk[] chunks, ANIMMovieTrack track)
            throws ParseException {
        for (int i = 0; i < chunks.length; i++) {
            String author = new String(chunks[i].getData());
            appendProperty("author", author);
            appendProperty("comment", "Author " + author);
        }
    }

    protected void decodeANNO(IFFChunk[] chunks, ANIMMovieTrack track)
            throws ParseException {
        for (int i = 0; i < chunks.length; i++) {
            String anno = new String(chunks[i].getData());
            appendProperty("annotation", anno);
            appendProperty("comment", anno);
        }
    }

    private void appendProperty(String name, String value) {
        String oldValue = (String) track.getProperty(name);
        if (oldValue == null) {
            track.setProperty(name, value);
        } else {
            track.setProperty(name, oldValue + "\n" + value);
        }
    }
    /*
    /* Normal identifiers. * /
    public final static int DEFAULT_MONITOR_ID = 0x00000000;
    public final static int NTSC_MONITOR_ID = 0x00011000;
    public final static int PAL_MONITOR_ID = 0x00021000;
    public final static int LORES_MASK  = 0x00000000;
    public final static int LACE_MASK  = 0x00000004;
    public final static int HIRES_MASK  = 0x00008000;
    public final static int SUPER_MASK  = 0x00008020;
    public final static int MODE_MASK    = 0x00000800;
    public final static int DPF_MASK    = 0x00000400;
    public final static int DPF2_MASK  = 0x00000440;
    public final static int MODE_MASK    = 0x00000080;

    /*
    The following 20 composite keys are for Modes on the default Monitor.
    NTSC & PAL "flavours" of these particular keys may be made by or'ing
    the NTSC or PAL MONITOR_ID with the desired MODE_KEY.
     * /
    public final static int LORES_KEY      = 0x00000000; // NTSC:320*200,44x52  PAL:320x256,44x44
    public final static int HIRES_KEY      = 0x00008000; // NTSC:640*200,22x52  PAL:640*256,22x44
    public final static int SUPER_KEY      = 0x00008020; // NTSC:1280*200,11x52  PAL:1280x256,11x44
    public final static int HAM_MODE        = 0x00000800; // NTSC:320*200,44x52  PAL:320x256,44x44
    public final static int LORESLACE_KEY    = 0x00000004; // NTSC:320*400,44x26 PAL:320x512,44x22
    public final static int HIRESLACE_KEY    = 0x00008004; // NTSC:640*400,22x26  PAL:640x512,22x22
    public final static int SUPERLACE_KEY    = 0x00008024; // NTSC:1280*400,11x26  PAL:1280x512,11x22
    public final static int HAMLACE_KEY      = 0x00000804; // NTSC:320*400,44x26  PAL:320x512,44x22
    public final static int LORESDPF_KEY      = 0x00000400; // 320*240,256
    public final static int HIRESDPF_KEY      = 0x00008400; // 640*240,256
    public final static int SUPERDPF_KEY      = 0x00008420; // 1280*240,256
    public final static int LORESLACEDPF_KEY    = 0x00000404; // 320*480,512
    public final static int HIRESLACEDPF_KEY    = 0x00008404; // 640*480,512
    public final static int SUPERLACEDPF_KEY    = 0x00008424; // 1280*480,512
    public final static int LORESDPF2_KEY    = 0x00000440; // 320*240,256
    public final static int HIRESDPF2_KEY    = 0x00008440; // 640*240,256
    public final static int SUPERDPF2_KEY    = 0x00008460; // 1280*240,256
    public final static int LORESLACEDPF2_KEY  = 0x00000444; // 320*480,512
    public final static int HIRESLACEDPF2_KEY  = 0x00008444; // 640*480,512
    public final static int SUPERLACEDPF2_KEY  = 0x00008464; // 1280*480,512
    public final static int EHB_MODE  = 0x00000080; // NTSC:320*200,44x52  PAL:320*256,44x44
    public final static int EXTRAHALFBRITELACE_KEY  = 0x00000084; // NTSC:320*400,44x26  PAL:320*512,44x22

    /* VGA identifiers. * /
    public final static int MULTISCAN_MONITOR_ID  = 0x00031000;
    public final static int VGALACE_MASK    = 0x00000001;
    public final static int VGALORES_MASK  = 0x00008000;

    public final static int VGAEXTRALORES_KEY    = 0x00031004; // 160*480 v 88x22
    public final static int VGALORES_KEY        = 0x00039004; // 320*480 v 44x22
    public final static int VGAPRODUCT_KEY      = 0x00039024; // 640*480 v 22x22
    public final static int VGAHAM_KEY        = 0x00031804; //
    public final static int VGAEXTRALORESLACE_KEY  = 0x00031005; // 160*960 v 88x11
    public final static int VGALORESLACE_KEY      = 0x00039005; // 320*960 v 44x11
    public final static int VGAPRODUCTLACE_KEY    = 0x00039025; // 640*960 v 22x11
    public final static int VGAHAMLACE_KEY      = 0x00031805; //
    public final static int VGAEXTRALORESDPF_KEY    = 0x00031404; //
    public final static int VGALORESDPF_KEY      = 0x00039404;
    public final static int VGAPRODUCTDPF_KEY    = 0x00039424;
    public final static int VGAEXTRALORESLACEDPF_KEY  = 0x00031405;
    public final static int VGALORESLACEDPF_KEY    = 0x00039405;
    public final static int VGAPRODUCTLACEDPF_KEY  = 0x00039425; //
    public final static int VGAEXTRALORESDPF2_KEY  = 0x00031444;
    public final static int VGALORESDPF2_KEY      = 0x00039444;
    public final static int VGAPRODUCTDPF2_KEY    = 0x00039464;
    public final static int VGAEXTRALORESLACEDPF2_KEY  = 0x00031445;
    public final static int VGALORESLACEDPF2_KEY    = 0x00039445;
    public final static int VGAPRODUCTLACEDPF2_KEY  = 0x00039465; // 640*960
    public final static int VGAEXTRAHALFBRITE_KEY  = 0x00031084;
    public final static int VGAEXTRAHALFBRITELACE_KEY = 0x00031085;

    /* A2024 identifiers. * /
    public final static int A2024_MONITOR_ID = 0x00041000;

    public final static int A2024TENHERTZ_KEY    = 0x00041000;
    public final static int A2024FIFTEENHERTZ_KEY  = 0x00049000;

    /* Proto identifiers. * /
    public final static int PROTO_MONITOR_ID = 0x00051000;

    /* Euro identifiers * /
    public final static int EURO36_MONITOR_ID = 0x00071000;

    public final static int EURO36EXTRAHALFBRITE_KEY  = 0x00071080; // 320*200 v 44x44
    public final static int EURO36EXTRAHALFBRITELACE_KEY = 0x00071084; // 320*400 v 44x22
    public final static int EURO36HAM_KEY      = 0x00071800; // 320*200 v 44x44
    public final static int EURO36HAMLACE_KEY    = 0x00071804; // 320*400 v 44x22
    public final static int EURO36HIRES_KEY      = 0x00079000; // 640*200 v 22x44
    public final static int EURO36HIRESLACE_KEY    = 0x00079004; // 640*400 v 22x22
    public final static int EURO36LORES_KEY      = 0x00071000; // 320*200 v 44x44
    public final static int EURO36LORESLACE_KEY    = 0x00071004; // 320*400 v 44x22
    public final static int EURO36SUPERHIRES_KEY    = 0x00079020; // 1280*200 v 11x44
    public final static int EURO36SUPERHIRESLACE_KEY  = 0x00079024; // 1280*400 v 11x44
    public final static int EURO72ECS_KEY      = 0x00069004; // 320*400 v 44x22
    public final static int EURO72ECSLACE_KEY    = 0x00069005; // 320*800 v 44x11
    public final static int EURO72PRODUCT_KEY    = 0x00069024; // 640*400 v 22x22
    public final static int EURO72PRODUCTLACE_KEY  = 0x00069025; // 640*800 v 22x11


    public final static int EURO_KEY = 0x00071000;
     */
}
