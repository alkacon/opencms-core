/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspActionElement.java,v $
 * Date   : $Date: 2005/05/03 12:17:52 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.staticexport.CmsLinkManager;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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
 * &lt;jsp:useBean id="cms" class="org.opencms.jsp.CmsJspActionElement"&gt;
 * &lt% cms.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 * 
 * All exceptions that occur when calling any method of this class are catched 
 * and written to the log output only, so that a template still has a chance of
 * working at last in some elements.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.16 $
 * 
 * @since 5.0 beta 2
 */
public class CmsJspActionElement extends CmsJspBean {

    /** Error message in case bean was not properly initialized. */
    // cannot use a string: At class-loading time the 
    // user request context for localization is not at hand. 
    public static final CmsMessageContainer C_NOT_INITIALIZED = Messages.get().container(
        Messages.GUI_ERR_ACTIONELEM_NOT_INIT_0);

    /** JSP navigation builder. */
    private CmsJspNavBuilder m_navigation;

    /**
     * Empty constructor, required for every JavaBean.
     */
    public CmsJspActionElement() {

        super();
    }

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsJspActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Includes direct edit scriptlets, same as 
     * using the <code>&lt;cms:editable /&gt;</code> tag.<p>
     * 
     * @param isEditable include scriptlets only if true
     * @throws JspException if something goes wrong
     */
    public void editable(boolean isEditable) throws JspException {

        if (isEditable) {
            CmsJspTagEditable.editableTagAction(getJspContext(), null, getRequest(), getResponse());
        }
    }

    /**
     * Includes direct edit scriptlets, same as
     * using the <code>&lt;cms:editable file="..." /&gt;</code>tag.<p>
     * 
     * @param isEditable include scriptlets only if true
     * @param filename file with scriptlets
     * @throws JspException if something goes wrong
     */
    public void editable(boolean isEditable, String filename) throws JspException {

        if (isEditable) {
            CmsJspTagEditable.editableTagAction(getJspContext(), filename, getRequest(), getResponse());
        }
    }

    /**
     * Returns the processed output of an OpenCms resource in a String.<p>
     * 
     * @param target the target to process
     * @return the processed output of an OpenCms resource in a String
     */
    public String getContent(String target) {

        return getContent(target, null, null);
    }

    /**
     * Returns the processed output of an element within an OpenCms resource.<p>
     * 
     * @param target the target to process
     * @param element name of the element
     * @param locale locale of the element
     * @return the processed output
     */
    public String getContent(String target, String element, Locale locale) {

        I_CmsResourceLoader loader;
        CmsFile file;
        target = toAbsolute(target);

        try {
            file = getCmsObject().readFile(target);
            loader = OpenCms.getResourceManager().getLoader(file);
        } catch (ClassCastException e) {
            // no loader implementation found
            return CmsMessages.formatUnknownKey(e.getMessage());
        } catch (CmsException e) {
            // file might not exist or no read permissions
            return CmsMessages.formatUnknownKey(e.getMessage());
        }

        try {
            byte[] result = loader.dump(getCmsObject(), file, element, locale, getRequest(), getResponse());
            return new String(result, getRequestContext().getEncoding());
        } catch (UnsupportedEncodingException uee) {
            // encoding unsupported
            return CmsMessages.formatUnknownKey(uee.getMessage());
        } catch (Throwable t) {
            // any other exception, check for hidden root cause first
            Throwable cause = CmsFlexController.getThrowable(getRequest());
            if (cause == null) {
                cause = t;
            }
            handleException(cause);
            return CmsMessages.formatUnknownKey(cause.getMessage());
        }
    }

