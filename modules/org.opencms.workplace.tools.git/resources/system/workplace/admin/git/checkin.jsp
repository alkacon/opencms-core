<%@page import="org.opencms.workplace.tools.git.CmsGitCheckin\
				, java.util.Map\
				, java.io.BufferedReader\
				, java.io.FileReader, org.opencms.jsp.*,org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
	CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response); 
	CmsGitCheckin checkinBean = new CmsGitCheckin(jsp.getCmsObject());
	String module;
	Map<String,String[]> parameters = request.getParameterMap();
%>
<%!
	public void setCommonParameters(CmsGitCheckin checkinBean, Map<String,String[]>parameters) {

		checkinBean.setPullBefore(parameters.get("pullfirst") != null);
		checkinBean.setPullAfter(parameters.get("pullafter") != null);
		checkinBean.setPush(parameters.get("autopush") != null);
		checkinBean.setExcludeLibs(parameters.get("excludelibs") != null);
		checkinBean.setCommit(parameters.get("autocommit") != null);
		checkinBean.setIgnoreUnclean(parameters.get("ignoreunclean") != null);
		checkinBean.setCopyAndUnzip(parameters.get("copyandunzip") != null);
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
<c:when test='<%= checkinBean.isConfigFileReadable() %>'>
	<c:choose>
	<c:when test='<%= parameters.get("all") != null || parameters.get("selected") != null || parameters.get("resetHead") != null || parameters.get("resetRemoteHead") != null %>'>
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
					String[] newmodules = parameters.get("newmodule");
					if (newmodules != null && newmodules.length > 0 && ! newmodules[0].trim().isEmpty()) {
				    	checkinBean.addModuleToExport(newmodules[0]);
				    	%>
						<p>Adding new module: <%= newmodules[0] %></p>
						<%
					}
				%>
			</c:when>
			<c:when test='<%= parameters.get("all") != null %>'>
				<h1>All modules selected.</h1>
				<%
					String[] newmodules = parameters.get("newmodule");
					if (newmodules != null && newmodules.length > 0 && ! newmodules[0].trim().isEmpty()) {
						for (String m : checkinBean.getConfiguredModules()) {
						    checkinBean.addModuleToExport(m);
						    %>
							<p>Check-in module: <%= m %></p>
							<%
						}
				    	checkinBean.addModuleToExport(newmodules[0]);
				    	%>
						<p>Also adding new module: <%= newmodules[0] %></p>
						<%
					}
				%>
			</c:when>
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
				<c:choose>
				<c:when test='<%= exitCode == 10 %>'>
					<h2 style="color: red;">Export and check in failed because of an unclean repository. Please consult the log file.</h2>
					<p>
						<a href="<cms:link>${cms.requestContext.uri}?resetHead</cms:link>">Reset local repository to HEAD. You loose uncommitted changes but get a clean repository.</a>
					</p>
				</c:when>
				<c:otherwise>
					<h2 style="color: red;">Export and check in failed. Please consult the log file.</h2>
				</c:otherwise>
				</c:choose>
				<p>
					<a href="<cms:link>${cms.requestContext.uri}?resetRemoteHead</cms:link>">Reset local repository to the head of the remote branch for conflict resolving. You lose all local changes, even committed, but unpushed ones.</a>
				</p>
			</c:otherwise>
			</c:choose>
		</c:when>
		<c:when test='<%= parameters.get("resetHead") != null %>'>
			<h1>Reset local repository to HEAD</h1>
			<% 
				checkinBean.setResetHead(true);
				int exitCode = checkinBean.checkIn();
			%>
			<c:choose>
			<c:when test='<%= exitCode == 0 %>'>
				<h2 style="color: green;">The repository was reset.</h2>
				<p>You can repeat your commit with the "Pull first" option checked. This should avoid conflicts</p>
				<p style="color: orange;"><strong>WARNING:</strong>Be aware that you may overwrite changes from the remote repository.</p>
			</c:when>
			<c:otherwise>
				<h2 style="color: red;">Reset failed. Please consult the log file.</h2>
				<p style="color: orange;">You may have an incorrect configuration or you have to manually resolve a GIT conflict.</p>
			</c:otherwise>
			</c:choose>
		</c:when>
		<c:when test='<%= parameters.get("resetRemoteHead") != null %>'>
			<h1>Reset local repository to head of the remote branch.</h1>
			<% 
				checkinBean.setResetRemoteHead(true);
				int exitCode = checkinBean.checkIn();
			%>
			<c:choose>
			<c:when test='<%= exitCode == 0 %>'>
				<h2 style="color: green;">The repository was reset.</h2>
				<p>You can repeat your commit. Maybe you need the "Pull first" option to avoid conflicts.</p>
			</c:when>
			<c:otherwise>
				<h2 style="color: red;">Reset failed. Please consult the log file.</h2>
				<p style="color: orange;">You may have an incorrect configuration or you have to manually resolve a GIT conflict.</p>
			</c:otherwise>
			</c:choose>
		</c:when>
		</c:choose>	
		<p>	
			<a href="<cms:link>${cms.requestContext.uri}</cms:link>">Back to the checkin options.</a>
		</p>
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
		<h1>Check in Modules into Git</h1>
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
			<legend>Add module</legend>
				<div>
					<label>Additional Module: </label>
					<select name="newmodule">
						<option value="">--- none ---</option>
						<c:forEach var="module" items="<%=checkinBean.getInstalledModules() %>">
							<option value="${module}">${module}</option>							
						</c:forEach>
					</select>
				</div>
			</fieldset>			
			<fieldset>
				<legend>Commit settings</legend>
				<div>
					<input type="checkbox" name="ignoreunclean" <%= checkinBean.getDefaultIngoreUnclean()? "checked=checked" : "" %>>
					<label>Ignore an unclean repository <span style="color: red;">Caution: This can cause serious problems when a merge conflict is present.</span></label>
				</div>
				<div>
					<input type="checkbox" name="pullfirst" <%= checkinBean.getDefaultAutoPullBefore()? "checked=checked" : "" %>>
					<label>Pull first <span style="color: orange;">This can overwrite changes from the remote repository.</span></label>
				</div>
				<div>
					<input type="checkbox" name="copyandunzip" <%= checkinBean.getDefaultCopyAndUnzip()? "checked=checked" : "" %>>
					<label>Copy and unzip modules <span style="color: orange;">Typically you want this</span></label>
				</div>
				<div>
					<input type="checkbox" name="autocommit" <%= checkinBean.getDefaultAutoCommit()? "checked=checked" : "" %>>
					<label>Add and commit automatically</label>
					<div style="margin-left:27px;">
						<label>Commit message</label>
						<input type="text" name="commitmessage" value="<%= checkinBean.getDefaultCommitMessage() %>">
					</div>
				</div>
				<div>
					<input type="checkbox" name="pullafter" <%= checkinBean.getDefaultAutoPullAfter()? "checked=checked" : "" %>>
					<label>Pull after commit</label>
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
</c:when>
<c:otherwise>
	<h1>
		Git plugin is not configured correctly.
	</h1>
	<p>
		There were problems reading the configuration file <code>module-checkin.conf</code> under <code>WEB-INF/git-scripts/</code>.
	</p>
	<p>
		Please ensure that the file is present and contains a valid configuration. You may use the file <code>module-checkin.conf.demo</code> under <code>WEB-INF/git-scripts/</code> as template for your own configuration.
	</p>
</c:otherwise>
</c:choose>

</body>
</html>