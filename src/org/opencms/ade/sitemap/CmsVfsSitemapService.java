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

package org.opencms.ade.sitemap;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.ade.configuration.CmsModelPageConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.detailpage.CmsDetailPageConfigurationWriter;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapInfo;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsTemplateFinder;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsClientLock;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Handles all RPC services related to the vfs sitemap.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsVfsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** Helper class for representing information about a  lock. */
    protected class LockInfo {

        /** The lock state. */
        private CmsLock m_lock;

        /** True if the lock was just locked. */
        private boolean m_wasJustLocked;

        /**
         * Creates a new LockInfo object.<p>
         * 
         * @param lock the lock state 
         * @param wasJustLocked true if the lock was just locked 
         */
        public LockInfo(CmsLock lock, boolean wasJustLocked) {

            m_lock = lock;
            m_wasJustLocked = wasJustLocked;
        }

        /** 
         * Returns the lock state.<p>
         * 
         * @return the lock state 
         */
        public CmsLock getLock() {

            return m_lock;
        }

        /**
         * Returns true if the lock was just locked.<p> 
         * 
         * @return true if the lock was just locked 
         */
        public boolean wasJustLocked() {

            return m_wasJustLocked;
        }
    }

    /** The configuration key for the functionDetail attribute in the container.info property. */
    public static final String KEY_FUNCTION_DETAIL = "functionDetail";

    /** The additional user info key for deleted list. */
    private static final String ADDINFO_ADE_DELETED_LIST = "ADE_DELETED_LIST";

    /** The additional user info key for modified list. */
    private static final String ADDINFO_ADE_MODIFIED_LIST = "ADE_MODIFIED_LIST";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsSitemapService.class);

    /** The redirect recource type name. */
    private static final String RECOURCE_TYPE_NAME_REDIRECT = "htmlredirect";

    /** The redirect target XPath. */
    private static final String REDIRECT_LINK_TARGET_XPATH = "Link";

    /** Serialization uid. */
    private static final long serialVersionUID = -7236544324371767330L;

    /** The navigation builder. */
    private CmsJspNavBuilder m_navBuilder;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsVfsSitemapService newInstance(HttpServletRequest request) {

        CmsVfsSitemapService service = new CmsVfsSitemapService();
        service.setCms(CmsFlexController.getCmsObject(request));
        service.setRequest(request);
        return service;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createSubSitemap(org.opencms.util.CmsUUID)
     */
    public CmsSitemapChange createSubSitemap(CmsUUID entryId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource subSitemapFolder = cms.readResource(entryId);
            ensureLock(subSitemapFolder);
            String sitePath = cms.getSitePath(subSitemapFolder);
            String folderName = CmsStringUtil.joinPaths(sitePath, CmsADEManager.CONFIG_FOLDER_NAME + "/");
            String sitemapConfigName = CmsStringUtil.joinPaths(folderName, CmsADEManager.CONFIG_FILE_NAME);
            if (!cms.existsResource(folderName)) {
                cms.createResource(
                    folderName,
                    OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_FOLDER_TYPE).getTypeId());
            }
            I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE);
            if (cms.existsResource(sitemapConfigName)) {
                CmsResource configFile = cms.readResource(sitemapConfigName);
                if (configFile.getTypeId() != configType.getTypeId()) {
                    throw new CmsException(Messages.get().container(
                        Messages.ERR_CREATING_SUB_SITEMAP_WRONG_CONFIG_FILE_TYPE_2,
                        sitemapConfigName,
                        CmsADEManager.CONFIG_TYPE));
                }
            } else {
                cms.createResource(
                    sitemapConfigName,
                    OpenCms.getResourceManager().getResourceType(CmsADEManager.CONFIG_TYPE).getTypeId());
            }
            subSitemapFolder.setType(getEntryPointType());
            cms.writeResource(subSitemapFolder);
            tryUnlock(subSitemapFolder);
            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(
                getNavBuilder().getNavigationForResource(sitePath, CmsResourceFilter.ONLY_VISIBLE),
                false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            CmsSitemapChange result = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            result.setUpdatedEntry(entry);
            return result;
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getChildren(java.lang.String, org.opencms.util.CmsUUID, int)
     */
    public CmsClientSitemapEntry getChildren(String entryPointUri, CmsUUID entryId, int levels) throws CmsRpcException {

        CmsClientSitemapEntry entry = null;

        try {
            CmsObject cms = getCmsObject();

            //ensure that root ends with a '/' if it's a folder 
            CmsResource rootRes = cms.readResource(entryId, CmsResourceFilter.ONLY_VISIBLE);
            String root = cms.getSitePath(rootRes);
            CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(root, CmsResourceFilter.ONLY_VISIBLE);
            boolean isRoot = root.equals(entryPointUri);
            entry = toClientEntry(navElement, isRoot);
            if ((levels > 0) && (isRoot || (rootRes.isFolder() && (!isSubSitemap(navElement))))) {
                entry.setSubEntries(getChildren(root, levels, null), null);
            }
        } catch (Throwable e) {
            error(e);
        }
        return entry;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#mergeSubSitemap(java.lang.String, org.opencms.util.CmsUUID)
     */
    public CmsSitemapChange mergeSubSitemap(String entryPoint, CmsUUID subSitemapId) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureSession();
            CmsResource subSitemapFolder = cms.readResource(subSitemapId, CmsResourceFilter.ONLY_VISIBLE);
            ensureLock(subSitemapFolder);
            subSitemapFolder.setType(OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getTypeId());
            cms.writeResource(subSitemapFolder);
            String sitePath = cms.getSitePath(subSitemapFolder);
            String sitemapConfigName = CmsStringUtil.joinPaths(
                sitePath,
                CmsADEManager.CONFIG_FOLDER_NAME,
                CmsADEManager.CONFIG_FILE_NAME);
            if (cms.existsResource(sitemapConfigName)) {
                cms.deleteResource(sitemapConfigName, CmsResource.DELETE_PRESERVE_SIBLINGS);
            }
            tryUnlock(subSitemapFolder);
            CmsSitemapClipboardData clipboard = getClipboardData();
            CmsClientSitemapEntry entry = toClientEntry(
                getNavBuilder().getNavigationForResource(
                    cms.getSitePath(subSitemapFolder),
                    CmsResourceFilter.ONLY_VISIBLE),
                false);
            clipboard.addModified(entry);
            setClipboardData(clipboard);
            entry = getChildren(entryPoint, subSitemapId, 1);
            CmsSitemapChange result = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            result.setUpdatedEntry(entry);
            return result;
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#prefetch(java.lang.String)
     */
    public CmsSitemapData prefetch(String sitemapUri) throws CmsRpcException {

        CmsSitemapData result = null;
        CmsObject cms = getCmsObject();

        try {
            String openPath = getRequest().getParameter(CmsCoreData.PARAM_PATH);
            if (!isValidOpenPath(cms, openPath)) {
                // if no path is supplied, start from root
                openPath = "/";
            }
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(openPath));
            Map<String, CmsXmlContentProperty> propertyConfig = new LinkedHashMap<String, CmsXmlContentProperty>(
                configData.getPropertyConfigurationAsMap());
            Map<String, CmsClientProperty> parentProperties = generateParentProperties(configData.getBasePath());
            String siteRoot = cms.getRequestContext().getSiteRoot();
            String exportRfsPrefix = OpenCms.getStaticExportManager().getDefaultRfsPrefix();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            boolean isSecure = site.hasSecureServer();
            String parentSitemap = null;
            if (configData.getBasePath() != null) {
                CmsADEConfigData parentConfigData = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    CmsResource.getParentFolder(configData.getBasePath()));
                parentSitemap = parentConfigData.getBasePath();
                if (parentSitemap != null) {
                    parentSitemap = cms.getRequestContext().removeSiteRoot(parentSitemap);
                }
            }
            String noEdit = "";
            CmsNewResourceInfo defaultNewInfo = null;
            List<CmsNewResourceInfo> newResourceInfos = null;
            CmsDetailPageTable detailPages = null;
            List<CmsNewResourceInfo> resourceTypeInfos = null;
            boolean canEditDetailPages = false;
            boolean isOnlineProject = CmsProject.isOnlineProject(cms.getRequestContext().getCurrentProject().getUuid());

            Locale locale = CmsLocaleManager.getDefaultLocale();
            try {
                String basePath = configData.getBasePath();
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                CmsResource baseDir = rootCms.readResource(basePath, CmsResourceFilter.ONLY_VISIBLE);
                OpenCms.getLocaleManager();
                locale = CmsLocaleManager.getMainLocale(cms, baseDir);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

            detailPages = new CmsDetailPageTable(configData.getAllDetailPages());
            if (!isOnlineProject) {
                newResourceInfos = getNewResourceInfos(cms, configData, locale);
                CmsResource modelResource = null;
                if (configData.getDefaultModelPage() != null) {
                    if (cms.existsResource(configData.getDefaultModelPage().getResource().getStructureId())) {
                        modelResource = configData.getDefaultModelPage().getResource();
                    } else {
                        try {
                            modelResource = cms.readResource(
                                cms.getSitePath(configData.getDefaultModelPage().getResource()),
                                CmsResourceFilter.ONLY_VISIBLE);
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                }
                if ((modelResource == null) && !newResourceInfos.isEmpty()) {
                    try {
                        modelResource = cms.readResource(newResourceInfos.get(0).getCopyResourceId());
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                if (modelResource != null) {
                    resourceTypeInfos = getResourceTypeInfos(
                        getCmsObject(),
                        configData.getResourceTypes(),
                        configData.getFunctionReferences(),
                        modelResource,
                        locale);
                    try {
                        defaultNewInfo = createNewResourceInfo(cms, modelResource, locale);
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                canEditDetailPages = !(configData.isModuleConfiguration());
            }
            if (isOnlineProject) {
                noEdit = Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_SITEMAP_NO_EDIT_ONLINE_0);
            }
            List<String> allPropNames = getPropertyNames(cms);
            String returnCode = getRequest().getParameter(CmsCoreData.PARAM_RETURNCODE);
            if (!isValidReturnCode(returnCode)) {
                returnCode = null;
            }
            cms.getRequestContext().getSiteRoot();
            result = new CmsSitemapData(
                (new CmsTemplateFinder(cms)).getTemplates(),
                propertyConfig,
                getClipboardData(),
                parentProperties,
                allPropNames,
                exportRfsPrefix,
                isSecure,
                noEdit,
                isDisplayToolbar(getRequest()),
                defaultNewInfo,
                newResourceInfos,
                createResourceTypeInfo(OpenCms.getResourceManager().getResourceType(RECOURCE_TYPE_NAME_REDIRECT), null),
                getSitemapInfo(configData.getBasePath()),
                parentSitemap,
                getRootEntry(configData.getBasePath(), openPath),
                openPath,
                30,
                detailPages,
                resourceTypeInfos,
                returnCode,
                canEditDetailPages);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapChange)
     */
    public CmsSitemapChange save(String entryPoint, CmsSitemapChange change) throws CmsRpcException {

        CmsSitemapChange result = null;
        try {
            result = saveInternal(entryPoint, change);
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#saveSync(java.lang.String, org.opencms.ade.sitemap.shared.CmsSitemapChange)
     */
    public CmsSitemapChange saveSync(String entryPoint, CmsSitemapChange change) throws CmsRpcException {

        return save(entryPoint, change);
    }

    /**
     * Creates a "broken link" bean based on a resource.<p>
     * 
     * @param resource the resource 
     * 
     * @return the "broken link" bean with the data from the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsBrokenLinkBean createSitemapBrokenLinkBean(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
        String defaultTitle = "";
        String title = titleProp.getValue(defaultTitle);
        String path = cms.getSitePath(resource);
        String subtitle = path;
        return new CmsBrokenLinkBean(title, subtitle);

    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     * 
     * @param resource the resource to lock
     * 
     * @return the assigned lock
     * 
     * @throws CmsException if the resource could not be locked
     */
    protected LockInfo ensureLockAndGetInfo(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        boolean justLocked = false;
        List<CmsResource> blockingResources = cms.getBlockingLockedResources(resource);
        if ((blockingResources != null) && !blockingResources.isEmpty()) {
            throw new CmsException(org.opencms.gwt.Messages.get().container(
                org.opencms.gwt.Messages.ERR_RESOURCE_HAS_BLOCKING_LOCKED_CHILDREN_1,
                cms.getSitePath(resource)));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
            lock = cms.getLock(resource);
            justLocked = true;
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
            lock = cms.getLock(resource);
            justLocked = true;
        }
        return new LockInfo(lock, justLocked);
    }

    /**
     * Internal method for saving a sitemap.<p>
     * 
     * @param entryPoint the URI of the sitemap to save
     * @param change the change to save
     * 
     * @return list of changed sitemap entries
     * 
     * @throws CmsException 
     */
    protected CmsSitemapChange saveInternal(String entryPoint, CmsSitemapChange change) throws CmsException {

        ensureSession();
        switch (change.getChangeType()) {
            case clipboardOnly:
                // do nothing
                break;
            case remove:
                change = removeEntryFromNavigation(change);
                break;
            case undelete:
                change = undelete(change);
                break;
            default:
                change = applyChange(entryPoint, change);
        }
        setClipboardData(change.getClipBoardData());
        return change;
    }

    /**
     * Converts a sequence of properties to a map of client-side property beans.<p>
     * 
     * @param props the sequence of properties
     * @param preserveOrigin if true, the origin of the properties should be copied to the client properties 
     *  
     * @return the map of client properties 
     * 
     */
    Map<String, CmsClientProperty> createClientProperties(Iterable<CmsProperty> props, boolean preserveOrigin) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : props) {
            CmsClientProperty clientProp = createClientProperty(prop, preserveOrigin);
            result.put(prop.getName(), clientProp);
        }
        return result;
    }

    /**
     * Removes unnecessary locales from a container page.<p>
     * 
     * @param containerPage the container page which should be changed 
     * @param localeRes the resource used to determine the locale 
     * 
     * @throws CmsException if something goes wrong 
     */
    void ensureSingleLocale(CmsXmlContainerPage containerPage, CmsResource localeRes) throws CmsException {

        CmsObject cms = getCmsObject();
        Locale mainLocale = CmsLocaleManager.getMainLocale(cms, localeRes);
        OpenCms.getLocaleManager();
        Locale defaultLocale = CmsLocaleManager.getDefaultLocale();
        if (containerPage.hasLocale(mainLocale)) {
            removeAllLocalesExcept(containerPage, mainLocale);
            // remove other locales 
        } else if (containerPage.hasLocale(defaultLocale)) {
            containerPage.copyLocale(defaultLocale, mainLocale);
            removeAllLocalesExcept(containerPage, mainLocale);
        } else if (containerPage.getLocales().size() > 0) {
            containerPage.copyLocale(containerPage.getLocales().get(0), mainLocale);
            removeAllLocalesExcept(containerPage, mainLocale);
        } else {
            containerPage.addLocale(cms, mainLocale);
        }
    }

    /**
     * Gets the properties of a resource as a map of client properties.<p>
     * 
     * @param cms the CMS context to use 
     * @param res the resource whose properties to read
     * @param search true if the inherited properties should be read
     *  
     * @return the client properties as a map
     * 
     * @throws CmsException if something goes wrong 
     */
    Map<String, CmsClientProperty> getClientProperties(CmsObject cms, CmsResource res, boolean search)
    throws CmsException {

        List<CmsProperty> props = cms.readPropertyObjects(res, search);
        Map<String, CmsClientProperty> result = createClientProperties(props, false);
        return result;
    }

    /**
     * Adds a function detail element to a container page.<p>
     * 
     * @param cms the current CMS context 
     * @param page the container page which should be changed 
     * @param containerName the name of the container which should be used for function detail elements 
     * @param elementId the structure id of the element to add  
     * @param formatterId the structure id of the formatter for the element
     *  
     * @throws CmsException if something goes wrong 
     */
    private void addFunctionDetailElement(
        CmsObject cms,
        CmsXmlContainerPage page,
        String containerName,
        CmsUUID elementId,
        CmsUUID formatterId) throws CmsException {

        List<Locale> pageLocales = page.getLocales();
        for (Locale locale : pageLocales) {
            CmsContainerPageBean bean = page.getContainerPage(cms, locale);
            List<CmsContainerBean> containerBeans = new ArrayList<CmsContainerBean>();
            Collection<CmsContainerBean> originalContainers = bean.getContainers().values();
            if ((containerName == null) && !originalContainers.isEmpty()) {
                CmsContainerBean firstContainer = originalContainers.iterator().next();
                containerName = firstContainer.getName();
            }
            boolean foundContainer = false;
            for (CmsContainerBean cntBean : originalContainers) {
                boolean isDetailTarget = cntBean.getName().equals(containerName);
                if (isDetailTarget && !foundContainer) {
                    foundContainer = true;
                    List<CmsContainerElementBean> newElems = new ArrayList<CmsContainerElementBean>();
                    newElems.addAll(cntBean.getElements());
                    CmsContainerElementBean newElement = new CmsContainerElementBean(
                        elementId,
                        formatterId,
                        new HashMap<String, String>(),
                        false);
                    newElems.add(0, newElement);
                    CmsContainerBean newCntBean = new CmsContainerBean(cntBean.getName(), cntBean.getType(), newElems);
                    containerBeans.add(newCntBean);
                } else {
                    containerBeans.add(cntBean);
                }
            }
            if (!foundContainer) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_NO_FUNCTION_DETAIL_CONTAINER_1,
                    page.getFile().getRootPath()));
            }
            CmsContainerPageBean bean2 = new CmsContainerPageBean(locale, new ArrayList<CmsContainerBean>(
                containerBeans));
            page.writeContainerPage(cms, locale, bean2);
        }
    }

    /**
     * Applys the given change to the VFS.<p>
     * 
     * @param entryPoint the sitemap entry-point
     * @param change the change
     * 
     * @return the updated entry 
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange applyChange(String entryPoint, CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource configFile = null;
        // lock the config file first, to avoid half done changes
        if (change.hasDetailPageInfos()) {
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(entryPoint));
            if (!configData.isModuleConfiguration() && (configData.getResource() != null)) {
                configFile = configData.getResource();
            }
            if (configFile != null) {
                ensureLock(configFile);
            }
        }
        if (change.isNew()) {
            CmsClientSitemapEntry newEntry = createNewEntry(entryPoint, change);
            change.setUpdatedEntry(newEntry);
            change.setEntryId(newEntry.getId());
        } else if (change.getChangeType() == ChangeType.delete) {
            delete(change);
        } else if (change.getEntryId() != null) {
            modifyEntry(change);
        }
        if (change.hasDetailPageInfos() && (configFile != null)) {
            saveDetailPages(change.getDetailPageInfos(), configFile, change.getEntryId());
            tryUnlock(configFile);
        }

        return change;
    }

    /**
     * Changes the navigation for a moved entry and its neighbors.<p>
     * 
     * @param change the sitemap change 
     * @param entryFolder the moved entry 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void applyNavigationChanges(CmsSitemapChange change, CmsResource entryFolder) throws CmsException {

        CmsObject cms = getCmsObject();
        String parentPath = null;
        if (change.hasNewParent()) {
            CmsResource parent = cms.readResource(change.getParentId());
            parentPath = cms.getSitePath(parent);
        } else {
            parentPath = CmsResource.getParentFolder(cms.getSitePath(entryFolder));
        }
        List<CmsJspNavElement> navElements = getNavBuilder().getNavigationForFolder(
            parentPath,
            true,
            CmsResourceFilter.ONLY_VISIBLE);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(navElements, entryFolder, change.getPosition());
        List<CmsJspNavElement> navs = npc.getNavigationChanges();
        List<CmsResource> needToUnlock = new ArrayList<CmsResource>();

        try {
            for (CmsJspNavElement nav : navs) {
                LockInfo lockInfo = ensureLockAndGetInfo(nav.getResource());
                if (!nav.getResource().equals(entryFolder) && lockInfo.wasJustLocked()) {
                    needToUnlock.add(nav.getResource());
                }
            }
            for (CmsJspNavElement nav : navs) {
                CmsProperty property = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_NAVPOS,
                    "" + nav.getNavPosition(),
                    null);
                cms.writePropertyObject(cms.getSitePath(nav.getResource()), property);
            }
        } finally {
            for (CmsResource lockedRes : needToUnlock) {
                try {
                    cms.unlockResource(lockedRes);
                } catch (CmsException e) {
                    // we catch this because we still want to unlock the other resources 
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Creates a client property bean from a server-side property.<p>
     * 
     * @param prop the property from which to create the client property
     * @param preserveOrigin if true, the origin will be copied into the new object
     *  
     * @return the new client property
     */
    private CmsClientProperty createClientProperty(CmsProperty prop, boolean preserveOrigin) {

        CmsClientProperty result = new CmsClientProperty(
            prop.getName(),
            prop.getStructureValue(),
            prop.getResourceValue());
        if (preserveOrigin) {
            result.setOrigin(prop.getOrigin());
        }
        return result;
    }

    /**
     * Creates a new page in navigation.<p>
     * 
     * @param entryPoint the site-map entry-point
     * @param change the new change
     * 
     * @return the updated entry 
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry createNewEntry(String entryPoint, CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsClientSitemapEntry newEntry = null;
        if (change.getParentId() != null) {
            CmsResource parentFolder = cms.readResource(change.getParentId(), CmsResourceFilter.ONLY_VISIBLE);
            String entryPath = "";
            CmsResource entryFolder = null;
            CmsResource newRes = null;
            byte[] content = null;
            List<CmsProperty> properties = Collections.emptyList();
            CmsResource copyPage = null;
            if (change.getNewCopyResourceId() != null) {
                copyPage = cms.readResource(change.getNewCopyResourceId(), CmsResourceFilter.ONLY_VISIBLE);
                content = cms.readFile(copyPage).getContents();
                properties = cms.readPropertyObjects(copyPage, false);
            }
            if (isRedirectType(change.getNewResourceTypeId())) {
                entryPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName());
                newRes = cms.createResource(
                    entryPath,
                    change.getNewResourceTypeId(),
                    null,
                    Collections.singletonList(new CmsProperty(
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        change.getName(),
                        null)));
                cms.writePropertyObjects(newRes, generateInheritProperties(change, newRes));
                applyNavigationChanges(change, newRes);
            } else {
                String entryFolderPath = CmsStringUtil.joinPaths(cms.getSitePath(parentFolder), change.getName() + "/");
                boolean idWasNull = change.getEntryId() == null;
                // we don'T really need to create a folder object here anymore.
                if (idWasNull) {
                    // need this for calculateNavPosition, even though the id will get overwritten 
                    change.setEntryId(new CmsUUID());
                }

                boolean isFunctionDetail = false;
                if (change.getCreateParameter() != null) {
                    if (CmsUUID.isValidUUID(change.getCreateParameter())) {
                        isFunctionDetail = true;
                    }
                }
                entryFolder = new CmsResource(
                    change.getEntryId(),
                    new CmsUUID(),
                    entryFolderPath,
                    CmsResourceTypeFolder.getStaticTypeId(),
                    true,
                    0,
                    cms.getRequestContext().getCurrentProject().getUuid(),
                    CmsResource.STATE_NEW,
                    System.currentTimeMillis(),
                    cms.getRequestContext().getCurrentUser().getId(),
                    System.currentTimeMillis(),
                    cms.getRequestContext().getCurrentUser().getId(),
                    CmsResource.DATE_RELEASED_DEFAULT,
                    CmsResource.DATE_EXPIRED_DEFAULT,
                    1,
                    0,
                    System.currentTimeMillis(),
                    0);
                List<CmsProperty> folderProperties = generateInheritProperties(change, entryFolder);
                entryFolder = cms.createResource(
                    entryFolderPath,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId(),
                    null,
                    folderProperties);
                if (idWasNull) {
                    change.setEntryId(entryFolder.getStructureId());
                }
                applyNavigationChanges(change, entryFolder);
                entryPath = CmsStringUtil.joinPaths(entryFolderPath, "index.html");
                boolean isContainerPage = change.getNewResourceTypeId() == CmsResourceTypeXmlContainerPage.getContainerPageTypeIdSafely();
                if (isContainerPage && (copyPage != null)) {

                    // do *NOT* get this from the cache, because we perform some destructive operation on the XML content 
                    CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(
                        cms,
                        cms.readFile(copyPage),
                        true,
                        true);
                    ensureSingleLocale(page, entryFolder);
                    if (isFunctionDetail) {
                        String functionDetailContainer = getFunctionDetailContainerName(parentFolder);
                        CmsUUID functionStructureId = new CmsUUID(change.getCreateParameter());
                        CmsResource functionFormatter = cms.readResource(
                            CmsXmlDynamicFunctionHandler.FORMATTER_PATH,
                            CmsResourceFilter.ONLY_VISIBLE);
                        addFunctionDetailElement(
                            cms,
                            page,
                            functionDetailContainer,
                            functionStructureId,
                            functionFormatter.getStructureId());
                    }
                    content = page.marshal();
                }
                newRes = cms.createResource(entryPath, change.getNewResourceTypeId(), content, properties);
                cms.writePropertyObjects(newRes, generateOwnProperties(change));

            }

            if (entryFolder != null) {
                tryUnlock(entryFolder);
                String sitePath = cms.getSitePath(entryFolder);
                newEntry = toClientEntry(
                    getNavBuilder().getNavigationForResource(sitePath, CmsResourceFilter.ONLY_VISIBLE),
                    false);
                newEntry.setSubEntries(getChildren(sitePath, 1, null), null);
                newEntry.setChildrenLoadedInitially(true);
            }
            if (newRes != null) {
                tryUnlock(newRes);
            }
            if (newEntry == null) {
                newEntry = toClientEntry(
                    getNavBuilder().getNavigationForResource(cms.getSitePath(newRes), CmsResourceFilter.ONLY_VISIBLE),
                    false);
            }
            // mark position as not set
            newEntry.setPosition(-1);
            newEntry.setNew(true);
            change.getClipBoardData().getModifications().remove(null);
            change.getClipBoardData().getModifications().put(newEntry.getId(), newEntry);
        }
        return newEntry;
    }

    /**
     * Creates a new resource info to a given model page resource.<p>
     * 
     * @param cms the current CMS context 
     * @param modelResource the model page resource
     * @param locale the locale used for retrieving descriptions/titles
     * 
     * @return the new resource info
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsNewResourceInfo createNewResourceInfo(CmsObject cms, CmsResource modelResource, Locale locale)
    throws CmsException {

        // if model page got overwritten by another resource, reread from site path
        if (!cms.existsResource(modelResource.getStructureId(), CmsResourceFilter.ONLY_VISIBLE)) {
            modelResource = cms.readResource(cms.getSitePath(modelResource), CmsResourceFilter.ONLY_VISIBLE);
        }
        int typeId = modelResource.getTypeId();
        String name = OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
        String title = cms.readPropertyObject(modelResource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        String description = cms.readPropertyObject(modelResource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue();

        try {
            CmsGallerySearchResult result = CmsGallerySearch.searchById(cms, modelResource.getStructureId(), locale);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(result.getTitle())) {
                title = result.getTitle();
            }
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(result.getDescription())) {
                description = result.getDescription();
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }

        boolean editable = false;
        try {
            CmsResource freshModelResource = cms.readResource(
                modelResource.getStructureId(),
                CmsResourceFilter.ONLY_VISIBLE);
            editable = cms.hasPermissions(
                freshModelResource,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.DEFAULT);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        CmsNewResourceInfo info = new CmsNewResourceInfo(
            typeId,
            name,
            title,
            description,
            modelResource.getStructureId(),
            editable,
            description);
        Float navpos = null;
        try {
            CmsProperty navposProp = cms.readPropertyObject(modelResource, CmsPropertyDefinition.PROPERTY_NAVPOS, true);
            String navposStr = navposProp.getValue();
            if (navposStr != null) {
                try {
                    navpos = Float.valueOf(navposStr);
                } catch (NumberFormatException e) {
                    // noop 
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }

        info.setNavPos(navpos);
        info.setDate(CmsDateUtil.getDate(
            new Date(modelResource.getDateLastModified()),
            DateFormat.LONG,
            getWorkplaceLocale()));
        info.setVfsPath(modelResource.getRootPath());
        return info;
    }

    /**
     * Creates a resource type info bean for a given resource type.<p>
     * 
     * @param resType the resource type
     * @param copyResource the structure id of the copy resource
     *  
     * @return the resource type info bean
     */
    private CmsNewResourceInfo createResourceTypeInfo(I_CmsResourceType resType, CmsResource copyResource) {

        String name = resType.getTypeName();
        Locale locale = getWorkplaceLocale();
        String subtitle = CmsWorkplaceMessages.getResourceTypeDescription(locale, name);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subtitle)) {
            subtitle = CmsWorkplaceMessages.getResourceTypeName(locale, name);
        }
        if (copyResource != null) {
            return new CmsNewResourceInfo(
                copyResource.getTypeId(),
                name,
                CmsWorkplaceMessages.getResourceTypeName(locale, name),
                CmsWorkplaceMessages.getResourceTypeDescription(locale, name),
                copyResource.getStructureId(),
                false,
                subtitle);
        } else {
            return new CmsNewResourceInfo(resType.getTypeId(), name, CmsWorkplaceMessages.getResourceTypeName(
                locale,
                name), CmsWorkplaceMessages.getResourceTypeDescription(locale, name), null, false, subtitle);
        }
    }

    /**
     * Deletes a resource according to the change data.<p>
     * 
     * @param change the change data
     * 
     * @return CmsClientSitemapEntry always null
     * 
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry delete(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE);
        ensureLock(resource);
        cms.deleteResource(cms.getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
        tryUnlock(resource);
        return null;
    }

    /**
     * Generates a client side lock info object representing the current lock state of the given resource.<p>
     * 
     * @param resource the resource
     * 
     * @return the client lock
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsClientLock generateClientLock(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsLock lock = cms.getLock(resource);
        CmsClientLock clientLock = new CmsClientLock();
        clientLock.setLockType(CmsClientLock.LockType.valueOf(lock.getType().getMode()));
        CmsUUID ownerId = lock.getUserId();
        if (!lock.isUnlocked() && (ownerId != null)) {
            clientLock.setLockOwner(cms.readUser(ownerId).getDisplayName(cms, cms.getRequestContext().getLocale()));
            clientLock.setOwnedByUser(cms.getRequestContext().getCurrentUser().getId().equals(ownerId));
        }
        return clientLock;
    }

    /**
     * Generates a list of property object to save to the sitemap entry folder to apply the given change.<p>
     * 
     * @param change the change
     * @param entryFolder the entry folder
     * 
     * @return the property objects
     */
    private List<CmsProperty> generateInheritProperties(CmsSitemapChange change, CmsResource entryFolder) {

        List<CmsProperty> result = new ArrayList<CmsProperty>();
        Map<String, CmsClientProperty> clientProps = change.getOwnInternalProperties();
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }
        result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, change.getName(), null));
        return result;
    }

    /**
     * Generates a list of property object to save to the sitemap entry resource to apply the given change.<p>
     * 
     * @param change the change
     * 
     * @return the property objects
     */
    private List<CmsProperty> generateOwnProperties(CmsSitemapChange change) {

        List<CmsProperty> result = new ArrayList<CmsProperty>();

        Map<String, CmsClientProperty> clientProps = change.getDefaultFileProperties();
        if (clientProps != null) {
            for (CmsClientProperty clientProp : clientProps.values()) {
                CmsProperty prop = new CmsProperty(
                    clientProp.getName(),
                    clientProp.getStructureValue(),
                    clientProp.getResourceValue());
                result.add(prop);
            }
        }
        result.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, change.getName(), null));
        return result;
    }

    /**
     * Generates a list of property values inherited to the site-map root entry.<p>
     * 
     * @param rootPath the root entry name
     * 
     * @return the list of property values inherited to the site-map root entry
     * 
     * @throws CmsException if something goes wrong reading the properties
     */
    private Map<String, CmsClientProperty> generateParentProperties(String rootPath) throws CmsException {

        CmsObject cms = getCmsObject();
        if (rootPath == null) {
            rootPath = cms.getRequestContext().addSiteRoot("/");
        }
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        String parentRootPath = CmsResource.getParentFolder(rootPath);

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        if (parentRootPath != null) {
            List<CmsProperty> props = rootCms.readPropertyObjects(parentRootPath, true);
            for (CmsProperty prop : props) {
                CmsClientProperty clientProp = createClientProperty(prop, true);
                result.put(clientProp.getName(), clientProp);
            }
        }
        return result;
    }

    /**
     * Returns the sitemap children for the given path with all descendants up to the given level or to the given target path, ie. 
     * <dl><dt>levels=1 </dt><dd>only children</dd><dt>levels=2</dt><dd>children and great children</dd></dl>
     * and so on.<p>
     * 
     * @param root the site relative root
     * @param levels the levels to recurse
     * @param targetPath the target path
     * 
     * @return the sitemap children
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsClientSitemapEntry> getChildren(String root, int levels, String targetPath) throws CmsException {

        List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>();
        int i = 0;
        for (CmsJspNavElement navElement : getNavBuilder().getNavigationForFolder(
            root,
            true,
            CmsResourceFilter.ONLY_VISIBLE)) {
            CmsClientSitemapEntry child = toClientEntry(navElement, false);
            if (child != null) {
                child.setPosition(i);
                children.add(child);
                int nextLevels = levels;
                if ((nextLevels == 1) && (targetPath != null) && targetPath.startsWith(child.getSitePath())) {
                    nextLevels = 2;
                }
                if (child.isFolderType() && ((nextLevels > 1) || (nextLevels == -1)) && !isSubSitemap(navElement)) {

                    child.setSubEntries(getChildren(child.getSitePath(), nextLevels - 1, targetPath), null);
                    child.setChildrenLoadedInitially(true);
                }
                i++;
            }
        }
        return children;
    }

    /**
     * Returns the clipboard data from the current user.<p>
     * 
     * @return the clipboard data
     */
    private CmsSitemapClipboardData getClipboardData() {

        CmsSitemapClipboardData result = new CmsSitemapClipboardData();
        result.setModifications(getModifiedList());
        result.setDeletions(getDeletedList());
        return result;
    }

    /**
     * Returns the deleted list from the current user.<p>
     * 
     * @return the deleted list
     */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getDeletedList() {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_DELETED_LIST);
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> result = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        CmsUUID delId = new CmsUUID(array.getString(i));
                        CmsResource res = cms.readResource(delId, CmsResourceFilter.ALL);
                        if (res.getState().isDeleted()) {
                            // make sure resource is still deleted
                            CmsClientSitemapEntry delEntry = new CmsClientSitemapEntry();
                            delEntry.setSitePath(cms.getSitePath(res));
                            delEntry.setOwnProperties(getClientProperties(cms, res, false));
                            delEntry.setName(res.getName());
                            delEntry.setVfsPath(cms.getSitePath(res));
                            delEntry.setEntryType(res.isFolder() ? EntryType.folder : isRedirectType(res.getTypeId())
                            ? EntryType.redirect
                            : EntryType.leaf);
                            delEntry.setId(delId);
                            result.put(delId, delEntry);
                        }
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        }
        return result;
    }

    /**
     * Gets the type id for entry point folders.<p>
     * 
     * @return the type id for entry point folders 
     * 
     * @throws CmsException if something goes wrong 
     */
    private int getEntryPointType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolderExtended.TYPE_ENTRY_POINT).getTypeId();
    }

    /**
     * Gets the container name for function detail elements depending on the parent folder.<p>
     * 
     * @param parent the parent folder 
     * @return the name of the function detail container
     */
    private String getFunctionDetailContainerName(CmsResource parent) {

        try {
            CmsObject cms = getCmsObject();
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsProperty templateProp = cms.readPropertyObject(parent, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
            String templateVal = templateProp.getValue();
            if (templateVal == null) {
                return null;
            }
            CmsResource templateRes;
            try {
                templateRes = cms.readResource(templateVal, CmsResourceFilter.ONLY_VISIBLE);
            } catch (CmsVfsResourceNotFoundException e) {
                templateRes = rootCms.readResource(templateVal, CmsResourceFilter.ONLY_VISIBLE);
            }
            CmsProperty containerInfoProp = cms.readPropertyObject(
                templateRes,
                CmsPropertyDefinition.PROPERTY_CONTAINER_INFO,
                true);
            String containerInfo = containerInfoProp.getValue() == null ? "" : containerInfoProp.getValue();
            Map<String, String> attrs = CmsStringUtil.splitAsMap(containerInfo, "|", "=");
            String functionDetailContainerName = attrs.get(KEY_FUNCTION_DETAIL);
            return functionDetailContainerName;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns the modified list from the current user.<p>
     * 
     * @return the modified list
     */
    private LinkedHashMap<CmsUUID, CmsClientSitemapEntry> getModifiedList() {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_MODIFIED_LIST);
        LinkedHashMap<CmsUUID, CmsClientSitemapEntry> result = new LinkedHashMap<CmsUUID, CmsClientSitemapEntry>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        CmsUUID modId = new CmsUUID(array.getString(i));
                        CmsResource res = cms.readResource(modId, CmsResourceFilter.ONLY_VISIBLE);
                        String sitePath = cms.getSitePath(res);
                        CmsJspNavElement navEntry = getNavBuilder().getNavigationForResource(
                            sitePath,
                            CmsResourceFilter.ONLY_VISIBLE);
                        if (navEntry.isInNavigation()) {
                            CmsClientSitemapEntry modEntry = toClientEntry(navEntry, false);
                            result.put(modId, modEntry);
                        }
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        }
        return result;
    }

    /**
     * Returns a navigation builder reference.<p>
     * 
     * @return the navigation builder
     */
    private CmsJspNavBuilder getNavBuilder() {

        if (m_navBuilder == null) {
            m_navBuilder = new CmsJspNavBuilder(getCmsObject());
        }
        return m_navBuilder;
    }

    /**
     * Returns the new resource infos.<p>
     * 
     * @param cms the current CMS context 
     * @param configData the configuration data from which the new resource infos should be read 
     * @param locale locale used for retrieving descriptions/titles
     * 
     * @return the new resource infos
     */
    private List<CmsNewResourceInfo> getNewResourceInfos(CmsObject cms, CmsADEConfigData configData, Locale locale) {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        for (CmsModelPageConfig modelConfig : configData.getModelPages()) {
            try {
                result.add(createNewResourceInfo(cms, modelConfig.getResource(), locale));
            } catch (CmsException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        Collections.sort(result, new Comparator<CmsNewResourceInfo>() {

            public int compare(CmsNewResourceInfo a, CmsNewResourceInfo b) {

                return ComparisonChain.start().compare(a.getNavPos(), b.getNavPos(), Ordering.natural().nullsLast()).result();
            }
        });
        return result;
    }

    /**
     * Gets the names of all available properties.<p>
     * 
     * @param cms the CMS context 
     * 
     * @return the list of all property names 
     *  
     * @throws CmsException
     */
    private List<String> getPropertyNames(CmsObject cms) throws CmsException {

        List<CmsPropertyDefinition> propDefs = cms.readAllPropertyDefinitions();
        List<String> result = new ArrayList<String>();
        for (CmsPropertyDefinition propDef : propDefs) {
            result.add(propDef.getName());
        }
        return result;
    }

    /**
     * Gets the resource type info beans for types for which new detail pages can be created.<p>
     * 
     * @param cms the current CMS context
     * @param resourceTypeConfigs the resource type configurations
     * @param functionReferences the function references
     * @param modelResource the model resource
     * @param locale the locale used for retrieving descriptions/titles  
     * 
     * @return the resource type info beans for types for which new detail pages can be created 
     */
    private List<CmsNewResourceInfo> getResourceTypeInfos(
        CmsObject cms,
        List<CmsResourceTypeConfig> resourceTypeConfigs,
        List<CmsFunctionReference> functionReferences,
        CmsResource modelResource,
        Locale locale) {

        List<CmsNewResourceInfo> result = new ArrayList<CmsNewResourceInfo>();
        for (CmsResourceTypeConfig typeConfig : resourceTypeConfigs) {
            if (typeConfig.isDetailPagesDisabled()) {
                continue;
            }
            String typeName = typeConfig.getTypeName();
            try {
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeName);
                result.add(createResourceTypeInfo(resourceType, modelResource));
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        for (CmsFunctionReference functionRef : functionReferences) {
            try {
                CmsResource functionRes = cms.readResource(functionRef.getStructureId());
                String description = cms.readPropertyObject(
                    functionRes,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false).getValue();
                String subtitle = description;
                try {
                    CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(
                        cms,
                        functionRef.getStructureId(),
                        getWorkplaceLocale());
                    subtitle = searchResult.getDescription();
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }

                CmsNewResourceInfo info = new CmsNewResourceInfo(
                    modelResource.getTypeId(),
                    CmsDetailPageInfo.FUNCTION_PREFIX + functionRef.getName(),
                    functionRef.getName(),
                    description,
                    modelResource.getStructureId(),
                    false,
                    subtitle);
                info.setCreateParameter(functionRef.getStructureId().toString());
                info.setIsFunction(true);
                result.add(info);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Reeds the site root entry.<p>
     * 
     * @param rootPath the root path of the sitemap root
     * @param targetPath the target path to open
     * 
     * @return the site root entry
     * 
     * @throws CmsSecurityException in case of insufficient permissions
     * @throws CmsException if something goes wrong
     */
    private CmsClientSitemapEntry getRootEntry(String rootPath, String targetPath)
    throws CmsSecurityException, CmsException {

        String sitePath = "/";
        if (rootPath != null) {
            sitePath = getCmsObject().getRequestContext().removeSiteRoot(rootPath);
        }
        CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(sitePath, CmsResourceFilter.ONLY_VISIBLE);
        CmsClientSitemapEntry result = toClientEntry(navElement, true);
        if (result != null) {
            result.setPosition(0);
            result.setChildrenLoadedInitially(true);
            result.setSubEntries(getChildren(sitePath, 2, targetPath), null);
        }
        return result;
    }

    /**
     * Returns the sitemap info for the given base path.<p>
     * 
     * @param basePath the base path
     * 
     * @return the sitemap info
     * 
     * @throws CmsException if something goes wrong reading the resources
     */
    private CmsSitemapInfo getSitemapInfo(String basePath) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource baseFolder = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(basePath)) {
            baseFolder = cms.readResource(
                cms.getRequestContext().removeSiteRoot(basePath),
                CmsResourceFilter.ONLY_VISIBLE);
        } else {
            // in case of an empty base path, use base folder of the current site
            basePath = "/";
            baseFolder = cms.readResource("/");
        }
        CmsResource defaultFile = cms.readDefaultFile(baseFolder, CmsResourceFilter.ONLY_VISIBLE);
        String title = cms.readPropertyObject(baseFolder, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title) && (defaultFile != null)) {
            title = cms.readPropertyObject(defaultFile, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        }
        String description = cms.readPropertyObject(baseFolder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(description) && (defaultFile != null)) {
            description = cms.readPropertyObject(defaultFile, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue();
        }
        return new CmsSitemapInfo(
            cms.getRequestContext().getCurrentProject().getName(),
            description,
            OpenCms.getLocaleManager().getDefaultLocale(cms, baseFolder).toString(),
            OpenCms.getSiteManager().getCurrentSite(cms).getUrl()
                + OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, basePath),
            title);
    }

    /**
     * Returns the workplace locale for the current user.<p>
     * 
     * @return the workplace locale
     */
    private Locale getWorkplaceLocale() {

        Locale result = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        if (result == null) {
            result = CmsLocaleManager.getDefaultLocale();
        }
        if (result == null) {
            result = Locale.getDefault();
        }
        return result;
    }

    /**
     * Checks whether the sitemap change has default file changes.<p>
     * 
     * @param change a sitemap change 
     * @return true if the change would change the default file 
     */
    private boolean hasDefaultFileChanges(CmsSitemapChange change) {

        return (change.getDefaultFileId() != null) && !change.isNew();
        //TODO: optimize this!
    }

    /**
     * Checks whether the sitemap change has changes for the sitemap entry resource.<p>
     * 
     * @param change the sitemap change 
     * @return true if the change would change the original sitemap entry resource 
     */
    private boolean hasOwnChanges(CmsSitemapChange change) {

        return !change.isNew();
        //TODO: optimize this! 
    }

    /**
     * Checks whether a resource is a default file of a folder.<p>
     * 
     * @param resource the resource to check 
     * 
     * @return true if the resource is the default file of a folder 
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean isDefaultFile(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        if (resource.isFolder()) {
            return false;
        }

        CmsResource parent = cms.readResource(
            CmsResource.getParentFolder(cms.getSitePath(resource)),
            CmsResourceFilter.ONLY_VISIBLE);
        CmsResource defaultFile = cms.readDefaultFile(parent, CmsResourceFilter.ONLY_VISIBLE);
        return resource.equals(defaultFile);
    }

    /**
     * Checks if the toolbar should be displayed.<p>
     * 
     * @param request the current request to get the default locale from 
     * 
     * @return <code>true</code> if the toolbar should be displayed
     */
    private boolean isDisplayToolbar(HttpServletRequest request) {

        // display the toolbar by default
        boolean displayToolbar = true;
        if (CmsHistoryResourceHandler.isHistoryRequest(request)) {
            // we do not want to display the toolbar in case of an historical request
            displayToolbar = false;
        }
        return displayToolbar;
    }

    /**
     * Returns if the given type id matches the xml-redirect resource type.<p>
     * 
     * @param typeId the resource type id
     * 
     * @return <code>true</code> if the given type id matches the xml-redirect resource type
     */
    private boolean isRedirectType(int typeId) {

        try {
            return typeId == OpenCms.getResourceManager().getResourceType(RECOURCE_TYPE_NAME_REDIRECT).getTypeId();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns if the given nav-element resembles a sub-sitemap entry-point.<p>
     * 
     * @param navElement the nav-element
     * 
     * @return <code>true</code> if the given nav-element resembles a sub-sitemap entry-point.<p>
     * @throws CmsException if something goes wrong
     */
    private boolean isSubSitemap(CmsJspNavElement navElement) throws CmsException {

        return navElement.getResource().getTypeId() == getEntryPointType();
    }

    /**
     * Checks if the given open path is valid.<p>
     * 
     * @param cms the cms context
     * @param openPath the open path
     * 
     * @return <code>true</code> if the given open path is valid
     */
    private boolean isValidOpenPath(CmsObject cms, String openPath) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(openPath)) {
            return false;
        }
        if (!cms.existsResource(openPath)) {
            // in case of a detail-page check the parent folder
            String parent = CmsResource.getParentFolder(openPath);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(parent) || !cms.existsResource(parent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns if the given return code is valid.<p>
     * 
     * @param returnCode the return code to check
     * 
     * @return <code>true</code> if the return code is valid
     */
    private boolean isValidReturnCode(String returnCode) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(returnCode)) {
            return false;
        }
        int pos = returnCode.indexOf(":");
        if (pos > 0) {
            return CmsUUID.isValidUUID(returnCode.substring(0, pos))
                && CmsUUID.isValidUUID(returnCode.substring(pos + 1));
        } else {
            return CmsUUID.isValidUUID(returnCode);
        }
    }

    /**
     * Applys the given changes to the entry.<p>
     * 
     * @param change the change to apply
     * 
     * @throws CmsException if something goes wrong
     */
    private void modifyEntry(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryPage = null;
        CmsResource entryFolder = null;

        CmsResource ownRes = null;
        CmsResource defaultFileRes = null;
        try {
            // lock all resources necessary first to avoid doing changes only half way through

            if (hasOwnChanges(change)) {
                ownRes = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE);
                ensureLock(ownRes);
            }

            if (hasDefaultFileChanges(change)) {
                defaultFileRes = cms.readResource(change.getDefaultFileId(), CmsResourceFilter.ONLY_VISIBLE);
                ensureLock(defaultFileRes);
            }

            if ((ownRes != null) && ownRes.isFolder()) {
                entryFolder = ownRes;
            }

            if ((ownRes != null) && ownRes.isFile()) {
                entryPage = ownRes;
            }

            if (defaultFileRes != null) {
                entryPage = defaultFileRes;
            }

            if (change.isLeafType()) {
                entryFolder = entryPage;
            }

            updateProperties(cms, ownRes, defaultFileRes, change.getPropertyChanges());
            if (change.hasChangedPosition()) {
                updateNavPos(ownRes, change);
            }

            if (entryFolder != null) {
                if (change.hasNewParent() || change.hasChangedName()) {
                    String destinationPath;
                    if (change.hasNewParent()) {
                        CmsResource futureParent = cms.readResource(
                            change.getParentId(),
                            CmsResourceFilter.ONLY_VISIBLE);
                        destinationPath = CmsStringUtil.joinPaths(cms.getSitePath(futureParent), change.getName());
                    } else {
                        destinationPath = CmsStringUtil.joinPaths(
                            CmsResource.getParentFolder(cms.getSitePath(entryFolder)),
                            change.getName());
                    }
                    if (change.isLeafType() && destinationPath.endsWith("/")) {
                        destinationPath = destinationPath.substring(0, destinationPath.length() - 1);
                    }
                    // only if the site-path has really changed
                    if (!cms.getSitePath(entryFolder).equals(destinationPath)) {
                        cms.moveResource(cms.getSitePath(entryFolder), destinationPath);
                    }
                    entryFolder = cms.readResource(entryFolder.getStructureId(), CmsResourceFilter.ONLY_VISIBLE);
                }
            }
        } finally {
            if (entryPage != null) {
                tryUnlock(entryPage);
            }
            if (entryFolder != null) {
                tryUnlock(entryFolder);
            }
        }
    }

    /**
     * Helper method for removing all locales except one from a container page.<p>
     * 
     * @param page the container page to proces
     * @param localeToKeep the locale which should be kept 
     * 
     * @throws CmsXmlException if something goes wrong
     */
    private void removeAllLocalesExcept(CmsXmlContainerPage page, Locale localeToKeep) throws CmsXmlException {

        List<Locale> locales = page.getLocales();
        for (Locale locale : locales) {
            if (!locale.equals(localeToKeep)) {
                page.removeLocale(locale);
            }
        }
    }

    /**
     * Applys the given remove change.<p>
     * 
     * @param change the change to apply
     * 
     * @return the changed client sitemap entry or <code>null</code>
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange removeEntryFromNavigation(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource entryFolder = cms.readResource(change.getEntryId(), CmsResourceFilter.ONLY_VISIBLE);
        ensureLock(entryFolder);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            CmsProperty.DELETE_VALUE,
            CmsProperty.DELETE_VALUE));
        properties.add(new CmsProperty(
            CmsPropertyDefinition.PROPERTY_NAVPOS,
            CmsProperty.DELETE_VALUE,
            CmsProperty.DELETE_VALUE));
        cms.writePropertyObjects(cms.getSitePath(entryFolder), properties);
        tryUnlock(entryFolder);
        return change;
    }

    /**
     * Saves the detail page information of a sitemap to the sitemap's configuration file.<p>
     * 
     * @param detailPages saves the detailpage configuration
     * @param resource the configuration file resource
     * @param newId the structure id to use for new detail page entries 
     * 
     * @throws CmsException
     */
    private void saveDetailPages(List<CmsDetailPageInfo> detailPages, CmsResource resource, CmsUUID newId)
    throws CmsException {

        CmsObject cms = getCmsObject();
        CmsDetailPageConfigurationWriter writer = new CmsDetailPageConfigurationWriter(cms, resource);
        writer.updateAndSave(detailPages, newId);
    }

    /**
     * Saves the given clipboard data to the session.<p>
     * 
     * @param clipboardData the clipboard data to save
     * 
     * @throws CmsException if something goes wrong writing the user 
     */
    private void setClipboardData(CmsSitemapClipboardData clipboardData) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();
        if (clipboardData != null) {
            JSONArray modified = new JSONArray();
            if (clipboardData.getModifications() != null) {
                for (CmsUUID id : clipboardData.getModifications().keySet()) {
                    if (id != null) {
                        modified.put(id.toString());
                    }
                }
            }
            user.setAdditionalInfo(ADDINFO_ADE_MODIFIED_LIST, modified.toString());
            JSONArray deleted = new JSONArray();
            if (clipboardData.getDeletions() != null) {
                for (CmsUUID id : clipboardData.getDeletions().keySet()) {
                    if (id != null) {
                        deleted.put(id.toString());
                    }
                }
            }
            user.setAdditionalInfo(ADDINFO_ADE_DELETED_LIST, deleted.toString());
            cms.writeUser(user);
        }
    }

    /**
     * Determines if the title property of the default file should be changed.<p>
     * 
     * @param properties the current default file properties
     * @param folderNavtext the 'NavText' property of the folder
     * 
     * @return <code>true</code> if the title property should be changed
     */
    private boolean shouldChangeDefaultFileTitle(Map<String, CmsProperty> properties, CmsProperty folderNavtext) {

        return (properties == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE) == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue() == null)
            || ((folderNavtext != null) && properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(
                folderNavtext.getValue()));
    }

    /**
     * Determines if the title property should be changed in case of a 'NavText' change.<p>
     * 
     * @param properties the current resource properties
     * 
     * @return <code>true</code> if the title property should be changed in case of a 'NavText' change
     */
    private boolean shouldChangeTitle(Map<String, CmsProperty> properties) {

        return (properties == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE) == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue() == null)
            || ((properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null) && properties.get(
                CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(
                properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT).getValue()));
    }

    /**
     * Converts a jsp navigation element into a client sitemap entry.<p>
     * 
     * @param navElement the jsp navigation element
     * @param isRoot true if the entry is a root entry
     * 
     * @return the client sitemap entry 
     * @throws CmsException 
     */
    private CmsClientSitemapEntry toClientEntry(CmsJspNavElement navElement, boolean isRoot) throws CmsException {

        CmsResource entryPage = null;
        CmsObject cms = getCmsObject();
        CmsClientSitemapEntry clientEntry = new CmsClientSitemapEntry();
        CmsResource entryFolder = null;

        CmsResource ownResource = navElement.getResource();
        clientEntry.setResourceState(ownResource.getState());
        CmsResource defaultFileResource = null;
        if (ownResource.isFolder()) {
            defaultFileResource = cms.readDefaultFile(ownResource, CmsResourceFilter.ONLY_VISIBLE);
        }

        Map<String, CmsClientProperty> ownProps = getClientProperties(cms, ownResource, false);

        Map<String, CmsClientProperty> defaultFileProps = null;
        if (defaultFileResource != null) {
            defaultFileProps = getClientProperties(cms, defaultFileResource, false);
            clientEntry.setDefaultFileId(defaultFileResource.getStructureId());
            clientEntry.setDefaultFileType(OpenCms.getResourceManager().getResourceType(defaultFileResource.getTypeId()).getTypeName());
        } else {
            defaultFileProps = new HashMap<String, CmsClientProperty>();
        }
        boolean isDefault = isDefaultFile(ownResource);
        clientEntry.setId(ownResource.getStructureId());
        clientEntry.setFolderDefaultPage(isDefault);
        if (navElement.getResource().isFolder()) {
            entryFolder = navElement.getResource();
            entryPage = defaultFileResource;
            clientEntry.setName(entryFolder.getName());
            if (entryPage == null) {
                entryPage = entryFolder;
            }
            if (!isRoot && isSubSitemap(navElement)) {
                clientEntry.setEntryType(EntryType.subSitemap);
                clientEntry.setDefaultFileType(null);
            }
            CmsLock folderLock = cms.getLock(entryFolder);
            clientEntry.setHasForeignFolderLock(!folderLock.isUnlocked()
                && !folderLock.isOwnedBy(cms.getRequestContext().getCurrentUser()));
            if (!cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                List<CmsResource> blockingChildren = cms.getBlockingLockedResources(entryFolder);
                clientEntry.setBlockingLockedChildren((blockingChildren != null) && !blockingChildren.isEmpty());
            }
        } else {
            entryPage = navElement.getResource();
            clientEntry.setName(entryPage.getName());
            if (isRedirectType(entryPage.getTypeId())) {
                clientEntry.setEntryType(EntryType.redirect);
                CmsFile file = cms.readFile(entryPage);
                I_CmsXmlDocument content = CmsXmlContentFactory.unmarshal(cms, file);
                Locale contentLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, entryPage);
                // ensure the content contains the default locale
                contentLocale = content.getBestMatchingLocale(contentLocale);
                if (contentLocale == null) {
                    // no best matching locale, use the first available
                    List<Locale> locales = content.getLocales();
                    if (!locales.isEmpty()) {
                        contentLocale = locales.get(0);
                    }
                }
                String link = "";
                if (contentLocale != null) {
                    link = content.getValue(REDIRECT_LINK_TARGET_XPATH, contentLocale).getStringValue(getCmsObject());
                }
                clientEntry.setRedirectTarget(link);
            } else {
                clientEntry.setEntryType(EntryType.leaf);
            }
        }
        long dateExpired = navElement.getResource().getDateExpired();
        if (dateExpired != CmsResource.DATE_EXPIRED_DEFAULT) {
            clientEntry.setDateExpired(CmsDateUtil.getDate(
                new Date(dateExpired),
                DateFormat.SHORT,
                getWorkplaceLocale()));
        }
        long dateReleased = navElement.getResource().getDateReleased();
        if (dateReleased != CmsResource.DATE_RELEASED_DEFAULT) {
            clientEntry.setDateReleased(CmsDateUtil.getDate(
                new Date(dateReleased),
                DateFormat.SHORT,
                getWorkplaceLocale()));
        }
        clientEntry.setResleasedAndNotExpired(navElement.getResource().isReleasedAndNotExpired(
            System.currentTimeMillis()));
        String path = cms.getSitePath(entryPage);
        clientEntry.setVfsPath(path);
        clientEntry.setOwnProperties(ownProps);
        clientEntry.setDefaultFileProperties(defaultFileProps);
        clientEntry.setSitePath(entryFolder != null ? cms.getSitePath(entryFolder) : path);
        //CHECK: assuming that, if entryPage refers to the default file, the lock state of the folder 
        clientEntry.setLock(generateClientLock(entryPage));
        clientEntry.setInNavigation(isRoot || navElement.isInNavigation());
        String type = OpenCms.getResourceManager().getResourceType(ownResource).getTypeName();
        clientEntry.setResourceTypeName(type);
        return clientEntry;
    }

    /**
     * Un-deletes a resource according to the change data.<p>
     * 
     * @param change the change data
     * 
     * @return the changed entry or <code>null</code>
     *  
     * @throws CmsException if something goes wrong
     */
    private CmsSitemapChange undelete(CmsSitemapChange change) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource deleted = cms.readResource(change.getEntryId(), CmsResourceFilter.ALL);
        if (deleted.getState().isDeleted()) {
            ensureLock(deleted);
            cms.undeleteResource(getCmsObject().getSitePath(deleted), true);
            tryUnlock(deleted);
        }
        String parentPath = CmsResource.getParentFolder(cms.getSitePath(deleted));
        CmsJspNavElement navElement = getNavBuilder().getNavigationForResource(
            parentPath,
            CmsResourceFilter.ONLY_VISIBLE);
        CmsClientSitemapEntry entry = toClientEntry(navElement, navElement.isInNavigation());
        entry.setSubEntries(getChildren(parentPath, 2, null), null);
        change.setUpdatedEntry(entry);
        return change;
    }

    /**
     * Updates the navigation position for a resource.<p>
     * 
     * @param res the resource for which to update the navigation position
     * @param change the sitemap change
     *  
     * @throws CmsException if something goes wrong 
     */
    private void updateNavPos(CmsResource res, CmsSitemapChange change) throws CmsException {

        if (change.hasChangedPosition()) {
            applyNavigationChanges(change, res);
        }
    }

    /**
     * Updates properties for a resource and possibly its detail page.<p>
     *     
     * @param cms the CMS context 
     * @param ownRes the resource 
     * @param defaultFileRes the default file resource (possibly null) 
     * @param propertyModifications the property modifications 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void updateProperties(
        CmsObject cms,
        CmsResource ownRes,
        CmsResource defaultFileRes,
        List<CmsPropertyModification> propertyModifications) throws CmsException {

        Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(ownRes, false));
        // determine if the title property should be changed in case of a 'NavText' change
        boolean changeOwnTitle = shouldChangeTitle(ownProps);

        boolean changeDefaultFileTitle = false;
        Map<String, CmsProperty> defaultFileProps = Collections.emptyMap();
        if (defaultFileRes != null) {
            defaultFileProps = getPropertiesByName(cms.readPropertyObjects(defaultFileRes, false));
            // determine if the title property of the default file should be changed
            changeDefaultFileTitle = shouldChangeDefaultFileTitle(
                defaultFileProps,
                ownProps.get(CmsPropertyDefinition.PROPERTY_NAVTEXT));
        }
        String hasNavTextChange = null;
        List<CmsProperty> ownPropertyChanges = new ArrayList<CmsProperty>();
        List<CmsProperty> defaultFilePropertyChanges = new ArrayList<CmsProperty>();
        for (CmsPropertyModification propMod : propertyModifications) {
            CmsProperty propToModify = null;
            if (ownRes.getStructureId().equals(propMod.getId())) {

                if (CmsPropertyDefinition.PROPERTY_NAVTEXT.equals(propMod.getName())) {
                    hasNavTextChange = propMod.getValue();
                } else if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propMod.getName())) {
                    changeOwnTitle = false;
                }
                propToModify = ownProps.get(propMod.getName());
                if (propToModify == null) {
                    propToModify = new CmsProperty(propMod.getName(), null, null);
                }
                ownPropertyChanges.add(propToModify);
            } else {
                if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propMod.getName())) {
                    changeDefaultFileTitle = false;
                }
                propToModify = defaultFileProps.get(propMod.getName());
                if (propToModify == null) {
                    propToModify = new CmsProperty(propMod.getName(), null, null);
                }
                defaultFilePropertyChanges.add(propToModify);
            }

            String newValue = propMod.getValue();
            if (newValue == null) {
                newValue = "";
            }
            if (propMod.isStructureValue()) {
                propToModify.setStructureValue(newValue);
            } else {
                propToModify.setResourceValue(newValue);
            }
        }
        if (hasNavTextChange != null) {
            if (changeOwnTitle) {
                CmsProperty titleProp = ownProps.get(CmsPropertyDefinition.PROPERTY_TITLE);
                if (titleProp == null) {
                    titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, null);
                }
                titleProp.setStructureValue(hasNavTextChange);
                ownPropertyChanges.add(titleProp);
            }
            if (changeDefaultFileTitle) {
                CmsProperty titleProp = defaultFileProps.get(CmsPropertyDefinition.PROPERTY_TITLE);
                if (titleProp == null) {
                    titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, null);
                }
                titleProp.setStructureValue(hasNavTextChange);
                defaultFilePropertyChanges.add(titleProp);
            }
        }
        if (!ownPropertyChanges.isEmpty()) {
            cms.writePropertyObjects(ownRes, ownPropertyChanges);
        }
        if (!defaultFilePropertyChanges.isEmpty() && (defaultFileRes != null)) {
            cms.writePropertyObjects(defaultFileRes, defaultFilePropertyChanges);
        }
    }
}
