package com.pojosontheweb.selenium;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.Test;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import static com.pojosontheweb.selenium.Findrs.attrEquals;

public class SeleniumRecordrTest {

    @Test
    public void chromeVideo() throws Exception {
        var d = new ChromeBuildr().build();
        try {
            doTest(d);
        } finally {
            d.quit();
        }
    }

    @Test
    public void firefoxVideo() throws Exception {
        var d = new FirefoxBuildr().build();
        try {
            doTest(d);
        } finally {
            d.quit();
        }
    }

    private void deleteDir(File dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private void doTest(WebDriver d) throws Exception {
        var r = new SeleniumRecordr((TakesScreenshot) d);
        r.start();
        d.get("http://localhost:8000/");
        new Findr(d)
                .$$("form")
                .where(attrEquals("role", "search"))
                .expectOne()
                .eval();
        var tmpDir = Files.createTempDirectory("yalla");
        var f = tmpDir.toFile();
        Thread.sleep(1000);
        try {
            r.moveVideoFilesTo(f, "toto");
            var children = f.listFiles();
            assertEquals(1, children.length);
            assertEquals("toto.mp4", children[0].getName());
        } finally {
            deleteDir(f);
        }
    }

}
