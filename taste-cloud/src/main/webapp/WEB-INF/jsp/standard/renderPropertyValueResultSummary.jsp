<%@ page import="com.pojosontheweb.tastecloud.model.ResultSummary" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="summary" value="${renderPropertyValue.propertyValue}"/>
<%
    RenderPropertyValue rpv = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    ResultSummary summary = (ResultSummary)rpv.getPropertyValue();
    if (summary.getFinished()) {
        // finished : display labels
        int nbFailed = summary.getNbFailed();
        int nbSuccess = summary.getNbSuccess();
        long elapsed = summary.getElapsed();
        if (nbSuccess>0) {
%>
            <span class="label label-success"><%=nbSuccess%></span>
<%
        }
        if (nbFailed>0) {
%>
            <span class="label label-danger"><%=nbFailed%></span>
<%
        }
%>
        <span class="label label-info">
            <i class="glyphicon glyphicon-time"> </i>
            <%=elapsed%> s
        </span>
<%
    } else  { %>
<img src="<%=request.getContextPath()%>/img/ajax-loader.gif" alt="loader" width="20">
<% } %>