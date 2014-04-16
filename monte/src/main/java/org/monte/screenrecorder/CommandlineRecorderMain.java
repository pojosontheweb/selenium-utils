/*
 * @(#)CommandlineRecorderMain.java  1.0  2011-08-05
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.screenrecorder;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

/**
 * {@code CommandlineRecorderMain}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-05 Created.
 */
public class CommandlineRecorderMain {

    /**
     * FIXME - Add commandline arguments for recording time.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        GraphicsConfiguration gc = GraphicsEnvironment//
                .getLocalGraphicsEnvironment()//
                .getDefaultScreenDevice()//
                .getDefaultConfiguration();
        // FIXME - Implement me
        ScreenRecorder sr = new ScreenRecorder(
                gc/*,
                "QuickTime", 24,
                ScreenRecorder.CursorEnum.BLACK,
                30, 15,
                44100*/);
        sr.start();
        Thread.sleep(5000);
        sr.stop();
    }
}
