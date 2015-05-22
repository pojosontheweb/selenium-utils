<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
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
      <title>Initial config</title>
        <link rel="stylesheet" href="${cp}/bootstrap-3.1.1-dist/css/bootstrap.min.css?${cacheTokenParams}" type="text/css">
        <link rel="stylesheet" href="${cp}/css/woko.css?${cacheTokenParams}" type="text/css">
      <link rel="stylesheet" href="${cp}/css/taste.css?${cacheTokenParams}" type="text/css">

      <script type="text/javascript" src="${cp}${jsLink}?${cacheTokenParams}"></script>
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
        <a class="navbar-brand" href="${cp}/home">taste-cloud</a>
      </div>
      <div class="collapse navbar-collapse">
        <ul class="nav navbar-nav">
        </ul>
      </div>
    </div>
  </div>

    <%-- messages/errors --%>
  <s:messages/>
  <s:errors/>

    <%-- body --%>
  <s:layout-component name="body"/>

  </body>
  </html>
</s:layout-definition>