<%@ page import="com.pojosontheweb.tastecloud.model.RunSummary" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.pojosontheweb.tastecloud.Util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Run run = (Run)renderPropertyValue.getOwningObject();
    RunSummary summary = (RunSummary)renderPropertyValue.getPropertyValue();
%>
<div class="row summary">
    <div class="col-sm-3">
        <h3>Browser</h3>
        <img class="browsr" alt="browser" src="<%=request.getContextPath()%>/img/<%=summary.getBrowsr().name()%>.png"/>
    </div>
    <div class="col-sm-3">
        <h3>Queued</h3>
        <fmt:formatDate value="<%=summary.getQueuedOn()%>" type="date" dateStyle="short"/>
        <br/>
        <fmt:formatDate value="<%=summary.getQueuedOn()%>" type="time" dateStyle="full"/>
        <br/>
        <small><%=Util.prettyTime(summary.getQueuedOn(), request.getLocale())%></small>
    </div>
    <div class="col-sm-3">
        <h3>Started</h3>
        <%
            Date startedOn = summary.getStartedOn();
            if (startedOn!=null) {
        %>
                <fmt:formatDate value="<%=summary.getStartedOn()%>" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="<%=summary.getStartedOn()%>" type="time" dateStyle="full"/>
                <br/>
                <small><%=Util.prettyTime(summary.getStartedOn(), request.getLocale())%></small>
        <%
            }
        %>
    </div>
    <div class="col-sm-3">
        <h3>Finished</h3>
        <%
            Date finishedOn = run.getFinishedOn();
            if (finishedOn==null) {
                if (startedOn!=null) {
        %>
                    <img src="<%=request.getContextPath()%>/img/ajax-loader.gif" alt="loader"/>
        <%
                }
            } else {
        %>
                <fmt:formatDate value="<%=finishedOn%>" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="<%=finishedOn%>" type="time" dateStyle="full"/>
                <br/>
                <small><%=Util.prettyTime(finishedOn, request.getLocale())%></small>
        <%
            }
        %>
    </div>
</div>