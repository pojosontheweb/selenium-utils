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

            <div class="page-header">
                <h1>Dashboard</h1>
            </div>

            <div class="dashboard">

                <c:choose>
                    <c:when test="${home.stats.totalRuns==0}">
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
                            <div class="col-md-3">
                                <div class="well text-center">
                                    <span id="nbRunning">...</span> running,
                                    <span id="nbQueued">...</span> tastes in queue,
                                    <span id="nbReposQueued">...</span> repos in queue
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="well text-center">
                                    <span id="totalRuns">...</span>
                                    total runs
                                </div>
                            </div>
                            <div class="col-md-3 text-center">
                                <div class="well">
                                    <span id="totalTime">...</span>
                                    total time
                                </div>
                            </div>
                            <div class="col-md-3 text-center">
                                <div class="well">
                                    <span id="successRate">...</span>
                                    success rate
                                </div>
                            </div>
                        </div>

                        <h2>Live activity stream</h2>
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