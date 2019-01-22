<%@ page session="false" import="
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*
"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsPreEditorAction pre = new CmsPreEditorAction(cms);

	if (!pre.doPreAction()) {
	
		CmsEditorFrameset wp = new CmsEditorFrameset(cms);

%><!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamEditorTitle() %></title>
		<script type="text/javascript">
		<!--
			// change window name when opening editor in direct edit mode 
			// to avoid loss of content when previewing another resource in Explorer view
			if (window.name == "preview") {
				window.name = "editor_directedit";
			}
		//-->
		</script>
	</head>
	<frameset rows="*,${param.nofoot == '1' ? '0' : '24' },0" border="0" frameborder="0" framespacing="0">
	    <frame <%= wp.getFrameSource("edit", cms.link("/system/workplace/editors/editor_main.jsp?" + wp.allParamsAsRequest())) %> noresize scrolling="no">
	    <c:if test="${param.nofoot != '1'}">
			<frame <%= wp.getFrameSource("foot", cms.link("/system/workplace/views/top_foot.jsp")) %> noresize scrolling="no">
		</c:if>
	</frameset>
</html><%
	}
%>