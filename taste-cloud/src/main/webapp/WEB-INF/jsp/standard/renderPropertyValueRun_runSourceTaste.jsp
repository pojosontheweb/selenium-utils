<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="taste" value="${renderPropertyValue.propertyValue}"/>
<w:url var="u" facetName="edit" object="${taste}"/>
<a href="u"><c:out value="${taste.name}"/></a>
