<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="woko.facets.builtin.RenderObject" %>
<%@ page import="woko.Woko" %>
<%@ page import="woko.persistence.ObjectStore" %>
<%@ page import="woko.facets.builtin.RenderTitleEdit" %>
<%@ page import="woko.facets.builtin.WokoFacets" %>

<%
    RenderObject renderObject = (RenderObject)request.getAttribute(WokoFacets.renderObjectEdit);
    Object o = renderObject.getFacetContext().getTargetObject();
    ObjectStore s = Woko.getWoko(application).getObjectStore();
    String className = s.getClassMapping(s.getObjectClass(o));
%>
<div class="w-object w-edit <%=className%>">

    <%-- Display title and wokoLinks in the same row --%>
    <div class="container-fluid">
        <div class="row">
            <%-- Call the renderTitle facet in order to display the title --%>
            <div class="w-title col-lg-10 col-sm-9">
                <w:includeFacet targetObject="<%=o%>" facetName="<%=RenderTitleEdit.FACET_NAME%>"/>
            </div>
            <%-- Call the renderTitle facet in order to display the available links --%>
            <div class="w-links col-lg-2 col-sm-3">
                <div class="pull-right">
                    <w:includeFacet targetObject="<%=o%>" facetName="<%=WokoFacets.renderLinksEdit%>"/>
                </div>
            </div>
        </div>
    </div>

    <%-- Call the renderPropertiesEdit facet in order to display the properties as a FORM --%>
    <w:includeFacet targetObject="<%=o%>" facetName="<%=WokoFacets.renderPropertiesEdit%>"/>

</div>