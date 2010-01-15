/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContainer.java,v $
 * Date   : $Date: 2010/01/15 14:55:48 $
 * Version: $Revision: 1.13 $
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the page container elements.<p>
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagContainer extends TagSupport {

    /** Json property name constants for containers. */
    public enum JsonContainer {

        /** The list of elements. */
        ELEMENTS("elements"),
        /** The max allowed number of elements in the container. */
        MAXELEMENTS("maxElem"),
        /** The container name. */
        NAME("name"),
        /** The container type. */
        TYPE("type");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonContainer(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The create no tag attribute value constant. */
    private static final String CREATE_NO_TAG = "none";

    /** The default tag name constant. */
    private static final String DEFAULT_TAG_NAME = "div";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContainer.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1228397990961282556L;

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

    /**
     * Internal action method.<p>
     * 
     * @param pageContext the current JSP page context
     * @param containerName the name of the container
     * @param containerType the type of the container
     * @param containerMaxElements the maximal number of elements in the container 
     * @param tag the tag to create
     * @param tagClass the tag class attribute
     * @param req the current request
     * @param res the current response
     * 
     * @throws CmsException if something goes wrong
     * @throws JspException if there is some problem calling the jsp formatter
     * @throws IOException if there is a problem writing on the response
     */
    public static void containerTagAction(
        PageContext pageContext,
        String containerName,
        String containerType,
        String containerMaxElements,
        String tag,
        String tagClass,
        ServletRequest req,
        ServletResponse res) throws CmsException, JspException, IOException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();
        boolean actAsTemplate = false;

        // get the container page itself, checking the history first
        CmsResource containerPage = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
        if (containerPage == null) {
            containerPage = cms.readResource(cms.getRequestContext().getUri());
        }
        if (!CmsResourceTypeXmlContainerPage.isContainerPage(containerPage)) {
            // container page is used as template
            String cntPagePath = cms.readPropertyObject(
                containerPage,
                CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                true).getValue("");
            try {
                containerPage = cms.readResource(cntPagePath);
            } catch (CmsException e) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_CONTAINER_PAGE_NOT_FOUND_3,
                    cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    cntPagePath), e);
            }
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(containerPage)) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_CONTAINER_PAGE_NOT_FOUND_3,
                    cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    cntPagePath));
            }
            actAsTemplate = true;
        } else if (req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER) != null) {
            actAsTemplate = true;
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
                        getCntDataTag(new CmsContainerBean(containerName, containerType, maxElements, null)));
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
        // actualize the cache
        container.setMaxElements(maxElements);

        if (!isOnline) {
            // add container data for the editor
            try {
                pageContext.getOut().print(getCntDataTag(container));
            } catch (JSONException e) {
                // should never happen
                throw new JspException(e);
            }
        }

        // validate the type
        if (!containerType.equals(container.getType())) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.LOG_WRONG_CONTAINER_TYPE_4,
                new Object[] {cms.getSitePath(containerPage), locale, containerName, containerType}));
        }

        // get the actual number of elements to render
        int renderElems = container.getElements().size();
        if ((maxElements > 0) && (renderElems > maxElements)) {
            renderElems = maxElements;
        }

        if (actAsTemplate) {
            if (!cntPage.getTypes().contains(CmsContainerPageBean.TYPE_TEMPLATE)) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_CONTAINER_PAGE_NO_TYPE_3,
                    cms.getRequestContext().getUri(),
                    cms.getSitePath(containerPage),
                    CmsContainerPageBean.TYPE_TEMPLATE));
            }
            if (containerType.equals(CmsContainerPageBean.TYPE_TEMPLATE)) {
                // render template element
                renderElems--;
                CmsResource resUri;
                if (req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER) != null) {
                    CmsUUID id = new CmsUUID(req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER));
                    resUri = cms.readResource(id);
                } else {
                    // check the history first
                    resUri = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
                    if (resUri == null) {
                        resUri = cms.readResource(cms.getRequestContext().getUri());
                    }
                }

                String elementFormatter = OpenCms.getResourceManager().getResourceType(resUri).getFormatterForContainerType(
                    cms,
                    resUri,
                    containerType);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(elementFormatter)) {
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        cms.getRequestContext().getUri(),
                        OpenCms.getResourceManager().getResourceType(resUri).getTypeName(),
                        CmsContainerPageBean.TYPE_TEMPLATE));
                }
                // execute the formatter jsp for the given element uri
                CmsContainerElementBean element = new CmsContainerElementBean(
                    resUri.getStructureId(),
                    cms.readResource(elementFormatter).getStructureId(),
                    null); // when used as template element there are no properties

                // do not write element data for the editor

                CmsJspTagInclude.includeTagAction(
                    pageContext,
                    elementFormatter,
                    null,
                    false,
                    null,
                    Collections.singletonMap(CmsADEManager.ATTR_CURRENT_ELEMENT, (Object)element),
                    req,
                    res);
            }
        }

        // iterate the elements
        for (CmsContainerElementBean element : container.getElements()) {
            if (renderElems < 1) {
                break;
            }
            renderElems--;

            CmsResource resUri = cms.readResource(element.getElementId());

            if (resUri.getTypeId() == CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_ID) {
                CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(cms, resUri, req);
                CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                    cms,
                    cms.getRequestContext().getLocale());
                if (!subContainer.getTypes().contains(containerType)) {
                    //TODO: change message
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        resUri.getRootPath(),
                        OpenCms.getResourceManager().getResourceType(resUri).getTypeName(),
                        containerType));
                }
                for (CmsContainerElementBean subelement : subContainer.getElements()) {
                    CmsResource subelementRes = cms.readResource(subelement.getElementId());
                    String subelementUri = cms.getSitePath(subelementRes);

                    //String subelementFormatter = cms.getSitePath(subelement.getFormatter());
                    //                    String subelementFormatter = OpenCms.getADEManager().getXmlContentFormatters(cms, subelementRes).get(
                    //                        containerType);
                    String subelementFormatter = OpenCms.getResourceManager().getResourceType(subelementRes).getFormatterForContainerType(
                        cms,
                        subelementRes,
                        containerType);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(subelementFormatter) && LOG.isErrorEnabled()) {
                        // skip this element, it has no formatter for this container type defined
                        LOG.error(new CmsIllegalStateException(Messages.get().container(
                            Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                            subelementUri,
                            OpenCms.getResourceManager().getResourceType(subelementRes).getTypeName(),
                            containerType)));
                        continue;
                    }

                    // execute the formatter jsp for the given element uri
                    CmsJspTagInclude.includeTagAction(
                        pageContext,
                        subelementFormatter,
                        null,
                        false,
                        null,
                        Collections.singletonMap(CmsADEManager.ATTR_CURRENT_ELEMENT, (Object)subelement),
                        req,
                        res);
                }

            } else {
                String elementFormatter = cms.getSitePath(cms.readResource(element.getFormatterId()));

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
            }
        }
        // close tag for container
        if (createTag) {
            pageContext.getOut().print(getTagClose(tagName));
        }
    }

    /**
     * Creates a new data tag for the given container.<p>
     * 
     * @param container the container to get the data tag for
     * 
     * @return html data tag for the given container
     *
     * @throws JSONException if there is a problem with JSON manipulation
     */
    protected static String getCntDataTag(CmsContainerBean container) throws JSONException {

        // add container data for the editor
        JSONObject jsonContainer = new JSONObject();
        jsonContainer.put(JsonContainer.NAME.getName(), container.getName());
        jsonContainer.put(JsonContainer.TYPE.getName(), container.getType());
        jsonContainer.put(JsonContainer.MAXELEMENTS.getName(), container.getMaxElements());

        JSONArray jsonElements = new JSONArray();
        for (CmsContainerElementBean element : container.getElements()) {
            jsonElements.put(element.getClientId());
        }
        jsonContainer.put(JsonContainer.ELEMENTS.getName(), jsonElements);
        return new StringBuffer("<div class='cms-ade-cnt-data ").append(jsonContainer.toString()).append("' ></div>").toString();
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
     * @return SKIP_BODY
     * @throws JspException in case something goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                containerTagAction(
                    pageContext,
                    getName(),
                    getType(),
                    getMaxElements(),
                    getTag(),
                    getTagClass(),
                    req,
                    res);
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
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_type = null;
        m_name = null;
        m_maxElements = null;
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
}
