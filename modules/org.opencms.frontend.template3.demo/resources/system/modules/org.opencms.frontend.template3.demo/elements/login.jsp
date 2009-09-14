<%@ page import="org.opencms.main.*, org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.frontend.template3.demo.messages">

<jsp:useBean id="login" class="org.opencms.jsp.CmsJspLoginBean" scope="page">
	<% 
		login.init (pageContext, request, response);
	%> 
	<c:if test="${param.action eq 'login' && !empty param.name && !empty param.password}">
		<% login.login ((String)request.getParameter("name"), (String)request.getParameter("password"), "Offline", (String)request.getParameter("requestedResource")); %>
	</c:if>
	<c:if test="${param.action eq 'logoff'}">
	<% 
		login.logout();
	%>
	</c:if>
</jsp:useBean> 

<c:choose>
	<c:when test="${!login.loggedIn}">
		<p><fmt:message key="login.message.enterdata" /></p>
		<c:if test="${!login.loginSuccess}">
			<div class="login-errormessage">
				<fmt:message key="login.message.failed" />:<br />
				${login.loginException.localizedMessage}
			</div>
		</c:if>
		<form method="get" action="<cms:link>${param.path}</cms:link>" class="loginform">
			<div class="boxform">
				<label for="name"><fmt:message key="login.label.username" />:</label>
				<input type="text" name="name">
			</div>
			<div class="boxform">
				<label for="password"><fmt:message key="login.label.password" />:</label>
				<input type="password" name="password">
			</div>
			<div class="boxform">
				<input type="hidden" name="action" value="login" />
				<input type="hidden" name="requestedResource" value="${param.requestedResource}" />
				<input class="button" type="submit" value="<fmt:message key="login.label.login" />"/>
			</div>
		</form>
	</c:when>
	<c:otherwise>
		<p><b><fmt:message key="login.message.loggedin" />:</b></p>
		<form method="get" action="<cms:link>${param.path}</cms:link>" class="loginform">
			<div class="boxform">
				<c:set var="firstname"><cms:user property="firstname"/></c:set>
				<c:set var="lastname"><cms:user property="lastname"/></c:set>
				<c:if test="${not empty firstname}">${firstname}&nbsp;</c:if><c:if test="${not empty lastname}">${lastname}</c:if>
				<c:set var="username"><cms:user property="name"/></c:set>
				<c:if test="${empty firstname && empty lastname}">
					<c:if test="${fn:indexOf(username, '/') != -1}">
						<c:set var="username">${fn:substringAfter(username, '/')}</c:set>
					</c:if>
					(${username})
				</c:if>
			</div>
			<div class="boxform">
				<input type="hidden" name="action" value="logoff" />
				<input type="hidden" name="requestedResource" value="${cms:vfs(pageContext).requestContext.uri}" />
				<input class="button" type="submit" value="<fmt:message key="login.label.logoff" />"/>
			</div>
		</form>
	</c:otherwise>

</c:choose>

</fmt:bundle>