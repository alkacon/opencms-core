<%@ page import="org.opencms.jsp.*" %><%	

    /*      
     * This page checks if all libraries required for the demo modules have been installed.
     */

String[] classNames = {
"org.opencms.frontend.templateone.CmsTemplateBean"
};

boolean missingClasses = false;

try {
	for (int i=0; i<classNames.length; i++) {

		// try to find all the named classes
		String className = classNames[i];
		Class.forName(className);
	}	
} catch (Throwable t) {
	missingClasses = true;
}

if (missingClasses) {
%>

<div style="border: 1px solid red; padding: 5px; background-color: #CCFFCC;">
<p><b>PLEASE NOTE:</b> The required classes for the OpenCms 'templateone' demo could not be loaded!<br>
<i>In case you just did a setup, <b>you must now restart your servlet engine</b>.</i></p>
<p><span class="small">If you intentionally did not install the 'templateone' demo, please disregard this message.</span></p>
</div>
<%
}
%>