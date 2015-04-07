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
        // we need to init a "fake" woko so that inter

    }

}
