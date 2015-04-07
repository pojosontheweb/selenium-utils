<%--
~ Copyright 2001-2013 Remi Vankeisbelck
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~       http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="java.util.Locale" %>
<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.selenium.Browsr" %>

<w:username var="username"/>
<c:set var="cp" value="${pageContext.request.contextPath}" scope="request"/>
<w:cacheToken paramName="cacheToken" tokenValue="cacheTokenValue"/>
<c:set var="cacheTokenParams" value="${cacheToken}=${cacheTokenValue}" scope="request"/>
<s:layout-definition>
  <!DOCTYPE html>
  <html>
  <head>
      <%-- Add the woko favicon --%>
    <link rel="shortcut icon" href="${cp}/favicon.ico?${cacheTokenParams}" />
      <%-- Needed by bootstrap to be responsive --%>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="utf-8">
    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js?${cacheTokenParams}"></script>
    <![endif]-->

      <%-- Display the pageTitle set in jsp's if any --%>
    <c:choose>
      <c:when test="${not empty pageTitle}">
        <title>${layout.appTitle} - ${pageTitle}</title>
      </c:when>
      <c:otherwise>
        <title>${layout.appTitle}</title>
      </c:otherwise>
    </c:choose>

      <%--  Import stylesheet
              - CSS from layout facet
              - CustomCSS
      ========================================================== --%>
    <c:forEach items="${layout.cssIncludes}" var="cssLink">
      <link rel="stylesheet" href="${cp}${cssLink}?${cacheTokenParams}" type="text/css">
    </c:forEach>
    <s:layout-component name="customCss"/>


      <%--  Import javascript
              - JQuery
              - Bootstrap
              - Bootstrap datepicker
              - Woko
              - JS from layout facet
              - CustomJS
      ========================================================== --%>

    <c:forEach items="${layout.jsIncludes}" var="jsLink">
      <script type="text/javascript" src="${cp}${jsLink}?${cacheTokenParams}"></script>
    </c:forEach>
    <link rel="stylesheet" type="text/css" href="http://eclipse.org/orion/editor/releases/current/built-editor.css"/>
    <script src="http://eclipse.org/orion/editor/releases/current/built-editor.min.js"></script>
    <script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js"></script>

      <%-- Set the locale to the datepicker --%>
    <% Locale l = request.getLocale(); %>
    <script type="text/javascript">
      $(document).ready(function() {
        $('input[rel="datepicker"]').datepicker({language: "<%=l%>"});
      });
    </script>

    <% if (l.toString().equals("fr")) { %>
    <script type="text/javascript" src="${cp}/js/bootstrap3-datepicker/bootstrap-datepicker.fr.min.js?${cacheTokenParams}"></script>
    <% } %>

    <link rel="stylesheet" href="${cp}/js/bootstrap3-datepicker/bootstrap-datepicker.css" type="text/css">

    <script type="text/javascript">
      window.wokoClient = new woko.rpc.Client("${cp}");
    </script>

    <s:layout-component name="customJs"/>
  </head>

  <body>

  <div class="navbar navbar-inverse navbar-static-top" role="navigation">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="${cp}/home">${layout.appTitle}</a>
      </div>
      <div class="collapse navbar-collapse">
        <ul class="nav navbar-nav">
          <w:includeFacet facetName="<%=WokoFacets.navBar%>" targetObject="${layout.facetContext.targetObject}"/>
        </ul>

          <%-- Display user/connexion info --%>
        <c:if test="${skipLoginLink==null}">
          <ul class="nav navbar-nav navbar-right">
            <li>
              <p class="navbar-text">
                <c:choose>
                  <c:when test="${username != null}">
                    <strong>${username}</strong> -
                    <a href="${cp}/logout"><fmt:message bundle="${wokoBundle}" key="woko.layout.logout"/> </a>
                  </c:when>
                  <c:otherwise>
                    <fmt:message bundle="${wokoBundle}" key="woko.layout.notLogged"/>
                    <a href="${cp}/login"><fmt:message bundle="${wokoBundle}" key="woko.layout.login"/> </a>
                  </c:otherwise>
                </c:choose>
              </p>
            </li>
          </ul>
        </c:if>
      </div>
    </div>
  </div>

    <%-- messages/errors --%>
  <s:messages/>
  <s:errors/>

    <%-- body --%>
  <s:layout-component name="body"/>

  <div class="modal run" id="runModal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                  aria-hidden="true">&times;</span></button>
          <h4 class="modal-title">Run <span class="name"></span></h4>
        </div>
        <div class="modal-body">
          <p>Select a browser and submit</p>

          <select class="image-picker show-html show-labels">
            <option value=""></option>
            <%
              for (Browsr b : Browsr.values()) {
            %>
            <option selected data-img-src="${cp}/img/<%=b.name()%>.png" value="<%=b.name()%>"><%=b.name()%></option>
            <%
              }
            %>
          </select>

        </div>
        <div class="modal-footer">
          <form action="" method="POST">
            <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            <button type="submit" class="btn btn-primary">Go</button>
          </form>
        </div>
      </div>
      <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
  </div>
  <!-- /.modal -->

  <script type="text/javascript">
    $(function () {
      $('#runModal').on('shown.bs.modal', function (e) {
        var select = $("#runModal select");
        select.imagepicker({
//          show_label: true
        });
        var button = $(e.relatedTarget);
        $('#runModal .name').text(button.data('taste-name'));
        var tasteId = button.data('taste-id');

        var setRunAction = function() {
          var runUrl = '${cp}/run/Taste/' + tasteId + "?facet.browsr=" + select.val();
          $('#runModal .modal-footer form').attr('action', runUrl);
        };
        select.change(setRunAction);

        setRunAction();
      });
    });
  </script>

  </body>
  </html>
</s:layout-definition>