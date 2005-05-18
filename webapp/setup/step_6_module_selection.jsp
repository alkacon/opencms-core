<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><%!
    private List sortModules(Map modules) {

       List aux = new ArrayList(modules.values());
       Collections.sort(aux, new Comparator() {
           public int compare(Object o1, Object o2) {

               Map m1 = (Map)o1;
               Map m2 = (Map)o2;
               String n1 = (String)m1.get("niceName");
               String n2 = (String)m2.get("niceName");
               return n1.compareTo(n2);
           }
       });

       List ret = new ArrayList(aux.size());
       for (Iterator it = aux.iterator(); it.hasNext();) {
            Map module = (Map)it.next();
            ret.add(module.get("name"));
	   }
       return ret;
   }
%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
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
	List moduleNames = sortModules(modules);
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
	if (recursionCounter==0) {
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

function sortAvailableModules() {
	var form = document.modules;	
	var installModules = new Array();
	var notInstallModules = new Array();

	if (modulePackageNames.length > 1) {		
		for (var i=0;i<modulePackageNames.length;i++) {
			if (form.availableModules[i].checked == true) {	
				installModules.push(modulePackageNames[i]);
			} else {
				notInstallModules.push(modulePackageNames[i]);
			}
		}
	
		form.installModules.value = topoSort(installModules, notInstallModules).join("|");
	} else {
		if (form.availableModules != null) {
			form.installModules.value = form.availableModules.value;
		}
	}
	//alert(form.installModules.value);
	return false;
}

function topoSort(list, notInstalled) {
    for (var i=0; i<notInstalled.length; i++) {
		removeDependencies(notInstalled[i]);
		moduleDependencies[getPackageNameIndex(notInstalled[i])] = new Array();
	}

    var retList = new Array();
	var finished = false;
	while(!finished) {
	    finished = true;
        for (var i=0; i<list.length; i++) {   
		    if (list[i]=="_visited_") {
				continue;
			}
			if (moduleDependencies[getPackageNameIndex(list[i])].length==0) {
			   retList.push(list[i]);
			   removeDependencies(list[i]);
			   finished = false;
			   list[i]="_visited_";
			}
		}
	}
	var cycle = new Array();
    for (var i=0; i<list.length; i++) {   
	    if (list[i]!="_visited_") {
			cycle.push(list[i]);
		}
	}
	if (cycle.length>0) {
	   alert("One or more dependency cycles has been detected.\n\nInvolved modules are:\n"+cycle.join("\n"));
	   retList = retList.concat(cycle);
	}
	retList.reverse();
	return retList;
}

function removeDependencies(module) {
    for (var i=0; i<moduleDependencies.length; i++) {
		var newDeps = new Array();
		var deps = moduleDependencies[i];
		for (var j=0; j<deps.length; j++) {
		   if (deps[j]==module) {
			   continue;
		   }
		   newDeps.push(deps[j]);
		}
		moduleDependencies[i] = newDeps;
    }
}

//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Module selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="post" class="nomargin" name="modules" onSubmit="sortAvailableModules();">
<input type="hidden" name="installModules" value="">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Modules available for installation") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="2" cellspacing="0">
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
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" >
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>