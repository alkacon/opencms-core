<%@page import="org.opencms.workplace.tools.git.CmsGitCheckin\
				, java.util.Map\
				, java.io.BufferedReader\
				, java.io.FileReader" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
	CmsGitCheckin checkinBean = new CmsGitCheckin();
	checkinBean.init(pageContext, request, response);
	String module;
	Map<String,String[]> parameters = request.getParameterMap();
%>
<%!
	public void setCommonParameters(CmsGitCheckin checkinBean, Map<String,String[]>parameters) {

		checkinBean.setAutoPull(parameters.get("autopull") != null);
		checkinBean.setAutoPush(parameters.get("autopush") != null);
		checkinBean.setExcludeLibs(parameters.get("excludelibs") != null);
		checkinBean.setAutoCommit(parameters.get("autocommit") != null);
		if(parameters.get("commitmessage") != null && parameters.get("commitmessage").length > 0 && parameters.get("commitmessage")[0] != null) {
			checkinBean.setCommitMessage(parameters.get("commitmessage")[0]);
		}
	}
%>

<html>
<head>

</head>
<body>
<c:choose>
<c:when test='<%= parameters.get("all") != null || parameters.get("selected") != null %>'>
	<c:choose>
	<c:when test='<%= parameters.get("selected") != null %>'>
		<h1>Selected only some modules.</h1>
		<%
			String[] modules = parameters.get("module");
			if (modules != null) {
				for(int i=0; i < modules.length; i++) {
					checkinBean.addModuleToExport(modules[i]);
					%>
					<p>Chosen module: <%= modules[i] %></p>
					<%
				}
			}
		%>
	</c:when>
	<c:otherwise>
		<h1>All modules selected.</h1>
	</c:otherwise>
	</c:choose>
	<% 	
		setCommonParameters(checkinBean, parameters);
		int exitCode = checkinBean.checkIn();
	%>
	<c:choose>
	<c:when test="<%= exitCode == 0 %>">
		<h2 style="color: green;">Modules exported and checked in successfully.</h2>
	</c:when> 
	<c:otherwise>
		<h2 style="color: red;">Export and check in failed. Please consult the log file.</h2>
	</c:otherwise>
	</c:choose>
	<h2>
		Logfile output:
	</h2>
	<div style="width: 90%; height: 400px; border: 1px; border-style:solid; overflow:auto; white-space:nowrap;">
		<% 
			String logFilePath = checkinBean.getLogFilePath();
			BufferedReader br = new BufferedReader(new FileReader(logFilePath));

			String line = br.readLine();
			while(line!=null){
 				out.println(line + "<br>");
 				line = br.readLine();
			}
			br.close();
		%>
	</div>
</c:when>
<c:otherwise>
<h1>Checkin Modules into Git</h1>
	<h2>
		Current configuration
	</h2>
	<table>
		<tr>
			<td>Repository home:</td>
			<td><%= checkinBean.getRepositoryPath() %></td>
		</tr>
		<tr>
			<td>Module export folder</td>
			<td><%= checkinBean.getModuleExportPath() %></td>
		</tr>
		<tr>
			<td>Export mode:</td>
			<td><%= checkinBean.getExportMode() %></td>
		</tr>
	</table>
	
	<form action="<cms:link>${cms.requestContext.uri}</cms:link>" method="get">
		<fieldset>
		<legend>Module selection</legend>
		<c:forEach var="module" items="<%=checkinBean.getConfiguredModules() %>">
			<% module = (String) pageContext.getAttribute("module"); %>
			<div>
				<input type="checkbox" <%=checkinBean.isModuleInstalled(module)?"":" disabled=\"disabled\"" %> name="module" value="<%= module %>">
				<label><%= module %></label>
			</div>
		</c:forEach>
		</fieldset>
		<fieldset>
			<legend>Commit settings</legend>
			<div>
				<label>Commit message</label>
				<input type="text" name="commitmessage" value="<%= checkinBean.getDefaultCommitMessage() %>">
			</div>
			<div>
				<input type="checkbox" name="autocommit" <%= checkinBean.getDefaultAutoCommit()? "checked=checked" : "" %>>
				<label>Add and commit automatically</label>
			</div>
			<div>
				<input type="checkbox" name="autopull" <%= checkinBean.getDefaultAutoPull()? "checked=checked" : "" %>>
				<label>Pull first</label>
			</div>
			<div>
				<input type="checkbox" name="autopush" <%= checkinBean.getDefaultAutoPush()? "checked=checked" : "" %>>
				<label>Push automatically</label>
			</div>
		</fieldset>
		<fieldset>
			<legend>Further options</legend>
			<div>
				<input type="checkbox" name="excludelibs" <%= checkinBean.getDefaultExcludeLibs()? "checked=checked" : "" %>>
				<label>Exclude <code>lib/</code> subfolder</label>
			</div>
		</fieldset>
		<fieldset>
		<legend>Actions</legend>
		<button type="submit" name="all" value="on">Checkin all modules</button>
		<button type="submit" name="selected" value="on">Checkin selected modules</button>
		</fieldset>
	</form>
</c:otherwise>
</c:choose>

</body>
</html>