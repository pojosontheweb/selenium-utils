<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<div class="page-header">
  <h1>${renderListTitle.title}</h1>
</div>
<p>
  Repositories can be defined and used by taste-cloud for running tests.
  They should contain .taste files, as well as a taste-cloud config file
  (optional - TODO). Webhooks can be used in order to trigger builds
  automatically.
</p>
<a class="btn btn-primary" href="${cp}/edit/Repository?createTransient=true">Add repository</a>
<h2>Registered repos <small>(${list.results.totalSize})</small></h2>