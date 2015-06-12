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
import net.sourceforge.stripes.validation.EmailTypeConverter
import net.sourceforge.stripes.validation.SimpleError
import net.sourceforge.stripes.validation.Validate
import net.sourceforge.stripes.validation.ValidateNestedProperties
import net.sourceforge.stripes.validation.ValidationErrors
import net.sourceforge.stripes.validation.ValidationMethod
import woko.Woko
import woko.actions.BaseActionBean
import woko.actions.WokoTxInterceptor
import woko.async.JobManager
import woko.auth.builtin.SessionUsernameResolutionStrategy
import woko.ext.usermanagement.core.AccountStatus
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

    @Validate(required = true, converter = EmailTypeConverter.class)
    String email

    @Validate(required = true)
    String password

    @Validate(required = true)
    String confirmPassword

    @DontValidate
    @DefaultHandler
    Resolution display() {
        new ForwardResolution('/WEB-INF/jsp/initial-config.jsp')
    }

    @ValidationMethod
    public void checkPasswords(ValidationErrors errors) {
        if (password!=confirmPassword) {
            errors.add("password", new SimpleError("Passwords don't match"))
        }
    }

    Resolution configure() {
        logger.info("Configuring...")
        String s = config.dbPath
        File dbPath = new File(s)
        dbPath.mkdirs()
        logger.info("dbPath=${dbPath.absolutePath}")
        Woko woko = TasteCloudInitListener.createWoko(dbPath, email, password, config.parallelJobs)
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



}
