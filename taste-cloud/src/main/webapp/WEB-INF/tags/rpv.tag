<%@ tag import="woko.util.Util" %>
<%@ tag import="woko.Woko" %>
<%@ tag import="woko.facets.builtin.RenderPropertyValue" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="owningObject" required="true" type="java.lang.Object" %>
<%@ attribute name="propertyName" required="true" type="java.lang.String" %>
<%@ attribute name="propertyValue" required="true" type="java.lang.Object" %>
<%
    Woko woko = Woko.getWoko(application);
    RenderPropertyValue rpv = Util.getRenderPropValueFacet(woko, request, owningObject, propertyName, propertyValue);
%>
<jsp:include page="<%=rpv.getFragmentPath(request)%>"/>