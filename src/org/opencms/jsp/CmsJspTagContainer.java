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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.ade.containerpage.CmsElementUtil;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagAddParams.ParamState;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.jsp.util.CmsJspStandardContextBean.CmsContainerElementWrapper;
import org.opencms.loader.CmsJspLoader;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsTemplateContext;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsTemplateContextProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
import org.opencms.xml.containerpage.CmsXmlInheritGroupContainerHandler;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.templatemapper.CmsTemplateMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;

/**
 * Provides access to the page container elements.<p>
 *
 * @since 8.0
 */
public class CmsJspTagContainer extends BodyTagSupport implements TryCatchFinally, I_CmsJspTagParamParent {

    /** Default number of max elements in the container in case no value has been set. */
    public static final String DEFAULT_MAX_ELEMENTS = "100";

    /** The detail function container name. */
    public static final String DETAIL_FUNCTION_CONTAINER_NAME = "FunctionDefault";

    /** HTML used for invisible dummy elements. */
    public static final String DUMMY_ELEMENT = "<div class='"
        + CmsTemplateContextInfo.DUMMY_ELEMENT_MARKER
        + "' style='display: none !important;'></div>";

    /** The default tag name constant. */
    private static final String DEFAULT_TAG_NAME = "div";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContainer.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1228397990961282556L;

    /** The evaluated body content if available. */
    private String m_bodyContent;

    /** If false, formatters are always included in non-cacheable mode, otherwise they are included in cacheable mode in the Online project only. */
    private boolean m_cacheable = true;

    /** States if this container should only be displayed on detail pages. */
    private boolean m_detailOnly;

    /** The detail-view attribute value. */
    private boolean m_detailView;

    /** The editable by tag attribute. A comma separated list of OpenCms principals. */
    private String m_editableBy;

    /** Indicating that the container page editor is active for the current request. */
    private boolean m_editableRequest;

    /** Indicates this container is nested within a model group, only set for editable requests. */
    private boolean m_hasModelGroupAncestor;

    /** The maxElements attribute value. */
    private String m_maxElements;

    /** The name attribute value. */
    private String m_name;

    /**
     * The container name prefix to use for nested container names.
     * If empty the element instance id of the parent element will be used.
     **/
    private String m_namePrefix;

    /** The optional container parameter. */
    private String m_param;

    /** The parameter state. */
    private CmsJspTagAddParams.ParamState m_paramState;

    /** The parent container. */
    private CmsContainerBean m_parentContainer;

    /** The parent element to this container. */
    private CmsContainerElementBean m_parentElement;

    /** The container setting presets. */
    private HashMap<String, String> m_settingPresets;

    /** The tag attribute value. */
    private String m_tag;

    /** The class attribute value. */
    private String m_tagClass;

    /** The type attribute value. */
    private String m_type;

    /** The container width as a string. */
    private String m_width;

    /**
     * Ensures the appropriate formatter configuration ID is set in the element settings.<p>
     *
     * @param cms the cms context
     * @param element the element bean
     * @param adeConfig the ADE configuration data
     * @param containerName the container name
     * @param containerType the container type
     * @param containerWidth the container width
     *
     * @return the formatter configuration bean, may be <code>null</code> if no formatter available or a schema formatter is used
     */
    public static I_CmsFormatterBean ensureValidFormatterSettings(
        CmsObject cms,
        CmsContainerElementBean element,
        CmsADEConfigData adeConfig,
        String containerName,
        String containerType,
        int containerWidth) {

        I_CmsFormatterBean formatterBean = getFormatterConfigurationForElement(
            cms,
            element,
            adeConfig,
            containerName,
            containerType,
            containerWidth);
        String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(containerName);
        if (formatterBean != null) {
            String keyOrId = formatterBean.getKeyOrId();
            if (keyOrId == null) {
                keyOrId = CmsFormatterConfig.SCHEMA_FORMATTER_ID + formatterBean.getJspStructureId().toString();
            }
            element.getSettings().put(settingsKey, keyOrId);
            element.setFormatterId(formatterBean.getJspStructureId());
        }
        return formatterBean;
    }

    /**
     * Returns the formatter configuration for the given element.<p>
     *
     * @param cms the cms context
     * @param element the element bean
     * @param adeConfig the ADE configuration
     * @param containerName the container name
     * @param containerType the container type
     * @param containerWidth the container width
     *
     * @return the formatter configuration
     */
    public static I_CmsFormatterBean getFormatterConfigurationForElement(
        CmsObject cms,
        CmsContainerElementBean element,
        CmsADEConfigData adeConfig,
        String containerName,
        String containerType,
        int containerWidth) {

        I_CmsFormatterBean formatterBean = null;
        String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(containerName);
        String formatterSetting = element.getSettings().get(settingsKey);
        CmsFormatterConfiguration formatterConfig = null;
        if (formatterSetting != null) {
            formatterConfig = adeConfig.getFormatters(cms, element.getResource());
            // getFormattersForKey also works for the schema_formaterXXXXXX setting values
            List<I_CmsFormatterBean> candidates = formatterConfig.getFormattersForKey(formatterSetting);
            if (candidates.size() > 0) {
                formatterBean = candidates.get(0);
            } else {
                formatterBean = adeConfig.findFormatter(formatterSetting);
            }
        }

        if ((formatterBean == null) && (element.getFormatterId() != null) && !element.getFormatterId().isNullUUID()) {
            if (formatterConfig == null) {
                formatterConfig = adeConfig.getFormatters(cms, element.getResource());
            }

            for (I_CmsFormatterBean formatter : adeConfig.getFormatters(
                cms,
                element.getResource()).getAllMatchingFormatters(containerType, containerWidth)) {

                if (element.getFormatterId().equals(formatter.getJspStructureId())) {
                    String formatterConfigId = formatter.getId();
                    if (formatterConfigId == null) {
                        formatterConfigId = CmsFormatterConfig.SCHEMA_FORMATTER_ID
                            + element.getFormatterId().toString();
                    }
                    formatterBean = formatter;
                    break;
                }
            }
        }

        if (formatterBean == null) {

            formatterBean = adeConfig.getFormatters(cms, element.getResource()).getDefaultFormatter(
                containerType,
                containerWidth);
        }
        return formatterBean;
    }

