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

    <div class="container-fluid">

      <div class="jumbotron">
        <h1>
          Taste
          <small>
            Selenium with Style
          </small>
        </h1>
        <p>
          Taste is a toolkit that makes Selenium easy etc etc.
        </p>
      </div>

      <div class="row">
        <div class="col-md-4">
          <h2>Findr</h2>
          <p>
            Taste uses Findr in order to provide robust, efficient and simple tests.
          </p>
        </div>
        <div class="col-md-4">
          <h2>Docker</h2>
          <p>
            Taste cloud is Docker-based, and allows to run your tests in parallel,
            in various browsers.
          </p>
        </div>
        <div class="col-md-4">
          <h2>Reporting</h2>
          <p>
            Monitor your tests over time.
          </p>
        </div>
      </div>

    </div>

  </s:layout-component>
</s:layout-render>