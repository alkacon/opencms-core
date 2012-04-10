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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsNewResourceXmlContent;
import org.opencms.workplace.explorer.CmsResourceUtil;
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
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * The RPC service used by the container-page editor.<p>
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#checkCreateNewElement(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String)
     */
    public CmsCreateElementData checkCreateNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCreateElementData result = new CmsCreateElementData();
        try {
            CmsResource currentPage = cms.readResource(pageStructureId);

            List<CmsResource> modelResources = CmsNewResourceXmlContent.getModelFiles(
                getCmsObject(),
                CmsResource.getFolderPath(cms.getSitePath(currentPage)),
                resourceType);
            if (modelResources.isEmpty()) {
                result.setCreatedElement(createNewElement(pageStructureId, clientId, resourceType, null, locale));
            } else {
                result.setModelResources(generateModelResourceList(
                    resourceType,
                    modelResources,
                    CmsLocaleManager.getLocale(locale)));
            }
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * Parses an element id.<p>
     * 
     * @param id the element id
     * 
     * @return the corresponding structure id
     * 
     * @throws CmsIllegalArgumentException if the id has not the right format
     */
    public CmsUUID convertToServerId(String id) throws CmsIllegalArgumentException {

        if (id == null) {
            throw new CmsIllegalArgumentException(org.opencms.xml.containerpage.Messages.get().container(
                org.opencms.xml.containerpage.Messages.ERR_INVALID_ID_1,
                id));
        }
        String serverId = id;
        try {
            if (serverId.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
                serverId = serverId.substring(0, serverId.indexOf(CmsADEManager.CLIENT_ID_SEPERATOR));
            }
            return new CmsUUID(serverId);
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(org.opencms.xml.containerpage.Messages.get().container(
                org.opencms.xml.containerpage.Messages.ERR_INVALID_ID_1,
                id));
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#createNewElement(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsContainerElement createNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsUUID modelResourceStructureId,
        String locale) throws CmsRpcException {

        CmsContainerElement element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(pageStructureId);
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(cms, pageResource.getRootPath());
            CmsResourceTypeConfig typeConfig = configData.getResourceType(resourceType);
            CmsObject cloneCms = OpenCms.initCmsObject(cms);
            cloneCms.getRequestContext().setLocale(CmsLocaleManager.getLocale(locale));
            CmsResource modelResource = null;
            if (modelResourceStructureId != null) {
                modelResource = cms.readResource(modelResourceStructureId);
            }
            CmsResource newResource = typeConfig.createNewElement(cloneCms, modelResource);
            CmsContainerElementBean bean = getCachedElement(clientId);
            CmsContainerElementBean newBean = new CmsContainerElementBean(
                newResource.getStructureId(),
                null,
                bean.getIndividualSettings(),
                false);
            String newClientId = newBean.editorHash();
            getSessionCache().setCacheContainerElement(newClientId, newBean);
            element = new CmsContainerElement();
            element.setClientId(newClientId);
            element.setSitePath(cms.getSitePath(newResource));
            element.setResourceType(resourceType);
        } catch (CmsException e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(org.opencms.util.CmsUUID, java.lang.String, java.util.Collection, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> getElementsData(
        CmsUUID pageStructureId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        Map<String, CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource pageResource = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(pageResource);
            result = getElements(clientIds, containerpageUri, containers, CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementWithSettings(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.util.Map, java.util.Collection, java.lang.String)
     */
    public CmsContainerElementData getElementWithSettings(
        CmsUUID pageStructureId,
        String uriParams,
        String clientId,
        Map<String, String> settings,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        CmsContainerElementData element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(pageStructureId);
            String containerpageUri = cms.getSitePath(pageResource);
            CmsElementUtil elemUtil = new CmsElementUtil(
                cms,
                containerpageUri,
                getRequest(),
                getResponse(),
                CmsLocaleManager.getLocale(locale));
            CmsContainerElementBean elementBean = getCachedElement(clientId);
            elementBean = CmsContainerElementBean.cloneWithSettings(
                elementBean,
                convertSettingValues(elementBean.getResource(), settings));
            getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);
            element = elemUtil.getElementData(elementBean, containers);
        } catch (Throwable e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getFavoriteList(org.opencms.util.CmsUUID, java.util.Collection, java.lang.String)
     */
    public List<CmsContainerElementData> getFavoriteList(
        CmsUUID pageStructureId,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource containerpage = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(containerpage);
            result = getListElementsData(
                OpenCms.getADEManager().getFavoriteList(getCmsObject()),
                containerpageUri,
                containers,
                CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getNewElementData(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.util.Collection, java.lang.String)
     */
    public CmsContainerElementData getNewElementData(
        CmsUUID pageStructureId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        String localeName) throws CmsRpcException {

        CmsContainerElementData result = null;
        try {
            ensureSession();
            CmsResource pageResource = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(pageResource);
            Locale locale = CmsLocaleManager.getLocale(localeName);
            result = getNewElement(resourceType, containerpageUri, containers, locale);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getRecentList(org.opencms.util.CmsUUID, java.util.Collection, java.lang.String)
     */
    public List<CmsContainerElementData> getRecentList(
        CmsUUID pageStructureId,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource containerpage = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(containerpage);
            result = getListElementsData(
                OpenCms.getADEManager().getRecentList(getCmsObject()),
                containerpageUri,
                containers,
                CmsLocaleManager.getLocale(locale));
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
            long lastModified = cntPage.getDateLastModified();
            String cntPageUri = cms.getSitePath(cntPage);
            data = new CmsCntPageData(
                cms.getSitePath(cntPage),
                getNoEditReason(cms, cntPage),
                CmsRequestUtil.encodeParams(request),
                CmsADEManager.PATH_SITEMAP_EDITOR_JSP,
                cntPageUri,
                CmsDetailPageResourceHandler.getDetailId(getRequest()),
                getNewTypes(cms, request),
                lastModified,
                cms.getRequestContext().getLocale().toString());
        } catch (Throwable e) {
            error(e);
        }
        return data;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveContainerpage(org.opencms.util.CmsUUID, java.util.List, java.lang.String)
     */
    public void saveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers, String locale)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource containerpage = cms.readResource(pageStructureId);
            String containerpageUri = cms.getSitePath(containerpage);
            Locale contentLocale = CmsLocaleManager.getLocale(locale);
            List<CmsContainerBean> containerBeans = new ArrayList<CmsContainerBean>();
            for (CmsContainer container : containers) {
                CmsContainerBean containerBean = getContainerBean(container, containerpage, locale);
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveGroupContainer(org.opencms.util.CmsUUID, java.lang.String, org.opencms.ade.containerpage.shared.CmsGroupContainer, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> saveGroupContainer(
        CmsUUID pageStructureId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            internalSaveGroupContainer(cms, pageStructureId, groupContainer, locale);
        } catch (Throwable e) {
            error(e);
        }
        Collection<String> ids = new ArrayList<String>();
        ids.add(groupContainer.getClientId());
        return getElementsData(pageStructureId, reqParams, ids, containers, locale);
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#syncSaveContainerpage(org.opencms.util.CmsUUID, java.util.List, java.lang.String)
     */
    public void syncSaveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers, String locale)
    throws CmsRpcException {

        saveContainerpage(pageStructureId, containers, locale);
    }

    /**
     * Converts the given setting values according to the setting configuration of the given resource.<p>
     * 
     * @param resource the resource
     * @param settings the settings to convert
     * 
     * @return the converted settings
     * @throws CmsException if something goes wrong 
     */
    private Map<String, String> convertSettingValues(CmsResource resource, Map<String, String> settings)
    throws CmsException {

        CmsObject cms = getCmsObject();
        Map<String, CmsXmlContentProperty> settingsConf = OpenCms.getADEManager().getElementSettings(cms, resource);
        Map<String, String> changedSettings = new HashMap<String, String>();
        if (settings != null) {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String settingName = entry.getKey();
                String settingType = settingsConf.get(settingName).getType();
                changedSettings.put(
                    settingName,
                    CmsXmlContentPropertyHelper.getPropValueIds(getCmsObject(), settingType, entry.getValue()));
            }
        }
        return changedSettings;
    }

    /**
     * Generates the model resource data list.<p>
     * 
     * @param resourceType the resource type name
     * @param modelResources the model resource
     * @param contentLocale the content locale
     * 
     * @return the model resources data
     * 
     * @throws CmsException if something goes wrong reading the resource information
     */
    private List<CmsModelResourceInfo> generateModelResourceList(
        String resourceType,
        List<CmsResource> modelResources,
        Locale contentLocale) throws CmsException {

        List<CmsModelResourceInfo> result = new ArrayList<CmsModelResourceInfo>();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        CmsModelResourceInfo defaultInfo = new CmsModelResourceInfo(Messages.get().getBundle(wpLocale).key(
            Messages.GUI_TITLE_DEFAULT_RESOURCE_CONTENT_0), Messages.get().getBundle(wpLocale).key(
            Messages.GUI_DESCRIPTION_DEFAULT_RESOURCE_CONTENT_0), null);
        defaultInfo.setResourceType(resourceType);
        result.add(defaultInfo);
        for (CmsResource model : modelResources) {
            CmsGallerySearchResult searchInfo = CmsGallerySearch.searchById(
                getCmsObject(),
                model.getStructureId(),
                contentLocale);
            CmsModelResourceInfo modelInfo = new CmsModelResourceInfo(
                searchInfo.getTitle(),
                searchInfo.getDescription(),
                null);
            modelInfo.addAdditionalInfo(
                Messages.get().getBundle(wpLocale).key(Messages.GUI_LABEL_PATH_0),
                getCmsObject().getSitePath(model));
            modelInfo.setResourceType(resourceType);
            modelInfo.setStructureId(model.getStructureId());
            result.add(modelInfo);
        }
        return result;
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
        element = new CmsContainerElementBean(convertToServerId(id), null, null, false);
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
     * @param containerpage the container page resource 
     * @param locale the locale to use  
     *  
     * @return a container bean
     */
    private CmsContainerBean getContainerBean(CmsContainer container, CmsResource containerpage, String locale) {

        CmsObject cms = getCmsObject();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : container.getElements()) {
            try {
                if (elementData.isNew()) {

                    if (CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(elementData.getResourceType())) {
                        CmsGroupContainer groupContainer = new CmsGroupContainer();
                        groupContainer.setNew(true);
                        groupContainer.setElements(new ArrayList<CmsContainerElement>());
                        Set<String> types = new HashSet<String>();
                        types.add(container.getType());
                        groupContainer.setTypes(types);
                        elementData = internalSaveGroupContainer(
                            cms,
                            containerpage.getStructureId(),
                            groupContainer,
                            locale);

                    } else {
                        elementData = createNewElement(
                            containerpage.getStructureId(),
                            elementData.getClientId(),
                            elementData.getResourceType(),
                            null,
                            locale);
                    }
                }
                CmsContainerElementBean element = getCachedElement(elementData.getClientId());

                // make sure resource is readable, 
                CmsResource resource = cms.readResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION);

                // check if there is a valid formatter
                int containerWidth = container.getWidth();

                CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, containerpage.getRootPath());
                CmsFormatterConfiguration formatters = config.getFormatters(cms, resource);
                String containerType = null;
                if (CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(OpenCms.getResourceManager().getResourceType(
                    resource).getTypeName())) {
                    // always reference the preview formatter for group containers
                    containerType = CmsFormatterBean.PREVIEW_TYPE;
                } else {
                    containerType = container.getType();
                }

                CmsFormatterBean formatter = formatters.getFormatter(containerType, containerWidth);
                if (formatter != null) {
                    elements.add(new CmsContainerElementBean(
                        element.getId(),
                        formatter.getJspStructureId(),
                        element.getIndividualSettings(),
                        false));
                }
            } catch (Exception e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        CmsContainerBean result = new CmsContainerBean(container.getName(), container.getType(), elements);
        return result;
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
     * @param locale the locale to use 
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
                CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(cms, locale);

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
     * @param pageStructureId the container page structure id  
     * @param locale the locale to use 
     * 
     * @return the group-container bean
     */
    private CmsGroupContainerBean getGroupContainerBean(
        CmsGroupContainer groupContainer,
        CmsUUID pageStructureId,
        String locale) {

        CmsObject cms = getCmsObject();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : groupContainer.getElements()) {
            try {
                if (elementData.isNew()) {
                    elementData = createNewElement(
                        pageStructureId,
                        elementData.getClientId(),
                        elementData.getResourceType(),
                        null,
                        locale);
                }
                CmsContainerElementBean element = getCachedElement(elementData.getClientId());

                // make sure resource is readable, 
                if (cms.existsResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION)) {
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
     * @param locale the locale to use 
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
            }
        }
        return result;
    }

    /**
     * Returns the element data for a new element not existing in the VFS yet.<p>
     * 
     * @param resourceTypeName the resource type name
     * @param uriParam the request parameters
     * @param containers the containers of the template
     * @param locale the current locale
     * 
     * @return the element data
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsContainerElementData getNewElement(
        String resourceTypeName,
        String uriParam,
        Collection<CmsContainer> containers,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, getRequest(), getResponse(), locale);
        CmsContainerElementBean elementBean = getSessionCache().getCacheContainerElement(resourceTypeName);
        if (elementBean == null) {
            elementBean = CmsContainerElementBean.createElementForResourceType(
                cms,
                OpenCms.getResourceManager().getResourceType(resourceTypeName),
                "/",
                Collections.<String, String> emptyMap(),
                locale);
            getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);
        }
        return elemUtil.getElementData(elementBean, containers);
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

        Map<String, String> result = new LinkedHashMap<String, String>();
        CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        try {
            List<CmsResourceTypeConfig> types = configData.getCreatableTypes(cms);
            for (CmsResourceTypeConfig type : types) {
                result.put(type.getTypeName(), CmsUUID.getNullUUID().toString());
            }
            return result;

        } catch (CmsException e) {
            error(e);
            return null;
        }
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
     * Internal method for saving a group container.<p>
     * 
     * @param cms the cms context 
     * @param pageStructureId the container page structure id 
     * @param groupContainer the group container to save 
     * @param locale the locale for which the group container should be saved 
     * 
     * @return the container element representing the group container
     *  
     * @throws CmsException if something goes wrong 
     * @throws CmsXmlException if the XML processing goes wrong 
     */
    private CmsContainerElement internalSaveGroupContainer(
        CmsObject cms,
        CmsUUID pageStructureId,
        CmsGroupContainer groupContainer,
        String locale) throws CmsException, CmsXmlException {

        ensureSession();
        CmsResource pageResource = getCmsObject().readResource(pageStructureId, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsResource groupContainerResource = null;
        if (groupContainer.isNew()) {
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                getCmsObject(),
                pageResource.getRootPath());
            CmsResourceTypeConfig typeConfig = config.getResourceType(CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME);
            groupContainerResource = typeConfig.createNewElement(getCmsObject());
            String resourceName = cms.getSitePath(groupContainerResource);
            groupContainer.setSitePath(resourceName);
            groupContainer.setClientId(groupContainerResource.getStructureId().toString());
        }
        if (groupContainerResource == null) {
            CmsUUID id = convertToServerId(groupContainer.getClientId());
            groupContainerResource = cms.readResource(id, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        }
        CmsGroupContainerBean groupContainerBean = getGroupContainerBean(
            groupContainer,
            pageResource.getStructureId(),
            locale);
        cms.lockResourceTemporary(groupContainerResource);
        CmsFile groupContainerFile = cms.readFile(groupContainerResource);
        CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(cms, groupContainerFile);
        xmlGroupContainer.save(cms, groupContainerBean, CmsLocaleManager.getLocale(locale));
        cms.unlockResource(groupContainerResource);

        CmsContainerElement element = new CmsContainerElement();
        element.setClientId(groupContainerFile.getStructureId().toString());
        element.setSitePath(cms.getSitePath(groupContainerFile));
        element.setResourceType(CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME);
        return element;
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
