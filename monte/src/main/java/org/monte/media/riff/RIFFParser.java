
package org.monte.media.riff;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import org.monte.media.io.ImageInputStreamAdapter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import javax.imageio.stream.ImageInputStream;

public class RIFFParser extends Object {
private final static boolean DEBUG =false;
    public final static int RIFF_ID = stringToID("RIFF");
    public final static int LIST_ID = stringToID("LIST");
    public final static int NULL_ID = stringToID("    ");
    public final static int NULL_NUL_ID = stringToID("\0\0\0\0");
    public final static int JUNK_ID = stringToID("JUNK");
    private RIFFVisitor visitor;
    private HashSet<RIFFChunk> dataChunks;
    private HashSet<RIFFChunk> propertyChunks;
    private HashSet<RIFFChunk> collectionChunks;
    private HashSet<Integer> stopChunkTypes;
    private HashSet<RIFFChunk> groupChunks;
    private RIFFPrimitivesInputStream in;
    private ImageInputStream iin;

    private boolean isStopChunks;

    private long streamOffset;

    /* ---- constructors ---- */
    public RIFFParser() {
    }

    public long getStreamOffset() {
        return streamOffset;
    }

    public void setStreamOffset(long offset) {
        this.streamOffset = offset;
    }



    /* ---- accessor methods ---- */
    /* ---- action methods ---- */
    public long parse(InputStream in, RIFFVisitor v)
            throws ParseException, AbortException, IOException {
        this.in = new RIFFPrimitivesInputStream(in);
        visitor = v;
        parseFile();
        return getScan(this.in);
    }

    public long parse(ImageInputStream in, RIFFVisitor v)
            throws ParseException, AbortException, IOException {
        return parse(new ImageInputStreamAdapter(in), v);
    }

    private void parseFile()
            throws ParseException, AbortException, IOException {
        int id = in.readFourCC();

        if (id == RIFF_ID) {
            parseFORM(null);
        } else if (id == JUNK_ID) {
            parseLocalChunk(null,id);
        } else {
            if (iin!=null) {
            throw new ParseException("Invalid RIFF File ID: \"" + idToString(id) +" 0x"+Integer.toHexString(id)+" near "+iin.getStreamPosition()+" 0x"+Long.toHexString(iin.getStreamPosition()));
            } else {
            throw new ParseException("Invalid RIFF File ID: \"" + idToString(id) +" 0x"+Integer.toHexString(id));
            }
        }
    }

    private long getScan(RIFFPrimitivesInputStream in) {
        return in.getScan()+streamOffset;
    }

    private void parseFORM(HashMap props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long offset = getScan(in);
        int type = in.readFourCC();
if (DEBUG)System.out.println("RIFFParser.parseForm "+idToString(type));
        if (!isGroupType(type)) {
            throw new ParseException("Invalid FORM Type: \"" + idToString(type) + "\"");
        }

        RIFFChunk propGroup = (props == null) ? null : (RIFFChunk) props.get(type);
        RIFFChunk chunk = new RIFFChunk(type, RIFF_ID, size, offset, propGroup);

        boolean visitorWantsToEnterGroup = false;
        if (isGroupChunk(chunk) && (visitorWantsToEnterGroup = visitor.enteringGroup(chunk))) {
            visitor.enterGroup(chunk);
        }

        try {
            long finish = offset + size;
            while (getScan(in) < finish) {
                long idscan = getScan(in);
                int id = in.readFourCC();

                if (id == RIFF_ID) {
                    parseFORM(props);
                } else if (id == LIST_ID) {
                    parseLIST(props);
                } else if (isLocalChunkID(id)) {
                    parseLocalChunk(chunk, id);
                } else {
                    ParseException pex = new ParseException("Invalid Chunk: \"" + id + "\" at offset:" + idscan);
                    chunk.setParserMessage(pex.getMessage());
                    throw pex;
                }

                in.align();
            }
        } catch (EOFException e) {
            e.printStackTrace();
            chunk.setParserMessage(
                    "Unexpected EOF after "
                    + NumberFormat.getInstance().format(getScan(in) - offset)
                    + " bytes");
        } finally {
            if (visitorWantsToEnterGroup) {
                visitor.leaveGroup(chunk);
            }
        }
    }

    private void parseLIST(HashMap props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = getScan(in);
        int type = in.readFourCC();
if (DEBUG)System.out.println("RIFFParser.parseLIST "+idToString(type));

        if (!isGroupType(type)) {
            throw new ParseException("Invalid LIST Type: \"" + type + "\"");
        }

        RIFFChunk propGroup = (props == null) ? null : (RIFFChunk) props.get(type);
        RIFFChunk chunk = new RIFFChunk(type, LIST_ID, size, scan, propGroup);

        boolean visitorWantsToEnterGroup = false;
        if (isGroupChunk(chunk) && (visitorWantsToEnterGroup = visitor.enteringGroup(chunk))) {
            visitor.enterGroup(chunk);
        }
        try {
            if (visitorWantsToEnterGroup) {
            long finish = scan + size;
            while (getScan(in) < finish) {
                long idscan = getScan(in);
                int id = in.readFourCC();
                if (id == LIST_ID) {
                    parseLIST(props);
                } else if (isLocalChunkID(id)) {
                    parseLocalChunk(chunk, id);
                } else {
                    parseGarbage(chunk, id, finish-getScan(in), getScan(in));
                    ParseException pex = new ParseException("Invalid Chunk: \"" + id + "\" at offset:" + idscan);
                    chunk.setParserMessage(pex.getMessage());
                    //throw pex;
                }

                    in.align();
                }
            } else {
                in.skipFully(size-4);
                in.align();
            }
        } finally {
            if (visitorWantsToEnterGroup) {
                visitor.leaveGroup(chunk);
            }
        }
    }

