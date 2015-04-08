<%--
    this is COPIED FROM renderProperties.jsp : there ain't such thing as a flat layout
    in bootstrap
    // TODO : clean up this flat layout thing !
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="woko.Woko" %>
<%@ page import="woko.facets.WokoFacetContext" %>
<%@ page import="woko.facets.builtin.RenderProperties" %>
<%@ page import="woko.facets.builtin.RenderPropertyName" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="woko.util.Util" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    RenderProperties renderProperties = (RenderProperties)request.getAttribute(WokoFacets.renderProperties);
    List<String> propertyNames = renderProperties.getPropertyNames();
    Map<String,Object> propertyValues = renderProperties.getPropertyValues();
    WokoFacetContext<?,?,?,?> fctx = (WokoFacetContext)renderProperties.getFacetContext();
    Woko<?,?,?,?> woko = fctx.getWoko();
    Object owningObject = fctx.getTargetObject();
%>
<%
    for (String pName : propertyNames) {
        Object pVal = propertyValues.get(pName);

        RenderPropertyName renderPropertyName =
            woko.getFacet(WokoFacets.renderPropertyName, request, owningObject, owningObject.getClass(), true);
        renderPropertyName.setPropertyName(pName);

        RenderPropertyValue renderPropertyValue = Util.getRenderPropValueFacet(woko, request, owningObject, pName, pVal);
        String pValFragmentPath = renderPropertyValue.getFragmentPath(request);

        Class<?> pType = Util.getPropertyType(owningObject.getClass(), pName);
        String pTypeStr = woko.getObjectStore().getClassMapping(pType);
%>
<div class="container-fluid w-properties">
    <div class="row w-property <%=pName%> <%=pTypeStr%>">
        <div class="w-property-value col-md-12">
            <jsp:include page="<%=pValFragmentPath%>"/>
        </div>
    </div>
</div>
<%
    }
%>