<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%
	
	// next page
	String nextPage = "step_7_save_properties.jsp";	
	// previous page
	String prevPage = "step_3_database_selection.jsp";
	
	boolean isFormSubmitted = (request.getParameter("submit") != null);
	
	if (Bean.isInitialized() && isFormSubmitted) {
		response.sendRedirect(nextPage);
	}	
	
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
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
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
	if (modulePackageNames.length == 1) {
		return;
	}

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
	
	if (modulePackageNames.length == 1) {
		return;
	}	
	
	moduleIndex = getPackageNameIndex(modulePackageName);		
	if (moduleIndex > -1 && moduleDependencies[moduleIndex].length > 0) {
		doUncheck = (form.availableModules[moduleIndex].checked == false);
		dependencies = moduleDependencies[moduleIndex];		
		
		// check/uncheck all modules that are dependent on the specified module
		for (var j=0;j<dependencies.length;j++) {
			dependentModuleIndex = getPackageNameIndex(dependencies[j]);
			if (dependentModuleIndex > -1) {
				if (doUncheck) {
					form.availableModules[dependentModuleIndex].checked = false;
				} else {
					form.availableModules[dependentModuleIndex].checked = true;
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
	
	if (modulePackageNames.length == 1) {
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
				if (!form.availableModules[i].checked) {
					form.availableModules[i].checked = true;
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

function compareModules(modulepackageName_A, modulepackageName_B) {
	var indexA = getPackageNameIndex(modulepackageName_A);
	var indexB = getPackageNameIndex(modulepackageName_B);
	var dependencies = null;
	
	if (modulepackageName_A == modulepackageName_B) {
		return 0;
	}
	
	dependencies = moduleDependencies[indexA];
	for (var i=0;i<dependencies.length;i++) {
		if (dependencies[i] == modulepackageName_B) {
			// module A will appear before module B
			return -1;
		}
	}
	
	dependencies = moduleDependencies[indexB];
	for (var i=0;i<dependencies.length;i++) {
		if (dependencies[i] == modulepackageName_A) {
			// module B will appear before moduel A
			return 1;
		}
	}
	
	return 0;
}

function sortAvailableModules() {
	var form = document.modules;	
	var installModules = new Array();

	if (modulePackageNames.length > 1) {		
		for (var i=0;i<modulePackageNames.length;i++) {
			if (form.availableModules[i].checked == true) {	
				installModules.push(modulePackageNames[i]);
			}
		}
	
		installModules.sort(compareModules);

		var moduleList = "";
		for (var j=0;j<installModules.length;j++) {
			moduleList += installModules[j];
		
			if (j<installModules.length-1) {
				moduleList += "|";
			}
		}
	
		//alert(moduleList);
		form.installModules.value = moduleList;
	} else {
		form.installModules.value = form.availableModules.value;
	}
}

//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Module selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="get" class="nomargin" name="modules" onSubmit="sortAvailableModules();">
<input type="hidden" name="installModules" value="">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Modules available for installation") %>

	<div style="width:100%; height: 300px; overflow: auto;">
	<table border="0" cellpadding="2">
<%
	String descriptions = "";
	for (int i=0; i<moduleNames.size(); i++) {
		moduleName = (String) moduleNames.get(i);
		module = (Map) modules.get(moduleName);
		moduleNiceName = (String) module.get("niceName");
		moduleVersion = (String) module.get("version");
		moduleDescription = (String) module.get("description");
%>
		<tr>
			<td style="vertical-align: top;">
				<input type="checkbox" name="availableModules" value="<%= moduleName %>" checked="checked" onClick="checkDependencies('<%= moduleName %>')">
			</td>
			<td style="vertical-align: top; width: 100%; padding-top: 4px;">
				<%= moduleNiceName %> (<%= moduleVersion %>)
			</td>
			<td style="vertical-align: top; text-align: right;">
				<%
					if (moduleDescription != null && !"".equals(moduleDescription)) {
						out.print(Bean.getHtmlHelpIcon("" + i, ""));
						descriptions += Bean.getHtmlPart("C_HELP_START", "" + i) + moduleDescription + Bean.getHtmlPart("C_HELP_END");
					}
				%>
			</td>
		</tr>
<%
	}
%>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= descriptions %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>