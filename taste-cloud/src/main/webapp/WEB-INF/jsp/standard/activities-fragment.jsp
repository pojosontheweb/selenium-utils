<%@ page import="com.pojosontheweb.tastecloud.facets.standard.Home" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.ActivityBase" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.TasteRunActivity" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.RepoRunActivity" %>
<%@ page import="com.pojosontheweb.tastecloud.woko.TasteStore" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Stats" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%
  Home home = (Home)request.getAttribute("home");
  TasteStore store = (TasteStore)home.getFacetContext().getWoko().getObjectStore();
  Stats stats = store.getStats();
  for (ActivityBase a : home.getActivities()) {
%>
  <tr>
    <td>
      <fmt:formatDate value="<%=a.getTstamp()%>" type="both" timeStyle="full"/>
    </td>
    <td>
      <%=a.getType()%>
    </td>
    <td>
      <% if (a instanceof TasteRunActivity) {
        TasteRunActivity ta = (TasteRunActivity)a;
      %>
      <a href="${cp}/view/Taste/<%=ta.getTasteId()%>">
        <c:out value="<%=ta.getTasteName()%>"/>
      </a>
      <% } else {
        RepoRunActivity ra = (RepoRunActivity)a;
      %>
      <a href="${cp}/view/Repository/<%=ra.getRepoRunId()%>">
        <c:out value="<%=ra.getRepoName()%>"/>
      </a>
      <% } %>
    </td>
    <td>
      <img class="browsr" alt="browser" src="/taste-cloud/img/<%=a.getBrowsr().name()%>.png">
    </td>
  </tr>
<%
  }
%>
<tr style="display: none;"
    class="rtinfo"
    data-nb-running="<%=stats.getNbRunning()%>"
    data-nb-submitted="<%=stats.getNbSubmitted()%>"
        data-total-runs="<%=stats.getTotalRuns()%>"
        data-total-time="<%=stats.getTotalTime()%>"
        data-success-rate="<%=stats.getSuccessRate()%>"></tr>




