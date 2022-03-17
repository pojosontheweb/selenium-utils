
package org.monte.media;

import org.monte.media.math.Rational;


public class VideoFormatKeys extends FormatKeys {

    public static final String ENCODING_BUFFERED_IMAGE = "image";
    
    public static final String ENCODING_QUICKTIME_CINEPAK = "cvid";
    public static final String COMPRESSOR_NAME_QUICKTIME_CINEPAK = "Cinepak";
    
    public static final String ENCODING_QUICKTIME_JPEG = "jpeg";
    public static final String COMPRESSOR_NAME_QUICKTIME_JPEG = "Photo - JPEG";
    
    public static final String ENCODING_QUICKTIME_PNG = "png ";
    public static final String COMPRESSOR_NAME_QUICKTIME_PNG = "PNG";
    
    public static final String ENCODING_QUICKTIME_ANIMATION = "rle ";
    public static final String COMPRESSOR_NAME_QUICKTIME_ANIMATION = "Animation";
    
    public static final String ENCODING_QUICKTIME_RAW = "raw ";
    public static final String COMPRESSOR_NAME_QUICKTIME_RAW = "NONE";

    
    public static final String ENCODING_AVI_DIB = "DIB ";
    
    public static final String ENCODING_AVI_RLE = "RLE ";
    
    public static final String ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE = "tscc";
    public static final String COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE = "Techsmith Screen Capture";
    
    public static final String ENCODING_AVI_DOSBOX_SCREEN_CAPTURE = "ZMBV";
    
    public static final String ENCODING_AVI_MJPG = "MJPG";
    
    public static final String ENCODING_AVI_PNG = "png ";
    
    public static final String ENCODING_BITMAP_IMAGE = "ILBM";



    
    public final static FormatKey<Integer> WidthKey = new FormatKey<Integer>("dimX","width", Integer.class);
    
    public final static FormatKey<Integer> HeightKey = new FormatKey<Integer>("dimY","height", Integer.class);
    
    public final static FormatKey<Integer> DepthKey = new FormatKey<Integer>("dimZ","depth", Integer.class);
    
    public final static FormatKey<Class> DataClassKey = new FormatKey<Class>("dataClass", Class.class);
    
    public final static FormatKey<String> CompressorNameKey = new FormatKey<String>("compressorName", "compressorName",String.class, true);
    
    public final static FormatKey<Rational> PixelAspectRatioKey = new FormatKey<Rational>("pixelAspectRatio", Rational.class);
    
    public final static FormatKey<Boolean> FixedFrameRateKey = new FormatKey<Boolean>("fixedFrameRate", Boolean.class);
    
    public final static FormatKey<Boolean> InterlaceKey = new FormatKey<Boolean>("interlace", Boolean.class);
    
    public final static FormatKey<Float> QualityKey = new FormatKey<Float>("quality", Float.class);
}
