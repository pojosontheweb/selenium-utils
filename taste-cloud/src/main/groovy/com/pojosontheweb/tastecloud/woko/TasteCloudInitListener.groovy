package com.pojosontheweb.tastecloud.woko

import woko.WokoIocInitListener
import woko.async.JobManager
import woko.ioc.WokoIocContainer
import woko.ext.usermanagement.hibernate.HibernateUserManager
import woko.ext.usermanagement.hibernate.HbUser
import woko.auth.builtin.SessionUsernameResolutionStrategy
import net.sourceforge.jfacets.IFacetDescriptorManager
import woko.ioc.SimpleWokoIocContainer
import woko.push.PushFacetDescriptorManager

class TasteCloudInitListener extends WokoIocInitListener {

    @Override
    protected WokoIocContainer createIocContainer() {

        def store = new TasteStore(getPackageNamesFromConfig(TasteStore.CTX_PARAM_PACKAGE_NAMES, true))

        def userManager = new HibernateUserManager<HbUser>(store, HbUser.class).createDefaultUsers()

        def usernameResolutionStrategy = new SessionUsernameResolutionStrategy()

        def facetDescriptorManager = new PushFacetDescriptorManager(createAnnotatedFdm())

        // sample MailService that logs emails to stdout, used in usermanagement (password
        // reset, registration/activation etc). Replace by a real implementation if you
        // need it, otherwise remove from IoC
//        def mailService = new ConsoleMailService("http://www.taste-cloud.com", "info@taste-cloud.com", BindingHelper.createDefaultMailTemplates())

        return new SimpleWokoIocContainer(
                store,
                userManager,
                usernameResolutionStrategy,
                facetDescriptorManager)
            .addComponent(JobManager.KEY, new JobManager())
            .addComponent(DockerManager.KEY, new DockerManager())
//            .addComponent(MailService.KEY, mailService)
    }
}
