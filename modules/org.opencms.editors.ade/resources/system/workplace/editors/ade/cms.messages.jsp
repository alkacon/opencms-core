<%@ page import="org.opencms.file.*"%>
<%@ page import="org.opencms.jsp.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.opencms.workplace.*"%>
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
// TODO: make a class for this JSP
// TODO: create a class for the messages. and include it to the test cases
// TODO: do not get the locale from the session, that wont work with a front-end login!
%>
<%
    CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
    Locale locale = settings.getUserSettings().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("org.opencms.workplace.editors.ade.ade_messages", locale);
%>
(function(cms) { var M = cms.messages;
<%
    setAllMessages(out, bundle);
%>
})(cms);
