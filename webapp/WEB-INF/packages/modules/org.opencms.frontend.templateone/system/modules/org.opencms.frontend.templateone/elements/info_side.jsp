<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*,org.opencms.util.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

String sideUri = request.getParameter("style_side_uri");
boolean showSideUri = CmsStringUtil.isNotEmpty(sideUri) && !"none".equals(sideUri);

String xmlDemo = cms.property("xmlcontent-side-uri", "search");
boolean showXmlDemo = CmsStringUtil.isNotEmpty(xmlDemo) && !"none".equals(xmlDemo);

boolean showSide = showSideUri || showXmlDemo;

String spacer = "<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n";

// create info box
if (showSide) {
    String blockElement = "td";
    if (cms.showAccessibleVersion()) {
        blockElement = "div";    
    }
    out.print("<");
    out.print(blockElement);
    out.print(" class=\"infobox\">\n");
    out.println("<!-- Side info box start -->\n");
    
    if (showXmlDemo) {
		cms.includeSilent("/system/modules/org.opencms.frontend.templateone.xmlcontentdemo/elements/side_article_list.jsp", null, true);
		if (showSideUri) {
			out.println(spacer);
		}
	}
    
    if (showSideUri) {    	
		cms.includeSilent(sideUri, "text1", true);
    }
    
    out.println("<!-- Side info box end -->");
    out.print("</");
    out.print(blockElement);
    out.println(">");

}

%>