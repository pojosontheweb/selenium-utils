
package org.monte.media.avi;

import org.monte.media.audio.*;
import org.monte.media.Format;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import static org.monte.media.AudioFormatKeys.*;


public class AVIPCMAudioCodec extends PCMAudioCodec {

    private final static HashSet<String> supportedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
               ENCODING_PCM_SIGNED,
                ENCODING_PCM_UNSIGNED, ENCODING_AVI_PCM,}));

   public AVIPCMAudioCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_PCM_SIGNED,
                            MimeTypeKey,MIME_JAVA,
                            SignedKey,true),
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_PCM_UNSIGNED,
                            MimeTypeKey,MIME_JAVA,
                            SignedKey,false),
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_AVI_PCM,
                            MimeTypeKey,MIME_AVI,
                            SignedKey,false,SampleSizeInBitsKey,8),
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_AVI_PCM,
                            MimeTypeKey,MIME_AVI,
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,16),
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_AVI_PCM,
                            MimeTypeKey,MIME_AVI,
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,24),
                    new Format(MediaTypeKey,MediaType.AUDIO,
                            EncodingKey,ENCODING_AVI_PCM,
                            MimeTypeKey,MIME_AVI,
                            ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                            SignedKey,true,SampleSizeInBitsKey,32),
                });
        name="AVI PCM Codec";
    }

}
