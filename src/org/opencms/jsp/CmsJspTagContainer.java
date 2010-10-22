/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContainer.java,v $
 * Date   : $Date: 2010/10/22 12:06:20 $
 * Version: $Revision: 1.29 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapEntry;

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
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.29 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagContainer extends TagSupport {

    /** Json property name constants for containers. */
    public enum JsonContainer {

        /** The list of elements. */
        elements,
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
    public static final String CLASS_CONTAINER_ELEMENTS = "cms_ade_element";

    /** HTML class used to identify sub container elements. */
    public static final String CLASS_SUB_CONTAINER_ELEMENTS = "cms_ade_subcontainer";

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
     * 
     * @return html data tag for the given container
     *
     * @throws JSONException if there is a problem with JSON manipulation
     */
    protected static String getCntDataTag(CmsContainerBean container, String widthStr) throws JSONException {

        // add container data for the editor
        JSONObject jsonContainer = new JSONObject();
        jsonContainer.put(JsonContainer.name.name(), container.getName());
        jsonContainer.put(JsonContainer.type.name(), container.getType());
        jsonContainer.put(JsonContainer.maxElem.name(), container.getMaxElements());
        int width = -1;
        try {
            if (widthStr != null) {
                width = Integer.parseInt(widthStr);
            }
        } catch (NumberFormatException e) {
            //ignore; set width to -1
        }
        jsonContainer.put(JsonContainer.width.name(), width);

        JSONArray jsonElements = new JSONArray();
        for (CmsContainerElementBean element : container.getElements()) {
            jsonElements.put(element.getClientId());
        }
        jsonContainer.put(JsonContainer.elements.name(), jsonElements);
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
     * 
     * @throws CmsException if something goes wrong
     * @throws JspException if there is some problem calling the jsp formatter
     * @throws IOException if there is a problem writing on the response
     */
    public void containerTagAction() throws CmsException, JspException, IOException {

        String containerName = getName();
        String containerType = getType();
        String width = getWidth();
        String tag = getTag();
        String tagClass = getTagClass();
        boolean detailView = m_detailView;
        ServletRequest req = pageContext.getRequest();

        // TODO: remove old ADE functions
        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        // get the container page itself, checking the history first
        CmsResource containerPage = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
        if (containerPage == null) {
            containerPage = cms.readResource(cms.getRequestContext().getUri());
        }

        // create tag for container if necessary
        boolean createTag = false;
        String tagName = CmsStringUtil.isEmptyOrWhitespaceOnly(tag) ? DEFAULT_TAG_NAME : tag;
        if (!CREATE_NO_TAG.equals(tag)) {
            createTag = true;
            pageContext.getOut().print(getTagOpen(tagName, containerName, tagClass));
        }

        CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, containerPage, req);
        CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
        Locale locale = cntPage.getLocale();

        // get the maximal number of elements
        int maxElements = getMaxElements(cms, containerPage, locale);

        // get the container
        CmsContainerBean container = cntPage.getContainers().get(containerName);
        boolean isOnline = cms.getRequestContext().currentProject().isOnlineProject();
        if (container == null) {
            // container not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().container(
                    Messages.LOG_CONTAINER_NOT_FOUND_3,
                    cms.getSitePath(containerPage),
                    locale,
                    containerName).key());
            }
            if (!isOnline) {
                // add container data for the editor
                try {
                    pageContext.getOut().print(
                        getCntDataTag(new CmsContainerBean(containerName, containerType, maxElements, null), width));
                } catch (JSONException e) {
                    // should never happen
                    throw new JspException(e);
                }
            }

            // close tag for the empty container
            if (createTag) {
                pageContext.getOut().print(getTagClose(tagName));
            }
            return;
        }

        // validate the type
        if (!containerType.equals(container.getType())) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.LOG_WRONG_CONTAINER_TYPE_4,
                new Object[] {cms.getSitePath(containerPage), locale, containerName, containerType}));
        }

        // actualize the cache
        container.setMaxElements(maxElements);

        List<CmsContainerElementBean> allElems = new ArrayList<CmsContainerElementBean>();
        allElems.addAll(container.getElements());

        if (detailView) {
            addDetailViewToElements(cms, allElems);
        }

        if (!isOnline) {
            // add container data for the editor
            try {
                pageContext.getOut().print(
                    getCntDataTag(new CmsContainerBean(containerName, containerType, maxElements, allElems), width));
            } catch (JSONException e) {
                // should never happen
                throw new JspException(e);
            }

            // writing elements to the session cache to improve performance of the container-page editor
            CmsADESessionCache sessionCache = (CmsADESessionCache)((HttpServletRequest)req).getSession().getAttribute(
                CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
            if (sessionCache == null) {
                sessionCache = new CmsADESessionCache(cms);
                ((HttpServletRequest)req).getSession().setAttribute(
                    CmsADESessionCache.SESSION_ATTR_ADE_CACHE,
                    sessionCache);
            }
            for (CmsContainerElementBean element : allElems) {
                sessionCache.setCacheContainerElement(element.getClientId(), element);
            }
        }

        // get the actual number of elements to render
        int renderElems = allElems.size();
        if ((maxElements > 0) && (renderElems > maxElements)) {
            renderElems = maxElements;
        }

        // iterate the elements
        for (CmsContainerElementBean element : allElems) {
            if (renderElems < 1) {
                break;
            }
            renderElems--;
            renderContainerElement(cms, element);
        }
        // close tag for container
        if (createTag) {
            pageContext.getOut().print(getTagClose(tagName));
        }
    }

    /**
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
                containerTagAction();
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

        return m_maxElements;
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
     * @return the type attribute value
     */
    public String getType() {

        return m_type;
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
     * @return the closing tag
     * 
     * @see org.opencms.jsp.CmsJspTagContainer#getElementWrapperTagStart(CmsObject, CmsResource, CmsContainerElementBean, boolean)
     */
    protected String getElementWrapperTagEnd() {

        return "</div>";
    }

    /**
     * Returns the opening wrapper tag for elements in the offline project. The wrapper tag is needed by the container-page editor
     * to identify elements within a container.<p>
     * 
     * @param cms the cms object
     * @param resource the element resource
     * @param elementBean the element
     * @param isSubcontainer <code>true</code> if element is a sub-container
     * 
     * @return the opening tag
     * 
     * @throws CmsException if something goes wrong reading permissions and lock state
     */
    protected String getElementWrapperTagStart(
        CmsObject cms,
        CmsResource resource,
        CmsContainerElementBean elementBean,
        boolean isSubcontainer) throws CmsException {

        StringBuffer result = new StringBuffer("<div class='");
        if (isSubcontainer) {
            result.append(CLASS_SUB_CONTAINER_ELEMENTS);
        } else {
            result.append(CLASS_CONTAINER_ELEMENTS);
        }
        result.append("'");

        String noEditReason = new CmsResourceUtil(cms, resource).getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            cms));

        result.append(" title='").append(elementBean.getClientId()).append("'");
        result.append(" alt='").append(elementBean.getSitePath()).append("'");
        result.append(" hasprops='").append(hasProperties(cms, resource)).append("'");
        result.append(" rel='").append(CmsStringUtil.escapeHtml(noEditReason)).append("'>");

        return result.toString();
    }

    /**
     * Prints the closing tag for an element wrapper if in online mode.<p>
     * 
     * @param online if true, we are online 
     * 
     * @throws IOException if the output fails 
     */
    protected void printElementWrapperTagEnd(boolean online) throws IOException {

        if (!online) {
            pageContext.getOut().print(getElementWrapperTagEnd());
        }
    }

    /**
     * Prints the opening element wrapper tag for the container page editor if we are in Offline mode.<p>
     *  
     * @param online true if we are in Online mode 
     * @param cms the Cms context 
     * @param resource the element resource 
     * @param elementBean the element bean 
     * @param isSubContainer true if the element is a subcontainer 
     * 
     * @throws IOException if the IO fails
     * @throws CmsException if something goes wrong
     */
    protected void printElementWrapperTagStart(
        boolean online,
        CmsObject cms,
        CmsResource resource,
        CmsContainerElementBean elementBean,
        boolean isSubContainer) throws IOException, CmsException {

        if (!online) {
            pageContext.getOut().print(getElementWrapperTagStart(cms, resource, elementBean, isSubContainer));
        }
    }

    /**
     * Adds a detail view element to a list of elements if necessary.<p>
     * 
     * @param cms the CMS context
     * @param allElems the list to which the element should be added
     *  
     * @throws CmsException if something goes wrong 
     */
    private void addDetailViewToElements(CmsObject cms, List<CmsContainerElementBean> allElems) throws CmsException {

        String containerType = getType();
        int containerWidth = getContainerWidth();
        ServletRequest req = pageContext.getRequest();
        CmsUUID detailId = null;
        boolean isOnline = cms.getRequestContext().currentProject().isOnlineProject();
        CmsSitemapEntry sitemap = OpenCms.getSitemapManager().getRuntimeInfo(req);
        if (sitemap != null) {
            detailId = sitemap.getContentId();
        }
        if (detailId != null) {
            // read detail view 
            CmsResource resUri = cms.readResource(detailId);
            // get the right formatter
            String elementFormatter = OpenCms.getADEManager().getFormatterForContainerTypeAndWidth(
                cms,
                resUri,
                containerType,
                containerWidth);
            // check it
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(elementFormatter)) {
                // throw exception if offline, ignore if online
                if (!isOnline) {
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        cms.getSitePath(resUri),
                        OpenCms.getResourceManager().getResourceType(resUri).getTypeName(),
                        containerType));
                }
            } else {
                // add the detail view in first first of the current container
                CmsContainerElementBean element = new CmsContainerElementBean(
                    resUri.getStructureId(),
                    cms.readResource(elementFormatter).getStructureId(),
                    null); // when used as template element there are no properties
                allElems.add(0, element);
            }
        }
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
     * @param cms the CMS context  
     * @param containerPage the container page resource
     * @param locale the current locale
     *  
     * @return the maximum number of elements of the container 
     */
    private int getMaxElements(CmsObject cms, CmsResource containerPage, Locale locale) {

        String containerName = getName();
        String containerMaxElements = getMaxElements();

        int maxElements = -1;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(containerMaxElements)) {
            try {
                maxElements = Integer.parseInt(containerMaxElements);
            } catch (NumberFormatException e) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.LOG_WRONG_CONTAINER_MAXELEMENTS_4,
                    new Object[] {cms.getSitePath(containerPage), locale, containerName, containerMaxElements}), e);
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().container(
                    Messages.LOG_MAXELEMENTS_NOT_SET_3,
                    new Object[] {containerName, locale, cms.getSitePath(containerPage)}));
            }
        }
        return maxElements;
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

        Map<String, CmsXmlContentProperty> propConfig = CmsXmlContentDefinition.getContentHandlerForResource(
            cms,
            resource).getProperties();
        return !propConfig.isEmpty();
    }

    /**
     * Renders a container element.<p>
     * 
     * @param cms the CMS context 
     * @param element
     * @throws CmsException
     * @throws CmsXmlException
     * @throws CmsLoaderException
     * @throws IOException
     * @throws JspException
     */
    private void renderContainerElement(CmsObject cms, CmsContainerElementBean element)
    throws CmsException, CmsXmlException, CmsLoaderException, IOException, JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        String containerType = getType();
        int containerWidth = getContainerWidth();
        boolean isOnline = cms.getRequestContext().currentProject().isOnlineProject();

        CmsResource resUri = cms.readResource(element.getElementId());

        if (resUri.getTypeId() == CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_ID) {
            CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(cms, resUri, req);
            CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(cms, cms.getRequestContext().getLocale());
            if (!subContainer.getTypes().contains(containerType)) {
                //TODO: change message
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                    resUri.getRootPath(),
                    OpenCms.getResourceManager().getResourceType(resUri).getTypeName(),
                    containerType));
            }
            element.setSitePath(cms.getSitePath(resUri));
            // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
            printElementWrapperTagStart(isOnline, cms, resUri, element, true);
            for (CmsContainerElementBean subelement : subContainer.getElements()) {
                CmsResource subelementRes = cms.readResource(subelement.getElementId());
                String subelementUri = cms.getSitePath(subelementRes);
                String subelementFormatter = OpenCms.getADEManager().getFormatterForContainerTypeAndWidth(
                    cms,
                    subelementRes,
                    containerType,
                    containerWidth);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(subelementFormatter) && LOG.isErrorEnabled()) {
                    // skip this element, it has no formatter for this container type defined
                    LOG.error(new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        subelementUri,
                        OpenCms.getResourceManager().getResourceType(subelementRes).getTypeName(),
                        containerType)));
                    continue;
                }
                subelement.setSitePath(subelementUri);
                // execute the formatter jsp for the given element uri
                // wrapping the elements with DIV containing initial element data. To be removed by the container-page editor
                printElementWrapperTagStart(isOnline, cms, subelementRes, subelement, false);
                CmsJspTagInclude.includeTagAction(
                    pageContext,
                    subelementFormatter,
                    null,
                    false,
                    null,
                    Collections.singletonMap(CmsADEManager.ATTR_CURRENT_ELEMENT, (Object)subelement),
                    req,
                    res);
                printElementWrapperTagEnd(isOnline);
            }
            printElementWrapperTagEnd(isOnline);

        } else {
            String elementFormatter = cms.getSitePath(cms.readResource(element.getFormatterId()));

            element.setSitePath(cms.getSitePath(resUri));
            printElementWrapperTagStart(isOnline, cms, resUri, element, false);
            // execute the formatter jsp for the given element uri
            CmsJspTagInclude.includeTagAction(
                pageContext,
                elementFormatter,
                null,
                false,
                null,
                Collections.singletonMap(CmsADEManager.ATTR_CURRENT_ELEMENT, (Object)element),
                req,
                res);
            printElementWrapperTagEnd(isOnline);
        }
    }

}
