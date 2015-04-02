package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.developer.ListImpl
import woko.facets.builtin.ListObjects
import woko.facets.builtin.developer.ListTabularImpl

@FacetKey(name="list", profileId="guest", targetObjectType=Run.class)
class ListRunGuest extends ListTabularImpl implements ListObjects {

    @Override
    List<String> getPropertyNames() {
        ['id', 'browsr', 'startedOn', 'finishedOn','resultSummary']
    }
}