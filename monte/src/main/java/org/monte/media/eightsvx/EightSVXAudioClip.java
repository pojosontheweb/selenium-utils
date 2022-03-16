
package org.monte.media.eightsvx;

import java.lang.Object;
import java.io.*;

import java.applet.AudioClip;
import sun.applet.AppletAudioClip;
import java.lang.reflect.*;


public class EightSVXAudioClip
implements LoopableAudioClip {

    private String name_ = "";
    private String author_ = "";
    private String copyright_ = "";
    private String remark_ = "";
    private byte[] body_;

    private long
    oneShotHiSamples_,
    repeatHiSamples_,
    samplesPerHiCycle_;
    private int
    sampleRate_,
    ctOctave_;

    public final static int S_CMP_NONE =  0;
    public final static int S_CMP_FIB_DELTA = 1;
    private int sCompression_;


    private final static double UNITY = 0x10000;
    private int volume_;



    private LoopableAudioClip cachedAudioClip_;
    private int cachedSampleRate_;

    public final static int RIGHT=4, LEFT=2, STEREO=6;
    private int sampleType_;

    private static Boolean javaxAudioIsPresent;




    protected void setName(String value) { name_ = value; }
    protected String getName() { return name_; }

    protected void setAuthor(String value) {author_ = value; }
    protected String getAuthor() { return author_; }

    protected void setCopyright(String value) { copyright_ = value; }
    protected String getCopyright() { return copyright_; }

    protected void setRemark(String value) { remark_ = value; }
    protected String getRemark() { return remark_; }

    public void set8SVXBody(byte[] value) {
        body_ = value;
        cachedAudioClip_ = null;

    }
    public byte[] get8SVXBody() { return body_; }

    public void setOneShotHiSamples(long value) { oneShotHiSamples_ = value; }
    public void setRepeatHiSamples(long value) { repeatHiSamples_ = value; }
    public void setSamplesPerHiCycle(long value) { samplesPerHiCycle_ = value; }
    public void setSampleType(int value) { sampleType_ = value; }

    public void setSampleRate(int value) { sampleRate_ = value; }
    public void setCtOctave(int value) { ctOctave_ = value; }
    public void setSCompression(int value) { sCompression_ = value; }
    public void setVolume(int value) { volume_ = value; }

    public long getOneShotHiSamples() { return oneShotHiSamples_; }
    public long getRepeatHiSamples() { return repeatHiSamples_; }
    public long getSamplesPerHiCycle() { return samplesPerHiCycle_; }
    public long getSampleType() { return sampleType_; }

    public int getSampleRate() { return sampleRate_; }
    public int getCtOctave() { return ctOctave_; }
    public int getVolume() { return volume_; }
    public int getSCompression() { return sCompression_; }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getName().length() == 0) buf.append("<unnamed>");
        else buf.append(getName());
        if (getAuthor().length() != 0) {
            buf.append(", ");
            buf.append(getAuthor());
        }
        if (getCopyright().length() != 0) {
            buf.append(", � ");
            buf.append(getCopyright());
        }
        buf.append(' ');
        buf.append(Integer.toString(getSampleRate()));
        buf.append(" Hz");
        return buf.toString();
    }

    public LoopableAudioClip createAudioClip() {
        return createAudioClip(getSampleRate(), volume_, 0f);
    }

    public LoopableAudioClip createAudioClip(int sampleRate, int volume, float pan) {
        if (javaxAudioIsPresent == null || javaxAudioIsPresent == Boolean.TRUE) {
            try {
                LoopableAudioClip clip = createJDK13AudioClip(sampleRate, volume, pan);
                javaxAudioIsPresent = Boolean.TRUE;
                return clip;
            } catch (Throwable t) {
                t.printStackTrace();
                javaxAudioIsPresent = Boolean.FALSE;
            }
        }
        return createJDK10AudioClip(sampleRate);
    }

    public LoopableAudioClip createJDK13AudioClip(int sampleRate , int volume, float pan) {
        AudioClip audioClip = null;

        if (sCompression_ == S_CMP_FIB_DELTA) {
            body_ = unpackFibonacciDeltaCompression(body_);
            sCompression_ = S_CMP_NONE;
        }


        if (sampleType_ == STEREO) {
            double volumeCorrection = computeStereoVolumeCorrection(body_);
            body_ = linear8StereoToMono(body_,volumeCorrection);
            sampleType_ = LEFT;
        }

        byte[] samples = get8SVXBody();
        if (samples.length > 1000000) {
            return new JDK13LongAudioClip(samples, sampleRate, volume, pan);
        } else {
            return new JDK13ShortAudioClip(samples, sampleRate, volume, pan);
        }

    }

    public LoopableAudioClip createJDK10AudioClip(int sampleRate ) {
        LoopableAudioClip audioClip = null;


        if (sCompression_ == S_CMP_FIB_DELTA) {
            body_ = unpackFibonacciDeltaCompression(body_);
            sCompression_ = S_CMP_NONE;
        }


        if (sampleType_ == STEREO) {
            double volumeCorrection = computeStereoVolumeCorrection(body_);
            body_ = linear8StereoToMono(body_,volumeCorrection);
            sampleType_ = LEFT;
        }

        byte[] samples = get8SVXBody();
        samples = resample(samples, sampleRate, 8000);
        samples = linear8ToULaw(samples);

        return new JDK10AudioClip(samples, 8000);
    }

    public void play() {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.play();
    }

    public void loop() {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.loop();
    }

    public void stop() {
        if (cachedAudioClip_ != null) {
            cachedAudioClip_.stop();
        }
    }


    public void prepare() {
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
    }



    public static double computeStereoVolumeCorrection(byte[] stereo) {
        int half = stereo.length / 2;
        int max = 0;
        for (int i=0; i < half; i++) {
            max = Math.max(max,Math.abs(stereo[i]+stereo[half+i]));
        }
        if (max < 128) {
            return 1.0;
        } else {
            return 128d / max;
        }
    }

    public static byte[] linear8StereoToMono(byte[] stereo, double volumeCorrection) {
        int half = stereo.length / 2;
        byte[] mono = new byte[half];
        for (int i=0; i < half; i++) {
            mono[i] = (byte) ((stereo[i]+stereo[half+i]) * volumeCorrection);
        }
        return mono;
    }


    public static byte[] resample(byte[] input, int inputSampleRate, int outputSampleRate) {
        if (inputSampleRate == outputSampleRate) {


            return input;

        } else if (inputSampleRate > outputSampleRate) {





            float factor = inputSampleRate / (float) outputSampleRate;
            byte[] output = new byte[(int) Math.floor(input.length / factor)];

            for (int i=0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }
            return output;
        } else {



            float factor = inputSampleRate / (float) outputSampleRate;
            byte[] output = new byte[(int) Math.ceil(input.length / factor)];

            for (int i=0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }

            return output;
        }
    }
    public static byte[] linear8ToULaw(byte[] linear8) {
        byte[] ulaw = new byte[linear8.length];

        for (int i=0; i < linear8.length; i++) {
            ulaw[i] = linear16ToULaw(linear8[i] << 8);
        }

        return ulaw;
    }
    public static byte[] linear16ToULaw(int[] linear16) {
        byte[] ulaw = new byte[linear16.length];

        for (int i=0; i < linear16.length; i++) {
            ulaw[i] = linear16ToULaw(linear16[i]);
        }

        return ulaw;
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright 1989 by Steve Hayes
     */
    private final static byte[] CODE_TO_DELTA = {-34,-21,-13,-8,-5,-3,-2,-1,0,1,2,3,5,8,13,21};
    public static byte[] unpackFibonacciDeltaCompression(byte[] source) {
        /* Original algorithm by Steve Hayes
        int n = source.length - 2;
        int lim = n * 2;
        byte[] dest = new byte[lim];
        int x = source[1];
        int d;

        int j=2;
        for (int i=0; i < lim; i++)
          { // Decode a data nibble; high nibble then low nibble.
          d = source[j];       // get a pair of nibbles
          if ( (i & 1) == 1)   // select low or high nibble?
            {
            j++;
            }
          else
            { d >>= 4; }  // shift to get the high nibble

          x += CODE_TO_DELTA[d & 0xf]; // add in the decoded delta
          dest[i] = (byte)x; // store a 1-byte sample
          }
         */

        /* Improved algorithm (faster) */
        int n = source.length - 2;
        int lim = n * 2;
        byte[] dest = new byte[lim];
        int x = source[1];
        int d;
        int i=0;
        for (int j=2; j < n; j++) {
            // Decode a data nibble; high nibble then low nibble.

            d = source[j];    // Get one byte containig a pair of nibbles

            x += CODE_TO_DELTA[ (d >> 4) & 0xf];
            // shift to get the high nibble and add in the
            // decoded delta.
            dest[i++] = (byte)x;
            // store a 1-byte sample

            x += CODE_TO_DELTA[ d & 0xf ];
            // get the low nibble and add in the
            // decoded delta.
            dest[i++] = (byte)x;
            // store a 1-byte sample
        }

        return dest;
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright (c) 1989 by Rich Gopstein and Harris Corporation
     */

    public static void writeSunAudioHeader(OutputStream outfile, int dataSize, int sampleRate, int sampleType)
    throws IOException {
        wrulong(outfile,0x2e736e64);  // Sun magic = ".snd"
        wrulong(outfile,24);  // header size in bytes
        wrulong(outfile,dataSize);  // data size
        wrulong(outfile,1);  // Sun uLaw format
        wrulong(outfile, sampleRate);  // sample rate (only 8000 is supported by Java 1.1)

        // two channels for stereo sound,
        // one channel for mono (don't care for left or right speakers).
        wrulong(outfile, sampleType == STEREO ? 2 : 1);
    }

    public static void wrulong(OutputStream outfile, int ulong)
    throws IOException {
        outfile.write(ulong >> 24 & 0xff);
        outfile.write(ulong >> 16 & 0xff);
        outfile.write(ulong >>  8 & 0xff);
        outfile.write(ulong >>  0 & 0xff);
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright (c) 1999,2000 by Florian Bomers <florian@bome.com>
     * Copyright (c) 2000 by Matthias Pfisterer <matthias.pfisterer@gmx.de>
     */
    private static final boolean ZEROTRAP=true;
    private static final short BIAS=0x84;
    private static final int CLIP=32635;
    private static final int exp_lut1[] ={
        0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
        4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7
    };


    private static byte linear16ToULaw(int sample) {
        int sign, exponent, mantissa, ulawbyte;

        if (sample>32767) sample=32767;
        else if (sample<-32768) sample=-32768;
        /* Get the sample into sign-magnitude. */
        sign = (sample >> 8) & 0x80;    /* set aside the sign */
        if (sign != 0) sample = -sample;    /* get magnitude */
        if (sample > CLIP) sample = CLIP;    /* clip the magnitude */

        /* Convert from 16 bit linear to ulaw. */
        sample = sample + BIAS;
        exponent = exp_lut1[(sample >> 7) & 0xFF];
        mantissa = (sample >> (exponent + 3)) & 0x0F;
        ulawbyte = ~(sign | (exponent << 4) | mantissa);
        if (ZEROTRAP)
            if (ulawbyte == 0) ulawbyte = 0x02;  /* optional CCITT trap */
        return((byte) ulawbyte);
    }

    public void loop(int count) {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.loop(count);
    }

}
