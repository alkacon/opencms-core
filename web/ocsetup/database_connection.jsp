<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

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

%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->
<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="style.css">	

<script language="Javascript">
	location.href = "<%= nextPage %>";
</script>

</head>

<body>
</body>
</html>