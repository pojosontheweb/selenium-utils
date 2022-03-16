

package org.monte.media.eightsvx;

import java.applet.*;
import javax.sound.sampled.*;

public class JDK13ShortAudioClip implements LoopableAudioClip {
    private Clip clip;

    private byte[] samples;

    private int sampleRate;


    private int volume;


    private float pan;

    private AudioFormat audioFormat;


    public JDK13ShortAudioClip(byte[] samples, int sampleRate, int volume, float pan) {
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.volume = volume;
        this.pan = pan;
    }

    public synchronized void loop() {
        loop(LOOP_CONTINUOUSLY);
    }

    public synchronized void play() {
        stop();
        if (clip == null) {
            try {
                clip = createClip();
                clip.open(getAudioFormat(), (byte[]) samples.clone(), 0, samples.length);
                if (clip.isControlSupported(FloatControl.Type.PAN)) {
                    FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
                    control.setValue(pan);
                }
                if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                    FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                    control.setValue(volume / 64f);
                }

                clip.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                throw new InternalError(e.getMessage());
            }
        }
    }

    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    private AudioFormat getAudioFormat() {
        if (audioFormat == null) {
            audioFormat = new AudioFormat(
            (float) sampleRate,
            8,
            1,
            true,
            true
            );
        }
        return audioFormat;
    }

    private Clip createClip() throws LineUnavailableException {
        Line.Info lineInfo = new DataLine.Info(Clip.class, getAudioFormat());
        Clip c;

        c = (Clip) AudioSystem.getLine(lineInfo);

        return c;
    }


    public void loop(int count) {
        stop();
        try {
            clip = createClip();
            clip.open(getAudioFormat(), (byte[]) samples.clone(), 0, samples.length);
            if (clip.isControlSupported(FloatControl.Type.PAN)) {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
                control.setValue(pan);
            }
            if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                control.setValue(volume / 64f);
            }
            clip.loop(count);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }

}
