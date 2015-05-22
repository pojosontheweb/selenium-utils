package com.pojosontheweb.tastecloud

import com.pojosontheweb.tastecloud.woko.RepoDef
import com.pojosontheweb.tastecloud.woko.Vcs
import junit.framework.Assert
import org.junit.Test

class VcsTest {

    private def doTest(Closure c) {
        Vcs vcs = new Vcs()
        Util.withTmpDir { File tmpDir ->
            RepoDef repo = new RepoDef(url: 'https://github.com/pojosontheweb/taste-sample-repo.git')
            c(vcs, repo, tmpDir)
        }
    }

    @Test
    void pull() {
        doTest { Vcs vcs, RepoDef repo, File tmpDir ->
            vcs.pull(repo, tmpDir)
            assert (new File(tmpDir, 'sample-findr.taste')).text =~ /Taste Examples on google\.com/
            assert (new File(tmpDir, 'sample-taste.taste')).text =~ /Taste Examples on google\.com/
        }
    }

    @Test
    void pullTwiceThrowsException() {
        doTest { Vcs vcs, RepoDef repo, File tmpDir ->
            vcs.pull(repo, tmpDir)
            assert (new File(tmpDir, 'sample-findr.taste')).text =~ /Taste Examples on google\.com/
            try {
                vcs.pull(repo, tmpDir)
                Assert.fail('should have thrown')
            } catch (Exception e) {
                // all good
            }
        }
    }

}
