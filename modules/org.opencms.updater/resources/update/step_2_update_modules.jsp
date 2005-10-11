<%@ page import="org.opencms.update.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.update.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_3_todo.jsp";	
	// previous page 
	String prevPage = "index.jsp";
	
	Bean.prepareStep2();	

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard - Update modules
<%= Bean.getHtmlPart("C_HEAD_START") %>

</head>
<frameset rows="100%,*">
	<frame src="step_2a_display_update.jsp" name="display">
	<frame src="about:blank" name="data">
</frameset>
</html>
