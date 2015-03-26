package com.pojosontheweb.taste-cloud.woko

import woko.WokoIocInitListener
import woko.ioc.WokoIocContainer
import woko.hbcompass.HibernateCompassStore
import woko.ext.usermanagement.hibernate.HibernateUserManager
import woko.ext.usermanagement.hibernate.HbUser
import woko.auth.builtin.SessionUsernameResolutionStrategy
import net.sourceforge.jfacets.IFacetDescriptorManager
import woko.ioc.SimpleWokoIocContainer
import woko.push.PushFacetDescriptorManager
import woko.mail.MailService
import woko.mail.ConsoleMailService
import woko.ext.usermanagement.mail.BindingHelper

/**
 * Example Woko init listener class that configures all default components (ObjectStore, UserManager etc.) using Simple IoC.
 * You can hack this class in order to inject the components you want. It should be declared as a listener in your web.xml.
 */
class Taste-cloudInitListener extends WokoIocInitListener<
        HibernateCompassStore,
        HibernateUserManager,
        SessionUsernameResolutionStrategy,
        IFacetDescriptorManager> {

    @Override
    protected WokoIocContainer<
            HibernateCompassStore,
            HibernateUserManager,
            SessionUsernameResolutionStrategy,
            IFacetDescriptorManager> createIocContainer() {

        // create default Hibernate + Compass ObjectStore implementation using the entities packages
        // found in web.xml
        def store = new HibernateCompassStore(getPackageNamesFromConfig(HibernateCompassStore.CTX_PARAM_PACKAGE_NAMES, true))

        // create the Hibernate user manager (handles POJO users and registration stuff)
        def userManager = new HibernateUserManager<HbUser>(store, HbUser.class).createDefaultUsers()

        // the username resolution strategy is session : we use built-in authentication
        def usernameResolutionStrategy = new SessionUsernameResolutionStrategy()

        // create the facet descriptor manager : annotated facets using the facet packages found
        // in web.xml, and wrap it with a PushFacetDescriptorManager so that we can use
        // push in the tooling module
        def facetDescriptorManager = new PushFacetDescriptorManager(createAnnotatedFdm())

        // sample MailService that logs emails to stdout, used in usermanagement (password
        // reset, registration/activation etc). Replace by a real implementation if you
        // need it, otherwise remove from IoC
        def mailService = new ConsoleMailService("http://www.taste-cloud.com", "info@taste-cloud.com", BindingHelper.createDefaultMailTemplates())

        return new SimpleWokoIocContainer<
            HibernateCompassStore,
            HibernateUserManager,
            SessionUsernameResolutionStrategy,
            IFacetDescriptorManager>(
                store,
                userManager,
                usernameResolutionStrategy,
                facetDescriptorManager)
            .addComponent(MailService.KEY, mailService)
    }
}
