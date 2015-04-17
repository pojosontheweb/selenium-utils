<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="repoRun" value="${renderPropertyValue.propertyValue}"/>
<w:url var="repoUrl" facetName="view" object="${repoRun.repository}"/>
<a href="${repoUrl}">
  <c:out value="${repoRun.repository.name}"/>
</a>
<span class="label label-default">Git</span>