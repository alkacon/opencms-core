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

package org.opencms.gwt;

import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.ade.galleries.CmsPreviewService;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsClientDateBean;
import org.opencms.gwt.shared.CmsDataViewConstants;
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsExternalLinkInfoBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsHistoryVersion;
import org.opencms.gwt.shared.CmsHistoryVersion.OfflineOnline;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.gwt.shared.CmsQuickLaunchParams;
import org.opencms.gwt.shared.CmsRenameInfoBean;
import org.opencms.gwt.shared.CmsReplaceInfo;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsRestoreInfoBean;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.dataview.I_CmsDataView;
import org.opencms.widgets.dataview.I_CmsDataViewItem;
import org.opencms.workplace.comparison.CmsHistoryListUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * A service class for reading the VFS tree.<p>
 *
 * @since 8.0.0
 */
public class CmsVfsService extends CmsGwtService implements I_CmsVfsService {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsService.class);

    /** The allowed preview mime types. Checked for binary content only. */
    private static Set<String> m_previewMimeTypes = new HashSet<String>();

    /** Serialization id. */
    private static final long serialVersionUID = -383483666952834348L;

    /** Initialize the preview mime types. */
    static {
        CollectionUtils.addAll(
            m_previewMimeTypes,
            (new String[] {
                "application/msword",
                "application/pdf",
                "application/excel",
                "application/mspowerpoint",
                "application/zip"}));
    }

    /** A helper object containing the implementations of the alias-related service methods. */
    private CmsAliasHelper m_aliasHelper = new CmsAliasHelper();

    /**
     * Adds the lock state information to the resource info bean.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to get the page info for
     * @param resourceInfo the resource info to add the lock state to
     *
     * @return the resource info bean
     *
     * @throws CmsException if something else goes wrong
     */
    public static CmsListInfoBean addLockInfo(CmsObject cms, CmsResource resource, CmsListInfoBean resourceInfo)
    throws CmsException {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsResourceUtil resourceUtil = new CmsResourceUtil(cms, resource);
        CmsLock lock = resourceUtil.getLock();
        LockIcon icon = LockIcon.NONE;
        String iconTitle = null;
        CmsLockType lockType = lock.getType();
        if (!lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)
                || (lockType == CmsLockType.SHALLOW)) {
                icon = LockIcon.CLOSED;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_CLOSED;
            }
        } else {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)
                || (lockType == CmsLockType.SHALLOW)) {
                icon = LockIcon.OPEN;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_OPEN;
            }
        }
        if ((lock.getUserId() != null) && !lock.getUserId().isNullUUID()) {
            CmsUser lockOwner = cms.readUser(lock.getUserId());
            iconTitle = Messages.get().getBundle(locale).key(Messages.GUI_LOCKED_BY_1, lockOwner.getFullName());
            resourceInfo.addAdditionalInfo(
                Messages.get().getBundle(locale).key(Messages.GUI_LOCKED_OWNER_0),
                lockOwner.getFullName());
        }
        resourceInfo.setLockIcon(icon);
        resourceInfo.setLockIconTitle(iconTitle);
        if (icon != LockIcon.NONE) {
            resourceInfo.setTitle(resourceInfo.getTitle() + " (" + iconTitle + ")");
        }
        return resourceInfo;
    }

    /**
     * Gets page information of a resource and adds it to the given list info bean.<p>
     *
     * @param cms the CMS context
     * @param resource the resource
     * @param listInfo the list info bean to add the information to
     *
     * @return the list info bean
     *
     * @throws CmsException if the resource info can not be read
     */
    public static CmsListInfoBean addPageInfo(CmsObject cms, CmsResource resource, CmsListInfoBean listInfo)
    throws CmsException {

        listInfo.setResourceState(resource.getState());

        String title = cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false,
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            listInfo.setTitle(title);
        } else {
            listInfo.setTitle(resource.getName());
        }
        listInfo.setSubTitle(cms.getSitePath(resource));
        listInfo.setIsFolder(Boolean.valueOf(resource.isFolder()));
        String resTypeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        CmsExplorerTypeSettings cmsExplorerTypeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            resTypeName);
        if (null == cmsExplorerTypeSettings) {
            CmsMessageContainer errMsg = Messages.get().container(
                Messages.ERR_EXPLORER_TYPE_SETTINGS_FOR_RESOURCE_TYPE_NOT_FOUND_3,
                resource.getRootPath(),
                resTypeName,
                Integer.valueOf(resource.getTypeId()));
            throw new CmsConfigurationException(errMsg);
        }
        String key = cmsExplorerTypeSettings.getKey();
        Locale currentLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(currentLocale);
        String resTypeNiceName = messages.key(key);
        listInfo.addAdditionalInfo(
            messages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TYPE_0),
            resTypeNiceName);
        listInfo.setResourceType(resTypeName);
        listInfo.setBigIconClasses(
            CmsIconUtil.getIconClasses(CmsIconUtil.getDisplayType(cms, resource), resource.getName(), false));
        // set the default file and detail type info
        String detailType = CmsResourceIcon.getDefaultFileOrDetailType(cms, resource);
        if (detailType != null) {
            listInfo.setSmallIconClasses(CmsIconUtil.getIconClasses(detailType, null, true));
        }
        return listInfo;
    }

    /**
     * Formats a date given the current user's workplace locale.<p>
     *
     * @param cms the current CMS context
     * @param date the date to format
     *
     * @return the formatted date
     */
    public static String formatDateTime(CmsObject cms, long date) {

        return CmsDateUtil.getDateTime(
            new Date(date),
            DateFormat.MEDIUM,
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
    }

    /**
     * Returns the no preview reason if there is any.<p>
     *
     * @param cms the current cms context
     * @param resource the resource to check
     *
     * @return the no preview reason if there is any
     */
    public static String getNoPreviewReason(CmsObject cms, CmsResource resource) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String noPreviewReason = null;
        if (resource.getState().isDeleted() && !(resource instanceof I_CmsHistoryResource)) {
            noPreviewReason = Messages.get().getBundle(locale).key(Messages.GUI_NO_PREVIEW_DELETED_0);
        } else if (resource.isFolder()) {
            noPreviewReason = Messages.get().getBundle(locale).key(Messages.GUI_NO_PREVIEW_FOLDER_0);
        } else {
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(resource.getRootPath());
            // previewing only resources that are in the same site or don't have a site root at all
            if ((siteRoot != null) && !siteRoot.equals(cms.getRequestContext().getSiteRoot())) {
                noPreviewReason = Messages.get().getBundle(locale).key(Messages.GUI_NO_PREVIEW_OTHER_SITE_0);
            } else if (resource.getTypeId() == CmsResourceTypeBinary.getStaticTypeId()) {
                String mimeType = OpenCms.getResourceManager().getMimeType(resource.getName(), null, "empty");
                if (!m_previewMimeTypes.contains(mimeType)) {
                    noPreviewReason = Messages.get().getBundle(locale).key(Messages.GUI_NO_PREVIEW_WRONG_MIME_TYPE_0);
                }
            }
        }
        return noPreviewReason;
    }

    /**
     * Gets page information of a resource.<p>
     *
     * @param cms the CMS context
     * @param res the resource
     *
     * @return gets the page information for the given resource
     *
     * @throws CmsException if the resource info can not be read
     */
    public static CmsListInfoBean getPageInfo(CmsObject cms, CmsResource res) throws CmsException {

        CmsListInfoBean result = new CmsListInfoBean();
        addPageInfo(cms, res, result);
        return result;
    }

    /**
     * Returns a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget} including the lock state.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to get the page info for
     *
     * @return a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     *
     * @throws CmsLoaderException if the resource type could not be found
     * @throws CmsException if something else goes wrong
     */
    public static CmsListInfoBean getPageInfoWithLock(CmsObject cms, CmsResource resource)
    throws CmsLoaderException, CmsException {

        CmsListInfoBean result = getPageInfo(cms, resource);
        addLockInfo(cms, resource, result);
        return result;
    }

    /**
     * Processes a file path, which may have macros in it, so it can be opened by the XML content editor.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the context menu option has been selected
     * @param pathWithMacros the file path which may contain macros
     *
     * @return the processed file path
     */
    public static String prepareFileNameForEditor(CmsObject cms, CmsResource res, String pathWithMacros) {

        String subsite = OpenCms.getADEManager().getSubSiteRoot(cms, res.getRootPath());
        CmsMacroResolver resolver = new CmsMacroResolver();
        if (subsite != null) {
            resolver.addMacro("subsite", cms.getRequestContext().removeSiteRoot(subsite));
        }
        resolver.addMacro("file", cms.getSitePath(res));
        String path = resolver.resolveMacros(pathWithMacros).replaceAll("/+", "/");
        return path;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#createNewExternalLink(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createNewExternalLink(String title, String link, String resourceName, String parentFolderPath)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
            @SuppressWarnings("deprecation")
            CmsResource resource = cms.createResource(
                CmsStringUtil.joinPaths(parentFolderPath, resourceName),
                CmsResourceTypePointer.getStaticTypeId(),
                new byte[0],
                Collections.singletonList(titleProp));
            CmsFile file = cms.readFile(resource);
            file.setContents(link.getBytes(CmsLocaleManager.getResourceEncoding(cms, resource)));
            cms.writeFile(file);
            tryUnlock(resource);
            // update the offline search indices
            OpenCms.getSearchManager().updateOfflineIndexes();
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#createPropertyDefinition(java.lang.String)
     */
    public void createPropertyDefinition(String name) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.createPropertyDefinition(name.trim());
        } catch (Exception e) {
            error(e);
        }

    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#deleteResource(org.opencms.util.CmsUUID)
     */
    public void deleteResource(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource res = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            deleteResource(res);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#deleteResource(java.lang.String)
     */
    public void deleteResource(String sitePath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
            deleteResource(res);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#forceUnlock(org.opencms.util.CmsUUID)
     */
    public void forceUnlock(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.ALL);
            // get the current lock
            CmsLock currentLock = getCmsObject().getLock(resource);
            // check if the resource is locked at all
            if (currentLock.getEditionLock().isUnlocked() && currentLock.getSystemLock().isUnlocked()) {
                getCmsObject().lockResourceTemporary(resource);
            } else {
                getCmsObject().changeLock(resource);
            }
            getCmsObject().unlockResource(resource);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getAliasesForPage(org.opencms.util.CmsUUID)
     */
    public List<CmsAliasBean> getAliasesForPage(CmsUUID uuid) throws CmsRpcException {

        try {
            return m_aliasHelper.getAliasesForPage(uuid);
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getBrokenLinks(org.opencms.util.CmsUUID)
     */
    public CmsDeleteResourceBean getBrokenLinks(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource entryResource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);

            return getBrokenLinks(entryResource);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getBrokenLinks(java.lang.String)
     */
    public CmsDeleteResourceBean getBrokenLinks(String sitePath) throws CmsRpcException {

        try {
            CmsResource entryResource = getCmsObject().readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);

            return getBrokenLinks(entryResource);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getChildren(java.lang.String)
     */
    public List<CmsVfsEntryBean> getChildren(String path) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> resources = new ArrayList<CmsResource>();
            resources.addAll(cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT));
            List<CmsVfsEntryBean> result = makeEntryBeans(resources, false);
            return result;
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getDataViewThumbnail(java.lang.String, java.lang.String)
     */
    public String getDataViewThumbnail(String config, String id) throws CmsRpcException {

        try {
            JSONObject obj = new JSONObject(config);
            String className = obj.optString(CmsDataViewConstants.CONFIG_VIEW_CLASS);
            String classArg = obj.optString(CmsDataViewConstants.CONFIG_VIEW_ARG);
            I_CmsDataView data = (I_CmsDataView)(Class.forName(className).newInstance());
            data.initialize(getCmsObject(), classArg, OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()));
            I_CmsDataViewItem item = data.getItemById(id);
            if (item == null) {
                LOG.warn("no dataview item found for id: " + id + " (config=" + config + ")");
                return null;
            }
            return item.getImage();
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getDefaultProperties(java.util.List)
     */
    public Map<CmsUUID, Map<String, CmsXmlContentProperty>> getDefaultProperties(List<CmsUUID> structureIds)
    throws CmsRpcException {

        try {
            CmsPropertyEditorHelper helper = new CmsPropertyEditorHelper(getCmsObject());
            return helper.getDefaultProperties(structureIds);
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getDefinedProperties()
     */
    public ArrayList<String> getDefinedProperties() throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            List<CmsPropertyDefinition> definitions = cms.readAllPropertyDefinitions();
            ArrayList<String> result = new ArrayList<String>();
            for (CmsPropertyDefinition def : definitions) {
                result.add(def.getName());
            }
            return result;
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getDetailName(org.opencms.util.CmsUUID, java.lang.String)
     */
    public String getDetailName(CmsUUID id, String localeStr) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            OpenCms.getLocaleManager();
            Locale locale = CmsLocaleManager.getLocale(localeStr);
            return cms.readBestUrlName(id, locale, OpenCms.getLocaleManager().getDefaultLocales());
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getFileReplaceInfo(org.opencms.util.CmsUUID)
     */
    public CmsReplaceInfo getFileReplaceInfo(CmsUUID structureId) throws CmsRpcException {

        CmsReplaceInfo result = null;
        try {
            CmsObject cms = getCmsObject();
            CmsResource res = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsListInfoBean fileInfo = getPageInfo(res);
            boolean isLockable = cms.getLock(res).isLockableBy(cms.getRequestContext().getCurrentUser());
            long maxFileSize = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(cms);
            result = new CmsReplaceInfo(fileInfo, cms.getSitePath(res), isLockable, maxFileSize);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getHistoryPreviewInfo(org.opencms.util.CmsUUID, java.lang.String, org.opencms.gwt.shared.CmsHistoryVersion)
     */
    public CmsPreviewInfo getHistoryPreviewInfo(CmsUUID structureId, String locale, CmsHistoryVersion versionBean)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource previewResource = null;
            if (versionBean.getVersionNumber() != null) {
                previewResource = (CmsResource)(cms.readResource(
                    structureId,
                    versionBean.getVersionNumber().intValue()));
            } else if (versionBean.isOffline()) {
                previewResource = cms.readResource(structureId, CmsResourceFilter.ALL);
            } else if (versionBean.isOnline()) {
                CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
                cms = OpenCms.initCmsObject(cms);
                cms.getRequestContext().setCurrentProject(online);
                previewResource = cms.readResource(structureId, CmsResourceFilter.ALL);
            }
            CmsFile previewFile = cms.readFile(previewResource);
            return getPreviewInfo(cms, previewFile, CmsLocaleManager.getLocale(locale));
        } catch (Exception e) {
            error(e);
            return null; // return statement will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getLockReportInfo(org.opencms.util.CmsUUID)
     */
    public CmsLockReportInfo getLockReportInfo(CmsUUID structureId) throws CmsRpcException {

        CmsLockReportInfo result = null;
        CmsObject cms = getCmsObject();
        try {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            List<CmsListInfoBean> lockedInfos = new ArrayList<CmsListInfoBean>();
            List<CmsResource> lockedResources = cms.getBlockingLockedResources(resource);
            if (lockedResources != null) {
                for (CmsResource lockedResource : lockedResources) {
                    lockedInfos.add(getPageInfoWithLock(cms, lockedResource));
                }
            }
            result = new CmsLockReportInfo(getPageInfoWithLock(cms, resource), lockedInfos);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPageInfo(org.opencms.util.CmsUUID)
     */
    public CmsListInfoBean getPageInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.ALL);
            return getPageInfo(res);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPageInfo(java.lang.String)
     */
    public CmsListInfoBean getPageInfo(String vfsPath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(vfsPath, CmsResourceFilter.IGNORE_EXPIRATION);
            return getPageInfo(res);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPreviewInfo(org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsPreviewInfo getPreviewInfo(CmsUUID structureId, String locale) throws CmsRpcException {

        CmsPreviewInfo result = null;
        try {
            result = getPreviewInfo(
                getCmsObject(),
                getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION),
                CmsLocaleManager.getLocale(locale));
        } catch (Exception e) {
            error(e);
        }
        return result;

    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getPreviewInfo(java.lang.String, java.lang.String)
     */
    public CmsPreviewInfo getPreviewInfo(String sitePath, String locale) throws CmsRpcException {

        CmsPreviewInfo result = null;
        try {
            result = getPreviewInfo(
                getCmsObject(),
                getCmsObject().readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION),
                CmsLocaleManager.getLocale(locale));
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getRenameInfo(org.opencms.util.CmsUUID)
     */
    public CmsRenameInfoBean getRenameInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsListInfoBean listInfo = getPageInfo(resource);
            String sitePath = cms.getSitePath(resource);
            return new CmsRenameInfoBean(sitePath, structureId, listInfo);
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getResourceHistory(org.opencms.util.CmsUUID)
     */
    public CmsHistoryResourceCollection getResourceHistory(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsHistoryResourceCollection result = getResourceHistoryInternal(structureId);
            CmsListInfoBean info = getPageInfo(structureId);
            result.setContentInfo(info);
            return result;

        } catch (Exception e) {
            error(e);
            return null; // return statement will  never be reached
        }
    }

    /**
     * Internal version of getResourceHistory.<p>
     *
     * @param structureId the structure id of the resource
     *
     * @return the resource history
     *
     * @throws CmsException if something goes wrong
     */
    public CmsHistoryResourceCollection getResourceHistoryInternal(CmsUUID structureId) throws CmsException {

        CmsHistoryResourceCollection result = new CmsHistoryResourceCollection();
        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
        List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(resource);
        if (!resource.getState().isUnchanged()) {
            result.add(createHistoryResourceBean(cms, resource, true, -1));
        }
        int maxVersion = 0;

        if (versions.isEmpty()) {
            try {
                CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
                CmsObject onlineCms = OpenCms.initCmsObject(cms);
                onlineCms.getRequestContext().setCurrentProject(online);
                CmsResource onlineResource = onlineCms.readResource(structureId, CmsResourceFilter.ALL);
                CmsHistoryResourceBean onlineResBean = createHistoryResourceBean(onlineCms, onlineResource, false, 0);
                result.add(onlineResBean);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.info(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            for (I_CmsHistoryResource historyRes : versions) {
                maxVersion = Math.max(maxVersion, historyRes.getVersion());
            }
            for (I_CmsHistoryResource historyRes : versions) {
                CmsHistoryResourceBean historyBean = createHistoryResourceBean(
                    cms,
                    (CmsResource)historyRes,
                    false,
                    maxVersion);
                result.add(historyBean);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getResourceStatus(org.opencms.util.CmsUUID, java.lang.String, boolean, org.opencms.util.CmsUUID, java.util.Map)
     */
    public CmsResourceStatusBean getResourceStatus(
        CmsUUID structureId,
        String contentLocale,
        boolean includeTargets,
        CmsUUID detailContentId,
        Map<String, String> context)
    throws CmsRpcException {

        if (context == null) {
            context = new HashMap<>();
        }
        try {
            CmsObject cms = getCmsObject();
            CmsDefaultResourceStatusProvider provider = new CmsDefaultResourceStatusProvider();
            return provider.getResourceStatus(
                getRequest(),
                cms,
                structureId,
                contentLocale,
                includeTargets,
                detailContentId,
                detailContentId != null ? Collections.singletonList(detailContentId) : null,
                context);
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getRestoreInfo(org.opencms.util.CmsUUID)
     */
    public CmsRestoreInfoBean getRestoreInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsListInfoBean listInfo = getPageInfo(resource);
            CmsRestoreInfoBean result = new CmsRestoreInfoBean();
            result.setListInfoBean(listInfo);

            CmsObject onlineCms = OpenCms.initCmsObject(cms);
            CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_NAME);
            onlineCms.getRequestContext().setCurrentProject(onlineProject);
            CmsResource onlineResource = onlineCms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            result.setOnlinePath(onlineResource.getRootPath());
            result.setOfflinePath(resource.getRootPath());

            String offlineDate = formatDateTime(resource.getDateLastModified());
            String onlineDate = formatDateTime(onlineResource.getDateLastModified());
            result.setOfflineDate(offlineDate);
            result.setOnlineDate(onlineDate);
            result.setStructureId(structureId);

            CmsObject offlineRootCms = OpenCms.initCmsObject(cms);
            offlineRootCms.getRequestContext().setSiteRoot("");
            CmsObject onlineRootCms = OpenCms.initCmsObject(onlineCms);
            onlineRootCms.getRequestContext().setSiteRoot("");
            String parent = CmsResource.getParentFolder(onlineResource.getRootPath());
            boolean canUndoMove = offlineRootCms.existsResource(parent, CmsResourceFilter.IGNORE_EXPIRATION);

            result.setCanUndoMove(canUndoMove);

            return result;
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getRootEntries()
     */
    public List<CmsVfsEntryBean> getRootEntries() throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> roots = new ArrayList<CmsResource>();
            roots.add(cms.readResource("/", CmsResourceFilter.IGNORE_EXPIRATION));
            return makeEntryBeans(roots, true);
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getSitePath(org.opencms.util.CmsUUID)
     */
    public String getSitePath(CmsUUID structureId) {

        try {
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return getCmsObject().getSitePath(resource);
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessageContainer(), e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getSitePath(org.opencms.util.CmsUUID)
     */
    public List<String> getSitePaths(List<CmsUUID> ids) {

        List<String> result = new ArrayList<>();
        for (CmsUUID id : ids) {
            try {
                CmsResource resource = getCmsObject().readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                result.add(getCmsObject().getSitePath(resource));
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getStructureId(java.lang.String)
     */
    public CmsUUID getStructureId(String vfsPath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(vfsPath, CmsResourceFilter.IGNORE_EXPIRATION);
            return res.getStructureId();
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getUploadFolderInfo(java.lang.String)
     */
    public CmsListInfoBean getUploadFolderInfo(String path) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsResource res = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
            return getPageInfo(res);
        } catch (CmsVfsResourceNotFoundException e) {
            String title = CmsResource.getName(path);
            CmsListInfoBean info = new CmsListInfoBean(title, path, new ArrayList<>());
            info.setResourceType(CmsResourceTypeFolder.getStaticTypeName());
            info.setBigIconClasses(CmsIconUtil.getIconClasses(CmsResourceTypeFolder.getStaticTypeName(), "", false));
            return info;
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#loadLinkInfo(org.opencms.util.CmsUUID)
     */
    public CmsExternalLinkInfoBean loadLinkInfo(CmsUUID structureId) throws CmsRpcException {

        CmsExternalLinkInfoBean info = new CmsExternalLinkInfoBean();
        CmsObject cms = getCmsObject();
        try {
            CmsResource linkResource = cms.readResource(structureId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            addPageInfo(cms, linkResource, info);
            CmsFile linkFile = cms.readFile(linkResource);
            OpenCms.getLocaleManager();
            String link = new String(linkFile.getContents(), CmsLocaleManager.getResourceEncoding(cms, linkResource));
            info.setLink(link);
            info.setSitePath(cms.getSitePath(linkResource));
        } catch (Exception e) {
            error(e);
        }
        return info;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#loadPropertyData(org.opencms.util.CmsUUID)
     */
    public CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsPropertyEditorHelper helper = new CmsPropertyEditorHelper(cms);
            return helper.loadPropertyData(id);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#loadQuickLaunchItems(org.opencms.gwt.shared.CmsQuickLaunchParams)
     */
    public List<CmsQuickLaunchData> loadQuickLaunchItems(CmsQuickLaunchParams params) throws CmsRpcException {

        try {
            return CmsQuickLaunchProvider.getQuickLaunchData(getCmsObject(), getRequest().getSession(), params);
        } catch (Exception e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#prepareEdit(org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsPrepareEditResponse prepareEdit(CmsUUID currentPageId, String pathWithMacros) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource resource = null;
            if (cms.existsResource(pathWithMacros, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                resource = cms.readResource(pathWithMacros, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } else {
                CmsResource currentPage = cms.readResource(currentPageId, CmsResourceFilter.IGNORE_EXPIRATION);
                String path = prepareFileNameForEditor(cms, currentPage, pathWithMacros);
                resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
            }
            ensureLock(resource);
            CmsPrepareEditResponse result = new CmsPrepareEditResponse();
            result.setRootPath(resource.getRootPath());
            result.setSitePath(cms.getSitePath(resource));
            result.setStructureId(resource.getStructureId());
            return result;
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#renameResource(org.opencms.util.CmsUUID, java.lang.String)
     */
    public String renameResource(CmsUUID structureId, String newName) throws CmsRpcException {

        try {
            return renameResourceInternal(structureId, newName);
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * Internal implementation for renaming a resource.<p>
     *
     * @param structureId the structure id of the resource to rename
     * @param newName the new resource name
     * @return either null if the rename was successful, or an error message
     *
     * @throws CmsException if something goes wrong
     */
    public String renameResourceInternal(CmsUUID structureId, String newName) throws CmsException {

        newName = newName.trim();
        CmsObject rootCms = OpenCms.initCmsObject(getCmsObject());
        rootCms.getRequestContext().setSiteRoot("");
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(rootCms);
        try {
            CmsResource.checkResourceName(newName);
        } catch (CmsIllegalArgumentException e) {
            return e.getLocalizedMessage(locale);
        }
        CmsResource resource = rootCms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        String oldPath = resource.getRootPath();
        String parentPath = CmsResource.getParentFolder(oldPath);
        String newPath = CmsStringUtil.joinPaths(parentPath, newName);
        try {
            ensureLock(resource);
            rootCms.moveResource(oldPath, newPath);
            resource = rootCms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(rootCms));
        }
        tryUnlock(resource);
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#restoreResource(org.opencms.util.CmsUUID, int)
     */
    public void restoreResource(CmsUUID structureId, int version) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            ensureLock(structureId);
            cms.restoreResourceVersion(structureId, version);
            try {
                CmsResource res = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                cms.unlockResource(res);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        } catch (Exception e) {
            error(e);
            return; // return stmt  will never be reached
        }

    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#saveAliases(org.opencms.util.CmsUUID, java.util.List)
     */
    public void saveAliases(CmsUUID structureId, List<CmsAliasBean> aliasBeans) throws CmsRpcException {

        try {
            m_aliasHelper.saveAliases(structureId, aliasBeans);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#saveExternalLink(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String)
     */
    public void saveExternalLink(CmsUUID structureId, String title, String link, String fileName)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource res = cms.readResource(structureId);
            ensureLock(res);
            CmsFile file = cms.readFile(res);
            String oldLink = new String(file.getContents(), CmsLocaleManager.getResourceEncoding(cms, res));
            if (!oldLink.equals(link)) {
                file.setContents(link.getBytes(CmsLocaleManager.getResourceEncoding(cms, res)));
                cms.writeFile(file);
            }
            CmsProperty titleProp = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false);
            if (titleProp.isNullProperty()) {
                titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                cms.writePropertyObject(cms.getSitePath(res), titleProp);
            } else if (!titleProp.getValue().equals(title)) {
                titleProp.setStructureValue(title);
                cms.writePropertyObject(cms.getSitePath(res), titleProp);
            }
            if (!res.getName().equals(fileName)) {
                String oldSitePath = cms.getSitePath(res);
                String newSitePath = CmsStringUtil.joinPaths(CmsResource.getParentFolder(oldSitePath), fileName);
                getCmsObject().renameResource(oldSitePath, newSitePath);
            }
            tryUnlock(res);
            // update the offline search indices
            OpenCms.getSearchManager().updateOfflineIndexes();
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#saveProperties(org.opencms.gwt.shared.property.CmsPropertyChangeSet, boolean)
     */
    public void saveProperties(CmsPropertyChangeSet changes, boolean updateIndex) throws CmsRpcException {

        String origSiteRoot = getCmsObject().getRequestContext().getSiteRoot();
        try {
            getCmsObject().getRequestContext().setSiteRoot("");
            CmsPropertyEditorHelper helper = new CmsPropertyEditorHelper(getCmsObject());
            helper.setUpdateIndex(updateIndex);
            helper.saveProperties(changes);
        } catch (Throwable t) {
            error(t);
        } finally {
            getCmsObject().getRequestContext().setSiteRoot(origSiteRoot);
        }
    }

    /**
     * Sets the current cms context.<p>
     *
     * @param cms the current cms context to set
     */
    @Override
    public synchronized void setCms(CmsObject cms) {

        super.setCms(cms);
        m_aliasHelper.setCms(cms);
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#substituteLinkForRootPath(java.lang.String, java.lang.String)
     */
    public String substituteLinkForRootPath(String currentSiteRoot, String rootPath) throws CmsRpcException {

        String result = null;
        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot(currentSiteRoot);
            result = OpenCms.getLinkManager().substituteLinkForRootPath(cms, rootPath);
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#syncDeleteResource(org.opencms.util.CmsUUID)
     */
    public void syncDeleteResource(CmsUUID structureId) throws CmsRpcException {

        deleteResource(structureId);
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#undelete(org.opencms.util.CmsUUID)
     */
    public void undelete(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            ensureLock(resource);
            cms.undeleteResource(resource.getRootPath(), true);
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#undoChanges(org.opencms.util.CmsUUID, boolean)
     */
    public void undoChanges(CmsUUID structureId, boolean undoMove) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            ensureLock(resource);
            CmsResourceUndoMode mode = undoMove ? CmsResource.UNDO_MOVE_CONTENT : CmsResource.UNDO_CONTENT;
            String path = cms.getSitePath(resource);
            cms.undoChanges(path, mode);
            try {
                resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                path = cms.getSitePath(resource);
                cms.unlockResource(path);
            } catch (CmsException e) {
                LOG.info("Could not unlock resource after undoing changes: " + e.getLocalizedMessage(), e);
            }
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#validateAliases(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, String> validateAliases(CmsUUID uuid, Map<String, String> aliasPaths) throws CmsRpcException {

        try {
            return m_aliasHelper.validateAliases(uuid, aliasPaths);
        } catch (Throwable e) {
            error(e);
        }
        return null;

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
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        String defaultTitle = "";
        String title = titleProp.getValue(defaultTitle);
        String path = cms.getSitePath(resource);
        String subtitle = path;
        String icon = CmsIconUtil.getIconClasses(CmsIconUtil.getDisplayType(cms, resource), resource.getName(), false);

        CmsBrokenLinkBean result = new CmsBrokenLinkBean(resource.getStructureId(), title, subtitle, typeName, icon);

        return result;
    }

    /**
     * Helper method for creating a VFS entry bean from a resource.<p>
     *
     * @param resource the resource whose data should be stored in the bean
     * @param root true if the resource is a root resource
     *
     * @return the data bean representing the resource
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsVfsEntryBean makeEntryBean(CmsResource resource, boolean root) throws CmsException {

        CmsObject cms = getCmsObject();
        boolean isFolder = resource.isFolder();
        String name = root ? "/" : resource.getName();
        String path = cms.getSitePath(resource);
        boolean hasChildren = false;
        if (isFolder) {
            List<CmsResource> children = cms.getResourcesInFolder(
                cms.getRequestContext().getSitePath(resource),
                CmsResourceFilter.DEFAULT);
            if (!children.isEmpty()) {
                hasChildren = true;
            }
        }
        String resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();

        return new CmsVfsEntryBean(path, name, resourceType, isFolder, hasChildren);
    }

    /**
     * Helper method for creating a list of VFS entry beans from a list of the corresponding resources.<p>
     *
     * @param resources the list of resources which should be converted to entry beans
     * @param root true if the resources in the list are root resources
     *
     * @return the list of VFS entry beans for the resources
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsVfsEntryBean> makeEntryBeans(List<CmsResource> resources, boolean root) throws CmsException {

        List<CmsVfsEntryBean> result = new ArrayList<CmsVfsEntryBean>();
        for (CmsResource res : resources) {
            result.add(makeEntryBean(res, root));
        }
        return result;
    }

    /**
     * Adds additional info items for broken links.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource from which the additional infos should be read
     * @param result the result in which to store the additional info
     */
    private void addBrokenLinkAdditionalInfo(CmsObject cms, CmsResource resource, CmsBrokenLinkBean result) {

        String dateLastModifiedLabel = org.opencms.workplace.commons.Messages.get().getBundle(
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                org.opencms.workplace.commons.Messages.GUI_LABEL_DATE_LAST_MODIFIED_0);
        String dateLastModified = CmsVfsService.formatDateTime(cms, resource.getDateLastModified());

        String userLastModifiedLabel = org.opencms.workplace.commons.Messages.get().getBundle(
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                org.opencms.workplace.commons.Messages.GUI_LABEL_USER_LAST_MODIFIED_0);
        String userLastModified = "" + resource.getUserLastModified();
        try {
            userLastModified = cms.readUser(resource.getUserLastModified()).getName();
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        result.addInfo(dateLastModifiedLabel, dateLastModified);
        result.addInfo(userLastModifiedLabel, userLastModified);
    }

    /**
     * Creates a bean representing a historical resource version.<p>
     *
     * @param cms the current CMS context
     * @param historyRes the historical resource
     * @param offline true if this resource was read from the offline project
     * @param maxVersion the largest version number found
     *
     * @return the bean representing the historical resource
     * @throws CmsException if something goes wrong
     */
    private CmsHistoryResourceBean createHistoryResourceBean(
        CmsObject cms,
        CmsResource historyRes,
        boolean offline,
        int maxVersion)
    throws CmsException {

        CmsHistoryResourceBean result = new CmsHistoryResourceBean();

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        result.setStructureId(historyRes.getStructureId());
        result.setRootPath(historyRes.getRootPath());
        result.setDateLastModified(formatDate(historyRes.getDateLastModified(), locale));
        CmsUUID userId = historyRes.getUserLastModified();
        String userName = userId.toString();
        try {
            CmsUser user = cms.readUser(userId);
            userName = user.getName();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        result.setUserLastModified(userName);
        result.setSize(historyRes.getLength());
        if (historyRes instanceof I_CmsHistoryResource) {
            int publishTag = ((I_CmsHistoryResource)historyRes).getPublishTag();
            CmsHistoryProject project = cms.readHistoryProject(publishTag);
            long publishDate = project.getPublishingDate();
            result.setDatePublished(formatDate(publishDate, locale));
            int version = ((I_CmsHistoryResource)historyRes).getVersion();
            result.setVersion(
                new CmsHistoryVersion(
                    Integer.valueOf(historyRes.getVersion()),
                    maxVersion == version ? OfflineOnline.online : null));

            List<CmsProperty> historyProperties = cms.readHistoryPropertyObjects((I_CmsHistoryResource)historyRes);
            Map<String, CmsProperty> historyPropertyMap = CmsProperty.toObjectMap(historyProperties);
            CmsProperty titleProp = CmsProperty.wrapIfNull(
                historyPropertyMap.get(CmsPropertyDefinition.PROPERTY_TITLE));
            CmsProperty descProp = CmsProperty.wrapIfNull(
                historyPropertyMap.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION));
            result.setTitle(titleProp.getValue());
            result.setDescription(descProp.getValue());
        } else {
            if (offline) {
                result.setVersion(new CmsHistoryVersion(null, OfflineOnline.offline));
            } else {
                result.setVersion(new CmsHistoryVersion(null, OfflineOnline.online));
            }
        }
        return result;
    }

    /**
     * Internal method to delete the given resource.<p>
     *
     * @param resource the resource to delete
     *
     * @throws CmsException if something goes wrong
     */
    private void deleteResource(CmsResource resource) throws CmsException {

        String path = null;
        CmsObject cms = getCmsObject();
        try {
            path = cms.getSitePath(resource);
            cms.lockResource(path);
            cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
            // check if any detail container page resources exist to this resource
            List<CmsResource> detailContainers = CmsDetailOnlyContainerUtil.getDetailOnlyResources(cms, resource);
            for (CmsResource detailContainer : detailContainers) {
                deleteResource(detailContainer);
            }
        } finally {
            try {
                if (path != null) {
                    getCmsObject().unlockResource(path);
                }
            } catch (Exception e) {
                // should really never happen
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Converts a date to a date bean.<p>
     *
     * @param date the date to convert
     * @param locale the locale to use for the conversion
     *
     * @return the date bean
     */
    private CmsClientDateBean formatDate(long date, Locale locale) {

        return new CmsClientDateBean(date, formatDateTime(date));
    }

    /**
     * Formats the date for the current user's locale.<p>
     *
     * @param date the date to format
     *
     * @return the formatted date for the current user's locale
     */
    private String formatDateTime(long date) {

        CmsObject cms = getCmsObject();
        return formatDateTime(cms, date);
    }

    /**
     * Returns the available locales mapped to there display name for the given resource
     * or <code>null</code> in case of non xml-content/xml-page resources.<p>
     *
     * @param resource the resource
     *
     * @return the available locales
     */
    private LinkedHashMap<String, String> getAvailableLocales(CmsResource resource) {

        LinkedHashMap<String, String> result = null;
        List<Locale> locales = null;
        try {
            if (CmsResourceTypeXmlPage.isXmlPage(resource)) {
                locales = CmsXmlPageFactory.unmarshal(getCmsObject(), resource, getRequest()).getLocales();
            } else if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                locales = CmsXmlContentFactory.unmarshal(getCmsObject(), resource, getRequest()).getLocales();
            } else if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                locales = CmsXmlContainerPageFactory.unmarshal(getCmsObject(), resource).getLocales();
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (locales != null) {
            Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
            result = new LinkedHashMap<String, String>();
            for (Locale locale : locales) {
                result.put(locale.toString(), locale.getDisplayName(wpLocale));
            }
        }
        return result;
    }

    /**
     * Helper method for converting a map which maps resources to resources to a list of "broken link" beans,
     * which have beans representing the source of the corresponding link as children.<p>
     *
     * @param linkMap a multimap from resource to resources
     *
     * @return a list of beans representing links which will be broken
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsBrokenLinkBean> getBrokenLinkBeans(Multimap<CmsResource, CmsResource> linkMap) throws CmsException {

        CmsBrokenLinkRenderer brokenLinkRenderer = new CmsBrokenLinkRenderer(getCmsObject());

        Multimap<CmsBrokenLinkBean, CmsBrokenLinkBean> resultMap = HashMultimap.create();

        for (CmsResource source : linkMap.keySet()) {

            for (CmsResource target : linkMap.get(source)) {
                CmsBrokenLinkBean targetBean = createSitemapBrokenLinkBean(target);
                addBrokenLinkAdditionalInfo(getCmsObject(), target, targetBean);
                List<CmsBrokenLinkBean> brokenLinkBeans = brokenLinkRenderer.renderBrokenLink(target, source);
                for (CmsBrokenLinkBean childBean : brokenLinkBeans) {
                    addBrokenLinkAdditionalInfo(getCmsObject(), source, childBean);
                    resultMap.put(childBean, targetBean);
                }
            }
        }

        // now convert multimap representation to parent/child representation
        for (CmsBrokenLinkBean parent : resultMap.keySet()) {
            for (CmsBrokenLinkBean child : resultMap.get(parent)) {
                parent.addChild(child);
            }
        }
        return Lists.newArrayList(resultMap.keySet());
    }

    /**
     * Internal method to get the broken links information for the given resource.<p>
     *
     * @param entryResource the resource
     *
     * @return the broken links information
     *
     * @throws CmsException if something goes wrong
     */
    private CmsDeleteResourceBean getBrokenLinks(CmsResource entryResource) throws CmsException {

        CmsDeleteResourceBean result = null;

        CmsListInfoBean info = null;
        List<CmsBrokenLinkBean> brokenLinks = null;

        CmsObject cms = getCmsObject();
        String resourceSitePath = cms.getSitePath(entryResource);

        ensureSession();

        List<CmsResource> descendants = new ArrayList<CmsResource>();
        HashSet<CmsUUID> deleteIds = new HashSet<CmsUUID>();

        descendants.add(entryResource);
        if (entryResource.isFolder()) {
            descendants.addAll(cms.readResources(resourceSitePath, CmsResourceFilter.IGNORE_EXPIRATION));
        }

        for (CmsResource deleteRes : descendants) {
            deleteIds.add(deleteRes.getStructureId());
        }
        Multimap<CmsResource, CmsResource> linkMap = HashMultimap.create();
        for (CmsResource resource : descendants) {
            List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
            List<CmsResource> result1 = new ArrayList<CmsResource>();
            for (CmsRelation relation : relations) {
                // only add related resources that are not going to be deleted
                if (!deleteIds.contains(relation.getSourceId())) {
                    CmsResource source1 = relation.getSource(cms, CmsResourceFilter.ALL);
                    if (!source1.getState().isDeleted()) {
                        result1.add(source1);
                    }
                }
            }
            List<CmsResource> linkSources = result1;
            for (CmsResource source : linkSources) {
                linkMap.put(source, resource);
            }
        }

        brokenLinks = getBrokenLinkBeans(linkMap);
        info = getPageInfo(entryResource);

        result = new CmsDeleteResourceBean(resourceSitePath, info, brokenLinks);

        return result;
    }

    /**
     * Gets the resources which link to a given structure id.<p>
     *
     * @param cms the current CMS context
     * @param resource the relation target resource
     * @param deleteIds set of resources to delete
     *
     * @return the list of resources which link to the given id
     *
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("unused")
    private List<CmsResource> getLinkSources(CmsObject cms, CmsResource resource, HashSet<CmsUUID> deleteIds)
    throws CmsException {

        List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsRelation relation : relations) {
            // only add related resources that are not going to be deleted
            if (!deleteIds.contains(relation.getSourceId())) {
                CmsResource source = relation.getSource(cms, CmsResourceFilter.ALL);
                if (!source.getState().isDeleted()) {
                    result.add(source);
                }
            }
        }
        return result;
    }

    /**
     * Returns a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     *
     * @param res the resource to get the page info for
     *
     * @return a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     *
     * @throws CmsLoaderException if the resource type could not be found
     * @throws CmsException if something else goes wrong
     */
    private CmsListInfoBean getPageInfo(CmsResource res) throws CmsException, CmsLoaderException {

        CmsObject cms = getCmsObject();
        return getPageInfo(cms, res);
    }

    /**
     * Returns the preview info for the given resource.<p>
     *
     *@param cms the CMS context
     * @param resource the resource
     * @param locale the requested locale
     *
     * @return the preview info
     */
    private CmsPreviewInfo getPreviewInfo(CmsObject cms, CmsResource resource, Locale locale) {

        String title = "";
        try {
            CmsProperty titleProperty = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
            title = titleProperty.getValue("");
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        String noPreviewReason = getNoPreviewReason(cms, resource);
        String previewContent = null;
        int height = 0;
        int width = 0;
        LinkedHashMap<String, String> locales = getAvailableLocales(resource);
        if (noPreviewReason != null) {
            previewContent = "<div>" + noPreviewReason + "</div>";
            return new CmsPreviewInfo(
                "<div>" + noPreviewReason + "</div>",
                null,
                false,
                title,
                cms.getSitePath(resource),
                locale.toString());
        } else if (OpenCms.getResourceManager().matchResourceType(
            CmsResourceTypeImage.getStaticTypeName(),
            resource.getTypeId())) {
            CmsImageScaler scaler = new CmsImageScaler(cms, resource);
            String imageLink = null;
            if (resource instanceof I_CmsHistoryResource) {
                int version = ((I_CmsHistoryResource)resource).getVersion();
                imageLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    cms,
                    CmsHistoryListUtil.getHistoryLink(cms, resource.getStructureId(), "" + version));
            } else {
                imageLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath());
            }
            imageLink = CmsRequestUtil.appendParameter(imageLink, "random", "" + Math.random());
            previewContent = "<img src=\"" + imageLink + "\" title=\"" + title + "\" style=\"display:block\" />";
            height = scaler.getHeight();
            width = scaler.getWidth();
        } else if (isContainerOrXmlPage(resource)) {
            String link = "";
            if (resource instanceof I_CmsHistoryResource) {
                int version = ((I_CmsHistoryResource)resource).getVersion();
                link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    cms,
                    CmsHistoryListUtil.getHistoryLink(cms, resource.getStructureId(), "" + version));
            } else if (resource.getRootPath().contains(CmsDetailOnlyContainerUtil.DETAIL_CONTAINERS_FOLDER_NAME)) {
                String detailContentPath = cms.getRequestContext().removeSiteRoot(CmsDetailOnlyContainerUtil.getDetailContentPath(resource.getRootPath()));
                if (cms.existsResource(detailContentPath, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, detailContentPath);
                } else {
                    link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath());
                }
            } else {
                link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath());
            }
            return new CmsPreviewInfo(null, link, true, null, cms.getSitePath(resource), locale.toString());
        } else if (isBinary(resource)) {
            String link = "";
            if (resource instanceof I_CmsHistoryResource) {
                int version = ((I_CmsHistoryResource)resource).getVersion();
                link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    cms,
                    CmsHistoryListUtil.getHistoryLink(cms, resource.getStructureId(), "" + version));
            } else {
                link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath());
            }
            return new CmsPreviewInfo(null, link, true, null, cms.getSitePath(resource), locale.toString());
        } else if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            if (!locales.containsKey(locale.toString())) {
                locale = CmsLocaleManager.getMainLocale(cms, resource);
            }
            previewContent = CmsPreviewService.getPreviewContent(getRequest(), getResponse(), cms, resource, locale);

        } else if (CmsResourceTypePlain.getStaticTypeId() == resource.getTypeId()) {
            try {
                previewContent = "<pre><code>" + new String(cms.readFile(resource).getContents()) + "</code></pre>";
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                previewContent = "<div>"
                    + Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                        Messages.GUI_NO_PREVIEW_CAN_T_READ_CONTENT_0)
                    + "</div>";
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(previewContent)) {
            CmsPreviewInfo result = new CmsPreviewInfo(
                previewContent,
                null,
                false,
                title,
                cms.getSitePath(resource),
                locale.toString());
            result.setHeight(height);
            result.setWidth(width);
            result.setLocales(locales);
            return result;
        }
        if (isContainerOrXmlPage(resource)) {
            CmsPreviewInfo result = new CmsPreviewInfo(
                null,
                OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath())
                    + "?"
                    + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
                    + "=true"
                    + "&__locale="
                    + locale.toString(),
                false,
                title,
                cms.getSitePath(resource),
                locale.toString());
            result.setLocales(locales);
            return result;
        }
        return new CmsPreviewInfo(
            null,
            OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath())
                + "?"
                + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
                + "=true",
            true,
            title,
            cms.getSitePath(resource),
            locale.toString());
    }

    /**
     * Checks if resource has the type 'binary'.
     *
     * @param resource the resource
     * @return true if the resource is a binary file
     */
    private boolean isBinary(CmsResource resource) {

        return OpenCms.getResourceManager().matchResourceType(
            CmsResourceTypeBinary.getStaticTypeName(),
            resource.getTypeId());
    }

    /**
     * Checks if resource is a container page or xml page.
     *
     * @param resource the resource to check
     * @return true if the resource is a container page or XML page
     */
    private boolean isContainerOrXmlPage(CmsResource resource) {

        return CmsResourceTypeXmlContainerPage.isContainerPage(resource) || CmsResourceTypeXmlPage.isXmlPage(resource);
    }

}
