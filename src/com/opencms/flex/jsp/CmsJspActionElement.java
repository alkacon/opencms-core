/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspActionElement.java,v $
 * Date   : $Date: 2003/03/02 13:56:43 $
 * Version: $Revision: 1.9 $
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
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.flex.cache.CmsFlexResponse;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Bean to be used on JSP pages scriplet code that provides direct 
 * access to the functionality offered by the opencms taglib.<p>
 * 
 * By instanciating a bean of this type and accessing the methods provided by 
 * the instance, all functionality of the OpenCms JSP taglib can be easily 
 * used from within JSP scriplet code,<p>
 * 
 * Initialize this bean at the beginning of your JSP like this:
 * <pre>
 * &lt;jsp:useBean id="cms" class="com.opencms.flex.jsp.CmsJspActionElement"&gt;
 * &lt% cms.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.9 $
 * 
 * @since 5.0 beta 2
 */
public class CmsJspActionElement {

    /** OpenCms JSP request */
    private CmsFlexRequest m_request;

    /** OpenCms JSP response */
    private CmsFlexResponse m_response;

    /** JSP page context */
    private PageContext m_context;
    
    /** JSP navigation builder */
    private CmsJspNavBuilder m_navigation = null;    
    
    /** Flag to indicate that this bean was properly initialized */
    private boolean m_notInitialized;
    
    /** Error message in case bean was not properly initialized */
    public final static String C_NOT_INITIALIZED = "+++ CmsJspActionElement not initialized +++";

    /**
     * Empty constructor, required for every JavaBean.
     */
    public CmsJspActionElement() {
        m_notInitialized = true;
    }
    
    /**
     * Constructor, with parameters.
     * 
     * @param content the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsJspActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        m_notInitialized = true;
        init(context, req, res);
    }    
    
    /**
     * Initialize the bean with the current page context, request and response.<p>
     * 
     * It is required to call one of the init() methods before you can use the 
     * instance of this bean.
     * 
     * @param content the JSP page context object
     * @param req the JSP request casted to a CmsFlexRequest
     * @param res the JSP response casted to a CmsFlexResponse
     */
    public void init(PageContext context, CmsFlexRequest req, CmsFlexResponse res) {
        m_context = context;
        m_request = req;
        m_response = res;
        m_notInitialized = false;
    }
    
    /**
     * Initialize the bean with the current page context, request and response.<p>
     * 
     * It is required to call one of the init() methods before you can use the 
     * instance of this bean.
     * 
     * @param content the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        try {
            this.init(context, (CmsFlexRequest)req, (CmsFlexResponse)res);
        } catch (ClassCastException e) {
            // probably request / response where not of the right type
            m_notInitialized = true;
        }
    }    

    /**
     * Returns the CmsObject from the wrapped request.<p>
     *
     * This is a convenience method in case you need access to
     * the CmsObject in your JSP scriplets.
     *
     * @return the CmsObject from the wrapped request
     *
     * @see com.opencms.flex.cache.CmsFlexRequest#getCmsObject()
     */
    public CmsObject getCmsObject() {
        if (m_notInitialized) return null;
        return m_request.getCmsObject();
    }
    
    /**
     * Include a sub-element without paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." /&gt;</code> tag.
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @throws JspException in case there were problems including the target
     *
     * @see com.opencms.flex.jsp.CmsJspTagInclude
     */
    public void include(String target) throws JspException {
        this.include(target, null, null);
    }
    
    /**
     * Include a named sub-element without paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." element="..." /&gt;</code> tag.
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @throws JspException in case there were problems including the target
     *
     * @see com.opencms.flex.jsp.CmsJspTagInclude
     */    
    public void include(String target, String element) throws JspException  {
        this.include(target, element, null);
    }

