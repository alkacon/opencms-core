/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInclude.java,v $
 * Date   : $Date: 2003/06/13 10:04:21 $
 * Version: $Revision: 1.25 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsResource;
import com.opencms.flex.cache.CmsFlexController;
import com.opencms.launcher.CmsXmlLauncher;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to include another OpenCms managed resource in a JSP.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.25 $
 */
public class CmsJspTagInclude extends BodyTagSupport implements I_CmsJspTagParamParent { 
    
    // Attribute member variables
    private String m_target = null;
    private String m_suffix = null;
    private String m_property = null;    
    private String m_attribute = null;    
    private String m_element = null;
    
    /** Hashmap to save paramters to the include in */
    private HashMap m_parameterMap = null;
        
    /** Debugging on / off */
    private static final boolean DEBUG = false;
    
    /** URI of the bodyloader XML file in the OpenCms VFS*/    
    public static final String C_BODYLOADER_URI = "/system/shared/bodyloader.html";
        
    /**
     * Sets the include page target.<p>
     * 
     * @param target the target to set
     */
    public void setPage(String target) {
        if (target != null) {
            m_target = target.toLowerCase();
        }
    }
    
    /**
     * Returns the include page target.<p>
     * 
     * @return the include page target
     */
    public String getPage() {
        return m_target!=null?m_target:"";
    }

    /**
     * Sets the file, same aus using <code>setPage()</code>
     * 
     * @param file the file to set
     * @see #setPage(String)
     */
    public void setFile(String file) {
        setPage(file);
    }
    
    /**
     * Returns the value of <code>getPage()</code>.<p>
     * 
     * @return the value of <code>getPage()</code>
     * @see #getPage()
     */
    public String getFile() {
        return getPage();
    }

    /**
     * Returns the property.<p>
     * 
     * @return the property
     */
    public String getProperty() {
        return m_property!=null?m_property:"";
    }

    /**
     * Sets the property.<p>
     * 
     * @param property the property to set
     */
    public void setProperty(String property) {
        if (property != null) {
            this.m_property = property;
        }
    }
    
    /**
     * Returns the attribute.<p>
     * 
     * @return the attribute
     */
    public String getAttribute() {
        return m_attribute!=null?m_attribute:"";
    }

    /**
     * Sets the attribute.<p>
     * 
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {
        if (attribute != null) {
            this.m_attribute = attribute;
        }
    }    

    /**
     * Returns the suffix.<p>
     * 
     * @return the suffix
     */
    public String getSuffix() {
        return m_suffix!=null?m_suffix:"";
    }

    /**
     * Sets the suffix.<p>
     * 
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {
        if (suffix != null) {
            this.m_suffix = suffix.toLowerCase();
        }
    }

    /**
     * Returns the element.<p>
     * 
     * @return the element
     */ 
    public String getElement() {
        return m_element;
    }

