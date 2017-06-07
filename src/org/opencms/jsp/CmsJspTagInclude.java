/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.loader.CmsJspLoader;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.I_CmsResourceStringDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of the <code>&lt;cms:include/&gt;</code> tag,
 * used to include another OpenCms managed resource in a JSP.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagInclude extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 705978510743164951L;

    /** The value of the "attribute" attribute. */
    private String m_attribute;

    /** The value of the "cacheable" attribute. */
    private boolean m_cacheable;

    /** The value of the "editable" attribute. */
    private boolean m_editable;

    /** The value of the "element" attribute. */
    private String m_element;

    /** Map to save parameters to the include in. */
    private Map<String, String[]> m_parameterMap;

    /** The value of the "property" attribute. */
    private String m_property;

    /** The value of the "suffix" attribute. */
    private String m_suffix;

    /** The value of the "page" attribute. */
    private String m_target;

    /**
     * Empty constructor, required for attribute value initialization.<p>
     */
    public CmsJspTagInclude() {

        super();
        m_cacheable = true;
    }

    /**
     * Adds parameters to a parameter Map that can be used for a http request.<p>
     *
     * @param parameters the Map to add the parameters to
     * @param name the name to add
     * @param value the value to add
     * @param overwrite if <code>true</code>, a parameter in the map will be overwritten by
     *      a parameter with the same name, otherwise the request will have multiple parameters
     *      with the same name (which is possible in http requests)
     */
    public static void addParameter(Map<String, String[]> parameters, String name, String value, boolean overwrite) {

        // No null values allowed in parameters
        if ((parameters == null) || (name == null) || (value == null)) {
            return;
        }

        // Check if the parameter name (key) exists
        if (parameters.containsKey(name) && (!overwrite)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = parameters.get(name);
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            parameters.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] {value};
            parameters.put(name, values);
        }
    }

    /**
     * Includes the selected target.<p>
     *
     * @param context the current JSP page context
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable flag to indicate if the target is editable
     * @param paramMap a map of parameters for the include, will be merged with the request
     *      parameters, might be <code>null</code>
     * @param attrMap a map of attributes for the include, will be merged with the request
     *      attributes, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     *
     * @throws JspException in case something goes wrong
     */
    public static void includeTagAction(
        PageContext context,
        String target,
        String element,
        boolean editable,
        Map<String, String[]> paramMap,
        Map<String, Object> attrMap,
        ServletRequest req,
        ServletResponse res)
    throws JspException {

        // no locale and no cachable parameter are used by default
        includeTagAction(context, target, element, null, editable, true, paramMap, attrMap, req, res);
    }

    /**
     * Includes the selected target.<p>
     *
     * @param context the current JSP page context
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target, might be <code>null</code>
     * @param locale the locale to use for the selected element, might be <code>null</code>
     * @param editable flag to indicate if the target is editable
     * @param cacheable flag to indicate if the target should be cacheable in the Flex cache
     * @param paramMap a map of parameters for the include, will be merged with the request
     *      parameters, might be <code>null</code>
     * @param attrMap a map of attributes for the include, will be merged with the request
     *      attributes, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     *
     * @throws JspException in case something goes wrong
     */
    public static void includeTagAction(
        PageContext context,
        String target,
        String element,
        Locale locale,
        boolean editable,
        boolean cacheable,
        Map<String, String[]> paramMap,
        Map<String, Object> attrMap,
        ServletRequest req,
        ServletResponse res)
    throws JspException {

        // the Flex controller provides access to the internal OpenCms structures
        CmsFlexController controller = CmsFlexController.getController(req);

        if (target == null) {
            // set target to default
            target = controller.getCmsObject().getRequestContext().getUri();
        }

        // resolve possible relative URI
        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());

        try {
            // check if the target actually exists in the OpenCms VFS
            controller.getCmsObject().readResource(target);
        } catch (CmsException e) {
            // store exception in controller and discontinue
            controller.setThrowable(e, target);
            throw new JspException(e);
        }

        // include direct edit "start" element (if enabled)
        boolean directEditOpen = editable
            && CmsJspTagEditable.startDirectEdit(context, new CmsDirectEditParams(target, element));

        // save old parameters from request
        Map<String, String[]> oldParameterMap = CmsCollectionsGenericWrapper.map(req.getParameterMap());
        try {
            // each include will have it's unique map of parameters
            Map<String, String[]> parameterMap = (paramMap == null)
            ? new HashMap<String, String[]>()
            : new HashMap<String, String[]>(paramMap);
            if (cacheable && (element != null)) {
                // add template element selector for JSP templates (only required if cacheable)
                addParameter(parameterMap, I_CmsResourceLoader.PARAMETER_ELEMENT, element, true);
            }
            // add parameters to set the correct element
            controller.getCurrentRequest().addParameterMap(parameterMap);
            // each include will have it's unique map of attributes
            Map<String, Object> attributeMap = (attrMap == null)
            ? new HashMap<String, Object>()
            : new HashMap<String, Object>(attrMap);
            // add attributes to set the correct element
            controller.getCurrentRequest().addAttributeMap(attributeMap);
            if (cacheable) {
                // use include with cache
                includeActionWithCache(controller, context, target, parameterMap, attributeMap, req, res);
            } else {
                // no cache required
                includeActionNoCache(controller, context, target, element, locale, req, res);
            }
        } finally {
            // restore old parameter map (if required)
            if (oldParameterMap != null) {
                controller.getCurrentRequest().setParameterMap(oldParameterMap);
            }
        }

        // include direct edit "end" element (if required)
        if (directEditOpen) {
            CmsJspTagEditable.endDirectEdit(context);
        }
    }

    /**
     * Includes the selected target without caching.<p>
     *
     * @param controller the current JSP controller
     * @param context the current JSP page context
     * @param target the target for the include
     * @param element the element to select form the target
     * @param locale the locale to select from the target
     * @param req the current request
     * @param res the current response
     *
     * @throws JspException in case something goes wrong
     */
    private static void includeActionNoCache(
        CmsFlexController controller,
        PageContext context,
        String target,
        String element,
        Locale locale,
        ServletRequest req,
        ServletResponse res)
    throws JspException {

        try {
            // include is not cachable
            CmsFile file = controller.getCmsObject().readFile(target);
            CmsObject cms = controller.getCmsObject();
            if (locale == null) {
                locale = cms.getRequestContext().getLocale();
            }
            // get the loader for the requested file
            I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
            String content;
            if (loader instanceof I_CmsResourceStringDumpLoader) {
                // loader can provide content as a String
                I_CmsResourceStringDumpLoader strLoader = (I_CmsResourceStringDumpLoader)loader;
                content = strLoader.dumpAsString(cms, file, element, locale, req, res);
            } else {
                if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
                    // http type is required for loader (no refactoring to avoid changes to interface)
                    CmsLoaderException e = new CmsLoaderException(
                        Messages.get().container(Messages.ERR_BAD_REQUEST_RESPONSE_0));
                    throw new JspException(e);
                }
                // get the bytes from the loader and convert them to a String
                byte[] result = loader.dump(
                    cms,
                    file,
                    element,
                    locale,
                    (HttpServletRequest)req,
                    (HttpServletResponse)res);

                String encoding;
                if (loader instanceof CmsJspLoader) {
                    // in case of JSPs use the response encoding
                    encoding = res.getCharacterEncoding();
                } else {
                    // use the encoding from the property or the system default if not available
                    encoding = cms.readPropertyObject(
                        file,
                        CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                        true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
                }
                // If the included target issued a redirect null will be returned from loader
                if (result == null) {
                    result = new byte[0];
                }
                content = new String(result, encoding);
            }
            // write the content String to the JSP output writer
            context.getOut().print(content);

        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        } catch (CmsException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    }

    /**
     * Includes the selected target using the Flex cache.<p>
     *
     * @param controller the current JSP controller
     * @param context the current JSP page context
     * @param target the target for the include, might be <code>null</code>
     * @param parameterMap a map of parameters for the include
     * @param attributeMap a map of request attributes for the include
     * @param req the current request
     * @param res the current response
     *
     * @throws JspException in case something goes wrong
     */
    private static void includeActionWithCache(
        CmsFlexController controller,
        PageContext context,
        String target,
        Map<String, String[]> parameterMap,
        Map<String, Object> attributeMap,
        ServletRequest req,
        ServletResponse res)
    throws JspException {

        try {

            // add the target to the include list (the list will be initialized if it is currently empty)
            controller.getCurrentResponse().addToIncludeList(target, parameterMap, attributeMap);
            // now use the Flex dispatcher to include the target (this will also work for targets in the OpenCms VFS)
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, res);
            // write out a FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimiter later
            context.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);
        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    }

    /**
     * This methods adds parameters to the current request.<p>
     *
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
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(String, String)
     */
    public void addParameter(String name, String value) {

        // No null values allowed in parameters
        if ((name == null) || (value == null)) {
            return;
        }

        // Check if internal map exists, create new one if not
        if (m_parameterMap == null) {
            m_parameterMap = new HashMap<String, String[]>();
        }

        addParameter(m_parameterMap, name, value, false);
    }

    /**
     * @return <code>EVAL_PAGE</code>
     *
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     *
     * @throws JspException by interface default
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();

        if (CmsFlexController.isCmsRequest(req)) {
            // this will always be true if the page is called through OpenCms
            CmsObject cms = CmsFlexController.getCmsObject(req);
            String target = null;

            // try to find out what to do
            if (m_target != null) {
                // option 1: target is set with "page" or "file" parameter
                target = m_target + getSuffix();
            } else if (m_property != null) {
                // option 2: target is set with "property" parameter
                try {
                    String prop = cms.readPropertyObject(cms.getRequestContext().getUri(), m_property, true).getValue();
                    if (prop != null) {
                        target = prop + getSuffix();
                    }
                } catch (RuntimeException e) {
                    // target must be null
                    target = null;
                } catch (Exception e) {
                    // target will be null
                    e = null;
                }
            } else if (m_attribute != null) {
                // option 3: target is set in "attribute" parameter
                try {
                    String attr = (String)req.getAttribute(m_attribute);
                    if (attr != null) {
                        target = attr + getSuffix();
                    }
                } catch (RuntimeException e) {
                    // target must be null
                    target = null;
                } catch (Exception e) {
                    // target will be null
                    e = null;
                }
            } else {
                // option 4: target might be set in body
                String body = null;
                if (getBodyContent() != null) {
                    body = getBodyContent().getString();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(body)) {
                        // target IS set in body
                        target = body + getSuffix();
                    }
                    // else target is not set at all, default will be used
                }
            }

            // now perform the include action
            includeTagAction(
                pageContext,
                target,
                m_element,
                null,
                m_editable,
                m_cacheable,
                m_parameterMap,
                CmsRequestUtil.getAtrributeMap(req),
                req,
                res);

            release();
        }

        return EVAL_PAGE;
    }

    /**
     * Returns <code>{@link #EVAL_BODY_BUFFERED}</code>.<p>
     *
     * @return <code>{@link #EVAL_BODY_BUFFERED}</code>
     *
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the attribute.<p>
     *
     * @return the attribute
     */
    public String getAttribute() {

        return m_attribute != null ? m_attribute : "";
    }

    /**
     * Returns the cacheable flag.<p>
     *
     * @return the cacheable flag
     */
    public String getCacheable() {

        return String.valueOf(m_cacheable);
    }

    /**
     * Returns the editable flag.<p>
     *
     * @return the editable flag
     */
    public String getEditable() {

        return String.valueOf(m_editable);
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
     * Returns the value of <code>{@link #getPage()}</code>.<p>
     *
     * @return the value of <code>{@link #getPage()}</code>
     * @see #getPage()
     */
    public String getFile() {

        return getPage();
    }

    /**
     * Returns the include page target.<p>
     *
     * @return the include page target
     */
    public String getPage() {

        return m_target != null ? m_target : "";
    }

    /**
     * Returns the property.<p>
     *
     * @return the property
     */
    public String getProperty() {

        return m_property != null ? m_property : "";
    }

    /**
     * Returns the suffix.<p>
     *
     * @return the suffix
     */
    public String getSuffix() {

        return m_suffix != null ? m_suffix : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_target = null;
        m_suffix = null;
        m_property = null;
        m_element = null;
        m_parameterMap = null;
        m_editable = false;
        m_cacheable = true;
    }

    /**
     * Sets the attribute.<p>
     *
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(attribute)) {
            m_attribute = attribute;
        }
    }

    /**
     * Sets the cacheable flag.<p>
     *
     * Cachable is <code>true</code> by default.<p>
     *
     * @param cacheable the flag to set
     */
    public void setCacheable(String cacheable) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cacheable)) {
            m_cacheable = Boolean.valueOf(cacheable).booleanValue();
        }
    }

    /**
     * Sets the editable flag.<p>
     *
     * Editable is <code>false</code> by default.<p>
     *
     * @param editable the flag to set
     */
    public void setEditable(String editable) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(editable)) {
            m_editable = Boolean.valueOf(editable).booleanValue();
        }
    }

    /**
     * Sets the element.<p>
     *
     * @param element the element to set
     */
    public void setElement(String element) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(element)) {
            m_element = element;
        }
    }

    /**
     * Sets the file, same as using <code>setPage()</code>.<p>
     *
     * @param file the file to set
     * @see #setPage(String)
     */
    public void setFile(String file) {

        setPage(file);
    }

    /**
     * Sets the include page target.<p>
     *
     * @param target the target to set
     */
    public void setPage(String target) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
            m_target = target;
        }
    }

    /**
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(property)) {
            m_property = property;
        }
    }

    /**
     * Sets the suffix.<p>
     *
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(suffix)) {
            m_suffix = suffix.toLowerCase();
        }
    }
}