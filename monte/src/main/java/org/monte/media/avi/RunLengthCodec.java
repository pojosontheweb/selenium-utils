
package org.monte.media.avi;

import org.monte.media.Format;
import org.monte.media.io.ByteArrayImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import static java.lang.Math.*;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

public class RunLengthCodec extends AbstractVideoCodec {

    private byte[] previousPixels;
    private int frameCounter;

    public RunLengthCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                            EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_RLE, DataClassKey, byte[].class,
                            FixedFrameRateKey, true, DepthKey,8), //
                });
    }

    @Override
    public void reset() {
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat==null) return CODEC_FAILED;
        if (outputFormat.get(EncodingKey).equals(ENCODING_AVI_RLE)) {
            return encode(in, out);
        } else {
            return decode(in, out);
        }
    }

    private int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format=outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        ByteArrayImageOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new ByteArrayImageOutputStream((byte[]) out.data);
        } else {
            tmp = new ByteArrayImageOutputStream();
        }

        // Handle sub-image
        Rectangle r;
        int scanlineStride;
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, outputFormat.get(WidthKey), outputFormat.get(HeightKey));
            scanlineStride = outputFormat.get(WidthKey);
            out.header = null;
        }
        int offset = r.x + r.y * scanlineStride;

        boolean isKeyframe = frameCounter== 0
                || frameCounter % outputFormat.get(KeyFrameIntervalKey,outputFormat.get(FrameRateKey).intValue()) == 0;
        frameCounter++;

        try {
            byte[] pixels = getIndexed8(in);
            if (pixels == null) {
                return CODEC_FAILED;
            }
            if (isKeyframe) {
                writeKey8(tmp, pixels, r.width, r.height, offset, scanlineStride);
                out.setFlag(KEYFRAME);
            } else {
                writeDelta8(tmp, pixels, previousPixels, r.width, r.height, offset, scanlineStride);
                out.clearFlag(KEYFRAME);
            }
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            //
            if (previousPixels == null) {
                previousPixels = pixels.clone();
            } else {
                System.arraycopy(pixels, 0, previousPixels, 0, pixels.length);
            }
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }

    private int decode(Buffer in, Buffer out) {
        return CODEC_FAILED;
    }

    public void writeKey8(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride) throws IOException {
        ByteArrayImageOutputStream buf = new ByteArrayImageOutputStream(data.length);
        writeKey8(buf, data, width, height, offset, scanlineStride);
        buf.toOutputStream(out);
    }

    public void writeKey8(ImageOutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        out.write(0);
                        out.write(literalCount); // Literal OP-code
                        out.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                out.write(1); // Repeat OP-code
                                out.write(data[xy - literalCount]);
                            }
                        } else {
                            out.write(0);
                            out.write(literalCount); // Literal OP-code
                            out.write(data, xy - literalCount, literalCount);
                            if (literalCount % 2 == 1) {
                                out.write(0); // pad byte
                            }
                            literalCount = 0;
                        }
                    }
                    out.write(repeatCount); // Repeat OP-code
                    out.write(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        out.write(1); // Repeat OP-code
                        out.write(data[xy - literalCount]);
                    }
                } else {
                    out.write(0);
                    out.write(literalCount);
                    out.write(data, xy - literalCount, literalCount);
                    if (literalCount % 2 == 1) {
                        out.write(0); // pad byte
                    }
                }
                literalCount = 0;
            }

            out.write(0);
            out.write(0x0000);// End of line
        }
        out.write(0);
        out.write(0x0001);// End of bitmap
    }

    public void writeDelta8(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride) throws IOException {
        ByteArrayImageOutputStream buf = new ByteArrayImageOutputStream(data.length);
        writeDelta8(buf, data, prev, width, height, offset, scanlineStride);
        buf.toOutputStream(out);
    }

    public void writeDelta8(ImageOutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                if (verticalOffset == 1 && skipCount == 0) {
                    out.write(0x00);
                    out.write(0x00); // End of line OP-code
                    verticalOffset = 0;
                } else {
                    out.write(0x00);
                    out.write(0x02); // Skip OP-code
                    out.write(min(255, skipCount)); // horizontal offset
                    out.write(min(255, verticalOffset)); // vertical offset
                    skipCount -= min(255, skipCount);
                    verticalOffset -= min(255, verticalOffset);
                }
            }


            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            out.write(1); // Repeat OP-code
                            out.write(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            out.write(0);
                            out.write(literalRun); // Literal OP-code
                            out.write(data, xy - literalCount, literalRun);
                            if (literalRun % 2 == 1) {
                                out.write(0); // pad byte
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            out.write(0);
                            out.write(0x0002); // Skip OP-code
                            out.write(min(255, skipCount));
                            out.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        out.write(repeatCount); // Repeat OP-code
                        out.write(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    out.write(1); // Repeat OP-code
                    out.write(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    out.write(0);
                    out.write(literalRun); // Literal OP-code
                    out.write(data, xy - literalCount, literalRun);
                    if (literalRun % 2 == 1) {
                        out.write(0); // pad byte
                    }
                    literalCount -= literalRun;
                }
            }

            out.write(0);
            out.write(0x0000); // End of line OP-code
        }

        out.write(0);
        out.write(0x0001);// End of bitmap
    }
}
