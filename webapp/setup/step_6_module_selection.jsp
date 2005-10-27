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
	
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript" language="JavaScript"><!--
<%= Bean.jsModuleNames() %>
<%= Bean.jsModuleDependencies() %>

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

	checkForwardDependencies(modulePackageName, 0);
	checkBackwardDependencies(modulePackageName, 0);
}

// Checks whether other modules depend on the specified module.
function checkForwardDependencies(modulePackageName, recursionCounter) {	
	var dependencies;
	var dependentModuleIndex = -1;
	var form = document.modules;
	var doUncheck = false;
	var moduleIndex = -1;
	
	// check if we are stuck in an infinite loop of module dependencies
	if (recursionCounter > 255) {
		return 0;
	}

	if (modulePackageNames.length == 1) {
		return 0;
	}	
	
	var updatesCount = 0;

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
			updatesCount = updatesCount + checkForwardDependencies(dependencies[j], recursionCounter+1);
		}	
		updatesCount = updatesCount + dependencies.length;
	}
	if (recursionCounter==0 && updatesCount>0) {
		alert("Dependencies have been updated!\r\n\r\n" + updatesCount + " module(s) depend on this module.");
	}
	return updatesCount;
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

function getAvailableModules() {
	var form = document.modules;	
	var installModules = new Array();

	if (modulePackageNames.length > 1) {		
		for (var i=0;i<modulePackageNames.length;i++) {
			if (form.availableModules[i].checked == true) {	
				installModules.push(modulePackageNames[i]);
			}
		}
		form.installModules.value = installModules.join("|");
	} else {
		if (form.availableModules != null) {
			form.installModules.value = form.availableModules.value;
		}
	}
	//alert(form.installModules.value);
	return false;
}
//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Module selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="post" class="nomargin" name="modules" onSubmit="getAvailableModules();">
<input type="hidden" name="installModules" value="">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Modules available for installation") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="2" cellspacing="0">
<%= Bean.htmlModules() %>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.htmlModuleHelpDescriptions() %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" >
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>