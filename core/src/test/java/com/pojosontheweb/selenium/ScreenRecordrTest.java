package com.pojosontheweb.selenium;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ScreenRecordrTest {

     @Test
    public void recordScreen() throws Exception {
         var r = new ScreenRecordr();
         r.start();
         Thread.sleep(1000);
         r.stop();
         var files = r.getVideoFiles();
         assertEquals(1, files.size());
         var f = files.get(0);
         assertTrue(f.exists());
         assertTrue(f.isFile());
         assertTrue(f.getName().endsWith(".mov"));
         String destDirName = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
         File destDir = new File(destDirName);
         try {
              r.moveVideoFilesTo(destDir, "myTestCase");
              List<File> destFiles = Arrays.asList(destDir.listFiles());
              assertNotNull(files);
              assertEquals(1, destFiles.size());
              assertTrue(destFiles.get(0).getAbsolutePath().endsWith("myTestCase.mov"));

         } finally {
               deleteDirectory(destDir);
         }
     }

     private static boolean deleteDirectory(File directoryToBeDeleted) {
          File[] allContents = directoryToBeDeleted.listFiles();
          if (allContents != null) {
               for (File file : allContents) {
                    deleteDirectory(file);
               }
          }
          return directoryToBeDeleted.delete();
     }
}
