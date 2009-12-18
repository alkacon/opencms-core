<%@ page import="org.opencms.file.*,org.opencms.jsp.*,java.util.*,javax.servlet.*,java.io.*,org.opencms.workplace.*"%><%!
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
    ResourceBundle adeBundle = ResourceBundle.getBundle("org.opencms.workplace.editors.ade.ade_messages", locale);
    ResourceBundle sitemapBundle = ResourceBundle.getBundle("org.opencms.workplace.editors.sitemap.sitemap_messages", locale);
%>
(function(cms) { var M = cms.messages;
<%
    setAllMessages(out, adeBundle);
	setAllMessages(out, sitemapBundle);

%>
})(cms);
