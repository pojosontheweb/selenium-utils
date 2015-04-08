package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.developer.ListImpl
import woko.facets.builtin.ListObjects
import woko.facets.builtin.developer.ListTabularImpl

@FacetKey(name="list", profileId="standard", targetObjectType=Run.class)
class List extends ListTabularImpl implements ListObjects {

    @Override
    java.util.List<String> getPropertyNames() {
        ['id', 'browsr', 'startedOn', 'finishedOn','resultSummary']
    }
}