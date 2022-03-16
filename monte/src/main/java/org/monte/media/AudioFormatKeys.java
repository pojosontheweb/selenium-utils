
package org.monte.media;

import org.monte.media.math.Rational;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;


public class AudioFormatKeys extends FormatKeys {


    
    public static final String ENCODING_PCM_SIGNED = Encoding.PCM_SIGNED.toString();
    
    public static final String ENCODING_PCM_UNSIGNED = Encoding.PCM_UNSIGNED.toString();
    
    public static final String ENCODING_ULAW = Encoding.ULAW.toString();
    
    public static final String ENCODING_ALAW = Encoding.ALAW.toString();
    
    public static final String ENCODING_AVI_PCM = "\u0000\u0000\u0000\u0001";
    
    public static final String ENCODING_QUICKTIME_TWOS_PCM = "twos";
    
    public static final String ENCODING_QUICKTIME_SOWT_PCM = "sowt";
    
    public static final String ENCODING_QUICKTIME_IN24_PCM = "in24";
    
    public static final String ENCODING_QUICKTIME_IN32_PCM = "in32";
    
    public static final String ENCODING_QUICKTIME_RAW_PCM = "raw ";
    
    public static final String ENCODING_MP3 = "MP3";
    
    public final static FormatKey<Integer> SampleSizeInBitsKey = new FormatKey<Integer>("sampleSizeInBits", Integer.class);
    
    public final static FormatKey<Integer> ChannelsKey = new FormatKey<Integer>("channels", Integer.class);
    
    public final static FormatKey<Integer> FrameSizeKey = new FormatKey<Integer>("frameSize", Integer.class);
    
    public final static FormatKey<ByteOrder> ByteOrderKey = new FormatKey<ByteOrder>("byteOrder", ByteOrder.class);
    
    public final static FormatKey<Rational> SampleRateKey = new FormatKey<Rational>("sampleRate", Rational.class);
    
    public final static FormatKey<Boolean> SignedKey = new FormatKey<Boolean>("signed", Boolean.class);
    
    public final static FormatKey<Boolean> SilenceBugKey = new FormatKey<Boolean>("silenceBug", Boolean.class);

    public static Format fromAudioFormat(AudioFormat fmt) {
        return new Format(
                MediaTypeKey, MediaType.AUDIO,
                EncodingKey, fmt.getEncoding().toString(),
                SampleRateKey, Rational.valueOf(fmt.getSampleRate()),
                SampleSizeInBitsKey, fmt.getSampleSizeInBits(),
                ChannelsKey, fmt.getChannels(),
                FrameSizeKey, fmt.getFrameSize(),
                FrameRateKey, Rational.valueOf(fmt.getFrameRate()),
                ByteOrderKey, fmt.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN,
                SignedKey, Encoding.PCM_SIGNED.equals(fmt.getEncoding())

                );
    }

    public static AudioFormat toAudioFormat(Format fmt) {

        return new AudioFormat(
                !fmt.containsKey(SignedKey) || fmt.get(SignedKey) ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED,
                fmt.get(SampleRateKey).floatValue(),
                fmt.get(SampleSizeInBitsKey, 16),
                fmt.get(ChannelsKey, 1),
                fmt.containsKey(FrameSizeKey) ? fmt.get(FrameSizeKey) : (fmt.get(SampleSizeInBitsKey, 16) + 7) / 8 * fmt.get(ChannelsKey, 1),
                fmt.containsKey(FrameRateKey) ? fmt.get(FrameRateKey).floatValue() : fmt.get(SampleRateKey).floatValue(),
                fmt.containsKey(ByteOrderKey) ? fmt.get(ByteOrderKey) == ByteOrder.BIG_ENDIAN : true);
    }
}
