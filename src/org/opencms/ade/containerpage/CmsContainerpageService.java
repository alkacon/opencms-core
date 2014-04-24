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
import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.configuration.CmsModelPageConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReference;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReferenceParser;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementReuseMode;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsElementViewInfo;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.ade.galleries.CmsGalleryService;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsDefaultResourceStatusProvider;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
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
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The RPC service used by the container-page editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageService extends CmsGwtService implements I_CmsContainerpageService {

    /** Additional info key for storing the "edit small elements" setting on the user. */
    public static final String ADDINFO_EDIT_SMALL_ELEMENTS = "EDIT_SMALL_ELEMENTS";

    /** Session attribute name used to store the selected clipboard tab. */
    public static final String ATTR_CLIPBOARD_TAB = "clipboardtab";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsContainerpageService.class);

    /** Serial version UID. */
    private static final long serialVersionUID = -6188370638303594280L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /** The workplace settings. */
    private CmsWorkplaceSettings m_workplaceSettings;

    /**
     * Generates the model resource data list.<p>
     * 
     * @param cms the cms context
     * @param resourceType the resource type name
     * @param modelResources the model resource
     * @param contentLocale the content locale
     * 
     * @return the model resources data
     * 
     * @throws CmsException if something goes wrong reading the resource information
     */
    public static List<CmsModelResourceInfo> generateModelResourceList(
        CmsObject cms,
        String resourceType,
        List<CmsResource> modelResources,
        Locale contentLocale) throws CmsException {

        List<CmsModelResourceInfo> result = new ArrayList<CmsModelResourceInfo>();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsModelResourceInfo defaultInfo = new CmsModelResourceInfo(Messages.get().getBundle(wpLocale).key(
            Messages.GUI_TITLE_DEFAULT_RESOURCE_CONTENT_0), Messages.get().getBundle(wpLocale).key(
            Messages.GUI_DESCRIPTION_DEFAULT_RESOURCE_CONTENT_0), null);
        defaultInfo.setResourceType(resourceType);
        result.add(defaultInfo);
        for (CmsResource model : modelResources) {
            CmsGallerySearchResult searchInfo = CmsGallerySearch.searchById(cms, model.getStructureId(), contentLocale);
            CmsModelResourceInfo modelInfo = new CmsModelResourceInfo(
                searchInfo.getTitle(),
                searchInfo.getDescription(),
                null);
            modelInfo.addAdditionalInfo(
                Messages.get().getBundle(wpLocale).key(Messages.GUI_LABEL_PATH_0),
                cms.getSitePath(model));
            modelInfo.setResourceType(resourceType);
            modelInfo.setStructureId(model.getStructureId());
            result.add(modelInfo);
        }
        return result;
    }

    /**
     * Returns serialized container data.<p>
     * 
     * @param container the container
     * 
     * @return the serialized data
     * 
     * @throws Exception if serialization fails
     */
    public static String getSerializedContainerInfo(CmsContainer container) throws Exception {

        return CmsGwtActionElement.serialize(I_CmsContainerpageService.class.getMethod("getContainerInfo"), container);
    }

    /**
     * Returns the serialized element data.<p>
     * 
     * @param cms the cms context
     * @param request the servlet request
     * @param response the servlet response
     * 
     * @param elementBean the element to serialize
     * 
     * @return the serialized element data
     * 
     * @throws Exception if something goes wrong
     */
    public static String getSerializedElementInfo(
        CmsObject cms,
        HttpServletRequest request,
        HttpServletResponse response,
        CmsContainerElementBean elementBean) throws Exception {

        CmsContainerElement result = new CmsContainerElement();
        CmsElementUtil util = new CmsElementUtil(
            cms,
            cms.getRequestContext().getUri(),
            null,
            request,
            response,
            cms.getRequestContext().getLocale());
        util.setElementInfo(elementBean, result);
        return CmsGwtActionElement.serialize(I_CmsContainerpageService.class.getMethod("getElementInfo"), result);
    }

    /**
     * Fetches the container page data.<p>
     * 
     * @param request the current request
     * 
     * @return the container page data
     * 
     * @throws CmsRpcException if something goes wrong
     */
    public static CmsCntPageData prefetch(HttpServletRequest request) throws CmsRpcException {

        CmsContainerpageService srv = new CmsContainerpageService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsCntPageData result = null;
        try {
            result = srv.prefetch();
        } finally {
            srv.clearThreadStorage();
        }
        return result;
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#checkContainerpageOrElementsChanged(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public boolean checkContainerpageOrElementsChanged(CmsUUID structureId, CmsUUID detailContentId)
    throws CmsRpcException {

        try {
            List<CmsUUID> additionalIds = new ArrayList<CmsUUID>();
            additionalIds.add(structureId);
            if (detailContentId != null) {
                additionalIds.add(detailContentId);
            }
            CmsRelationTargetListBean result = CmsDefaultResourceStatusProvider.getContainerpageRelationTargets(
                getCmsObject(),
                structureId,
                additionalIds,
                true);
            return result.isChanged();
        } catch (Throwable e) {
            error(e);
            return false; // will never be reached 
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
                    getCmsObject(),
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#checkNewWidgetsAvailable(org.opencms.util.CmsUUID)
     */
    public boolean checkNewWidgetsAvailable(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource resource = cms.readResource(structureId);
            return CmsWorkplaceEditorManager.checkAcaciaEditorAvailable(cms, resource);
        } catch (Throwable t) {
            error(t);
        }
        return false;
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#copyElement(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsUUID copyElement(CmsUUID pageId, CmsUUID originalElementId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource page = cms.readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsResource element = cms.readResource(originalElementId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, page.getRootPath());
            String typeName = OpenCms.getResourceManager().getResourceType(element.getTypeId()).getTypeName();
            CmsResourceTypeConfig typeConfig = config.getResourceType(typeName);
            if (typeConfig == null) {
                LOG.error("copyElement: Type not configured in ADE configuration: " + typeName);
                return originalElementId;
            } else {
                CmsResource newResource = typeConfig.createNewElement(cms, element);
                return newResource.getStructureId();
            }
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached 
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
            element.setNewEditorDisabled(!CmsWorkplaceEditorManager.checkAcaciaEditorAvailable(cms, newResource));
            element.setClientId(newClientId);
            element.setSitePath(cms.getSitePath(newResource));
            element.setResourceType(resourceType);
        } catch (CmsException e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getContainerInfo()
     */
    public CmsContainer getContainerInfo() {

        throw new UnsupportedOperationException("This method is used for serialization only.");
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementInfo()
     */
    public CmsContainerElement getElementInfo() {

        throw new UnsupportedOperationException("This method is used for serialization only.");
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, java.util.Collection, java.util.Collection, boolean, java.lang.String)
     */
    public Map<String, CmsContainerElementData> getElementsData(
        CmsContainerPageRpcContext context,

        CmsUUID detailContentId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale) throws CmsRpcException {

        Map<String, CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource pageResource = getCmsObject().readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = getCmsObject().getSitePath(pageResource);
            result = getElements(
                pageResource,
                clientIds,
                containerpageUri,
                detailContentId,
                containers,
                allowNested,
                CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementWithSettings(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.util.Map, java.util.Collection, boolean, java.lang.String)
     */
    public CmsContainerElementData getElementWithSettings(
        CmsContainerPageRpcContext context,

        CmsUUID detailContentId,
        String uriParams,
        String clientId,
        Map<String, String> settings,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale) throws CmsRpcException {

        CmsContainerElementData element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = cms.getSitePath(pageResource);
            Locale contentLocale = CmsLocaleManager.getLocale(locale);
            CmsElementUtil elemUtil = new CmsElementUtil(cms, containerpageUri, generateContainerPageForContainers(
                containers,
                pageResource.getRootPath()), detailContentId, getRequest(), getResponse(), contentLocale);

            CmsContainerElementBean elementBean = getCachedElement(clientId);
            elementBean.initResource(cms);

            // make sure to keep the element instance id
            if (!settings.containsKey(CmsContainerElementBean.ELEMENT_INSTANCE_ID)
                && elementBean.getIndividualSettings().containsKey(CmsContainerElementBean.ELEMENT_INSTANCE_ID)) {
                settings.put(
                    CmsContainerElementBean.ELEMENT_INSTANCE_ID,
                    elementBean.getIndividualSettings().get(CmsContainerElementBean.ELEMENT_INSTANCE_ID));
            }

            elementBean = CmsContainerElementBean.cloneWithSettings(
                elementBean,
                convertSettingValues(elementBean.getResource(), settings, contentLocale));
            getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);
            element = elemUtil.getElementData(pageResource, elementBean, containers, allowNested);
        } catch (Throwable e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getFavoriteList(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.util.Collection, boolean, java.lang.String)
     */
    public List<CmsContainerElementData> getFavoriteList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource containerpage = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(containerpage);
            result = getListElementsData(
                OpenCms.getADEManager().getFavoriteList(getCmsObject()),
                containerpageUri,
                detailContentId,
                containers,
                allowNested,
                CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getGalleryDataForPage(java.util.List, org.opencms.util.CmsUUID, java.lang.String, java.lang.String)
     */
    public CmsGalleryDataBean getGalleryDataForPage(
        List<CmsContainer> containers,
        CmsUUID elementView,
        String uri,
        String locale) throws CmsRpcException {

        CmsGalleryDataBean data = null;
        try {

            CmsObject cms = getCmsObject();

            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(uri));
            List<I_CmsResourceType> resourceTypes = new ArrayList<I_CmsResourceType>();
            List<String> disabledTypes = new ArrayList<String>();
            for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
                if (typeConfig.isAddDisabled() || !elementView.equals(typeConfig.getElementView())) {
                    continue;
                }
                if (typeConfig.checkViewable(cms, uri)) {
                    String typeName = typeConfig.getTypeName();
                    I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(typeName);
                    resourceTypes.add(resType);
                    if (!config.hasFormatters(cms, resType, containers)) {
                        disabledTypes.add(typeName);
                    }
                }
            }
            List<String> creatableTypes = new ArrayList<String>();
            for (CmsResourceTypeConfig typeConfig : config.getCreatableTypes(getCmsObject())) {
                if (typeConfig.isAddDisabled()
                    || !elementView.equals(typeConfig.getElementView())
                    || disabledTypes.contains(typeConfig.getTypeName())) {
                    continue;
                }
                String typeName = typeConfig.getTypeName();
                creatableTypes.add(typeName);
            }

            CmsGalleryService srv = new CmsGalleryService();
            srv.setCms(cms);
            srv.setRequest(getRequest());
            data = srv.getInitialSettingsForContainerPage(resourceTypes, creatableTypes, disabledTypes, uri, locale);

        } catch (Exception e) {
            error(e);
        }
        return data;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getNewElementData(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.util.Collection, boolean, java.lang.String)
     */
    public CmsContainerElementData getNewElementData(
        CmsContainerPageRpcContext context,

        CmsUUID detailContentId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String localeName) throws CmsRpcException {

        CmsContainerElementData result = null;
        try {
            ensureSession();
            CmsResource pageResource = getCmsObject().readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = getCmsObject().getSitePath(pageResource);
            Locale locale = CmsLocaleManager.getLocale(localeName);
            result = getNewElement(resourceType, containerpageUri, detailContentId, containers, allowNested, locale);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getRecentList(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.util.Collection, boolean, java.lang.String)
     */
    public List<CmsContainerElementData> getRecentList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale) throws CmsRpcException {

        List<CmsContainerElementData> result = null;
        try {
            ensureSession();
            CmsResource containerpage = getCmsObject().readResource(pageStructureId);
            String containerpageUri = getCmsObject().getSitePath(containerpage);
            result = getListElementsData(
                OpenCms.getADEManager().getRecentList(getCmsObject()),
                containerpageUri,
                detailContentId,
                containers,
                allowNested,
                CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getRemovedElementStatus(java.lang.String, org.opencms.util.CmsUUID)
     */
    public CmsRemovedElementStatus getRemovedElementStatus(String id, CmsUUID containerpageId) throws CmsRpcException {

        if ((id == null) || !id.matches(CmsUUID.UUID_REGEX + ".*$")) {
            return new CmsRemovedElementStatus(null, null, false);
        }
        try {
            CmsUUID structureId = convertToServerId(id);
            return internalGetRemovedElementStatus(structureId, containerpageId);
        } catch (CmsException e) {
            error(e);
            return null;
        }
    }

    /**
     * Internal helper method to get the status of a removed element.<p>
     * 
     * @param structureId the structure id of the removed element
     * @param containerpageId the id of the page to exclude from the relation check, or null if no page should be excluded  
     * 
     * @return the status of the removed element
     *   
     * @throws CmsException in case reading the resource fails
     */
    public CmsRemovedElementStatus internalGetRemovedElementStatus(CmsUUID structureId, CmsUUID containerpageId)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource elementResource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        boolean hasWritePermissions = cms.hasPermissions(
            elementResource,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            CmsResourceFilter.ALL);
        boolean isSystemResource = elementResource.getRootPath().startsWith(CmsResource.VFS_FOLDER_SYSTEM + "/");
        CmsRelationFilter relationFilter = CmsRelationFilter.relationsToStructureId(structureId);
        List<CmsRelation> relationsToElement = cms.readRelations(relationFilter);
        Iterator<CmsRelation> iter = relationsToElement.iterator();

        // ignore XML_STRONG (i.e. container element) relations from the container page, this must be checked on the client side.
        while (iter.hasNext()) {
            CmsRelation relation = iter.next();
            if ((containerpageId != null)
                && containerpageId.equals(relation.getSourceId())
                && relation.getType().equals(CmsRelationType.XML_STRONG)) {
                iter.remove();
            }
        }
        boolean hasNoRelations = relationsToElement.isEmpty();
        boolean deletionCandidate = hasNoRelations && hasWritePermissions && !isSystemResource;
        CmsListInfoBean elementInfo = CmsVfsService.getPageInfo(cms, elementResource);
        return new CmsRemovedElementStatus(structureId, elementInfo, deletionCandidate);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#loadClipboardTab()
     */
    public int loadClipboardTab() {

        Integer clipboardTab = (Integer)(getRequest().getSession().getAttribute(ATTR_CLIPBOARD_TAB));
        if (clipboardTab == null) {
            clipboardTab = Integer.valueOf(0);
        }
        return clipboardTab.intValue();
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#prefetch()
     */
    public CmsCntPageData prefetch() throws CmsRpcException {

        CmsCntPageData data = null;
        CmsObject cms = getCmsObject();

        HttpServletRequest request = getRequest();
        try {
            CmsTemplateContextInfo info = OpenCms.getTemplateContextManager().getContextInfoBean(cms, request);
            CmsResource containerPage = getContainerpage(cms);
            boolean isModelPage = isModelPage(cms, containerPage);

            TemplateBean templateBean = (TemplateBean)getRequest().getAttribute(
                CmsTemplateContextManager.ATTR_TEMPLATE_BEAN);
            CmsADESessionCache sessionCache = CmsADESessionCache.getCache(getRequest(), cms);
            sessionCache.setTemplateBean(containerPage.getRootPath(), templateBean);
            long lastModified = containerPage.getDateLastModified();
            String editorUri = OpenCms.getWorkplaceManager().getEditorHandler().getEditorUri(
                cms,
                "xmlcontent",
                "User agent",
                false);
            boolean useClassicEditor = (editorUri == null) || !editorUri.contains("acacia");
            CmsResource detailResource = CmsDetailPageResourceHandler.getDetailResource(request);
            String noEditReason;
            String detailContainerPage = null;
            if (detailResource != null) {
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(detailResource.getRootPath());
                if (rootCms.existsResource(detailContainerPage)) {
                    noEditReason = getNoEditReason(rootCms, rootCms.readResource(detailContainerPage));
                } else {
                    String permissionFolder = CmsResource.getFolderPath(detailContainerPage);
                    if (!rootCms.existsResource(permissionFolder)) {
                        permissionFolder = CmsResource.getParentFolder(permissionFolder);
                    }
                    noEditReason = getNoEditReason(rootCms, rootCms.readResource(permissionFolder));
                }
            } else {
                noEditReason = getNoEditReason(cms, containerPage);
            }

            String sitemapPath = "";
            boolean sitemapManager = OpenCms.getRoleManager().hasRole(cms, CmsRole.EDITOR);
            if (sitemapManager) {
                sitemapPath = CmsADEManager.PATH_SITEMAP_EDITOR_JSP;
            }
            CmsCntPageData.ElementReuseMode reuseMode = ElementReuseMode.reuse;
            String reuseModeString = getWorkplaceSettings().getUserSettings().getAdditionalPreference(
                "elementReuseMode",
                true);

            try {
                reuseMode = ElementReuseMode.valueOf(reuseModeString);
            } catch (Exception e) {
                LOG.info("Invalid reuse mode : " + reuseModeString);
            }

            data = new CmsCntPageData(
                noEditReason,
                CmsRequestUtil.encodeParams(request),
                sitemapPath,
                sitemapManager,
                detailResource != null ? detailResource.getStructureId() : null,
                detailContainerPage,
                lastModified,
                getLockInfo(containerPage),
                cms.getRequestContext().getLocale().toString(),
                useClassicEditor,
                info,
                isEditSmallElements(request, cms),
                Lists.newArrayList(getElementViews().values()),
                getSessionCache().getElementView(),
                reuseMode,
                isModelPage);
        } catch (Throwable e) {
            error(e);
        }
        return data;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveClipboardTab(int)
     */
    public void saveClipboardTab(int tabIndex) {

        getRequest().getSession().setAttribute(ATTR_CLIPBOARD_TAB, new Integer(tabIndex));
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveContainerpage(org.opencms.util.CmsUUID, java.util.List)
     */
    public void saveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource containerpage = cms.readResource(pageStructureId);
            ensureLock(containerpage);
            String containerpageUri = cms.getSitePath(containerpage);
            saveContainers(cms, containerpage, containerpageUri, containers);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveDetailContainers(java.lang.String, java.util.List)
     */
    public void saveDetailContainers(String detailContainerResource, List<CmsContainer> containers)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource containerpage;
            ensureSession();
            if (rootCms.existsResource(detailContainerResource)) {
                containerpage = rootCms.readResource(detailContainerResource);
            } else {
                String parentFolder = CmsResource.getFolderPath(detailContainerResource);
                // ensure the parent folder exists
                if (!rootCms.existsResource(parentFolder)) {
                    CmsResource parentRes = rootCms.createResource(
                        parentFolder,
                        OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId());
                    // set the search exclude property on parent folder
                    rootCms.writePropertyObject(parentFolder, new CmsProperty(
                        CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE,
                        CmsSearchIndex.PROPERTY_SEARCH_EXCLUDE_VALUE_ALL,
                        null));
                    tryUnlock(parentRes);
                }
                containerpage = rootCms.createResource(
                    detailContainerResource,
                    CmsResourceTypeXmlContainerPage.getContainerPageTypeId());
            }
            ensureLock(containerpage);
            saveContainers(rootCms, containerpage, detailContainerResource, containers);
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveGroupContainer(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, org.opencms.ade.containerpage.shared.CmsGroupContainer, java.util.Collection, java.lang.String)
     */
    public CmsGroupContainerSaveResult saveGroupContainer(
        CmsContainerPageRpcContext context,

        CmsUUID detailContentId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        List<CmsRemovedElementStatus> removedElements = null;
        try {
            CmsPair<CmsContainerElement, List<CmsRemovedElementStatus>> saveResult = internalSaveGroupContainer(
                cms,
                context.getPageStructureId(),
                groupContainer);
            removedElements = saveResult.getSecond();
        } catch (Throwable e) {
            error(e);
        }
        Collection<String> ids = new ArrayList<String>();
        ids.add(groupContainer.getClientId());
        // update offline indices
        OpenCms.getSearchManager().updateOfflineIndexes(2 * CmsSearchManager.DEFAULT_OFFLINE_UPDATE_FREQNENCY);
        return new CmsGroupContainerSaveResult(getElementsData(
            context,
            detailContentId,
            reqParams,
            ids,
            containers,
            false,
            locale), removedElements);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveInheritanceContainer(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.ade.containerpage.shared.CmsInheritanceContainer, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> saveInheritanceContainer(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        CmsInheritanceContainer inheritanceContainer,
        Collection<CmsContainer> containers,
        String locale) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource containerPage = cms.readResource(pageStructureId);
            String sitePath = cms.getSitePath(containerPage);
            Locale requestedLocale = CmsLocaleManager.getLocale(locale);
            CmsResource referenceResource = null;
            if (inheritanceContainer.isNew()) {
                CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, containerPage.getRootPath());
                CmsResourceTypeConfig typeConfig = config.getResourceType(CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME);
                referenceResource = typeConfig.createNewElement(cms);
                inheritanceContainer.setClientId(referenceResource.getStructureId().toString());
            }
            if (referenceResource == null) {
                CmsUUID id = convertToServerId(inheritanceContainer.getClientId());
                referenceResource = cms.readResource(id, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            }
            ensureLock(referenceResource);
            saveInheritanceGroup(referenceResource, inheritanceContainer);
            tryUnlock(referenceResource);
            List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
            for (CmsContainerElement clientElement : inheritanceContainer.getElements()) {
                CmsContainerElementBean elementBean = getCachedElement(clientElement.getClientId());
                elementBean = CmsContainerElementBean.cloneWithSettings(
                    elementBean,
                    elementBean.getIndividualSettings());
                CmsInheritanceInfo inheritanceInfo = clientElement.getInheritanceInfo();
                // if a local elements misses the key it was newly added
                if (inheritanceInfo.isNew() && CmsStringUtil.isEmptyOrWhitespaceOnly(inheritanceInfo.getKey())) {
                    // generating new key
                    inheritanceInfo.setKey(CmsResource.getFolderPath(sitePath) + new CmsUUID().toString());
                }
                elementBean.setInheritanceInfo(inheritanceInfo);
                elements.add(elementBean);
            }
            cms.getRequestContext().setLocale(requestedLocale);
            if (inheritanceContainer.getElementsChanged()) {
                OpenCms.getADEManager().saveInheritedContainer(
                    cms,
                    containerPage,
                    inheritanceContainer.getName(),
                    true,
                    elements);
            }
            return getElements(
                containerPage,
                new ArrayList<String>(Collections.singletonList(inheritanceContainer.getClientId())),
                sitePath,
                detailContentId,
                containers,
                false,
                requestedLocale);
        } catch (Exception e) {
            error(e);
        }
        return null;
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#setEditSmallElements(boolean)
     */
    public void setEditSmallElements(boolean editSmallElements) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsUser user = cms.getRequestContext().getCurrentUser();
            user.getAdditionalInfo().put(ADDINFO_EDIT_SMALL_ELEMENTS, "" + editSmallElements);
            cms.writeUser(user);
        } catch (Throwable t) {
            error(t);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#setElementView(org.opencms.util.CmsUUID)
     */
    public void setElementView(CmsUUID elementView) {

        getSessionCache().setElementView(elementView);
    }

    /**
     * Sets the session cache.<p>
     * 
     * @param cache the session cache 
     */
    public void setSessionCache(CmsADESessionCache cache) {

        m_sessionCache = cache;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#syncSaveContainerpage(org.opencms.util.CmsUUID, java.util.List)
     */
    public void syncSaveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers) throws CmsRpcException {

        saveContainerpage(pageStructureId, containers);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#syncSaveDetailContainers(java.lang.String, java.util.List)
     */
    public void syncSaveDetailContainers(String detailContainerResource, List<CmsContainer> containers)
    throws CmsRpcException {

        saveDetailContainers(detailContainerResource, containers);
    }

    /**
     * Converts the given setting values according to the setting configuration of the given resource.<p>
     * 
     * @param resource the resource
     * @param settings the settings to convert
     * @param locale the locale used for accessing the element settings 
     * 
     * @return the converted settings
     * @throws CmsException if something goes wrong 
     */
    private Map<String, String> convertSettingValues(CmsResource resource, Map<String, String> settings, Locale locale)
    throws CmsException {

        CmsObject cms = getCmsObject();
        Locale origLocale = cms.getRequestContext().getLocale();
        try {
            cms.getRequestContext().setLocale(locale);
            Map<String, CmsXmlContentProperty> settingsConf = OpenCms.getADEManager().getElementSettings(cms, resource);
            Map<String, String> changedSettings = new HashMap<String, String>();
            if (settings != null) {
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    String settingName = entry.getKey();
                    String settingType = "string";
                    if (settingsConf.get(settingName) != null) {
                        settingType = settingsConf.get(settingName).getType();
                    }
                    changedSettings.put(
                        settingName,
                        CmsXmlContentPropertyHelper.getPropValueIds(getCmsObject(), settingType, entry.getValue()));
                }
            }
            return changedSettings;
        } finally {
            cms.getRequestContext().setLocale(origLocale);
        }
    }

    /**
     * Generates the XML container page bean for the given containers.<p>
     * 
     * @param containers the containers
     * @param containerpageRootPath the container page root path
     * 
     * @return the container page bean
     */
    private CmsContainerPageBean generateContainerPageForContainers(
        Collection<CmsContainer> containers,
        String containerpageRootPath) {

        List<CmsContainerBean> containerBeans = new ArrayList<CmsContainerBean>();
        for (CmsContainer container : containers) {
            CmsContainerBean containerBean = getContainerBeanToSave(container, containerpageRootPath);
            containerBeans.add(containerBean);
        }
        CmsContainerPageBean page = new CmsContainerPageBean(containerBeans);
        return page;
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
        getSessionCache().setCacheContainerElement(element.editorHash(), element);
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
     * @param containerpageRootPath the container page root path 
     *  
     * @return a container bean
     */
    private CmsContainerBean getContainerBeanToSave(CmsContainer container, String containerpageRootPath) {

        CmsObject cms = getCmsObject();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : container.getElements()) {
            try {
                CmsContainerElementBean newElementBean = getContainerElementBeanToSave(
                    cms,
                    containerpageRootPath,
                    container,
                    elementData);
                if (newElementBean != null) {
                    elements.add(newElementBean);
                }
            } catch (Exception e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        CmsContainerBean result = new CmsContainerBean(
            container.getName(),
            container.getType(),
            container.getParentInstanceId(),
            elements);
        return result;
    }

    /**
     * Converts container page element data to a bean which can be saved in a container page.<p>
     * 
     * @param cms the current CMS context 
     * @param containerpageRootPath the container page root path
     * @param container the container containing the element 
     * @param elementData the data for the single element 
     * 
     * @return the container element bean 
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsContainerElementBean getContainerElementBeanToSave(
        CmsObject cms,
        String containerpageRootPath,
        CmsContainer container,
        CmsContainerElement elementData) throws CmsException {

        String elementClientId = elementData.getClientId();
        boolean hasUuidPrefix = (elementClientId != null) && elementClientId.matches(CmsUUID.UUID_REGEX + ".*$");
        boolean isCreateNew = elementData.isNew() && hasUuidPrefix;
        if (elementData.isNew() && !hasUuidPrefix) {

            // Due to the changed save system without the save button, we need to make sure that new elements 
            // are only created once. This must happen when the user first edits a new element. But we still 
            // want to save changes to non-new elements on the page, so we skip new elements while saving.
            return null;
        }
        CmsContainerElementBean element = getCachedElement(elementData.getClientId());

        // make sure resource is readable, 
        CmsResource resource = cms.readResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION);

        // check if there is a valid formatter
        int containerWidth = container.getWidth();

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, containerpageRootPath);
        CmsFormatterConfiguration formatters = config.getFormatters(cms, resource);
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        String containerType = null;

        if (CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(typeName)
            || CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME.equals(typeName)) {
            // always reference the preview formatter for group containers
            containerType = CmsFormatterBean.PREVIEW_TYPE;
        } else {
            containerType = container.getType();
        }

        I_CmsFormatterBean formatter = null;
        if ((element.getSettings() != null)
            && element.getSettings().containsKey(CmsFormatterConfig.getSettingsKeyForContainer(container.getName()))) {
            String formatterConfigId = element.getSettings().get(
                CmsFormatterConfig.getSettingsKeyForContainer(container.getName()));
            if (CmsUUID.isValidUUID(formatterConfigId)) {
                formatter = OpenCms.getADEManager().getCachedFormatters(false).getFormatters().get(
                    new CmsUUID(formatterConfigId));
            }
            if (formatter == null) {
                formatter = formatters.getDefaultSchemaFormatter(containerType, containerWidth);
            }
        }
        if (formatter == null) {
            formatter = formatters.getDefaultFormatter(containerType, containerWidth, true);
        }
        CmsContainerElementBean newElementBean = null;
        if (formatter != null) {
            Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
            settings.put(CmsFormatterConfig.getSettingsKeyForContainer(container.getName()), formatter.getId());
            newElementBean = new CmsContainerElementBean(
                element.getId(),
                formatter.getJspStructureId(),
                settings,
                isCreateNew);
        }
        return newElementBean;
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
     * @param page the current container page 
     * @param clientIds the list of IDs of the elements to retrieve the data for
     * @param uriParam the current URI
     * @param detailContentId the detail content structure id
     * @param containers the containers for which the element data should be fetched 
     * @param allowNested if nested containers are allowed
     * @param locale the locale to use 
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private Map<String, CmsContainerElementData> getElements(
        CmsResource page,
        Collection<String> clientIds,
        String uriParam,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, generateContainerPageForContainers(
            containers,
            cms.getRequestContext().addSiteRoot(uriParam)), detailContentId, getRequest(), getResponse(), locale);
        Map<String, CmsContainerElementData> result = new HashMap<String, CmsContainerElementData>();
        Set<String> ids = new HashSet<String>();
        for (String elemId : clientIds) {
            if ((elemId == null) || ids.contains(elemId)) {
                continue;
            }
            CmsContainerElementBean element = getCachedElement(elemId);
            if (element.getInstanceId() == null) {
                element = element.clone();
                getSessionCache().setCacheContainerElement(element.editorHash(), element);
            }
            CmsContainerElementData elementData = elemUtil.getElementData(page, element, containers, allowNested);
            if (elementData == null) {
                continue;
            }
            result.put(elemId, elementData);
            if (elementData.isGroupContainer() || elementData.isInheritContainer()) {
                // this is a group-container 
                CmsResource elementRes = cms.readResource(element.getId());
                List<CmsContainerElementBean> subElements = elementData.isGroupContainer()
                ? getGroupContainerElements(elementRes)
                : getInheritedElements(elementRes, locale, uriParam);
                // adding all sub-items to the elements data
                for (CmsContainerElementBean subElement : subElements) {
                    getSessionCache().setCacheContainerElement(subElement.editorHash(), subElement);
                    if (!ids.contains(subElement.editorHash())) {
                        CmsContainerElementData subItemData = elemUtil.getElementData(
                            page,
                            subElement,
                            containers,
                            allowNested);
                        ids.add(subElement.editorHash());
                        result.put(subElement.editorHash(), subItemData);
                    }
                }
            }
            ids.add(elemId);
        }
        return result;
    }

    /**
     * Returns the available element views.<p>
     * 
     * @return the element views
     */
    private Map<CmsUUID, CmsElementViewInfo> getElementViews() {

        Map<CmsUUID, CmsElementViewInfo> result = new LinkedHashMap<CmsUUID, CmsElementViewInfo>();
        CmsObject cms = getCmsObject();

        // collect the actually used element view ids
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri()));
        Set<CmsUUID> usedIds = new HashSet<CmsUUID>();
        for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
            usedIds.add(typeConfig.getElementView());
        }

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        for (CmsElementView view : OpenCms.getADEManager().getElementViews(cms).values()) {
            // add only element view that are used within the type configuration and the user has sufficient permissions for
            if (usedIds.contains(view.getId()) && view.hasPermission(cms)) {
                result.put(view.getId(), new CmsElementViewInfo(view.getTitle(cms, wpLocale), view.getId()));
            }
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
     * Returns the sub-elements of this group container resource.<p>
     * 
     * @param resource the group container resource
     * 
     * @return the sub-elements
     * 
     * @throws CmsException if something goes wrong reading the resource
     */
    private List<CmsContainerElementBean> getGroupContainerElements(CmsResource resource) throws CmsException {

        CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
            getCmsObject(),
            resource,
            getRequest());
        CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(getCmsObject());
        return groupContainer.getElements();
    }

    /**
     * Gets the structure ids of group container elements from an unmarshalled group container for a single locale.<p>
     * 
     * @param groupContainer the group container 
     * @param locale the locale for which we want the element ids 
     * 
     * @return the group container's element ids for the given locale 
     */
    private Set<CmsUUID> getGroupElementIds(CmsXmlGroupContainer groupContainer, Locale locale) {

        Set<CmsUUID> idSet = new HashSet<CmsUUID>();
        CmsGroupContainerBean groupContainerBean = groupContainer.getGroupContainer(getCmsObject());
        if (groupContainerBean != null) {
            for (CmsContainerElementBean element : groupContainerBean.getElements()) {
                idSet.add(element.getId());
            }
        }
        return idSet;

    }

    /**
     * Returns the sub-elements of this inherit container resource.<p>
     * 
     * @param resource the inherit container resource
     * @param locale the requested locale
     * @param uriParam the current URI
     * 
     * @return the sub-elements
     * 
     * @throws CmsException if something goes wrong reading the resource
     */
    private List<CmsContainerElementBean> getInheritedElements(CmsResource resource, Locale locale, String uriParam)
    throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(locale);
        CmsInheritanceReferenceParser parser = new CmsInheritanceReferenceParser(cms);
        parser.parse(resource);
        CmsInheritanceReference ref = parser.getReference(locale);
        if (ref == null) {
            // new inheritance reference, return an empty list
            return Collections.emptyList();
        }
        String name = ref.getName();
        CmsADEManager adeManager = OpenCms.getADEManager();
        CmsInheritedContainerState result = adeManager.getInheritedContainerState(cms, cms.addSiteRoot(uriParam), name);
        return result.getElements(true);
    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param listElements the list of element beans to retrieve the data for
     * @param containerpageUri the current URI
     * @param detailContentId the detail content structure id
     * @param containers the containers which exist on the container page  
     * @param allowNested if nested containers are allowed
     * @param locale the locale to use 
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private List<CmsContainerElementData> getListElementsData(
        List<CmsContainerElementBean> listElements,
        String containerpageUri,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            containerpageUri,
            generateContainerPageForContainers(containers, cms.getRequestContext().addSiteRoot(containerpageUri)),
            detailContentId,
            getRequest(),
            getResponse(),
            locale);
        CmsADESessionCache cache = getSessionCache();
        List<CmsContainerElementData> result = new ArrayList<CmsContainerElementData>();
        for (CmsContainerElementBean element : listElements) {
            // checking if resource exists
            if (cms.existsResource(element.getId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                cache.setCacheContainerElement(element.editorHash(), element);
                CmsContainerElementData elementData = elemUtil.getElementData(
                    elemUtil.getPage(),
                    element,
                    containers,
                    allowNested);
                result.add(elementData);
            }
        }
        return result;
    }

    /**
     * Returns the lock information to the given resource.<p>
     * 
     * @param resource the resource
     * 
     * @return lock information, if the page is locked by another user
     * 
     * @throws CmsException if something goes wrong reading the lock owner user
     */
    private String getLockInfo(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResourceUtil resourceUtil = new CmsResourceUtil(cms, resource);
        CmsLock lock = resourceUtil.getLock();
        String lockInfo = null;
        if (!lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
            if (lock.getType() == CmsLockType.PUBLISH) {
                lockInfo = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                    Messages.GUI_LOCKED_FOR_PUBLISH_0);
            } else {
                CmsUser lockOwner = cms.readUser(lock.getUserId());
                lockInfo = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                    Messages.GUI_LOCKED_BY_1,
                    lockOwner.getFullName());
            }
        }
        return lockInfo;
    }

    /**
     * Returns the element data for a new element not existing in the VFS yet.<p>
     * 
     * @param resourceTypeName the resource type name
     * @param uriParam the request parameters
     * @param detailContentId the detail content structure id
     * @param containers the containers of the template
     * @param allowNested if nested containers are allowed
     * @param locale the current locale
     * 
     * @return the element data
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsContainerElementData getNewElement(
        String resourceTypeName,
        String uriParam,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        Locale locale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, generateContainerPageForContainers(
            containers,
            cms.getRequestContext().addSiteRoot(uriParam)), detailContentId, getRequest(), getResponse(), locale);
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
        return elemUtil.getElementData(elemUtil.getPage(), elementBean, containers, allowNested);
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
            m_sessionCache = CmsADESessionCache.getCache(getRequest(), getCmsObject());
        }
        return m_sessionCache;
    }

    /**
     * Returns the workplace settings of the current user.<p>
     * 
     * @return the workplace settings
     */
    private CmsWorkplaceSettings getWorkplaceSettings() {

        if (m_workplaceSettings == null) {
            m_workplaceSettings = CmsWorkplace.getWorkplaceSettings(getCmsObject(), getRequest());
        }
        return m_workplaceSettings;
    }

    /**
     * Initializes request attributes using data from the RPC context.<p>
     * 
     * @param context the RPC context 
     */
    private void initRequestFromRpcContext(CmsContainerPageRpcContext context) {

        if (context.getTemplateContext() != null) {
            getRequest().setAttribute(CmsTemplateContextManager.ATTR_RPC_CONTEXT_OVERRIDE, context.getTemplateContext());
        }
    }

    /**
     * Internal method for saving a group container.<p>
     * 
     * @param cms the cms context 
     * @param pageStructureId the container page structure id 
     * @param groupContainer the group container to save 
     * 
     * @return the container element representing the group container
     *  
     * @throws CmsException if something goes wrong 
     * @throws CmsXmlException if the XML processing goes wrong 
     */
    private CmsPair<CmsContainerElement, List<CmsRemovedElementStatus>> internalSaveGroupContainer(
        CmsObject cms,
        CmsUUID pageStructureId,
        CmsGroupContainer groupContainer) throws CmsException, CmsXmlException {

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
            Locale.ENGLISH.toString());

        cms.lockResourceTemporary(groupContainerResource);
        CmsFile groupContainerFile = cms.readFile(groupContainerResource);
        Locale locale = Locale.ENGLISH;
        CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(cms, groupContainerFile);
        Set<CmsUUID> oldElementIds = getGroupElementIds(xmlGroupContainer, locale);
        xmlGroupContainer.clearLocales();
        xmlGroupContainer.save(cms, groupContainerBean, locale);
        cms.unlockResource(groupContainerResource);
        Set<CmsUUID> newElementIds = getGroupElementIds(xmlGroupContainer, locale);
        Set<CmsUUID> removedElementIds = Sets.difference(oldElementIds, newElementIds);
        List<CmsRemovedElementStatus> deletionCandidateStatuses = new ArrayList<CmsRemovedElementStatus>();
        for (CmsUUID removedId : removedElementIds) {
            CmsRemovedElementStatus status = internalGetRemovedElementStatus(removedId, null);
            if (status.isDeletionCandidate()) {
                deletionCandidateStatuses.add(status);
            }
        }
        CmsContainerElement element = new CmsContainerElement();
        element.setClientId(groupContainerFile.getStructureId().toString());
        element.setSitePath(cms.getSitePath(groupContainerFile));
        element.setResourceType(CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME);
        return CmsPair.create(element, deletionCandidateStatuses);
    }

    /** 
     * Checks if small elements in a container page should be initially editable.<p>
     * 
     * @param request the current request 
     * @param cms the current CMS context 
     * @return true if small elements should be initially editable 
     */
    private boolean isEditSmallElements(HttpServletRequest request, CmsObject cms) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        String editSmallElementsStr = (String)(user.getAdditionalInfo().get(ADDINFO_EDIT_SMALL_ELEMENTS));
        if (editSmallElementsStr == null) {
            return true;
        } else {
            return Boolean.valueOf(editSmallElementsStr).booleanValue();
        }
    }

    /**
     * Checks if a page is a model page.<p>
     * 
     * @param cms the CMS context to use 
     * @param containerPage the page to check 
     * 
     * @return true if the resource is a model page 
     */
    private boolean isModelPage(CmsObject cms, CmsResource containerPage) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, containerPage.getRootPath());
        for (CmsModelPageConfig modelConfig : config.getModelPages()) {
            if (modelConfig.getResource().getStructureId().equals(containerPage.getStructureId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the given containers to the container page resource.<p>
     * 
     * @param cms the cms context
     * @param containerpage the container page resource
     * @param containerpageUri the container page site path
     * @param containers the container to save
     * 
     * @throws CmsException if something goes wrong writing the file
     */
    private void saveContainers(
        CmsObject cms,
        CmsResource containerpage,
        String containerpageUri,
        List<CmsContainer> containers) throws CmsException {

        CmsContainerPageBean page = generateContainerPageForContainers(containers, containerpage.getRootPath());
        CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(containerpageUri));
        xmlCnt.save(cms, page);
    }

    /**
     * Saves the inheritance group.<p>
     * 
     * @param resource the inheritance group resource
     * @param inheritanceContainer the inherited group container data
     * 
     * @throws CmsException if something goes wrong
     */
    private void saveInheritanceGroup(CmsResource resource, CmsInheritanceContainer inheritanceContainer)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsFile file = cms.readFile(resource);
        CmsXmlContent document = CmsXmlContentFactory.unmarshal(cms, file);

        for (Locale docLocale : document.getLocales()) {
            document.removeLocale(docLocale);
        }
        Locale locale = Locale.ENGLISH;
        document.addLocale(cms, locale);
        document.getValue("Title", locale).setStringValue(cms, inheritanceContainer.getTitle());
        document.getValue("Description", locale).setStringValue(cms, inheritanceContainer.getDescription());
        document.getValue("ConfigName", locale).setStringValue(cms, inheritanceContainer.getName());
        byte[] content = document.marshal();
        file.setContents(content);
        cms.writeFile(file);
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

        CmsContainerElementBean element = getCachedElement(clientId).clone();
        element.removeInstanceId();
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
