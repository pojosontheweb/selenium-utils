

package org.monte.media.seq;

import org.monte.media.eightsvx.*;



public class SEQAudioCommand {
    
    public final static int COMMAND_PLAY_SOUND = 1;
    
    public final static int COMMAND_STOP_SOUND = 2;
    
    public final static int COMMAND_SET_FREQVOL = 3;
    
    
    public final static int FLAG_NO_INTERRUPT = 1;
    
    private int command;
    
    private int volume;
    
    private int sound;
    
    private int repeats;
    
    private int channelMask;
    
    private final static int CHANNEL0_MASK = 1, CHANNEL1_MASK = 2, CHANNEL2_MASK = 4,
    CHANNEL3_MASK = 8;
    private final static int CHANNEL_LEFT_MASK = CHANNEL0_MASK | CHANNEL2_MASK;
    private final static int CHANNEL_RIGHT_MASK = CHANNEL1_MASK | CHANNEL3_MASK;
    
    
    private int frequency;
    
    private int flags;
    
    
    private int activeChannelMask;
    
    
    private LoopableAudioClip audioClip;
    
    
    public SEQAudioCommand(int command, int volume, int sound, int repeats, int channelMask, int frequency, int flags) {
        this.command = command;
        this.volume = volume;
        this.sound = sound;
        this.repeats = repeats;
        this.channelMask = channelMask;
        this.frequency = frequency;
        this.flags = flags;
    }
    
    public int getChannelMask() {
        return channelMask;
    }
    public int getFrequency() {
        return frequency;
    }
    public int getSound() {
        return sound;
    }
    public int getVolume() {
        return volume;
    }
    public int getCommand() {
        return command;
    }
    
    public void prepare(SEQMovieTrack track) {
        if (command == COMMAND_PLAY_SOUND && audioClip == null) {
            float pan;
            if ((channelMask & CHANNEL_LEFT_MASK) != 0 
            && (channelMask & CHANNEL_RIGHT_MASK) == 0) {
                pan = -1f;
            } else if ((channelMask & CHANNEL_RIGHT_MASK) != 0 
            && (channelMask & CHANNEL_LEFT_MASK) == 0) {
                pan = 1f;
            } else {
                pan = 0f;
            }

            EightSVXAudioClip eightSVXAudioClip = (EightSVXAudioClip) track.getAudioClip(sound - 1);
            audioClip = eightSVXAudioClip.createAudioClip(
            (frequency == 0) ? eightSVXAudioClip.getSampleRate() : frequency,
            volume,
            track.isSwapSpeakers() ? -pan : pan
            );
        }
    }
    public void play(SEQMovieTrack track) {
        prepare(track);
        if (audioClip != null) {
            if (repeats < 2) {
            audioClip.play();
            } else {
            audioClip.loop(repeats);
            }
        }
        activeChannelMask = channelMask;
    }
    
    public void stop(SEQMovieTrack track) {
        activeChannelMask = 0;
        if (audioClip != null) {
            audioClip.stop();
        }
    }
    
    
    public void stop(SEQMovieTrack track, int channelMask) {
        activeChannelMask &= ~channelMask;
        if (activeChannelMask == 0) {
            audioClip.stop();
        }
    }
    
    public void doCommand(SEQMovieTrack track, SEQAudioCommand[] runningCommands) {

        switch (command) {
            case COMMAND_PLAY_SOUND : {
                boolean isPlayingOnOneChannel = false;
                for (int j=0; j < 4; j++) {
                    if ((channelMask & (1 << j)) != 0) {


                        if (runningCommands[j] != null) {
                            runningCommands[j].stop(track, 1 << j);
                        }
                        
                        if (! isPlayingOnOneChannel) {




                            isPlayingOnOneChannel = true;
                            play(track);
                        }
                        runningCommands[j] = this;
                    }
                }
            }
            break;
            case COMMAND_STOP_SOUND : {
                for (int j=0; j < 4; j++) {
                    if ((channelMask & (1 << j)) != 0) {


                        if (runningCommands[j] != null) {
                            runningCommands[j].stop(track, 1 << j);
                            runningCommands[j] = null;
                        }
                        
                    }
                }
            }
            break;
            case COMMAND_SET_FREQVOL :
                break;
        }
    }
    
    public void dispose() {
        if (audioClip != null) {
            audioClip = null;
        }
    }
}
