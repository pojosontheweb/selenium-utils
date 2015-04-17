<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%@ taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="rr" value="${runsFragment.facetContext.targetObject}"/>
<table class="table Run">
    <thead>
    <tr>
        <th>Browser</th>
        <th>Started</th>
        <th>Finished</th>
        <th>Result</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${rr.runs}" var="run">
        <tr>
            <td>
                <img class="browsr" alt="browser" src="${cp}/img/${run.browsr}.png"/>
            </td>
            <td><taste:fullDate date="${run.startedOn}"/></td>
            <td><taste:fullDate date="${run.finishedOn}"/></td>
            <td>TODO</td>
            <td>
                <w:url var="runUrl" facetName="view" object="${run}"/>
                <a class="btn btn-default" href="${runUrl}">
                    View
                </a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>