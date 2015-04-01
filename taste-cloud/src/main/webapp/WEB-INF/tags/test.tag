<%@ tag import="java.io.BufferedReader" %>
<%@ tag import="java.io.StringReader" %>
<%@ tag import="java.net.URLEncoder" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="test" required="true" type="com.pojosontheweb.tastecloud.model.Test" %>
<%@ attribute name="run" required="true" type="com.pojosontheweb.tastecloud.model.Run" %>
<div class="test">
<%
    boolean success = test.getSuccess()!=null && test.getSuccess();
%>
<% if (success) { %>
<span class="label label-success">Passed</span>
<% } else { %>
<span class="label label-danger">Failed</span>
<% }%>
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
<%
    String taste_stack_done = "__taste_stack_done";
    Boolean scriptedAlready = (Boolean)request.getAttribute(taste_stack_done);
    if (scriptedAlready==null) {
        request.setAttribute(taste_stack_done, true);
%>
<script type="text/javascript">
    $(function() {
        $('.stack').click(function(e) {
            var p = $(this).parent().parent();
            p.find('.stack-wrapper').slideToggle();
            e.preventDefault();
        })
    })
</script>
<%
    }
%>
</div>