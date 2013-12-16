<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="cloneModule" scope="request" class="org.opencms.workplace.tools.modules.CmsCloneModule">
	<jsp:setProperty name="cloneModule" property="*" />
</jsp:useBean>
<c:if test="${param.submit}">
	<div id="success">
	<%
		Throwable error = null;
		try {
		    cloneModule.init(pageContext, request, response);
			cloneModule.executeModuleClone();
		} catch (Throwable t) {
		    error = t;
		    if (cloneModule.success()) {
			    %><h2>Module: <c:out value="${param.packageName}" escapeXml="true" /> has been created successfully!</h2><%
		    } else {
			    %><h2>Oops module: <c:out value="${param.packageName}" escapeXml="true" /> not created successfully!</h2><%
		    }
		}
		if (error == null) {
		    %><div><h2>Module: <c:out value="${param.packageName}" escapeXml="true" /> created successfully!</h2></div><%
		} else {
		    %><p>The error message was: <%= error.getLocalizedMessage() %></p>
		    <p>Please have a look at the opencms.log for more info.</p><%
		}
	%>
	</div>
</c:if>
