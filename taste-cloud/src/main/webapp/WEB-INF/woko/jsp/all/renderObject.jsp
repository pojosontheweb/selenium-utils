<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>
<%@ page import="woko.Woko" %>
<%@ page import="woko.persistence.ObjectStore" %>
<%@ page import="woko.facets.builtin.*" %>
<%
    RenderObject renderObject = (RenderObject) request.getAttribute(RenderObject.FACET_NAME);
    Object o = renderObject.getFacetContext().getTargetObject();
    Class<?> c = o.getClass();
    Woko<?, ?, ?, ?> woko = Woko.getWoko(application);
    ObjectStore store = woko.getObjectStore();
    String className = store.getClassMapping(store.getObjectClass(o));
%>
<div class="w-object <%=className%>">

    <%-- Display title and wokoLinks in the same row --%>
    <div class="container-fluid">
        <div class="row">
            <%-- Call the renderTitle facet in order to display the title --%>
            <div class="w-title col-lg-10 col-sm-9">
                <w:includeFacet targetObject="<%=o%>" facetName="<%=RenderTitle.FACET_NAME%>"/>
            </div>

            <%-- Call the renderTitle facet in order to display the available links --%>
            <div class="w-links col-lg-2 col-sm-3">
                <div class="pull-right">
                    <w:includeFacet targetObject="<%=o%>" facetName="<%=RenderLinks.FACET_NAME%>"/>
                </div>
            </div>
        </div>
    </div>

    <%-- before properties if any --%>
    <w:includeFacet targetObject="<%=o%>"
                    facetName="<%=RenderPropertiesBefore.FACET_NAME%>"
                    throwIfNotFound="false"/>

    <%-- Call the renderProperties facet in order to display the properties --%>
    <w:includeFacet targetObject="<%=o%>" facetName="<%=RenderProperties.FACET_NAME%>"/>

    <%-- after properties if any --%>
    <w:includeFacet targetObject="<%=o%>"
                    facetName="<%=RenderPropertiesAfter.FACET_NAME%>"
                    throwIfNotFound="false"/>
</div>