<%@ page import="org.opencms.workplace.*" buffer="none" session="false" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>
	<style type="text/css">
	<!--
		body.dialog {
			text-align: center;
			background-color: Menu;
		}
	//-->
	</style>
	
</head>
<body class="dialog" unselectable="on">
<div name="imgtitle" id="imgtitle" style="font-weight: bold; margin-top: 3px; margin-bottom: 5px;" unselectable="on">&nbsp;</div>
<div class="hide" id="imgdiv">
	<img src="" name="imgnode" id="imgnode" border="0" title="" alt=""></td>
</div>
</body>
<%= wp.htmlEnd() %>