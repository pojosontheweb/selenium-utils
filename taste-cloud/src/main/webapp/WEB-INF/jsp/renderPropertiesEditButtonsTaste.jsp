<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="taste" value="${renderPropertiesEditButtons.facetContext.targetObject}"/>
<div class="col-lg-offset-2 col-md-offset-3 col-sm-offset-3">
  <s:submit name="save" class="btn btn-primary"/>
  <a href="#" class="btn btn-default" id="saveAndRun">Save and run (TODO)</a>
</div>
