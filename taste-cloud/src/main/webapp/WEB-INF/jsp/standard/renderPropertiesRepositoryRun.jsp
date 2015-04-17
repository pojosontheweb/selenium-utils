<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>
<%@taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="rr" value="${renderProperties.facetContext.targetObject}"/>
<div class="container-fluid">
    <div class="row summary">
        <div class="col-sm-3">
            <h3>Repository</h3>
            <c:out value="${rr.branch}"/>@<w:link object="${rr.repository}" facetName="view"/>
            <br/>
            <small class="revision"><c:out value="${rr.revision}"/></small>
        </div>
        <div class="col-sm-2">
            <h3>Queued</h3>
            <fmt:formatDate value="${rr.queuedOn}" type="date" dateStyle="short"/>
            <br/>
            <fmt:formatDate value="${rr.queuedOn}" type="time" dateStyle="full"/>
            <br/>
            <small><taste:prettyTime date="${rr.queuedOn}"/></small>
        </div>
        <div class="col-sm-2">
            <h3>Started</h3>
            <c:if test="${rr.startedOn!=null}">
                <fmt:formatDate value="${rr.startedOn}" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="${rr.startedOn}" type="time" dateStyle="full"/>
                <br/>
                <small><taste:prettyTime date="${rr.startedOn}"/></small>
            </c:if>
        </div>
        <div class="col-sm-2">
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
        <div class="col-sm-3">
            <h3>Results</h3>
            <c:choose>
                <c:when test="${rr.finishedOn==null}">
                    <c:if test="${rr.startedOn!=null}">
                        <img src="${cp}/img/ajax-loader.gif" alt="loader"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <c:set var="s" value="${rr.resultSummary}"/>
                    <c:if test="${s.nbSuccess>0}">
                        <span class="label label-success">${s.nbSuccess}</span>
                    </c:if>
                    <c:if test="${s.nbFailed>0}">
                        <span class="label label-danger">${s.nbFailed}</span>
                    </c:if>
                    <c:choose>
                        <c:when test="${s.successRatio<100.0}">
                            <span class="label label-danger">${s.successRatio} %</span>
                        </c:when>
                        <c:otherwise>
                            <span class="label label-success">${s.successRatio} %</span>
                        </c:otherwise>
                    </c:choose>
                    <br/>
                    <span class="label label-info">${s.nbFailed+s.nbSuccess}</span>
                    <span class="label label-info"><taste:prettyDuration millis="${s.elapsed*1000}"/></span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <h2>Runs</h2>
    TODO
</div>

