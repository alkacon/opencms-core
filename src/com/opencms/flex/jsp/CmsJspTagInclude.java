/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInclude.java,v $
* Date   : $Date: 2002/09/03 19:45:27 $
* Version: $Revision: 1.4 $
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

import com.opencms.flex.util.CmsPropertyLookup;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * This Tag is used to include another OpenCms managed resource in a JSP.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsJspTagInclude extends BodyTagSupport implements I_CmsJspConstants { 
    
    private String m_target = null;
    private String m_page = null;
    private String m_suffix = null;
    private String m_property = null;    
    private String m_body = null;
    
    /** Debugging on / off */
    private static final boolean DEBUG = false;
    
    /**
     * Sets the include page/file target.
     * @param target The target to set
     */
    public void setPage(String page) {
        if (page != null) {
            m_page = page.toLowerCase();
            updateTarget();
        }
    }
    
    /**
     * Returns the include page/file target.
     * @return String
     */
    public String getPage() {
        return m_page!=null?m_page:"";
    }

    /**
     * Sets the include page/file target.
     * @param target The target to set
     */
    public void setFile(String file) {
        setPage(file);
    }
    
    /**
     * Returns the include page/file target.
     * @return String
     */
    public String getFile() {
        return getPage();
    }

    /**
     * Returns the property.
     * @return String
     */
    public String getProperty() {
        return m_property!=null?m_property:"";
    }

    /**
     * Sets the property.
     * @param property The property to set
     */
    public void setProperty(String property) {
        if (property != null) {
            this.m_property = property.toLowerCase();
        }
    }

    /**
     * Returns the suffix.
     * @return String
     */
    public String getSuffix() {
        return m_suffix!=null?m_suffix:"";
    }

    /**
     * Sets the suffix.
     * @param suffix The suffix to set
     */
    public void setSuffix(String suffix) {
        if (suffix != null) {
            this.m_suffix = suffix.toLowerCase();
            updateTarget();
        }
    }
    
    /**
     * Sets the include body attribute to indicate the body is to be evaluated.
     * @param value This must be "eval", otherweise the TEI will generate an error.
     */
    public void setBody(String value) {
        m_body = value;
    }
        

    /**
     * Internal utility method to update the target.
     */
    private void updateTarget() {
        if ((m_page != null) && (m_suffix != null)) {
            m_target = m_page + m_suffix;
        } else if (m_page != null) {
            m_target = m_page;
        }
    }
    
    public void release() {
        super.release();
        m_target = null;
        m_page = null;
        m_suffix = null;
        m_property = null;           
        m_body = null; 
    }
    
    public int doStartTag() throws JspException {
        if (m_body == null) return SKIP_BODY;
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        javax.servlet.ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if ((req instanceof com.opencms.flex.cache.CmsFlexRequest) &&
            (res instanceof com.opencms.flex.cache.CmsFlexResponse)) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;
            com.opencms.flex.cache.CmsFlexResponse c_res = (com.opencms.flex.cache.CmsFlexResponse)res;    

            String target = null;
            
            // Try to find out what to do
            if (m_target != null) {
                // Option 1: target is set with page/file attribute
                target = m_target;
            } else if (m_property != null) {            
                // Option 2: target is set in property attribute
                try { 
                    String prop = CmsPropertyLookup.lookupProperty(c_req.getCmsObject(), c_req.getCmsRequestedResource(), m_property, true);
                    target = prop + getSuffix();
                } catch (Exception e) {} // target will be null
            } else {
                // Option 3: target is set in body
                String body = null;
                if (getBodyContent() != null) {
                    body = getBodyContent().getString();
                    if ((body != null) && (! "".equals(body.trim()))) {
                        target = body;
                    }
                }
            } 
            
            if (target == null) {
                throw new JspException("CmsJspIncludeTag: No target specified!");
            }
                        
            try {
                javax.servlet.jsp.JspWriter out = pageContext.getOut();
             
                // Write out a C_FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
                out.print((char)com.opencms.flex.cache.CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
                
                // Add an element to the include list (will be initialized if empty)
                c_res.addToIncludeList(target);
                
                // CmsResponse w_res = new CmsResponse(c_res, target, true);
                c_req.getCmsRequestDispatcher(target).include(c_req, c_res);    
                
            } catch (javax.servlet.ServletException e) {
                if (DEBUG) System.err.println("JspTagInclude: ServletException in Jsp 'include' tag processing: " + e);
                if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
                throw new JspException(e);            
            } catch (java.io.IOException e) {
                if (DEBUG) System.err.println("JspTagInclude: IOException in Jsp 'include' tag processing: " + e);
                if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
                throw new JspException(e);
            }            
        }
        
        return EVAL_PAGE;
    }
}
