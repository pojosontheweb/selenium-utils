package com.pojosontheweb.tastecloud

import com.pojosontheweb.tastecloud.woko.DockerManager
import org.junit.Test

class DockerManagerTest {

    @Test
    void startTaste() {
        DockerManager dm = new DockerManager()
        File dataDir = new File('/Users/vankeisb/projects/selenium-utils/taste/docker/mountpoint')
        dm.startRun('pojosontheweb/taste', 'http://10.211.55.16:2375', dataDir,  'tests.taste') { o ->
            println o
        }
        println "Done"
    }

}
