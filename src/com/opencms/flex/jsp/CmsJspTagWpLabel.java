/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagWpLabel.java,v $
* Date   : $Date: 2002/10/30 10:25:45 $
* Version: $Revision: 1.3 $
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

import com.opencms.core.CmsException;
import com.opencms.flex.cache.CmsFlexRequest;

/**
 * This Tag provides access to the labels stored in the
 * language files of the OpenCms workplace.<p>
 * 
 * This is rather a "proof-of-concept" then a perfect implementation.
 * The overhead required to read and parse the XML language files 
 * is to high and performance would be hit seriously if this tag 
 * is used often on a page.<p>
 * 
 * Instead of using the XML based workplace tags one should 
 * consider using standard Java resource bundles to provide language independent 
 * implementations.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsJspTagWpLabel extends javax.servlet.jsp.tagext.BodyTagSupport {
    
    /** Attribute to store WP label definition file */
    public static final String C_JSP_ATTR_TAGLABEL_DEF = "com.opencms.flex.CmsJspTagWpLabel.labeldeffile";
    
    /** Attribute to store WP language file */
    public static final String C_JSP_ATTR_TAGLABEL_LANG = "com.opencms.flex.CmsJspTagWpLabel.langfile";
            
    public int doAfterBody() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;
               
            try {       
                
                // Get label string from the body and reset body 
                javax.servlet.jsp.tagext.BodyContent body = this.getBodyContent();
                String label = body.getString();            
                body.clearBody();  
                
                // Get the result...
                String result = wpLabelTagAction(label, c_req);
                this.getPreviousOut().print(result);
                
            } catch (Exception ex) {
                System.err.println("Error in Jsp 'label' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }            
        }
        return SKIP_BODY;
    }
    
    public static String wpLabelTagAction(String label, CmsFlexRequest req) 
    throws CmsException {
        com.opencms.workplace.CmsXmlWpLabelDefFile labeldeffile;
        com.opencms.workplace.CmsXmlLanguageFile langfile;
        
        labeldeffile = new com.opencms.workplace.CmsXmlWpLabelDefFile(req.getCmsObject(), "/system/workplace/templates/" + com.opencms.workplace.I_CmsWpConstants.C_LABELTEMPLATE);
        langfile = new com.opencms.workplace.CmsXmlLanguageFile(req.getCmsObject());
        
        return labeldeffile.getLabel(langfile.getLanguageValue(label));                        
    }

}
