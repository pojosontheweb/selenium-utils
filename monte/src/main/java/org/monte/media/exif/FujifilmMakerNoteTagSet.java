
package org.monte.media.exif;

import org.monte.media.tiff.*;
import static org.monte.media.tiff.TIFFTag.*;


public class FujifilmMakerNoteTagSet extends TagSet {

    private static FujifilmMakerNoteTagSet instance;

    public final static TIFFTag ParallaxXShift = new TIFFTag("ParallaxXShift", 0xb211, SRATIONAL_MASK);

    public final static TIFFTag ParallaxYShift = new TIFFTag("ParallaxYShift", 0xb212, SRATIONAL_MASK);

    public final static TIFFTag ConvergenceAngle = new TIFFTag("ConvergenceAngle", 0xb205, SRATIONAL_MASK);
    public final static TIFFTag BaselineLength = new TIFFTag("BaselineLength", 0xb206, RATIONAL_MASK);
    public final static TIFFTag SerialNumber = new TIFFTag("SerialNumber", 0x10, ASCII_MASK);

    private FujifilmMakerNoteTagSet(TIFFTag[] tags) {
        super("FujifilmMakerNote", tags);
    }


    public static FujifilmMakerNoteTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {

                new TIFFTag("Version", 0x0, UNDEFINED_MASK, new ASCIIValueFormatter()),

                SerialNumber,

                new TIFFTag("Quality", 0x1000, ASCII_MASK),

                new TIFFTag("Sharpness", 0x1001, SHORT_MASK, new EnumValueFormatter(
                "soft", 1,
                "soft2", 2,
                "normal", 3,
                "hard", 4,
                "hard2", 5,
                "mediumSoft", 0x82,
                "mediumHard", 0x84,
                "filmSimulationMode", 0x8000,
                "off", 0xffff
                )),

                new TIFFTag("WhiteBalance", 0x1002, SHORT_MASK, new EnumValueFormatter(
                "auto", 0,
                "daylight", 0x100,
                "cloudy", 0x200,
                "daylightColorFluorescence", 0x300,
                "daywhiteColorFluorescence", 0x301,
                "whiteFluorescence", 0x302,
                "fluorescence4", 0x303,
                "fluorescence5", 0x304,
                "incandescence", 0x400,
                "flash", 0x500,
                "customWhiteBalance", 0xf00,
                "custom2", 0xf01,
                "custom3", 0xf02,
                "custom4", 0xf03,
                "custom5", 0xf03
                )),

                new TIFFTag("Color", 0x1003, SHORT_MASK, new EnumValueFormatter(
                "normal", 0,
                "mediumHigh", 0x80,
                "high", 0x100,
                "mediumLow", 0x180,
                "low", 0x200,
                "blackAndWhite", 0x300,
                "filmSimulationMode", 0x8000
                )),

                new TIFFTag("Tone", 0x1004, SHORT_MASK, new EnumValueFormatter(
                "normal", 0,
                "mediumHard", 0x80,
                "hard", 0x100,
                "mediumSoft", 0x180,
                "soft", 0x200,
                "filmSimulationMode", 0x8000
                )),

                new TIFFTag("FlashMode", 0x1010, SHORT_MASK, new EnumValueFormatter(
                "auto", 0,
                "on", 1,
                "off", 2,
                "redEyeReduction", 3
                )),

                new TIFFTag("FlashStrength", 0x1011, SRATIONAL_MASK),

                new TIFFTag("Macro", 0x1020, SHORT_MASK, new EnumValueFormatter(
                "off", 0,
                "on", 1
                )),

                new TIFFTag("FocusMode", 0x1021, SHORT_MASK, new EnumValueFormatter(
                "auto", 0,
                "manual", 1
                )),

                new TIFFTag("PrincipalPoint", 0x1023, SHORT_MASK),


                new TIFFTag("SlowSync", 0x1030, SHORT_MASK, new EnumValueFormatter(
                "off", 0,
                "on", 1
                )),

                new TIFFTag("PictureMode", 0x1031, SHORT_MASK, new EnumValueFormatter(
                "auto", 0,
                "portraitScene", 1,
                "landscapeScene", 2,
                "sportsScene", 4,
                "nightScene", 5,
                "programAE", 6,
                "aperturePriorAE", 256,
                "shutterPriorAE", 512,
                "manualExposure", 768
                )),

                new TIFFTag("Continuous", 0x1100, SHORT_MASK, new EnumValueFormatter(
                "continuous", 0,
                "autoBracketing", 1
                )),

                new TIFFTag("SequenceNumber", 0x1101, SHORT_MASK),

                new TIFFTag("FinePixColor", 0x1210, SHORT_MASK, new EnumValueFormatter(
                "standard", 0,
                "chrome", 16

                )),

                new TIFFTag("BlurWarning", 0x1300, SHORT_MASK, new EnumValueFormatter(
                "good", 0,
                "blurred", 1
                )),

                new TIFFTag("FocusWarning", 0x1301, SHORT_MASK, new EnumValueFormatter(
                "good", 0,
                "outOfFocus", 1
                )),

                new TIFFTag("ExposureWarning", 0x1302, SHORT_MASK, new EnumValueFormatter(
                "good", 0,
                "overExposure", 1
                )),

                new TIFFTag("DynamicRange", 0x1400, SHORT_MASK),

                new TIFFTag("FilmMode", 0x1401, SHORT_MASK),

                new TIFFTag("DynamicRangeSetting", 0x1402, SHORT_MASK),

                new TIFFTag("DevelopmentDynamicRange", 0x1403, SHORT_MASK),

                new TIFFTag("MinFocalLength", 0x1404, RATIONAL_MASK),

                new TIFFTag("MaxFocalLength", 0x1405, RATIONAL_MASK),

                new TIFFTag("MaxApertureAtMinFocal", 0x1406, RATIONAL_MASK),

                new TIFFTag("MaxApertureAtMaxFocal", 0x1407, RATIONAL_MASK),

                new TIFFTag("FileSource", 0x8000, ASCII_MASK),

                new TIFFTag("OrderNumber", 0x8002, LONG_MASK),

                new TIFFTag("FrameNumber", 0x8003, SHORT_MASK),
                ParallaxXShift, ParallaxYShift,

                new TIFFTag("MPIndividualImageNumber", 0xb101, SHORT_MASK),
                new TIFFTag("BaseViewpointNumber", 0xb204, SHORT_MASK),
                ConvergenceAngle,
                BaselineLength,};
            instance = new FujifilmMakerNoteTagSet(tags);

        }
        return instance;
    }
}
