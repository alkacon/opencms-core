/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagLink.java,v $
* Date   : $Date: 2002/12/12 18:42:40 $
* Version: $Revision: 1.7 $
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

import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.util.LinkSubstitution;

import javax.servlet.jsp.JspException;

/**
 * Implements the <code>&lt;cms:link&gt;[filename]&lt;/cms:link&gt;</code> 
 * tag to add OpenCms managed links to a JSP page, required for the static 
 * export to work properly. 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsJspTagLink extends javax.servlet.jsp.tagext.BodyTagSupport {
    
    /** Debugging on / off */
    private static final boolean DEBUG = false;

    /**
     * Default JSP method to process the tag data.
     * 
     * @return EVAL_PAGE
     * 
     * @throws JspException in case of trouble calculating the link or writing the output to the JSP
     */
    public int doEndTag() throws JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            CmsFlexRequest c_req = (CmsFlexRequest)req;
                
            try {
                // Get link-string from the body and reset body 
                String link = this.getBodyContent().getString();                          
                this.getBodyContent().clear();            
                // Calculate the link substitution
                String newlink = linkTagAction(link, c_req);
                // Write the result back to the page                
                this.getBodyContent().print(newlink);
                this.getBodyContent().writeOut(pageContext.getOut());

            } catch (Exception ex) {
                System.err.println("Error in Jsp 'link' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new JspException(ex);
            }            
        }
        return EVAL_PAGE;        
    }
    
    /**
     * Calulates a link using the OpenCms link export rules using the
     * given CmsFlexRequest to access the link substitutor.<p>
     * 
     * @param link the link that should be calculated, can be relative or absolute
     * @param req the current request
     * 
     * @return the calculated link
     * 
     * @see com.opencms.util.LinkSubstitution#getLinkSubstitution(CmsObject, String)
     */
    public static String linkTagAction(String link, CmsFlexRequest req) {
        if (link.indexOf(':') >= 0) {
            return LinkSubstitution.getLinkSubstitution(req.getCmsObject(), link);
        } else {
            return LinkSubstitution.getLinkSubstitution(req.getCmsObject(), req.toAbsolute(link));
        }        
    }
}
