<%@ page import="woko.facets.builtin.RenderTabularListItemLinks" %>
<%@ page import="java.util.List" %>
<%@ page import="woko.facets.builtin.all.Link" %>
<%--
~ Copyright 2001-2012 Remi Vankeisbelck
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~       http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%
    RenderTabularListItemLinks renderLinks = (RenderTabularListItemLinks)request.getAttribute(RenderTabularListItemLinks.FACET_NAME);
    List<Link> links = renderLinks.getLinks();
%>
<div class="btn-group">
    <%
        for (Link l : links) {
            String css = l.getCssClass();
            if (css==null) {
                css = "btn btn-default";
            } else {
                css = css + " btn btn-default";
            }
            String attrs = l.getAttributesString();
    %>
    <a href="${pageContext.request.contextPath}/<%=l.getHref()%>"
       role="button" class="<%=css%>" <%=attrs%>>
        <c:out value="<%=l.getText()%>"/>
    </a>
    <%
        }
    %>
</div>