<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="run" value="${renderPropertyValue.owningObject}"/>
<c:set var="logs" value="${renderPropertyValue.propertyValue}"/>
<w:objectKey var="runId" object="${run}"/>
<div class="logs">
    <h2>Logs</h2>
    <p>
        Only the last ${renderPropertyValue.limit} log messages are shown.
        Get <a href="${logUrl}">full logs</a>
    </p>
    <table class="logs-wrapper">
        <c:forEach var="log" items="${logs}">
            <tr>
                <td class="date" valign="top"><fmt:formatDate value="${log.logDate}" type="both" dateStyle="short" timeStyle="full"/></td>
                <td class="text" valign="top"><c:out value="${log.text}"/></td>
            </tr>
        </c:forEach>
    </table>
</div>