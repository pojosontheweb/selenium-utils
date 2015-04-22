package com.pojosontheweb.tastecloud

import org.apache.commons.io.FileUtils
import org.ocpsoft.prettytime.PrettyTime
import woko.util.WLogger

import java.util.concurrent.TimeUnit

class Util {

    private static final WLogger logger = WLogger.getLogger(Util.class)

    static def withTmpDir(Closure c) {
        String s = System.getProperty('java.io.tmpdir') + File.separator + UUID.randomUUID().toString()
        File tmpDir = new File(s)
        tmpDir.mkdirs()
        try {
            return c(tmpDir)
        } finally {
            FileUtils.deleteDirectory(tmpDir)
        }
    }

    static File tmpFile(String name) {
        String s = System.getProperty('java.io.tmpdir') + File.separator + UUID.randomUUID().toString()
        File tmpDir = new File(s)
        tmpDir.mkdirs()
        return new File(tmpDir, name)
    }


    static String prettyTime(Date d, Locale l) {
        new PrettyTime(d, l).format(new Date())
    }

    static String prettyDuration(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!")
        }

        def sb = new StringBuilder()

        long days = TimeUnit.MILLISECONDS.toDays(millis)
        if (days) {
            sb << days << "d "
        }
        millis -= TimeUnit.DAYS.toMillis(days)
        long hours = TimeUnit.MILLISECONDS.toHours(millis)
        if (hours) {
            sb << hours << "h "
        }
        millis -= TimeUnit.HOURS.toMillis(hours)
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        if (minutes) {
            sb << minutes << "m "
        }
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        sb << seconds << "s"

        sb.toString()
    }

    static List<String> tail(File file, int nbLines) {
        // TODO use RandomFileAccess this is gonna be very expensive on large files...
        List lines = file.readLines('utf-8')
        int totalLines = lines.size()
        if (totalLines<=nbLines) {
            return lines
        } else {
            return lines[totalLines-nbLines..-1]
        }
    }

}
