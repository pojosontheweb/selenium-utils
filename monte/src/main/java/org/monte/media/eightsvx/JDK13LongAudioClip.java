

package org.monte.media.eightsvx;

import java.applet.*;
import javax.sound.sampled.*;
import java.io.*;

public class JDK13LongAudioClip implements LoopableAudioClip, Runnable {

    private SourceDataLine dataLine;

    private byte[] samples;

    private int sampleRate;

    private int framePosition;

    private int loopStart;

    private int loopEnd;


    private int loopCount;

    private volatile Thread thread;


    private int volume;


    private float pan;


    public JDK13LongAudioClip(byte[] samples, int sampleRate, int volume, float pan) {
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.volume = volume;
        this.pan = pan;
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.loopStart = 0;
        this.loopEnd = samples.length;
    }


    public void loop() {
        stop();
        framePosition = 0;
        loop(LOOP_CONTINUOUSLY);
    }

    public synchronized void loop(int count) {
        stop();
        try {
            dataLine = createDataLine();
            dataLine.open();
                if (dataLine.isControlSupported(FloatControl.Type.BALANCE)) {
                    FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.BALANCE);
                    control.setValue(pan);
                }
                if (dataLine.isControlSupported(FloatControl.Type.VOLUME)) {
                    FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.VOLUME);
                    control.setValue(volume / 64f);
                }
            loopCount = count;
            thread = new Thread(this, "JDK13AudioClip");
            thread.setPriority(Thread.NORM_PRIORITY + 1);
            thread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }


    public void play() {
        stop();
        framePosition = 0;
        loop(0);
    }

    public void start() {
        loop(0);
    }


    public synchronized void stop() {
        Thread t = thread;
        if (thread != null) {
            thread =  null;
            try {
                t.join();
            } catch (InterruptedException e) {
            }
            dataLine = null;
        }
    }


    public void setFramePosition(int param) {
        framePosition = param;
    }


    public long getMicrosecondLength() {

        return samples.length / sampleRate;
    }


    public long getMicrosecondPosition() {
        SourceDataLine sdl = dataLine;
        return (sdl == null) ? 0 : sdl.getMicrosecondPosition();
    }


    public void setLoopPoints(int start, int end) {
        if (start < 0 || start >= samples.length || end < start && end != -1 || end >= samples.length)
            throw new IllegalArgumentException("start:"+start+" end:"+end);
        loopStart = start;
        loopEnd = (end == -1) ? samples.length : end + 1;
    }



    private SourceDataLine createDataLine() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(
        (float) sampleRate,
        8,
        1,
        true,
        true
        );
        Line.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sdl;

        sdl = (SourceDataLine) AudioSystem.getLine(lineInfo);


        return sdl;
    }


    public void run() {
        dataLine.start();

        byte[] buf = new byte[512];
        if (loopCount > 0 && framePosition < loopEnd) {
            while (thread == Thread.currentThread()
            && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {

                while (thread == Thread.currentThread()
                && framePosition < loopEnd) {
                    System.arraycopy(samples, framePosition, buf, 0, Math.min(512, loopEnd - framePosition));
                    framePosition += dataLine.write(buf, 0, Math.min(512, loopEnd - framePosition));

                }


                if (thread == Thread.currentThread()
                && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                    if (loopCount != Clip.LOOP_CONTINUOUSLY) loopCount--;
                    framePosition = loopStart;
                }
            }
        }


        while (thread == Thread.currentThread()
        && framePosition < samples.length) {
            System.arraycopy(samples, framePosition, buf, 0, Math.min(512, samples.length - framePosition));
            framePosition += dataLine.write(buf, 0, Math.min(512, samples.length - framePosition));
        }



        if (thread == Thread.currentThread()) {
            dataLine.drain();
        }



        dataLine.stop();
        dataLine.close();
        if (thread == null) System.out.println(this+" PRELIMINARY finish");
        else System.out.println(this+" liberate finish");
    }
}
