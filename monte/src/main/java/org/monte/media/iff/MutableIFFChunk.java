
package org.monte.media.iff;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;


public class MutableIFFChunk extends DefaultMutableTreeNode {


    public final static int ID_FORM = 0x464f524d;

    public final static int ID_CAT = 0x43415420;

    public final static int ID_LIST = 0x4c495354;

    public final static int ID_PROP = 0x50524f50;

    public final static int ID_FILLER = 0x20202020;

    private int type;

    private int id;

    private byte[] data;


    public MutableIFFChunk() {
    }


    public MutableIFFChunk(int id, int type) {
        this.id = id;
        this.type = type;
    }


    public MutableIFFChunk(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }


    public MutableIFFChunk(String id, String type) {
        this.id = stringToId(id);
        this.type = stringToId(type);
    }


    public MutableIFFChunk(String id, byte[] data) {
        this.id = stringToId(id);
        this.data = data;
    }

    public void setType(int newValue) {
        int oldValue = type;
        type = newValue;
    }

    public void setId(int newValue) {
        int oldValue = id;
        id = newValue;
    }

    public void setData(byte[] newValue) {
        byte[] oldValue = data;
        data = newValue;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        if (data != null) {
            return data.length;
        } else {
            int length = 4;
            for (MutableIFFChunk child : childChunks()) {
                int childLength = child.getLength();
                length += 8 + childLength + childLength % 2;
            }
            return length;
        }
    }

    public Vector<MutableIFFChunk> childChunks() {
        return (children == null) ? new Vector<MutableIFFChunk>(0) : new Vector<MutableIFFChunk>(children);
    }

    public String dump() {
        return dump(0);

    }

    public String dump(int depth) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            buf.append('.');
        }
        buf.append(idToString(getId()));
        buf.append(' ');
        buf.append(getLength());
        if (getChildCount() > 0) {
            buf.append(' ');
            buf.append(idToString(getType()));
            for (MutableIFFChunk child : childChunks()) {
                buf.append('\n');
                buf.append(child.dump(depth + 1));
            }
        }
        return buf.toString();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(idToString(getId()));
        buf.append(' ');
        buf.append(getLength());
        if (data == null) {
            buf.append(' ');
            buf.append(idToString(getType()));
        }
        return buf.toString();
    }


    public static String idToString(int anID) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (anID >>> 24);
        bytes[1] = (byte) (anID >>> 16);
        bytes[2] = (byte) (anID >>> 8);
        bytes[3] = (byte) (anID >>> 0);

        return new String(bytes);
    }


    public static int stringToId(String aString) {
        byte[] bytes = aString.getBytes();

        return ((int) bytes[0]) << 24 |
                ((int) bytes[1]) << 16 |
                ((int) bytes[2]) << 8 |
                ((int) bytes[3]) << 0;
    }

    public void read(File f) throws IOException {
        MC68000InputStream in = new MC68000InputStream(
                new BufferedInputStream(
                new FileInputStream(f)));
        try {
            read(in);
        } finally {
            in.close();
        }
    }

    public void read(MC68000InputStream in) throws IOException {
        id = in.readLONG();
        long length = in.readULONG();
        switch (id) {
            case ID_CAT:
            case ID_FORM:
            case ID_LIST:
            case ID_PROP:
                type = in.readLONG();
                length -= 4;
                while (length > 1) {
                    MutableIFFChunk child = new MutableIFFChunk();
                    child.read(in);
                    add(child);
                    int childLength = child.getLength();
                    length -= childLength + childLength % 2 + 8;
                }
                break;
            default:
                data = new byte[(int) length];
                in.readFully(data, 0, (int) length);
                break;
        }
        if (length % 2 == 1) {
            in.read();
        }
    }

    public void Write(File f) throws IOException {
        MC68000OutputStream out = new MC68000OutputStream(
                new BufferedOutputStream(
                new FileOutputStream(f)));
        try {
            write(out);
        } finally {
            out.close();
        }
    }

    public void write(MC68000OutputStream out) throws IOException {
        out.writeULONG(id);
        long length = getLength();
        out.writeULONG(length);
        if (data == null) {
            out.writeULONG(type);
            for (MutableIFFChunk child : childChunks()) {
                child.write(out);
            }
        } else {
            out.write(data);
        }
        if (length % 2 == 1) {
            out.write((byte) 0);
        }
    }
}
