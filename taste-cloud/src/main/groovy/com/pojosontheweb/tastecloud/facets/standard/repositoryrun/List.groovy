package com.pojosontheweb.tastecloud.facets.standard.repositoryrun

import com.pojosontheweb.tastecloud.model.Repository
import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import net.sourceforge.stripes.action.ActionBeanContext
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import woko.facets.builtin.developer.ListTabularImpl
import woko.persistence.ResultIterator

@FacetKey(name="list", profileId="standard", targetObjectType=RepositoryRun.class)
class List extends ListTabularImpl {

    Repository repo

    @Override
    protected ResultIterator<?> createResultIterator(ActionBeanContext abc, int start, int limit) {
        TasteStore store = woko.objectStore
        Criteria crit = store.session.createCriteria(RepositoryRun.class)
        if (repo) {
            crit.add(Restrictions.eq('repository', repo))
        }
        store.list(crit, start, limit)
    }

    @Override
    java.util.List<String> getPropertyNames() {
        ['repository', 'branch', 'startedOn', 'finishedOn', 'resultSummary']
    }
}