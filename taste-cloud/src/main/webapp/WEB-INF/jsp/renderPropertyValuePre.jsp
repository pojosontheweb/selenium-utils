<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="v" value="${renderPropertyValue.propertyValue}"/>
<c:if test="${v!=null && v!=''}">
    <pre class="${renderPropertyValue.cssClass}"><c:out value="${renderPropertyValue.propertyValue}"/></pre>
</c:if>

