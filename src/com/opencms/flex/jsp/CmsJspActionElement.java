/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspActionElement.java,v $
 * Date   : $Date: 2003/04/08 07:15:31 $
 * Version: $Revision: 1.19 $
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
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.flex.CmsJspLoader;
import com.opencms.flex.CmsJspTemplate;
import com.opencms.flex.cache.CmsFlexRequest;
import com.opencms.flex.cache.CmsFlexResponse;
import com.opencms.flex.util.CmsMessages;
import com.opencms.launcher.CmsDumpLauncher;
import com.opencms.launcher.CmsLinkLauncher;
import com.opencms.launcher.CmsXmlLauncher;
import com.opencms.launcher.I_CmsLauncher;
import com.opencms.template.CmsXmlTemplate;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Bean to be used in JSP scriptlet code that provides direct 
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
 * All exceptions that occur when calling any method of this class are catched 
 * and written to the log output only, so that a template still has a chance of
 * working at last in some elements.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.19 $
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

    /** Will be set by the first call to {@link #isInWorkplaceMode()} */
    private Boolean m_isWorkplaceUser = null;
        
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
        try { 
            return m_request.getCmsObject();
        } catch (Throwable t) {
            handleException(t);
        }      
        return null;            
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
                HashMap modParameterMap = new HashMap(parameterMap.size());
                // ensure parameters are always of type String[] not just String
                Iterator i = parameterMap.keySet().iterator();
                while (i.hasNext()) {
                    String key = (String)i.next();
                    Object value = parameterMap.get(key);
                    if (value instanceof String[]) {
                        modParameterMap.put(key, value);
                    } else {
                        if (value == null)
                            value = "null";
                        String[] newValue = new String[] { value.toString()};
                        modParameterMap.put(key, newValue);
                    }
                }
                parameterMap = modParameterMap;
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
     * @see com.opencms.flex.jsp.CmsJspTagLink
     */
    public String link(String link) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        try {        
            return CmsJspTagLink.linkTagAction(link, m_request);
        } catch (Throwable t) {
            handleException(t);
        }  
        return "+++ error generating link to '" + link + "' +++";             
    }
    
    /**
     * Returns a selected user property, i.e. information about the currently
     * logged in user, same as using 
     * the <code>&lt;cms:user property="..." /&gt;</code> tag.<p>
     * 
     * @param property the user property to display, please see the tag documentation for valid options
     * @return the value of the selected user property
     * 
     * @see com.opencms.flex.jsp.CmsJspTagUser
     */
    public String user(String property) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        try {
            return CmsJspTagUser.userTagAction(property, m_request);
        } catch (Throwable t) {
            handleException(t);
        }  
        return "+++ error reading user property '" + property + "' +++";            
    }
    
    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." /&gt;</code> tag or
     * calling {@link #property(String name, String null, String null, boolean false)}.<p>
     * 
     * @param name the name of the property to look for
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see #property(String, String, String, boolean)
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name) {
        return this.property(name, null, null, false);       
    }
        
    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." file="..." /&gt;</code> tag or
     * calling {@link #property(String name, String file, String null, boolean false)}.<p>
     * 
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see #property(String, String, String, boolean)
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file) {
        return this.property(name, file, null, false);       
    }

    /**
     * Returns a selected file property value, same as using
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag or
     * calling {@link #property(String name, String file, String defaultValue, boolean false)}.<p>
     *
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @param defaultValue a default value in case the property was not found
     * @return the value of the property found, or the value of defaultValue
     *     if the property could not be found
     *
     * @see #property(String, String, String, boolean)
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue) {
        return this.property(name, file, defaultValue, false);
    }
            
    /**
     * Returns a selected file property value with optional HTML escaping, same as using 
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag.<p>
     * 
     * Please see the description of the class {@link com.opencms.flex.jsp.CmsJspTagProperty} for
     * valid options of the <code>file</code> parameter.<p>
     * 
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @param defaultValue a default value in case the property was not found
     * @param escapeHtml if <code>true</code>, special HTML characters in the return value
     *     are escaped with their number representations (e.g. &amp; becomes &amp;#38;)
     * @return the value of the property found, or the value of defaultValue 
     *     if the property could not be found
     *
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue, boolean escapeHtml) {
        if (m_notInitialized) return C_NOT_INITIALIZED;
        try {
            if (file == null) file = m_request.getCmsObject().getRequestContext().getUri();
            return CmsJspTagProperty.propertyTagAction(name, file, defaultValue, escapeHtml, m_request);
        } catch (Throwable t) {
            handleException(t);
        }   
        if (defaultValue == null) {
            return "+++ error reading file property '" + name + "' on '" + file + "' +++";
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns all properites of the current file.<p>
     * 
     * @return Map all properties of the current file
     */
    public Map properties() {
        return this.properties(null);
    }
       
    /**
     * Returns all properites of the selected file.<p>
     * 
     * Please see the description of the class {@link com.opencms.flex.jsp.CmsJspTagProperty} for
     * valid options of the <code>file</code> parameter.<p>
     * 
     * @param file the file (or folder) to look at for the properties
     * @return Map all properties of the current file 
     *     (and optional of the folders containing the file)
     * 
     * @see com.opencms.flex.jsp.CmsJspTagProperty
     */
    public Map properties(String file) {
        if (m_notInitialized) return new HashMap();
        Map value = new HashMap();        
        try {
            if (file == null) file = CmsJspTagProperty.USE_URI;   
            switch (CmsJspTagProperty.m_actionValue.indexOf(file)) {      
                case 0: // USE_URI
                case 1: // USE_PARENT
                    value = getCmsObject().readProperties(getRequestContext().getUri(), false);
                    break;
                case 2: // USE_SEARCH
                case 3: // USE_SEARCH_URI
                case 4: // USE_SEARCH_PARENT 
                    value = getCmsObject().readProperties(getRequestContext().getUri(), true);                        
                    break;                
                case 5: // USE_ELEMENT_URI
                case 6: // USE_THIS
                    // Read properties of this file            
                    value = getCmsObject().readProperties(m_request.getCmsResource(), false);
                    break;
                case 7: // USE_SEARCH_ELEMENT_URI
                case 8: // USE_SEARCH_THIS
                    // Try to find property on this file and all parent folders
                    value = getCmsObject().readProperties(m_request.getCmsResource(), true);
                    break;
                default:
                    // Read properties of the file named in the attribute            
                    value = getCmsObject().readProperties(m_request.toAbsolute(file), false);
            }            
        } catch (Throwable t) {
            handleException(t);
        }
        return value;          
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
     * @see com.opencms.flex.jsp.CmsJspTagInfo
     */
    public String info(String property) {
        try {        
            return CmsJspTagInfo.infoTagAction(property, m_request);   
        } catch (Throwable t) {
            handleException(t);
        }  
        return "+++ error reading info property '" + property + "' +++";
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
        } catch (Throwable t) {
            handleException(t);
        }  
        return "+++ error reading workplace label '" + label + "' +++";
    }    
    
    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template element="..." /&gt;</code> tag.<p>
     * 
     * @param part the template element to check 
     * @return <code>true</code> if the element is active, <code>false</code> otherwise
     * 
     * @see com.opencms.flex.jsp.CmsJspTagUser
     */
    public boolean template(String element) {
        if (m_notInitialized) return true;        
        try {
            return CmsJspTagTemplate.templateTagAction(element, m_request);
        } catch (Throwable t) {
            handleException(t);
        }
        return true;
    }
    
    /** 
     * Returns the current request context from the internal CmsObject.
     * 
     * @return the current request context from the internal CmsObject
     */
    public CmsRequestContext getRequestContext() {
        if (m_notInitialized) return null;
        try {
            return m_request.getCmsObject().getRequestContext();
        } catch (Throwable t) {
            handleException(t);
        }
        return null;
    }
    
    /**
     * Returns an initialized {@link CmsJspNavBuilder} instance.<p>
     *  
     * @return CmsJspNavBuilder an initialized <code>CmsJspNavBuilder</code>
     * 
     * @see com.opencms.flex.jsp.CmsJspNavBuilder
     */
    public CmsJspNavBuilder getNavigation() {
        if (m_notInitialized) return null;
        try {
            if (m_navigation == null) {
                m_navigation = new CmsJspNavBuilder(m_request.getCmsObject());
            }
            return m_navigation;
        } catch (Throwable t) {
            handleException(t);
        }
        return null;            
    }
    
    /**
     * Generates an initialized instance of {@link com.opencms.flex.util.CmsMessages} for 
     * convenient access to localized resource bundles.<p>
     * 
     * @param bundleName the name of the ResourceBundle to use
     * @param language language indentificator for the locale of the bundle
     * @return CmsMessages a message bundle initialized with the provided values
     */       
    public CmsMessages getMessages(String bundleName, String language) {
        return getMessages(bundleName, language, "", "", null);
    }
    
    /**
     * Generates an initialized instance of {@link com.opencms.flex.util.CmsMessages} for 
     * convenient access to localized resource bundles.<p>
     * 
     * @param bundleName the name of the ResourceBundle to use
     * @param language language indentificator for the locale of the bundle
     * @param defaultLanguage default for the language, will be used 
     *         if language is null or empty String "", and defaultLanguage is not null
     * @return CmsMessages a message bundle initialized with the provided values
     */    
    public CmsMessages getMessages(String bundleName, String language, String defaultLanguage) {
        return getMessages(bundleName, language, "", "", defaultLanguage);
    }
    
    /**
     * Generates an initialized instance of {@link com.opencms.flex.util.CmsMessages} for 
     * convenient access to localized resource bundles.<p>
     * 
     * @param bundleName the name of the ResourceBundle to use
     * @param language language indentificator for the locale of the bundle
     * @param country 2 letter country code for the locale of the bundle 
     * @param variant a vendor or browser-specific variant code
     * @param defaultLanguage default for the language, will be used 
     *         if language is null or empty String "", and defaultLanguage is not null
     * @return CmsMessages a message bundle initialized with the provided values
     * 
     * @see java.util.ResourceBundle
     * @see com.opencms.flex.util.CmsMessages
     */
    public CmsMessages getMessages(String bundleName, String language, String country, String variant, String defaultLanguage) {
        try {
            if ((defaultLanguage != null) && ((language == null) || ("".equals(language)))) {
                language = defaultLanguage;
            }
            if (language == null) language = "";
            if (country == null) country = "";
            if (variant == null) variant = "";
            return new CmsMessages(bundleName, language, country, variant);
        } catch (Throwable t) {
            handleException(t);
        }
        return null;
    }    
    
    /**
     * Returns the processed output of an OpenCms resource in a String.<p>
     * 
     * @param target the target to process
     * @return the processed output of an OpenCms resource in a String
     */
    public String getContent(String target) {
        try {
            I_CmsLauncher launcher = null;
            target = m_request.toAbsolute(target);
            try {
                CmsResource resource = getCmsObject().readFileHeader(target);
                launcher = getCmsObject().getLauncherManager().getLauncher(resource.getLauncherType());
            } catch (java.lang.ClassCastException e) {
                // no loader omplementation found
                return "??? " + e.getMessage() + " ???";
            } catch (com.opencms.core.CmsException e) {
                // file might not exist or no read permissions
                return "??? " + e.getMessage() + " ???";
            }
            try {
                if (launcher instanceof CmsJspLoader) {
                    // jsp page
                    CmsJspTemplate template = new CmsJspTemplate();
                    byte[] res = template.getContent(getCmsObject(), target, null, null);
                    return new String(res, getRequestContext().getEncoding());
                } else if (launcher instanceof CmsXmlLauncher) {
                    // XmlTemplate page (will not work if file does not use the standard XmlTemplate class)
                    CmsXmlTemplate template = new CmsXmlTemplate();
                    byte[] res = template.getContent(getCmsObject(), target, null, null);
                    return new String(res, getRequestContext().getEncoding());
                } else if (launcher instanceof CmsDumpLauncher) {
                    // static page
                    CmsFile file = getCmsObject().readFile(target);
                    return new String(file.getContents(), getRequestContext().getEncoding());
                } else if (launcher instanceof CmsLinkLauncher) {
                    // link
                    CmsFile file = getCmsObject().readFile(target);
                    return new String(file.getContents());
                }
            } catch (CmsException ce) {
                return "??? " + ce.getMessage() + " ???";
            } catch (UnsupportedEncodingException uee) {
                return "??? " + uee.getMessage() + " ???";
            }
        } catch (Throwable t) {
            handleException(t);
            return "??? " + t.getMessage() + " ???";
        }
        return "";
    }
    
    /**
     * Returns <code>true</code> if the current page is called form the workplace, 
     * <code>false</code> otherwise.<p>
     * 
     * Use this method in case you want to display different information for OpenCms
     * Workplace users (i.e. members of the groups "Users", "Projectmanager" or 
     * "Administrators") that have logged on to the workplace.<p>
     *  
     * @return <code>true</code> if the current page is called form the workplace, 
     * <code>false</code> otherwise
     */
    public boolean isInWorkplaceMode() {
        if (m_isWorkplaceUser != null)
            return m_isWorkplaceUser.booleanValue();
        boolean result = false;
        I_CmsSession session = getRequestContext().getSession(false);
        if (session != null) {
            // a workplace user will always have a session
            String username = getRequestContext().currentUser().getName();
            try {
                if (getRequestContext().getUri().startsWith("/system/modules/") || 
                    getRequestContext().getUri().startsWith("/system/workplace/")) {
                    // use the same logic as if logging in to the workplace
                    if ((getCmsObject().userInGroup(username, CmsObject.C_GROUP_USERS)) || 
                        (getCmsObject().userInGroup(username, CmsObject.C_GROUP_PROJECTLEADER)) || 
                        (getCmsObject().userInGroup(username, CmsObject.C_GROUP_ADMIN))) {
                        result = true;
                    }
                }
            } catch (CmsException e) {
                // result will be "false"
            }
        }
        m_isWorkplaceUser = new Boolean(result);
        return result;
    }
    
    /**
     * Handles any exception that might occur in the context of this element to 
     * ensure that templates are not disturbed.<p>
     * 
     * @param e the exception that was catched
     */
    private void handleException(Throwable t) {
        System.err.println("Exception in " + this.getClass().getName() + ":");
        t.printStackTrace(System.err);
    }
}
