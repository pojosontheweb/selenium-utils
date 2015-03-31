<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.tastecloud.actions.InitialConfigAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="run" value="${results.facetContext.targetObject}"/>
<w:facet facetName="<%=WokoFacets.layout%>"/>
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="results">
  <s:layout-component name="body">

    <div class="container">

      <div class="page-header">
        <h1>
          <w:title object="${run}"/>
          <small>
            Results
          </small>
        </h1>
      </div>

      <ul>
        <c:forEach var="link" items="${results.links}">
          <li>
            <a href="${link.href}">${link.text}</a>
          </li>
        </c:forEach>
      </ul>

    </div>

  </s:layout-component>
</s:layout-render>