    private void parseLocalChunk(RIFFChunk parent, int id)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = getScan(in);
if (DEBUG)System.out.println("RIFFParser.parseLocalChunk "+idToString(id));
        RIFFChunk chunk = new RIFFChunk(parent==null?0:parent.getType(), id, size, scan);

        if (isDataChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            visitor.visitChunk(parent, chunk);
        } else if (isPropertyChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.putPropertyChunk(chunk);
        } else if (isCollectionChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.addCollectionChunk(chunk);
        } else {
            in.skipFully((int) size);
            if (isStopChunks) {
            visitor.visitChunk(parent, chunk);
            }
        }
    }
    private void parseGarbage(RIFFChunk parent, int id, long size, long scan)
            throws ParseException, AbortException, IOException {
        //long size = in.readULONG();
        //long scan = getScan(in);

        RIFFChunk chunk = new RIFFChunk(parent.getType(), id, size, scan);

        if (isDataChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            visitor.visitChunk(parent, chunk);
        } else if (isPropertyChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.putPropertyChunk(chunk);
        } else if (isCollectionChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.addCollectionChunk(chunk);
        } else {
            in.skipFully((int) size);
            if (isStopChunk(chunk)) {
                visitor.visitChunk(parent, chunk);
            }
        }
    }

    protected boolean isDataChunk(RIFFChunk chunk) {
        if (dataChunks == null) {
            if (collectionChunks == null && propertyChunks == null && (stopChunkTypes==null||!stopChunkTypes.contains(chunk.getType()))) {
                return true;
            } else {
                return false;
            }
        } else {
            return dataChunks.contains(chunk);
        }
    }

    protected boolean isGroupChunk(RIFFChunk chunk) {
        if (groupChunks == null) {
            return true;
        } else {
            return groupChunks.contains(chunk);
        }
    }

    protected boolean isPropertyChunk(RIFFChunk chunk) {
        if (propertyChunks == null) {
            return false;
        } else {
            return propertyChunks.contains(chunk);
        }
    }

    protected boolean isCollectionChunk(RIFFChunk chunk) {
        if (collectionChunks == null) {
            return false;
        } else {
            return collectionChunks.contains(chunk);
        }
    }

    public void declareDataChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (dataChunks == null) {
            dataChunks = new HashSet<RIFFChunk>();
        }
        dataChunks.add(chunk);
    }

    public void declareGroupChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (groupChunks == null) {
            groupChunks = new HashSet<RIFFChunk>();
        }
        groupChunks.add(chunk);
    }

    public void declarePropertyChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (propertyChunks == null) {
            propertyChunks = new HashSet<RIFFChunk>();
        }
        propertyChunks.add(chunk);
    }

    public void declareCollectionChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (collectionChunks == null) {
            collectionChunks = new HashSet<RIFFChunk>();
        }
        collectionChunks.add(chunk);
    }

    public void declareStopChunkType(int type) {
        if (stopChunkTypes == null) {
            stopChunkTypes = new HashSet<Integer>();
        }
        stopChunkTypes.add(type);
    }

    public void declareStopChunks() {
        isStopChunks = true;
    }

    private boolean isStopChunk(RIFFChunk chunk) {
        return isStopChunks||stopChunkTypes!=null&&stopChunkTypes.contains(chunk.getType());
    }

    /* ---- Class methods ---- */
    public static boolean isGroupID(int id) {
        return id == LIST_ID || id == RIFF_ID;
    }

    public static boolean isGroupType(int id) {
        return isID(id) && !isGroupID(id) && id != NULL_ID;
    }

    public static boolean isID(int id) {
        int c0 = id >> 24;
        int c1 = (id >> 16) & 0xff;
        int c2 = (id >> 8) & 0xff;
        int c3 = id & 0xff;

        return id == NULL_NUL_ID
                || c0 >= 0x20 && c0 <= 0x7e
                && c1 >= 0x20 && c1 <= 0x7e
                && c2 >= 0x20 && c2 <= 0x7e
                && c3 >= 0x20 && c3 <= 0x7e;
    }

    public static boolean isLocalChunkID(int id) {
        if (isGroupID(id)) {
            return false;
        }
        return id != NULL_ID && isID(id);
    }
    private WeakHashMap<String, String> ids;

    public static String idToString(int anInt) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (anInt >>> 24);
        bytes[1] = (byte) (anInt >>> 16);
        bytes[2] = (byte) (anInt >>> 8);
        bytes[3] = (byte) (anInt >>> 0);

        try {
            return new String(bytes, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public static int stringToID(String aString) {
        byte[] bytes = aString.getBytes();

        return ((int) bytes[0]) << 24
                | ((int) bytes[1]) << 16
                | ((int) bytes[2]) << 8
                | ((int) bytes[3]) << 0;
    }
}
