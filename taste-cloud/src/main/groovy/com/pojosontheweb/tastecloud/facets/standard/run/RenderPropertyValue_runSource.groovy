package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_runSource", profileId="standard", targetObjectType=Run.class)
class RenderPropertyValue_runSource extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        if (propertyValue instanceof Taste) {
            '/WEB-INF/jsp/standard/renderPropertyValueRun_runSourceTaste.jsp'
        } else {
            '/WEB-INF/jsp/standard/renderPropertyValueRun_runSourceRepoRun.jsp'
        }
    }


}