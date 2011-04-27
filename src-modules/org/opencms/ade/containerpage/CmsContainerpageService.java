/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsContainerpageService.java,v $
 * Date   : $Date: 2011/04/27 13:05:08 $
 * Version: $Revision: 1.40 $
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

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsADESessionCache;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * The RPC service used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.40 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageService extends CmsGwtService implements I_CmsContainerpageService {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsContainerpageService.class);

    /** Serial version UID. */
    private static final long serialVersionUID = -6188370638303594280L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsContainerpageService newInstance(HttpServletRequest request) {

        CmsContainerpageService srv = new CmsContainerpageService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToFavoriteList(java.lang.String)
     */
    public void addToFavoriteList(String clientId) throws CmsRpcException {

        try {
            ensureSession();
            List<CmsContainerElementBean> list = OpenCms.getADEManager().getFavoriteList(getCmsObject());
            updateFavoriteRecentList(clientId, list);
            OpenCms.getADEManager().saveFavoriteList(getCmsObject(), list);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToRecentList(java.lang.String)
     */
    public void addToRecentList(String clientId) throws CmsRpcException {

        try {
            ensureSession();
            List<CmsContainerElementBean> list = OpenCms.getADEManager().getRecentList(getCmsObject());
            updateFavoriteRecentList(clientId, list);
            OpenCms.getADEManager().saveRecentList(getCmsObject(), list);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#createNewElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public CmsContainerElement createNewElement(
        String containerpageUri,
        String clientId,
        String resourceType,
        String locale) throws CmsRpcException {

        CmsContainerElement element = null;
        try {
            ensureSession();
            CmsResource newResource = OpenCms.getADEManager().createNewElement(
                getCmsObject(),
                containerpageUri,
                getRequest(),
                resourceType,
                new Locale(locale));
            CmsContainerElementBean bean = getCachedElement(clientId);
            CmsContainerElementBean newBean = new CmsContainerElementBean(
                newResource.getStructureId(),
                null,
                bean.getSettings(),
                false);
            String newClientId = newBean.editorHash();
            getSessionCache().setCacheContainerElement(newClientId, newBean);
            element = new CmsContainerElement();
            element.setClientId(newClientId);
            element.setSitePath(getCmsObject().getSitePath(newResource));
            element.setResourceType(resourceType);
        } catch (CmsException e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(java.lang.String, java.lang.String, java.util.Collection, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        Map<String, CmsContainerElementData> result = null;
        try {
            ensureSession();
            result = getElements(clientIds, containerpageUri, containers, new Locale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementWithProperties(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Collection, java.lang.String)
     */
    public CmsContainerElementData getElementWithProperties(
        String containerpageUri,
        String uriParams,
        String clientId,
        Map<String, String> properties,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        CmsContainerElementData element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsElementUtil elemUtil = new CmsElementUtil(
                cms,
                containerpageUri,
                getRequest(),
                getResponse(),
                new Locale(locale));
            CmsUUID serverId = OpenCms.getADEManager().convertToServerId(clientId);
            CmsContainerElementBean elementBean = createElement(serverId, properties);
            getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);
            element = elemUtil.getElementData(elementBean, containers);
        } catch (Throwable e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getFavoriteList(java.lang.String, java.util.Collection, java.lang.String)
     */
    public List<CmsContainerElementData> getFavoriteList(
        String containerpageUri,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            result = getListElementsData(
                OpenCms.getADEManager().getFavoriteList(getCmsObject()),
                containerpageUri,
                containers,
                new Locale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getRecentList(java.lang.String, java.util.Collection, java.lang.String)
     */
    public List<CmsContainerElementData> getRecentList(
        String containerpageUri,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            result = getListElementsData(
                OpenCms.getADEManager().getRecentList(getCmsObject()),
                containerpageUri,
                containers,
                new Locale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#prefetch()
     */
    public CmsCntPageData prefetch() throws CmsRpcException {

        CmsCntPageData data = null;
        CmsObject cms = getCmsObject();

        HttpServletRequest request = getRequest();
        try {
            CmsResource cntPage = getContainerpage(cms);
            String cntPageUri = cms.getSitePath(cntPage);
            data = new CmsCntPageData(
                cms.getSitePath(cntPage),
                getNoEditReason(cms, cntPage),
                CmsRequestUtil.encodeParams(request),
                CmsADEManager.PATH_SITEMAP_EDITOR_JSP,
                cntPageUri,
                getNewTypes(cms, request),
                cms.getRequestContext().getLocale().toString());
        } catch (Throwable e) {
            error(e);
        }
        return data;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveContainerpage(java.lang.String, java.util.List, java.lang.String)
     */
    public void saveContainerpage(String containerpageUri, List<CmsContainer> containers, String locale)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            Locale contentLocale = new Locale(locale);
            List<CmsContainerBean> containerBeans = new ArrayList<CmsContainerBean>();
            for (CmsContainer container : containers) {
                CmsContainerBean containerBean = getContainerBean(container, containerpageUri, locale);
                containerBeans.add(containerBean);
            }
            CmsContainerPageBean page = new CmsContainerPageBean(contentLocale, containerBeans);
            cms.lockResourceTemporary(containerpageUri);
            CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(containerpageUri));
            xmlCnt.save(cms, contentLocale, page);
            cms.unlockResource(containerpageUri);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveFavoriteList(java.util.List)
     */
    public void saveFavoriteList(List<String> clientIds) throws CmsRpcException {

        try {
            ensureSession();
            OpenCms.getADEManager().saveFavoriteList(getCmsObject(), getCachedElements(clientIds));
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveGroupContainer(java.lang.String, java.lang.String, org.opencms.ade.containerpage.shared.CmsGroupContainer, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> saveGroupContainer(
        String containerpageUri,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            String resourceName = groupContainer.getSitePath();
            if (groupContainer.isNew()) {
                CmsResource groupContainerResource = OpenCms.getADEManager().createNewElement(
                    getCmsObject(),
                    containerpageUri,
                    getRequest(),
                    CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
                    new Locale(locale));
                resourceName = cms.getSitePath(groupContainerResource);
                groupContainer.setSitePath(resourceName);
                groupContainer.setClientId(groupContainerResource.getStructureId().toString());
            }
            CmsGroupContainerBean groupContainerBean = getGroupContainerBean(groupContainer, containerpageUri, locale);
            cms.lockResourceTemporary(resourceName);
            CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                cms,
                cms.readFile(resourceName));
            xmlGroupContainer.save(cms, groupContainerBean);
            cms.unlockResource(resourceName);
        } catch (Throwable e) {
            error(e);
        }
        Collection<String> ids = new ArrayList<String>();
        ids.add(groupContainer.getClientId());
        return getElementsData(containerpageUri, reqParams, ids, containers, locale);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveRecentList(java.util.List)
     */
    public void saveRecentList(List<String> clientIds) throws CmsRpcException {

        try {
            ensureSession();
            OpenCms.getADEManager().saveRecentList(getCmsObject(), getCachedElements(clientIds));
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#syncSaveContainerpage(java.lang.String, java.util.List, java.lang.String)
     */
    public void syncSaveContainerpage(String containerpageUri, List<CmsContainer> containers, String locale)
    throws CmsRpcException {

        saveContainerpage(containerpageUri, containers, locale);
    }

    /**
     * Creates a new container element from a resource id and a map of properties.<p> 
     * 
     * @param resourceId the resource id 
     * @param properties the map of properties 
     * 
     * @return the new container element bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsContainerElementBean createElement(CmsUUID resourceId, Map<String, String> properties)
    throws CmsException {

        CmsObject cms = getCmsObject();
        Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementSettings(
            cms,
            cms.readResource(resourceId));

        Map<String, String> changedProps = new HashMap<String, String>();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String propName = entry.getKey();
                String propType = propertiesConf.get(propName).getType();
                changedProps.put(
                    propName,
                    CmsXmlContentPropertyHelper.getPropValueIds(cms, propType, properties.get(propName)));
            }
        }
        return new CmsContainerElementBean(resourceId, null, changedProps, false);
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * 
     * @return the cached container element bean
     */
    private CmsContainerElementBean getCachedElement(String clientId) {

        String id = clientId;
        CmsContainerElementBean element = null;
        element = getSessionCache().getCacheContainerElement(id);
        if (element != null) {
            return element;
        }
        if (id.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
            id = id.substring(0, id.indexOf(CmsADEManager.CLIENT_ID_SEPERATOR));
            element = getSessionCache().getCacheContainerElement(id);
            if (element != null) {
                return element;
            }
        }
        // this is necessary if the element has not been cached yet
        element = new CmsContainerElementBean(OpenCms.getADEManager().convertToServerId(id), null, null, false);
        getSessionCache().setCacheContainerElement(id, element);
        return element;
    }

    /**
     * Returns a list of container elements from a list with client id's.<p>
     * 
     * @param clientIds list of client id's
     * 
     * @return a list of element beans
     */
    private List<CmsContainerElementBean> getCachedElements(List<String> clientIds) {

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>();
        for (String id : clientIds) {
            try {
                result.add(getCachedElement(id));
            } catch (CmsIllegalArgumentException e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Helper method for converting a CmsContainer to a CmsContainerBean when saving a container page.<p>
     * 
     * @param container the container for which the CmsContainerBean should be created
     * @param containerpageUri the URI of the container page 
     *  
     * @return a container bean
     */
    private CmsContainerBean getContainerBean(CmsContainer container, String containerpageUri, String locale) {

        CmsObject cms = getCmsObject();
        CmsADESessionCache cache = getSessionCache();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : container.getElements()) {
            try {
                if (elementData.isNew()) {
                    elementData = createNewElement(
                        containerpageUri,
                        elementData.getClientId(),
                        elementData.getResourceType(),
                        locale);
                }
                CmsContainerElementBean element = cache.getCacheContainerElement(elementData.getClientId());

                // make sure resource is readable, 
                CmsResource resource = cms.readResource(element.getId());

                // check if there is a valid formatter
                int containerWidth = container.getWidth();
                String formatterUri = OpenCms.getADEManager().getFormatterForContainerTypeAndWidth(
                    cms,
                    resource,
                    container.getType(),
                    containerWidth);
                boolean hasValidFormatter = CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatterUri);
                if (hasValidFormatter) {
                    CmsResource formatter = cms.readResource(formatterUri);
                    elements.add(new CmsContainerElementBean(
                        element.getId(),
                        formatter.getStructureId(),
                        element.getSettings(),
                        false));
                }
            } catch (Exception e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        CmsContainerBean containerBean = new CmsContainerBean(container.getName(), container.getType(), -1, elements);
        return containerBean;
    }

    /**
     * Returns the requested container-page resource.<p>
     * 
     * @param cms the current cms object
     * 
     * @return the container-page resource
     * 
     * @throws CmsException if the resource could not be read for any reason
     */
    private CmsResource getContainerpage(CmsObject cms) throws CmsException {

        String currentUri = cms.getRequestContext().getUri();
        CmsResource containerPage = cms.readResource(currentUri);
        if (!CmsResourceTypeXmlContainerPage.isContainerPage(containerPage)) {
            // container page is used as template
            String cntPagePath = cms.readPropertyObject(
                containerPage,
                CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                true).getValue("");
            try {
                containerPage = cms.readResource(cntPagePath);
            } catch (CmsException e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return containerPage;
    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param clientIds the list of IDs of the elements to retrieve the data for
     * @param uriParam the current URI
     * @param containers the containers for which the element data should be fetched 
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private Map<String, CmsContainerElementData> getElements(
        Collection<String> clientIds,
        String uriParam,
        Collection<CmsContainer> containers,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, getRequest(), getResponse(), locale);
        Map<String, CmsContainerElementData> result = new HashMap<String, CmsContainerElementData>();
        Set<String> ids = new HashSet<String>();
        Iterator<String> it = clientIds.iterator();
        while (it.hasNext()) {
            String elemId = it.next();
            if (ids.contains(elemId)) {
                continue;
            }
            CmsContainerElementBean element = getCachedElement(elemId);
            CmsContainerElementData elementData = elemUtil.getElementData(element, containers);
            result.put(element.editorHash(), elementData);
            if (elementData.isGroupContainer()) {
                // this is a group-container 
                CmsResource elementRes = cms.readResource(element.getId());
                CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                    cms,
                    elementRes,
                    getRequest());
                CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(
                    cms,
                    cms.getRequestContext().getLocale());

                // adding all sub-items to the elements data
                for (CmsContainerElementBean subElement : groupContainer.getElements()) {
                    if (!ids.contains(subElement.getId())) {
                        String subId = subElement.editorHash();
                        if (ids.contains(subId)) {
                            continue;
                        }
                        CmsContainerElementData subItemData = elemUtil.getElementData(subElement, containers);
                        ids.add(subId);
                        result.put(subId, subItemData);
                    }
                }
            }
            ids.add(elemId);
        }
        return result;
    }

    /**
     * Helper method for converting a CmsGroupContainer to a CmsGroupContainerBean when saving a group container.<p>
     * 
     * @param groupContainer the group-container data
     * @param containerpageUri the URI of the container page 
     * 
     * @return the group-container bean
     */
    private CmsGroupContainerBean getGroupContainerBean(
        CmsGroupContainer groupContainer,
        String containerpageUri,
        String locale) {

        CmsObject cms = getCmsObject();
        CmsADESessionCache cache = getSessionCache();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : groupContainer.getElements()) {
            try {
                if (elementData.isNew()) {
                    elementData = createNewElement(
                        containerpageUri,
                        elementData.getClientId(),
                        elementData.getResourceType(),
                        locale);
                }
                CmsContainerElementBean element = cache.getCacheContainerElement(elementData.getClientId());

                // make sure resource is readable, 
                if (cms.existsResource(element.getId())) {
                    elements.add(element);
                }

            } catch (Exception e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        return new CmsGroupContainerBean(
            groupContainer.getTitle(),
            groupContainer.getDescription(),
            elements,
            groupContainer.getTypes());
    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param listElements the list of element beans to retrieve the data for
     * @param containerpageUri the current URI
     * @param containers the containers which exist on the container page  
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private List<CmsContainerElementData> getListElementsData(
        List<CmsContainerElementBean> listElements,
        String containerpageUri,
        Collection<CmsContainer> containers,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, containerpageUri, getRequest(), getResponse(), locale);
        CmsADESessionCache cache = getSessionCache();
        List<CmsContainerElementData> result = new ArrayList<CmsContainerElementData>();
        for (CmsContainerElementBean element : listElements) {
            // checking if resource exists
            if (cms.existsResource(element.getId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                cache.setCacheContainerElement(element.editorHash(), element);
                CmsContainerElementData elementData = elemUtil.getElementData(element, containers);
                result.add(elementData);
                if (elementData.isGroupContainer()) {
                    // this is a group-container 

                    CmsResource elementRes = cms.readResource(element.getId());
                    CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                        cms,
                        elementRes,
                        getRequest());
                    CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(
                        cms,
                        cms.getRequestContext().getLocale());

                    // adding all sub-items to the elements data
                    for (CmsContainerElementBean subElement : groupContainer.getElements()) {
                        CmsContainerElementData subItemData = elemUtil.getElementData(subElement, containers);
                        result.add(subItemData);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the map a resource type to be newly created for this container-page.<p>
     * 
     * @param cms the current cms object
     * @param request the current request
     * 
     * @return the map a resource type to be newly created for this container-page
     * 
     * @throws CmsRpcException if something goes wrong reading the ADE configuration
     */
    private Map<String, String> getNewTypes(CmsObject cms, HttpServletRequest request) throws CmsRpcException {

        Map<String, String> result = new HashMap<String, String>();
        CmsResourceManager resourceManager = OpenCms.getResourceManager();
        try {
            Collection<CmsResource> resources = OpenCms.getADEManager().getCreatableElements(
                cms,
                cms.getRequestContext().getUri(),
                request);
            for (CmsResource resource : resources) {
                result.put(
                    resourceManager.getResourceType(resource).getTypeName(),
                    resource.getStructureId().toString());
            }
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * Returns the no-edit reason for the given resource.<p>
     * 
     * @param cms the current cms object
     * @param containerPage the resource
     * 
     * @return the no-edit reason, empty if editing is allowed
     * 
     * @throws CmsException is something goes wrong
     */
    private String getNoEditReason(CmsObject cms, CmsResource containerPage) throws CmsException {

        return new CmsResourceUtil(cms, containerPage).getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            cms));
    }

    /**
     * Returns the session cache.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache() {

        if (m_sessionCache == null) {
            m_sessionCache = (CmsADESessionCache)getRequest().getSession().getAttribute(
                CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
            if (m_sessionCache == null) {
                m_sessionCache = new CmsADESessionCache(getCmsObject());
                getRequest().getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, m_sessionCache);
            }
        }
        return m_sessionCache;
    }

    /**
     * Update favorite or recent list with the given element.<p>
     * 
     * @param clientId the elements client id
     * @param list the list to update
     * 
     * @return the updated list
     */
    private List<CmsContainerElementBean> updateFavoriteRecentList(String clientId, List<CmsContainerElementBean> list) {

        CmsContainerElementBean element = getCachedElement(clientId);
        Iterator<CmsContainerElementBean> listIt = list.iterator();
        while (listIt.hasNext()) {
            CmsContainerElementBean listElem = listIt.next();
            if (listElem.getId().equals(element.getId())) {
                listIt.remove();
            }
        }
        list.add(0, element);
        return list;
    }
}
