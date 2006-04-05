<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*,org.opencms.util.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

String sideUri = request.getParameter("style_side_uri");
boolean showSideUri = CmsStringUtil.isNotEmpty(sideUri) && !"none".equals(sideUri);

String xmlDemo = cms.property("xmlcontent-side-uri", "search");
boolean showXmlDemo = CmsStringUtil.isNotEmpty(xmlDemo) && !"none".equals(xmlDemo);

String newsList = cms.property(CmsTemplateBean.PROPERTY_LAYOUT_RIGHT, "search", CmsTemplateBean.PROPERTY_VALUE_NONE);
boolean showList = ! CmsTemplateBean.PROPERTY_VALUE_NONE.equals(newsList);

boolean showSide = showSideUri || showList || showXmlDemo;

// create info box
if (showSide) {
	String spacer = "<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n";
	
    String blockElement = "td";
    if (cms.showAccessibleVersion()) {
        blockElement = "div";    
    }
    
    out.print("<");
    out.print(blockElement);
    out.print(" class=\"infobox\">\n");
    out.println("<!-- Side info box start -->\n");
    
    if (showSideUri) {    	
		cms.includeSilent(sideUri, "text1", true);
    	out.println(spacer);
    }
    
    if (showList) {
    	cms.includeRightLists();
    	if (showSideUri || showXmlDemo) {
    		out.println(spacer);
    	}
    }
    
    if (showXmlDemo) {
		cms.includeSilent("/system/modules/org.opencms.frontend.templateone.xmlcontentdemo/elements/side_article_list.jsp", null, true);
		if (showSideUri) {
			out.println(spacer);
		}
	}
    
    if (showSideUri) {    	
		cms.includeSilent(sideUri, "text2", true);
    }
    
    out.println("<!-- Side info box end -->");
    out.print("</");
    out.print(blockElement);
    out.println(">");

}

%>