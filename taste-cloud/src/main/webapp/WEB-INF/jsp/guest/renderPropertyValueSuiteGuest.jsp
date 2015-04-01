<%@ page import="woko.facets.builtin.RenderPropertyValue" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Suite" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Test" %>
<%@ page import="com.pojosontheweb.tastecloud.model.Run" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="com.pojosontheweb.tastecloud.model.SuiteCounts" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<%
    RenderPropertyValue renderPropertyValue = (RenderPropertyValue)request.getAttribute(RenderPropertyValue.FACET_NAME);
    Suite suite = (Suite)renderPropertyValue.getPropertyValue();
%>
<table class="results table table-bordered">
    <tr>
        <td style="white-space: nowrap;">
                <w:title object="<%=suite%>"/>
        </td>
        <td>
            <%
                SuiteCounts counts = suite.getCounts();
                double ratio = Math.ceil(counts.getRatio() * 100) / 100;
            %>
                <span class="label label-success"><%=counts.getNbSuccess()%></span>
                <% if (ratio==100) { %>
                    <span class="label label-success"><%=ratio%> %</span>
                <% } else if (ratio==0) { %>
                    <span class="label label-danger"><%=counts.getNbFailed()%></span>
                    <span class="label label-danger"><%=ratio%> %</span>
                <% } else { %>
                    <span class="label label-danger"><%=counts.getNbFailed()%></span>
                    <span class="label label-warning"><%=ratio%> %</span>
                <% }%>
                <span class="label label-info"><%=counts.getTotal()%></span>
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
                    <%
                        String err = test.getErr();
                        if (err!=null) {
                            String firstLine = new BufferedReader(new StringReader(err)).readLine();
                    %>
                            <span class="err-line text-danger">
                                <c:out value="<%=firstLine%>"/>
                            </span>
                        <% } %>
                    <div class="err-links">
                        <a href="#" class="btn btn-default stack">
                            Stack
                        </a>
                        <%
                            Run run = (Run)renderPropertyValue.getOwningObject();
                            String videoPath = "/results/Run/" + run.getId() + "?facet.file="
                                + URLEncoder.encode(test.getName(), "utf-8") + ".mov&download=true";
                        %>
                        <a class="btn btn-default" href="${cp}<%=videoPath%>">
                            Video
                        </a>
                    </div>
                    <div class="stack-wrapper" style="display: none;">
                        <%
                            String stack = test.getStack();
                            StringBuilder res = new StringBuilder();
                            if (stack!=null) {
                                BufferedReader r = new BufferedReader(new StringReader(stack));
                                String line;
                                while ((line=r.readLine())!=null) {
                                    String css = "stack-line";
                                    if (line.contains("tests.taste:")) {
                                        css += " hl";
                                        line = "<a href='#'>" + line + "</a>";
                                    }
                                    res.append("<div class=\"").append(css).append("\">")
                                            .append(line)
                                            .append("</div>");
                                }
                            }
                        %>
                        <%=res.toString()%>
                    </div>
                <% } %>
            </td>
        </tr>
    <% } %>
</table>
<script type="text/javascript">
    $(function() {
        $('.stack').click(function(e) {
            var p = $(this).parent().parent();
            p.find('.stack-wrapper').slideToggle();
            e.preventDefault();
        })
    })
</script>
