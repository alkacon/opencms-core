/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInclude.java,v $
* Date   : $Date: 2003/01/20 17:57:52 $
* Version: $Revision: 1.16 $
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

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.flex.cache.CmsFlexResponse;
import com.opencms.launcher.CmsXmlLauncher;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * This Tag is used to include another OpenCms managed resource in a JSP.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.16 $
 */
public class CmsJspTagInclude extends BodyTagSupport implements I_CmsJspTagParamParent { 
    
    // Attribute member variables
    private String m_target = null;
    private String m_page = null;
    private String m_suffix = null;
    private String m_property = null;    
    private String m_attribute = null;    
    private String m_body = null;
    private String m_element = null;
    
    /** Hashmap to save paramters to the include in */
    private HashMap m_parameterMap = null;
        
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
            this.m_property = property;
        }
    }
    
    /**
     * Returns the attribute.
     * @return String
     */
    public String getAttribute() {
        return m_attribute!=null?m_attribute:"";
    }

    /**
     * Sets the attribute.
     * @param property The property to set
     */
    public void setAttribute(String attribute) {
        if (attribute != null) {
            this.m_attribute = attribute;
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
     * Returns the part.
     * @return String
     */
    public String getElement() {
        return m_element;
    }

    /**
     * Sets the part
     * @param part the part to set
     */
    public void setElement(String element) {
        if (element != null) {
            this.m_element = element.toLowerCase();
        }
    }
    
    /**
     * Sets the include body attribute to indicate the body is to be evaluated.
     * @param value This must be "eval" or "params", otherweise the TEI will generate an error.
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
        m_element = null;
        m_parameterMap = null;
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
            
            if (m_element != null) {
                addParameter(CmsJspTagTemplate.C_TEMPLATE_ELEMENT, m_element);
                // Check for body part and add special parameters for XMLTemplate if required
                if (I_CmsConstants.C_XML_BODY_ELEMENT.equalsIgnoreCase(m_element.trim())) {
                    // First check if a body was set in the cms request context
                    String body = (String)c_req.getCmsObject().getRequestContext().getAttribute(I_CmsConstants.C_XML_BODY_ELEMENT);
                    if (body == null) {
                        // no body was set, try to calculate the name (will not work with linked files, though)
                        body = I_CmsWpConstants.C_VFS_PATH_BODIES + c_req.getCmsRequestedResource().substring(1);
                    }
                    addParameter(CmsXmlLauncher.C_ELEMENT_REPLACE, "body:" + body);
                }
            }
            
            // Try to find out what to do
            if (m_target != null) {
                // Option 1: target is set with "page" or "file" parameter
                target = m_target;
            } else if (m_property != null) {            
                // Option 2: target is set with "property" parameter
                try { 
                    String prop = c_req.getCmsObject().readProperty(c_req.getCmsRequestedResource(), m_property, true);
                    target = prop + getSuffix();
                } catch (Exception e) {} // target will be null
            } else if (m_attribute != null) {            
                // Option 3: target is set in "attribute" parameter
                try { 
                    String attr = (String)c_req.getAttribute(m_attribute);
                    if (attr != null) target = attr + getSuffix();
                } catch (Exception e) {} // target will be null
            }else {
                // Option 4: target is set in body
                String body = null;
                if (getBodyContent() != null) {
                    body = getBodyContent().getString();
                    if ((body != null) && (! "".equals(body.trim()))) {
                        target = body;
                    }
                }
            } 
              
            includeTagAction(pageContext, target, m_parameterMap, c_req, c_res);
            
            // must call release here manually to make sure m_parameterMap is cleared
            release();
        }
        
        return EVAL_PAGE;
    }
    
    public static void includeTagAction(PageContext context, String target, Map parameterMap, CmsFlexRequest req, CmsFlexResponse res) 
    throws JspException {
        if (target == null) {
            throw new JspException("CmsJspIncludeTag: No target specified!");
        }
                                  
        java.util.Map oldParamterMap = null;        
        // Check parameters and update if required
        if (parameterMap != null) {
            oldParamterMap = req.getParameterMap();
            req.addParameterMap(parameterMap);                
        }
                                            
        try {         
            // Write out a C_FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
            context.getOut().print((char)com.opencms.flex.cache.CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
            
            // Add an element to the include list (will be initialized if empty)
            res.addToIncludeList(target, parameterMap);
            
            // CmsResponse w_res = new CmsResponse(c_res, target, true);
            req.getCmsRequestDispatcher(target).include(req, res);    
            
        } catch (javax.servlet.ServletException e) {
            if (DEBUG) System.err.println("JspTagInclude: ServletException in Jsp 'include' tag processing: " + e);
            if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
            throw new JspException(e);            
        } catch (java.io.IOException e) {
            if (DEBUG) System.err.println("JspTagInclude: IOException in Jsp 'include' tag processing: " + e);
            if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
            throw new JspException(e);
        } finally {
            if (oldParamterMap != null) req.setParameterMap(oldParamterMap);            
        }           
    }
    
	/**
     * This methods adds parameters to the FlexRequest. 
     * Parameters added here will be treated like parameters from the 
     * HttpRequest on included pages.<p>
     * 
     * Remember that the value for a parameter in a HttpRequest is a 
     * String array, not just a simple String. If a parameter added here does
     * not already exist in the HttpRequest, it will be added. If a parameter 
     * exists, another value will be added to the array of values. If the 
     * value already exists for the parameter, nothing will be added, since a 
     * value can appear only once per parameter.<p>
     * 
	 * @see com.opencms.flex.jsp.I_CmsJspTagParamParent#addParameter(String, String)
	 */
	public void addParameter(String name, String value) {
        // No null values allowed in parameters
        if ((name == null) || (value == null)) return;

        if (DEBUG) System.err.println("CmsJspIncludeTag.addParameter: param=" + name + " value=" + value);
        
        // Check if internal map exists, create new one if not
        if (m_parameterMap == null) {
            m_parameterMap = new HashMap();
        }
        
        // Check if the parameter name (key) exists
        if (m_parameterMap.containsKey(name)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = (String[]) m_parameterMap.get(name);
            String[] newValues = new String[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            m_parameterMap.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] { value };
            m_parameterMap.put(name, values);
        } 
    }

}
