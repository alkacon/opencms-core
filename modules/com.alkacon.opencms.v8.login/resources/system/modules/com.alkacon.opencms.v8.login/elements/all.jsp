<%@page buffer="none" session="false" taglibs="c,cms,fmt,fn" import="org.opencms.main.*, org.opencms.file.*"%>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.login.messages">
<cms:formatter var="content" val="value">

<c:set var="boxschema"><cms:elementsetting name="boxschema" default="box_schema1" /></c:set>
<div class="box ${boxschema}">

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

<%-- Title of the login box--%>
<h4>${value.Title}</h4>
	
<div class="boxbody">
<c:choose>
	<c:when test="${empty param.requestedResource}">
		<c:set var="path">${cms.requestContext.uri}</c:set>
	</c:when>
	<c:otherwise>
		<c:set var="path">${param.requestedResource}</c:set>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${!login.loggedIn}">
		<p>${value.Text}</p>
	</c:when>
	<c:otherwise>
		<p><b><fmt:message key="login.message.loggedin" />:</b></p>
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
	</c:otherwise>
	</c:choose>
		<c:if test="${!login.loginSuccess}">
			<div class="login-errormessage">
				<fmt:message key="login.message.failed" />:<br />
				${login.loginException.localizedMessage}
			</div>
		</c:if>
		<form method="get" action="<cms:link>${path}</cms:link>" class="loginform">
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

</div>
</div>
</cms:formatter>
</fmt:bundle>