    /**
     * Generates an initialized instance of {@link CmsMessages} for 
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
     * Generates an initialized instance of {@link CmsMessages} for 
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
     * Generates an initialized instance of {@link CmsMessages} for 
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
     * @see org.opencms.i18n.CmsMessages
     */
    public CmsMessages getMessages(
        String bundleName,
        String language,
        String country,
        String variant,
        String defaultLanguage) {

        try {
            if ((defaultLanguage != null) && ((language == null) || ("".equals(language)))) {
                language = defaultLanguage;
            }
            if (language == null) {
                language = "";
            }
            if (country == null) {
                country = "";
            }
            if (variant == null) {
                variant = "";
            }
            return new CmsMessages(bundleName, language, country, variant);
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
     * @see org.opencms.jsp.CmsJspNavBuilder
     */
    public CmsJspNavBuilder getNavigation() {

        if (isNotInitialized()) {
            return null;
        }
        if (m_navigation == null) {
            m_navigation = new CmsJspNavBuilder(getController().getCmsObject());
        }
        return m_navigation;
    }

    /**
     * Include a sub-element without paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." /&gt;</code> tag.
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @throws JspException in case there were problems including the target
     *
     * @see org.opencms.jsp.CmsJspTagInclude
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
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element) throws JspException {

        this.include(target, element, null);
    }

    /**
     * Include a named sub-element without paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." element="..." /&gt;</code> tag.
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if element is editable
     * @throws JspException in case there were problems including the target
     *
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, boolean editable) throws JspException {

        this.include(target, element, editable, null);
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
     * <b>Important:</b> Exceptions that occur in the include process are NOT
     * handled even if {@link #setSupressingExceptions(boolean)} was set to <code>true</code>.
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if element is editable
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, boolean editable, Map parameterMap) throws JspException {

        if (isNotInitialized()) {
            return;
        }
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
                        if (value == null) {
                            value = "null";
                        }
                        String[] newValue = new String[] {value.toString()};
                        modParameterMap.put(key, newValue);
                    }
                }
                parameterMap = modParameterMap;
            } catch (UnsupportedOperationException e) {
                // parameter map is immutable, just use it "as is"
            }
        }
        CmsJspTagInclude.includeTagAction(
            getJspContext(),
            target,
            element,
            editable,
            parameterMap,
            getRequest(),
            getResponse());
    }

    /**
     * Include a named sub-element with paramters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="..." element="..." /&gt;</code> tag
     * with parameters in the tag body.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, Map parameterMap) throws JspException {

        this.include(target, element, false, parameterMap);
    }

    /**
     * Includes a named sub-element supressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     */
    public void includeSilent(String target, String element) {

        try {
            include(target, element, null);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Includes a named sub-element supressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if element is editable
     */
    public void includeSilent(String target, String element, boolean editable) {

        try {
            include(target, element, editable, null);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Includes a named sub-element supressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if element is editable
     * @param parameterMap a map of the request parameters
     */
    public void includeSilent(String target, String element, boolean editable, Map parameterMap) {

        try {
            include(target, element, editable, parameterMap);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Includes a named sub-element supressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     */
    public void includeSilent(String target, String element, Map parameterMap) {

        try {
            include(target, element, parameterMap);
        } catch (Throwable t) {
            // ignore
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
     * @see org.opencms.jsp.CmsJspTagInfo
     */
    public String info(String property) {

        try {
            return CmsJspTagInfo.infoTagAction(property, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_INFO_PROP_READ_1, property);
        return this.getMessage(msgContainer);
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
     * @see org.opencms.jsp.CmsJspTagLabel
     */
    public String label(String label) {

        if (isNotInitialized()) {
            return this.getMessage(C_NOT_INITIALIZED);
        }
        try {
            return CmsJspTagLabel.wpLabelTagAction(label, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_WORKPL_LABEL_READ_1, label);
        return this.getMessage(msgContainer);
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
     * @see org.opencms.jsp.CmsJspTagLink
     */
    public String link(String link) {

        if (isNotInitialized()) {
            return this.getMessage(C_NOT_INITIALIZED);
        }
        try {
            return CmsJspTagLink.linkTagAction(link, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_GEN_LINK_1, link);
        return this.getMessage(msgContainer);
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
     * Please see the description of the class {@link org.opencms.jsp.CmsJspTagProperty} for
     * valid options of the <code>file</code> parameter.<p>
     * 
     * @param file the file (or folder) to look at for the properties
     * @return Map all properties of the current file 
     *     (and optional of the folders containing the file)
     * 
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public Map properties(String file) {

        if (isNotInitialized()) {
            return new HashMap();
        }
        Map value = new HashMap();
        try {
            if (file == null) {
                file = CmsJspTagProperty.USE_URI;
            }
            switch (CmsJspTagProperty.ACTION_VALUES_LIST.indexOf(file)) {
                case 0: // USE_URI
                case 1: // USE_PARENT
                    value = CmsProperty.toMap(getCmsObject().readPropertyObjects(getRequestContext().getUri(), false));
                    break;
                case 2: // USE_SEARCH
                case 3: // USE_SEARCH_URI
                case 4: // USE_SEARCH_PARENT 
                    value = CmsProperty.toMap(getCmsObject().readPropertyObjects(getRequestContext().getUri(), true));
                    break;
                case 5: // USE_ELEMENT_URI
                case 6: // USE_THIS
                    // Read properties of this file            
                    value = CmsProperty.toMap(getCmsObject().readPropertyObjects(
                        getController().getCurrentRequest().getElementUri(),
                        false));
                    break;
                case 7: // USE_SEARCH_ELEMENT_URI
                case 8: // USE_SEARCH_THIS
                    // Try to find property on this file and all parent folders
                    value = CmsProperty.toMap(getCmsObject().readPropertyObjects(
                        getController().getCurrentRequest().getElementUri(),
                        true));
                    break;
                default:
                    // Read properties of the file named in the attribute            
                    value = CmsProperty.toMap(getCmsObject().readPropertyObjects(toAbsolute(file), false));
            }
        } catch (Throwable t) {
            handleException(t);
        }
        return value;
    }

    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." /&gt;</code> tag or
     * calling {@link #property(String, String, String, boolean)}.<p>
     * 
     * @param name the name of the property to look for
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see #property(String, String, String, boolean)
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public String property(String name) {

        return this.property(name, null, null, false);
    }

    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="..." file="..." /&gt;</code> tag or
     * calling {@link #property(String, String, String, boolean)}.<p>
     * 
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see #property(String, String, String, boolean)
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public String property(String name, String file) {

        return this.property(name, file, null, false);
    }

    /**
     * Returns a selected file property value, same as using
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag or
     * calling {@link #property(String, String, String, boolean)}.<p>
     *
     * @param name the name of the property to look for
     * @param file the file (or folder) to look at for the property
     * @param defaultValue a default value in case the property was not found
     * @return the value of the property found, or the value of defaultValue
     *     if the property could not be found
     *
     * @see #property(String, String, String, boolean)
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue) {

        return this.property(name, file, defaultValue, false);
    }

    /**
     * Returns a selected file property value with optional HTML escaping, same as using 
     * the <code>&lt;cms:property name="..." file="..." default="..." /&gt;</code> tag.<p>
     * 
     * Please see the description of the class {@link org.opencms.jsp.CmsJspTagProperty} for
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
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public String property(String name, String file, String defaultValue, boolean escapeHtml) {

        if (isNotInitialized()) {
            return this.getMessage(C_NOT_INITIALIZED);
        }
        try {
            if (file == null) {
                file = getController().getCmsObject().getRequestContext().getUri();
            }
            return CmsJspTagProperty.propertyTagAction(name, file, defaultValue, escapeHtml, getRequest());
        } catch (CmsSecurityException e) {
            if (defaultValue == null) {
                handleException(e);
            }
        } catch (Throwable t) {
            handleException(t);
        }
        if (defaultValue == null) {
            CmsMessageContainer msgContainer = Messages.get().container(
                Messages.GUI_ERR_FILE_PROP_MISSING_2,
                name,
                file);
            return this.getMessage(msgContainer);
        } else {
            return defaultValue;
        }
    }

    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template element="..." /&gt;</code> tag.<p>
     * 
     * @param element the template element to check 
     * @return <code>true</code> if the element is active, <code>false</code> otherwise
     * 
     * @see org.opencms.jsp.CmsJspTagUser
     */
    public boolean template(String element) {

        return template(element, null, false);
    }

    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template ifexists="..." /&gt;</code> tag.<p>
     * 
     * @param elementlist the list of elements to check
     * @param checkall <code>true</code> if all elements in the list should be checked 
     * @return <code>true</code> if the elements available, <code>false</code> otherwise
     * 
     * @see org.opencms.jsp.CmsJspTagUser
     */
    public boolean template(String elementlist, boolean checkall) {

        return template(null, elementlist, checkall);
    }

    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template element="..." ifexists="..." /&gt;</code> tag.<p>
     * 
     * @param element the template element to check
     * @param elementlist the list of elements to check 
     * @param checkall <code>true</code> if all elements in the list should be checked
     * @return <code>true</code> if the element is active, <code>false</code> otherwise
     * 
     * @see org.opencms.jsp.CmsJspTagUser
     */
    public boolean template(String element, String elementlist, boolean checkall) {

        if (isNotInitialized()) {
            return true;
        }
        try {
            return CmsJspTagTemplate.templateTagAction(element, elementlist, checkall, false, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        return true;
    }

    /**
     * Converts a relative URI in the OpenCms VFS to an absolute one based on 
     * the location of the currently processed OpenCms URI.
     * 
     * @param target the relative URI to convert
     * @return the target URI converted to an absolute one
     */
    public String toAbsolute(String target) {

        if (isNotInitialized()) {
            return this.getMessage(C_NOT_INITIALIZED);
        }
        return CmsLinkManager.getAbsoluteUri(target, getController().getCurrentRequest().getElementUri());
    }

    /**
     * Returns a selected user property, i.e. information about the currently
     * logged in user, same as using 
     * the <code>&lt;cms:user property="..." /&gt;</code> tag.<p>
     * 
     * @param property the user property to display, please see the tag documentation for valid options
     * @return the value of the selected user property
     * 
     * @see org.opencms.jsp.CmsJspTagUser
     */
    public String user(String property) {

        if (isNotInitialized()) {
            return this.getMessage(C_NOT_INITIALIZED);
        }
        try {
            return CmsJspTagUser.userTagAction(property, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_USER_PROP_READ_1, property);
        return this.getMessage(msgContainer);
    }
}
