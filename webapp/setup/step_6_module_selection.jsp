<%@ page import="java.util.*" %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" />
<%
	String nextPage = "step_7_save_properties.jsp";
	boolean isSetupOk = (Bean.getProperties() != null);
	boolean isFormSubmitted = (request.getParameter("submit") != null);
	
	Map modules = Bean.getAvailableModules();
	Map moduleDependencies = Bean.getModuleDependencies();
	List dependencies = null;
    List moduleNames = (List) new ArrayList(modules.keySet());
	Collections.sort(moduleNames);	
	String moduleName = null;
	String moduleNiceName = null;
	String moduleVersion = null;
	String moduleDescription = null;
	Map module = null;
	
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<script type="text/javascript" language="JavaScript">
<!--

// an array from 1...n holding the module package names
var modulePackageNames = new Array(<%= moduleNames.size() %>);
// an array holding the dependent modules for the n-th module
var moduleDependencies = new Array(<%= moduleNames.size() %>);

<%
	out.println("// these modules...");
	for (int i=0;i<moduleNames.size();i++) {
		moduleName = (String) moduleNames.get(i);	
		out.println("modulePackageNames[" + i + "] = \"" + moduleName + "\";");
	}
	
	out.println();

	out.println("// ...have these dependencies:");	
	for (int i=0;i<moduleNames.size();i++) {
		moduleName = (String) moduleNames.get(i);	
		dependencies = (List) moduleDependencies.get(moduleName);
		out.print("moduleDependencies[" + i + "] = new Array(");
		
		if (dependencies != null) {	
			for (int j=0;j<dependencies.size();j++) {
				out.print("\"" + dependencies.get(j) + "\"");
				
				if (j<dependencies.size()-1) {
					out.print(", ");
				}
			}
		}
		
		out.println(");");
	}
%>

// Returns the index for a specified module package name.
function getPackageNameIndex(modulePackageName) {
	for (var i=0;i<modulePackageNames.length;i++) {
		if (modulePackageNames[i] == modulePackageName) {
			return i;
		}
	}
	
	return -1;
}

// Checks the dependencies for a specified module package name.
function checkDependencies(modulePackageName) {	
	checkForwardDependencies(modulePackageName);
	checkBackwardDependencies(modulePackageName, 0);
}

// Checks whether other modules depend on the specified module.
function checkForwardDependencies(modulePackageName) {	
	var dependencies;
	var dependentModuleIndex = -1;
	var form = document.modules;
	var doUncheck = false;
	var moduleIndex = -1;
	
	moduleIndex = getPackageNameIndex(modulePackageName);		
	if (moduleIndex > -1 && moduleDependencies[moduleIndex].length > 0) {
		doUncheck = (form.installModules[moduleIndex].checked == false);
		dependencies = moduleDependencies[moduleIndex];		
		
		// check/uncheck all modules that are dependent on the specified module
		for (var j=0;j<dependencies.length;j++) {
			dependentModuleIndex = getPackageNameIndex(dependencies[j]);
			if (dependentModuleIndex > -1) {
				if (doUncheck) {
					form.installModules[dependentModuleIndex].checked = false;
				} else {
					form.installModules[dependentModuleIndex].checked = true;
				}
			}
		}	
		
		alert("Dependencies have been updated!\r\n\r\n" + dependencies.length + " module(s) depend on this module.");			
	}
}

// Checks whether the specified module depends on another module.
function checkBackwardDependencies(modulePackageName, recursionCounter) {
	var form = document.modules;
	var hasMissingDependency = false;
	
	// check if we are stuck in an infinite loop of module dependencies
	if (recursionCounter > 255) {
		return;
	}
	
	// visit all module dependency lists and check if the specified module is found in one of those lists
	for (var i=0;i<moduleDependencies.length;i++) {
		dependencies = moduleDependencies[i];
		
		for (var j=0;j<dependencies.length;j++) {
			if (dependencies[j] == modulePackageName) {
				// yup, the specified module has been found in a dependency list
				// modulePackageNames[i] is the module that is required to fulfill the dependency
				// set the checkbox of the required module on checked				
				if (!form.installModules[i].checked) {
					form.installModules[i].checked = true;
					hasMissingDependency = true;
					
					// check whether the required module itself depends on other modules
					checkBackwardDependencies(modulePackageNames[i], recursionCounter+1);
				}
			}
		}
	}
	
	if (hasMissingDependency && recursionCounter == 0) {
		alert("Dependencies have been updated!\r\n\r\nRequired modules have been added.");
	}
}

<%
	if (isSetupOk && isFormSubmitted) {
		out.println("document.location.href='"+nextPage+"';");
	}
%>
//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Module selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS", "") %>

<% if(isSetupOk)	{ %>
<form method="POST" class="nomargin" name="modules">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<table border="0">
			<tr>
				<td align="center">
					<table border="0" cellpadding="2">
<%
	for (int i=0;i<moduleNames.size();i++) {
		moduleName = (String) moduleNames.get(i);
		module = (Map) modules.get(moduleName);
		moduleNiceName = (String) module.get("niceName");
		moduleVersion = (String) module.get("version");
		moduleDescription = (String) module.get("description");
%>
						<tr>
							<td>
								<input type="checkbox" name="installModules" value="<%= moduleName %>" checked onClick="checkDependencies('<%= moduleName %>')">
							</td>
							<td width="450" align="left" style="font-weight:bold;">
								<%= moduleNiceName %> (<%= moduleVersion %>)
							</td>
						</tr>
<%
	}
%>
					</table>
				</td>
			</tr>
		</table>
	</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='index.jsp';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<p><b>ERROR</b></p>
		The setup wizard has not been started correctly!<br>
		Please click <a href="<%= request.getContextPath() %>/setup/">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>