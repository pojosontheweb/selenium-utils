<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Test" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Test test = (Test)renderPropertyValue.getPropertyValue();
    Run run = (Run)renderPropertyValue.getOwningObject();
%>
<h2>Test Result</h2>
<table class="results table table-bordered">
    <tr>
        <td style="white-space: nowrap;">
            <w:title object="<%=test%>"/>
        </td>
        <td>
            <taste:test test="<%=test%>" run="<%=run%>"/>
        </td>
    </tr>
</table>

