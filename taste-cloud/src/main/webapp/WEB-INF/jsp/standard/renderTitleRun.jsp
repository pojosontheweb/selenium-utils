<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<div class="page-header">
    <h1>
        <c:choose>
            <c:when test="${renderTitle.facetContext.targetObject.finishedOn==null}">
                Run in progress
            </c:when>
            <c:otherwise>
                Run finished
            </c:otherwise>
        </c:choose>
        <small>
            <c:out value="${renderTitle.title}"/>
        </small>
    </h1>
</div>
