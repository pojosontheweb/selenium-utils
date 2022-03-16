
package org.monte.media;

import org.monte.media.math.Rational;


public class FormatKeys {
     public static enum MediaType {
        AUDIO,
        VIDEO,
        MIDI,
        TEXT,
        META,
        FILE
    }

    public final static FormatKey<MediaType> MediaTypeKey = new FormatKey<MediaType>("mediaType", MediaType.class);

    public final static FormatKey<String> EncodingKey = new FormatKey<String>("encoding", String.class);


    public final static String MIME_AVI = "video/avi";
    public final static String MIME_QUICKTIME = "video/quicktime";
    public final static String MIME_MP4 = "video/mp4";
    public final static String MIME_JAVA = "Java";
    public final static String MIME_ANIM = "x-iff/anim";
    public final static String MIME_IMAGE_SEQUENCE = "ImageSequence";

    public final static FormatKey<String> MimeTypeKey = new FormatKey<String>("mimeType", String.class);

    public final static FormatKey<Rational> FrameRateKey = new FormatKey<Rational>("frameRate", Rational.class);


    public final static FormatKey<Integer> KeyFrameIntervalKey = new FormatKey<Integer>("keyFrameInterval", Integer.class);
}
