
package org.monte.media.exif;

import org.monte.media.tiff.TagSet;
import org.monte.media.tiff.EnumValueFormatter;
import org.monte.media.tiff.*;
import static org.monte.media.tiff.TIFFTag.*;


public class MPEntryTagSet extends TagSet {

    public final static int TAG_DependentParentImageFlag = -1;
    public final static int TAG_DependentChildImageFlag = -2;
    public final static int TAG_RepresentativeImageFlag = -3;
    public final static int TAG_ImageDataFormat = -4;
    public final static int TAG_MPTypeCode = -5;
    public final static int TAG_IndividualImageSize = -6;
    public final static int TAG_IndividualImageDataOffset = -7;
    public final static int TAG_DependentImage1EntryNumber = -8;
    public final static int TAG_DependentImage2EntryNumber = -9;


    public final static TIFFTag IndividualImageDataOffset = new TIFFTag("IndividualImageDataOffset", TAG_IndividualImageDataOffset, LONG_MASK);

    public final static TIFFTag IndividualImageSize = new TIFFTag("IndividualImageSize", TAG_IndividualImageSize, LONG_MASK);

    private static MPEntryTagSet instance;

    private MPEntryTagSet(TIFFTag[] tags) {
        super("MPEntry", tags);
    }

    public static TIFFTag get(int tagNumber) {
        return getInstance().getTag(tagNumber);
    }


    public static MPEntryTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {
                new TIFFTag("IndividualImageUniqueIDList", 0xb003, SHORT_MASK),
                new TIFFTag("TotalNumberOfCapturedFrames", 0xb004, SHORT_MASK),
                new TIFFTag("MPIndividualImageNumber", 0xb101, LONG_MASK),
                new TIFFTag("PanOrientation", 0xb201, LONG_MASK),
                new TIFFTag("PanOverlap_H", 0xb202, RATIONAL_MASK),
                new TIFFTag("PanOverlap_V", 0xb203, RATIONAL_MASK),
                new TIFFTag("BaseViewpointNum", 0xb204, LONG_MASK),
                new TIFFTag("ConvergenceAngle", 0xb205, SRATIONAL_MASK),
                new TIFFTag("BaselineLength", 0xb206, RATIONAL_MASK),
                new TIFFTag("VerticalDivergence", 0xb207, SRATIONAL_MASK),
                new TIFFTag("AxisDistance_X", 0xb208, SRATIONAL_MASK),
                new TIFFTag("AxisDistance_Y", 0xb209, SRATIONAL_MASK),
                new TIFFTag("AxisDistance_Z", 0xb20a, SRATIONAL_MASK),
                new TIFFTag("YawAngle", 0xb20b, SRATIONAL_MASK),
                new TIFFTag("PitchAngle", 0xb20c, SRATIONAL_MASK),
                new TIFFTag("RollAngle", 0xb20d, SRATIONAL_MASK),




                new TIFFTag("DependentParentImageFlag", TAG_DependentParentImageFlag, BYTE_MASK, new EnumValueFormatter(
                "notAParent", 0,
                "isParent", 1
                )),

                new TIFFTag("DependentChildImageFlag", TAG_DependentChildImageFlag, BYTE_MASK, new EnumValueFormatter(
                "notAChild", 0,
                "isChild", 1
                )),

                new TIFFTag("RepresentativeImageFlag", TAG_RepresentativeImageFlag, BYTE_MASK, new EnumValueFormatter(
                "notRepresentative", 0,
                "isRepresentative", 1
                )),

                new TIFFTag("ImageDataFormat", TAG_ImageDataFormat, BYTE_MASK, new EnumValueFormatter(
                "JPEG", 0
                )),

                new TIFFTag("MPTypeCode", TAG_MPTypeCode, LONG_MASK, new EnumValueFormatter(
                "BaselineMPPrimaryImage", 0x30000,
                "LargeThumbnailVGA", 0x10001,
                "LargeThumbnailFullHD", 0x10002,
                "MultiFramePanoramaImage", 0x20001,
                "MultiFrameDisparityImage", 0x20002,
                "MultiFrameMultiAngleImage", 0x20003,
                "Undefined", 0x0

                )),

                IndividualImageSize,
                IndividualImageDataOffset,

                new TIFFTag("DependentImage1EntryNumber", TAG_DependentImage1EntryNumber, SHORT_MASK),
                new TIFFTag("DependentImage2EntryNumber", TAG_DependentImage2EntryNumber, SHORT_MASK),

            };
            instance = new MPEntryTagSet(tags);

        }
        return instance;
    }
}
