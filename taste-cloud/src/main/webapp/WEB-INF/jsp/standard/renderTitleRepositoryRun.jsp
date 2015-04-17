<%@ page import="com.pojosontheweb.tastecloud.Util" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="rr" value="${renderTitle.facetContext.targetObject}"/>
<div class="page-header">
    <h1>
        Repo Run
        <small>From <w:link object="${rr.repository}" facetName="view"/></small>
    </h1>
</div>
<div class="container-fluid repo-run-summary">
    <div class="row summary">
        <div class="col-sm-4">
            <h3>Queued</h3>
            <fmt:formatDate value="${rr.queuedOn}" type="date" dateStyle="short"/>
            <br/>
            <fmt:formatDate value="${rr.queuedOn}" type="time" dateStyle="full"/>
            <br/>
            <small><taste:prettyTime date="${rr.queuedOn}"/></small>
        </div>
        <div class="col-sm-4">
            <h3>Started</h3>
            <c:if test="${rr.startedOn!=null}">
                <fmt:formatDate value="${rr.startedOn}" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="${rr.startedOn}" type="time" dateStyle="full"/>
                <br/>
                <small><taste:prettyTime date="${rr.startedOn}"/></small>
            </c:if>
        </div>
        <div class="col-sm-4">
            <h3>Finished</h3>
            <c:choose>
                <c:when test="${rr.finishedOn==null}">
                    <c:if test="${rr.startedOn!=null}">
                        <img src="${cp}/img/ajax-loader.gif" alt="loader"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <fmt:formatDate value="${rr.finishedOn}" type="date" dateStyle="short"/>
                    <br/>
                    <fmt:formatDate value="${rr.finishedOn}" type="time" dateStyle="full"/>
                    <br/>
                    <small><taste:prettyTime date="${rr.finishedOn}"/></small>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
