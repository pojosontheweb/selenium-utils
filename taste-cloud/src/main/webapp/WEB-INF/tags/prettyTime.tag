<%@ tag import="com.pojosontheweb.tastecloud.Util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="date" required="true" type="java.util.Date" %>
<%=Util.prettyTime(date, request.getLocale())%>