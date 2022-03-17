
package org.monte.media.tiff;

import org.monte.media.math.Rational;


public class TIFFTag {

    public final static int ASCII_MASK = 1 << IFDDataType.ASCII.getTypeNumber();
    public final static int BYTE_MASK = 1 << IFDDataType.BYTE.getTypeNumber();
    public final static int DOUBLE_MASK = 1 << IFDDataType.DOUBLE.getTypeNumber();
    public final static int FLOAT_MASK = 1 << IFDDataType.FLOAT.getTypeNumber();
    public final static int IFD_MASK = 1 << IFDDataType.IFD.getTypeNumber();
    public final static int LONG_MASK = 1 << IFDDataType.LONG.getTypeNumber();
    public final static int SHORT_MASK = 1 << IFDDataType.SHORT.getTypeNumber();
    public final static int RATIONAL_MASK = 1 << IFDDataType.RATIONAL.getTypeNumber();
    public final static int SBYTE_MASK = 1 << IFDDataType.BYTE.getTypeNumber();
    public final static int SLONG_MASK = 1 << IFDDataType.SLONG.getTypeNumber();
    public final static int SSHORT_MASK = 1 << IFDDataType.SSHORT.getTypeNumber();
    public final static int SRATIONAL_MASK = 1 << IFDDataType.SRATIONAL.getTypeNumber();
    public final static int UNDEFINED_MASK = 1 << IFDDataType.UNDEFINED.getTypeNumber();
    public final static int ALL_MASK = -1;
    private String name;
    private int number;
    private int dataTypes;
    private TagSet tagSet;
    private ValueFormatter formatter;


    public TIFFTag(String name,
            int number,
            int dataTypes,
            ValueFormatter formatter) {
        this.name = name;
        this.number = number;
        this.dataTypes = dataTypes;
        this.formatter = formatter;
    }


    public TIFFTag(String name,
            int number,
            int dataTypes) {
        this(name, number, dataTypes, null);
    }


     void setTagSet(TagSet tagSet) {
        this.tagSet = tagSet;
    }


    public int getNumber() {
        return number;
    }


    public String getName() {
        return name;
    }

    public boolean isSynthetic() {
        return number < 0;
    }

    public IFDDataType getType(Object data) {
        int m = dataTypes;

        if (data != null && data.getClass().isArray()) {
            data = ((Object[]) data)[0];
        }

        for (int i = 0; i < 32; i++) {
            if ((m & 1) == 1) {
                switch (IFDDataType.valueOf(i)) {
                    case ASCII:
                        if (data == null
                                || (data instanceof String)) {
                            return IFDDataType.ASCII;
                        }
                        break;
                    case BYTE:
                        if (data == null
                                || (data instanceof Short)) {
                            return IFDDataType.BYTE;
                        }
                        break;
                    case DOUBLE:
                        if (data == null
                                || (data instanceof Double)) {
                            return IFDDataType.DOUBLE;
                        }
                        break;
                    case FLOAT:
                        if (data == null
                                || (data instanceof Float)) {
                            return IFDDataType.FLOAT;
                        }
                        break;
                    case IFD:
                        if (data == null
                                || (data instanceof Long)) {
                            return IFDDataType.IFD;
                        }
                        break;
                    case LONG:
                        if (data == null
                                || (data instanceof Long)) {
                            return IFDDataType.LONG;
                        }
                        break;
                    case RATIONAL:
                        if (data == null
                                || (data instanceof Rational)) {
                            return IFDDataType.RATIONAL;
                        }
                        break;
                    case SBYTE:
                        if (data == null
                                || (data instanceof Byte)) {
                            return IFDDataType.SBYTE;
                        }
                        break;
                    case SHORT:
                        if (data == null
                                || (data instanceof Integer)) {
                            return IFDDataType.SHORT;
                        }
                        break;
                    case SLONG:
                        if (data == null
                                || (data instanceof Integer)) {
                            return IFDDataType.SLONG;
                        }
                        break;
                    case SRATIONAL:
                        if (data == null
                                || (data instanceof Rational)) {
                            return IFDDataType.SRATIONAL;
                        }
                        break;
                    case SSHORT:
                        if (data == null
                                || (data instanceof Short)) {
                            return IFDDataType.SSHORT;
                        }
                        break;
                    case UNDEFINED:
                        if (data == null
                                || (data instanceof Byte)) {
                            return IFDDataType.UNDEFINED;
                        }
                        break;
                }

                return IFDDataType.valueOf(i);
            }
            m >>= 1;
        }
        return IFDDataType.UNDEFINED;
    }

    public Object prettyFormat(Object data) {
        return (formatter == null) ? data : formatter.prettyFormat(data);
    }

    public Object format(Object data) {
        return (formatter == null) ? data : formatter.format(data);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription(Object data) {
        return (formatter == null) ? null : formatter.descriptionFormat(data);
    }
}
