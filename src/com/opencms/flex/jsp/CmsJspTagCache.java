/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagCache.java,v $
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
 * This is an experimental tag for caching element support.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsJspTagCache extends javax.servlet.jsp.tagext.BodyTagSupport implements I_CmsJspConstants {

    private String m_key = null;
    
    public void setKey(String key) {
        if (key != null) {
            m_key = key.toLowerCase();
        }
    }
    
    public String getKey() {
        return m_key!=null?m_key:"";
    }
    
    public void release() {
        super.release();
        m_key = null;
    }
    
    /** Debugging on / off */
    private static final boolean DEBUG = false;
    
    public int doAfterBody() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        javax.servlet.ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if ((req instanceof com.opencms.flex.cache.CmsFlexRequest) &&
            (res instanceof com.opencms.flex.cache.CmsFlexResponse)) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;
            com.opencms.flex.cache.CmsFlexResponse c_res = (com.opencms.flex.cache.CmsFlexResponse)res;   
                
            try {       

                com.opencms.file.CmsObject cms = c_req.getCmsObject();

                // Get link-string from the body and reset body 
                javax.servlet.jsp.tagext.BodyContent body = this.getBodyContent();
                // String link = body.getString();            
                // body.clearBody();            

                this.getPreviousOut().print("The key is: [" + m_key + "]");            

            } catch (Exception ex) {
                System.err.println("Error in Jsp 'cache' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }            
        }
        // return SKIP_BODY;
        return EVAL_BODY_AGAIN;
    }
}
