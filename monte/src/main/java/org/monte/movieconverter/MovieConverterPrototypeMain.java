
package org.monte.movieconverter;

import org.monte.media.Buffer;


public class MovieConverterPrototypeMain {
    private static class MovieReader {



        public long getSampleCount(int track) { return 1;}
        public void readSample(int track, long sampleIndex, Buffer buf) {}
        public void readSamples(int track, long sampleIndex, int sampleCount, Buffer buf) {}
        public long movieTimeToSample(long time) {return 1;}
        public long timeToSample(int track, long time) {return 1;}
        public long sampleToTime(int track, long sample) {return 1;}
        public long sampleToMovieTime(long sample) {return 1;}



        public int getTrackCount() {return 1;}
        public long getMovieDuration() {return 1;}
        public long getMovieTimeScale() { return 1;}
        public void setMovieStartTime(long time) {}
        public void setMovieEndTime(long time) {}
        public void setMovieTime(long time) {}
        public long getTimeScale(int track) { return 1;}
        public long getDuration(int track) {return 1;}
        public long getStartTime(int track) {return 1;}
        public void read(int track, Buffer buf) {}
    }
}
