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

      <div class="page-header">
        <h1>Your dashboard</h1>
      </div>

    <div class="dashboard">
        
        <c:choose>
          <c:when test="${home.nbRuns==0}">
            <p>
              You have not yet ran any Taste yet !
              Get started :
            </p>
            <ul>
              <li>
                <a href="${cp}/edit/Taste?createTransient=true">Create a simple taste</a> :
                edit and run your Taste. Good for basic tests, and learning the APIs.
              </li>
              <li>
                <a href="${cp}/list/Repository">Bind a git repository</a> : The full monty.
                Automatic build, parallel tests, reporting, etc.
              </li>
            </ul>

          </c:when>
          <c:otherwise>

            <div class="row">
              <div class="col-md-4">
                <div class="well text-center">
                  ${home.nbRuns}
                  <br/>
                  total runs
                </div>
              </div>
              <div class="col-md-4 text-center">
                <div class="well">
                  TODO
                  <br/>
                  total time
                </div>
              </div>
              <div class="col-md-4 text-center">
                <div class="well">
                  TODO
                  <br/>
                  success rate
                </div>
              </div>
            </div>

            <h2>Job Queue</h2>
            <p>
              TODO show the currently running jobs somewhere
            </p>

            <h2>Recent runs</h2>
            <p>
              TODO show the last X runs
            </p>


          </c:otherwise>
        </c:choose>


      </div>

    </div>

  </s:layout-component>
</s:layout-render>