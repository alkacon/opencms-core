<%@page buffer="none" session="false" import="org.opencms.file.*, org.opencms.frontend.templateone.*, org.opencms.workplace.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

String linkUri = cms.link(cms.getRequestContext().getUri());
String systemUri = cms.link(I_CmsWpConstants.C_VFS_PATH_MODULES + CmsTemplateBean.C_MODULE_NAME + "/pages/");

String helpUri = request.getParameter(CmsTemplateBean.C_PARAM_HELPURI);
boolean showHelp = !"none".equals(helpUri);

String loginText = cms.key("link.login");
CmsUser currUser = cms.getRequestContext().currentUser();
if (!currUser.isGuestUser()) {
   loginText = cms.key("link.login.loggedin") + " " + currUser.getName();   
}

if (CmsTemplateBean.C_PARAM_ACCESSIBLE.equals(cms.getLayout())) {
	%><!-- Beginn Footer-Links -->
	<div class="linksfoot">
	<a class="linksheadfoot" href="#top" title="<%= cms.key("link.top") %>"><%= cms.key("link.top") %></a>
	| <a class="linksheadfoot" href="<%= cms.link(cms.getStartFolder() + "login.html") %>" title="<%= loginText %>"><%= loginText %></a>
	| <a class="linksheadfoot" href="javascript:openRecommendForm('<%= systemUri %>recommend.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');" title="<%= cms.key("link.recommend") %>"><%= cms.key("link.recommend") %></a>
	| <a class="linksheadfoot" href="javascript:openPrintVersion();" title="<%= cms.key("link.print") %>"><%= cms.key("link.print") %></a>
	| <a class="linksheadfoot" href="javascript:openLetterForm('<%= systemUri %>letter.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');" title="<%= cms.key("link.contact") %>"><%= cms.key("link.contact") %></a>
	<% if (showHelp) { %>
	| <a class="linksheadfoot" title="<%= cms.key("link.help") %>" href="<%= cms.link(helpUri) %>"><%= cms.key("link.help") %></a>
	<% } %>
	| <a class="linksheadfoot" title="<%= cms.key("link.version.common") %>" href="<%= linkUri %>?accessible=false"><%= cms.key("link.version.common") %></a>
	| <a class="linksheadfoot" title="<%= cms.key("link.imprint") %>" href="javascript:openImprint('<%= systemUri %>imprint.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');"><%= cms.key("link.imprint") %></a>&nbsp;
	</div>
	<!-- Ende Footer-Links -->
	<%
} else if (CmsTemplateBean.C_PARAM_COMMON.equals(cms.getLayout())) {
	%><!-- Beginn Footer-Links -->
	<div class="linksfoot">
	<a href="#top" title="<%= cms.key("link.top") %>"><img src="<%= cms.getResourcePath() %>up.gif" height="12" alt="<%= cms.key("link.top") %>" width="10" align="middle" border="0" /></a>
	<a href="<%= cms.link(cms.getStartFolder() + "login.html") %>" title="<%= loginText %>"><img height="14" alt="<%= loginText %>" src="<%= cms.getResourcePath() %>login.gif" width="19" align="middle" border="0" /></a>
	<a href="javascript:openRecommendForm('<%= systemUri %>recommend.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');" title="<%= cms.key("link.recommend") %>"><img height="12" alt="<%= cms.key("link.recommend") %>" src="<%= cms.getResourcePath() %>mail.gif" width="15" align="middle" border="0" /></a>
	<a href="javascript:openPrintVersion();" title="<%= cms.key("link.print") %>"><img height="12" alt="<%= cms.key("link.print") %>" src="<%= cms.getResourcePath() %>print.gif" width="15" align="middle" border="0" /></a>
	<a href="javascript:openLetterForm('<%= systemUri %>letter.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');" title="<%= cms.key("link.contact") %>"><img height="13" alt="<%= cms.key("link.contact") %>" src="<%= cms.getResourcePath() %>contact.gif" width="11" align="middle" border="0" /></a>
	<% if (showHelp) { %>
	&nbsp;<a class="linksheadfoot" title="<%= cms.key("link.help") %>" href="<%= cms.link(helpUri) %>"><%= cms.key("link.help") %></a>
	<% } %>
	&nbsp;<a class="linksheadfoot" title="<%= cms.key("link.version.accessible") %>" href="<%= linkUri %>?accessible=true"><%= cms.key("link.version.accessible") %></a>
	&nbsp;<a class="linksheadfoot" title="<%= cms.key("link.imprint") %>" href="javascript:openImprint('<%= systemUri %>imprint.html', '<%= cms.getRequestContext().getUri() %>', '<%= cms.getRequestContext().getLocale() %>');"><%= cms.key("link.imprint") %></a>&nbsp;
	</div>
	<!-- Ende Footer-Links -->
	<%
}
%>