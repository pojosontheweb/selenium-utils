<%@ page import="com.pojosontheweb.tastecloud.model.RunSummary" %>
<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.pojosontheweb.tastecloud.Util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Run run = (Run)renderPropertyValue.getOwningObject();
    RunSummary summary = (RunSummary)renderPropertyValue.getPropertyValue();
%>
<div class="row run-summary">
    <div class="col-sm-3">
        <h3>Browser</h3>
        <img class="browsr" alt="browser" src="<%=request.getContextPath()%>/img/<%=summary.getBrowsr().name()%>.png"/>
    </div>
    <div class="col-sm-3">
        <h3>Queued</h3>
        <fmt:formatDate value="<%=summary.getQueuedOn()%>" type="date" dateStyle="short"/>
        <br/>
        <fmt:formatDate value="<%=summary.getQueuedOn()%>" type="time" dateStyle="full"/>
        <br/>
        <small><%=Util.prettyTime(summary.getQueuedOn(), request.getLocale())%></small>
    </div>
    <div class="col-sm-3">
        <h3>Started</h3>
        <%
            Date startedOn = summary.getStartedOn();
            if (startedOn!=null) {
        %>
                <fmt:formatDate value="<%=summary.getStartedOn()%>" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="<%=summary.getStartedOn()%>" type="time" dateStyle="full"/>
                <br/>
                <small><%=Util.prettyTime(summary.getStartedOn(), request.getLocale())%></small>
        <%
            }
        %>
    </div>
    <div class="col-sm-3">
        <h3>Finished</h3>
        <%
            Date finishedOn = run.getFinishedOn();
            if (finishedOn==null) {
                if (startedOn!=null) {
        %>
                    <img src="<%=request.getContextPath()%>/img/ajax-loader.gif" alt="loader"/>
        <%
                }
            } else {
        %>
                <fmt:formatDate value="<%=finishedOn%>" type="date" dateStyle="short"/>
                <br/>
                <fmt:formatDate value="<%=finishedOn%>" type="time" dateStyle="full"/>
                <br/>
                <small><%=Util.prettyTime(finishedOn, request.getLocale())%></small>
        <%
            }
        %>
    </div>
</div>
<%
    if (startedOn==null && finishedOn==null) {
        // queued
%>
        <w:objectKey var="runId" object="<%=run%>"/>
        <div class="run-queued">
            <h2>Run in queue</h2>
            <p>
                The run is in the queue. It will start automatically...
            </p>
        </div>
        <script type="text/javascript">
            $(function() {
                var poll = function() {
                    setTimeout(function() {
                        wokoClient.loadObject('Run', '${runId}', {
                            onSuccess: function(run) {
                                if (run.summary && run.summary.startedOn) {
                                    window.location.reload();
                                } else {
                                    poll();
                                }
                            }
                        })
                    }, 1000);
                };
                poll();
            });
        </script>

<%
    } else {
        if (finishedOn==null) {
            // started
%>
            <div class="logs">
                <h2>Logs</h2>
                <p>
                    Logs are updating live.
                </p>
                <div class="logs-wrapper" id="live-logs">
                </div>
            </div>
            <script type="text/javascript">
                <w:url var="logsFragmentUrl" facetName="logsFragment" object="<%=run%>"/>
                $(function() {
                    var poll = function() {
                        var timeout = setTimeout(function() {
                            clearTimeout(timeout);
                            $.get('${logsFragmentUrl}', function(frag) {
                                var rtInfo = $('#live-logs').html(frag).find('.rtinfo');
                                if (rtInfo.data('finished')) {
                                    window.location.reload();
                                } else {
                                    poll();
                                }
                            });
                        }, 1000);
                    };

                    poll();

                });
            </script>

<%
        } else {
            // finished
%>
            <w:objectKey var="runId" object="<%=run%>"/>
            <div class="logs">
                <h2>Logs</h2>
                <p>
                    Only the last XYZ log messages are shown.
                    Get <a href="#">full logs</a> (TODO)
                </p>
                <div class="logs-wrapper">
                    <w:includeFacet facetName="logsFragment" targetObject="<%=run%>"/>
                </div>
            </div>
<%
        }
    }
%>
