
package org.monte.media.tiff;

import java.io.IOException;
import java.nio.ByteOrder;


public class IFDEntry {


    private int tagNumber;

    private int typeNumber;

    private long count;

    private long valueOffset;

    private long entryOffset;

    private long ifdOffset;

    private Object data;

    public IFDEntry(int tagNumber, int typeNumber, long count, long valueOffset, long entryOffset) {
        this.tagNumber = tagNumber;
        this.typeNumber = typeNumber;
        this.count = count;
        this.valueOffset = valueOffset;
        this.entryOffset = entryOffset;
    }

    public long getCount() {
        return count;
    }

    public int getTagNumber() {
        return tagNumber;
    }

    public int getTypeNumber() {
        return typeNumber;
    }


    public long getValueOffset() {
        return valueOffset;
    }


    public long getDataOffset() {
        return isDataInValueOffset() ? entryOffset + 8 : valueOffset+ifdOffset;
    }

    public void setIFDOffset(long newValue) {
        ifdOffset = newValue;
    }

    public long getEntryOffset() {
        return entryOffset;
    }
    public long getIFDOffset() {
        return ifdOffset;
    }

    public boolean isDataInValueOffset() {
        switch (IFDDataType.valueOf(typeNumber)) {
            case ASCII:

                return false;
            case BYTE:
                return count <= 4;
            case SHORT:
                return count <= 2;
            case LONG:
                return count <= 1;
            case RATIONAL:
                return false;
            case SBYTE:
                return count <= 4;
            case UNDEFINED:
                return count <= 4;
            case SSHORT:
                return count <= 2;
            case SLONG:
                return count <= 1;
            case SRATIONAL:
                return false;
            case FLOAT:
                return count <= 1;
            case DOUBLE:
                return false;
            default:
                return true;
        }
    }

    public long getLength() {
        switch (IFDDataType.valueOf(typeNumber)) {
            case ASCII:
                return count;
            case BYTE:
                return count;
            case SHORT:
                return count * 2;
            case LONG:
                return count * 4;
            case RATIONAL:
                return count * 8;
            case SBYTE:
                return count;
            case UNDEFINED:
                return count;
            case SSHORT:
                return count * 2;
            case SLONG:
                return count * 4;
            case SRATIONAL:
                return count * 8;
            case FLOAT:
                return count * 4;
            case DOUBLE:
                return count * 8;
            default:
                return 0;
        }
    }


    public Object readData(TIFFInputStream in) throws IOException {
        return readData(in, ifdOffset);
    }


