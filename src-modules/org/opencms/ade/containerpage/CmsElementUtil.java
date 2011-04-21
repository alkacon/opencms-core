/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsElementUtil.java,v $
 * Date   : $Date: 2011/04/21 10:30:33 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage;

import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class to generate the element data objects used within the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 8.0.0
 */
public class CmsElementUtil {

    /** The cms context. */
    private CmsObject m_cms;

    /** The actual container page uri. */
    private String m_cntPageUri;

    /** The http request. */
    private HttpServletRequest m_req;

    /** The http response. */
    private HttpServletResponse m_res;

    private CmsJspStandardContextBean m_standardContext;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param cntPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(CmsObject cms, String cntPageUri, HttpServletRequest req, HttpServletResponse res)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_req = req;
        m_res = res;
        m_cntPageUri = cntPageUri;
        // initializing request for standard context bean
        req.setAttribute(CmsJspStandardContextBean.ATTRIBUTE_CMS_OBJECT, m_cms);
        m_standardContext = CmsJspStandardContextBean.getInstance(req);
        CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(
            cms,
            m_cms.readResource(cntPageUri),
            req);
        CmsContainerPageBean containerPage = xmlContainerPage.getCntPage(cms, cms.getRequestContext().getLocale());
        m_standardContext.setPage(containerPage);
    }

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param element the element bean
     * @param formatter the formatter uri
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    private String getElementContent(CmsContainerElementBean element, CmsResource formatter, CmsContainer container)
    throws CmsException, ServletException, IOException {

        element.initResource(m_cms);
        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            formatter), element.getResource(), formatter);

        CmsResource loaderRes = loaderFacade.getLoaderStartResource();

        String oldUri = m_cms.getRequestContext().getUri();
        try {
            m_cms.getRequestContext().setUri(m_cntPageUri);
            CmsContainerBean containerBean = null;
            if (m_standardContext.getPage().getContainers().containsKey(container.getName())) {
                containerBean = m_standardContext.getPage().getContainers().get(container.getName());
            } else {
                containerBean = new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getMaxElements(),
                    Collections.<CmsContainerElementBean> emptyList());
            }
            if (containerBean.getWidth() == null) {
                containerBean.setWidth(String.valueOf(container.getWidth()));
            }
            m_standardContext.setContainer(containerBean);
            m_standardContext.setElement(element);
            // to enable 'old' direct edit features for content-collector-elements, 
            // set the direct-edit-provider-attribute in the request
            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(m_cms, CmsDirectEditMode.TRUE, element.getSitePath());
            m_req.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);
            String encoding = m_res.getCharacterEncoding();
            return (new String(loaderFacade.getLoader().dump(
                m_cms,
                loaderRes,
                null,
                m_cms.getRequestContext().getLocale(),
                m_req,
                m_res), encoding)).trim();
        } finally {
            m_cms.getRequestContext().setUri(oldUri);
        }
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param element the resource
     * @param containers the containers on the current container page 
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElementData getElementData(CmsContainerElementBean element, Collection<CmsContainer> containers)
    throws CmsException {

        CmsResource resource = m_cms.readResource(element.getId());
        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        CmsContainerElementData elementBean = new CmsContainerElementData();
        elementBean.setClientId(element.editorHash());
        elementBean.setSitePath(resUtil.getFullPath());
        elementBean.setLastModifiedDate(resource.getDateLastModified());
        elementBean.setLastModifiedByUser(m_cms.readUser(resource.getUserLastModified()).getName());
        elementBean.setNavText(resUtil.getNavText());
        elementBean.setTitle(resUtil.getTitle());
        elementBean.setResourceType(OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName());
        Map<String, CmsXmlContentProperty> propertyConfig = CmsXmlContentPropertyHelper.getPropertyInfo(m_cms, resource);
        elementBean.setProperties(CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
            m_cms,
            element.getSettings(),
            propertyConfig));
        elementBean.setPropertyConfig(new HashMap<String, CmsXmlContentProperty>(propertyConfig));
        elementBean.setViewPermission(m_cms.hasPermissions(
            resource,
            CmsPermissionSet.ACCESS_VIEW,
            false,
            CmsResourceFilter.DEFAULT_ONLY_VISIBLE));
        elementBean.setNoEditReason(CmsEncoder.escapeHtml(resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            m_cms))));
        elementBean.setStatus(resUtil.getStateAbbreviation());

        Map<String, String> contents = new HashMap<String, String>();
        if (resource.getTypeId() == CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_ID) {
            Set<String> types = new HashSet<String>();
            Map<String, CmsContainer> containersByName = new HashMap<String, CmsContainer>();
            for (CmsContainer container : containers) {
                types.add(container.getType());
                containersByName.put(container.getName(), container);
            }
            CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(m_cms, resource, m_req);
            CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(
                m_cms,
                m_cms.getRequestContext().getLocale());
            elementBean.setGroupContainer(true);
            elementBean.setTypes(groupContainer.getTypes());
            elementBean.setDescription(groupContainer.getDescription());
            if (groupContainer.getTypes().isEmpty()) {
                if (groupContainer.getElements().isEmpty()) {
                    String emptySub = "<div>NEW AND EMPTY</div>";
                    for (String name : containersByName.keySet()) {
                        contents.put(name, emptySub);
                    }
                } else {
                    // TODO: throw appropriate exception
                    return null;
                }
            } else {
                // add formatter and content entries for the supported types
                for (CmsContainer cnt : containersByName.values()) {
                    String type = cnt.getType();
                    if (groupContainer.getTypes().contains(type)) {
                        contents.put(cnt.getName(), "<div>should not be used</div>");
                    }
                }
            }
            // add subitems
            List<String> subItems = new ArrayList<String>();

            for (CmsContainerElementBean subElement : groupContainer.getElements()) {
                // collect ids
                subItems.add(subElement.editorHash());
            }
            elementBean.setSubItems(subItems);
        } else {
            // get map from type/width combination to formatter uri 
            Map<CmsPair<String, Integer>, String> formatters = getFormatterMap(resource, containers);
            Map<String, String> contentsByName = getContentsByContainerName(element, containers, formatters);
            contents = contentsByName;
        }
        elementBean.setContents(contents);
        return elementBean;
    }

    /**
     * Combines the a map of from type/width keys to formatters, a map from formatters to contents, and a list of containers
     * into a map from container names to contents. 
     *  
     * @param containers the containers 
     * @param formatters a map from type/width pairs to formatter jsps 
     * @param contentsByFormatter a map from formatter jsps to contents
     *  
     * @return a map from container names to contents 
     */
    private Map<String, String> getContentsByContainerName(
        CmsContainerElementBean element,
        Collection<CmsContainer> containers,
        Map<CmsPair<String, Integer>, String> formatters) {

        Map<String, String> contentsByName = new HashMap<String, String>();
        for (CmsContainer container : containers) {
            String name = container.getName();
            CmsPair<String, Integer> key = CmsPair.create(container.getType(), new Integer(container.getWidth()));
            String fmtUri = formatters.get(key);
            if (fmtUri != null) {
                String content = null;
                try {
                    content = getElementContent(element, m_cms.readResource(fmtUri), container);
                } catch (Exception e) {
                    // TODO: Log error
                    //                    LOG.error(Messages.get().getBundle().key(
                    //                        Messages.ERR_GENERATE_FORMATTED_ELEMENT_3,
                    //                        m_cms.getSitePath(resource),
                    //                        formatterUri,
                    //                        type), e);
                }
                if (content != null) {
                    contentsByName.put(name, content);
                }
            }
        }
        return contentsByName;
    }

    /**
     * Creates a map from type/width pairs to formatter jsp uris.<p>
     * 
     * @param resource the resource which should be formatted 
     * @param containers the list of containers 
     * 
     * @return a map from type/width pairs to formatter jsp uris
     * 
     * @throws CmsException if something goes wrong  
     */
    private Map<CmsPair<String, Integer>, String> getFormatterMap(
        CmsResource resource,
        Collection<CmsContainer> containers) throws CmsException {

        Map<CmsPair<String, Integer>, String> formatters = new HashMap<CmsPair<String, Integer>, String>();
        for (CmsContainer container : containers) {
            CmsPair<String, Integer> key = CmsPair.create(container.getType(), new Integer(container.getWidth()));
            if (!formatters.containsKey(key)) {
                String formatter = OpenCms.getADEManager().getFormatterForContainerTypeAndWidth(
                    m_cms,
                    resource,
                    container.getType(),
                    container.getWidth());
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(formatter)) {
                    formatters.put(key, formatter);
                }
            }
        }
        return formatters;
    }
}
