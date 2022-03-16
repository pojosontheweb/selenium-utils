
package org.monte.media.jpeg;

import org.monte.media.io.ByteArrayImageInputStream;
import javax.imageio.ImageReader;
import org.monte.media.Format;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;


public class JPEGCodec extends AbstractVideoCodec {

    public JPEGCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE),
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_JPEG,
                    CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG,
                    DataClassKey, byte[].class, DepthKey, 24),

                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_MJPG, DataClassKey, byte[].class, DepthKey, 24),
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE),
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_JPEG,
                    CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG,
                    DataClassKey, byte[].class, DepthKey, 24),

                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_MJPG, DataClassKey, byte[].class, DepthKey, 24),
                }
                );
        name = "JPEG Codec";
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat.get(EncodingKey).equals(ENCODING_BUFFERED_IMAGE)) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        BufferedImage image = getBufferedImage(in);
        if (image == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        ByteArrayImageOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new ByteArrayImageOutputStream((byte[]) out.data);
        } else {
            tmp = new ByteArrayImageOutputStream();
        }

        try {
            ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
            ImageWriteParam iwParam = iw.getDefaultWriteParam();
            iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = outputFormat.get(QualityKey, 1f);
            iwParam.setCompressionQuality(quality);
            iw.setOutput(tmp);
            IIOImage img = new IIOImage(image, null, null);
            iw.write(null, img, iwParam);
            iw.dispose();

            out.sampleCount = 1;
            out.setFlag(KEYFRAME);
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        byte[] data = (byte[]) in.data;
        if (data == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        ByteArrayImageInputStream tmp = new ByteArrayImageInputStream(data);

        try {

            ImageReader ir = new MJPGImageReader(new MJPGImageReaderSpi());
            ir.setInput(tmp);
            out.data = ir.read(0);
            ir.dispose();

            out.sampleCount = 1;
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
}