    /**
     * Include a named sub-element with paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." element="..." /&gt;</code> tag
     * with parameters in the tag body.<p>
     * 
     * The parameter map should be a map where the keys are Strings 
     * (the parameter names) and the values are of type String[].
     * However, as a convenience feature,
     * in case you provide just a String for the parameter value, 
     * it will automatically be translated to a String[1].<p>
     * 
     * The handling of the <code>element</code> parameter depends on the 
     * included file type. Most often it is used as template selector.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see com.opencms.flex.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, Map parameterMap) throws JspException {
        if (m_notInitialized) return;
        if (parameterMap != null) {
            try {
                // ensure parameters are always of type String[] not just String
                Iterator i = parameterMap.keySet().iterator();
                while (i.hasNext()) {
                    String key = (String)i.next();
                    Object value = parameterMap.get(key);
                    if (value instanceof String) {
                        String[] newValue = new String[] {(String)value };
                        parameterMap.put(key, newValue);
                    }
                }
            } catch (UnsupportedOperationException e) {
                // parameter map is immutable, just use it "as is"
            }
        }
        CmsJspTagInclude.includeTagAction(m_context, target, element, parameterMap, m_request, m_response);
    }
            
    /**
     * Calculate a link with the OpenCms link management,
     * same as using the <code>&lt;cms:link&gt;...&lt;/cms:link&gt;</code> tag.<p>
     * 
     * This is important to get the right link for exported resources, 
     * e.g. for images in the online project.
     * 
     * @param link the uri in the OpenCms to link to
     * @return the translated link
     * 
     * @see  com.opencms.flex.jsp.CmsJspTagLink
     */
    public String link(String link) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        return CmsJspTagLink.linkTagAction(link, m_request);
    }
    
    /**
     * Returns a selected user property, i.e. information about the currently
     * logged in user, same as using 
     * the <code>&lt;cms:user property="..." /&gt;</code> tag.<p>
     * 
     * @param property the user property to display, please see the tag documentation for valid options
     * @return the value of the selected user property
     * 
     * @see  com.opencms.flex.jsp.CmsJspTagUser
     */
    public String user(String property) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        return CmsJspTagUser.userTagAction(property, m_request);
    }
    
    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." file="..." /&gt;</code> tag.<p>
     * 
     * The <code>file</code> parameter controls from which file the
     * property is read. Keep in mind that there are two basic options
     * to read the property from: The file that was requested by the
     * user is the obvious first option. However, a JSP might also have been 
     * included as a sub-element on a page, and so the currently processed JSP 
     * file is the second option.<p>
     * 
     * Valid options for the <code>file</code> parameter are:<br><ul>
     * <li><code>"this"</code>: look for the property only at the currently 
     *     processed file, which could be an included sub-element
     * <li><code>"parent"</code>: look for the property only at the file requested by the user
     * <li><code>"search-this":</code> look for the property at <code>"this"</code> file. 
     *     If not found, walk all folders upwards to the root folder and look 
     *     at the folders for the property, return the first value found.
     * <li><code>"search-parent"</code> or <code>"search"</code>:
     *     look for the property at the <code>"parent"</code> file.
     *     If not found, walk all folders upwards to the root folder and look 
     *     at the folders for the property, return the first value found.
     * <li><code>[filename]</code>: Look for the property only at 
     *     the file <code>[filename]</code>. This way you can read a property
     *     from any file in the VFS.
     * </ul><p>
     * 
     * If the named property could not be read from the selected file, 
     * <code>null</code> is returned.<p>
     * 
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        return this.property(name, file, null, false);       
    }

    /**
     * Returns a selected file property value, same as using
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag.<p>
     *
     * Please see the description of {@link #property(String, String)} for
     * valid options of the <code>file</code> parameter.<p>
     *
     * If the named property could not be read from the selected file,
     * the value of <code>defaultValue</code> is returned.<p>
     *
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @param defaultValue a default value in case the property was not found
     * @return the value of the property found, or the value of defaultValue
     *     if the property could not be found
     *
     * @see  com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        return this.property(name, file, defaultValue, false);
    }
            
    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag.<p>
     * 
     * Please see the description of {@link #property(String, String)} for
     * valid options of the <code>file</code> parameter.<p>
     * 
     * If the named property could not be read from the selected file, 
     * the value of <code>defaultValue</code> is returned.<p>
     * 
     * If the selected property is not found, the empty string "" is returned,
     * NOT <code>null</code>.
     * 
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @param defaultValue a default value in case the property was not found
     * @param escapeHtml if <code>true</code>, special HTML characters in the return value
     *     are escaped with their number representations (e.g. &amp; becomes &amp;#38;)
     * @return the value of the property found, or the value of defaultValue 
     *     if the property could not be found
     *
     * @see  com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue, boolean escapeHtml) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        try {
            return CmsJspTagProperty.propertyTagAction(name, file, defaultValue, escapeHtml, m_request);
        } catch (CmsException e) {
            if (defaultValue == null) {
                return "+++ error reading property '" + name + "' +++";
            } else {
                return defaultValue;
            }
        }        
    }
    
    /**
     * Returns an OpenCms or JVM system info property value, same as using
     * the <code>&lt;cms:info property="..." /&gt;</code> tag.<p>
     * 
     * See the description of the class {@link CmsJspTagInfo} for a detailed list 
     * of available options for the property value.<p>
     *  
     * @param property the property to look up
     * @return String the value of the system property
     * @see CmsJspTagInfo
     */
    public String info(String property) {
        return CmsJspTagInfo.infoTagAction(property, m_request);        
    }
    
    /**
     * Returns an OpenCms workplace label.<p>
     * 
     * You should consider using a standard 
     * {@link java.util.ResourceBundle java.util.ResourceBundle} instead of the 
     * OpenCms workplace language files.
     * 
     * @param label the label to look up
     * @return label the value of the label
     * 
     * @see com.opencms.flex.jsp.CmsJspTagLabel
     */
    public String label(String label) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        try {
            return CmsJspTagLabel.wpLabelTagAction(label, m_request);
        } catch (CmsException e) {
            return "+++ error reading workplace label '" + label + "' +++";
        }         
    }    
    
    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template element="..." /&gt;</code> tag.<p>
     * 
     * @param part the template element to check 
     * @return <code>true</code> if the element is active, <code>false</code> otherwise
     * 
     * @see  com.opencms.flex.jsp.CmsJspTagUser
     */
    public boolean template(String element) {
        if (m_notInitialized) return true;        
        return CmsJspTagTemplate.templateTagAction(element, m_request);
    }
    
    /** 
     * Returns the current request context from the internal CmsObject.
     * 
     * @return the current request context from the internal CmsObject
     */
    public CmsRequestContext getRequestContext() {
        if (m_notInitialized) return null;
        return m_request.getCmsObject().getRequestContext();  
    }
    
    /**
     * Returns an initialized {@link CmsJspNavBuilder} instance.<p>
     *  
     * @return CmsJspNavBuilder an initialized <code>CmsJspNavBuilder</code>
     */
    public CmsJspNavBuilder getNavigation() {
        if (m_navigation == null) {
            m_navigation = new CmsJspNavBuilder(m_request.getCmsObject());
        }
        return m_navigation;
    }
}
