
package org.monte.media.anim;

import org.monte.media.image.BitmapImage;


public class ANIMDeltaFrame
        extends ANIMFrame {

    private int leftBound, topBound, rightBound, bottomBound;
    private final static int
            ENCODING_BYTE_VERTICAL = 5,
            ENCODING_VERTICAL_7_SHORT = 6,
            ENCODING_VERTICAL_7_LONG = 7,
            ENCODING_VERTICAL_8_SHORT = 8,
            ENCODING_VERTICAL_8_LONG = 9,
            ENCODING_J = 74;
    public final static int
            OP_Direct = 0,
            OP_XOR = 1,
            OP_LongDelta = 2,
            OP_ShortDelta = 3,
            OP_GeneralDelta = 4,
            OP_ByteVertical = 5,
            OP_StereoDelta = 6,
            OP_Vertical7 = 7,
            OP_Vertical8 = 8,
            OP_J = 74;

    private boolean isWarningPrinted = false;

    public ANIMDeltaFrame() {
    }

    private int getEncoding() {
        switch (getOperation()) {
            case OP_Direct:
                throw new InternalError("Key Frames not yet supported (Anim Op0)");
            case OP_ByteVertical:
                if (getBits() == BIT_XOR) {

                } else if ((getBits() & BadBitsOP_ByteVertical) != 0) {
                    throw new InternalError("Unknown Bits for Anim Op5 in ANHD; Bits:" + getBits());
                }
                return ENCODING_BYTE_VERTICAL;
            case OP_Vertical7:
                if ((getBits() & BIT_LongData) == 0) {
                    return ENCODING_VERTICAL_7_SHORT;
                } else {
                    return ENCODING_VERTICAL_7_LONG;
                }
            case OP_Vertical8:
                if ((getBits() & BIT_LongData) == 0) {
                    return ENCODING_VERTICAL_8_SHORT;
                } else {
                    return ENCODING_VERTICAL_8_LONG;
                }
            case OP_J:
                return ENCODING_J;
            default:
                throw new InternalError("ANIM Op" + getOperation() + " not supported.");
        }
    }

    @Override
    public void decode(BitmapImage bitmap, ANIMMovieTrack track) {
        switch (getEncoding()) {
            case ENCODING_BYTE_VERTICAL:
                decodeByteVertical(bitmap, track);
                break;
            case ENCODING_VERTICAL_7_SHORT:
                decodeVertical7Short(bitmap, track);
                break;
            case ENCODING_VERTICAL_7_LONG:
                decodeVertical7Long(bitmap, track);
                break;
            case ENCODING_VERTICAL_8_SHORT:
                decodeVertical8Short(bitmap, track);
                break;
            case ENCODING_VERTICAL_8_LONG:
                decodeVertical8Long(bitmap, track);
                break;
            case ENCODING_J:
                decodeJ(bitmap, track);
                break;
            default:
                throw new InternalError("Unsupported encoding." + getEncoding());
        }
    }

    private void decodeByteVertical(BitmapImage bitmap, ANIMMovieTrack track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight();
        bottomBound = 0;
        int height = track.getHeight();
        boolean isXOR = getBits() == BIT_XOR;


        for (int i = 0,n=track.getNbPlanes();i<n; ++i) {


            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);
try {
            if (iOp > 0) {

                for (columns = 0; columns < widthInBytes; ++columns) {


                    iPl = columns + i * widthInBytes;
                    opCount = data[iOp++] & 0xff;

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = data[iOp];
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        if (isXOR) {
                            for (; opCount > 0; opCount--) {
                                opCode = data[iOp++];
                                if (opCode > 0) {
                                    iPl += opCode * interleave;
                                } else if (opCode < 0) {
                                    opCode &= 0x7f;
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] ^= data[iOp++];
                                        iPl += interleave;
                                    }
                                } else {
                                    opCode = data[iOp++] & 0xff;
                                    if (opCode == 0) {
                                        return;
                                    }
                                    copyByte = data[iOp++];
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] ^= copyByte;
                                        iPl += interleave;
                                    }
                                }
                            }
                        } else {
                            for (; opCount > 0; opCount--) {
                                opCode = data[iOp++];
                                if (opCode > 0) {
                                    iPl += opCode * interleave;
                                } else if (opCode < 0) {
                                    opCode &= 0x7f;
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = data[iOp++];
                                        iPl += interleave;
                                    }
                                } else {
                                    opCode = data[iOp++] & 0xff;
                                    if (opCode == 0) {
                                        return;
                                    }
                                    copyByte = data[iOp++];
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = copyByte;
                                        iPl += interleave;
                                    }
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
        }

        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 8;
        }
    }

    private void decodeVertical8Short(BitmapImage bitmap, ANIMMovieTrack track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;


        for (int i = 0; i < track.getNbPlanes(); i++) {


            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            if (iOp > 0) {

                for (columns = 0; columns < widthInBytes; columns += 2) {


                    iPl = columns + i * widthInBytes;
                    opCount = ((data[iOp++] & 0xff) << 8) | (data[iOp++] & 0xff);

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = (data[iOp] << 8) | (data[iOp + 1] & 0xff);
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = (data[iOp++] << 8) | (data[iOp++] & 0xff);
                            if (opCode > 0) {
                                iPl += opCode * interleave;
                            } else if (opCode < 0) {
                                opCode &= 0x7fff;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iOp++];
                                    planeBytes[iPl + 1] = data[iOp++];
                                    iPl += interleave;
                                }
                            } else {
                                opCode = ((data[iOp++] << 8) | (data[iOp++] & 0xff)) & 0xffff;
                                if (opCode == 0) {
                                    return;
                                }
                                copyByte1 = data[iOp++];
                                copyByte2 = data[iOp++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 16;
        }
    }

    private void decodeVertical8Long(BitmapImage bitmap, ANIMMovieTrack track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        byte copyByte3 = 0;
        byte copyByte4 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;


        for (int i = 0; i < track.getNbPlanes(); i++) {


            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            if (iOp > 0) {

                for (columns = 0; columns < widthInBytes; columns += 4) {


                    iPl = columns + i * widthInBytes;
                    opCount = ((data[iOp++] & 0xff) << 24)
                            + ((data[iOp++] & 0xff) << 16)
                            + ((data[iOp++] & 0xff) << 8)
                            + (data[iOp++] & 0xff);

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = ((data[iOp] & 0xff) << 24)
                                + ((data[iOp + 1] & 0xff) << 16)
                                + ((data[iOp + 2] & 0xff) << 8)
                                + (data[iOp + 3] & 0xff);
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = ((data[iOp++] & 0xff) << 24)
                                    + ((data[iOp++] & 0xff) << 16)
                                    + ((data[iOp++] & 0xff) << 8)
                                    + (data[iOp++] & 0xff);
                            if (opCode > 0) {
                                iPl += opCode * interleave;
                            } else if (opCode < 0) {
                                opCode &= 0x7fffffff;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iOp++];
                                    planeBytes[iPl + 1] = data[iOp++];
                                    planeBytes[iPl + 2] = data[iOp++];
                                    planeBytes[iPl + 3] = data[iOp++];
                                    iPl += interleave;
                                }
                            } else {
                                opCode = ((data[iOp++] & 0xff) << 24)
                                        + ((data[iOp++] & 0xff) << 16)
                                        + ((data[iOp++] & 0xff) << 8)
                                        + (data[iOp++] & 0xff);
                                if (opCode == 0) {
                                    return;
                                }
                                copyByte1 = data[iOp++];
                                copyByte2 = data[iOp++];
                                copyByte3 = data[iOp++];
                                copyByte4 = data[iOp++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    planeBytes[iPl + 2] = copyByte3;
                                    planeBytes[iPl + 3] = copyByte4;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 32;
        }
    }

    private void decodeVertical7Short(BitmapImage bitmap, ANIMMovieTrack track) {
        int columns = 0;
        int iOp = 0;
        int iData = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = bitmap.getScanlineStride();
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        for (int i = 0; i < track.getNbPlanes(); i++) {
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            iData = ((data[i * 4 + 32] & 0xff) << 24)
                    + ((data[i * 4 + 33] & 0xff) << 16)
                    + ((data[i * 4 + 34] & 0xff) << 8)
                    + (data[i * 4 + 35] & 0xff);

            if (iOp > 0) {
                for (columns = 0; columns < widthInBytes; columns += 2) {
                    iPl = columns + i * widthInBytes;
                    opCount = data[iOp++] & 0xff;

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = data[iOp];
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = data[iOp++];
                            if (opCode > 0) {
                                iPl += opCode * interleave;
                            } else if (opCode < 0) {
                                opCode &= 0x7f;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iData++];
                                    planeBytes[iPl + 1] = data[iData++];
                                    iPl += interleave;
                                }
                            } else {
                                opCode = data[iOp++] & 0xff;
                                if (opCode == 0) {
                                    return;
                                }
                                copyByte1 = data[iData++];
                                copyByte2 = data[iData++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 32;
        }
    }

    private void decodeVertical7Long(BitmapImage bitmap, ANIMMovieTrack track) {
        int columns = 0;
        int iOp = 0;
        int iData = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        byte copyByte3 = 0;
        byte copyByte4 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        for (int i = 0; i < track.getNbPlanes(); i++) {
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            iData = ((data[i * 4 + 32] & 0xff) << 24)
                    + ((data[i * 4 + 33] & 0xff) << 16)
                    + ((data[i * 4 + 34] & 0xff) << 8)
                    + (data[i * 4 + 35] & 0xff);

            if (iOp > 0) {
                for (columns = 0; columns < widthInBytes; columns += 4) {
                    try {
                        iPl = columns + i * widthInBytes;
                        opCount = data[iOp++] & 0xff;

                        if (opCount > 0) {
                            if (columns < leftBound) {
                                leftBound = columns;
                            }
                            if (columns > rightBound) {
                                rightBound = columns;
                            }
                            opCode = data[iOp];
                            if (opCode <= 0) {
                                topBound = 0;
                            } else {
                                if (opCode < topBound) {
                                    topBound = opCode;
                                }
                            }

                            for (; opCount > 0; opCount--) {
                                opCode = data[iOp++];
                                if (opCode > 0) {
                                    iPl += opCode * interleave;
                                } else if (opCode < 0) {
                                    opCode &= 0x7f;
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = data[iData++];
                                        planeBytes[iPl + 1] = data[iData++];
                                        planeBytes[iPl + 2] = data[iData++];
                                        planeBytes[iPl + 3] = data[iData++];
                                        iPl += interleave;
                                    }
                                } else {
                                    opCode = data[iOp++] & 0xff;
                                    if (opCode == 0) {
                                        return;
                                    }
                                    copyByte1 = data[iData++];
                                    copyByte2 = data[iData++];
                                    copyByte3 = data[iData++];
                                    copyByte4 = data[iData++];
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = copyByte1;
                                        planeBytes[iPl + 1] = copyByte2;
                                        planeBytes[iPl + 2] = copyByte3;
                                        planeBytes[iPl + 3] = copyByte4;
                                        iPl += interleave;
                                    }
                                }
                            }

                            if (opCode <= 0) {
                                int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                                if (bottom > bottomBound) {
                                    bottomBound = bottom;
                                }
                            } else {
                                if (height - opCode > bottomBound) {
                                    bottomBound = height - opCode;
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {


                        if (!isWarningPrinted) {
                            e.printStackTrace();
                            isWarningPrinted = true;
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 64;
        }
    }


    private void decodeJ(BitmapImage bitmap, ANIMMovieTrack track) {

        int nbPlanes = track.getNbPlanes();
        int widthInBytes = bitmap.getBitplaneStride();




        leftBound = track.getWidth() - 1;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;


        int pos = 0;


        byte[] planeBytes = bitmap.getBitmap();


        int changeType;
        try {
            decodingLoop:
            while (pos < data.length) {
                changeType = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                switch (changeType) {
                    case 0:
                        break decodingLoop;

                    case 1: {





                        int uniFlag = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int ySize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int numBlocks = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);


                        for (int b = 0; b < numBlocks; b++) {
                            int offset = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                            leftBound = Math.min(leftBound, (offset % widthInBytes) * 8);
                            rightBound = Math.max(rightBound, (offset % widthInBytes) * 8 + 8);
                            topBound = Math.min(topBound, (offset / widthInBytes));
                            bottomBound = Math.max(bottomBound, (offset / widthInBytes) + ySize);

                            int realOffset = (offset / widthInBytes) * nbPlanes;
                            realOffset *= widthInBytes;
                            realOffset += offset % widthInBytes;

                            if (uniFlag == 1) {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        int dest = z * widthInBytes * ySize
                                                + y * widthInBytes
                                                + realOffset;
                                        planeBytes[dest] ^= data[pos++];
                                    }
                                }
                            } else {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        int dest = z * widthInBytes * ySize
                                                + y * widthInBytes
                                                + realOffset;
                                        planeBytes[dest] = data[pos++];
                                    }
                                }
                            }



                            if (pos % 2 == 1) {
                                pos++;
                            }
                        }
                        break;
                    }
                    case 2: {






                        int uniFlag = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int ySize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int xSize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int numBlocks = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);


                        for (int b = 0; b < numBlocks; b++) {
                            int offset = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                            leftBound = Math.min(leftBound, (offset % widthInBytes) * 8);
                            rightBound = Math.max(rightBound, (offset % widthInBytes + xSize) * 8 + 8);
                            topBound = Math.min(topBound, (offset / widthInBytes));
                            bottomBound = Math.max(bottomBound, (offset / widthInBytes) + ySize);

                            int realOffset = (offset / widthInBytes) * nbPlanes;
                            realOffset *= widthInBytes;
                            realOffset += offset % widthInBytes;

                            if (uniFlag == 1) {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        for (int x = 0; x < xSize; x++) {
                                            int dest = z * widthInBytes * ySize
                                                    + y * widthInBytes
                                                    + realOffset + x;
                                            planeBytes[dest] ^= data[pos++];
                                        }
                                    }
                                }
                            } else {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        for (int x = 0; x < xSize; x++) {
                                            int dest = z * widthInBytes * ySize
                                                    + y * widthInBytes
                                                    + realOffset + x;
                                            planeBytes[dest] = data[pos++];
                                        }
                                    }
                                }
                            }



                            if (pos % 2 == 1) {
                                pos++;
                            }
                        }
                        break;
                    }
                    default:
                        System.out.println("Unsupported changeType in 'J' delta frame:" + changeType);
                        break decodingLoop;

                }
            }
        } catch (IndexOutOfBoundsException e) {


            if (!isWarningPrinted) {
                e.printStackTrace();
                isWarningPrinted = true;
            }
        }

    }

    @Override
    public int getTopBound(ANIMMovieTrack track) {
        return topBound;
    }

    @Override
    public int getBottomBound(ANIMMovieTrack track) {
        return bottomBound;
    }

    @Override
    public int getLeftBound(ANIMMovieTrack track) {
        return leftBound;
    }

    @Override
    public int getRightBound(ANIMMovieTrack track) {
        return rightBound;
    }


    @Override
    public boolean isBidirectional() {
        switch (getOperation()) {
            case OP_Direct:
                return true;
            case OP_ByteVertical:
                if (getBits() == BIT_XOR) {
                    return true;
                }
                break;
            case OP_J:
                return true;
            default:
                break;
        }
        return false;
    }
}
