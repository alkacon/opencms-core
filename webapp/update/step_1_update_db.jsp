<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	Bean.prepareUpdateStep1();	

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard - Update Database
<%= Bean.getHtmlPart("C_HEAD_START") %>

</head>
<frameset rows="100%,*">
	<frame src="step_1a_display_update.jsp" name="display">
	<frame src="about:blank" name="data">
</frameset>
</html>
