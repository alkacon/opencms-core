/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagLink.java,v $
 * Date   : $Date: 2003/09/12 10:01:53 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.flex.jsp;

import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;

import com.opencms.flex.cache.CmsFlexController;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Implements the <code>&lt;cms:link&gt;[filename]&lt;/cms:link&gt;</code> 
 * tag to add OpenCms managed links to a JSP page, required for link
 * management and the static 
 * export to work properly.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.18 $
 */
public class CmsJspTagLink extends javax.servlet.jsp.tagext.BodyTagSupport {
 
    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @return EVAL_PAGE
     * @throws JspException in case soemthing goes wrong
     */
    public int doEndTag() throws JspException {
        
        ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            try {
                // Get link-string from the body and reset body 
                String link = this.getBodyContent().getString();                          
                this.getBodyContent().clear();            
                // Calculate the link substitution
                String newlink = linkTagAction(link, req);
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
     * Internal action method.<p>
     * 
     * Calulates a link using the OpenCms link export rules.<p>
     * 
     * @param link the link that should be calculated, can be relative or absolute
     * @param req the current request
     * @return the calculated link
     * 
     * @see org.opencms.staticexport.CmsLinkManager#substituteLink(CmsObject, String)
     */
    public static String linkTagAction(String link, ServletRequest req) {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        if (link.indexOf(':') >= 0) {
            return OpenCms.getLinkManager().substituteLink(controller.getCmsObject(), link);
        } else {
            return OpenCms.getLinkManager().substituteLink(controller.getCmsObject(), CmsLinkManager.getAbsoluteUri(link, controller.getCurrentRequest().getElementUri()));
        }        
    }
}
