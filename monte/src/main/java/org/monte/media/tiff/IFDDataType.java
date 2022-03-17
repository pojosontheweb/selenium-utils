
package org.monte.media.tiff;

import java.util.HashMap;


public enum IFDDataType {

    
    ASCII(2),

    
    BYTE(1),

    
    SHORT(3),

    
    LONG(4),

    
    RATIONAL(5),

    
    SBYTE(6),

    
    UNDEFINED(7),

    
    SSHORT(8),

    
    SLONG(9),

    
    SRATIONAL(10),

    
    FLOAT(11),

    
    DOUBLE(12),
    
    IFD(13)
    ;

    private final int typeNumber;
    private final static HashMap<Integer, IFDDataType> valueToFieldType = new HashMap<Integer, IFDDataType>();

    static {
        for (IFDDataType t : IFDDataType.values()) {
            valueToFieldType.put(t.getTypeNumber(), t);
        }
    }

    private IFDDataType(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    
    public static IFDDataType valueOf(int typeNumber) {
        return valueToFieldType.get(typeNumber);
    }

}
