package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.Repository
import com.pojosontheweb.tastecloud.model.RunJob
import com.pojosontheweb.tastecloud.model.Stats
import net.sourceforge.jfacets.annotations.AnnotatedFacetDescriptorManager
import woko.Woko
import woko.async.Job
import woko.async.JobListener
import woko.async.JobManager
import woko.auth.builtin.SessionUsernameResolutionStrategy
import woko.ext.usermanagement.core.AccountStatus
import woko.ext.usermanagement.hibernate.HbUser
import woko.ext.usermanagement.hibernate.HibernateUserManager
import woko.ioc.SimpleWokoIocContainer
import woko.push.PushFacetDescriptorManager

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TasteCloudInitListener implements ServletContextListener {

    /**
     * Close Woko
     */
    public final void contextDestroyed(ServletContextEvent e) {
        Woko woko = Woko.getWoko(e.getServletContext());
        if (woko != null) {
            woko.close();
        }
    }

    @Override
    void contextInitialized(ServletContextEvent servletContextEvent) {
        if (System.getProperty('taste.cloud.dev')) {
            Config c = new Config(
                imageName: 'pojosontheweb/taste',
                webappDir: '/Users/vankeisb/projects/selenium-utils/taste/docker/mountpoint',
                dockerDir: '/media/psf/projects/selenium-utils/taste/docker/mountpoint',
                dockerUrl: 'http://10.211.55.16:2375/',
                parallelJobs: 2
            )
            Woko woko = createWoko(new File(c.dbPath), "demo@taste.com", "demodemo", c.parallelJobs)
            servletContextEvent.servletContext.setAttribute(Woko.CTX_KEY, woko)
            TasteStore s = (TasteStore)woko.objectStore
            s.inTx {
                Config existing = s.config
                // drop existing one and replace
                s.delete(existing)
                s.save(c)
            }
        }
    }

    public static Woko createWoko(File dbPath, String email, String password, int parallelJobs) {
        def store = new TasteStore(
            ['com.pojosontheweb.tastecloud.model', 'woko.ext.usermanagement.hibernate'],
            dbPath
        )
        def facetDescriptorManager = new PushFacetDescriptorManager(
            new AnnotatedFacetDescriptorManager(
                ['com.pojosontheweb.tastecloud.facets', 'facets', 'woko.facets.builtin']
            ).initialize()
        )
        def userManager = new HibernateUserManager<HbUser>(store, HbUser.class).createDefaultUsers()
        store.inTx {
            def u = userManager.getUserByUsername(email)
            if (u) {
                store.delete(u)
            }
            // create a sample repo
            if (store.list(store.getClassMapping(Repository.class), 0, 1).totalSize==0) {
                store.save(
                    new Repository(
                        url:'https://github.com/pojosontheweb/taste-sample-repo.git',
                        name: 'GitHub Taste Samples'
                    )
                )
            }
            // create the stats object
            if (store.list(store.getClassMapping(Stats.class), 0, 1).totalSize==0) {
                store.save(
                    new Stats()
                )
            }
            store.stats.nbRunning = 0
            store.stats.nbTastesSubmitted = 0
            store.save(store.stats)
        }
        userManager.createUser(email, password, email, ['standard'], AccountStatus.Active)
        def ioc = new SimpleWokoIocContainer(
            store,
            userManager,
            new SessionUsernameResolutionStrategy(),
            facetDescriptorManager)
            .addComponent(JobManager.KEY, new TasteJobManager(Executors.newFixedThreadPool(parallelJobs), store))
            .addComponent(DockerManager.KEY, new DockerManager())
            .addComponent(Vcs.KEY, new Vcs())
        return new Woko(ioc, ['guest'])
    }
}

class TasteJobManager extends JobManager {

    private final TasteStore store

    TasteJobManager(ExecutorService pool, TasteStore store) {
        super(pool)
        this.store = store
    }

    @Override
    void submit(Job job, List<JobListener> listeners) {
        if (job instanceof RunJob) {
            store.save(store.stats.runSubmitted())
        }
        super.submit(job, listeners)
    }
}
