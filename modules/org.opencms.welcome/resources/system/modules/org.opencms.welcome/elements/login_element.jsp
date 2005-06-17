<%@ page session="false" import="org.opencms.jsp.*" %><%

// get required OpenCms objects
CmsJspLoginBean cms = new CmsJspLoginBean(pageContext, request, response);

// read parameters from the request
String username = request.getParameter("username");
String password = request.getParameter("password");
String action = request.getParameter("action");
boolean hideLoginForm = Boolean.valueOf(request.getParameter("hideloginform")).booleanValue();

// read properties
String login_project = cms.property("login_project", "search");
String login_redirect = cms.property("login_redirect", "search");

// now process the login data
if ("logout".equals(action)) {
	cms.logout();
} else if ("login".equals(action)) {
	cms.login(username, password, login_project, login_redirect);
}

if (! cms.isLoggedIn() && ! hideLoginForm) {
// current user is not logged in - display the login form

	String message = "Please login:";
	if (! cms.isLoginSuccess()) {
		// previous login attempt was not successful
		message = "Login failed, please try again:";
	}
%>
<form action="<%= cms.getFormLink() %>" method="POST">

<table border="0" cellpadding="2" cellspacing="0">
<tr><td colspan="2"><%= message %></td><tr>
<tr><td>Username:</td><td><input name="username" size="25" value="<%= username!=null?username:"" %>"></td></tr>
<tr><td>Password:</td><td><input name="password" size="25" type="password"></td></tr>
<tr><td><input type="submit" value="Login"></td><td><input type="hidden" name="action" value="login"></td></tr>
</table>

</form>
<%

} else if (! hideLoginForm) {

// current user is already logged in - display the logout form
%>
<form action="<%= cms.getFormLink() %>" method="POST">

<table border="0" cellpadding="2" cellspacing="0">
<tr><td>You are logged in as: <%= cms.getUserName() %></tr>
<tr><td><input type="submit" value="Logout"><input type="hidden" name="action" value="logout"></td></tr>
</table>

</form>
<%
}
%>