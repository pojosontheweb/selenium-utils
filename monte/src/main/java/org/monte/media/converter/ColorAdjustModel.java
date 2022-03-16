
package org.monte.media.converter;

import java.beans.PropertyChangeListener;


public interface ColorAdjustModel {

    public final static String WHITE_POINT_PROPERTY = "whitePoint";

    public final static String BLACK_POINT_PROPERTY = "blackPoint";

    public final static String MID_POINT_PROPERTY = "midPoint";


    public final static String EXPOSURE_PROPERTY = "exposure";

    public final static String BRIGHTNESS_PROPERTY = "brightness";

    public final static String CONTRAST_PROPERTY = "contrast";

    public final static String SATURATION_PROPERTY = "saturation";


    public final static String DEFINITION_PROPERTY = "definition";

    public final static String HIGHLIGHTS_PROPERTY = "highlights";

    public final static String SHADOWS_PROPERTY = "shadows";


    public final static String SHARPNESS_PROPERTY = "sharpness";

    public final static String DENOISE_PROPERTY = "denoise";


    public final static String TEMPERATURE_PROPERTY = "temperature";

    public final static String TINT_PROPERTY = "tint";


    public final static String WHITE_BALANCE_QM_PROPERTY = "whiteBalanceQM";
    public final static String WHITE_BALANCE_QM_ENABLED_PROPERTY = "whiteBalanceQMEnabled";
    public final static String WHITE_BALANCE_TT_ENABLED_PROPERTY = "whiteBalanceTTEnabled";

    public float getWhitePoint();

    public void setWhitePoint(float newValue);

    public float getBlackPoint();

    public void setBlackPoint(float newValue);

    public float getMidPoint();

    public void setMidPoint(float newValue);

    public float getContrast();

    public void setContrast(float newValue);

    public float getDefinition();

    public void setDefinition(float newValue);

    public float getDenoise();

    public void setDenoise(float newValue);

    public float getBrightness();

    public void setBrightness(float newValue);

    public float getExposure();

    public void setExposure(float newValue);

    public float getHighlights();

    public void setHighlights(float newValue);

    public float getSaturation();

    public void setSaturation(float newValue);

    public float getShadows();

    public void setShadows(float newValue);

    public float getSharpness();

    public void setSharpness(float newValue);

    public float getTemperature();

    public void setTemperature(float newValue);

    public float getTint();

    public void setTint(float newValue);

    public void setWhiteBalanceTTEnabled(boolean newValue);
    public boolean isWhiteBalanceTTEnabled();
    public void setWhiteBalanceQMEnabled(boolean newValue);
    public boolean isWhiteBalanceQMEnabled();
    public void setWhiteBalanceQM(float[] newValue);
    public float[] getWhiteBalanceQM();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);


    public void reset();

    public void setTo(ColorAdjustModel that);


    public boolean isIdentity();
}