    /**
     * Returns the element group elements.<p>
     *
     * @param cms the current cms context
     * @param element group element
     * @param req the servlet request
     * @param containerType the container type
     *
     * @return the elements of this group
     *
     * @throws CmsException if something goes wrong
     */
    public static List<CmsContainerElementBean> getGroupContainerElements(
        CmsObject cms,
        CmsContainerElementBean element,
        ServletRequest req,
        String containerType)
    throws CmsException {

        List<CmsContainerElementBean> subElements;
        CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(cms, element.getResource(), req);
        CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(cms);
        groupContainer = CmsTemplateMapper.get(req).transformGroupContainer(
            cms,
            groupContainer,
            xmlGroupContainer.getFile().getRootPath());
        if (!CmsElementUtil.checkGroupAllowed(containerType, groupContainer)) {
            LOG.warn(
                new CmsIllegalStateException(
                    Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        element.getResource().getRootPath(),
                        OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName(),
                        containerType)));
            return Collections.emptyList();
        }
        subElements = groupContainer.getElements();
        return subElements;
    }

    /**
     * Reads elements from an inherited container.<p>
     *
     * @param cms the current CMS context
     * @param element the element which references the inherited container
     *
     * @return the container elements
     */

    public static List<CmsContainerElementBean> getInheritedContainerElements(
        CmsObject cms,
        CmsContainerElementBean element) {

        CmsResource resource = element.getResource();
        return CmsXmlInheritGroupContainerHandler.loadInheritContainerElements(cms, resource);
    }

    /**
     * Returns the prefixed nested container name.<p>
     * This will be either {parentInstanceId}-{name} or {namePrefix}-{name} or in case namePrefix equals 'none' {name} only.<p>
     *
     * @param name the container name
     * @param parentIstanceId the parent instance id
     * @param namePrefix the name prefix attribute
     *
     * @return the nested container name
     */
    public static String getNestedContainerName(String name, String parentIstanceId, String namePrefix) {

        String prefix;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(namePrefix)) {
            prefix = parentIstanceId + "-";
        } else if ("none".equals(namePrefix)) {
            prefix = "";
        } else {
            prefix = namePrefix + "-";
        }
        return prefix + name;
    }

    /**
     * Creates the closing tag for the container.<p>
     *
     * @param tagName the tag name
     *
     * @return the closing tag
     */
    protected static String getTagClose(String tagName) {

        return "</" + tagName + ">";
    }

    /**
     * Creates the opening tag for the container assigning the appropriate id and class attributes.<p>
     *
     * @param tagName the tag name
     * @param containerName the container name used as id attribute value
     * @param tagClass the tag class attribute value
     * @param nested true if this is a nested container
     * @param online true if we are in the online project
     * @param containerData the container data
     *
     * @return the opening tag
     */
    protected static String getTagOpen(
        String tagName,
        String containerName,
        String tagClass,
        boolean nested,
        boolean online,
        String containerData) {

        StringBuffer buffer = new StringBuffer(32);
        buffer.append("<").append(tagName).append(" ");
        if (online && nested) {
            // omit generated ids when online
        } else {
            buffer.append(" id=\"").append(containerName).append("\" ");
        }
        if (containerData != null) {
            buffer.append(" " + CmsGwtConstants.ATTR_DATA_CONTAINER + "=\"").append(containerData).append("\" ");
            // set the marker CSS class
            tagClass = tagClass == null
            ? CmsContainerElement.CLASS_CONTAINER
            : tagClass + " " + CmsContainerElement.CLASS_CONTAINER;
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(tagClass)) {
            buffer.append("class=\"").append(tagClass).append("\" ");
        }
        buffer.append(">");
        return buffer.toString();
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        if (m_paramState != null) {
            m_paramState.addParameter(name, value);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    @SuppressWarnings("resource")
    @Override
    public int doAfterBody() {

        // store the evaluated body content for later use
        BodyContent bc = getBodyContent();
        if (bc != null) {
            m_bodyContent = bc.getString();
            try {
                bc.clear();
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally#doCatch(java.lang.Throwable)
     */
    public void doCatch(Throwable t) throws Throwable {

        throw t;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                CmsFlexController controller = CmsFlexController.getController(req);
                CmsObject cms = controller.getCmsObject();
                String requestUri = cms.getRequestContext().getUri();
                Locale locale = cms.getRequestContext().getLocale();
                CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
                standardContext.initPage();
                m_editableRequest = standardContext.getIsEditMode();
                m_parentElement = standardContext.getElement();
                m_parentContainer = standardContext.getContainer();
                m_hasModelGroupAncestor = m_editableRequest ? hasModelGroupAncestor(standardContext) : false;
                CmsContainerPageBean containerPage = standardContext.getPage();
                CmsResource detailContent = standardContext.getDetailContent();
                CmsResource detailFunctionPage = standardContext.getDetailFunctionPage();
                // get the container
                CmsContainerBean container = null;
                boolean detailOnly = m_detailOnly || ((m_parentContainer != null) && m_parentContainer.isDetailOnly());
                if (detailOnly) {
                    if (detailContent == null) {
                        // this is no detail page, so the detail only container will not be rendered at all
                        resetState();
                        return EVAL_PAGE;
                    } else {
                        String pageRootPath = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
                        CmsContainerPageBean detailOnlyPage = CmsDetailOnlyContainerUtil.getDetailOnlyPage(
                            cms,
                            req,
                            pageRootPath);
                        if (detailOnlyPage != null) {
                            container = detailOnlyPage.getContainers().get(getName());
                        }
                        if ((container == null) && m_editableRequest && (containerPage != null)) {
                            // this is for the case where the current container is the nested container of a model group which the user is dragging into a detail container
                            container = containerPage.getContainers().get(getName());
                        }
                    }
                } else if (containerPage != null) {
                    container = containerPage.getContainers().get(getName());
                }
                // get the maximal number of elements
                int maxElements = getMaxElements(requestUri);
                if (container == null) {
                    container = new CmsContainerBean(
                        getName(),
                        getType(),
                        m_parentElement != null ? m_parentElement.getInstanceId() : null,
                        (m_parentContainer == null) || (m_detailOnly && !m_parentContainer.isDetailOnly()),
                        maxElements,
                        Collections.<CmsContainerElementBean> emptyList());
                } else if ((m_parentElement != null)
                    && !m_detailOnly //ignore parent information for detail only containers to render content on different detail pages.
                    && !m_parentElement.getInstanceId().equals(container.getParentInstanceId())) {
                    // the container parent instance id does not match the parent element instance id, skip rendering to avoid recursion
                    LOG.error(
                        new CmsIllegalStateException(
                            Messages.get().container(
                                Messages.ERR_INVALID_CONTAINER_PARENT_2,
                                getName(),
                                m_parentElement.getInstanceId())));
                    resetState();
                    return EVAL_PAGE;
                }
                // set the parameter
                container.setParam(getParam());
                // set the detail only flag
                container.setDetailOnly(detailOnly);
                boolean isUsedAsDetailView = false;
                if (m_detailView && ((detailContent != null) || (detailFunctionPage != null))) {
                    isUsedAsDetailView = true;
                }
                // create tag for container
                String tagName = CmsStringUtil.isEmptyOrWhitespaceOnly(getTag()) ? DEFAULT_TAG_NAME : getTag();
                pageContext.getOut().print(
                    getTagOpen(
                        tagName,
                        getName(),
                        getTagClass(),
                        isNested(),
                        !m_editableRequest,
                        m_editableRequest ? getContainerData(cms, maxElements, isUsedAsDetailView, detailOnly) : null));

                standardContext.setContainer(container);
                // validate the type
                if (!getType().equals(container.getType())) {
                    container.setType(getType());
                    LOG.warn(
                        new CmsIllegalStateException(
                            Messages.get().container(
                                Messages.LOG_WRONG_CONTAINER_TYPE_4,
                                new Object[] {requestUri, locale, getName(), getType()})));
                }

                // update the cache
                container.setMaxElements(maxElements);
                container.setWidth("" + getContainerWidth());
                List<CmsContainerElementBean> allElements = new ArrayList<CmsContainerElementBean>();
                CmsContainerElementBean detailElement = null;
                if (isUsedAsDetailView) {
                    if (detailContent != null) {
                        detailElement = generateDetailViewElement(req, cms, detailContent, container);
                    } else {
                        detailElement = getDetailFunctionElement(cms, detailFunctionPage, req);
                    }
                }
                if (detailElement != null) {
                    allElements.add(detailElement);
                } else {
                    allElements.addAll(container.getElements());
                }
                // iterate over elements to render
                int numRenderedElements = 0;
                boolean first = true;
                for (CmsContainerElementBean elementBean : allElements) {
                    // in case of rendering a detail container on a detail page,
                    // the first element may be used to provide settings for the detail content
                    // this element will not be rendered, in case the detail page is not actually used to render detail content
                    boolean skipDetailTemplateElement = false;
                    try {
                        skipDetailTemplateElement = first
                            && !m_editableRequest
                            && m_detailView
                            && (detailElement == null)
                            && OpenCms.getADEManager().isDetailPage(cms, standardContext.getPageResource())
                            && OpenCms.getADEManager().getDetailPages(cms, elementBean.getTypeName()).contains(
                                CmsResource.getFolderPath(standardContext.getPageResource().getRootPath()));
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    first = false;
                    if (!skipDetailTemplateElement) {
                        try {
                            boolean rendered = renderContainerElement(
                                (HttpServletRequest)req,
                                cms,
                                standardContext,
                                elementBean,
                                locale,
                                numRenderedElements >= maxElements);
                            if (rendered) {
                                numRenderedElements += 1;
                            }
                        } catch (Exception e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
                if ((numRenderedElements == 0) && (m_bodyContent != null) && CmsJspTagEditable.isEditableRequest(req)) {
                    // the container is empty, print the evaluated body content
                    pageContext.getOut().print(m_bodyContent);
                }
                // close tag for container
                pageContext.getOut().print(getTagClose(tagName));
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "container"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }

        resetState();
        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally#doFinally()
     */
    public void doFinally() {

        if (m_paramState != null) {
            m_paramState.undoChanges();
            m_paramState = null;
        }
    }

    /**
     * Internal action method.<p>
     *
     * @return EVAL_BODY_BUFFERED
     *
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        if (CmsFlexController.isCmsRequest(pageContext.getRequest())) {
            m_paramState = new ParamState(
                CmsFlexController.getController(pageContext.getRequest()).getCurrentRequest());
            m_paramState.init();
        }
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the boolean value if this container is target of detail views.<p>
     *
     * @return <code>true</code> or <code>false</code>
     */
    public String getDetailview() {

        return String.valueOf(m_detailView);
    }

    /**
     * Returns the editable by tag attribute.<p>
     *
     * @return the editable by tag attribute
     */
    public String getEditableby() {

        return m_editableBy;
    }

    /**
     * Returns the maxElements attribute value.<p>
     *
     * @return the maxElements attribute value
     */
    public String getMaxElements() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_maxElements) ? DEFAULT_MAX_ELEMENTS : m_maxElements;
    }

    /**
     * Returns the container name, in case of nested containers with a prefix to guaranty uniqueness.<p>
     *
     * @return String the container name
     */
    public String getName() {

        if (isNested()) {
            return getNestedContainerName(m_name, m_parentElement.getInstanceId(), m_namePrefix);
        }
        return m_name;
    }

    /**
     * Returns the name prefix.<p>
     *
     * @return the namePrefix
     */
    public String getNameprefix() {

        return m_namePrefix;
    }

    /**
     * Returns the (optional) container parameter.<p>
     *
     * This is useful for a dynamically generated nested container,
     * to pass information to the formatter used inside that container.
     *
     * If no parameters have been set, this will return <code>null</code>
     *
     * @return the (optional) container parameter
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns the tag attribute.<p>
     *
     * @return the tag attribute
     */
    public String getTag() {

        return m_tag;
    }

    /**
     * Returns the tag class attribute.<p>
     *
     * @return the tag class attribute
     */
    public String getTagClass() {

        return m_tagClass;
    }

    /**
     * Returns the type attribute value.<p>
     *
     * If the container type has not been set, the name is substituted as type.<p>
     *
     * @return the type attribute value
     */
    public String getType() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_type) ? getName() : m_type;
    }

    /**
     * Returns the container width as a string.<p>
     *
     * @return the container width as a string
     */
    public String getWidth() {

        return m_width;
    }

    /**
     * Sets the 'cacheable' mode for included formatters.
     *
     * <p>If this is set to false, formatters will never be included in cacheable mode, otherwise they will
     * only be included in cacheable mode in the Online project.
     *
     * @param cacheable the cacheable mode (true or false)
     */
    public void setCacheable(String cacheable) {

        m_cacheable = Boolean.parseBoolean(cacheable);
    }

    /**
     * Sets if this container should only be displayed on detail pages.<p>
     *
     * @param detailOnly if this container should only be displayed on detail pages
     */
    public void setDetailonly(String detailOnly) {

        m_detailOnly = Boolean.parseBoolean(detailOnly);
    }

    /**
     * Sets if the current container is target of detail views.<p>
     *
     * @param detailView <code>true</code> or <code>false</code>
     */
    public void setDetailview(String detailView) {

        m_detailView = Boolean.parseBoolean(detailView);
    }

    /**
     * Sets the editable by tag attribute.<p>
     *
     * @param editableBy the editable by tag attribute to set
     */
    public void setEditableby(String editableBy) {

        m_editableBy = editableBy;
    }

    /**
     * Sets the maxElements attribute value.<p>
     *
     * @param maxElements the maxElements value to set
     */
    public void setMaxElements(String maxElements) {

        m_maxElements = maxElements;
    }

    /**
     * Sets the name attribute value.<p>
     *
     * @param name the name value to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the name prefix.<p>
     *
     * @param namePrefix the name prefix to set
     */
    public void setNameprefix(String namePrefix) {

        m_namePrefix = namePrefix;
    }

    /**
     * Sets the container parameter.<p>
     *
     * This is useful for a dynamically generated nested container,
     * to pass information to the formatter used inside that container.
     *
     * @param param the parameter String to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Sets the setting presets.<p>
     *
     * @param presets a map with string keys and values, or null
     */
    @SuppressWarnings("unchecked")
    public void setSettings(Object presets) {

        if (presets == null) {
            m_settingPresets = null;
        } else if (!(presets instanceof Map)) {
            throw new IllegalArgumentException(
                "cms:container -- value of 'settings' attribute  should be a map, but is "
                    + ClassUtils.getCanonicalName(presets));
        } else {
            m_settingPresets = new HashMap<>((Map<String, String>)presets);
        }
    }

    /**
     * Sets the tag attribute.<p>
     *
     * @param tag the createTag to set
     */
    public void setTag(String tag) {

        m_tag = tag;
    }

    /**
     * Sets the tag class attribute.<p>
     *
     * @param tagClass the tag class attribute to set
     */
    public void setTagClass(String tagClass) {

        m_tagClass = tagClass;
    }

    /**
     * Sets the type attribute value.<p>
     *
     * @param type the type value to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the container width as a string.<p>
     *
     * @param width the container width as a string
     */
    public void setWidth(String width) {

        m_width = width;
    }

    /**
     * Returns the serialized data of the given container.<p>
     *
     * @param cms the cms context
     * @param maxElements the maximum number of elements allowed within this container
     * @param isDetailView <code>true</code> if this container is currently being used for the detail view
     * @param isDetailOnly <code>true</code> if this is a detail only container
     *
     * @return the serialized container data
     */
    protected String getContainerData(CmsObject cms, int maxElements, boolean isDetailView, boolean isDetailOnly) {

        int width = -1;
        try {
            if (getWidth() != null) {
                width = Integer.parseInt(getWidth());
            }
        } catch (NumberFormatException e) {
            //ignore; set width to -1
            LOG.debug("Error parsing container width.", e);
        }
        CmsContainer cont = new CmsContainer(
            getName(),
            getType(),
            m_bodyContent,
            width,
            maxElements,
            m_detailView,
            isDetailView,
            !m_hasModelGroupAncestor && isEditable(cms),
            null,
            m_parentContainer != null ? m_parentContainer.getName() : null,
            m_parentElement != null ? m_parentElement.getInstanceId() : null,
            m_settingPresets);
        cont.setDetailOnly(isDetailOnly);
        String result = "";
        try {
            result = CmsContainerpageService.getSerializedContainerInfo(cont);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    /**
     * Returns if the container is editable by the current user.<p>
     *
     * @param cms the cms context
     *
     * @return <code>true</code> if the container is editable by the current user
     */
    protected boolean isEditable(CmsObject cms) {

        boolean result = false;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_editableBy)) {
            String[] principals = m_editableBy.split(",");
            List<CmsGroup> groups = null;
            for (int i = 0; i < principals.length; i++) {
                String key = principals[i];
                // get the principal name from the principal String
                String principal = key.substring(key.indexOf('.') + 1, key.length());

                if (CmsGroup.hasPrefix(key)) {
                    // read the group
                    principal = OpenCms.getImportExportManager().translateGroup(principal);
                    try {
                        CmsGroup group = cms.readGroup(principal);
                        if (groups == null) {
                            try {
                                groups = cms.getGroupsOfUser(cms.getRequestContext().getCurrentUser().getName(), false);
                            } catch (Exception ex) {
                                if (LOG.isErrorEnabled()) {
                                    LOG.error(ex.getLocalizedMessage(), ex);
                                }
                                groups = Collections.emptyList();
                            }
                        }
                        result = groups.contains(group);
                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else if (CmsUser.hasPrefix(key)) {
                    // read the user
                    principal = OpenCms.getImportExportManager().translateUser(principal);
                    try {
                        result = cms.getRequestContext().getCurrentUser().equals(cms.readUser(principal));
                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else if (CmsRole.hasPrefix(key)) {
                    // read the role with role name
                    CmsRole role = CmsRole.valueOfRoleName(principal);
                    if (role == null) {
                        // try to read the role in the old fashion with group name
                        role = CmsRole.valueOfGroupName(principal);
                    }
                    if (role != null) {
                        result = OpenCms.getRoleManager().hasRole(
                            cms,
                            role.forOrgUnit(cms.getRequestContext().getCurrentUser().getOuFqn()));
                    }
                }
                if (result) {
                    break;
                }
            }
        } else {
            result = OpenCms.getRoleManager().hasRole(cms, CmsRole.ELEMENT_AUTHOR);
        }
        return result;
    }

    /**
     * Returns true if this is a nested container.<p>
     *
     * @return true if this is a nested container
     */
    protected boolean isNested() {

        return (m_parentContainer != null) && (m_parentElement != null);
    }

    /**
     * Prints the closing tag for an element wrapper if in online mode.<p>
     *
     * @param isGroupcontainer <code>true</code> if element is a group-container
     *
     * @throws IOException if the output fails
     */
    protected void printElementWrapperTagEnd(boolean isGroupcontainer) throws IOException {

        if (m_editableRequest) {
            String result;
            if (isGroupcontainer) {
                result = "</div>";
            } else {
                result = "<div class=\""
                    + CmsContainerElement.CLASS_CONTAINER_ELEMENT_END_MARKER
                    + "\" style=\"display:none\"></div>";
            }
            pageContext.getOut().print(result);
        }
    }

    /**
     * Prints the opening element wrapper tag for the container page editor if we are in Offline mode.<p>
     *
     * @param cms the Cms context
     * @param elementBean the element bean
     * @param page the container page
     * @param isGroupContainer true if the element is a group-container
     *
     * @throws Exception if something goes wrong
     */
    protected void printElementWrapperTagStart(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        CmsContainerPageBean page,
        boolean isGroupContainer)
    throws Exception {

        if (m_editableRequest) {
            StringBuffer result = new StringBuffer("<div class='");
            if (isGroupContainer) {
                result.append(CmsContainerElement.CLASS_GROUP_CONTAINER_ELEMENT_MARKER);
            } else {
                result.append(CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER);
            }
            String serializedElement = getElementInfo(cms, elementBean, page);
            result.append("'");
            result.append(" " + CmsGwtConstants.ATTR_DATA_ELEMENT + "='").append(serializedElement);
            if (isGroupContainer) {
                result.append("'>");
            } else {
                result.append("' style='display:none;'></div>");
            }
            pageContext.getOut().print(result);
        }
    }

    /**
     * Generates the detail view element.<p>
     *
     * @param request the current request
     * @param cms the CMS context
     * @param detailContent the detail content resource
     * @param container the container
     *
     * @return the detail view element
     */
    private CmsContainerElementBean generateDetailViewElement(
        ServletRequest request,
        CmsObject cms,
        CmsResource detailContent,
        CmsContainerBean container) {

        CmsContainerElementBean element = null;
        if (detailContent != null) {
            // get the right formatter

            CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                cms,
                cms.getRequestContext().getRootUri());
            CmsFormatterConfiguration formatters = config.getFormatters(cms, detailContent);
            I_CmsFormatterBean formatter = formatters.getDetailFormatter(getType(), getContainerWidth());

            if (formatter != null) {
                // use structure id as the instance id to enable use of nested containers
                Map<String, String> settings = new HashMap<String, String>();
                for (CmsContainerElementBean el : container.getElements()) {
                    try {
                        el.initResource(cms);
                        if (el.getResource().getTypeId() == detailContent.getTypeId()) {
                            settings.putAll(el.getIndividualSettings());
                            break;
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

                String formatterKey = CmsFormatterConfig.getSettingsKeyForContainer(container.getName());
                if (settings.containsKey(formatterKey)) {
                    String formatterConfigId = settings.get(formatterKey);
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(formatterConfigId);
                    if (dynamicFmt != null) {
                        formatter = dynamicFmt;
                    }
                }
                settings.put(formatterKey, formatter.getKeyOrId());
                settings.put(CmsContainerElement.ELEMENT_INSTANCE_ID, new CmsUUID().toString());
                // create element bean
                element = new CmsContainerElementBean(
                    detailContent.getStructureId(),
                    formatter.getJspStructureId(),
                    settings,
                    false);
                String pageRootPath = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
                element = CmsTemplateMapper.get(request).transformDetailElement(cms, element, pageRootPath);
            }
        }
        return element;
    }

    /**
     * Gets the container width as a number.<p>
     *
     * If the container width is not set, or not a number, -1 will be returned.<p>
     *
     * @return the container width or -1
     */
    private int getContainerWidth() {

        int containerWidth = -1;
        try {
            containerWidth = Integer.parseInt(m_width);
        } catch (NumberFormatException e) {
            // do nothing, set width to -1
            LOG.debug("Error parsing container width.", e);
        }
        return containerWidth;
    }

    /**
     * Returns the detail function element.<p>
     *
     * @param cms the cms context
     * @param detailFunctionPage the detail function page
     * @param req the current request
     *
     * @return the detail function element, if available
     */
    private CmsContainerElementBean getDetailFunctionElement(
        CmsObject cms,
        CmsResource detailFunctionPage,
        ServletRequest req) {

        try {
            CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, detailFunctionPage, req);

            CmsContainerPageBean page = xmlContainerPage.getContainerPage(cms);
            CmsContainerBean container = page.getContainers().get(DETAIL_FUNCTION_CONTAINER_NAME);
            if (container == null) {
                for (Entry<String, CmsContainerBean> entry : page.getContainers().entrySet()) {
                    if (entry.getKey().endsWith("-" + DETAIL_FUNCTION_CONTAINER_NAME)) {
                        container = entry.getValue();
                        break;
                    }
                }
            }
            if (container != null) {
                return container.getElements().get(0);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Returns the serialized element data.<p>
     *
     * @param cms the current cms context
     * @param elementBean the element to serialize
     * @param page the container page
     *
     * @return the serialized element data
     *
     * @throws Exception if something goes wrong
     */
    private String getElementInfo(CmsObject cms, CmsContainerElementBean elementBean, CmsContainerPageBean page)
    throws Exception {

        return CmsContainerpageService.getSerializedElementInfo(
            cms,
            (HttpServletRequest)pageContext.getRequest(),
            (HttpServletResponse)pageContext.getResponse(),
            elementBean,
            page);
    }

    /**
     * Parses the maximum element number from the current container and returns the resulting number.<p>
     *
     * @param requestUri the requested URI
     *
     * @return the maximum number of elements of the container
     */
    private int getMaxElements(String requestUri) {

        String containerMaxElements = getMaxElements();

        int maxElements = -1;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(containerMaxElements)) {
            try {
                maxElements = Integer.parseInt(containerMaxElements);
            } catch (NumberFormatException e) {
                throw new CmsIllegalStateException(
                    Messages.get().container(
                        Messages.LOG_WRONG_CONTAINER_MAXELEMENTS_3,
                        new Object[] {requestUri, getName(), containerMaxElements}),
                    e);
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_MAXELEMENTS_NOT_SET_2,
                        new Object[] {getName(), requestUri}));
            }
        }
        return maxElements;
    }

    /**
     * Returns the ADE session cache for container elements in case of an editable request, otherwise <code>null</code>.<p>
     *
     * @param cms the cms context
     *
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache(CmsObject cms) {

        return m_editableRequest
        ? CmsADESessionCache.getCache((HttpServletRequest)(pageContext.getRequest()), cms)
        : null;
    }

    /**
     * Evaluates if this container is nested within a model group.<p>
     *
     * @param standardContext the standard context
     *
     * @return <code>true</code> if the container has model group ancestors
     */
    private boolean hasModelGroupAncestor(CmsJspStandardContextBean standardContext) {

        boolean result = false;
        if (!standardContext.isModelGroupPage()) {
            CmsContainerElementWrapper parent = standardContext.getElement();
            while ((parent != null) && !result) {
                result = parent.isModelGroup();
                parent = parent.getParent();
            }
        }
        return result;
    }

    /**
     * Prints an element error tag to the response out.<p>
     *
     * @param elementSitePath the element site path
     * @param formatterSitePath the formatter site path
     * @param exception the exception causing the error
     *
     * @throws IOException if something goes wrong writing to response out
     */
    private void printElementErrorTag(String elementSitePath, String formatterSitePath, Exception exception)
    throws IOException {

        if (m_editableRequest) {
            String stacktrace = CmsException.getStackTraceAsString(exception);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(stacktrace)) {
                stacktrace = null;
            } else {
                // stacktrace = CmsStringUtil.escapeJavaScript(stacktrace);
                stacktrace = CmsEncoder.escapeXml(stacktrace);
            }
            StringBuffer errorBox = new StringBuffer(256);
            errorBox.append(
                "<div style=\"display:block; padding: 5px; border: red solid 2px; color: black; background: white;\" class=\"");
            errorBox.append(CmsContainerElement.CLASS_ELEMENT_ERROR);
            errorBox.append("\">");
            errorBox.append(
                Messages.get().getBundle().key(
                    Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                    elementSitePath,
                    formatterSitePath));
            errorBox.append("<br />");
            errorBox.append(exception.getLocalizedMessage());
            if (stacktrace != null) {
                errorBox.append(
                    "<span onclick=\"opencms.openStacktraceDialog(event);\" style=\"border: 1px solid black; cursor: pointer;\">");
                errorBox.append(Messages.get().getBundle().key(Messages.GUI_LABEL_STACKTRACE_0));
                String title = Messages.get().getBundle().key(
                    Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                    elementSitePath,
                    formatterSitePath);
                errorBox.append("<span title=\"");
                errorBox.append(CmsEncoder.escapeXml(title));
                errorBox.append("\" class=\"hiddenStacktrace\" style=\"display:none;\">");
                errorBox.append(stacktrace);
                errorBox.append("</span></span>");
            }
            errorBox.append("</div>");
            pageContext.getOut().print(errorBox.toString());
        }
    }

    /**
     * Renders a container element.<p>
     *
     * @param request the current request
     * @param cms the CMS context
     * @param standardContext the current standard contxt bean
     * @param element the container element to render
     * @param locale the requested locale
     * @param alreadyFull if true, only render invisible elements (they don't count towards the "max elements")
     *
     * @return true if an element was rendered that counts towards the container's maximum number of elements
     *
     * @throws Exception if something goes wrong
     */
    private boolean renderContainerElement(
        HttpServletRequest request,
        CmsObject cms,
        CmsJspStandardContextBean standardContext,
        CmsContainerElementBean element,
        Locale locale,
        boolean alreadyFull)
    throws Exception {

        CmsTemplateContext context = (CmsTemplateContext)(request.getAttribute(
            CmsTemplateContextManager.ATTR_TEMPLATE_CONTEXT));
        if ((context == null) && alreadyFull) {
            return false;
        }
        String contextKey = null;
        if (context != null) {
            contextKey = context.getKey();
        } else {
            String rpcContextOverride = (String)request.getAttribute(
                CmsTemplateContextManager.ATTR_RPC_CONTEXT_OVERRIDE);
            contextKey = rpcContextOverride;
        }
        boolean ignoreTemplateContexts = false;
        try {
            I_CmsTemplateContextProvider templateProvider = null;
            if (context != null) {
                templateProvider = context.getProvider();
            }
            if (templateProvider == null) {
                templateProvider = OpenCms.getTemplateContextManager().getTemplateContextProvider(
                    cms,
                    cms.getRequestContext().getUri());
            }
            ignoreTemplateContexts = (templateProvider != null) && templateProvider.isIgnoreTemplateContextsSetting();
        } catch (CmsException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
        boolean showInContext = ignoreTemplateContexts
            || shouldShowInContext(element, context != null ? context.getKey() : null);
        boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
        if (!m_editableRequest && !showInContext) {
            return false;
        }
        try {
            element.initResource(cms);
        } catch (CmsPermissionViolationException e) {
            LOG.info(e.getLocalizedMessage(), e);
            return false;
        }
        if (!m_editableRequest && !element.isReleasedAndNotExpired()) {
            // do not render expired resources for the online project
            return false;
        }
        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        String containerType = getType();
        int containerWidth = getContainerWidth();
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfigurationWithCache(
            cms,
            cms.getRequestContext().getRootUri());
        boolean isGroupContainer = element.isGroupContainer(cms);
        boolean isInheritedContainer = element.isInheritedContainer(cms);
        I_CmsFormatterBean formatterConfig = null;
        if (!isGroupContainer && !isInheritedContainer) {
            // ensure that the formatter configuration id is added to the element settings, so it will be persisted on save
            formatterConfig = ensureValidFormatterSettings(
                cms,
                element,
                adeConfig,
                getName(),
                containerType,
                containerWidth);
            element.initSettings(cms, adeConfig, formatterConfig, locale, request, m_settingPresets);
        }
        // writing elements to the session cache to improve performance of the container-page editor in offline project
        if (m_editableRequest) {
            getSessionCache(cms).setCacheContainerElement(element.editorHash(), element);
        }

        if (isGroupContainer || isInheritedContainer) {
            if (alreadyFull) {
                return false;
            }
            List<CmsContainerElementBean> subElements;
            if (isGroupContainer) {
                subElements = getGroupContainerElements(cms, element, req, containerType);
            } else {
                // inherited container case
                subElements = getInheritedContainerElements(cms, element);
            }
            // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
            printElementWrapperTagStart(cms, element, standardContext.getPage(), true);
            for (CmsContainerElementBean subelement : subElements) {

                try {
                    subelement.initResource(cms);
                    boolean shouldShowSubElementInContext = ignoreTemplateContexts
                        || shouldShowInContext(subelement, contextKey);
                    if (!m_editableRequest
                        && (!shouldShowSubElementInContext || !subelement.isReleasedAndNotExpired())) {
                        continue;
                    }
                    I_CmsFormatterBean subElementFormatterConfig = ensureValidFormatterSettings(
                        cms,
                        subelement,
                        adeConfig,
                        getName(),
                        containerType,
                        containerWidth);
                    subelement.initSettings(
                        cms,
                        adeConfig,
                        subElementFormatterConfig,
                        locale,
                        request,
                        m_settingPresets);
                    // writing elements to the session cache to improve performance of the container-page editor
                    if (m_editableRequest) {
                        getSessionCache(cms).setCacheContainerElement(subelement.editorHash(), subelement);
                    }
                    if (subElementFormatterConfig == null) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                new CmsIllegalStateException(
                                    Messages.get().container(
                                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                                        subelement.getSitePath(),
                                        OpenCms.getResourceManager().getResourceType(
                                            subelement.getResource()).getTypeName(),
                                        containerType)));
                        }
                        // skip this element, it has no formatter for this container type defined
                        continue;
                    }
                    // execute the formatter JSP for the given element URI
                    // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
                    printElementWrapperTagStart(cms, subelement, standardContext.getPage(), false);
                    standardContext.setElement(subelement);
                    try {
                        String formatterSitePath;
                        try {
                            CmsResource formatterResource = cms.readResource(
                                subElementFormatterConfig.getJspStructureId());
                            formatterSitePath = cms.getSitePath(formatterResource);
                        } catch (CmsVfsResourceNotFoundException ex) {
                            LOG.debug("Formatter JSP not found by id, try using path.", ex);
                            formatterSitePath = cms.getRequestContext().removeSiteRoot(
                                subElementFormatterConfig.getJspRootPath());
                        }
                        if (shouldShowSubElementInContext) {
                            CmsJspTagInclude.includeTagAction(
                                pageContext,
                                formatterSitePath,
                                null,
                                locale,
                                false,
                                isOnline && m_cacheable,
                                null,
                                CmsRequestUtil.getAttributeMap(req),
                                req,
                                res);
                        } else {
                            pageContext.getOut().print(DUMMY_ELEMENT);
                        }
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            if (CmsJspLoader.isJasperCompilerException(e)) {
                                LOG.error(
                                    Messages.get().getBundle().key(
                                        Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                        subelement.getSitePath(),
                                        subElementFormatterConfig) + "\n" + e.getMessage());
                                LOG.debug("Full stack trace for error", e);
                            } else {
                                LOG.error(
                                    Messages.get().getBundle().key(
                                        Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                        subelement.getSitePath(),
                                        subElementFormatterConfig),
                                    e);
                            }
                        }
                        printElementErrorTag(subelement.getSitePath(), subElementFormatterConfig.getJspRootPath(), e);
                    }
                    printElementWrapperTagEnd(false);
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            printElementWrapperTagEnd(true);
            return true;
        } else {
            boolean result = true;
            if (alreadyFull) {
                result = false;
                if (!showInContext) {
                    printElementWrapperTagStart(cms, element, standardContext.getPage(), false);
                    pageContext.getOut().print(DUMMY_ELEMENT);
                    printElementWrapperTagEnd(false);
                }
            } else {
                String formatter = null;
                try {
                    if (formatterConfig != null) {
                        try {
                            CmsResource formatterResource = cms.readResource(formatterConfig.getJspStructureId());
                            formatter = cms.getSitePath(formatterResource);
                        } catch (CmsVfsResourceNotFoundException ex) {
                            LOG.debug("Formatter JSP not found by id, try using path.", ex);
                            if (cms.existsResource(
                                cms.getRequestContext().removeSiteRoot(formatterConfig.getJspRootPath()))) {
                                formatter = cms.getRequestContext().removeSiteRoot(formatterConfig.getJspRootPath());
                            }
                        }
                    } else {
                        formatter = cms.getSitePath(cms.readResource(element.getFormatterId()));
                    }
                } catch (CmsException e) {
                    LOG.debug("Formatter resource can not be found, try reading it from the configuration.", e);
                    // the formatter resource can not be found, try reading it form the configuration
                    CmsFormatterConfiguration elementFormatters = adeConfig.getFormatters(cms, element.getResource());
                    I_CmsFormatterBean elementFormatterBean = elementFormatters.getDefaultFormatter(
                        containerType,
                        containerWidth);
                    if (elementFormatterBean == null) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                new CmsIllegalStateException(
                                    Messages.get().container(
                                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                                        element.getSitePath(),
                                        OpenCms.getResourceManager().getResourceType(
                                            element.getResource()).getTypeName(),
                                        containerType)));
                        }
                        // skip this element, it has no formatter for this container type defined
                        return false;
                    }
                    try {
                        CmsResource formatterResource = cms.readResource(elementFormatterBean.getJspStructureId());
                        formatter = cms.getSitePath(formatterResource);
                    } catch (CmsVfsResourceNotFoundException ex) {
                        LOG.debug("Formatter JSP not found by id, try using path.", ex);
                        formatter = cms.getRequestContext().removeSiteRoot(elementFormatterBean.getJspRootPath());
                    }
                }

                printElementWrapperTagStart(cms, element, standardContext.getPage(), false);
                standardContext.setElement(element);
                try {
                    if (!showInContext) {
                        // write invisible dummy element
                        pageContext.getOut().print(DUMMY_ELEMENT);
                        result = false;
                    } else {
                        // execute the formatter jsp for the given element uri
                        CmsJspTagInclude.includeTagAction(
                            pageContext,
                            formatter,
                            null,
                            locale,
                            false,
                            isOnline && m_cacheable,
                            null,
                            CmsRequestUtil.getAtrributeMap(req),
                            req,
                            res);
                    }
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        if (CmsJspLoader.isJasperCompilerException(e)) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                    element.getSitePath(),
                                    formatter) + "\n" + e.getMessage());
                            LOG.debug("Full stack trace for error", e);
                        } else {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                    element.getSitePath(),
                                    formatter),
                                e);
                        }
                    }
                    printElementErrorTag(element.getSitePath(), formatter, e);
                }
                printElementWrapperTagEnd(false);
            }
            return result;
        }
    }

    /**
     * Resets the tag instance and standard context state.<p>
     */
    private void resetState() {

        // clear all members so the tag object may be reused
        m_type = null;
        m_name = null;
        m_param = null;
        m_maxElements = null;
        m_tag = null;
        m_tagClass = null;
        m_detailView = false;
        m_detailOnly = false;
        m_width = null;
        m_editableBy = null;
        m_bodyContent = null;
        m_hasModelGroupAncestor = false;
        // reset the current element
        CmsJspStandardContextBean cmsContext = CmsJspStandardContextBean.getInstance(pageContext.getRequest());
        cmsContext.setElement(m_parentElement);
        cmsContext.setContainer(m_parentContainer);
        m_parentElement = null;
        m_parentContainer = null;
    }

    /**
     * Helper method to determine whether an element should be shown in a context.<p>
     *
     * @param element the element for which the visibility should be determined
     * @param contextKey the key of the context for which to check
     *
     * @return true if the current context doesn't prohibit the element from being shown
     */
    private boolean shouldShowInContext(CmsContainerElementBean element, String contextKey) {

        if (contextKey == null) {
            return true;
        }

        try {
            if ((element.getResource() != null)
                && !OpenCms.getTemplateContextManager().shouldShowType(
                    contextKey,
                    OpenCms.getResourceManager().getResourceType(element.getResource().getTypeId()).getTypeName())) {
                return false;
            }
        } catch (CmsLoaderException e) {
            // ignore and log
            LOG.error(e.getLocalizedMessage(), e);
        }
        Map<String, String> settings = element.getSettings();
        if (settings == null) {
            return true;
        }
        String contextsAllowed = settings.get(CmsTemplateContextInfo.SETTING);
        if (contextsAllowed == null) {
            return true;
        }
        if (contextsAllowed.equals(CmsTemplateContextInfo.EMPTY_VALUE)) {
            return false;
        }

        List<String> contextsAllowedList = CmsStringUtil.splitAsList(contextsAllowed, "|");
        if (!contextsAllowedList.contains(contextKey)) {
            return false;
        }
        return true;
    }
}
