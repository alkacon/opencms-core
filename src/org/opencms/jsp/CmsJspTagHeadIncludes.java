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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsModelGroupHelper;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * This tag includes required CSS or JavaScript resources that are to be places in the HTML head.<p>
 *
 * Required resources can be configured in the resource type schema.
 * Set attribute type to 'css' to include css resources or to 'javascript' to include JavaScript resources.<p>
 *
 * @since 8.0
 */
public class CmsJspTagHeadIncludes extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** The include type CSS. */
    public static final String TYPE_CSS = "css";

    /** The include type java-script. */
    public static final String TYPE_JAVASCRIPT = "javascript";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagHeadIncludes.class);

    /** Serial version UID required for safe serialisation. */
    private static final long serialVersionUID = 5496349529835666345L;

    /** The value of the closetags attribute. */
    private String m_closeTags;

    /** The default include resources separated by '|'. */
    private String m_defaults;

    /** The detail container type. */
    private String m_detailType;

    /** The detail container width. */
    private String m_detailWidth;

    /** Map to save parameters to the include in. */
    private Map<String, String[]> m_parameterMap;

    /** The include type. */
    private String m_type;

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
     * Gets the head includes for the given formatter bean.<p>
     *
     * @param formatter the formatter bean
     * @param type the head include type
     *
     * @return the list of head includes
     */
    protected static List<String> getHeadIncludes(I_CmsFormatterBean formatter, String type) {

        if (TYPE_CSS.equals(type)) {
            return Lists.newArrayList(formatter.getCssHeadIncludes());
        } else if (TYPE_JAVASCRIPT.equals(type)) {
            return formatter.getJavascriptHeadIncludes();
        }
        return null;
    }

    /**
     * Gets the inline CSS/Javascrip for the given formatter bean.<p>
     *
     * @param formatter the formatter bean
     * @param type the type (CSS or Javascript)
     *
     * @return the inline data for the given formatter bean
     */
    protected static String getInlineData(I_CmsFormatterBean formatter, String type) {

        if (TYPE_CSS.equals(type)) {
            return formatter.getInlineCss();
        } else if (TYPE_JAVASCRIPT.equals(type)) {
            return formatter.getInlineJavascript();
        }
        return null;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
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
        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();
        try {
            if (TYPE_CSS.equals(m_type)) {
                tagCssAction(cms, req);
            }
            if (TYPE_JAVASCRIPT.equals(m_type)) {
                tagJSAction(cms, req);
            }
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
            m_parameterMap = null;
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
     * Returns the default include resources separated by '|'.<p>
     *
     * @return the default include resources
     */
    public String getDefaults() {

        return m_defaults;
    }

    /**
     * Returns the detail container type.<p>
     *
     * @return the detail container type
     */
    public String getDetailtype() {

        return m_detailType;
    }

    /**
     * Returns the detail container width.<p>
     *
     * @return the detail container width
     */
    public String getDetailwidth() {

        return m_detailWidth;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Sets the value of the closetags attribute.<p>
     *
     * @param closeTags the value of the closetags attribute
     */
    public void setClosetags(String closeTags) {

        m_closeTags = closeTags;
    }

    /**
     * Sets the default include resources separated by '|'.<p>
     *
     * @param defaults the default include resources to set
     */
    public void setDefaults(String defaults) {

        m_defaults = defaults;
    }

    /**
     * Sets the detail container type.<p>
     *
     * @param detailType the detail container type to set
     */
    public void setDetailtype(String detailType) {

        m_detailType = detailType;
    }

    /**
     * Sets the detail container width.<p>
     *
     * @param detailWidth the detail container width to set
     */
    public void setDetailwidth(String detailWidth) {

        m_detailWidth = detailWidth;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Returns true if the headincludes tag should be closed.<p>
     *
     * @return true if the headincludes tag should be closed
     */
    public boolean shouldCloseTags() {

        if (m_closeTags == null) {
            return true;
        }
        return Boolean.parseBoolean(m_closeTags);
    }

    /**
     * Action to include the CSS resources.<p>
     *
     * @param cms the current cms context
     * @param req the current request
     *
     * @throws CmsException if something goes wrong reading the resources
     * @throws IOException if something goes wrong writing to the response out
     */
    public void tagCssAction(CmsObject cms, ServletRequest req) throws CmsException, IOException {

        String includeType = TYPE_CSS;

        CmsJspStandardContextBean standardContext = getStandardContext(cms, req);
        CmsContainerPageBean containerPage = standardContext.getPage();

        Set<String> cssIncludes = new LinkedHashSet<String>();
        Map<String, String> inlineCss = new LinkedHashMap<String, String>();
        // add defaults
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_defaults)) {
            String[] defaults = m_defaults.split("\\|");
            for (int i = 0; i < defaults.length; i++) {
                cssIncludes.add(defaults[i].trim());
            }
        }

        collectHeadIncludesForContainerElement(
            cms,
            req,
            standardContext,
            containerPage,
            includeType,
            cssIncludes,
            inlineCss);
        if (standardContext.getDetailContentId() != null) {
            try {
                CmsResource detailContent = cms.readResource(
                    standardContext.getDetailContentId(),
                    CmsResourceFilter.ignoreExpirationOffline(cms));
                CmsFormatterConfiguration config = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    cms.getRequestContext().getRootUri()).getFormatters(cms, detailContent);
                boolean requiresAllIncludes = true;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getDetailtype())
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getDetailwidth())) {
                    try {
                        int width = Integer.parseInt(getDetailwidth());
                        I_CmsFormatterBean formatter = config.getDetailFormatter(getDetailtype(), width);
                        cssIncludes.addAll(formatter.getCssHeadIncludes());
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatter.getInlineCss())) {
                            inlineCss.put(formatter.getId(), formatter.getInlineCss());
                        }
                        requiresAllIncludes = false;
                    } catch (NumberFormatException ne) {
                        // nothing to do, we will include CSS for all detail containers
                    }
                }
                if (requiresAllIncludes) {
                    for (I_CmsFormatterBean formatter : config.getDetailFormatters()) {
                        cssIncludes.addAll(getHeadIncludes(formatter, includeType));
                        String inlineIncludeData = getInlineData(formatter, includeType);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(inlineIncludeData)) {
                            inlineCss.put(formatter.getId(), inlineIncludeData);
                        }
                    }
                }
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_READING_REQUIRED_RESOURCE_1,
                        standardContext.getDetailContentId()),
                    e);
            }
        }
        for (String cssUri : cssIncludes) {
            pageContext.getOut().print(
                "\n<link rel=\"stylesheet\" href=\""
                    + CmsJspTagLink.linkTagAction(cssUri.trim(), req)
                    + generateReqParams()
                    + "\" type=\"text/css\">");
            if (shouldCloseTags()) {
                pageContext.getOut().print("</link>");
            }
        }
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            if (!inlineCss.isEmpty()) {
                StringBuffer inline = new StringBuffer("\n<style type=\"text/css\">\n");
                for (Entry<String, String> cssEntry : inlineCss.entrySet()) {
                    inline.append(cssEntry.getValue()).append("\n\n");
                }
                inline.append("\n</style>\n");
                pageContext.getOut().print(inline.toString());
            }
        } else {
            StringBuffer inline = new StringBuffer();
            for (Entry<String, String> cssEntry : inlineCss.entrySet()) {
                inline.append("\n<style type=\"text/css\" rel=\"" + cssEntry.getKey() + "\">\n");
                inline.append(cssEntry.getValue()).append("\n\n");
                inline.append("\n</style>\n");
            }
            pageContext.getOut().print(inline.toString());
        }
    }

    /**
     * Action to include the java-script resources.<p>
     *
     * @param cms the current cms context
     * @param req the current request
     *
     * @throws CmsException if something goes wrong reading the resources
     * @throws IOException if something goes wrong writing to the response out
     */
    public void tagJSAction(CmsObject cms, ServletRequest req) throws CmsException, IOException {

        CmsJspStandardContextBean standardContext = getStandardContext(cms, req);
        CmsContainerPageBean containerPage = standardContext.getPage();
        String includeType = TYPE_JAVASCRIPT;
        Set<String> jsIncludes = new LinkedHashSet<String>();
        Map<String, String> inlineJS = new LinkedHashMap<String, String>();
        // add defaults
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_defaults)) {
            String[] defaults = m_defaults.split("\\|");
            for (int i = 0; i < defaults.length; i++) {
                jsIncludes.add(defaults[i].trim());
            }
        }
        collectHeadIncludesForContainerElement(
            cms,
            req,
            standardContext,
            containerPage,
            includeType,
            jsIncludes,
            inlineJS);
        if (standardContext.getDetailContentId() != null) {
            try {
                CmsResource detailContent = cms.readResource(
                    standardContext.getDetailContentId(),
                    CmsResourceFilter.ignoreExpirationOffline(cms));
                CmsFormatterConfiguration config = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    cms.getRequestContext().getRootUri()).getFormatters(cms, detailContent);
                boolean requiresAllIncludes = true;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getDetailtype())
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getDetailwidth())) {
                    try {
                        int width = Integer.parseInt(getDetailwidth());
                        I_CmsFormatterBean formatter = config.getDetailFormatter(getDetailtype(), width);
                        jsIncludes.addAll(formatter.getJavascriptHeadIncludes());
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatter.getInlineJavascript())) {
                            inlineJS.put(formatter.getId(), formatter.getInlineJavascript());
                        }
                        requiresAllIncludes = false;
                    } catch (NumberFormatException ne) {
                        // nothing to do, we will include JavaScript for all detail containers
                    }
                }
                if (requiresAllIncludes) {
                    for (I_CmsFormatterBean formatter : config.getDetailFormatters()) {
                        jsIncludes.addAll(getHeadIncludes(formatter, includeType));
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatter.getInlineJavascript())) {
                            inlineJS.put(formatter.getId(), formatter.getInlineJavascript());
                        }
                    }
                }
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_READING_REQUIRED_RESOURCE_1,
                        standardContext.getDetailContentId()),
                    e);
            }
        }
        for (String jsUri : jsIncludes) {
            pageContext.getOut().print(
                "\n<script type=\"text/javascript\" src=\""
                    + CmsJspTagLink.linkTagAction(jsUri.trim(), req)
                    + generateReqParams()
                    + "\"></script>");
        }
        if (!inlineJS.isEmpty()) {
            StringBuffer inline = new StringBuffer();
            for (Entry<String, String> jsEntry : inlineJS.entrySet()) {
                inline.append("\n<script type=\"text/javascript\">\n");
                inline.append(jsEntry.getValue()).append("\n\n");
                inline.append("\n</script>\n");
            }
            pageContext.getOut().print(inline.toString());
        }
    }

    /**
     * Collects the head includes for a given container element.<p>
     *
     * @param cms the current CMS context
     * @param req the current request
     * @param standardContext the current standard context
     * @param containerPage the current container page
     * @param includeType the type of head includes (CSS or Javascript)
     * @param headincludes the set to which normal head includes should be added
     * @param inline the map to which inline head includes should be added
     */
    protected void collectHeadIncludesForContainerElement(
        CmsObject cms,
        ServletRequest req,
        CmsJspStandardContextBean standardContext,
        CmsContainerPageBean containerPage,
        String includeType,
        Set<String> headincludes,
        Map<String, String> inline) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        if ((containerPage != null) && (containerPage.getElements() != null)) {
            Map<CmsUUID, I_CmsFormatterBean> formatters = OpenCms.getADEManager().getCachedFormatters(
                standardContext.getIsOnlineProject()).getFormatters();
            List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>(containerPage.getContainers().values());
            // add detail only containers if available
            if (standardContext.isDetailRequest()) {
                CmsContainerPageBean detailOnly = CmsJspTagContainer.getDetailOnlyPage(cms, req);
                if (detailOnly != null) {
                    containers.addAll(detailOnly.getContainers().values());
                }
            }
            for (CmsContainerBean container : containers) {
                for (CmsContainerElementBean element : container.getElements()) {
                    try {
                        element.initResource(cms);
                        if (!standardContext.getIsOnlineProject()
                            || element.getResource().isReleasedAndNotExpired(
                                cms.getRequestContext().getRequestTime())) {
                            if (element.isGroupContainer(cms) || element.isInheritedContainer(cms)) {
                                List<CmsContainerElementBean> subElements;
                                if (element.isGroupContainer(cms)) {
                                    subElements = CmsJspTagContainer.getGroupContainerElements(
                                        cms,
                                        element,
                                        req,
                                        container.getType());
                                } else {
                                    subElements = CmsJspTagContainer.getInheritedContainerElements(cms, element);
                                }
                                for (CmsContainerElementBean subElement : subElements) {
                                    subElement.initResource(cms);
                                    if (!standardContext.getIsOnlineProject()
                                        || subElement.getResource().isReleasedAndNotExpired(
                                            cms.getRequestContext().getRequestTime())) {
                                        I_CmsFormatterBean formatter = getFormatterBeanForElement(
                                            cms,
                                            config,
                                            subElement,
                                            container,
                                            false,
                                            formatters);
                                        if (formatter != null) {
                                            headincludes.addAll(getHeadIncludes(formatter, includeType));
                                            String inlineIncludeData = getInlineData(formatter, includeType);
                                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(inlineIncludeData)) {
                                                inline.put(formatter.getId(), inlineIncludeData);
                                            }
                                        } else {

                                            headincludes.addAll(
                                                getSchemaHeadIncludes(cms, subElement.getResource(), includeType));
                                        }
                                    }
                                }
                            } else {
                                I_CmsFormatterBean formatter = getFormatterBeanForElement(
                                    cms,
                                    config,
                                    element,
                                    container,
                                    true,
                                    formatters);
                                if (formatter != null) {
                                    headincludes.addAll(getHeadIncludes(formatter, includeType));
                                    String inlineIncludeData = getInlineData(formatter, includeType);
                                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(inlineIncludeData)) {
                                        inline.put(formatter.getId(), inlineIncludeData);
                                    }
                                } else {

                                    headincludes.addAll(getSchemaHeadIncludes(cms, element.getResource(), includeType));
                                }
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_READING_REQUIRED_RESOURCE_1,
                                element.getSitePath()),
                            e);
                    }
                }
            }
        }
    }

    /**
     * Generates the request parameter string.<p>
     *
     * @return the request parameter string
     *
     * @throws UnsupportedEncodingException if something goes wrong encoding the request parameters
     */
    private String generateReqParams() throws UnsupportedEncodingException {

        String params = "";
        if ((m_parameterMap != null) && !m_parameterMap.isEmpty()) {
            for (Entry<String, String[]> paramEntry : m_parameterMap.entrySet()) {
                if (paramEntry.getValue() != null) {
                    for (int i = 0; i < paramEntry.getValue().length; i++) {
                        params += "&"
                            + paramEntry.getKey()
                            + "="
                            + URLEncoder.encode(paramEntry.getValue()[i], "UTF-8");
                    }
                }
            }
            params = "?" + params.substring(1);
        }
        return params;
    }

    /**
     * Returns the schema configured CSS head include resources.<p>
     *
     * @param cms the current cms context
     * @param resource the resource
     *
     * @return the configured CSS head include resources
     */
    private Set<String> getCSSHeadIncludes(CmsObject cms, CmsResource resource) {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            try {
                CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.getContentDefinitionForResource(
                    cms,
                    resource);
                return contentDefinition.getContentHandler().getCSSHeadIncludes(cms, resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                // NOOP, use the empty set
            }
        }
        return Collections.emptySet();
    }

    /**
     * Returns the formatter configuration for the given element, will return <code>null</code> for schema formatters.<p>
     *
     * @param cms the current CMS context
     * @param config the current sitemap configuration
     * @param element the element bean
     * @param container the container bean
     * @param allowNested if nested containers are allowed
     * @param formatters the formatter map
     *
     * @return the formatter configuration bean
     */
    private I_CmsFormatterBean getFormatterBeanForElement(
        CmsObject cms,
        CmsADEConfigData config,
        CmsContainerElementBean element,
        CmsContainerBean container,
        boolean allowNested,
        Map<CmsUUID, I_CmsFormatterBean> formatters) {

        int containerWidth = -1;
        if (container.getWidth() == null) {
            // the container width has not been set yet
            containerWidth = CmsFormatterConfiguration.MATCH_ALL_CONTAINER_WIDTH;
        } else {
            try {

                containerWidth = Integer.parseInt(container.getWidth());
            } catch (NumberFormatException e) {
                // do nothing, set width to -1
            }
        }
        I_CmsFormatterBean result = CmsJspTagContainer.getFormatterConfigurationForElement(
            cms,
            element,
            config,
            container.getName(),
            container.getType(),
            containerWidth,
            allowNested);
        return result;
    }

    /**
     * Returns the schema configured JavaScript head include resources.<p>
     *
     * @param cms the current cms context
     * @param resource the resource
     *
     * @return the configured JavaScript head include resources
     *
     * @throws CmsLoaderException if something goes wrong reading the resource type
     */
    private Set<String> getJSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsLoaderException {

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        if (resType instanceof CmsResourceTypeXmlContent) {
            try {
                CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.getContentDefinitionForResource(
                    cms,
                    resource);
                return contentDefinition.getContentHandler().getJSHeadIncludes(cms, resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                // NOOP, use the empty set
            }
        }
        return Collections.emptySet();
    }

    /**
     * Gets the head includes of a resource from the content definition.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the head includes should be fetched
     * @param type the head include type (CSS or Javascript)
     *
     * @return the set of schema head includes
     *
     * @throws CmsLoaderException if something goes wrong
     */
    private Set<String> getSchemaHeadIncludes(CmsObject cms, CmsResource res, String type) throws CmsLoaderException {

        if (type.equals(TYPE_CSS)) {
            return getCSSHeadIncludes(cms, res);
        } else if (type.equals(TYPE_JAVASCRIPT)) {
            return getJSHeadIncludes(cms, res);
        }
        return null;
    }

    /**
     * Returns the standard context bean.<p>
     *
     * @param cms the current cms context
     * @param req the current request
     *
     * @return the standard context bean
     *
     * @throws CmsException if something goes wrong
     */
    private CmsJspStandardContextBean getStandardContext(CmsObject cms, ServletRequest req) throws CmsException {

        String requestUri = cms.getRequestContext().getUri();
        CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
        CmsContainerPageBean containerPage = standardContext.getPage();
        if (containerPage == null) {
            // get the container page itself, checking the history first
            CmsResource pageResource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
            if (pageResource == null) {
                pageResource = cms.readResource(requestUri);
            }
            CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, pageResource, req);
            containerPage = xmlContainerPage.getContainerPage(cms);
            CmsModelGroupHelper modelHelper = new CmsModelGroupHelper(
                cms,
                OpenCms.getADEManager().lookupConfiguration(cms, cms.getRequestContext().getRootUri()),
                CmsJspTagEditable.isEditableRequest(req)
                ? CmsADESessionCache.getCache((HttpServletRequest)(pageContext.getRequest()), cms)
                : null,
                CmsContainerpageService.isEditingModelGroups(cms, pageResource));
            containerPage = modelHelper.readModelGroups(xmlContainerPage.getContainerPage(cms));
            standardContext.setPage(containerPage);
        }
        return standardContext;
    }

}
