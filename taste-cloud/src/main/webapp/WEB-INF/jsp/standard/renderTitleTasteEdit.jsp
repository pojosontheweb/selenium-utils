<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<div class="page-header">
    <h1>
        <c:out value="${renderTitleEdit.title}"/>
        <small>
            <c:out value="${renderTitleEdit.facetContext.targetObject.id}"/>
        </small>
    </h1>
</div>
