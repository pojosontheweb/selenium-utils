<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="text" value="${renderPropertyValue.propertyValue}"/>
<h2>Taste</h2>
<p>
  Here's the taste script that was used for this run.
</p>
<pre class="prettyprint taste-script"><c:out value="${text}"/></pre>