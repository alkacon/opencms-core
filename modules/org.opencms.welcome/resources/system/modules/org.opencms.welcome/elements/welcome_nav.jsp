<%@ page import="org.opencms.jsp.*" %><%	

    /*      
     * This is a simple example on how to build a dynamic navigation using JSP in OpenCms.
     * It serves the purpose of demonstrating 
     * the general way to build a dynamic navigation using OpenCms resource properties.
     */
     
    // Create a JSP action element
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	 
	// Some shortcuts to often used Strings
	String filename = cms.getRequestContext().getUri();

	// List of all pages / subfolders (sorted by NavPos property)
	java.util.List list = cms.getNavigation().getNavigationForFolder();	
    java.util.Iterator i = list.iterator();

	// Now build the navigation
	out.println("<p class=\"small\">Navigation: ");
	
    while (i.hasNext()) {
    	CmsJspNavElement ne = (CmsJspNavElement)i.next();
		if (! ne.getResourceName().equals(filename)) {						
			out.println("<a href=\"" + 
			cms.link(ne.getResourceName()) + "\">" 
			+ ne.getNavText() + "</a>");			
		} else {
			out.println(ne.getNavText());						
		}    	
		if (i.hasNext()) {
			out.println(" | ");
		}
    }
	out.println("</p>");  	
%>