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
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_END_MARKER = "cms_ade_element_end";

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_START_MARKER = "cms_ade_element_start";

    /** HTML class used to identify error message for elements where rendering failed to render. */
    public static final String CLASS_ELEMENT_ERROR = "cms_ade_element_error";

    /** HTML class used to identify group container elements. */
    public static final String CLASS_GROUP_CONTAINER_ELEMENT_MARKER = "cms_ade_groupcontainer";

    /** Default number of max elements in the container in case no value has been set. */
    public static final String DEFAULT_MAX_ELEMENTS = "100";

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
     * Creates a new data tag for the given container.<p>
     * 
     * @param container the container to get the data tag for
     * @param widthStr the width of the container as a string 
     * @param isDetailView true if this container is currently being used for the detail view
     * 
     * @return html data tag for the given container
     *
     * @throws JSONException if there is a problem with JSON manipulation
     */
    protected static String getContainerDataTag(CmsContainerBean container, String widthStr, boolean isDetailView)
    throws JSONException {

        // add container data for the editor
        JSONObject jsonContainer = new JSONObject();
        jsonContainer.put(CmsContainerJsonKeys.NAME, container.getName());
        jsonContainer.put(CmsContainerJsonKeys.TYPE, container.getType());
        jsonContainer.put(CmsContainerJsonKeys.MAXELEMENTS, container.getMaxElements());
        jsonContainer.put(CmsContainerJsonKeys.DETAILVIEW, isDetailView);
        int width = -1;
        try {
            if (widthStr != null) {
                width = Integer.parseInt(widthStr);
            }
        } catch (NumberFormatException e) {
            //ignore; set width to -1
        }
        jsonContainer.put(CmsContainerJsonKeys.WIDTH, width);

        JSONArray jsonElements = new JSONArray();
        for (CmsContainerElementBean element : container.getElements()) {
            jsonElements.put(element.editorHash());
        }
        jsonContainer.put(CmsContainerJsonKeys.ELEMENTS, jsonElements);
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
                // create tag for container if necessary
                boolean createTag = false;
                String tagName = CmsStringUtil.isEmptyOrWhitespaceOnly(getTag()) ? DEFAULT_TAG_NAME : getTag();
                if (!CREATE_NO_TAG.equals(getTag())) {
                    createTag = true;
                    pageContext.getOut().print(getTagOpen(tagName, getName(), getTagClass()));
                }

                // get the maximal number of elements
                int maxElements = getMaxElements(requestUri);

                // get the container
                CmsContainerBean container = null;
                if (containerPage != null) {
                    container = containerPage.getContainers().get(getName());
                }
                boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();

                boolean isUsedAsDetailView = false;
                CmsResource detailContent = standardContext.getDetailContent();
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
                                        isUsedAsDetailView));
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
                        throw new CmsIllegalStateException(Messages.get().container(
                            Messages.LOG_WRONG_CONTAINER_TYPE_4,
                            new Object[] {requestUri, locale, getName(), getType()}));
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
                            pageContext.getOut().print(getContainerDataTag(cntBean, getWidth(), isUsedAsDetailView));
                        } catch (JSONException e) {
                            // should never happen
                            throw new JspException(e);
                        }
                    }
                    // iterate over elements to render
                    for (int i = 0; (i < maxElements) && (i < allElements.size()); i++) {
                        try {
                            renderContainerElement(cms, standardContext, allElements.get(i), locale);
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
     * Returns the closing wrapper tag for a container element.<p>
     * 
     * @param isGroupcontainer <code>true</code> if element is a group-container
     * 
     * @return the closing tag
     * 
     * @see org.opencms.jsp.CmsJspTagContainer#getElementWrapperTagStart(CmsObject, CmsContainerElementBean, boolean)
     */
    protected String getElementWrapperTagEnd(boolean isGroupcontainer) {

        if (isGroupcontainer) {
            return "</div>";
        }
        return "<div class=\"" + CLASS_CONTAINER_ELEMENT_END_MARKER + "\" style=\"display:none\"></div>";

    }

    /**
     * Returns the opening wrapper tag for elements in the offline project. The wrapper tag is needed by the container-page editor
     * to identify elements within a container.<p>
     * 
     * @param cms the cms object
     * @param elementBean the element
     * @param isGroupcontainer <code>true</code> if element is a group-container
     * 
     * @return the opening tag
     * 
     * @throws CmsException if something goes wrong reading permissions and lock state
     */
    protected String getElementWrapperTagStart(
        CmsObject cms,
        CmsContainerElementBean elementBean,
        boolean isGroupcontainer) throws CmsException {

        StringBuffer result = new StringBuffer("<div class='");
        if (isGroupcontainer) {
            result.append(CLASS_GROUP_CONTAINER_ELEMENT_MARKER);
        } else {
            result.append(CLASS_CONTAINER_ELEMENT_START_MARKER);
        }
        result.append("'");
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String noEditReason = "";
        // reinitializing resource to avoid caching issues
        elementBean.initResource(cms);
        if (CmsResourceTypeXmlContent.isXmlContent(elementBean.getResource())) {
            noEditReason = new CmsResourceUtil(cms, elementBean.getResource()).getNoEditReason(wpLocale, true);
        } else {
            noEditReason = Messages.get().getBundle().key(Messages.GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0);
        }
        result.append(" clientId='").append(elementBean.editorHash()).append("'");
        result.append(" alt='").append(elementBean.getSitePath()).append("'");
        String typeName = OpenCms.getResourceManager().getResourceType(elementBean.getResource().getTypeId()).getTypeName();
        if (elementBean.isCreateNew()) {
            result.append(" newType='").append(typeName).append("'");
            CmsResourceTypeConfig typeConfig = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().getRootUri()).getResourceType(typeName);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(noEditReason)
                && ((typeConfig == null) || !typeConfig.checkCreatable(cms))) {
                String niceName = CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName);
                noEditReason = Messages.get().getBundle().key(Messages.GUI_CONTAINERPAGE_TYPE_NOT_CREATABLE_1, niceName);
            }
        }
        result.append(" hasprops='").append(hasProperties(cms, elementBean.getResource())).append("'");
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
        boolean viewPermission = cms.hasPermissions(
            elementBean.getResource(),
            CmsPermissionSet.ACCESS_VIEW,
            false,
            CmsResourceFilter.IGNORE_EXPIRATION)
            && settings.getAccess().getPermissions(cms, elementBean.getResource()).requiresViewPermission();
        result.append(" hasviewpermission='").append(viewPermission).append("'");
        result.append(" releasedandnotexpired='").append(elementBean.isReleasedAndNotExpired()).append("'");
        result.append(" rel='").append(CmsStringUtil.escapeHtml(noEditReason));
        if (isGroupcontainer) {
            result.append("'>");
        } else {
            result.append("' style='display:none;'></div>");
        }
        return result.toString();
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
            pageContext.getOut().print(getElementWrapperTagEnd(isGroupcontainer));
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
     * @throws IOException if the IO fails
     * @throws CmsException if something goes wrong
     */
    protected void printElementWrapperTagStart(
        boolean isOnline,
        CmsObject cms,
        CmsContainerElementBean elementBean,
        boolean isGroupContainer) throws IOException, CmsException {

        if (!isOnline) {
            pageContext.getOut().print(getElementWrapperTagStart(cms, elementBean, isGroupContainer));
        }
    }

    /**
     * Adds a detail view element to a list of elements if necessary.<p>
     * 
     * @param cms the CMS context
     * @param allElems the list to which the element should be added
     */
    private CmsContainerElementBean generateDetailViewElement(CmsObject cms, CmsResource detailContent) {

        CmsContainerElementBean element = null;
        if (detailContent != null) {
            // get the right formatter

            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().getRootUri());
            CmsFormatterConfiguration formatters = config.getFormatters(cms, detailContent);
            CmsFormatterBean formatter = formatters.getFormatter(getType(), getContainerWidth());
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

        CmsADESessionCache sessionCache = (CmsADESessionCache)((HttpServletRequest)pageContext.getRequest()).getSession().getAttribute(
            CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        if (sessionCache == null) {
            sessionCache = new CmsADESessionCache(cms);
            ((HttpServletRequest)pageContext.getRequest()).getSession().setAttribute(
                CmsADESessionCache.SESSION_ATTR_ADE_CACHE,
                sessionCache);
        }
        return sessionCache;
    }

    /**
     * Helper method for checking whether there are properties defined for a given content element.<p>
     * 
     * @param cms the CmsObject to use for VFS operations 
     * @param resource the resource for which it should be checked whether it has properties 
     * 
     * @return true if the resource has properties defined 
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean hasProperties(CmsObject cms, CmsResource resource) throws CmsException {

        if (!CmsResourceTypeXmlContent.isXmlContent(resource)) {
            return false;
        }
        Map<String, CmsXmlContentProperty> propConfig = CmsXmlContentDefinition.getContentHandlerForResource(
            cms,
            resource).getSettings(cms, resource);
        return !propConfig.isEmpty();
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
            errorBox.append(CLASS_ELEMENT_ERROR);
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
     * @param cms the CMS context 
     * @param element the container element to render
     * @throws CmsException if something goes wrong reading the resources
     * @throws IOException if something goes wrong writing to the response
     */
    private void renderContainerElement(
        CmsObject cms,
        CmsJspStandardContextBean standardContext,
        CmsContainerElementBean element,
        Locale locale) throws CmsException, CmsXmlException, CmsLoaderException, IOException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        String containerType = getType();
        int containerWidth = getContainerWidth();
        boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
        element.initResource(cms);
        // writing elements to the session cache to improve performance of the container-page editor
        getSessionCache(cms).setCacheContainerElement(element.editorHash(), element);
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        if (element.isGroupContainer(cms)) {
            CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                cms,
                element.getResource(),
                req);
            CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(
                cms,
                cms.getRequestContext().getLocale());
            if (!groupContainer.getTypes().contains(containerType)) {
                //TODO: change message
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                    element.getResource().getRootPath(),
                    OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName(),
                    containerType));
            }
            // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
            printElementWrapperTagStart(isOnline, cms, element, true);
            for (CmsContainerElementBean subelement : groupContainer.getElements()) {
                try {
                    subelement.initResource(cms);
                    // writing elements to the session cache to improve performance of the container-page editor
                    getSessionCache(cms).setCacheContainerElement(subelement.editorHash(), subelement);
                    CmsFormatterConfiguration subelementFormatters = adeConfig.getFormatters(
                        cms,
                        subelement.getResource());
                    CmsFormatterBean subelementFormatter = subelementFormatters.getFormatter(
                        containerType,
                        containerWidth);
                    if (subelementFormatter == null) {
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
                        CmsJspTagInclude.includeTagAction(
                            pageContext,
                            subelementFormatter.getJspRootPath(),
                            null,
                            locale,
                            false,
                            isOnline,
                            null,
                            CmsRequestUtil.getAtrributeMap(req),
                            req,
                            res);
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().getBundle().key(
                                Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                                subelement.getSitePath(),
                                subelementFormatter), e);
                        }
                        printElementErrorTag(
                            isOnline,
                            subelement.getSitePath(),
                            subelementFormatter.getJspRootPath(),
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

        } else {

            String formatter = null;
            try {
                formatter = cms.getSitePath(cms.readResource(element.getFormatterId()));
            } catch (CmsException e) {
                // the formatter resource can not be found, try reading it form the configuration
                CmsFormatterConfiguration elementFormatters = adeConfig.getFormatters(cms, element.getResource());
                CmsFormatterBean elementFormatterBean = elementFormatters.getFormatter(containerType, containerWidth);
                if (elementFormatterBean == null) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(new CmsIllegalStateException(Messages.get().container(
                            Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                            element.getSitePath(),
                            OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName(),
                            containerType)));
                    }
                    // skip this element, it has no formatter for this container type defined
                    return;
                }
                formatter = elementFormatterBean.getJspRootPath();
            }
            printElementWrapperTagStart(isOnline, cms, element, false);
            standardContext.setElement(element);
            try {
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
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_CONTAINER_PAGE_ELEMENT_RENDER_ERROR_2,
                        element.getSitePath(),
                        formatter), e);
                }
                printElementErrorTag(isOnline, element.getSitePath(), formatter, e);
            }
            printElementWrapperTagEnd(isOnline, false);
        }
    }
}
