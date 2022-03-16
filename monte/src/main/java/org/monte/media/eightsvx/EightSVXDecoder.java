
package org.monte.media.eightsvx;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import org.monte.media.iff.*;
import java.util.Vector;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.applet.AudioClip;

public class EightSVXDecoder
implements IFFVisitor {
    /* Constants */
    public final static int EIGHT_SVX_ID = IFFParser.stringToID("8SVX");
    public final static int VHDR_ID = IFFParser.stringToID("VHDR");
    public final static int NAME_ID = IFFParser.stringToID("NAME");
    public final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");
    public final static int ANNO_ID = IFFParser.stringToID("ANNO");
    public final static int AUTH_ID = IFFParser.stringToID("AUTH");
    //public final static int ATAK_ID = IFFParser.stringToID("ATAK");
    //public final static int RLSE_ID = IFFParser.stringToID("RLSE");
    public final static int CHAN_ID = IFFParser.stringToID("CHAN");
    //public final static int PAN_ID = IFFParser.stringToID("PAN ");
    public final static int BODY_ID = IFFParser.stringToID("BODY");

    /* Instance variables */
    private Vector samples_ = new Vector();
    private boolean within8SVXGroup_ = false;

    /* Constructors  */
    public EightSVXDecoder(InputStream in)
    throws IOException {
        try {
            IFFParser iff = new IFFParser();
            registerChunks(iff);
            iff.parse(in,this);
        }
        catch (ParseException e) {
            throw new IOException(e.toString());
        }
        catch (AbortException e) {
            throw new IOException(e.toString());
        }
        finally {
            in.close();
        }
    }

    public EightSVXDecoder() {
    }

    /* Accessors */
    public Vector getSamples() {
        return samples_;
    }

    /* Actions */
    public void registerChunks(IFFParser iff) {
        iff.declareGroupChunk(EIGHT_SVX_ID,IFFParser.ID_FORM);
        iff.declarePropertyChunk(EIGHT_SVX_ID,VHDR_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID,NAME_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID,COPYRIGHT_ID);
        iff.declareCollectionChunk(EIGHT_SVX_ID,ANNO_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID,AUTH_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID,CHAN_ID);
        iff.declareDataChunk(EIGHT_SVX_ID,BODY_ID);
    }

    public void enterGroup(IFFChunk group) {
        if (group.getType() == EIGHT_SVX_ID) { within8SVXGroup_ = true;}
    }
    public void leaveGroup(IFFChunk group) {
        if (group.getType() == EIGHT_SVX_ID) { within8SVXGroup_ = false;}
    }
    public void visitChunk(IFFChunk group, IFFChunk chunk)
    throws ParseException {
        if (within8SVXGroup_) {
            if (chunk.getID() == BODY_ID ) // && group.getID() == EIGHT_SVX_ID)
            {
                if (group.getPropertyChunk(VHDR_ID) == null) {
                    throw new ParseException("Sorry: Without 8SVX.VHDR-Chunk no sound possible");
                }
                EightSVXAudioClip newSample = new EightSVXAudioClip();
                decodeVHDR(newSample,group.getPropertyChunk(VHDR_ID));
                decodeCHAN(newSample,group.getPropertyChunk(CHAN_ID));
                decodeNAME(newSample,group.getPropertyChunk(NAME_ID));
                decodeCOPYRIGHT(newSample,group.getPropertyChunk(COPYRIGHT_ID));
                decodeAUTH(newSample,group.getPropertyChunk(COPYRIGHT_ID));
                decodeANNO(newSample,group.getCollectionChunks(ANNO_ID));
                decodeBODY(newSample,chunk);
                addAudioClip(newSample);
            }
        }
    }

    public void addAudioClip(AudioClip clip) {
        samples_.addElement(clip);
    }

    protected void decodeVHDR(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        try {
            if (chunk != null) {
                MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
                sample.setOneShotHiSamples(in.readULONG());
                sample.setRepeatHiSamples(in.readULONG());
                sample.setSamplesPerHiCycle(in.readULONG());
                sample.setSampleRate(in.readUWORD());
                sample.setCtOctave(in.readUBYTE());
                sample.setSCompression(in.readUBYTE());
                sample.setVolume(in.readLONG());
            }
        }
        catch (IOException e) {
            throw new ParseException("Error parsing 8SVX VHDR:" +e.getMessage());
        }
    }

    protected void decodeCHAN(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        if (chunk != null) {
            sample.setSampleType(chunk.getData()[3]);
        }
    }

    protected void decodeNAME(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        if (chunk != null) {
            sample.setName(new String(chunk.getData()));
        }
    }

    protected void decodeCOPYRIGHT(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        if (chunk != null) {
            sample.setCopyright(new String(chunk.getData()));
        }
    }

    protected void decodeAUTH(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        if (chunk != null) {
            sample.setAuthor(new String(chunk.getData()));
        }
    }

    protected void decodeANNO(EightSVXAudioClip sample,IFFChunk[] chunks)
    throws ParseException {
        if (chunks != null) {
            for (int i=0; i < chunks.length; i++) {
                IFFChunk chunk = chunks[i];
                sample.setRemark(sample.getRemark() + new String(chunk.getData()));
            }
        }
    }

    protected void decodeBODY(EightSVXAudioClip sample,IFFChunk chunk)
    throws ParseException {
        if (chunk != null) {
            byte[] data = chunk.getData();
            sample.set8SVXBody(data);
        }
    }

}
