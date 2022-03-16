

package org.monte.media.eightsvx;

import java.applet.*;

public interface LoopableAudioClip extends AudioClip {

    public final static int LOOP_CONTINUOUSLY = -1;


    public void loop(int count);

}
