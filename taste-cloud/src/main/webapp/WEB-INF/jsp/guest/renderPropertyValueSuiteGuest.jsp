<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Suite" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Test" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Suite suite = (Suite)renderPropertyValue.getPropertyValue();
%>
<table class="table table-bordered">
    <tr>
        <td style="white-space: nowrap;">
                <w:title object="<%=suite%>"/>
        </td>
        <td>
                <c:set var="counts" value="<%=suite.getCounts()%>"/>
                <span class="label label-success">${counts.nbSuccess}</span>
                <span class="label label-danger">${counts.nbFailed}</span>
                <span class="label label-default">${counts.total}</span>
                <span class="label label-warning">${counts.ratio} %</span>
                <span class="label label-info">
                    <i class="glyphicon glyphicon-time"> </i>
                    <%=suite.getElapsed()%> s
                </span>
        </td>
    </tr>
    <% for (Test test : suite.getTestResults()) { %>

        <tr>
            <td style="white-space: nowrap;">
                &nbsp;- <w:title object="<%=test%>"/>
            </td>
            <td>
                <% boolean success = test.getSuccess()!=null && test.getSuccess(); %>
                <% if (success) { %>
                    <span class="label label-success">Passed</span>
                <% } else { %>
                    <span class="label label-danger">Failed</span>
                <% } %>
                <span class="label label-info">
                    <i class="glyphicon glyphicon-time"> </i>
                    <%=test.getElapsed()%> s
                </span>
                <% if (!success) { %>
                    <div class="error">
                        <c:out value="<%=test.getErr()%>"/>
                    </div>
                    <div class="err-links">
                        <a href="#" class="stack">
                            Stack
                        </a>
                        |
                        <%
                            Run run = (Run)renderPropertyValue.getOwningObject();
                            String videoPath = "/results/Run/" + run.getId() + "?facet.file="
                                + URLEncoder.encode(test.getName(), "utf-8") + ".mov&download=true";
                        %>
                        <a href="${cp}<%=videoPath%>">
                            Video
                        </a>
                    </div>
                    <div class="stack-wrapper" style="display: none;">
                        <pre><code><%=test.getStack()%></code></pre>
                    </div>
                <% } %>
            </td>
        </tr>
    <% } %>
</table>
<script type="text/javascript">
    $(function() {
        $('.stack').click(function(e) {
            $(this).parent().parent().find('.stack-wrapper').slideToggle();
            e.preventDefault();
        })
    })
</script>
