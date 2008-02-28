<%@ page session="false" import="java.util.*" %><%

// initialise Cms Action Element
org.opencms.jsp.CmsJspActionElement cms = new org.opencms.jsp.CmsJspActionElement(pageContext, request, response);
String target = cms.property("template-elements");

if (target.indexOf(':') > -1) {
    StringTokenizer tok1 = new StringTokenizer(target, ":");
    while (tok1.hasMoreTokens()) {
        String token = tok1.nextToken();
        int pos = token.indexOf('=');
        if (pos > -1) {
            String newtarget = token.substring(pos+1);
            String server = token.substring(0, pos);
            if (request.getServerName().indexOf(server) > -1) {
                target = newtarget;
                break;
            }  
        }
    }
}

List browserLanguages = new ArrayList();
String accLangs = request.getHeader("Accept-Language");
if (accLangs != null) {
    StringTokenizer toks = new StringTokenizer(accLangs, ",");
    while (toks.hasMoreTokens()) {
        // Loop through all languages and cut off trailing extensions
        String current = toks.nextToken().trim();
        if (current.indexOf("-") > -1) {
            current = current.substring(0, current.indexOf("-"));
        }
        if (current.indexOf(";") > -1) {
            current = current.substring(0, current.indexOf(";"));
        }
        browserLanguages.add(current.toLowerCase());
    }
}

Iterator i = browserLanguages.iterator();
String targetLang = null;
boolean found = false;
while(i.hasNext() && (! found)) {
	String lang = (String)i.next();
	targetLang = target + lang + "/";
	try {
		cms.getCmsObject().readFileHeader(targetLang);
		found = true;
	} catch (Exception e) {}
}
if (found == false) {
	targetLang = target + "en/";
}
target = targetLang;


if ((target != null) && (!"".equals(target)) && (request.getParameter("test") == null)) {
	response.sendRedirect(cms.link(target));
}

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Please redirect to <%= target %></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<meta name="description" content="Alkacon OpenCms, easy to use, professional open source content management based on Java and XML." />
<meta name="keywords" content="Alkacon, OpenCms, Java, CMS, J2EE, OSS, Open Source, Content, Management, Software, Website" />
<meta name="author" content="Alkacon Software GmbH, Cologne, Germany" />
<meta name="copyright" content="Alkacon Software GmbH, Cologne, Germany" />
<meta name="email" content="webmaster@alkacon.com" />
<meta name="robots" content="noindex, follow, noarchive" />
</head>
<body>
<h1>This page has moved!</h1>
<p>Please redirect your browser to: <a href="<%= cms.link(target) %>"><%= cms.link(target) %></a></p>
</body>
</html>