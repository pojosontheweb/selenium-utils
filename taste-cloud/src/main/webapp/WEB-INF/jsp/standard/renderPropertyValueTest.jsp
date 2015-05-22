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
<table class="results table">
    <tr>
        <td style="white-space: nowrap;">
            <h2>
                <w:title object="<%=test%>"/>
            </h2>
        </td>
        <td>
            <taste:test test="<%=test%>" run="<%=run%>"/>
        </td>
    </tr>
</table>

