<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:useBean id="xmlBean" class="org.opencms.setup.xml.CmsSetupXmlManager" scope="page" /><%
    xmlBean.initialize(Bean.getDetectedVersion());
%><jsp:setProperty name="xmlBean" property="*" /><%
	
	// next page
	String nextPage = "step_4_module_selection.jsp";	
	// previous page
	String prevPage = "step_2_settings.jsp";
	
	boolean isFormSubmitted = (request.getParameter("submit") != null);
	
	String htmlAvailablePlugins = "";
	if (Bean.isInitialized()) {
	  if (isFormSubmitted) {
	    xmlBean.execute(Bean);
		response.sendRedirect(nextPage);
		return;
      }
	  htmlAvailablePlugins = xmlBean.htmlAvailablePlugins(Bean);
	  if (htmlAvailablePlugins.trim().length()==0) {
		response.sendRedirect(nextPage);
		return;
      }
	}	
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript">//<!--
function getSelectedPlugins2() {
	var form = document.xmlupdate;	
	var selPlugins = new Array();

	if (form.availablePlugins.length > 1) {		
		for (var i=0;i<form.availablePlugins.length;i++) {
			if (form.availablePlugins[i].checked == true) {	
				selPlugins.push(form.availablePlugins[i].value);
			}
		}	
		form.selectedPlugins.value = selPlugins.join("|");
	} else {
		if (form.availablePlugins != null) {
			form.selectedPlugins.value = form.availablePlugins.value;
		}
	}
	//alert(form.selectedPlugins.value);
	return false;
}

function switchview(id) {
	
	var elem = document.getElementById(id);
	if (elem != undefined) {
		if (elem.style.display == "block") {
			elem.style.display = "none";
		} else {
			elem.style.display = "block";
		}
	}
}
//-->
</script>
<style type="text/css">
	pre.code {
		font-family: "Courier New", Courier, monospace;
		background-color: #ffffff;
		border: 1px;
		border-style: solid;
		border-color: #000000;
		padding: 5px 5px 5px 5px;
		margin-left: 5px;
		margin-right: 5px;
	}
</style>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Update Wizard - XML Configuration Files Update
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="post" class="nomargin" name="xmlupdate" onSubmit="getSelectedPlugins2();">
<input type="hidden" name="selectedPlugins" value="">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "XML changes available for update") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="2" cellspacing="0">
<%= htmlAvailablePlugins %>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" >
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
