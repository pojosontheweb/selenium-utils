
package org.monte.media.iff;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import javax.imageio.stream.ImageOutputStream;


public class IFFOutputStream extends OutputStream {

    private byte[] writeBuffer = new byte[4];
    private Stack<Chunk> stack = new Stack<Chunk>();
    private ImageOutputStream out;
    private long streamOffset;

    public IFFOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        streamOffset = out.getStreamPosition();
    }

    public void pushCompositeChunk(String compositeType, String chunkType) throws IOException {
        stack.push(new CompositeChunk(compositeType, chunkType));
    }

    public void pushDataChunk(String chunkType) throws IOException {
        stack.push(new DataChunk(chunkType));
    }

    public void popChunk() throws IOException {
        Chunk chunk = stack.pop();
        chunk.finish();
    }

    public void finish() throws IOException {
        while (!stack.empty()) {
            popChunk();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            finish();
        } finally {
            out.close();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    
    public long getStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    
    public void seek(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    
    private abstract class Chunk {

        
        protected String chunkType;
        
        protected long offset;
        protected boolean finished;

        
        public Chunk(String chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getStreamPosition();
        }

        
        public abstract void finish() throws IOException;

        public abstract boolean isComposite();
    }

    
    private class CompositeChunk extends Chunk {

        
        protected String compositeType;

        
        public CompositeChunk(String compositeType, String chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;

            out.writeLong(0);
            out.writeInt(0);
        }

        
        @Override
        public void finish() throws IOException {
            if (!finished) {
                long size = getStreamPosition() - offset;
                if (size > 0xffffffffL) {
                    throw new IOException("CompositeChunk \"" + chunkType + "\" is too large: " + size);
                }

                long pointer = getStreamPosition();
                seek(offset);

                writeTYPE(compositeType);
                writeULONG(size - 8);
                writeTYPE(chunkType);
                seek(pointer);
                if (size % 2 == 1) {
                    out.writeByte(0);
                }
                finished = true;
            }
        }

        @Override
        public boolean isComposite() {
            return true;
        }
    }

    
    private class DataChunk extends Chunk {

        
        public DataChunk(String name) throws IOException {
            super(name);
            out.writeLong(0);
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long size = getStreamPosition() - offset;
                if (size > 0xffffffffL) {
                    throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size);
                }

                long pointer = getStreamPosition();
                seek(offset);

                writeTYPE(chunkType);
                writeULONG(size - 8);
                seek(pointer);
                if (size % 2 == 1) {
                    out.writeByte(0);
                }
                finished = true;
            }
        }

        @Override
        public boolean isComposite() {
            return false;
        }
    }

    public void writeLONG(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 24);
        writeBuffer[1] = (byte) (v >>> 16);
        writeBuffer[2] = (byte) (v >>> 8);
        writeBuffer[3] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 4);
    }

    public void writeULONG(long v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 24);
        writeBuffer[1] = (byte) (v >>> 16);
        writeBuffer[2] = (byte) (v >>> 8);
        writeBuffer[3] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 4);
    }

    public void writeWORD(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 8);
        writeBuffer[1] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 2);
    }

    public void writeUWORD(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 8);
        writeBuffer[1] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 2);
    }

    public void writeUBYTE(int v) throws IOException {
        out.write(v);
    }

    
    public void writeTYPE(String s) throws IOException {
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            out.write(s.getBytes("ASCII"), 0, 4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    
    public void writeByteRun1(byte[] data) throws IOException {
        writeByteRun1(data, 0, data.length);
    }

    public void writeByteRun1(byte[] data, int offset, int length) throws IOException {
        int end = offset + length;


        int literalOffset = offset;
        int i;
        for (i = offset; i < end; i++) {

            byte b = data[i];


            int repeatCount = i + 1;
            for (; repeatCount < end; repeatCount++) {
                if (data[repeatCount] != b) {
                    break;
                }
            }
            repeatCount = repeatCount - i;

            if (repeatCount == 1) {

                if (i - literalOffset > 127) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                    literalOffset = i;
                }



            } else if (repeatCount == 2
                    && literalOffset < i && i - literalOffset < 127) {
                i++;
            } else {

                if (literalOffset < i) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                }

                i += repeatCount - 1;
                literalOffset = i + 1;


                for (; repeatCount > 128; repeatCount -= 128) {
                    write(-127);
                    write(b);
                }
                write(-repeatCount + 1);
                write(b);
            }
        }


        if (literalOffset < end) {
            write(i - literalOffset - 1);
            write(data, literalOffset, i - literalOffset);
        }
    }
}
