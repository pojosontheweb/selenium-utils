package com.pojosontheweb.tastecloud.facets.standard

import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.Stats
import com.pojosontheweb.tastecloud.model.activities.ActivityBase
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.hibernate.criterion.Order
import woko.facets.builtin.all.HomeImpl

@FacetKey(name="home", profileId="standard")
class Home extends HomeImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/home.jsp'
    }

    Stats getStats() {
        TasteStore s = (TasteStore)woko.objectStore
        s.stats
    }

    Boolean getNoRuns() {
        objectStore.list(objectStore.getClassMapping(Run.class), 0, 1).totalSize==0
    }

    List<ActivityBase> getActivities() {
        TasteStore store = (TasteStore)woko.objectStore
        store.session.createCriteria(ActivityBase.class)
            .addOrder(Order.desc("tstamp"))
            .setMaxResults(25)
            .list()
    }

    Resolution activitiesFragment() {
        return new ForwardResolution('/WEB-INF/jsp/standard/activities-fragment.jsp')
    }
}