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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsTemplateContext;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the page container elements.<p>
 * 
 * @since 8.0
 */
public class CmsJspTagContainer extends TagSupport {

    /** Json property name constants for containers. */
    public enum JsonContainer {

        /** The list of elements. */
        elements,
        /** Flag for the detail view container. */
        isDetailView,
        /** The max allowed number of elements in the container. */
        maxElem,
        /** The container name. */
        name,
        /** The container type. */
        type,
        /** The container width. */
        width;
    }

    /** Default number of max elements in the container in case no value has been set. */
    public static final String DEFAULT_MAX_ELEMENTS = "100";

    /** The detail containers folder name. */
    public static final String DETAIL_CONTAINERS_FOLDER_NAME = ".detailContainers";

    /** HTML used for invisible dummy elements. */
    public static final String DUMMY_ELEMENT = "<div class='"
        + CmsTemplateContextInfo.DUMMY_ELEMENT_MARKER
        + "' style='display: none !important;'></div>";

    /** Key used to write container data into the javascript window object. */
    public static final String KEY_CONTAINER_DATA = "org_opencms_ade_containerpage_containers";

    /** The create no tag attribute value constant. */
    private static final String CREATE_NO_TAG = "none";

    /** The default tag name constant. */
    private static final String DEFAULT_TAG_NAME = "div";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContainer.class);

    /** Serial version UID required for safe serialisation. */
    private static final long serialVersionUID = -1228397990961282556L;

    /** States if this container should only be displayed on detail pages. */
    private boolean m_detailOnly;

    /** The detail-view attribute value. */
    private boolean m_detailView;

    /** The maxElements attribute value. */
    private String m_maxElements;

    /** The name attribute value. */
    private String m_name;

    /** The tag attribute value. */
    private String m_tag;

    /** The class attribute value. */
    private String m_tagClass;

    /** The type attribute value. */
    private String m_type;

    /** The container width as a string. */
    private String m_width;

    /**
     * Ensures the appropriate formatter configuration  ID is set in the element settings.<p>
     * 
     * @param cms the cms context
     * @param element the element bean
     * @param adeConfig the ADE configuration data
     * @param containerName
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

        I_CmsFormatterBean formatterBean = null;
        String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(containerName);
        if (element.getFormatterId() != null) {

            if (!element.getSettings().containsKey(settingsKey)) {
                for (I_CmsFormatterBean formatter : adeConfig.getFormatters(cms, element.getResource()).getAllMatchingFormatters(
                    containerType,
                    containerWidth)) {
                    if (element.getFormatterId().equals(formatter.getJspStructureId())) {
                        String formatterConfigId = formatter.getId();
                        if (formatterConfigId == null) {
                            formatterConfigId = CmsFormatterConfig.SCHEMA_FORMATTER_ID;
                        }
                        element.getSettings().put(
                            CmsFormatterConfig.getSettingsKeyForContainer(containerName),
                            formatterConfigId);
                        formatterBean = formatter;
                        break;
                    }
                }
            } else {
                String formatterConfigId = element.getSettings().get(settingsKey);
                if (CmsUUID.isValidUUID(formatterConfigId)) {
                    formatterBean = OpenCms.getADEManager().getCachedFormatters(
                        cms.getRequestContext().getCurrentProject().isOnlineProject()).getFormatters().get(
                        formatterConfigId);
                }
            }
        } else {
            formatterBean = adeConfig.getFormatters(cms, element.getResource()).getDefaultFormatter(
                containerType,
                containerWidth);
            if (formatterBean != null) {
                String formatterConfigId = formatterBean.getId();
                if (formatterConfigId == null) {
                    formatterConfigId = CmsFormatterConfig.SCHEMA_FORMATTER_ID;
                }
                element.getSettings().put(settingsKey, formatterConfigId);
                element.setFormatterId(formatterBean.getJspStructureId());
            } else {
                element.setFormatterId(null);
            }
        }
        return formatterBean;
    }

    /**
     * Returns the site path to the detail only container page.<p>
     * 
     * @param detailContentSitePath the detail content site path
     * 
     * @return the site path to the detail only container page
     */
    public static String getDetailOnlyPageName(String detailContentSitePath) {

        String result = CmsResource.getFolderPath(detailContentSitePath);
        result = CmsStringUtil.joinPaths(
            result,
            DETAIL_CONTAINERS_FOLDER_NAME,
            CmsResource.getName(detailContentSitePath));
        return result;
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
        String containerType) throws CmsException {

        List<CmsContainerElementBean> subElements;
        CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(cms, element.getResource(), req);
        CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(
            cms,
            cms.getRequestContext().getLocale());
        if (!groupContainer.getTypes().contains(containerType)) {
            LOG.warn(new CmsIllegalStateException(Messages.get().container(
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
     * Creates a new data tag for the given container.<p>
     * 
     * @param container the container to get the data tag for
     * @param widthStr the width of the container as a string 
     * @param isDetailView <code>true</code> if this container is currently being used for the detail view
     * @param isDetailOnly <code>true</code> if this container is displayed in detail view only
     * 
     * @return html data tag for the given container
     *
     * @throws JSONException if there is a problem with JSON manipulation
     */
    protected static String getContainerDataTag(
        CmsContainerBean container,
        String widthStr,
        boolean isDetailView,
        boolean isDetailOnly) throws JSONException {

        // add container data for the editor
        JSONObject jsonContainer = new JSONObject();
        jsonContainer.put(CmsCntPageData.JSONKEY_NAME, container.getName());
        jsonContainer.put(CmsCntPageData.JSONKEY_TYPE, container.getType());
        jsonContainer.put(CmsCntPageData.JSONKEY_MAXELEMENTS, container.getMaxElements());
        jsonContainer.put(CmsCntPageData.JSONKEY_DETAILVIEW, isDetailView);
        jsonContainer.put(CmsCntPageData.JSONKEY_DETAILONLY, isDetailOnly);
        int width = -1;
        try {
            if (widthStr != null) {
                width = Integer.parseInt(widthStr);
            }
        } catch (NumberFormatException e) {
            //ignore; set width to -1
        }
        jsonContainer.put(CmsCntPageData.JSONKEY_WIDTH, width);

        JSONArray jsonElements = new JSONArray();
        for (CmsContainerElementBean element : container.getElements()) {
            jsonElements.put(element.editorHash());
        }
        jsonContainer.put(CmsCntPageData.JSONKEY_ELEMENTS, jsonElements);
        // the container meta data is added to the javascript window object by the following tag, used within the container-page editor 
        return new StringBuffer("<script type=\"text/javascript\">if (").append(KEY_CONTAINER_DATA).append("!=null) {").append(
            KEY_CONTAINER_DATA).append(".push(").append(jsonContainer.toString()).append("); } </script>").toString();
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
     * 
     * @return the opening tag
     */
    protected static String getTagOpen(String tagName, String containerName, String tagClass) {

        String classAttr = CmsStringUtil.isEmptyOrWhitespaceOnly(tagClass) ? "" : "class=\"" + tagClass + "\" ";
        return "<" + tagName + " id=\"" + containerName + "\" " + classAttr + ">";
    }

    /**
     * Internal action method.<p>
     * 
     * @return SKIP_BODY
     * @throws JspException in case something goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                CmsFlexController controller = CmsFlexController.getController(req);
                CmsObject cms = controller.getCmsObject();
                String requestUri = cms.getRequestContext().getUri();
                Locale locale = cms.getRequestContext().getLocale();
                CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
                CmsContainerPageBean containerPage = standardContext.getPage();
                if (containerPage == null) {
                    // get the container page itself, checking the history first
                    CmsResource pageResource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
                    if (pageResource == null) {
                        pageResource = cms.readResource(requestUri);
                    }
                    CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, pageResource, req);
                    containerPage = xmlContainerPage.getContainerPage(cms, locale);
                    standardContext.setPage(containerPage);
                }
                CmsResource detailContent = standardContext.getDetailContent();
                // get the container
                CmsContainerBean container = null;
                if (m_detailOnly) {
                    if (detailContent == null) {
                        // this is no detail page, so the detail only container will not be rendered at all
                        return SKIP_BODY;
                    } else {
                        CmsContainerPageBean detailOnlyPage = standardContext.getDetailOnlyPage();
                        if (detailOnlyPage == null) {
                            String resourceName = getDetailOnlyPageName(cms.getSitePath(detailContent));
                            if (cms.existsResource(resourceName)) {
                                CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(
                                    cms,
                                    cms.readResource(resourceName),
                                    req);
                                detailOnlyPage = xmlContainerPage.getContainerPage(cms, locale);
                                standardContext.setDetailOnlyPage(detailOnlyPage);
                                container = detailOnlyPage.getContainers().get(m_name);
                            }
                        } else {
                            container = detailOnlyPage.getContainers().get(m_name);
                        }
                    }
                } else if (containerPage != null) {
                    container = containerPage.getContainers().get(getName());
                }
                // create tag for container if necessary
                boolean createTag = false;
                String tagName = CmsStringUtil.isEmptyOrWhitespaceOnly(getTag()) ? DEFAULT_TAG_NAME : getTag();
                if (!CREATE_NO_TAG.equals(getTag())) {
                    createTag = true;
                    pageContext.getOut().print(getTagOpen(tagName, getName(), getTagClass()));
                }

                // get the maximal number of elements
                int maxElements = getMaxElements(requestUri);

                boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
                boolean isUsedAsDetailView = false;
                if (m_detailView && (detailContent != null)) {
                    isUsedAsDetailView = true;
                }
                if (container == null) {
                    if (!isUsedAsDetailView) {
                        // container not found
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_CONTAINER_NOT_FOUND_3,
                                requestUri,
                                locale,
                                getName()));
                        }
                        if (!isOnline) {
                            // add container data for the editor
                            try {
                                pageContext.getOut().print(
                                    getContainerDataTag(
                                        new CmsContainerBean(getName(), getType(), maxElements, null),
                                        getWidth(),
                                        isUsedAsDetailView,
                                        m_detailOnly));
                            } catch (JSONException e) {
                                // should never happen
                                throw new JspException(e);
                            }
                        }

                        // close tag for the empty container
                        if (createTag) {
                            pageContext.getOut().print(getTagClose(tagName));
                        }
                    } else {
                        container = new CmsContainerBean(
                            getName(),
                            getType(),
                            maxElements,
                            Collections.<CmsContainerElementBean> emptyList());
                    }
                }
                if (container != null) {
                    standardContext.setContainer(container);
                    // validate the type
                    if (!getType().equals(container.getType())) {
                        container.setType(getType());
                        LOG.warn(new CmsIllegalStateException(Messages.get().container(
                            Messages.LOG_WRONG_CONTAINER_TYPE_4,
                            new Object[] {requestUri, locale, getName(), getType()})));
                    }

                    // update the cache
                    container.setMaxElements(maxElements);
                    container.setWidth(getWidth());
                    List<CmsContainerElementBean> allElements = new ArrayList<CmsContainerElementBean>();
                    CmsContainerElementBean detailElement = null;
                    if (isUsedAsDetailView) {
                        detailElement = generateDetailViewElement(cms, detailContent);
                    }
                    if (detailElement != null) {
                        allElements.add(detailElement);
                    } else {
                        allElements.addAll(container.getElements());
                    }
                    if (!isOnline) {
                        // add container data for the editor
                        try {
                            CmsContainerBean cntBean = new CmsContainerBean(
                                getName(),
                                getType(),
                                maxElements,
                                allElements);
                            pageContext.getOut().print(
                                getContainerDataTag(cntBean, getWidth(), isUsedAsDetailView, m_detailOnly));
                        } catch (JSONException e) {
                            // should never happen
                            throw new JspException(e);
                        }
                    }
                    // iterate over elements to render
                    int numRenderedElements = 0;
                    for (CmsContainerElementBean elementBean : allElements) {
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
                    // close tag for container
                    if (createTag) {
                        pageContext.getOut().print(getTagClose(tagName));
                    }
                }
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "container"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
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
     * Returns the maxElements attribute value.<p>
     * 
     * @return the maxElements attribute value
     */
    public String getMaxElements() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_maxElements) ? DEFAULT_MAX_ELEMENTS : m_maxElements;
    }

    /**
     * Returns the name attribute value.<p>
     * 
     * @return String the name attribute value
     */
    public String getName() {

        return m_name;
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
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_type = null;
        m_name = null;
        m_maxElements = null;
        m_tag = null;
        m_tagClass = null;
        m_detailView = false;
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
     * Prints the closing tag for an element wrapper if in online mode.<p>
     * 
     * @param isOnline if true, we are online 
     * @param isGroupcontainer <code>true</code> if element is a group-container
     * 
     * @throws IOException if the output fails 
     */
    protected void printElementWrapperTagEnd(boolean isOnline, boolean isGroupcontainer) throws IOException {

        if (!isOnline) {
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
     * @param isOnline true if we are in Online mode 
     * @param cms the Cms context 
     * @param elementBean the element bean 
     * @param isGroupContainer true if the element is a group-container 
     * 
     * @throws Exception if something goes wrong
     */
    protected void printElementWrapperTagStart(
        boolean isOnline,
        CmsObject cms,
        CmsContainerElementBean elementBean,
        boolean isGroupContainer) throws Exception {

        if (!isOnline) {
            StringBuffer result = new StringBuffer("<div class='");
            if (isGroupContainer) {
                result.append(CmsContainerElement.CLASS_GROUP_CONTAINER_ELEMENT_MARKER);
            } else {
                result.append(CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER);
            }
            String serializedElement = getElementInfo(cms, elementBean);
            result.append("'");
            result.append(" rel='").append(serializedElement);
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
     * @param cms the CMS context
     * @param detailContent the detail content resource
     * 
     * @return the detail view element 
     */
    private CmsContainerElementBean generateDetailViewElement(CmsObject cms, CmsResource detailContent) {

        CmsContainerElementBean element = null;
        if (detailContent != null) {
            // get the right formatter

            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().getRootUri());
            CmsFormatterConfiguration formatters = config.getFormatters(cms, detailContent);
            I_CmsFormatterBean formatter = formatters.getDetailFormatter(getType(), getContainerWidth());
            if (formatter != null) {
                // create element bean
                element = new CmsContainerElementBean(
                    detailContent.getStructureId(),
                    formatter.getJspStructureId(),
                    null,
                    false); // when used as template element there are no properties
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
        }
        return containerWidth;
    }

    /**
     * Returns the serialized element data.<p>
     * 
     * @param cms the current cms context
     * @param elementBean the element to serialize
     * 
     * @return the serialized element data
     * 
     * @throws Exception if something goes wrong
     */
    private String getElementInfo(CmsObject cms, CmsContainerElementBean elementBean) throws Exception {

        return CmsContainerpageService.getSerializedElementInfo(
            cms,
            (HttpServletRequest)pageContext.getRequest(),
            (HttpServletResponse)pageContext.getResponse(),
            elementBean);
    }

    /**
     * Parses the maximum element number from the current container and returns the resulting number.<p>
     *  
     * @param requestUri the requested URI
     *  
     * @return the maximum number of elements of the container 
     */
    private int getMaxElements(String requestUri) {

        String containerName = getName();
        String containerMaxElements = getMaxElements();

        int maxElements = -1;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(containerMaxElements)) {
            try {
                maxElements = Integer.parseInt(containerMaxElements);
            } catch (NumberFormatException e) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.LOG_WRONG_CONTAINER_MAXELEMENTS_3,
                    new Object[] {requestUri, containerName, containerMaxElements}), e);
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_MAXELEMENTS_NOT_SET_2,
                    new Object[] {containerName, requestUri}));
            }
        }
        return maxElements;
    }

    /**
     * Returns the ADE session cache for container elements.<p>
     * 
     * @param cms the cms context
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache(CmsObject cms) {

        return CmsADESessionCache.getCache((HttpServletRequest)(pageContext.getRequest()), cms);
    }

    /**
     * Prints an element error tag to the response out.<p>
     * 
     * @param isOnline true if we are in Online mode 
     * @param elementSitePath the element site path
     * @param formatterSitePath the formatter site path
     * @param exception the exception causing the error
     * 
     * @throws IOException if something goes wrong writing to response out
     */
    private void printElementErrorTag(
        boolean isOnline,
        String elementSitePath,
        String formatterSitePath,
        Exception exception) throws IOException {

        if (!isOnline) {
            String stacktrace = CmsException.getStackTraceAsString(exception);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(stacktrace)) {
                stacktrace = null;
            } else {
                // stacktrace = CmsStringUtil.escapeJavaScript(stacktrace);
                stacktrace = CmsEncoder.escapeXml(stacktrace);
            }
            StringBuffer errorBox = new StringBuffer(256);
            errorBox.append("<div style=\"display:block; padding: 5px; border: red solid 2px; color: black; background: white;\" class=\"");
            errorBox.append(CmsContainerElement.CLASS_ELEMENT_ERROR);
            errorBox.append("\">");
            errorBox.append(Messages.get().getBundle().key(
                Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                elementSitePath,
                formatterSitePath));
            errorBox.append("<br />");
            errorBox.append(exception.getLocalizedMessage());
            if (stacktrace != null) {
                errorBox.append("<span onclick=\"__openStacktraceDialog(event);\" style=\"border: 1px solid black; cursor: pointer;\">");
                errorBox.append(Messages.get().getBundle().key(Messages.GUI_LABEL_STACKTRACE_0));
                errorBox.append("<span title=\"");
                errorBox.append(Messages.get().getBundle().key(Messages.GUI_LABEL_STACKTRACE_0));
                errorBox.append("\" class=\"hiddenStacktrace\" style=\"display:none;\"><pre><b>");
                errorBox.append(exception.getLocalizedMessage());
                errorBox.append("</b>\n\n");
                errorBox.append(stacktrace);
                errorBox.append("</pre></span></span>");
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
        boolean alreadyFull) throws Exception {

        CmsTemplateContext context = (CmsTemplateContext)(request.getAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_CONTEXT));
        if ((context == null) && alreadyFull) {
            return false;
        }
        boolean showInContext = shouldShowInContext(element, context);
        boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
        if (isOnline && !showInContext) {
            return false;
        }
        element.initResource(cms);
        if (isOnline && !element.isReleasedAndNotExpired()) {
            // do not render expired resources for the online project
            return false;
        }
        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        String containerType = getType();
        int containerWidth = getContainerWidth();
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        boolean isGroupContainer = element.isGroupContainer(cms);
        boolean isInheritedContainer = element.isInheritedContainer(cms);
        I_CmsFormatterBean formatterConfig = null;
        // writing elements to the session cache to improve performance of the container-page editor in offline project
        if (!isOnline) {
            if (!isGroupContainer && !isInheritedContainer) {
                // ensure that the formatter configuration id is added to the element settings, so it will be persisted on save
                formatterConfig = ensureValidFormatterSettings(
                    cms,
                    element,
                    adeConfig,
                    getName(),
                    containerType,
                    containerWidth);
            }
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
            printElementWrapperTagStart(isOnline, cms, element, true);
            for (CmsContainerElementBean subelement : subElements) {

                try {
                    subelement.initResource(cms);
                    boolean shouldShowSubElementInContext = shouldShowInContext(subelement, context);
                    if (isOnline && (!shouldShowSubElementInContext || !subelement.isReleasedAndNotExpired())) {
                        continue;
                    }
                    I_CmsFormatterBean subElementFormatterConfig = null;
                    // writing elements to the session cache to improve performance of the container-page editor
                    if (!isOnline) {
                        subElementFormatterConfig = ensureValidFormatterSettings(
                            cms,
                            subelement,
                            adeConfig,
                            getName(),
                            containerType,
                            containerWidth);
                        getSessionCache(cms).setCacheContainerElement(subelement.editorHash(), subelement);
                    }
                    if (subElementFormatterConfig == null) {
                        CmsFormatterConfiguration subelementFormatters = adeConfig.getFormatters(
                            cms,
                            subelement.getResource());
                        subElementFormatterConfig = subelementFormatters.getDefaultFormatter(
                            containerType,
                            containerWidth);
                    }

                    if (subElementFormatterConfig == null) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(new CmsIllegalStateException(Messages.get().container(
                                Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                                subelement.getSitePath(),
                                OpenCms.getResourceManager().getResourceType(subelement.getResource()).getTypeName(),
                                containerType)));
                        }
                        // skip this element, it has no formatter for this container type defined
                        continue;
                    }
                    // execute the formatter JSP for the given element URI
                    // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
                    printElementWrapperTagStart(isOnline, cms, subelement, false);
                    standardContext.setElement(subelement);
                    try {
                        String formatterSitePath = cms.getRequestContext().removeSiteRoot(
                            subElementFormatterConfig.getJspRootPath());
                        if (shouldShowSubElementInContext) {
                            CmsJspTagInclude.includeTagAction(
                                pageContext,
                                formatterSitePath,
                                null,
                                locale,
                                false,
                                isOnline,
                                null,
                                CmsRequestUtil.getAtrributeMap(req),
                                req,
                                res);
                        } else {
                            pageContext.getOut().print(DUMMY_ELEMENT);
                        }
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                    subelement.getSitePath(),
                                    subElementFormatterConfig),
                                e);
                        }
                        printElementErrorTag(
                            isOnline,
                            subelement.getSitePath(),
                            subElementFormatterConfig.getJspRootPath(),
                            e);
                    }
                    printElementWrapperTagEnd(isOnline, false);
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e);
                    }
                }
            }
            printElementWrapperTagEnd(isOnline, true);
            return true;
        } else {
            boolean result = true;
            if (alreadyFull) {
                result = false;
                if (!showInContext) {
                    printElementWrapperTagStart(isOnline, cms, element, false);
                    pageContext.getOut().print(DUMMY_ELEMENT);
                    printElementWrapperTagEnd(isOnline, false);
                }
            } else {
                String formatter = null;
                try {
                    if (formatterConfig != null) {
                        formatter = cms.getRequestContext().removeSiteRoot(formatterConfig.getJspRootPath());
                    } else {
                        formatter = cms.getSitePath(cms.readResource(element.getFormatterId()));
                    }
                } catch (CmsException e) {
                    // the formatter resource can not be found, try reading it form the configuration
                    CmsFormatterConfiguration elementFormatters = adeConfig.getFormatters(cms, element.getResource());
                    I_CmsFormatterBean elementFormatterBean = elementFormatters.getDefaultFormatter(
                        containerType,
                        containerWidth);
                    if (elementFormatterBean == null) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(new CmsIllegalStateException(Messages.get().container(
                                Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                                element.getSitePath(),
                                OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName(),
                                containerType)));
                        }
                        // skip this element, it has no formatter for this container type defined
                        return false;
                    }
                    formatter = cms.getRequestContext().removeSiteRoot(elementFormatterBean.getJspRootPath());
                }

                printElementWrapperTagStart(isOnline, cms, element, false);
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
                            isOnline,
                            null,
                            CmsRequestUtil.getAtrributeMap(req),
                            req,
                            res);
                    }
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                element.getSitePath(),
                                formatter),
                            e);
                    }
                    printElementErrorTag(isOnline, element.getSitePath(), formatter, e);
                }
                printElementWrapperTagEnd(isOnline, false);
            }
            return result;
        }
    }

    /**
     * Helper method to determine whether an element should be shown in a context.<p>
     * 
     * @param element the element for which the visibility should be determined 
     * @param context the context for which to check 
     * 
     * @return true if the current context doesn't prohibit the element from being shown 
     */
    private boolean shouldShowInContext(CmsContainerElementBean element, CmsTemplateContext context) {

        if (context == null) {
            return true;
        }

        try {
            if ((element.getResource() != null)
                && !OpenCms.getTemplateContextManager().shouldShowType(
                    context,
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
        if (!contextsAllowedList.contains(context.getKey())) {
            return false;
        }
        return true;
    }
}
