/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.directedit.CmsDirectEditJspIncludeProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Bean to be used in JSP scriptlet code that provides direct 
 * access to the functionality offered by the OpenCms taglib.<p>
 * 
 * By instantiating a bean of this type and accessing the methods provided by 
 * the instance, all functionality of the OpenCms JSP taglib can be easily 
 * used from within JSP scriptlet code.<p>
 * 
 * Initialize this bean at the beginning of your JSP like this:
 * <pre>
 * &lt;jsp:useBean id="cms" class="org.opencms.jsp.CmsJspActionElement"&gt;
 * &lt% cms.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 * 
 * You can also access the current users <code>{@link org.opencms.file.CmsObject}</code>
 * by using <code>{@link org.opencms.jsp.CmsJspBean#getCmsObject()}</code>.<p>
 * 
 * All exceptions that occur when calling any method of this class are caught 
 * and written to the log output only, so that a template still has a chance of
 * working at least in some elements.<p>
 * 
 * @since 6.0.0 
 */
public class CmsJspActionElement extends CmsJspBean {

    /** Error message in case bean was not properly initialized. */
    // cannot use a string: At class-loading time the 
    // user request context for localization is not at hand. 
    public static final CmsMessageContainer NOT_INITIALIZED = Messages.get().container(
        Messages.GUI_ERR_ACTIONELEM_NOT_INIT_0);

    /** JSP navigation builder. */
    private CmsJspNavBuilder m_vfsNav;

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
     * Includes the direct edit scriptlet, same as 
     * using the <code>&lt;cms:editable /&gt;</code> tag.<p>
     * 
     * The configured default direct edit provider is used.<p>
     * 
     * @param isEditable include scriptlet only if true
     * 
     * @throws JspException if something goes wrong
     */
    public void editable(boolean isEditable) throws JspException {

        if (isEditable) {
            CmsJspTagEditable.editableTagAction(getJspContext(), null, CmsDirectEditMode.AUTO, null);
        }
    }

    /**
     * Includes the direct edit scriptlet, same as
     * using the <code>&lt;cms:editable file="..." /&gt;</code>tag.<p>
     * 
     * For backward compatibility, this always uses the JSP include based direct edit provider<p>.
     * 
     * @param isEditable include scriptlet only if true
     * @param filename file with scriptlet code
     * 
     * @throws JspException if something goes wrong
     */
    public void editable(boolean isEditable, String filename) throws JspException {

        if (isEditable) {
            CmsJspTagEditable.editableTagAction(
                getJspContext(),
                CmsDirectEditJspIncludeProvider.class.getName(),
                CmsDirectEditMode.AUTO,
                filename);
        }
    }

    /**
     * Includes the direct edit scriptlet, same as
     * using the <code>&lt;cms:editable provider="..." mode="..." file="..." /&gt;</code>tag.<p>
     * 
     * @param provider the direct edit provider class name
     * @param mode the direct edit mode to use
     * @param filename file with scriptlet code (may be <code>null</code>)
     * 
     * @throws JspException if something goes wrong
     */
    public void editable(String provider, String mode, String filename) throws JspException {

        CmsJspTagEditable.editableTagAction(getJspContext(), provider, CmsDirectEditMode.valueOf(mode), filename);
    }

    /**
     * Insert the end HTML for the direct edit buttons in manual mode (if required).<p>
     * 
     * Same as closing the <code>&lt;/cms:editable</code> tag after opening one in manual mode.<p>
     * 
     * @param needsClose result of {@link #editableManualOpen()} should be the value for this parameter
     * 
     * @throws JspException if something goes wrong
     */
    public void editableManualClose(boolean needsClose) throws JspException {

        if (needsClose) {
            CmsJspTagEditable.editableTagAction(getJspContext(), null, CmsDirectEditMode.MANUAL, null);
        }
    }

    /**
     * Insert the start HTML for the direct edit buttons in manual mode.<p>
     * 
     * Same as opening the <code>&lt;cms:editable mode="manual"&gt;</code> tag.<p>
     * 
     * @return <code>true</code> if HTML was inserted that needs to be closed
     * 
     * @throws JspException if something goes wrong
     */
    public boolean editableManualOpen() throws JspException {

        boolean result = false;
        if (!CmsFlexController.isCmsOnlineRequest(getJspContext().getRequest())) {
            // all this does NOT apply to the "online" project
            I_CmsDirectEditProvider eb = CmsJspTagEditable.getDirectEditProvider(getJspContext());
            if ((eb != null)) {
                // check if the provider support manual placement of buttons
                if (eb.isManual(CmsDirectEditMode.MANUAL)) {
                    // the provider supports manual placement of buttons
                    result = true;
                    CmsJspTagEditable.editableTagAction(getJspContext(), null, CmsDirectEditMode.MANUAL, null);
                }
            }
        }
        return result;
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
     * @param locale the locale to use for localization
     * 
     * @return CmsMessages a message bundle initialized with the provided values
     */
    public CmsMessages getMessages(String bundleName, Locale locale) {

        return new CmsMessages(bundleName, locale);
    }

    /**
     * Generates an initialized instance of {@link CmsMessages} for 
     * convenient access to localized resource bundles.<p>
     * 
     * @param bundleName the name of the ResourceBundle to use
     * @param language language identifier for the locale of the bundle
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
     * @param language language identifier for the locale of the bundle
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
     * @param language language identifier for the locale of the bundle
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
            if ((defaultLanguage != null) && CmsStringUtil.isEmpty(language)) {
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
            return getMessages(bundleName, new Locale(language, country, variant));
        } catch (Throwable t) {
            handleException(t);
        }
        return null;
    }

    /**
     * Returns an initialized {@link CmsJspNavBuilder} instance.<p>
     *  
     * @return an initialized navigation builder instance
     * 
     * @see org.opencms.jsp.CmsJspNavBuilder
     */
    public CmsJspNavBuilder getNavigation() {

        if (isNotInitialized()) {
            return null;
        }
        if (m_vfsNav == null) {
            m_vfsNav = new CmsJspNavBuilder(getCmsObject());
        }
        return m_vfsNav;
    }

    /**
     * Returns the current uri for the navigation.<p>
     *  
     * @return the current uri for the navigation
     */
    public String getNavigationUri() {

        return getCmsObject().getRequestContext().getUri();
    }

    /**
     * Returns the HTML for an <code>&lt;img src="..." /&gt;</code> tag that includes the given image scaling parameters.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS
     * @param scaler the image scaler to use for scaling the image
     * @param attributes a map of additional HTML attributes that are added to the output
     * 
     * @return the HTML for an <code>&lt;img src&gt;</code> tag that includes the given image scaling parameters
     */
    public String img(String target, CmsImageScaler scaler, Map<String, String> attributes) {

        return img(target, scaler, attributes, false);
    }

    /**
     * Returns the HTML for an <code>&lt;img src="..." /&gt;</code> tag that includes the given image scaling parameters.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS
     * @param scaler the image scaler to use for scaling the image
     * @param attributes a map of additional HTML attributes that are added to the output
     * @param partialTag if <code>true</code>, the opening <code>&lt;img</code> and closing <code> /&gt;</code> is omitted
     * 
     * @return the HTML for an <code>&lt;img src&gt;</code> tag that includes the given image scaling parameters
     */
    public String img(String target, CmsImageScaler scaler, Map<String, String> attributes, boolean partialTag) {

        try {
            return CmsJspTagImage.imageTagAction(target, scaler, attributes, partialTag, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(
            Messages.GUI_ERR_IMG_SCALE_2,
            target,
            scaler == null ? "null" : scaler.toString());
        return getMessage(msgContainer);
    }

    /**
     * Include a sub-element without parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" /&gt;</code> tag.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @throws JspException in case there were problems including the target
     *
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target) throws JspException {

        include(target, null, null);
    }

    /**
     * Include a named sub-element without parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" element="***" /&gt;</code> tag.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @throws JspException in case there were problems including the target
     *
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element) throws JspException {

        include(target, element, null);
    }

    /**
     * Include a named sub-element without parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" element="***" /&gt;</code> tag.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if direct edit should be enabled for the element 
     * @throws JspException in case there were problems including the target
     *
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, boolean editable) throws JspException {

        include(target, element, editable, null);
    }

    /**
     * Include a named sub-element with parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" element="***" /&gt;</code> tag
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
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if direct edit should be enabled for the element 
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, boolean editable, Map<String, ?> parameterMap)
    throws JspException {

        include(target, element, editable, true, parameterMap);
    }

    /**
     * Include a named sub-element with parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" element="***" /&gt;</code> tag
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
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if direct edit should be enabled for the element
     * @param cacheable flag to indicate if the target should be cacheable in the Flex cache
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, boolean editable, boolean cacheable, Map<String, ?> parameterMap)
    throws JspException {

        if (isNotInitialized()) {
            return;
        }
        Map<String, String[]> modParameterMap = null;
        if (parameterMap != null) {
            try {
                modParameterMap = new HashMap<String, String[]>(parameterMap.size());
                // ensure parameters are always of type String[] not just String
                Iterator<?> i = parameterMap.entrySet().iterator();
                while (i.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, ?> entry = (Entry<String, ?>)i.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String[]) {
                        modParameterMap.put(key, (String[])value);
                    } else {
                        if (value == null) {
                            value = "null";
                        }
                        String[] newValue = new String[] {value.toString()};
                        modParameterMap.put(key, newValue);
                    }
                }
            } catch (UnsupportedOperationException e) {
                // parameter map is immutable, just use it "as is"
            }
        }
        CmsJspTagInclude.includeTagAction(
            getJspContext(),
            target,
            element,
            null,
            editable,
            cacheable,
            modParameterMap,
            CmsRequestUtil.getAtrributeMap(getRequest()),
            getRequest(),
            getResponse());
    }

    /**
     * Include a named sub-element with parameters from the OpenCms VFS, same as
     * using the <code>&lt;cms:include file="***" element="***" /&gt;</code> tag
     * with parameters in the tag body.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     * @throws JspException in case there were problems including the target
     * 
     * @see org.opencms.jsp.CmsJspTagInclude
     */
    public void include(String target, String element, Map<String, ?> parameterMap) throws JspException {

        include(target, element, false, parameterMap);
    }

    /**
     * Includes a named sub-element suppressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
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
     * Includes a named sub-element suppressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if direct edit should be enabled for the element 
     */
    public void includeSilent(String target, String element, boolean editable) {

        try {
            include(target, element, editable, null);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Includes a named sub-element suppressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param editable flag to indicate if direct edit should be enabled for the element 
     * @param parameterMap a map of the request parameters
     */
    public void includeSilent(String target, String element, boolean editable, Map<String, ?> parameterMap) {

        try {
            include(target, element, editable, parameterMap);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Includes a named sub-element suppressing all Exceptions that occur during the include,
     * otherwise the same as using {@link #include(String, String, Map)}.<p>
     * 
     * This is a convenience method that allows to include elements on a page without checking 
     * if they exist or not. If the target element does not exist, nothing is printed to 
     * the JSP output.<p>
     * 
     * @param target the target URI of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     */
    public void includeSilent(String target, String element, Map<String, ?> parameterMap) {

        try {
            include(target, element, parameterMap);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Returns an OpenCms or JVM system info property value, same as using
     * the <code>&lt;cms:info property="***" /&gt;</code> tag.<p>
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
        return getMessage(msgContainer);
    }

    /**
     * Returns an OpenCms workplace label.<p>
     * 
     * You should consider using a standard 
     * {@link java.util.ResourceBundle java.util.ResourceBundle} instead of the 
     * OpenCms workplace language files.<p>
     * 
     * @param label the label to look up
     * @return label the value of the label
     * 
     * @see org.opencms.jsp.CmsJspTagLabel
     */
    public String label(String label) {

        if (isNotInitialized()) {
            return getMessage(NOT_INITIALIZED);
        }
        try {
            return CmsJspTagLabel.wpLabelTagAction(label, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_WORKPL_LABEL_READ_1, label);
        return getMessage(msgContainer);
    }

    /**
     * Returns a link to a file in the OpenCms VFS 
     * that has been adjusted according to the web application path and the 
     * OpenCms static export rules.<p>
     * 
     * Please note that the target is always assumed to be in the OpenCms VFS, so you can't use 
     * this method for links external to OpenCms.<p>
     * 
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     * 
     * This is the same as using the <code>&lt;cms:link&gt;***&lt;/cms:link&gt;</code> tag.<p>
     * 
     * @param target the URI in the OpenCms VFS to link to
     * 
     * @return the translated link
     * 
     * @see org.opencms.jsp.CmsJspTagLink
     * @see #link(String, String)
     */
    public String link(String target) {

        return link(target, null);
    }

    /**
     * Returns a link to a file in the OpenCms VFS 
     * that has been adjusted according to the web application path and the 
     * OpenCms static export rules.<p>
     * 
     * Please note that the target is always assumed to be in the OpenCms VFS, so you can't use 
     * this method for links external to OpenCms.<p>
     * 
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     * 
     * This is the same as using the <code>&lt;cms:link baseUri=&quot;...&quot; &gt;***&lt;/cms:link&gt;</code> tag.<p>
     * 
     * @param target the URI in the OpenCms VFS to link to
     * @param baseUri the optional alternative base URI
     * 
     * @return the translated link
     * 
     * @see org.opencms.jsp.CmsJspTagLink
     * @see #link(String)
     */
    public String link(String target, String baseUri) {

        if (isNotInitialized()) {
            return getMessage(NOT_INITIALIZED);
        }
        try {
            return CmsJspTagLink.linkTagAction(target, getRequest(), baseUri);
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_GEN_LINK_1, target);
        return getMessage(msgContainer);
    }

    /**
     * Returns all properties of the current file.<p>
     * 
     * @return Map all properties of the current file
     */
    public Map<String, String> properties() {

        return properties(null);
    }

    /**
     * Returns all properties of the selected file.<p>
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
    public Map<String, String> properties(String file) {

        Map<String, String> props = new HashMap<String, String>();
        if (isNotInitialized()) {
            return props;
        }
        try {
            props = CmsJspTagProperty.propertiesTagAction(file, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        return props;
    }

    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="***" /&gt;</code> tag or
     * calling {@link #property(String, String, String, boolean)}.<p>
     * 
     * @param name the name of the property to look for
     * @return the value of the property found, or null if the property could not be found
     * 
     * @see #property(String, String, String, boolean)
     * @see org.opencms.jsp.CmsJspTagProperty
     */
    public String property(String name) {

        return property(name, null, null, false);
    }

    /**
     * Returns a selected file property value, same as using 
     * the <code>&lt;cms:property name="***" file="***" /&gt;</code> tag or
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

        return property(name, file, null, false);
    }

    /**
     * Returns a selected file property value, same as using
     * the <code>&lt;cms:property name="***" file="***" default="***" /&gt;</code> tag or
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

        return property(name, file, defaultValue, false);
    }

    /**
     * Returns a selected file property value with optional HTML escaping, same as using 
     * the <code>&lt;cms:property name="***" file="***" default="***" /&gt;</code> tag.<p>
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
            return getMessage(NOT_INITIALIZED);
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
            return getMessage(msgContainer);
        } else {
            return defaultValue;
        }
    }

    /**
     * Checks if a template part should be used or not, same as using 
     * the <code>&lt;cms:template element="***" /&gt;</code> tag.<p>
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
     * the <code>&lt;cms:template ifexists="***" /&gt;</code> tag.<p>
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
     * the <code>&lt;cms:template element="***" ifexists="***" /&gt;</code> tag.<p>
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
     * the location of the currently processed OpenCms URI.<p>
     * 
     * @param target the relative URI to convert
     * @return the target URI converted to an absolute one
     */
    public String toAbsolute(String target) {

        if (isNotInitialized()) {
            return getMessage(NOT_INITIALIZED);
        }
        return CmsLinkManager.getAbsoluteUri(target, getController().getCurrentRequest().getElementUri());
    }

    /**
     * Returns a selected user property, i.e. information about the currently
     * logged in user, same as using 
     * the <code>&lt;cms:user property="***" /&gt;</code> tag.<p>
     * 
     * @param property the user property to display, please see the tag documentation for valid options
     * @return the value of the selected user property
     * 
     * @see org.opencms.jsp.CmsJspTagUser
     */
    public String user(String property) {

        if (isNotInitialized()) {
            return getMessage(NOT_INITIALIZED);
        }
        try {
            return CmsJspTagUser.userTagAction(property, getRequest());
        } catch (Throwable t) {
            handleException(t);
        }
        CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_ERR_USER_PROP_READ_1, property);
        return getMessage(msgContainer);
    }
}