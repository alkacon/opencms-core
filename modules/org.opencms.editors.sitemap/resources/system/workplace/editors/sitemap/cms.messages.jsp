<%@ page import="org.opencms.file.*, 
	org.opencms.i18n.CmsResourceBundleLoader, 
	org.opencms.jsp.*, 
	java.util.*, 
	javax.servlet.*,
	java.io.*,
	org.opencms.workplace.*"%>
<%!
public void setJavascriptMessage(String name, JspWriter out, ResourceBundle bundle) throws IOException {
	String value = bundle.getString(name).replace("'", "\\'");
    out.println("M." + name + " = '" + value + "';");
}

public void setAllMessages(JspWriter out, ResourceBundle bundle) throws Exception {
    Enumeration<String> keys = bundle.getKeys();
    while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        setJavascriptMessage(key, out, bundle);
    }
}
%><%
    CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
    Locale locale = settings.getUserSettings().getLocale();
    ResourceBundle adeBundle = CmsResourceBundleLoader.getBundle("org.opencms.workplace.editors.ade.messagesADE", locale);
    ResourceBundle sitemapBundle = CmsResourceBundleLoader.getBundle("org.opencms.workplace.editors.sitemap.messagesSitemap", locale);
%>
(function(cms) { var M = cms.messages;
<%
    setAllMessages(out, adeBundle);
	setAllMessages(out, sitemapBundle);

%>
})(cms);
