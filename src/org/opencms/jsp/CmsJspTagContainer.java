/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspTagContainer.java,v $
 * Date   : $Date: 2009/10/06 08:19:07 $
 * Version: $Revision: 1.1.2.12 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.ade.CmsContainerBean;
import org.opencms.workplace.editors.ade.CmsContainerElementBean;
import org.opencms.workplace.editors.ade.CmsContainerPageBean;
import org.opencms.workplace.editors.ade.CmsContainerPageCache;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
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
 * @version $Revision: 1.1.2.12 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagContainer extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1228397990961282556L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContainer.class);

    /** The maxElements attribute value. */
    private String m_maxElements;

    /** The name attribute value. */
    private String m_name;

    /** The type attribute value. */
    private String m_type;

    /**
     * Internal action method.<p>
     * 
     * @param pageContext the current JSP page context
     * @param containerName the name of the container
     * @param containerType the type of the container
     * @param containerMaxElements the maximal number of elements in the container 
     * @param req the current request
     * @param res the current response
     * 
     * @throws CmsException if something goes wrong
     * @throws JspException if there is some problem calling the jsp formatter
     */
    public static void containerTagAction(
        PageContext pageContext,
        String containerName,
        String containerType,
        String containerMaxElements,
        ServletRequest req,
        ServletResponse res) throws CmsException, JspException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();
        boolean actAsTemplate = false;

        // get the container page itself
        CmsResource containerPage = cms.readResource(cms.getRequestContext().getUri());
        if (containerPage.getTypeId() != CmsResourceTypeContainerPage.getStaticTypeId()) {
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
            if (containerPage.getTypeId() != CmsResourceTypeContainerPage.getStaticTypeId()) {
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
        CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
            cms,
            containerPage,
            cms.getRequestContext().getLocale());
        Locale locale = cntPage.getLocale();

        // get the container
        if (!cntPage.getContainers().containsKey(containerName)) {
            /*
            if (!cms.getRequestContext().currentProject().isOnlineProject()) {
                CmsFile file = cms.readFile(containerPage);
                CmsXmlContent cnt = CmsXmlContentFactory.unmarshal(cms, file);
                I_CmsXmlContentValue value = cnt.addValue(
                    cms,
                    CmsContainerPageLoader.N_CONTAINER,
                    locale,
                    cnt.getIndexCount(CmsContainerPageLoader.N_CONTAINER, locale));
                cnt.getValue(CmsXmlUtils.concatXpath(value.getPath(), CmsContainerPageLoader.N_NAME), locale, 0).setStringValue(
                    cms,
                    containerName);
                cnt.getValue(CmsXmlUtils.concatXpath(value.getPath(), CmsContainerPageLoader.N_TYPE), locale, 0).setStringValue(
                    cms,
                    containerType);
                file.setContents(cnt.marshal());
                cms.lockResource(cms.getSitePath(file));
                cms.writeFile(file);
                containerTagAction(pageContext, containerName, containerType, containerMaxElements, req, res);
                return;
            } else {
            */
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.LOG_CONTAINER_NOT_FOUND_3,
                cms.getSitePath(containerPage),
                locale,
                containerName));
            //}
        }
        CmsContainerBean container = cntPage.getContainers().get(containerName);

        // validate the type
        if (!containerType.equals(container.getType())) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.LOG_WRONG_CONTAINER_TYPE_4,
                new Object[] {cms.getSitePath(containerPage), locale, containerName, containerType}));
        }

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
            // actualize the cache
            container.setMaxElements(maxElements);
        }
        CmsXmlEntityResolver entityResolver = new CmsXmlEntityResolver(cms);

        // get the actual number of elements to render
        int renderElems = container.getElements().size();
        if ((maxElements > -1) && (renderElems > maxElements)) {
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
                    resUri = cms.readResource(cms.getRequestContext().getUri());
                }

                // get the content definition
                List<CmsRelation> relations = cms.getRelationsForResource(
                    resUri,
                    CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
                CmsXmlContentDefinition contentDef = null;
                if ((relations != null) && !relations.isEmpty()) {
                    String xsd = cms.getSitePath(relations.get(0).getTarget(cms, CmsResourceFilter.ALL));
                    contentDef = entityResolver.getCachedContentDefinition(xsd);
                }
                if (contentDef == null) {
                    CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resUri));
                    contentDef = content.getContentDefinition();
                }

                String elementFormatter = contentDef.getContentHandler().getFormatters().get(containerType);

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(elementFormatter)) {
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                        cms.getRequestContext().getUri(),
                        contentDef.getSchemaLocation(),
                        CmsContainerPageBean.TYPE_TEMPLATE));
                }
                // HACK: we use the __element param for the element uri
                // execute the formatter jsp for the given element uri
                CmsJspTagInclude.includeTagAction(
                    pageContext,
                    elementFormatter,
                    cms.getSitePath(resUri),
                    false,
                    null,
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

            CmsResource resUri = element.getElement();
            if (resUri.getTypeId() == CmsResourceTypeContainerPage.getStaticTypeId()) {
                // get the subcontainer data from cache
                CmsContainerPageBean subcntPage = CmsContainerPageCache.getInstance().getCache(
                    cms,
                    resUri,
                    cms.getRequestContext().getLocale());
                // get the first subcontainer
                CmsContainerBean subcontainer = subcntPage.getContainers().values().iterator().next();
                // iterate the subelements
                for (CmsContainerElementBean subelement : subcontainer.getElements()) {
                    String subelementUri = cms.getSitePath(subelement.getElement());
                    // get the content definition
                    List<CmsRelation> relations = cms.getRelationsForResource(
                        subelement.getElement(),
                        CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
                    CmsXmlContentDefinition contentDef = null;
                    if ((relations != null) && !relations.isEmpty()) {
                        String xsd = cms.getSitePath(relations.get(0).getTarget(cms, CmsResourceFilter.ALL));
                        contentDef = entityResolver.getCachedContentDefinition(xsd);
                    }
                    if (contentDef == null) {
                        CmsXmlContent content = CmsXmlContentFactory.unmarshal(
                            cms,
                            cms.readFile(subelement.getElement()));
                        contentDef = content.getContentDefinition();
                    }
                    //String subelementFormatter = cms.getSitePath(subelement.getFormatter());
                    String subelementFormatter = contentDef.getContentHandler().getFormatters().get(containerType);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(subelementFormatter)) {
                        subelementFormatter = cms.getSitePath(subelement.getFormatter());
                    }
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(subelementFormatter)) {
                        throw new CmsIllegalStateException(Messages.get().container(
                            Messages.ERR_XSD_NO_TEMPLATE_FORMATTER_3,
                            subelementUri,
                            contentDef.getSchemaLocation(),
                            containerType));
                    }

                    // HACK: we use the __element param for the element uri
                    // execute the formatter jsp for the given element uri
                    CmsJspTagInclude.includeTagAction(
                        pageContext,
                        subelementFormatter,
                        subelementUri,
                        false,
                        null,
                        req,
                        res);
                }
            } else {
                String elementUri = cms.getSitePath(element.getElement());
                String elementFormatter = cms.getSitePath(element.getFormatter());

                // HACK: we use the __element param for the element uri
                // execute the formatter jsp for the given element uri
                CmsJspTagInclude.includeTagAction(pageContext, elementFormatter, elementUri, false, null, req, res);
            }
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
        ServletResponse res = pageContext.getResponse();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                containerTagAction(pageContext, getName(), getType(), getMaxElements(), req, res);
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
     * Sets the type attribute value.<p>
     *
     * @param type the type value to set
     */
    public void setType(String type) {

        m_type = type;
    }

}
