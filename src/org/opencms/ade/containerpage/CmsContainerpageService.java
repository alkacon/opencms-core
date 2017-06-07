/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementReuseMode;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElement.ModelGroupState;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsContainerPageGalleryData;
import org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsElementViewInfo;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.containerpage.shared.CmsLocaleLinkBean;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.ade.galleries.CmsGalleryService;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.sitemap.CmsVfsSitemapService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
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
import org.opencms.i18n.CmsLocaleGroup;
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
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.apps.CmsQuickLaunchLocationCache;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The RPC service used by the container-page editor.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerpageService extends CmsGwtService implements I_CmsContainerpageService {

    /**
     * Helper class used to determine both the available views and the active start view when loading a container page.<p>
     */
    private class InitialElementViewProvider {

        /** Start view id. */
        private CmsUUID m_defaultView;

        /** Map of available views. */
        private Map<CmsUUID, CmsElementViewInfo> m_viewMap;

        /**
         * Empty default constructor.<p>
         */
        public InitialElementViewProvider() {

            // do nothing
        }

        /**
         * Returns the default view info.<p>
         *
         * @return the default view info
         */
        public CmsElementViewInfo getDefaultView() {

            return getViewMap().get(getDefaultViewId());
        }

        /**
         * Gets the start view id.<p>
         *
         * @return the start view id
         */
        public CmsUUID getDefaultViewId() {

            return m_defaultView;
        }

        /**
         * Gets the map of available views.<p>
         *
         * @return the map of available views
         */
        public Map<CmsUUID, CmsElementViewInfo> getViewMap() {

            return m_viewMap;
        }

        /**
         * Initializes this object.<p>
         *
         * @param defaultValue the default view id from the session cache
         * @param checkRes the resource used to check permissions
         */
        @SuppressWarnings("synthetic-access")
        public void init(CmsUUID defaultValue, CmsResource checkRes) {

            Map<CmsUUID, CmsElementViewInfo> result = new LinkedHashMap<CmsUUID, CmsElementViewInfo>();
            CmsObject cms = getCmsObject();

            // collect the actually used element view ids
            CmsADEConfigData config = getConfigData(
                cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri()));
            Set<CmsUUID> usedIds = new HashSet<CmsUUID>();
            for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
                usedIds.add(typeConfig.getElementView());
            }

            Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            Map<CmsUUID, CmsElementView> realViewMap = OpenCms.getADEManager().getElementViews(cms);

            Set<CmsUUID> parentIds = Sets.newHashSet();
            for (CmsElementView view : realViewMap.values()) {
                if (view.getParentViewId() != null) {
                    parentIds.add(view.getParentViewId());
                }
                // add only element view that are used within the type configuration and the user has sufficient permissions for
                if (usedIds.contains(view.getId()) && view.hasPermission(cms, checkRes) && !view.isOther()) {
                    result.put(view.getId(), new CmsElementViewInfo(view.getTitle(cms, wpLocale), view.getId()));
                }

            }
            m_viewMap = result;
            for (Map.Entry<CmsUUID, CmsElementViewInfo> viewEntry : m_viewMap.entrySet()) {
                CmsElementView realView = realViewMap.get(viewEntry.getKey());
                CmsUUID parentViewId = realView.getParentViewId();
                if ((parentViewId != null) && !parentIds.contains(viewEntry.getKey())) {
                    CmsElementViewInfo parentBean = m_viewMap.get(parentViewId);
                    if (parentBean != null) {
                        viewEntry.getValue().setParent(parentBean);
                    }
                }
            }
            if (m_viewMap.containsKey(defaultValue)) {
                m_defaultView = defaultValue;
            } else if (m_viewMap.containsKey(CmsElementView.DEFAULT_ELEMENT_VIEW.getId())) {
                m_defaultView = CmsElementView.DEFAULT_ELEMENT_VIEW.getId();
            } else if (!m_viewMap.isEmpty()) {
                m_defaultView = m_viewMap.values().iterator().next().getElementViewId();
            } else {
                m_defaultView = defaultValue;
                LOG.error(
                    "Initial view not available and no suitable replacement view found: user="
                        + getCmsObject().getRequestContext().getCurrentUser().getName()
                        + " view="
                        + defaultValue
                        + " path="
                        + checkRes.getRootPath());
            }

        }
    }

    /** Additional info key for storing the "edit small elements" setting on the user. */
    public static final String ADDINFO_EDIT_SMALL_ELEMENTS = "EDIT_SMALL_ELEMENTS";

    /** Session attribute name used to store the selected clipboard tab. */
    public static final String ATTR_CLIPBOARD_TAB = "clipboardtab";

    /** The model group pages path fragment. */
    public static final String MODEL_GROUP_PATH_FRAGMENT = "/.content/.modelgroups/";

    /** The source container page id settings key. */
    public static final String SOURCE_CONTAINERPAGE_ID_SETTING = "source_containerpage_id";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsContainerpageService.class);

    /** Serial version UID. */
    private static final long serialVersionUID = -6188370638303594280L;

    /** The configuration data of the current container page context. */
    private CmsADEConfigData m_configData;

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
        Locale contentLocale)
    throws CmsException {

        List<CmsModelResourceInfo> result = new ArrayList<CmsModelResourceInfo>();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsModelResourceInfo defaultInfo = new CmsModelResourceInfo(
            Messages.get().getBundle(wpLocale).key(Messages.GUI_TITLE_DEFAULT_RESOURCE_CONTENT_0),
            Messages.get().getBundle(wpLocale).key(Messages.GUI_DESCRIPTION_DEFAULT_RESOURCE_CONTENT_0),
            null);
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
     * @param elementBean the element to serialize
     * @param page the container page
     *
     * @return the serialized element data
     *
     * @throws Exception if something goes wrong
     */
    public static String getSerializedElementInfo(
        CmsObject cms,
        HttpServletRequest request,
        HttpServletResponse response,
        CmsContainerElementBean elementBean,
        CmsContainerPageBean page)
    throws Exception {

        CmsContainerElement result = new CmsContainerElement();
        CmsElementUtil util = new CmsElementUtil(
            cms,
            cms.getRequestContext().getUri(),
            page,
            null,
            request,
            response,
            false,
            cms.getRequestContext().getLocale());
        util.setElementInfo(elementBean, result);
        return CmsGwtActionElement.serialize(I_CmsContainerpageService.class.getMethod("getElementInfo"), result);
    }

    /**
     * Checks whether the current page is a model group page.<p>
     *
     * @param cms the CMS context
     * @param containerPage the current page
     *
     * @return <code>true</code> if the current page is a model group page
     */
    public static boolean isEditingModelGroups(CmsObject cms, CmsResource containerPage) {

        return (OpenCms.getResourceManager().getResourceType(containerPage).getTypeName().equals(
            CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME)
            && OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER));
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
     * Returns the server id part of the given client id.<p>
     *
     * @param id the id
     *
     * @return the server id
     */
    private static String getServerIdString(String id) {

        if (id.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
            id = id.substring(0, id.indexOf(CmsADEManager.CLIENT_ID_SEPERATOR));
        }
        return id;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToFavoriteList(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, java.lang.String)
     */
    public void addToFavoriteList(CmsContainerPageRpcContext context, String clientId) throws CmsRpcException {

        try {
            ensureSession();
            List<CmsContainerElementBean> list = OpenCms.getADEManager().getFavoriteList(getCmsObject());
            CmsResource containerPage = getCmsObject().readResource(context.getPageStructureId());
            updateFavoriteRecentList(containerPage, clientId, list);
            OpenCms.getADEManager().saveFavoriteList(getCmsObject(), list);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToRecentList(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, java.lang.String)
     */
    public void addToRecentList(CmsContainerPageRpcContext context, String clientId) throws CmsRpcException {

        try {
            ensureSession();
            List<CmsContainerElementBean> list = OpenCms.getADEManager().getRecentList(getCmsObject());
            CmsResource containerPage = getCmsObject().readResource(context.getPageStructureId());
            updateFavoriteRecentList(containerPage, clientId, list);
            OpenCms.getADEManager().saveRecentList(getCmsObject(), list);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#checkContainerpageOrElementsChanged(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String)
     */
    public boolean checkContainerpageOrElementsChanged(
        CmsUUID structureId,
        CmsUUID detailContentId,
        String contentLocale)
    throws CmsRpcException {

        try {
            List<CmsUUID> additionalIds = new ArrayList<CmsUUID>();
            additionalIds.add(structureId);
            boolean detailOnlyChanged = false;
            if (detailContentId != null) {
                additionalIds.add(detailContentId);
                try {

                    CmsObject cms = getCmsObject();
                    CmsResource detailContentRes = cms.readResource(detailContentId, CmsResourceFilter.ALL);
                    OpenCms.getLocaleManager();
                    Optional<CmsResource> detailOnlyRes = CmsJspTagContainer.getDetailOnlyPage(
                        cms,
                        detailContentRes,
                        CmsJspTagContainer.getDetailContainerLocale(cms, contentLocale, cms.readResource(structureId)));
                    if (detailOnlyRes.isPresent()) {
                        detailOnlyChanged = CmsDefaultResourceStatusProvider.getContainerpageRelationTargets(
                            getCmsObject(),
                            detailOnlyRes.get().getStructureId(),
                            Arrays.asList(detailOnlyRes.get().getStructureId()),
                            true).isChanged();
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return detailOnlyChanged
                || CmsDefaultResourceStatusProvider.getContainerpageRelationTargets(
                    getCmsObject(),
                    structureId,
                    additionalIds,
                    true /*stop looking if we find a changed resource.*/).isChanged();
        } catch (Throwable e) {
            error(e);
            return false; // will never be reached
        }

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#checkCreateNewElement(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, org.opencms.ade.containerpage.shared.CmsContainer, java.lang.String)
     */
    public CmsCreateElementData checkCreateNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsContainer container,
        String locale)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCreateElementData result = new CmsCreateElementData();
        try {
            CmsResource currentPage = cms.readResource(pageStructureId);

            List<CmsResource> modelResources = CmsResourceTypeXmlContent.getModelFiles(
                getCmsObject(),
                CmsResource.getFolderPath(cms.getSitePath(currentPage)),
                resourceType);
            if (modelResources.isEmpty()) {
                CmsContainerElementBean bean = getCachedElement(clientId, currentPage.getRootPath());
                I_CmsFormatterBean formatter = CmsElementUtil.getFormatterForContainer(
                    cms,
                    bean,
                    container,
                    getConfigData(currentPage.getRootPath()),
                    true,
                    getSessionCache());
                CmsUUID modelResId = null;
                if (formatter instanceof CmsMacroFormatterBean) {
                    modelResId = ((CmsMacroFormatterBean)formatter).getDefaultContentStructureId();
                }
                result.setCreatedElement(createNewElement(pageStructureId, clientId, resourceType, modelResId, locale));
            } else {
                result.setModelResources(
                    generateModelResourceList(
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
            throw new CmsIllegalArgumentException(
                org.opencms.xml.containerpage.Messages.get().container(
                    org.opencms.xml.containerpage.Messages.ERR_INVALID_ID_1,
                    id));
        }
        String serverId = getServerIdString(id);
        try {
            return new CmsUUID(serverId);
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(
                org.opencms.xml.containerpage.Messages.get().container(
                    org.opencms.xml.containerpage.Messages.ERR_INVALID_ID_1,
                    id),
                e);
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
                CmsResource newResource = typeConfig.createNewElement(
                    cms,
                    element,
                    CmsResource.getParentFolder(page.getRootPath()));
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
        String locale)
    throws CmsRpcException {

        CmsContainerElement element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(pageStructureId);
            CmsADEConfigData configData = getConfigData(pageResource.getRootPath());
            CmsResourceTypeConfig typeConfig = configData.getResourceType(resourceType);
            CmsObject cloneCms = OpenCms.initCmsObject(cms);
            cloneCms.getRequestContext().setLocale(CmsLocaleManager.getLocale(locale));

            CmsResource modelResource = null;
            if (modelResourceStructureId != null) {
                modelResource = cms.readResource(modelResourceStructureId);
            }
            CmsResource newResource = typeConfig.createNewElement(
                cloneCms,
                modelResource,
                CmsResource.getParentFolder(pageResource.getRootPath()));
            CmsContainerElementBean bean = getCachedElement(clientId, pageResource.getRootPath());
            CmsContainerElementBean newBean = new CmsContainerElementBean(
                newResource.getStructureId(),
                null,
                bean.getIndividualSettings(),
                typeConfig.isCopyInModels());
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, java.util.Collection, java.util.Collection, boolean, boolean, java.lang.String, java.lang.String)
     */
    public Map<String, CmsContainerElementData> getElementsData(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        boolean allowNested,
        boolean allwaysCopy,
        String dndSource,
        String locale)
    throws CmsRpcException {

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
                allwaysCopy,
                dndSource,
                CmsStringUtil.isNotEmptyOrWhitespaceOnly(dndSource),
                CmsLocaleManager.getLocale(locale));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementSettingsConfig(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, java.lang.String, java.lang.String, java.util.Collection, boolean, java.lang.String)
     */
    public CmsContainerElementData getElementSettingsConfig(
        CmsContainerPageRpcContext context,
        String clientId,
        String containerId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException {

        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = cms.getSitePath(pageResource);

            CmsContainerPageBean pageBean = generateContainerPageForContainers(
                containers,
                cms.getRequestContext().addSiteRoot(containerpageUri));

            CmsElementUtil elemUtil = new CmsElementUtil(
                cms,
                containerpageUri,
                pageBean,
                null,
                getRequest(),
                getResponse(),
                false,
                CmsLocaleManager.getLocale(locale));
            CmsContainerElementBean element = getCachedElement(
                clientId,
                cms.getRequestContext().addSiteRoot(containerpageUri));
            if (element.getInstanceId() == null) {
                element = element.clone();
                getSessionCache().setCacheContainerElement(element.editorHash(), element);
            }
            element.initResource(cms);
            return elemUtil.getElementSettingsConfig(pageResource, element, containerId, containers, allowNested);
        } catch (Throwable e) {
            error(e);
        }
        return null;
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
        String locale)
    throws CmsRpcException {

        CmsContainerElementData element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = cms.getSitePath(pageResource);
            Locale contentLocale = CmsLocaleManager.getLocale(locale);
            CmsElementUtil elemUtil = new CmsElementUtil(
                cms,
                containerpageUri,
                generateContainerPageForContainers(containers, pageResource.getRootPath()),
                detailContentId,
                getRequest(),
                getResponse(),
                false,
                contentLocale);

            CmsContainerElementBean elementBean = getCachedElement(clientId, pageResource.getRootPath());
            elementBean.initResource(cms);
            storeFormatterSelection(elementBean, settings);
            // make sure to keep the element instance id
            if (!settings.containsKey(CmsContainerElement.ELEMENT_INSTANCE_ID)
                && elementBean.getIndividualSettings().containsKey(CmsContainerElement.ELEMENT_INSTANCE_ID)) {
                settings.put(
                    CmsContainerElement.ELEMENT_INSTANCE_ID,
                    elementBean.getIndividualSettings().get(CmsContainerElement.ELEMENT_INSTANCE_ID));
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
        String locale)
    throws CmsRpcException {

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
    public CmsContainerPageGalleryData getGalleryDataForPage(
        final List<CmsContainer> containers,
        CmsUUID elementView,
        String uri,
        String locale)
    throws CmsRpcException {

        CmsGalleryDataBean data = null;
        try {

            CmsObject cms = getCmsObject();

            CmsAddDialogTypeHelper typeHelper = new CmsAddDialogTypeHelper();
            List<CmsResourceTypeBean> resTypeBeans = typeHelper.getResourceTypes(
                cms,
                cms.getRequestContext().addSiteRoot(uri),
                uri,
                OpenCms.getADEManager().getElementViews(cms).get(elementView),
                new I_CmsResourceTypeEnabledCheck() {

                    public boolean checkEnabled(
                        CmsObject paramCms,
                        CmsADEConfigData config,
                        I_CmsResourceType resType) {

                        boolean isModelGroup = CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(
                            resType.getTypeName());
                        return isModelGroup || config.hasFormatters(paramCms, resType, containers);
                    }
                });
            CmsGalleryService srv = new CmsGalleryService();
            srv.setCms(cms);
            srv.setRequest(getRequest());
            data = srv.getInitialSettingsForContainerPage(resTypeBeans, uri, locale);
            CmsContainerPageGalleryData result = new CmsContainerPageGalleryData();

            CmsADESessionCache cache = CmsADESessionCache.getCache(getRequest(), cms);
            CmsGallerySearchBean search = cache.getLastPageEditorGallerySearch();
            String subsite = OpenCms.getADEManager().getSubSiteRoot(cms, cms.addSiteRoot(uri));
            String searchStoreKey = elementView + "|" + subsite + "|" + locale;
            data.getContextParameters().put("searchStoreKey", searchStoreKey);
            if (search != null) {
                if (searchStoreKey.equals(
                    search.getOriginalGalleryData().getContextParameters().get("searchStoreKey"))) {
                    if (hasCompatibleSearchData(search.getOriginalGalleryData(), data, search)) {

                        CmsVfsEntryBean preloadData = null;
                        if (search.getFolders() != null) {
                            preloadData = CmsGalleryService.generateVfsPreloadData(
                                getCmsObject(),
                                CmsGalleryService.getVfsTreeState(getRequest(), data.getTreeToken()),
                                search.getFolders());
                        }

                        // only restore last result list if the search was performed in a 'similar' context
                        search.setTabId(GalleryTabId.cms_tab_results.toString());
                        search.setPage(1);
                        search.setLastPage(0);
                        data.setStartTab(GalleryTabId.cms_tab_results);
                        search = srv.getSearch(search);
                        data.setVfsPreloadData(preloadData);
                        data.setIncludeExpiredDefault(search.isIncludeExpired());
                        result.setGallerySearch(search);
                    }
                }
            }
            result.setGalleryData(data);
            return result;

        } catch (Exception e) {
            error(e);
            return null;
        }
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
        String localeName)
    throws CmsRpcException {

        CmsContainerElementData result = null;
        try {
            ensureSession();
            CmsResource pageResource = getCmsObject().readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            String containerpageUri = getCmsObject().getSitePath(pageResource);
            Locale locale = CmsLocaleManager.getLocale(localeName);
            result = getNewElement(
                getServerIdString(resourceType),
                containerpageUri,
                detailContentId,
                containers,
                allowNested,
                locale);
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
        String locale)
    throws CmsRpcException {

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
            return new CmsRemovedElementStatus(null, null, false, null);
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
        ElementDeleteMode elementDeleteMode = null;
        CmsResource pageResource = cms.readResource(containerpageId, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cms, pageResource.getRootPath());
        CmsResourceTypeConfig typeConfig = adeConfig.getResourceType(
            OpenCms.getResourceManager().getResourceType(elementResource).getTypeName());

        if (typeConfig != null) {
            elementDeleteMode = typeConfig.getElementDeleteMode();
        }

        boolean hasNoRelations = relationsToElement.isEmpty();
        boolean deletionCandidate = hasNoRelations && hasWritePermissions && !isSystemResource;
        CmsListInfoBean elementInfo = CmsVfsService.getPageInfo(cms, elementResource);
        return new CmsRemovedElementStatus(structureId, elementInfo, deletionCandidate, elementDeleteMode);
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
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        HttpServletRequest request = getRequest();
        try {
            CmsTemplateContextInfo info = OpenCms.getTemplateContextManager().getContextInfoBean(cms, request);
            CmsResource containerPage = getContainerpage(cms);
            boolean isEditingModelGroup = isEditingModelGroups(cms, containerPage);
            boolean isModelPage = isModelPage(cms, containerPage);
            if (isModelPage) {
                // the model edit confirm dialog should only be shown once per session, disable it after first model editing
                getRequest().getSession().setAttribute(
                    CmsVfsSitemapService.ATTR_SHOW_MODEL_EDIT_CONFIRM,
                    Boolean.FALSE);
            }

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
            CmsQuickLaunchLocationCache locationCache = CmsQuickLaunchLocationCache.getLocationCache(
                request.getSession());
            if (detailResource != null) {
                locationCache.setPageEditorLocation(
                    cms.getRequestContext().getSiteRoot(),
                    cms.getSitePath(detailResource));
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(
                    detailResource.getRootPath(),
                    CmsJspTagContainer.getDetailContainerLocale(
                        cms,
                        cms.getRequestContext().getLocale().toString(),
                        containerPage));
                if (rootCms.existsResource(detailContainerPage, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    noEditReason = getNoEditReason(
                        rootCms,
                        rootCms.readResource(detailContainerPage, CmsResourceFilter.IGNORE_EXPIRATION));
                } else {
                    String permissionFolder = CmsResource.getFolderPath(detailContainerPage);
                    while (!rootCms.existsResource(permissionFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
                        permissionFolder = CmsResource.getParentFolder(permissionFolder);
                    }
                    noEditReason = getNoEditReason(
                        rootCms,
                        rootCms.readResource(permissionFolder, CmsResourceFilter.IGNORE_EXPIRATION));
                }
            } else {
                if (!isModelPage && !isEditingModelGroup) {
                    locationCache.setPageEditorLocation(
                        cms.getRequestContext().getSiteRoot(),
                        cms.getSitePath(containerPage));
                }
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
                LOG.info("Invalid reuse mode : " + reuseModeString, e);
            }
            InitialElementViewProvider viewHelper = new InitialElementViewProvider();
            viewHelper.init(getSessionCache().getElementView(), containerPage);
            CmsLocaleGroup group = cms.getLocaleGroupService().readLocaleGroup(containerPage);
            Locale mainLocale = null;

            if (group.isRealGroup() && !cms.getRequestContext().getLocale().equals(group.getMainLocale())) {
                mainLocale = group.getMainLocale();
            }
            CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
            String ownRoot = siteManager.getSiteRoot(containerPage.getRootPath());
            Map<String, CmsLocaleLinkBean> localeLinkBeans = null;
            if (group.isRealGroup()) {
                localeLinkBeans = Maps.newHashMap();
                Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
                for (Map.Entry<Locale, CmsResource> entry : group.getResourcesByLocale().entrySet()) {
                    String otherRoot = siteManager.getSiteRoot(entry.getValue().getRootPath());
                    if ((otherRoot != null) && otherRoot.equals(ownRoot)) {
                        String theLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                            cms,
                            cms.getRequestContext().removeSiteRoot(entry.getValue().getRootPath()));
                        localeLinkBeans.put(entry.getKey().getDisplayLanguage(locale), CmsLocaleLinkBean.link(theLink));
                    } else {
                        localeLinkBeans.put(
                            entry.getKey().getDisplayLanguage(locale),
                            CmsLocaleLinkBean.error(
                                Messages.get().getBundle(locale).key(Messages.GUI_SHOWLOCALE_WRONG_SITE_0)));
                    }
                }
            }

            String onlineLink = null;
            if (!OpenCms.getSiteManager().getWorkplaceServer().equals(
                OpenCms.getSiteManager().getSiteForSiteRoot(cms.getRequestContext().getSiteRoot()).getUrl())) {
                if (detailResource != null) {
                    onlineLink = OpenCms.getLinkManager().getOnlineLink(
                        cms,
                        cms.getSitePath(containerPage),
                        cms.getSitePath(detailResource),
                        false);
                } else {
                    onlineLink = OpenCms.getLinkManager().getOnlineLink(cms, cms.getSitePath(containerPage));
                }
            }

            String modelGroupElementId = null;
            if (isEditingModelGroup) {
                CmsProperty modelElementProp = cms.readPropertyObject(
                    containerPage,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    false);
                if (!modelElementProp.isNullProperty() && CmsUUID.isValidUUID(modelElementProp.getValue())) {
                    modelGroupElementId = modelElementProp.getValue();
                }
            }
            String title = null;
            if (isModelPage || isEditingModelGroup) {
                title = Messages.get().getBundle(wpLocale).key(Messages.GUI_TITLE_MODEL_0);

            }
            ElementDeleteMode deleteMode = OpenCms.getWorkplaceManager().getElementDeleteMode();
            if (deleteMode == null) {
                deleteMode = ElementDeleteMode.askDelete;
            }

            data = new CmsCntPageData(
                onlineLink,
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
                Lists.newArrayList(viewHelper.getViewMap().values()),
                viewHelper.getDefaultView(),
                reuseMode,
                deleteMode,
                isModelPage,
                isEditingModelGroup,
                modelGroupElementId,
                mainLocale != null ? mainLocale.toString() : null,
                localeLinkBeans,
                title);
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveDetailContainers(org.opencms.util.CmsUUID, java.lang.String, java.util.List)
     */
    public void saveDetailContainers(CmsUUID detailId, String detailContainerResource, List<CmsContainer> containers)
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
                List<String> foldersToCreate = new ArrayList<String>();
                // ensure the parent folder exists
                while (!rootCms.existsResource(parentFolder)) {
                    foldersToCreate.add(0, parentFolder);
                    parentFolder = CmsResource.getParentFolder(parentFolder);
                }
                for (String folderName : foldersToCreate) {
                    CmsResource parentRes = rootCms.createResource(
                        folderName,
                        OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
                    // set the search exclude property on parent folder
                    rootCms.writePropertyObject(
                        folderName,
                        new CmsProperty(
                            CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE,
                            CmsSearchIndex.PROPERTY_SEARCH_EXCLUDE_VALUE_ALL,
                            null));
                    tryUnlock(parentRes);
                }
                containerpage = rootCms.createResource(
                    detailContainerResource,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlContainerPage.getStaticTypeName()));
            }
            ensureLock(containerpage);
            try {
                CmsResource detailResource = cms.readResource(detailId, CmsResourceFilter.IGNORE_EXPIRATION);
                String title = cms.readPropertyObject(
                    detailResource,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    true).getValue();
                if (title != null) {
                    title = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                        Messages.GUI_DETAIL_CONTENT_PAGE_TITLE_1,
                        title);
                    CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                    cms.writePropertyObjects(containerpage, Arrays.asList(titleProp));
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            saveContainers(rootCms, containerpage, detailContainerResource, containers);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveElementSettings(org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.util.Map, java.util.List, boolean, java.lang.String)
     */
    public CmsContainerElementData saveElementSettings(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        List<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException {

        CmsContainerElementData element = null;
        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            CmsResource pageResource = cms.readResource(context.getPageStructureId());
            initRequestFromRpcContext(context);
            Locale contentLocale = CmsLocaleManager.getLocale(locale);
            CmsContainerElementBean elementBean = getCachedElement(clientId, pageResource.getRootPath());
            elementBean.initResource(cms);
            storeFormatterSelection(elementBean, settings);
            // make sure to keep the element instance id
            if (!settings.containsKey(CmsContainerElement.ELEMENT_INSTANCE_ID)
                && elementBean.getIndividualSettings().containsKey(CmsContainerElement.ELEMENT_INSTANCE_ID)) {
                settings.put(
                    CmsContainerElement.ELEMENT_INSTANCE_ID,
                    elementBean.getIndividualSettings().get(CmsContainerElement.ELEMENT_INSTANCE_ID));
            }
            if (!isEditingModelGroups(cms, pageResource)) {
                // in case of model group state set to 'noGroup', the group will be dissolved and former group id forgotten
                if (!(settings.containsKey(CmsContainerElement.MODEL_GROUP_STATE)
                    && (ModelGroupState.noGroup == ModelGroupState.evaluate(
                        settings.get(CmsContainerElement.MODEL_GROUP_STATE))))) {
                    if (elementBean.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_ID)) {
                        // make sure to keep the model group id
                        settings.put(
                            CmsContainerElement.MODEL_GROUP_ID,
                            elementBean.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_ID));
                    }
                    if (elementBean.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_STATE)) {
                        settings.put(
                            CmsContainerElement.MODEL_GROUP_STATE,
                            elementBean.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_STATE));
                    }
                }
            }
            elementBean = CmsContainerElementBean.cloneWithSettings(
                elementBean,
                convertSettingValues(elementBean.getResource(), settings, contentLocale));
            getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);

            // update client id within container data
            for (CmsContainer container : containers) {
                for (CmsContainerElement child : container.getElements()) {
                    if (child.getClientId().equals(clientId)) {
                        child.setClientId(elementBean.editorHash());
                    }
                }
            }
            if (detailContentId == null) {
                saveContainers(cms, pageResource, cms.getSitePath(pageResource), containers);
            } else {
                List<CmsContainer> detailContainers = new ArrayList<CmsContainer>();
                for (CmsContainer container : containers) {
                    if (container.isDetailOnly()) {
                        detailContainers.add(container);
                    }
                }
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                CmsResource detailResource = rootCms.readResource(detailContentId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsResource detailContainerPage = rootCms.readResource(
                    CmsJspTagContainer.getDetailOnlyPageName(
                        detailResource.getRootPath(),
                        CmsJspTagContainer.getDetailContainerLocale(cms, locale, pageResource)));

                ensureLock(detailContainerPage);
                saveContainers(rootCms, detailContainerPage, detailContainerPage.getRootPath(), detailContainers);
            }
            String containerpageUri = cms.getSitePath(pageResource);
            CmsElementUtil elemUtil = new CmsElementUtil(
                cms,
                containerpageUri,
                generateContainerPageForContainers(containers, pageResource.getRootPath()),
                detailContentId,
                getRequest(),
                getResponse(),
                false,
                contentLocale);
            element = elemUtil.getElementData(pageResource, elementBean, containers, allowNested);
        } catch (Throwable e) {
            error(e);
        }
        return element;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveFavoriteList(java.util.List, java.lang.String)
     */
    public void saveFavoriteList(List<String> clientIds, String uri) throws CmsRpcException {

        try {
            ensureSession();
            OpenCms.getADEManager().saveFavoriteList(
                getCmsObject(),
                getCachedElements(clientIds, getCmsObject().getRequestContext().addSiteRoot(uri)));
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
        String locale)
    throws CmsRpcException {

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
        OpenCms.getSearchManager().updateOfflineIndexes();
        return new CmsGroupContainerSaveResult(
            getElementsData(context, detailContentId, reqParams, ids, containers, false, false, null, locale),
            removedElements);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveInheritanceContainer(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.ade.containerpage.shared.CmsInheritanceContainer, java.util.Collection, java.lang.String)
     */
    public Map<String, CmsContainerElementData> saveInheritanceContainer(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        CmsInheritanceContainer inheritanceContainer,
        Collection<CmsContainer> containers,
        String locale)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource containerPage = cms.readResource(pageStructureId);
            String sitePath = cms.getSitePath(containerPage);
            Locale requestedLocale = CmsLocaleManager.getLocale(locale);
            CmsResource referenceResource = null;
            if (inheritanceContainer.isNew()) {
                CmsADEConfigData config = getConfigData(containerPage.getRootPath());
                CmsResourceTypeConfig typeConfig = config.getResourceType(
                    CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME);
                referenceResource = typeConfig.createNewElement(cms, containerPage.getRootPath());
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
                CmsContainerElementBean elementBean = getCachedElement(
                    clientElement.getClientId(),
                    containerPage.getRootPath());
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
                false,
                null,
                false,
                requestedLocale);
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveRecentList(java.util.List, java.lang.String)
     */
    public void saveRecentList(List<String> clientIds, String uri) throws CmsRpcException {

        try {
            ensureSession();
            OpenCms.getADEManager().saveRecentList(
                getCmsObject(),
                getCachedElements(clientIds, getCmsObject().getRequestContext().addSiteRoot(uri)));
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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#setLastPage(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void setLastPage(CmsUUID pageId, CmsUUID detailId) throws CmsRpcException {

        try {
            HttpServletRequest req = getRequest();
            CmsObject cms = getCmsObject();
            CmsADESessionCache cache = CmsADESessionCache.getCache(req, cms);
            cache.setLastPage(cms, pageId, detailId);
        } catch (Exception e) {
            error(e);
        }

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
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#syncSaveDetailContainers(org.opencms.util.CmsUUID, java.lang.String, java.util.List)
     */
    public void syncSaveDetailContainers(
        CmsUUID detailId,
        String detailContainerResource,
        List<CmsContainer> containers)
    throws CmsRpcException {

        saveDetailContainers(detailId, detailContainerResource, containers);
    }

    /**
     * Gets the settings which should be updated for an element in the DND case.<p>
     *
     * @param originalSettings the original settings
     * @param formatterConfig the formatter configuration for the element
     * @param containers the containers
     * @param dndContainer the id of the DND origin container
     * @param allowNested true if nested containers are allowed
     *
     * @return the map of settings to update
     */
    Map<String, String> getSettingsToChangeForDnd(
        Map<String, String> originalSettings,
        CmsFormatterConfiguration formatterConfig,
        Collection<CmsContainer> containers,
        String dndContainer,
        boolean allowNested) {

        Map<String, String> result = Maps.newHashMap();
        if (dndContainer == null) {
            return result;
        }
        String key = CmsFormatterConfig.getSettingsKeyForContainer(dndContainer);
        String formatterId = originalSettings.get(key);
        if (formatterId == null) {
            return result;
        }
        for (CmsContainer container : containers) {
            if (container.getName().equals(dndContainer)) {
                continue;
            }
            Map<String, I_CmsFormatterBean> formatterSelection = formatterConfig.getFormatterSelection(
                container.getType(),
                container.getWidth(),
                allowNested);
            if (formatterSelection.containsKey(formatterId)) {
                String newKey = CmsFormatterConfig.getSettingsKeyForContainer(container.getName());
                result.put(newKey, formatterId);
            }
        }
        return result;
    }

    /**
     * Creates a new container element bean from an existing one, but changes some of the individual settings in the copy.<p>
     *
     * @param element the original container element
     * @param settingsToOverride the map of settings to change
     *
     * @return the new container element bean with the changed settings
     */
    CmsContainerElementBean overrideSettings(CmsContainerElementBean element, Map<String, String> settingsToOverride) {

        Map<String, String> settings = Maps.newHashMap(element.getIndividualSettings());
        settings.putAll(settingsToOverride);
        CmsContainerElementBean result = new CmsContainerElementBean(
            element.getId(),
            element.getFormatterId(),
            settings,
            element.isCreateNew());
        return result;
    }

    /**
     * Adds the formatter to the recently used formatter list.<p>
     *
     * @param elementBean the element bean
     * @param settings the changed settings
     */
    void storeFormatterSelection(CmsContainerElementBean elementBean, Map<String, String> settings) {

        Entry<String, String> previousFormatterEntry = null;
        for (Entry<String, String> entry : elementBean.getIndividualSettings().entrySet()) {
            if (entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                previousFormatterEntry = entry;
                break;
            }
        }
        Entry<String, String> formatterEntry = null;
        for (Entry<String, String> entry : settings.entrySet()) {
            if (entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                formatterEntry = entry;
                break;
            }
        }
        if ((formatterEntry != null)
            && ((previousFormatterEntry == null)
                || !formatterEntry.getKey().equals(previousFormatterEntry.getKey())
                || !formatterEntry.getValue().equals(previousFormatterEntry.getValue()))) {
            String idString = formatterEntry.getValue();
            if (CmsUUID.isValidUUID(idString)) { // TODO: Make this work for schema formatters
                // the formatter setting has changed
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(elementBean.getResource());
                getSessionCache().addRecentFormatter(resType.getTypeName(), new CmsUUID(idString));
            }
        }
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
     * @param pageRootPath the container page root path
     *
     * @return the cached container element bean
     *
     * @throws CmsException in case reading the element resource fails
     */
    private CmsContainerElementBean getCachedElement(String clientId, String pageRootPath) throws CmsException {

        String id = clientId;
        CmsContainerElementBean element = null;
        element = getSessionCache().getCacheContainerElement(id);
        if (element != null) {
            return element;
        }
        if (id.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
            id = getServerIdString(id);
            element = getSessionCache().getCacheContainerElement(id);
            if (element != null) {
                return element;
            }
        }
        // this is necessary if the element has not been cached yet
        CmsResource resource = getCmsObject().readResource(convertToServerId(id), CmsResourceFilter.IGNORE_EXPIRATION);
        CmsADEConfigData configData = getConfigData(pageRootPath);
        CmsResourceTypeConfig typeConfig = configData.getResourceType(
            OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        element = new CmsContainerElementBean(
            convertToServerId(id),
            null,
            null,
            (typeConfig != null) && typeConfig.isCopyInModels());
        getSessionCache().setCacheContainerElement(element.editorHash(), element);
        return element;
    }

    /**
     * Returns a list of container elements from a list with client id's.<p>
     *
     * @param clientIds list of client id's
     * @param pageRootPath the container page root path
     *
     * @return a list of element beans
     * @throws CmsException in case reading the element resource fails
     */
    private List<CmsContainerElementBean> getCachedElements(List<String> clientIds, String pageRootPath)
    throws CmsException {

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>();
        for (String id : clientIds) {
            try {
                result.add(getCachedElement(id, pageRootPath));
            } catch (CmsIllegalArgumentException e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the configuration data of the current container page context.<p>
     *
     * @param pageRootPath the container page root path
     *
     * @return the configuration data of the current container page context
     */
    private CmsADEConfigData getConfigData(String pageRootPath) {

        if (m_configData == null) {
            m_configData = OpenCms.getADEManager().lookupConfiguration(getCmsObject(), pageRootPath);
        }
        return m_configData;
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
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        CmsContainerBean result = new CmsContainerBean(
            container.getName(),
            container.getType(),
            container.getParentInstanceId(),
            container.isRootContainer(),
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
        CmsContainerElement elementData)
    throws CmsException {

        String elementClientId = elementData.getClientId();
        boolean hasUuidPrefix = (elementClientId != null) && elementClientId.matches(CmsUUID.UUID_REGEX + ".*$");
        boolean isCreateNew = elementData.isCreateNew();
        if (elementData.isNew() && !hasUuidPrefix) {

            // Due to the changed save system without the save button, we need to make sure that new elements
            // are only created once. This must happen when the user first edits a new element. But we still
            // want to save changes to non-new elements on the page, so we skip new elements while saving.
            return null;
        }
        CmsContainerElementBean element = getCachedElement(elementData.getClientId(), containerpageRootPath);

        CmsResource resource;
        if (element.getResource() == null) {
            element.initResource(cms);
            resource = element.getResource();
        } else {
            // make sure resource is readable, this is necessary for new content elements
            if (element.getId().isNullUUID()) {
                // in memory only element, can not be read nor saved
                return null;
            }
            resource = cms.readResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION);
        }

        // check if there is a valid formatter
        int containerWidth = container.getWidth();
        CmsADEConfigData config = getConfigData(containerpageRootPath);
        CmsFormatterConfiguration formatters = config.getFormatters(cms, resource);
        String containerType = null;
        containerType = container.getType();
        I_CmsFormatterBean formatter = null;
        String formatterConfigId = null;
        if ((element.getIndividualSettings() != null)
            && (element.getIndividualSettings().get(
                CmsFormatterConfig.getSettingsKeyForContainer(container.getName())) != null)) {
            formatterConfigId = element.getIndividualSettings().get(
                CmsFormatterConfig.getSettingsKeyForContainer(container.getName()));
            if (CmsUUID.isValidUUID(formatterConfigId)) {
                formatter = OpenCms.getADEManager().getCachedFormatters(false).getFormatters().get(
                    new CmsUUID(formatterConfigId));
            } else if (formatterConfigId.startsWith(CmsFormatterConfig.SCHEMA_FORMATTER_ID)
                && CmsUUID.isValidUUID(formatterConfigId.substring(CmsFormatterConfig.SCHEMA_FORMATTER_ID.length()))) {
                formatter = formatters.getFormatterSelection(containerType, containerWidth, true).get(
                    formatterConfigId);
            }
        }
        if (formatter == null) {
            formatter = CmsElementUtil.getFormatterForContainer(
                cms,
                element,
                container,
                config,
                true,
                getSessionCache());
            if (formatter != null) {
                formatterConfigId = formatter.isFromFormatterConfigFile()
                ? formatter.getId()
                : CmsFormatterConfig.SCHEMA_FORMATTER_ID + formatter.getJspStructureId().toString();
            }
        }
        CmsContainerElementBean newElementBean = null;
        if (formatter != null) {
            Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
            String formatterKey = CmsFormatterConfig.getSettingsKeyForContainer(container.getName());
            settings.put(formatterKey, formatterConfigId);
            // remove not used formatter settings
            Iterator<Entry<String, String>> entries = settings.entrySet().iterator();
            while (entries.hasNext()) {
                Entry<String, String> entry = entries.next();
                if (entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)
                    && !entry.getKey().equals(formatterKey)) {
                    entries.remove();
                }
            }

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
     * @param allwaysCopy <code>true</code> in case reading data for a clipboard element used as a copy group
     * @param dndOriginContainer the container from which an element was dragged (null if this method is not called for DND)
     * @param isDragMode if the page is in drag mode
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
        boolean allwaysCopy,
        String dndOriginContainer,
        boolean isDragMode,
        Locale locale)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsContainerPageBean pageBean = generateContainerPageForContainers(
            containers,
            cms.getRequestContext().addSiteRoot(uriParam));
        Map<String, CmsContainerElementBean> idMapping = new HashMap<String, CmsContainerElementBean>();
        for (String elemId : clientIds) {
            if ((elemId == null)) {
                continue;
            }
            CmsContainerElementBean element = getCachedElement(elemId, cms.getRequestContext().addSiteRoot(uriParam));
            if (element.getInstanceId() == null) {
                element = element.clone();
                getSessionCache().setCacheContainerElement(element.editorHash(), element);
            }
            element.initResource(cms);
            idMapping.put(elemId, element);
        }
        List<String> foundGroups = new ArrayList<String>();
        if (CmsContainerElement.MENU_CONTAINER_ID.equals(dndOriginContainer)) {
            // this indicates the element is added to the page and not being repositioned, check for model group data
            CmsModelGroupHelper modelHelper = new CmsModelGroupHelper(
                cms,
                getConfigData(uriParam),
                getSessionCache(),
                isEditingModelGroups(cms, page));
            pageBean = modelHelper.prepareforModelGroupContent(idMapping, foundGroups, pageBean, allwaysCopy, locale);
        }

        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            uriParam,
            pageBean,
            detailContentId,
            getRequest(),
            getResponse(),
            isDragMode,
            locale);
        Map<String, CmsContainerElementData> result = new HashMap<String, CmsContainerElementData>();
        Set<String> ids = new HashSet<String>();
        for (Entry<String, CmsContainerElementBean> entry : idMapping.entrySet()) {
            CmsContainerElementBean element = entry.getValue();
            String dndId = null;
            if (ids.contains(element.editorHash())) {
                continue;
            }
            if ((dndOriginContainer != null) && !CmsContainerElement.MENU_CONTAINER_ID.equals(dndOriginContainer)) {
                CmsFormatterConfiguration formatterConfig = elemUtil.getFormatterConfiguration(element.getResource());
                Map<String, String> dndSettings = getSettingsToChangeForDnd(
                    element.getIndividualSettings(),
                    formatterConfig,
                    containers,
                    dndOriginContainer,
                    allowNested);
                if (!dndSettings.isEmpty()) {
                    CmsContainerElementBean dndElementBean = overrideSettings(element, dndSettings);
                    getSessionCache().setCacheContainerElement(dndElementBean.editorHash(), dndElementBean);
                    dndId = dndElementBean.editorHash();
                    Map<String, CmsContainerElementData> dndResults = getElements(
                        page,
                        Arrays.asList(dndId),
                        uriParam,
                        detailContentId,
                        containers,
                        allowNested,
                        false,
                        null,
                        isDragMode,
                        locale);
                    result.putAll(dndResults);
                }
            }

            CmsContainerElementData elementData = elemUtil.getElementData(page, element, containers, allowNested);
            if (elementData == null) {
                continue;
            }
            elementData.setDndId(dndId);
            result.put(entry.getKey(), elementData);
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
            ids.add(element.editorHash());
        }
        for (CmsContainerElementData elementData : result.values()) {
            elementData.setGroup(foundGroups.contains(elementData.getClientId()));
        }
        return result;
    }

    /**
     * Helper method for converting a CmsGroupContainer to a CmsGroupContainerBean when saving a group container.<p>
     *
     * @param groupContainer the group-container data
     * @param containerPage the container page
     * @param locale the locale to use
     *
     * @return the group-container bean
     */
    private CmsGroupContainerBean getGroupContainerBean(
        CmsGroupContainer groupContainer,
        CmsResource containerPage,
        String locale) {

        CmsObject cms = getCmsObject();
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElement elementData : groupContainer.getElements()) {
            try {
                if (elementData.isNew()) {
                    elementData = createNewElement(
                        containerPage.getStructureId(),
                        elementData.getClientId(),
                        elementData.getResourceType(),
                        null,
                        locale);
                }
                CmsContainerElementBean element = getCachedElement(
                    elementData.getClientId(),
                    containerPage.getRootPath());

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
        Locale locale)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            containerpageUri,
            generateContainerPageForContainers(containers, cms.getRequestContext().addSiteRoot(containerpageUri)),
            detailContentId,
            getRequest(),
            getResponse(),
            true,
            locale);
        CmsADESessionCache cache = getSessionCache();
        List<CmsContainerElementData> result = new ArrayList<CmsContainerElementData>();
        for (CmsContainerElementBean element : listElements) {
            // checking if resource exists
            if (cms.existsResource(element.getId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFile())) {
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
        Locale locale)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            uriParam,
            generateContainerPageForContainers(containers, cms.getRequestContext().addSiteRoot(uriParam)),
            detailContentId,
            getRequest(),
            getResponse(),
            true,
            locale);
        CmsADEConfigData configData = getConfigData(cms.getRequestContext().addSiteRoot(uriParam));
        CmsResourceTypeConfig typeConfig = configData.getResourceType(resourceTypeName);
        CmsContainerElementBean elementBean = CmsContainerElementBean.createElementForResourceType(
            cms,
            OpenCms.getResourceManager().getResourceType(resourceTypeName),
            "/",
            Collections.<String, String> emptyMap(),
            typeConfig.isCopyInModels(),
            locale);
        CmsContainerElementData data = elemUtil.getElementData(
            elemUtil.getPage(),
            elementBean,
            containers,
            allowNested);
        getSessionCache().setCacheContainerElement(resourceTypeName, elementBean);
        getSessionCache().setCacheContainerElement(elementBean.editorHash(), elementBean);
        return data;
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

        return new CmsResourceUtil(cms, containerPage).getNoEditReason(
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
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
     * Checks if results for the stored gallery data can be restored for the new gallery data.<p>
     *
     * @param originalGalleryData the original gallery data
     * @param data the new gallery data
     * @param search the search bean
     *
     * @return true if the original and new gallery data are compatible, i.e. we can restore the search results
     */
    private boolean hasCompatibleSearchData(
        CmsGalleryDataBean originalGalleryData,
        CmsGalleryDataBean data,
        CmsGallerySearchBean search) {

        Set<String> originalUsableTypes = Sets.newHashSet();
        Set<String> usableTypes = Sets.newHashSet();
        for (CmsResourceTypeBean type : originalGalleryData.getTypes()) {
            if (!type.isDeactivated()) {
                originalUsableTypes.add(type.getType());
            }
        }
        for (CmsResourceTypeBean type : data.getTypes()) {
            if (!type.isDeactivated()) {
                usableTypes.add(type.getType());
            }
        }
        if (!usableTypes.containsAll(originalUsableTypes)) {
            return false;
        }
        return true;
    }

    /**
     * Initializes request attributes using data from the RPC context.<p>
     *
     * @param context the RPC context
     */
    private void initRequestFromRpcContext(CmsContainerPageRpcContext context) {

        if (context.getTemplateContext() != null) {
            getRequest().setAttribute(
                CmsTemplateContextManager.ATTR_RPC_CONTEXT_OVERRIDE,
                context.getTemplateContext());
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
        CmsGroupContainer groupContainer)
    throws CmsException, CmsXmlException {

        ensureSession();
        CmsResource pageResource = getCmsObject().readResource(pageStructureId, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsResource groupContainerResource = null;
        if (groupContainer.isNew()) {
            CmsADEConfigData config = getConfigData(pageResource.getRootPath());
            CmsResourceTypeConfig typeConfig = config.getResourceType(
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME);
            groupContainerResource = typeConfig.createNewElement(getCmsObject(), pageResource.getRootPath());
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
            pageResource,
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

        CmsADEConfigData config = getConfigData(containerPage.getRootPath());
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
        List<CmsContainer> containers)
    throws CmsException {

        CmsContainerPageBean page = generateContainerPageForContainers(containers, containerpage.getRootPath());

        CmsModelGroupHelper modelHelper = new CmsModelGroupHelper(
            getCmsObject(),
            getConfigData(containerpage.getRootPath()),
            getSessionCache(),
            isEditingModelGroups(cms, containerpage));
        if (isEditingModelGroups(cms, containerpage)) {
            page = modelHelper.saveModelGroups(page, containerpage);
        } else {
            page = modelHelper.removeModelGroupContainers(page);
        }
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
     * @param containerPage the edited container page
     * @param clientId the elements client id
     * @param list the list to update
     *
     * @return the updated list
     *
     * @throws CmsException in case reading the element resource fails
     */
    private List<CmsContainerElementBean> updateFavoriteRecentList(
        CmsResource containerPage,
        String clientId,
        List<CmsContainerElementBean> list)
    throws CmsException {

        try {
            CmsContainerElementBean element = getCachedElement(clientId, containerPage.getRootPath());
            Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
            String formatterID = null;
            Iterator<Entry<String, String>> entries = settings.entrySet().iterator();
            while (entries.hasNext()) {
                Entry<String, String> entry = entries.next();
                if (entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                    formatterID = entry.getValue();
                    entries.remove();
                }
            }
            settings.put(CmsFormatterConfig.FORMATTER_SETTINGS_KEY, formatterID);
            settings.put(SOURCE_CONTAINERPAGE_ID_SETTING, containerPage.getStructureId().toString());
            element = CmsContainerElementBean.cloneWithSettings(element, settings);
            Iterator<CmsContainerElementBean> listIt = list.iterator();
            while (listIt.hasNext()) {
                CmsContainerElementBean listElem = listIt.next();
                if (element.getInstanceId().equals(listElem.getInstanceId())) {
                    listIt.remove();
                }
            }
            list.add(0, element);
            return list;
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return list;
        }
    }
}
