<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="woko.Woko" %>
<%@ page import="woko.facets.builtin.RenderTitle" %>
<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="woko.persistence.ObjectStore" %>

<%
    Woko<?,?,?,?> woko = Woko.getWoko(application);
    RenderTitle renderTitle = (RenderTitle)request.getAttribute(WokoFacets.renderTitleEdit);
    if (renderTitle==null) {
        renderTitle = (RenderTitle)request.getAttribute(WokoFacets.renderTitle);
    }
    Object target = renderTitle.getFacetContext().getTargetObject();
    ObjectStore s = woko.getObjectStore();
    String className = s.getClassMapping(s.getObjectClass(target));
%>
<div class="page-header">
    <h1><%=renderTitle.getTitle()%> <small>(<%=className%>)</small></h1>
</div>