<%@page buffer="none" session="false" import="java.util.*, org.opencms.frontend.templateone.*, org.opencms.workplace.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

List links = cms.getHeadLinks();
String searchLink = cms.link(I_CmsWpConstants.C_VFS_PATH_MODULES + CmsTemplateBean.C_MODULE_NAME + "/pages/search.html");
String separator = "";

%><div class="linkshead">
<!-- Beginn der Head-Links -->
	<form style="margin: 0; padding: 0;" name="searchhead" action="<%= searchLink %>" method="post" onsubmit="return parseSearchQuery(document.forms['searchhead'], '<%= cms.key("search.error.wordlength") %>');">	
	<%
	int linkSize = links.size();
	for (int i=0; i<links.size(); i++) {
		CmsTemplateLink link = (CmsTemplateLink)links.get(i);
		String targetAttr = link.getTarget();
		if ("".equals(targetAttr)) {
			targetAttr = "_self";
		}
		%> <%= separator %>&nbsp;<a class="linksheadfoot" href="<%= link.getAnchor() %>" target="<%= targetAttr %>"><%= link.getText() %></a><%
		if (CmsTemplateBean.C_PARAM_ACCESSIBLE.equals(cms.getLayout())) {
			separator = "|";
		}
	}
	%>
	<input type="hidden" name="action" value="search" />
	<input type="hidden" name="uri" value="<%= cms.getRequestContext().getUri() %>" />
	<input type="hidden" name="__locale" value="<%= cms.getRequestContext().getLocale() %>" />
	<input type="hidden" name="query" value="" />
	<input type="hidden" name="page" value="1" />
	<input type="hidden" name="index" value="<%= cms.getSearchIndexName() %>" />
	&nbsp;<span style="vertical-align: middle;"><input type="text" class="search" name="query2" /></span>
	&nbsp;<span style="vertical-align: middle;"><input type="submit" name="startsearch" value="<%= cms.key("link.search") %>" class="formbutton" /></span>&nbsp;
	</form>
<!-- Ende der Head-Links -->
</div>