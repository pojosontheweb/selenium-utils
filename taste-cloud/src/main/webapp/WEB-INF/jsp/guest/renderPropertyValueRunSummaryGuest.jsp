<%@ page import="com.pojosontheweb.tastecloud.model.RunSummary" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Run run = (Run)renderPropertyValue.getOwningObject();
    RunSummary summary = (RunSummary)renderPropertyValue.getPropertyValue();
%>
<div class="row summary">
    <div class="col-sm-4">
        <h3>Browser</h3>
        <div class="well">
            <img class="browsr" alt="browser" src="${cp}/img/<%=summary.getBrowsr().name()%>.png"/>
        </div>
    </div>
    <div class="col-sm-4">
        <h3>Started on</h3>
        <div class="well">
            <fmt:formatDate value="<%=summary.getStartedOn()%>" type="date" dateStyle="short"/>
            <br/>
            <fmt:formatDate value="<%=summary.getStartedOn()%>" type="time" dateStyle="full"/>
        </div>
    </div>
    <div class="col-sm-4">
        <h3>Finished on</h3>
        <%
            Date finishedOn = run.getFinishedOn();
            if (finishedOn==null) {
        %>
        <div class="well">
            Run in progress
            <br/>
            <img src="${cp}/img/ajax-loader.gif" alt="loader"/>
        </div>
        <%
            } else {
        %>
            <div class="well">
                <fmt:formatDate value="<%=finishedOn%>" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="<%=finishedOn%>" type="time" dateStyle="full"/>
            </div>
        <%
            }
        %>
    </div>
</div>