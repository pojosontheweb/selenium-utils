package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.Stats
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.hibernate.Criteria
import org.hibernate.cfg.Configuration
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections
import org.w3c.dom.Document
import org.xml.sax.InputSource
import woko.hbcompass.HibernateCompassStore
import woko.hibernate.HibernateStore
import woko.persistence.ListResultIterator
import woko.persistence.ResultIterator
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

    Stats getStats() {
        (Stats)session.createCriteria(Stats.class).uniqueResult()
    }

    public ResultIterator<?> list(String className, Integer start, Integer limit) {
        Class clazz = getMappedClass(className);
        int s = start == null ? 0 : start;
        int l = limit == null ? -1 : limit;
        if (clazz == null) {
            return new ListResultIterator<Object>(Collections.emptyList(), s, l, 0);
        } else {
            Criteria crit = createListCriteria(clazz);

            // count
            crit.setProjection(Projections.rowCount());
            Long count = (Long)crit.uniqueResult();

            // sublist
            crit.setProjection(null);
            crit.setFirstResult(s);
            if (l != -1) {
                crit.setMaxResults(l);
            }
            if (clazz==Run.class) {
                crit.addOrder(Order.desc("queuedOn"))
            }

            // TODO optimize with scrollable results ?
            List<?> objects = crit.list();

            return new ListResultIterator<Object>(objects, s, l, count.intValue());
        }
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

    RepositoryRun getRepositoryRun(Long id) {
        (RepositoryRun)session.load(RepositoryRun.class, id)
    }
}
