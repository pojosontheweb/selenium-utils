package com.pojosontheweb.tastecloud

import org.apache.commons.io.FileUtils
import woko.util.WLogger

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

}
