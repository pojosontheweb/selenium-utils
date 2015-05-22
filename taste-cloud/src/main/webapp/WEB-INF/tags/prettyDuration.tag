<%@ tag import="com.pojosontheweb.tastecloud.Util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="millis" required="true" type="java.lang.Long" %>
<%=Util.prettyDuration(millis)%>