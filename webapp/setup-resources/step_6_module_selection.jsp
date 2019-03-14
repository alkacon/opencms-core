<%@ page session="true" %><%--
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
Alkacon OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript" language="JavaScript"><!--
<%= Bean.jsModuleNames() %>
<%= Bean.jsModuleDependencies() %>
<%= Bean.jsComponentNames() %>
<%= Bean.jsComponentModules() %>
<%= Bean.jsComponentDependencies() %>

// Checks the dependencies for a specified module package name.
function checkModuleDependencies(modulePackageName) {	

	if (modulePackageNames.length == 1) {
		return;
	}
	var updatesCount = checkForwardDependencies(modulePackageName, modulePackageNames, moduleDependencies, document.modules.availableModules, 0);
	if (updatesCount > 0) {
		alert("Dependencies have been updated!\r\n\r\n" + updatesCount + " module(s) depend on this module.");
	}
	var hasMissingDependencies = checkBackwardDependencies(modulePackageName, modulePackageNames, moduleDependencies, document.modules.availableModules, 0);
	if (hasMissingDependencies) {
    	alert("Dependencies have been updated!\r\n\r\nRequired modules have been added.");
	}
}

// Checks the dependencies for a specified component name.
function checkComponentDependencies(componentName) {	

	if (componentNames.length == 1) {
		return;
	}
	var updatesCount = checkForwardDependencies(componentName, componentNames, componentDependencies, document.modules.availableComponents, 0);
	if (updatesCount > 0) {
		alert("Dependencies have been updated!\r\n\r\n" + updatesCount + " group(s) depend on this component.");
	}
	var hasMissingDependencies = checkBackwardDependencies(componentName, componentNames, componentDependencies, document.modules.availableComponents, 0);
	if (hasMissingDependencies) {
    	alert("Dependencies have been updated!\r\n\r\nRequired groups have been added.");
	}
}

// Returns the index for a specified module package name.
function getIndex(list, entry) {

	for (var i = 0; i < list.length; i++) {
		if (list[i] == entry) {
			return i;
		}
	}
	return -1;
}

// Checks whether other entries depend on the specified entry.
function checkForwardDependencies(entry, list, dependencyList, checkBoxes, recursionCounter) {	

	// check if we are stuck in an infinite loop of dependencies
	if (recursionCounter > 255) {
		return 0;
	}

	if (list.length == 1) {
		return 0;
	}	
	
	var index = getIndex(list, entry);		
	if ((index < 0) || (dependencyList[index].length == 0)) {
	    return 0;
	}
	
	var doUncheck = !checkBoxes[index].checked;
	var dependencies = dependencyList[index];		
	var updatesCount = 0;	
	
	// check/uncheck all entries that are dependent on the specified entry
	for (var j = 0; j < dependencies.length; j++) {
		var dependencyIndex = getIndex(list, dependencies[j]);
		if (dependencyIndex > -1) {
			if (doUncheck) {
				checkBoxes[dependencyIndex].checked = false;
			} else {
				checkBoxes[dependencyIndex].checked = true;
			}
		}
		updatesCount = updatesCount + checkForwardDependencies(dependencies[j], list, dependencyList, checkBoxes, recursionCounter + 1);
	}	
	return updatesCount + dependencies.length;
}

// Checks whether the specified entry depends on another entries.
function checkBackwardDependencies(entry, list, dependencyList, checkBoxes, recursionCounter) {

	// check if we are stuck in an infinite loop of dependencies
	if (recursionCounter > 255) {
		return false;
	}
	
	if (list.length == 1) {
		return false;
	}
	
	var hasMissingDependency = false;
	// visit all dependency lists and check if the specified entry is found in one of those lists
	for (var i = 0; i < dependencyList.length; i++) {
		var dependencies = dependencyList[i];
		
		for (var j = 0; j < dependencies.length; j++) {
			if (dependencies[j] == entry) {
				// yup, the specified entry has been found in a dependency list
				// list[i] is the entry that is required to fulfill the dependency
				// set the checkbox of the required entry on checked				
				if (!checkBoxes[i].checked) {
					checkBoxes[i].checked = true;
					hasMissingDependency = true;
					
					// check whether the required module itself depends on other modules
					checkBackwardDependencies(list[i], list, dependencyList, checkBoxes, recursionCounter + 1);
				}
			}
		}
	}
	return hasMissingDependency;
}

function getAvailableModules() {

	var form = document.modules;	
	var installModules = new Array();

	if (document.getElementById('modulesView').style.display == 'none') {
		// component selection
		if (componentNames.length > 1) {		
			for (var i = 0; i < componentNames.length; i++) {
				if (form.availableComponents[i].checked) {	
					installModules.push(componentModules[i]);
				}
			}
			form.installModules.value = installModules.join("|");
		} else {
			if (form.availableComponents!= null && form.availableComponents.checked) {
				form.installModules.value = componentModules[0];
			}
		}
	} else {
		// module selection
		if (modulePackageNames.length > 1) {		
			for (var i = 0; i < modulePackageNames.length; i++) {
				if (form.availableModules[i].checked) {	
					installModules.push(modulePackageNames[i]);
				}
			}
			form.installModules.value = installModules.join("|");
		} else {
			if (form.availableModules != null && form.availableModules.checked) {
				form.installModules.value = modulePackageNames[0];
			}
		}
	}
	//alert(form.installModules.value);
	return false;
}

function switchView() {
	
	if (document.getElementById('modulesView').style.display == 'none') {
       document.getElementById('componentsView').style.display = 'none';
       document.getElementById('modulesView').style.display = '';
    } else {
       document.getElementById('modulesView').style.display = 'none';
       document.getElementById('componentsView').style.display = '';
    }
}

//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
Alkacon OpenCms Setup Wizard - Module selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>

<form method="post" class="nomargin" name="modules" onSubmit="getAvailableModules();">
<input type="hidden" name="installModules" value="">
<table id="componentsView" border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Module groups available for installation") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="1" cellspacing="0">
<%= Bean.htmlComponents() %>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr><tr><td align='right'><a href="javascript:switchView()">Individual module selection</a></td></tr>
</table>
<table id="modulesView" style='width: 100%; height: 100%; display:none;' border="0" cellpadding="5" cellspacing="0" >
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
</tr><tr><td align='right'><a href="javascript:switchView()">Module group selection</a></td></tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.htmlModuleHelpDescriptions() %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" >
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
</form>
</span>
<% } else { %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>