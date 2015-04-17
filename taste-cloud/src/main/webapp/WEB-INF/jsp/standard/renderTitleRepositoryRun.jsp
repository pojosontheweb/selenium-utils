<%@ page import="com.pojosontheweb.tastecloud.Util" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="rr" value="${renderTitle.facetContext.targetObject}"/>
<div class="page-header">
    <h1>
        <c:choose>
            <c:when test="${rr.finishedOn==null}">
                Repo Run in progress...
            </c:when>
            <c:otherwise>
                Repo Run finished
            </c:otherwise>
        </c:choose>
    </h1>
</div>