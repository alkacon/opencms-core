<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_6_finished.jsp";	
	// previous page 
	String prevPage = "index.jsp";
	
	Bean.prepareUpdateStep5();	

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard - Update modules
<%= Bean.getHtmlPart("C_HEAD_START") %>

</head>
<frameset rows="100%,*">
	<frame src="step_5a_display_update.jsp" name="display">
	<frame src="about:blank" name="data">
</frameset>
</html>
