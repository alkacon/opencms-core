/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInclude.java,v $
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
 * This Tag is used to include another OpenCms managed resource in a JSP.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsJspTagInclude extends javax.servlet.jsp.tagext.TagSupport implements I_CmsJspConstants { 
    
    private String m_target = null;
    
    /** Debugging on / off */
    private static final boolean DEBUG = false;
    
    public void setPage(String target) {
        if (target != null) {
            m_target = target.toLowerCase();
        }
    }
    
    public String getPage() {
        return m_target!=null?m_target:"";
    }
    
    public void release() {
        super.release();
        m_target = null;
    }

    public int doStartTag() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        javax.servlet.ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if ((req instanceof com.opencms.flex.cache.CmsFlexRequest) &&
            (res instanceof com.opencms.flex.cache.CmsFlexResponse)) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;
            com.opencms.flex.cache.CmsFlexResponse c_res = (com.opencms.flex.cache.CmsFlexResponse)res;    

            try {
                javax.servlet.jsp.JspWriter out = pageContext.getOut();
             
                // Write out a C_FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
                out.print((char)com.opencms.flex.cache.CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
                
                // Add an element to the include list (will be initialized if empty)
                c_res.addToIncludeList(m_target);
                
                // CmsResponse w_res = new CmsResponse(c_res, target, true);
                c_req.getCmsRequestDispatcher(m_target).include(c_req, c_res);    
                
            } catch (Exception e) {
                System.err.println("JspTagInclude: Error in Jsp 'include' tag processing: " + e);
                System.err.println(com.opencms.util.Utils.getStackTrace(e));                
                throw new javax.servlet.jsp.JspException(e);
            }
        }
        
        return SKIP_BODY;
    }
}
