package com.pojosontheweb.tastecloud.actions

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.DockerManager
import com.pojosontheweb.tastecloud.woko.TasteCloudInitListener
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.AnnotatedFacetDescriptorManager
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.SimpleMessage
import net.sourceforge.stripes.action.UrlBinding
import net.sourceforge.stripes.validation.Validate
import net.sourceforge.stripes.validation.ValidateNestedProperties
import woko.Woko
import woko.actions.BaseActionBean
import woko.actions.WokoTxInterceptor
import woko.async.JobManager
import woko.auth.builtin.SessionUsernameResolutionStrategy
import woko.ext.usermanagement.hibernate.HbUser
import woko.ext.usermanagement.hibernate.HibernateUserManager
import woko.ioc.SimpleWokoIocContainer
import woko.push.PushFacetDescriptorManager
import woko.util.WLogger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@UrlBinding("/initial-config")
class InitialConfigAction extends BaseActionBean {

    private static final WLogger logger = WLogger.getLogger(InitialConfigAction.class)

    @ValidateNestedProperties([
        @Validate(field = 'webappDir', required = true),
        @Validate(field = 'dockerDir', required = true),
        @Validate(field = 'dockerDir', required = true),
        @Validate(field = 'parallelJobs', required = true, minvalue = 1.0d, maxvalue = 64.0d)
    ])
    Config config

    @DontValidate
    @DefaultHandler
    Resolution display() {
        new ForwardResolution('/WEB-INF/jsp/initial-config.jsp')
    }

    Resolution configure() {
        logger.info("Configuring...")
        String s = config.webappDir + File.separator + 'db' + File.separator + 'taste'
        File dbPath = new File(s)
        dbPath.mkdirs()
        logger.info("dbPath=${dbPath.absolutePath}")
        Woko woko = createWoko(dbPath)
        context.servletContext.setAttribute('woko', woko)
        TasteStore store = (TasteStore)woko.objectStore
        store.inTx {
            def existingCfg = store.config
            if (existingCfg) {
                existingCfg.webappDir = config.webappDir
                existingCfg.dockerDir = config.dockerDir
                existingCfg.dockerUrl = config.dockerUrl
                existingCfg.imageName = config.imageName
                existingCfg.parallelJobs = config.parallelJobs
                store.save(existingCfg)
            } else {
                store.save(config)
            }
        }
        context.messages.add(new SimpleMessage('Application configured and ready to rock.'))
        logger.info("App configured : $config")
        new RedirectResolution("/")
    }

    private Woko createWoko(File dbPath) {
        def store = new TasteStore(
            ['com.pojosontheweb.tastecloud.model', 'woko.ext.usermanagement.hibernate'],
            dbPath
        )
        def facetDescriptorManager = new PushFacetDescriptorManager(
            new AnnotatedFacetDescriptorManager(
                ['com.pojosontheweb.tastecloud.facets', 'facets', 'woko.facets.builtin']
            ).initialize()
        )
        def ioc = new SimpleWokoIocContainer(
            store,
            new HibernateUserManager<HbUser>(store, HbUser.class).createDefaultUsers(),
            new SessionUsernameResolutionStrategy(),
            facetDescriptorManager)
            .addComponent(JobManager.KEY, new JobManager(Executors.newFixedThreadPool(config.parallelJobs)))
            .addComponent(DockerManager.KEY, new DockerManager())
        return new Woko(ioc, ['guest'])
    }

}
