package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.Run
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.hibernate.cfg.Configuration
import org.w3c.dom.Document
import org.xml.sax.InputSource
import woko.hbcompass.HibernateCompassStore
import woko.hibernate.HibernateStore
import woko.persistence.TransactionCallbackWithResult

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class TasteStore extends HibernateStore {

    private final File dbPath

    TasteStore(List<String> packageNames, File dbPath) {
        super(packageNames, false)
        this.dbPath = dbPath
        initialize()
    }

    def <T> T inTx(Closure<T> c) {
        doInTransactionWithResult(c as TransactionCallbackWithResult<T>)
    }

    Run getRun(runId) {
        (Run)session.get(Run.class, (Serializable)runId)
    }

    Config getConfig() {
        (Config)session.createCriteria(Config.class).uniqueResult()
    }

    @Override
    protected Configuration configure(Configuration config) {
        // load config template
        try {
            SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
            Template t = templateEngine.createTemplate(config.getClass().getResource("/hibernate.cfg.xml"))
            Map<String, String> binding = new HashMap<String, String>()
            binding.put("dbPath", dbPath.absolutePath)
            String replacedConfigXml = t.make(binding).toString()
            DocumentBuilder b = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
            Document doc = b.parse(new InputSource(new StringReader(replacedConfigXml)))
            return config.configure(doc)
        } catch(Exception e) {
            throw new RuntimeException(e)
        }
    }
}
