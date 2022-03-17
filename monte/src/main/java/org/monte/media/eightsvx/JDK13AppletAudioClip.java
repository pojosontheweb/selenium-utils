

package org.monte.media.eightsvx;

import javax.sound.sampled.*;
import java.util.*;
import java.io.*;

public class JDK13AppletAudioClip implements LoopableAudioClip, Runnable {

    private byte[] samples;

    private static Mixer mixer;


    private static Vector lines = new Vector();


    private volatile Thread workerThread;


    private int loopCount;


    private int volume;


    private int sampleRate;


    private float pan;

    private int loopStart;

    private int loopEnd;




    public JDK13AppletAudioClip(byte[] samples, int sampleRate, int volume, float pan)
    throws IOException {
        this.samples = samples;
        this.volume = volume;
        this.pan = pan;
        this.loopStart = 0;
        this.loopEnd = samples.length;
        this.sampleRate = sampleRate;


        try {
            getMixer();
        } catch (LineUnavailableException e) {
            throw new IOException(e.toString());
        }

    }

    private static Mixer getMixer() throws LineUnavailableException {
        if (mixer == null) {
            mixer = (Mixer) AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
            SourceDataLine[] l = new SourceDataLine[16];
            for (int i=0; i < 16; i++) {
                l[i] = aquireLine();
            }
            for (int i=0; i < 16; i++) {
                poolLine(l[i]);
            }
        }
        return mixer;
    }


    private synchronized static SourceDataLine aquireLine()
    throws LineUnavailableException {
        SourceDataLine line;
        if (lines.size() > 0) {
            line = (SourceDataLine) lines.elementAt(0);
            lines.removeElementAt(0);
        } else {
            AudioFormat audioFormat = new AudioFormat(
            (float) 8000,
            8,
            1,
            true,
            true
            );

            Line.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            line = (SourceDataLine) getMixer().getLine(lineInfo);
            line.open();
            line.start();

        }
        return line;
    }


    private synchronized static void poolLine(SourceDataLine line) {
        if (lines.size() < 16) {


            lines.addElement(line);
        } else {
            line.close();
        }
    }


    public void setLoopPoints(int start, int end) {
        if (start < 0 || start >= samples.length || end < start && end != -1 || end >= samples.length)
            throw new IllegalArgumentException("start:"+start+" end:"+end);
        loopStart = start;
        loopEnd = (end == -1) ? samples.length : end + 1;
    }

    public void loop() {
        loop(LOOP_CONTINUOUSLY);
    }


    public synchronized void loop(int count) {
        stop();
        loopCount = count;
        workerThread = new Thread(this, this.toString());
        workerThread.setPriority(Thread.NORM_PRIORITY + 1);
        workerThread.start();
    }

    public void play() {
        loop(0);
    }

    public synchronized void stop() {
        if (workerThread != null) {
            Thread t = workerThread;
            workerThread = null;

        }
    }


    private void configureDataLine(DataLine clip) {
        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            control.setValue(pan);
System.out.println("setPan:"+pan);
        } else {
System.out.println("panning not supported "+pan);
        }
        if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            control.setValue(volume / 64f);
        }

        if (clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
            control.setValue((float) sampleRate);
        }
    }


    public void run() {

        long start = System.currentTimeMillis();
        long mediaDuration = (samples.length * Math.max(loopCount, 1)) / 8;
        int framePosition = 0;

        SourceDataLine out = null;
        try {
            out = aquireLine();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("configureDataLine");
        configureDataLine(out);

        byte[] buf = new byte[100];

        if (loopCount > 0 && framePosition < loopEnd) {
            while (workerThread == Thread.currentThread()
            && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {

                while (workerThread == Thread.currentThread()
                && framePosition < loopEnd) {
                    System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, loopEnd - framePosition));
                    framePosition += out.write(buf, 0, Math.min(buf.length, loopEnd - framePosition));

                }


                if (workerThread == Thread.currentThread()
                && (loopCount > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                    if (loopCount != Clip.LOOP_CONTINUOUSLY) {
                        loopCount--;
                        if (loopCount != 0) framePosition = loopStart;
                    }
                }
            }


            while (workerThread == Thread.currentThread()
            && framePosition < samples.length) {
                System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, samples.length - framePosition));
                framePosition += out.write(buf, 0, Math.min(buf.length, samples.length - framePosition));
            }
        } else {

            while (workerThread == Thread.currentThread()
            && framePosition < samples.length) {
                System.arraycopy(samples, framePosition, buf, 0, Math.min(buf.length, samples.length - framePosition));
                framePosition += out.write(buf, 0, Math.min(buf.length, samples.length - framePosition));
            }
        }



            long end = System.currentTimeMillis();
            long elapsed = end - start;
            while (workerThread == Thread.currentThread() && mediaDuration > elapsed) {
                try {
                    Thread.sleep(Math.max(1, Math.min(mediaDuration - elapsed, 100)));
                } catch (InterruptedException e) {
                }
                elapsed = System.currentTimeMillis() - start;
            }


        if (workerThread != Thread.currentThread()) {
            out.flush();
        }


        poolLine(out);
    }
}
