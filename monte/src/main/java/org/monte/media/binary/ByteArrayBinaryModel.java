
package org.monte.media.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


public class ByteArrayBinaryModel implements BinaryModel {




    private Vector elemTable;

    private long length;

    private int elemSize = 1024;

    public ByteArrayBinaryModel() {
        elemTable = new Vector();
        length = 0;
    }

    public ByteArrayBinaryModel(byte[] data) {
        elemTable = new Vector();
        if (data == null || data.length == 0) {
            length = 0;
        } else {
            elemTable.addElement(data);
            length = elemSize = data.length;
        }
    }

    public ByteArrayBinaryModel(InputStream in)
            throws IOException {
        this();



        byte[] elem = new byte[elemSize];
        int elemLen = 0;
        while (true) {
            int readLen = in.read(elem, elemLen, elemSize - elemLen);
            if (readLen == -1) {
                elemTable.addElement(elem);
                length += elemLen;
                break;
            }
            elemLen += readLen;
            if (elemLen == elemSize) {
                elemTable.addElement(elem);
                length += elemSize;
                elem = new byte[elemSize];
                elemLen = 0;
            }
        }
    }

    public long getLength() {
        return length;
    }


    @Override
    public int getBytes(long offset, int len, byte[] target) {
        int off = (int) offset;
        if (len + offset > length) {
            len = (int) (length - offset);
        }


        int index = off / elemSize;


        byte[] elem = (byte[]) elemTable.elementAt(index);


        int count = 0;


        int i = off % elemSize;


        while (count < len) {
            if (i == elem.length) {
                elem = (byte[]) elemTable.elementAt(++index);
                i = 0;
            }
            target[count++] = elem[i++];
        }
        return count;
    }

    @Override
    public void close() {
        elemTable=null;
        length=0;
    }
}
