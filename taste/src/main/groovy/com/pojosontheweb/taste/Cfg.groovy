package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Browsr

import static com.pojosontheweb.selenium.SysProps.*

class Cfg {

    Map<String,String> sysProps = [:]
    OutputFormat output = OutputFormat.text

    static Cfg load(File configFile) {
        if (configFile.exists()) {
            // load the config
            GroovyShell shell = new GroovyShell()
            Cfg c = (Cfg)shell.evaluate(configFile)
            return c
        } else {
            // no config provided
            return null
        }
    }

    static Cfg config(@DelegatesTo(Cfg) Closure c) {
        Cfg cfg = new Cfg()
        def code = c.rehydrate(cfg, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return cfg
    }

    void output(@DelegatesTo(OutputCfg) Closure c) {
        OutputCfg cfg = new OutputCfg()
        def code = c.rehydrate(cfg, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    void locales(String... locales) {
        sysProps[webtests.locales] = locales.join(",")
    }

    void findr(@DelegatesTo(FindrCfg) Closure c) {
        FindrCfg cfg = new FindrCfg()
        def code = c.rehydrate(cfg, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    @Override
    public String toString() {
        return "Cfg{" +
            "sysProps=" + sysProps +
            '}';
    }

    class OutputCfg {
        void json() {
            output = OutputFormat.json
        }

        void text() {
            output = OutputFormat.text
        }

        void html() {
            output = OutputFormat.html
        }

    }

    class FindrCfg {
        void timeout(int i) {
            sysProps[webtests.findr.timeout] = Integer.toString(i)
        }
        void verbose(boolean v) {
            sysProps[webtests.findr.verbose] = Boolean.toString(v)
        }
    }

    void video(@DelegatesTo(VideoCfg) Closure c) {
        VideoCfg cfg = new VideoCfg()
        def code = c.rehydrate(cfg, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    class VideoCfg {
        void enabled(boolean enabled) {
            sysProps[webtests.video.enabled] = enabled.toString()
        }
        void dir(String path) {
            sysProps[webtests.video.dir] = path
        }
        void failuresOnly(boolean f) {
            sysProps[webtests.video.failures.only] = f.toString()
        }

    }

    void chrome() {
        sysProps[webtests.browser] = Browsr.Chrome.sysProp
    }

    void chrome(@DelegatesTo(ChromeCfg) Closure c) {
        chrome()
        ChromeCfg cc = new ChromeCfg()
        def code = c.rehydrate(cc, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    class ChromeCfg {

        void driverPath(String path) {
            sysProps[webdriver.chrome.driver] = path
        }

    }

    void firefox() {
        sysProps[webtests.browser] = Browsr.Firefox.sysProp
    }



}

