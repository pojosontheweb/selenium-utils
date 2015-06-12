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
          Taste is a toolkit that helps writing and running robust, efficient, and
          simple Selenium tests.
        </p>
      </div>

      <div class="row">
        <div class="col-md-4">
          <h2>Neat APIs</h2>
          <p>
            Taste relies on <a href="http://pojosontheweb.com#findr">Findr</a>
            and a few Groovy DSLs in order to provide fluent, easy to use, yet
            powerful APIs. It allows for async-friendly, reusable and compact
            test code.
          </p>
        </div>
        <div class="col-md-4">
          <h2>Cloud-ready</h2>
          <p>
            Taste relies on Docker in order to run your tests on demand, in parallel,
            and in various browsers. We provide pre-configured container images, so
            you don't have no setup to do.
          </p>
        </div>
        <div class="col-md-4">
          <h2>Video & Reporting</h2>
          <p>
            Taste records your tests as videos, so that you can check out what went
            wrong in your tests.
            You can also Monitor your tests over time, using various KPIs.
          </p>
        </div>
      </div>

    </div>

  </s:layout-component>
</s:layout-render>