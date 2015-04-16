<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="repositoryRun" value="${renderTitle.facetContext.targetObject}"/>
<div class="page-header">
    <h1>
        Repo Run
        <c:if test="${repositoryRun.finishedOn==null}">
            <small>In progress</small>
        </c:if>
    </h1>
</div>
