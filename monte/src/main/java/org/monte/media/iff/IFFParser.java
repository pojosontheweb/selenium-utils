
package org.monte.media.iff;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import java.io.*;
import java.util.Hashtable;

public class IFFParser
        extends Object {

    public final static int ID_FORM = 0x464f524d;
    public final static int ID_CAT = 0x43415420;
    public final static int ID_LIST = 0x4c495354;
    public final static int ID_PROP = 0x50524f50;
    public final static int ID_FILLER = 0x20202020;
    public final static int[] RESERVED_IDs = {
        0x4c495354, 0x464f524d, 0x50524f50, 0x43415420, 0x20202020,
        0x4c495331, 0x4c495332, 0x4c495333, 0x4c495334, 0x4c495335, 0x4c495336, 0x4c495337, 0x4c495338, 0x4c495339,
        0x464f5231, 0x464f5232, 0x464f5233, 0x464f5234, 0x464f5235, 0x464f5236, 0x464f5237, 0x464f5238, 0x464f5239,
        0x43415431, 0x43415432, 0x43415433, 0x43415434, 0x43415435, 0x43415436, 0x43415437, 0x43415438, 0x43415439
    };
    private IFFVisitor visitor;
    private Hashtable<IFFChunk,IFFChunk> dataChunks;
    private Hashtable<IFFChunk,IFFChunk> propertyChunks;
    private Hashtable<IFFChunk,IFFChunk> collectionChunks;
    private Hashtable<Integer,IFFChunk> groupChunks;
    private MC68000InputStream in;

    /* ---- constructors ---- */
    public IFFParser() {
    }

    /* ---- accessor methods ---- */
    /* ---- action methods ---- */
    public void parse(InputStream in, IFFVisitor v)
            throws ParseException, AbortException, IOException {
        this.in = new MC68000InputStream(new BufferedInputStream(in));
        visitor = v;
        parseFile();
    }

    private void parseFile()
            throws ParseException, AbortException, IOException {
        int id = in.readLONG();

        switch (id) {
            case ID_FORM:
                parseFORM(null);
                break;
            case ID_CAT:
                parseCAT(null);
                break;
            case ID_LIST:
                parseLIST(null);
                break;
            default:
                throw new ParseException("IFF-85 files must start with 'FORM', 'CAT ', or 'LIST'. But not with: '" + idToString(id) + "'");
        }
    }

    private void parseFORM(Hashtable props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : (IFFChunk) props.get(new Integer(type));
        IFFChunk chunk = new IFFChunk(type, ID_FORM, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        try {
            while (in.getScan() < finish) {
                long idscan = in.getScan();
                int id = in.readLONG();
                switch (id) {
                    case ID_FORM:
                        parseFORM(props);
                        break;
                    case ID_CAT:
                        parseCAT(props);
                        break;
                    case ID_LIST:
                        parseLIST(props);
                        break;
                    default:
                        if (isLocalChunkID(id)) {
                            parseLocalChunk(chunk, id);
                        } else {
                            throw new ParseException("Invalid IFFChunk within FORM: " + idToString(id) + " at offset:" + idscan);
                        }
                }
                //            System.out.println("Found IFFChunk within Form:" + idToString(id)+" at offset:"+idscan);

                in.align();
            }
        } catch (EOFException e) {
            System.err.println("Unexpected EOF after:" + (in.getScan() - scan) + " should be:" + size);
            e.printStackTrace();
        }
        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    private void parseCAT(Hashtable props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isContentType(type)) {
            throw new ParseException("Invalid Content Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : (IFFChunk) props.get(new Integer(type));
        IFFChunk chunk = new IFFChunk(type, ID_CAT, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            switch (id) {
                case ID_FORM:
                    parseFORM(props);
                    break;
                case ID_CAT:
                    parseCAT(props);
                    break;
                case ID_LIST:
                    parseLIST(props);
                    break;
                default:
                    throw new ParseException("Invalid IFFChunk within CAT: " + idToString(id));
            }
            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseLIST(Hashtable props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : (IFFChunk) props.get(new Integer(type));
        IFFChunk chunk = new IFFChunk(type, ID_LIST, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        props = new Hashtable();
        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            switch (id) {
                case ID_FORM:
                    parseFORM(props);
                    break;
                case ID_CAT:
                    parseCAT(props);
                    break;
                case ID_LIST:
                    parseLIST(props);
                    break;
                case ID_PROP:
                    IFFChunk prop = parsePROP();
                    props.put(new Integer(prop.getType()), prop);
                    break;
                default:
                    if (isLocalChunkID(id)) {
                        parseLocalChunk(chunk, id);
                    } else {
                        throw new ParseException("Invalid IFFChunk ID within LIST: " + idToString(id));
                    }
            }
            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    private IFFChunk parsePROP()
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk chunk = new IFFChunk(type, ID_PROP, size, scan);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            if (isLocalChunkID(id)) {
                parseLocalChunk(chunk, id);
            } else {
                throw new ParseException("Invalid IFFChunk ID within PROP: " + idToString(id));
            }

            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }

        return chunk;
    }

    private void parseLocalChunk(IFFChunk parent, int id)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();

        IFFChunk chunk = new IFFChunk(parent.getType(), id, size, scan);

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
            if (size > 0) {
                in.skipFully((int) size);
            }
        }
    }

    protected boolean isDataChunk(IFFChunk chunk) {
        if (dataChunks == null) {
            if (collectionChunks == null && propertyChunks == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return dataChunks.containsKey(chunk);
        }
    }

    protected boolean isGroupChunk(IFFChunk chunk) {
        if (groupChunks == null) {
            return true;
        } else {
            return groupChunks.containsKey(new Integer(chunk.getID()));
        }
    }

    protected boolean isPropertyChunk(IFFChunk chunk) {
        if (propertyChunks == null) {
            return false;
        } else {
            return propertyChunks.containsKey(chunk);
        }
    }

    protected boolean isCollectionChunk(IFFChunk chunk) {
        if (collectionChunks == null) {
            return false;
        } else {
            return collectionChunks.containsKey(chunk);
        }
    }

    @SuppressWarnings("unchecked")
    public void declareDataChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (dataChunks == null) {
            dataChunks = new Hashtable();
        }
        dataChunks.put(chunk, chunk);
    }

    public void declareGroupChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (groupChunks == null) {
            groupChunks = new Hashtable<Integer,IFFChunk>();
        }
        groupChunks.put(new Integer(id), chunk);
    }

    public void declarePropertyChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (propertyChunks == null) {
            propertyChunks = new Hashtable<IFFChunk,IFFChunk>();
        }
        propertyChunks.put(chunk, chunk);
    }

    public void declareCollectionChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (collectionChunks == null) {
            collectionChunks = new Hashtable<IFFChunk,IFFChunk>();
        }
        collectionChunks.put(chunk, chunk);
    }


    /* ---- Class methods ---- */
    public static boolean isGroupID(int id) {
        if (id == ID_FORM
                || id == ID_CAT
                || id == ID_LIST
                || id == ID_PROP) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isID(int id) {
        int value1 = id >> 24;
        int value2 = (id >> 16) & 0xff;
        int value3 = (id >> 8) & 0xff;
        int value4 = id & 0xff;

        if (value1 < 0x20 || value1 > 0x7e
                || value2 < 0x20 || value2 > 0x7e
                || value3 < 0x20 || value3 > 0x7e
                || value4 < 0x20 || value4 > 0x7e) {
            return false;
        }

        if (id != ID_FILLER && value1 == 0x20) {
            return false;
        }

        return true;
    }

    public static boolean isLocalChunkID(int id) {
        if (id == ID_FILLER) {
            return false;
        }
        if (isGroupID(id)) {
            return false;
        }
        return isID(id);
    }

    public static boolean isReservedID(int id) {
        for (int i = 0; i < RESERVED_IDs.length; i++) {
            if (id == RESERVED_IDs[i]) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFormType(int id) {
        if (isReservedID(id)) {
            return false;
        }

        int value1 = id >> 24;
        int value2 = (id >> 16) & 0xff;
        int value3 = (id >> 8) & 0xff;
        int value4 = id & 0xff;

        if (value1 < 0x30 || value1 > 0x5a || (value1 > 0x49 && value1 < 0x41)
                || (value2 < 0x30 && value2 != 0x20) || value2 > 0x5a || (value2 > 0x49 && value2 < 0x41)
                || (value3 < 0x30 && value3 != 0x20) || value3 > 0x5a || (value3 > 0x49 && value3 < 0x41)
                || (value4 < 0x30 && value4 != 0x20) || value4 > 0x5a || (value4 > 0x49 && value4 < 0x41)) {
            return false;
        }

        if (isGroupID(id)) {
            return false;
        }

        return true;
    }

    public static boolean isContentType(int id) {
        if (id == ID_FILLER) {
            return true;
        } else {
            return isFormType(id);
        }
    }

    public static String idToString(int anID) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (anID >>> 24);
        bytes[1] = (byte) (anID >>> 16);
        bytes[2] = (byte) (anID >>> 8);
        bytes[3] = (byte) (anID >>> 0);

        return new String(bytes);
    }

    public static int stringToID(String aString) {
        byte[] bytes = aString.getBytes();

        return ((int) bytes[0]) << 24
                | ((int) bytes[1]) << 16
                | ((int) bytes[2]) << 8
                | ((int) bytes[3]) << 0;
    }
}
