<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Suite" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Test" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="com.pojosontheweb.tastecloud.model.SuiteCounts" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Suite suite = (Suite)renderPropertyValue.getPropertyValue();
    Run run = (Run)renderPropertyValue.getOwningObject();
%>
<table class="results table">
    <tr>
        <td style="white-space: nowrap;">
            <h2>
                <w:title object="<%=suite%>"/>
            </h2>
        </td>
        <td>
            <h2>
            <%
                SuiteCounts counts = suite.getCounts();
                double ratio = Math.ceil(counts.getRatio() * 100) / 100;
            %>
                <span class="label label-success"><%=counts.getNbSuccess()%></span>
                <% if (ratio==100) { %>
                    <span class="label label-success"><%=ratio%> %</span>
                <% } else if (ratio==0) { %>
                    <span class="label label-danger"><%=counts.getNbFailed()%></span>
                    <span class="label label-danger"><%=ratio%> %</span>
                <% } else { %>
                    <span class="label label-danger"><%=counts.getNbFailed()%></span>
                    <span class="label label-warning"><%=ratio%> %</span>
                <% }%>
                <span class="label label-info"><%=counts.getTotal()%></span>
                <span class="label label-info">
                    <i class="glyphicon glyphicon-time"> </i>
                    <%=suite.getElapsed()%> s
                </span>
            </h2>
        </td>
    </tr>
    <% for (Test test : suite.getTestResults()) { %>

        <tr>
            <td style="white-space: nowrap;">
                <h3>
                    <w:title object="<%=test%>"/>
                </h3>
            </td>
            <td>
                <taste:test test="<%=test%>" run="<%=run%>"/>
            </td>
        </tr>
    <% } %>
</table>