    /**
     * Sets the element.<p>
     * 
     * @param element the element to set
     */
    public void setElement(String element) {
        if (element != null) {
            this.m_element = element;
        }
    }
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        m_target = null;
        m_suffix = null;
        m_property = null;           
        m_element = null;
        m_parameterMap = null;
    }  
      
    /**
     * @return <code>EVAL_BODY_BUFFERED</code>
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     * @throws JspException by interface default
     */    
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }    
    
    /**
     * @return <code>EVAL_PAGE</code>
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @throws JspException by interface default
     */
    public int doEndTag() throws JspException {
        
        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME); 
            String target = null;                       
            
            // Try to find out what to do
            if (m_target != null) {
                // Option 1: target is set with "page" or "file" parameter
                target = m_target + getSuffix();
            } else if (m_property != null) {            
                // Option 2: target is set with "property" parameter
                if (DEBUG) System.err.println("IncludeTag: property=" + m_property);
                try { 
                    String prop = controller.getCmsObject().readProperty(controller.getCmsObject().getRequestContext().getUri(), m_property, true);
                    if (DEBUG) System.err.println("IncludeTag: property=" + m_property + " is " + prop);                    
                    if (prop != null) target = prop + getSuffix();
                } catch (Exception e) {} // target will be null
            } else if (m_attribute != null) {            
                // Option 3: target is set in "attribute" parameter
                try { 
                    String attr = (String)req.getAttribute(m_attribute);
                    if (attr != null) target = attr + getSuffix();
                } catch (Exception e) {} // target will be null
            } else {
                // Option 4: target might be set in body
                String body = null;
                if (getBodyContent() != null) {
                    body = getBodyContent().getString();
                    if ((body != null) && (! "".equals(body.trim()))) {
                        // target IS set in body
                        target = body + getSuffix();
                    } 
                    // else target is not set at all, default will be used 
                }
            } 
              
            // no perfrom the include action
            includeTagAction(pageContext, target, m_element, m_parameterMap, req, res);
            
            // must call release here manually to make sure m_parameterMap is cleared
            release();
        }
        
        return EVAL_PAGE;
    }
    
    /**
     * Include action method.<p>
     * 
     * The logic in this mehod is more complex than it should be.
     * This is because of the XMLTemplate integration, which requires some settings 
     * to the parameters understandable only to XMLTemplate gurus.
     * By putting this logic here it is not required to care about these issues
     * on JSP pages, and you end up with considerable less JSP code.
     * Also JSP developers need not to know the intrinsics of XMLTemplates this way.<p>
     * 
     * @param context the current JSP page context
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param paramMap a map of parameters for the include, will be merged with the request 
     *      parameters, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws JspException in case something goes wrong
     */
    static void includeTagAction(PageContext context, String target, String element, Map paramMap, ServletRequest req, ServletResponse res) 
    throws JspException {
        
        if (DEBUG) System.err.println("includeTagAction/1: target=" + target);
        
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        
        if (target == null) {
            // set target to default
            target = controller.getCmsObject().getRequestContext().getUri();
            if (DEBUG) System.err.println("includeTagAction/2: target=" + target);
        }
                
        Map parameterMap = new HashMap();      
        if (paramMap != null) {
            // add all parameters 
            parameterMap.putAll(paramMap);
        }                    
        
        if (element != null) {            
            // add template element selector for JSP templates
            addParameter(parameterMap, CmsJspTagTemplate.C_TEMPLATE_ELEMENT, element, true);
            if (!("body".equals(element) || "(default)".equals(element))) {
                // add template selector for multiple body XML files
                addParameter(parameterMap, CmsXmlTemplate.C_FRAME_SELECTOR, element, true);
            }
        }
                       
        // try to figure out if the target is a page or XMLTemplate file        
        boolean isPageTarget = false;         
        try {
            target = controller.getCurrentRequest().toAbsolute(target);
            CmsResource resource = controller.getCmsObject().readFileHeader(target);
            isPageTarget = ((controller.getCmsObject().getResourceType("page").getResourceType() == resource.getType()));
        } catch (CmsException e) {
            // any exception here and we will treat his as non-Page file
            throw new JspException("File not found: " + target, e);
        }
                
        String bodyAttribute = (String) controller.getCmsObject().getRequestContext().getAttribute(I_CmsConstants.C_XML_BODY_ELEMENT);               
        if (bodyAttribute == null) {
            // no body attribute is set: this is NOT a sub-element in a XML mastertemplate
            if (isPageTarget) {
                // add body file path to target 
                if (! target.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
                    target = I_CmsWpConstants.C_VFS_PATH_BODIES + target.substring(1);
                }              
                // save target as "element replace" parameter  
                addParameter(parameterMap, CmsXmlLauncher.C_ELEMENT_REPLACE, "body:" + target, true);  
                target = C_BODYLOADER_URI;              
            } 
            // for other cases setting of "target" is fine 
        } else {
            // body attribute is set: this is a sub-element in a XML mastertemplate
            if (target.equals(controller.getCmsObject().getRequestContext().getUri())) {
                // target can be ignored, set body attribute as "element replace" parameter  
                addParameter(parameterMap, CmsXmlLauncher.C_ELEMENT_REPLACE, "body:" + bodyAttribute, true);
                // redirect target to body loader
                target = C_BODYLOADER_URI;                
            } else {
                if (isPageTarget) {
                    // add body file path to target 
                    if (! target.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
                        target = I_CmsWpConstants.C_VFS_PATH_BODIES + target.substring(1);
                    }              
                    // save target as "element replace" parameter  
                    addParameter(parameterMap, CmsXmlLauncher.C_ELEMENT_REPLACE, "body:" + target, true);  
                    target = C_BODYLOADER_URI;                     
                }
            }
            // for other cases setting of "target" is fine             
        }          

        if (DEBUG) System.err.println("includeTagAction/3: target=" + target);
       
        // save old parameters from request
        Map oldParamterMap = req.getParameterMap();
        controller.getCurrentRequest().addParameterMap(parameterMap);  
        
        try {         
            // Write out a C_FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
            context.getOut().print((char)com.opencms.flex.cache.CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
            
            // Add an element to the include list (will be initialized if empty)
            controller.getCurrentResponse().addToIncludeList(target, parameterMap);
            
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, res);    
            
        } catch (javax.servlet.ServletException e) {
            if (DEBUG) System.err.println("JspTagInclude: ServletException in Jsp 'include' tag processing: " + e);
            if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
            throw new JspException(e);            
        } catch (java.io.IOException e) {
            if (DEBUG) System.err.println("JspTagInclude: IOException in Jsp 'include' tag processing: " + e);
            if (DEBUG) System.err.println(com.opencms.util.Utils.getStackTrace(e));                
            throw new JspException(e);
        } finally {
            if (oldParamterMap != null) controller.getCurrentRequest().setParameterMap(oldParamterMap);            
        }           
    }
    
	/**
     * This methods adds parameters to the current request.
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
     * @param name the name to add
     * @param value the value to add
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
        
        addParameter(m_parameterMap, name, value, false);
    }

    /**
     * Internal method to add parameters.<p>
     * 
     * @param parameters the Map to add the parameters to
     * @param name the name to add
     * @param value the value to add
     * @param overwrite if <code>true</code>, a parameter in the map will be overwritten by
     *      a parameter with the same name, otherwise the request will have multiple parameters 
     *      with the same name (which is possible in http requests)
     */
    private static void addParameter(Map parameters, String name, String value, boolean overwrite) {
        // No null values allowed in parameters
        if ((parameters == null) || (name == null) || (value == null)) return;
        
        // Check if the parameter name (key) exists
        if (parameters.containsKey(name) && (! overwrite)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = (String[]) parameters.get(name);
            String[] newValues = new String[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            parameters.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] {value};
            parameters.put(name, values);
        } 
    }
}
