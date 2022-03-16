
package org.monte.media.quicktime;

import org.monte.media.audio.*;
import org.monte.media.Format;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;


public class QuickTimePCMAudioCodec extends PCMAudioCodec {

    private final static HashSet<String> signedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
                ENCODING_PCM_SIGNED, ENCODING_QUICKTIME_TWOS_PCM, ENCODING_QUICKTIME_SOWT_PCM,
                ENCODING_QUICKTIME_IN24_PCM, ENCODING_QUICKTIME_IN32_PCM,}));
    private final static HashSet<String> unsignedEncodings = new HashSet<String>(
            Arrays.asList(new String[]{
                ENCODING_PCM_UNSIGNED, ENCODING_QUICKTIME_RAW_PCM}));

    public QuickTimePCMAudioCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.AUDIO,
                    MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_PCM_SIGNED,
                    SignedKey, true),
                    new Format(MediaTypeKey, MediaType.AUDIO,
                    MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_PCM_UNSIGNED,
                    SignedKey, false),


                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_RAW_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    SignedKey, false, SampleSizeInBitsKey, 8),


                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_SOWT_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.LITTLE_ENDIAN,
                    SignedKey, true, SampleSizeInBitsKey, 8),


                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.BIG_ENDIAN,
                    SignedKey, true, SampleSizeInBitsKey, 8),


                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_SOWT_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.LITTLE_ENDIAN,
                    SignedKey, true,
                    SampleSizeInBitsKey, 16),


                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.BIG_ENDIAN,
                    SignedKey, true,
                    SampleSizeInBitsKey, 16),

                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_IN24_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.BIG_ENDIAN,
                    SignedKey, true, SampleSizeInBitsKey, 24),

                    new Format(MediaTypeKey, MediaType.AUDIO,
                    EncodingKey, ENCODING_QUICKTIME_IN32_PCM,
                    MimeTypeKey, MIME_QUICKTIME,
                    ByteOrderKey, ByteOrder.BIG_ENDIAN,
                    SignedKey, true, SampleSizeInBitsKey, 32),
                });
        name="QuickTime PCM Codec";
    }
}
