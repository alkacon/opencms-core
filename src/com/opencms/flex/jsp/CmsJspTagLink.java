/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagLink.java,v $
* Date   : $Date: 2002/08/21 11:29:32 $
* Version: $Revision: 1.2 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2002  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.flex.jsp;

/**
 * This Tag is used to add OpenCms managed links to a JSP.
 * Required for the static export to work.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsJspTagLink extends javax.servlet.jsp.tagext.BodyTagSupport implements I_CmsJspConstants {
    
    /** One static substitutor should be enough */
    private static com.opencms.util.LinkSubstitution m_substitutor = new com.opencms.util.LinkSubstitution();   
    
    /** Debugging on / off */
    private static final boolean DEBUG = false;

    public int doEndTag() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;
                
            try {       

                com.opencms.file.CmsObject cms = c_req.getCmsObject();

                // Get link-string from the body and reset body 
                javax.servlet.jsp.tagext.BodyContent body = this.getBodyContent();
                String link = body.getString();            

                // Cache to store found subsitutions in, reduces access to subtitutor 
                java.util.Hashtable substitutions = null;

                // Aqquire a hashtable with all link substitutions
                // This is saved in the page context so that the database is not accessed more then once per page
                Object o = pageContext.getAttribute(this.C_JSP_ATTR_TAGLINK);
                if (o == null ) {
                    substitutions = new java.util.Hashtable();
                    // Save property hashtable in page context
                } else {
                    // Properties have already been loaded, just reuse them
                    substitutions = (java.util.Hashtable)o;
                }

                // Now check if we have already stored that link substitution
                String newlink = null;
                boolean changed = false;
                if (substitutions.containsKey(link)) {
                    newlink = (String)substitutions.get(link);
                    if (DEBUG) System.err.println("Reused link substitution for :" + link);
                } else {
                    // Substitution is not stored, so we must calculate it
                    if (link.indexOf(':') >= 0) {
                        newlink = m_substitutor.getLinkSubstitution(cms, link);
                    } else {
                        newlink = m_substitutor.getLinkSubstitution(cms, c_req.toAbsolute(link));
                    }
                    substitutions.put(link, newlink);
                    changed = true;
                    if (DEBUG) System.err.println("Stored new link substitution for :" + link);
                }
                                
                this.getBodyContent().clear();            
                this.getBodyContent().print(newlink);
                this.getBodyContent().writeOut(pageContext.getOut());
                /*
                body.clear();            
                body.print(newlink);
                body.writeOut(pageContext.getOut());
                */
                // Finally store the hashtable with the substitutions back in the page context
                if (changed) {
                    pageContext.setAttribute(this.C_JSP_ATTR_TAGLINK, substitutions);
                }

            } catch (Exception ex) {
                System.err.println("Error in Jsp 'link' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }            
        }
        return EVAL_PAGE;        
    }
}