    public Object readData(TIFFInputStream in, long ifdDataOffset) throws IOException {
        Object d = null;
        IFDDataType tt = IFDDataType.valueOf(typeNumber);
        if (tt != null) {
            switch (tt) {
                case ASCII:

                    if (count <= 4) {
                        StringBuilder buf = new StringBuilder();
                        int data = (int) valueOffset;
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            for (int i = 0; i < count - 1; i++) {
                                buf.append((char) (data & 0xff));
                                data >>= 8;
                            }
                        } else {
                            for (int i = 0; i < count - 1; i++) {
                                buf.append((char) (data >>> 24));
                                data <<= 8;
                            }
                        }
                        return buf.toString();
                    } else {
                        return in.readASCII(valueOffset + ifdDataOffset, count);
                    }
                case SHORT:
                    if (count == 1) {
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            d = (int) (valueOffset & 0xffff);
                        } else {
                            d = (int) ((valueOffset >> 16) & 0xffff);
                        }
                    } else if (count == 2) {
                        d = new int[]{(int) (valueOffset & 0xffff), (int) ((valueOffset & 0xffff0000) >> 16)};
                    } else {
                        d = in.readSHORT(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case LONG:
                    if (count == 1) {
                        d = valueOffset;
                    } else {
                        d = in.readLONG(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case RATIONAL:
                    if (count == 1) {
                        d = in.readRATIONAL(valueOffset + ifdDataOffset);
                    } else {
                        d = in.readRATIONAL(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case BYTE:
                    if (count == 1) {
                        d = (short) (valueOffset & 0xff);
                    } else if (count == 2) {
                        d = new short[]{(short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else if (count == 3) {
                        d = new short[]{(short) ((valueOffset & 0xff0000) >> 16), (short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else if (count == 4) {
                        d = new short[]{(short) ((valueOffset & 0xff000000) >> 24), (short) ((valueOffset & 0xff0000) >> 16), (short) ((valueOffset & 0xff00) >> 8), (short) (valueOffset & 0xff)};
                    } else {
                        byte[] b = new byte[(int) count];
                        in.read(valueOffset + ifdDataOffset, b, 0, b.length);
                        short[] s = new short[(int) count];
                        for (int i = 0; i < b.length; i++) {
                            s[i] = (short) (b[i] & 0xff);
                        }
                        d = s;
                    }
                    break;
                case SBYTE:
                case UNDEFINED:
                    if (count == 1) {
                        d = (byte) valueOffset;
                    } else if (count == 2) {
                        d = new byte[]{(byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else if (count == 3) {
                        d = new byte[]{(byte) ((valueOffset & 0xff0000) >> 16), (byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else if (count == 4) {
                        d = new byte[]{(byte) ((valueOffset & 0xff000000) >> 24), (byte) ((valueOffset & 0xff0000) >> 16), (byte) ((valueOffset & 0xff00) >> 8), (byte) (valueOffset & 0xff)};
                    } else {
                        byte[] b = new byte[(int) count];
                        in.read(valueOffset + ifdDataOffset, b, 0, b.length);
                        d = b;
                    }
                    break;
                case SSHORT:
                    if (count == 1) {
                        if (in.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                            d = (short) (valueOffset & 0xffff);
                        } else {
                            d = (short) ((valueOffset >> 16) & 0xffff);
                        }
                    } else if (count == 2) {
                        d = new int[]{(short) (valueOffset & 0xffff), (short) ((valueOffset & 0xffff0000) >> 16)};
                    } else {
                        d = in.readSSHORT(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case SLONG:
                    throw new IOException("Format " + typeNumber + " not implemented");
                case SRATIONAL:
                    if (count == 1) {
                        d = in.readSRATIONAL(valueOffset + ifdDataOffset);
                    } else {
                        d = in.readSRATIONAL(valueOffset + ifdDataOffset, count);
                    }
                    break;
                case FLOAT:
                case DOUBLE:
                default:
                    throw new IOException("Format " + typeNumber + " not implemented");
            }
        }
        return d;
    }

    public void loadData(TIFFInputStream in) throws IOException {
        data = readData(in);
    }

    public Object getData() {
        return data;
    }


    @Override
    public String toString() {
        return "IFD Entry: tag:0x" + Integer.toHexString(tagNumber) + " type:0x" + Integer.toHexString(typeNumber) + " count:0x" + Long.toHexString(count) + " valueOffset:0x" + Long.toHexString(valueOffset);
    }

    public String toString(Enum tagName) {
        StringBuilder buf = new StringBuilder();
        buf.append(
                "Entry tag:" + tagName + "(" + Integer.toHexString(tagNumber) + "), type:" + IFDDataType.valueOf(typeNumber) + "(" + typeNumber + "), count:" + count + ", valueOffset:" + valueOffset);
        if (data != null) {
            buf.append(", data:");
            if (data instanceof byte[]) {
                byte[] d = (byte[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof short[]) {
                short[] d = (short[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof int[]) {
                int[] d = (int[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof long[]) {
                long[] d = (long[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else if (data instanceof Object[]) {
                Object[] d = (Object[]) data;
                for (int i = 0; i < d.length; i++) {
                    if (i != 0) {
                        buf.append(',');
                    }
                    buf.append(d[i]);
                }
            } else {
                buf.append(data);
            }
        }
        return buf.toString();
    }
}
