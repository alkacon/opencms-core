<%@ page import="com.opencms.core.I_CmsConstants,com.opencms.flex.jsp.CmsJspNavElement" %><%	

	/*	    
	 * This is a simple example on how to build a dynamic navigation using JSP in OpenCms.
	 * It is not an exercise in good JSP programming techniques, since for that there is far 
	 * too much java scriplet code on this page. However, it serves the purpose of demonstrating 
	 * the general way to build a dynamic navigation using OpenCms resource properties.
	 */

    // Link substitutor to make sure static export works 
    com.opencms.util.LinkSubstitution subst = new com.opencms.util.LinkSubstitution();

	// Collect the objects required to access the OpenCms VFS from the request
	com.opencms.flex.cache.CmsFlexRequest req = (com.opencms.flex.cache.CmsFlexRequest)request;
	com.opencms.file.CmsObject cms = req.getCmsObject();
	
	// Some shortcuts to often used Strings
	String filename = cms.getRequestContext().getUri();
	String foldername = cms.getRequestContext().getFolderUri();

	// List of all pages / subfolders (sorted by NavPos property)
	java.util.ArrayList list = CmsJspNavElement.getNavigationForFolder(cms, foldername);	
    java.util.Iterator i = list.iterator();

	// Now build the navigation
	out.println("<p class=\"small\">");
    while (i.hasNext()) {
    	CmsJspNavElement ne = (CmsJspNavElement)i.next();
		if (! ne.getResourceName().equals(filename)) {						
			out.println("<a href=\"" + 
			subst.getLinkSubstitution(cms, ne.getResourceName()) + "\">" 
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