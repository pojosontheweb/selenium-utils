<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="woko.Woko" %>
<%@ page import="woko.facets.WokoFacetContext" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="woko.facets.builtin.RenderTitle" %>
<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="woko.persistence.ObjectStore" %>
<%@ page import="woko.util.Util" %>

<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(WokoFacets.renderPropertyValue);
    WokoFacetContext<?,?,?,?> fctx = (WokoFacetContext)renderPropertyValue.getFacetContext();
    Woko<?,?,?,?> woko = fctx.getWoko();
    ObjectStore os = fctx.getWoko().getObjectStore();
    Object propertyValue = fctx.getTargetObject();
    String propertyName = renderPropertyValue.getPropertyName();
    Object owningObject = renderPropertyValue.getOwningObject();
    Class<?> propertyClass = propertyValue!=null ?
            os.getObjectClass(propertyValue) :
            Util.getPropertyType(owningObject.getClass(), propertyName);

    String propertyMappedClassName = os.getClassMapping(propertyClass);
    String propertyClassName;
    String href = null;
    String linkTitle = null;
    if (propertyMappedClassName!=null) {
        propertyClassName = propertyMappedClassName;
        if (propertyValue!=null) {
            RenderTitle rt = woko.getFacet(WokoFacets.renderTitle, request, propertyValue, propertyClass, true);
            linkTitle = rt.getTitle();
            String key = os.getKey(propertyValue);
            if (key!=null) {
                // we have a className and a key, can the user view the object ?
                if (woko.getFacet(WokoFacets.view, request, propertyValue, propertyClass)!=null) {
                    href = request.getContextPath() + "/view/" + propertyClassName + "/" + key;
                }
            }
        }
    }
    Object propertyValueStr = linkTitle!=null ? linkTitle : propertyValue;
%>
<c:choose>
    <c:when test="<%=href!=null%>">
        <a href="<%=href%>"><c:out value="<%=linkTitle%>"/></a>
    </c:when>
    <c:otherwise>
        <p class="form-control-static">
            <c:out value="<%=propertyValueStr%>"/>
        </p>
    </c:otherwise>
</c:choose>