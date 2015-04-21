package com.pojosontheweb.tastecloud

import com.pojosontheweb.tastecloud.woko.DockerManager
import org.junit.Ignore
import org.junit.Test

class DockerManagerTest {

    @Test
    @Ignore("just for testing docker stuff")
    void startTaste() {
        DockerManager dm = new DockerManager()
        File dataDir = new File('/media/psf/projects/selenium-utils/taste/docker/sample')
        dm.startRun('pojosontheweb/taste', 'http://10.211.55.16:2375', dataDir,  'tests.taste')
        println "Done"
    }

}
