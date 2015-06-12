<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="logs" value="${logsFragment.logs}"/>
<ul>
<c:forEach var="log" items="${logs}">
    <li><c:out value="${log}"/></li>
</c:forEach>
</ul>
<div style="display:none;" class="rtinfo" data-finished="${logsFragment.facetContext.targetObject.finishedOn==null ? 'false' : 'true'}"></div>