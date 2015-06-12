<%@ page import="com.pojosontheweb.tastecloud.facets.standard.Home" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.ActivityBase" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.TasteRunActivity" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.RepoRunActivity" %>
<%@ page import="com.pojosontheweb.tastecloud.woko.TasteStore" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Stats" %>
<%@ page import="com.pojosontheweb.tastecloud.model.activities.ActivityType" %>
<%@ page import="com.pojosontheweb.tastecloud.model.RepositoryRun" %>
<%@ page import="com.pojosontheweb.tastecloud.model.ResultSummary" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="com.pojosontheweb.tastecloud.Util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<%
    Home home = (Home) request.getAttribute("home");
    TasteStore store = (TasteStore) home.getFacetContext().getWoko().getObjectStore();
    Stats stats = store.getStats();
    for (ActivityBase a : home.getActivities()) {
%>
<tr>

    <%--
      Date
    --%>
    <td>
        <%=Util.prettyTime(a.getTstamp(), request.getLocale())%>
    </td>

    <%--
      Browser
    --%>
    <td>
        <img class="browsr" alt="browser" src="<%=request.getContextPath()%>/img/<%=a.getBrowsr().name()%>.png">
    </td>

    <%
        if (a instanceof TasteRunActivity) {
            TasteRunActivity ta = (TasteRunActivity) a;
    %>

        <%--
          Run
        --%>
        <td>
            <a href="${cp}/edit/Taste/<%=ta.getTasteId()%>">
                <c:out value="<%=ta.getTasteName()%>"/>
            </a>
            /
            <a href="${cp}/view/Run/<%=ta.getRunId()%>">
                <c:out value="<%=ta.getRelativePath()%>"/>
            </a>
        </td>
        <td>
            <%
                ActivityType type = a.getType();
                if (type == ActivityType.Finish) {
                    Run run = store.getRun(a.getRunId());
                    ResultSummary s = run!=null ? run.getResultSummary() : null;
                    if (s==null || s.getSuccessRatio()==null) {
                        // BUG !
            %>
            <span class="label label-warning">BUG !</span>
            <%

                    } else {
                        String ratioClass = s.isSuccess() ? "label-success" : "label-danger";
            %>
                        <span class="label label-success"><%=s.getNbSuccess()%></span>
                        <% if (s.getNbFailed() > 0) { %>
                            <span class="label label-danger"><%=s.getNbFailed()%></span>
                        <% }%>
                        <span class="label <%=ratioClass%>"><%=s.getSuccessRatio()%> %</span>
                        <span class="label label-info"><%=s.getElapsed()%> s</span>
            <%
                    }
                } else { %>
                    <span class="label label-info"><%=type.name()%></span>
            <%
                }
            %>
        </td>

    <% } else {
        RepoRunActivity ra = (RepoRunActivity) a;
        ActivityType type = ra.getType();
    %>

        <%--
          Repo Run
        --%>

        <td>
            <a href="${cp}/view/RepositoryRun/<%=ra.getRepoRunId()%>">
                <c:out value="<%=ra.getRepoName()%>"/>
            </a>
            <%
                if (ra.getRelativePath() != null) {
            %>
                /
                <a href="${cp}/view/Run/<%=ra.getRunId()%>">
                    <c:out value="<%=ra.getRelativePath()%>"/>
                </a>
            <%
                }
            %>
        </td>
        <td>
            <%
                if (type == ActivityType.Finish) {
                    RepositoryRun rr = store.getRepositoryRun(ra.getRepoRunId());
                    if (ra.getRunId() != null) {
                        Run r = store.getRun(ra.getRunId());
                        ResultSummary s = r!=null ? r.getResultSummary() : null;
                        if (s!=null && s.getSuccessRatio()==null) {
            %>
                            <span class="label label-warning">BUG !</span>
            <%
                        } else {
                            String ratioClass = s.isSuccess() ? "label-success" : "label-danger";
            %>

                            <span class="label label-success"><%=s.getNbSuccess()%></span>
                            <% if (s.getNbFailed() > 0) { %>
                                <span class="label label-danger"><%=s.getNbFailed()%></span>
                            <% }%>
                            <span class="label <%=ratioClass%>"><%=s.getSuccessRatio()%> %</span>
                            <span class="label label-info"><%=s.getElapsed()%> s</span>

            <%
                        }
                    } else {
                        ResultSummary s = rr.getResultSummary();
                        String ratioClass = s.isSuccess() ? "label-success" : "label-danger";
            %>

                        <span class="label label-success"><%=s.getNbSuccess()%></span>
                        <% if (s.getNbFailed() > 0) { %>
                            <span class="label label-danger"><%=s.getNbFailed()%></span>
                        <% }%>
                        <span class="label <%=ratioClass%>"><%=s.getSuccessRatio()%> %</span>
                        <span class="label label-info"><%=s.getElapsed()%> s</span>
            <%
                    }
                } else {
            %>
                    <span class="label label-info"><%=type.name()%></span>
            <%
                }
            %>
        </td>
    <% } %>
</tr>
<%
    }
%>
<tr style="display: none;"
    class="rtinfo"
    data-nb-running="<%=stats.getNbRunning()%>"
    data-nb-submitted="<%=stats.getNbTastesSubmitted()%>"
    data-total-runs="<%=stats.getTotalRuns()%>"
    data-total-time="<%=Util.prettyDuration(stats.getTotalTime())%>"
    data-success-rate="<%=stats.getSuccessRate()!=null ? stats.getSuccessRate() : 0d%>"
    data-nb-repos-queued="<%=stats.getNbReposQueued()%>"></tr>




