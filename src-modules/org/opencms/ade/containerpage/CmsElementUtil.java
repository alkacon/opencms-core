/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsElementUtil.java,v $
 * Date   : $Date: 2010/10/12 06:55:30 $
 * Version: $Revision: 1.8 $
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
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
 * @version $Revision: 1.8 $
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
    public String getElementContent(CmsContainerElementBean element, CmsResource formatter)
    throws CmsException, ServletException, IOException {

        CmsResource elementRes = m_cms.readResource(element.getElementId());
        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            formatter), elementRes, formatter);

        CmsResource loaderRes = loaderFacade.getLoaderStartResource();

        String oldUri = m_cms.getRequestContext().getUri();
        try {
            m_cms.getRequestContext().setUri(m_cntPageUri);

            // to enable 'old' direct edit features for content-collector-elements, 
            // set the direct-edit-provider-attribute in the request
            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(m_cms, CmsDirectEditMode.TRUE, m_cms.getSitePath(elementRes));
            m_req.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);

            element.setSitePath(m_cms.getSitePath(elementRes));
            m_req.setAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT, element);

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

        CmsResource resource = m_cms.readResource(element.getElementId());
        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        //      resElement.put(JsonElement.objtype.name(), TYPE_ELEMENT);
        CmsContainerElementData elementBean = new CmsContainerElementData();
        elementBean.setClientId(element.getClientId());
        elementBean.setSitePath(resUtil.getFullPath());
        elementBean.setLastModifiedDate(resource.getDateLastModified());
        elementBean.setLastModifiedByUser(m_cms.readUser(resource.getUserLastModified()).getName());
        elementBean.setNavText(resUtil.getNavText());
        elementBean.setTitle(resUtil.getTitle());

        Map<String, CmsXmlContentProperty> propertyConfig = CmsXmlContentPropertyHelper.getPropertyInfo(m_cms, resource);
        elementBean.setProperties(CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
            m_cms,
            element.getProperties(),
            propertyConfig));
        elementBean.setPropertyConfig(new HashMap<String, CmsXmlContentProperty>(propertyConfig));

        elementBean.setNoEditReason(CmsEncoder.escapeHtml(resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            m_cms))));
        elementBean.setStatus(resUtil.getStateAbbreviation());

        Map<String, String> contents = new HashMap<String, String>();
        if (resource.getTypeId() == CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_ID) {
            Set<String> types = new HashSet<String>();
            Map<String, CmsContainer> containersByName = new HashMap<String, CmsContainer>();
            for (CmsContainer container : containers) {
                types.add(container.getType());
                containersByName.put(container.getName(), container);
            }
            CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(m_cms, resource, m_req);
            CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                m_cms,
                m_cms.getRequestContext().getLocale());
            elementBean.setSubContainer(true);
            elementBean.setTypes(subContainer.getTypes());
            elementBean.setDescription(subContainer.getDescription());
            if (subContainer.getTypes().isEmpty()) {
                if (subContainer.getElements().isEmpty()) {
                    //TODO: use formatter to generate the 'empty'-content
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
                    if (subContainer.getTypes().contains(type)) {
                        contents.put(cnt.getName(), "<div>should not be used</div>");
                    }
                }
            }
            // add subitems
            List<String> subItems = new ArrayList<String>();

            for (CmsContainerElementBean subElement : subContainer.getElements()) {
                // collect ids
                subItems.add(subElement.getClientId());
            }
            elementBean.setSubItems(subItems);
        } else {
            // get map from type/width combination to formatter uri 
            Map<CmsPair<String, Integer>, String> formatters = getFormatterMap(resource, containers);
            // find unique formatters (we don't want to call the same formatter twice)  
            Set<String> formatterUris = new HashSet<String>(formatters.values());
            // get map from formatter uris to content 
            Map<String, String> contentsByFormatter = getContentsByFormatter(element, formatterUris);
            // combine them into mapping from container names to contents 
            Map<String, String> contentsByName = getContentsByContainerName(containers, formatters, contentsByFormatter);
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
        Collection<CmsContainer> containers,
        Map<CmsPair<String, Integer>, String> formatters,
        Map<String, String> contentsByFormatter) {

        Map<String, String> contentsByName = new HashMap<String, String>();
        for (CmsContainer container : containers) {
            String name = container.getName();
            CmsPair<String, Integer> key = CmsPair.create(container.getType(), new Integer(container.getWidth()));
            String fmtUri = formatters.get(key);
            if (fmtUri != null) {
                String content = contentsByFormatter.get(fmtUri);
                if (content != null) {
                    contentsByName.put(name, content);
                }
            }
        }
        return contentsByName;
    }

    /**
     * Formats the contents of an element using a set of formatters and returns a map from formatter uris to the 
     * corresponding formatted output.<p>
     * 
     * @param element the content element 
     * @param formatterUris a set of formatter jsp uris 
     * 
     * @return a map from formatter jsp uris to formatted contents 
     */
    private Map<String, String> getContentsByFormatter(CmsContainerElementBean element, Set<String> formatterUris) {

        Map<String, String> contentsByFormatter = new HashMap<String, String>();
        for (String formatterUri : formatterUris) {
            try {
                String jspResult = getElementContent(element, m_cms.readResource(formatterUri));
                // set the results
                contentsByFormatter.put(formatterUri, jspResult);
            } catch (Exception e) {
                //                    LOG.error(Messages.get().getBundle().key(
                //                        Messages.ERR_GENERATE_FORMATTED_ELEMENT_3,
                //                        m_cms.getSitePath(resource),
                //                        formatterUri,
                //                        type), e);
            }
        }
        return contentsByFormatter;
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
                    key.getFirst(),
                    key.getSecond().intValue());
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(formatter)) {
                    formatters.put(key, formatter);
                }
            }
        }
        return formatters;
    }
}
