<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.selenium.Browsr" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<c:set var="o" value="${home.facetContext.targetObject}"/>
<w:facet facetName="<%=WokoFacets.layout%>" targetObject="${o}"/>
<w:facet targetObject="${o}" facetName="<%=WokoFacets.renderTitle%>"/>

<fmt:message bundle="${wokoBundle}" var="pageTitle" key="woko.guest.home.pageTitle"/>
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="${pageTitle}">
  <s:layout-component name="body">

    <div class="container">

      <div class="jumbotron">
        <h1>
          Taste Cloud
        </h1>
        <p>
          Run your taste scripts remotely
        </p>
        <p>
          <a class="btn btn-primary btn-lg"
             href="${cp}/run">
            Create simple taste
          </a>
        </p>
      </div>

    </div>

  </s:layout-component>
</s:layout-render>