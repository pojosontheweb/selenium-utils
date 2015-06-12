<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="run" value="${renderTitle.facetContext.targetObject}"/>
<div class="page-header">
    <h1>
        <c:choose>
            <c:when test="${run.finishedOn==null}">
                Run in progress...
            </c:when>
            <c:otherwise>
                Run finished
            </c:otherwise>
        </c:choose>
        <small>
            <c:choose>
                <c:when test="${run.repositoryRun==null}">
                    From simple taste <w:link object="${run.fromTaste}" facetName="edit"/>
                </c:when>
                <c:otherwise>
                    <w:url var="repoRunUrl" facetName="view" object="${run.repositoryRun}"/>
                    Part of a <a href="${repoRunUrl}">repository run</a>
                </c:otherwise>
            </c:choose>
        </small>
    </h1>

</div>

