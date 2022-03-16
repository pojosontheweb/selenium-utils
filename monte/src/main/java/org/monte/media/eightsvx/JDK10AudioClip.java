
package org.monte.media.eightsvx;

import sun.audio.*;


public class JDK10AudioClip implements LoopableAudioClip {
    private int sampleRate;
    private byte[] samples;
    private AudioDataStream audioStream;
    
    
    public JDK10AudioClip(byte [] samples, int sampleRate) {
        this.samples = samples;
        this.sampleRate = sampleRate;
    }
    
    
    public synchronized void play() {
        stop();
        byte[] data = new byte[samples.length + 24];
        writeSunAudioHeader(data, sampleRate, samples.length);
        System.arraycopy(samples, 0, data, 24, samples.length);
        AudioData audioData = new AudioData(data);
        audioStream = new AudioDataStream(audioData);
        AudioPlayer.player.start(audioStream);
    }
    
    
    public synchronized void loop() {
        stop();
        byte[] data = new byte[samples.length + 24];
        writeSunAudioHeader(data, sampleRate, samples.length);
        System.arraycopy(samples, 0, data, 24, samples.length);
        AudioData audioData = new AudioData(data);
        AudioDataStream audioStream = new ContinuousAudioDataStream(audioData);
        AudioPlayer.player.start(audioStream);
    }
    
    
    
    public void loop(int count) {
        if (count == 1 || count == 0) play();
        else if (count == LOOP_CONTINUOUSLY) loop();
        else {



            stop();
            
            byte[] data = new byte[samples.length * count + 24];
            writeSunAudioHeader(data, sampleRate, samples.length * count);
            for (int i=0; i < count; i++) {
                System.arraycopy(samples, 0, data, 24 + i * samples.length, samples.length);
            }
            AudioData audioData = new AudioData(data);
            AudioDataStream audioStream = new ContinuousAudioDataStream(audioData);
            AudioPlayer.player.start(audioStream);
        }
        
    }
    
    public synchronized void stop() {
        if (audioStream != null) {
            AudioPlayer.player.stop(audioStream);
            audioStream = null;
        }
    }
    
    
    public static void writeSunAudioHeader(byte[] data, int sampleRate, int datasize) {
        int headersize = 24;
        

        byte[] header = {

            (byte) 0x2e, (byte) 0x73, (byte) 0x6e, (byte) 0x64,

            (byte) (headersize >>> 24 & 0xff), (byte) (headersize >>> 16 & 0xff),
            (byte) (headersize >>> 8 & 0xff), (byte) (headersize & 0xff),

            (byte) (datasize >>> 24 & 0xff), (byte) (datasize >>> 16 & 0xff),
            (byte) (datasize >>> 8 & 0xff), (byte) (datasize & 0xff),

            (byte) 0, (byte) 0, (byte) 0, (byte) 1,

            (byte) (sampleRate >>> 24 & 0xff), (byte) (sampleRate >>> 16 & 0xff),
            (byte) (sampleRate >>> 8 & 0xff), (byte) (sampleRate & 0xff),

            (byte) 0, (byte) 0, (byte) 0, (byte) 1
        };
        

        System.arraycopy(header, 0, data, 0, headersize);
    }
}
