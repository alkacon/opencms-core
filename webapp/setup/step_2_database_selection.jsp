<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<%	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
%>

<% /* Import packages */ %>
<%@ page import="java.util.*" %>

<% /* Set all given properties */ %>
<jsp:setProperty name="Bean" property="*" />

<% 
	String nextPage = "";
	
	if(setupOk)	{
		String temp = Bean.getDbSetupProps().getProperty(Bean.getDatabase()+".page");
		if(temp != null)	{
			nextPage = temp;
		}
	}

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script type="text/javascript">
	location.href = "<%= nextPage %>";
</script>
</head>

<body>
</body>
</html>