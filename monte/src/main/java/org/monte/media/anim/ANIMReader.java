
package org.monte.media.anim;

import org.monte.media.seq.SEQMovieTrack;
import org.monte.media.seq.*;
import org.monte.media.image.BitmapImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ANIMReader {

    private ANIMMovieTrack track;


    private int fetchedEven = -1, fetchedOdd = -1;

    private BitmapImage bitmapEven, bitmapOdd;


    public ANIMReader(File file) throws IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            ANIMDecoder decoder = new ANIMDecoder(in);
            track = new ANIMMovieTrack();
            decoder.produce(track, 0, true);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void close() throws IOException {

    }

    public int getFrameCount() {
        return track.getFrameCount();
    }

    public int getTimeBase() {
        return track.getJiffies();
    }

    public BitmapImage createCompatibleBitmap() {
        return new BitmapImage(
                track.getWidth(),
                track.getHeight(),
                track.getNbPlanes() + (track.getMasking() == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                track.getFrame(0).getColorModel());
    }


    public void readFrame(int index,BitmapImage image) {
        BitmapImage fetched = fetchFrame(index);

        System.arraycopy(fetched.getBitmap(), 0, image.getBitmap(), 0, fetched.getBitmap().length);
        image.setPlanarColorModel(track.getFrame(index).getColorModel());
    }


    public int getDuration(int index) {
        return (int) track.getFrame(index).getRelTime();
    }

    private BitmapImage fetchFrame(int index) {
        if (bitmapOdd == null || bitmapEven == null) {
            bitmapOdd = createCompatibleBitmap();
            bitmapEven = createCompatibleBitmap();
        }

        ANIMFrame frame = null;
        int fetched;
        int interleave = track.getInterleave();
        BitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {

            if (fetchedEven == index) {
                return bitmapEven;
            }
            fetched = fetchedEven;
            bitmap = bitmapEven;
            fetchedEven = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = (ANIMFrame) track.getFrame(fetched);
                frame.decode(bitmap, track);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = (ANIMFrame) track.getFrame(0);
                    frame.decode(bitmap, track);
                    fetched = 0;
                }
            }
        } else {

            if (fetchedOdd == index) {
                return bitmapOdd;
            }
            fetched = fetchedOdd;
            bitmap = bitmapOdd;
            fetchedOdd = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = (ANIMFrame) track.getFrame(fetched);
                frame.decode(bitmap, track);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = (ANIMFrame) track.getFrame(0);
                    frame.decode(bitmap, track);
                    frame = (ANIMFrame) track.getFrame(1);
                    frame.decode(bitmap, track);
                    fetched = 1;
                }
            }
        }
        for (int i = fetched + interleave; i <= index; i += interleave) {
            frame = (ANIMFrame) track.getFrame(i);
            frame.decode(bitmap, track);
        }
        return bitmap;
    }

    public int getJiffies() {
        return track.getJiffies();
    }
}
