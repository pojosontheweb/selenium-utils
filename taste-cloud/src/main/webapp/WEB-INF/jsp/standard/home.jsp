<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.selenium.Browsr" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>

<c:set var="o" value="${home.facetContext.targetObject}"/>
<w:facet facetName="<%=WokoFacets.layout%>" targetObject="${o}"/>
<w:facet targetObject="${o}" facetName="<%=WokoFacets.renderTitle%>"/>

<fmt:message bundle="${wokoBundle}" var="pageTitle" key="woko.guest.home.pageTitle"/>
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="${pageTitle}">
    <s:layout-component name="body">

        <div class="container-fluid">

            <div class="dashboard">

                <c:choose>
                    <c:when test="${home.stats.nbSuccess+home.stats.nbFailure==0}">

                        <div class="page-header">
                            <h1>Welcome to Taste</h1>
                        </div>
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

                        <div class="page-header">
                            <h1>Cloud status</h1>
                        </div>

                        <div class="row stats">
                            <div class="col-xs-4 stat">
                                <h3>
                                    <span id="nbRunning"></span> /
                                        ${home.woko.objectStore.config.parallelJobs}
                                </h3>
                                <small>Running / Max jobs</small>
                            </div>
                            <div class="col-xs-4 stat">
                                <h3 id="nbQueued"></h3>
                                <small>Runs in queue</small>
                            </div>
                            <div class="col-xs-4 stat">
                                <h3 id="nbReposQueued"></h3>
                                <small>Repos in queue</small>
                            </div>
                        </div>

                        <div class="row stats">
                            <div class="col-xs-4 stat">
                                <h3 id="totalRuns"></h3>
                                <small>Total runs</small>
                            </div>
                            <div class="col-xs-4 stat">
                                <h3 id="totalTime"></h3>
                                <small>Total test time</small>
                            </div>
                            <div class="col-xs-4 stat">
                                <h3><span id="successRate"></span> %</h3>
                                <small>Success rate</small>
                            </div>
                        </div>

                        <h3>Activity stream</h3>
                        <table class="table table-condensed">
                            <tbody id="stream"></tbody>
                        </table>

                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script type="text/javascript">
            $(function () {
                function update() {
                    $.get('${cp}/home?activitiesFragment', function (frag) {
                        var rtInfo = $('#stream').html(frag).find('.rtinfo');
                        var nbRunning = rtInfo.data('nb-running');
                        var nbSubmitted = rtInfo.data('nb-submitted');
                        var nbQueued = nbSubmitted - nbRunning;
                        $('#nbRunning').text(nbRunning);
                        $('#nbQueued').text(nbQueued);
                        $('#totalRuns').text(rtInfo.data('total-runs'));
                        $('#totalTime').text(rtInfo.data('total-time'));
                        $('#successRate').text(rtInfo.data('success-rate'));
                        $('#nbReposQueued').text(rtInfo.data('nb-repos-queued'));
                        setTimeout(update, 1000);
                    });
                }

                update();
            });
        </script>

    </s:layout-component>
</s:layout-render>