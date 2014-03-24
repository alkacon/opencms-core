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

package org.opencms.gwt;

import org.opencms.ade.galleries.CmsPreviewService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsClientDateBean;
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.gwt.shared.CmsPrincipalBean;
import org.opencms.gwt.shared.CmsRenameInfoBean;
import org.opencms.gwt.shared.CmsReplaceInfo;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsRestoreInfoBean;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

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

    /** A helper object containing the implementations of the alias-related service methods. */
    private CmsAliasHelper m_aliasHelper = new CmsAliasHelper();

    /** Initialize the preview mime types. */
    static {
        CollectionUtils.addAll(m_previewMimeTypes, (new String[] {
            "application/msword",
            "application/pdf",
            "application/excel",
            "application/mspowerpoint",
            "application/zip"}));
    }

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

        CmsResourceUtil resourceUtil = new CmsResourceUtil(cms, resource);
        CmsLock lock = resourceUtil.getLock();
        LockIcon icon = LockIcon.NONE;
        String iconTitle = null;
        CmsLockType lockType = lock.getType();
        if (!lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)) {
                icon = LockIcon.CLOSED;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_CLOSED;
            }
        } else {
            if ((lockType == CmsLockType.EXCLUSIVE)
                || (lockType == CmsLockType.INHERITED)
                || (lockType == CmsLockType.TEMPORARY)) {
                icon = LockIcon.OPEN;
            } else if ((lockType == CmsLockType.SHARED_EXCLUSIVE) || (lockType == CmsLockType.SHARED_INHERITED)) {
                icon = LockIcon.SHARED_OPEN;
            }
        }
        if ((lock.getUserId() != null) && !lock.getUserId().isNullUUID()) {
            CmsUser lockOwner = cms.readUser(lock.getUserId());
            iconTitle = Messages.get().getBundle().key(Messages.GUI_LOCKED_BY_1, lockOwner.getFullName());
            resourceInfo.addAdditionalInfo(
                Messages.get().getBundle().key(Messages.GUI_LOCKED_OWNER_0),
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

        String noPreviewReason = null;
        if (resource.getState().isDeleted()) {
            noPreviewReason = Messages.get().getBundle().key(Messages.GUI_NO_PREVIEW_DELETED_0);
        } else if (resource.isFolder()) {
            noPreviewReason = Messages.get().getBundle().key(Messages.GUI_NO_PREVIEW_FOLDER_0);
        } else {
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(resource.getRootPath());
            // previewing only resources that are in the same site or don't have a site root at all
            if ((siteRoot != null) && !siteRoot.equals(cms.getRequestContext().getSiteRoot())) {
                noPreviewReason = Messages.get().getBundle().key(Messages.GUI_NO_PREVIEW_OTHER_SITE_0);
            } else if (resource.getTypeId() == CmsResourceTypeBinary.getStaticTypeId()) {
                String mimeType = OpenCms.getResourceManager().getMimeType(resource.getName(), null, "empty");
                if (!m_previewMimeTypes.contains(mimeType)) {
                    noPreviewReason = Messages.get().getBundle().key(Messages.GUI_NO_PREVIEW_WRONG_MIME_TYPE_0);
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
     * @throws CmsException
     * @throws CmsLoaderException
     */
    public static CmsListInfoBean getPageInfo(CmsObject cms, CmsResource res) throws CmsException, CmsLoaderException {

        CmsListInfoBean result = new CmsListInfoBean();

        result.setResourceState(res.getState());

        String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            result.setTitle(title);
        } else {
            result.setTitle(res.getName());
        }
        result.setSubTitle(cms.getSitePath(res));
        String secure = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue();
        if (Boolean.parseBoolean(secure)) {
            result.setStateIcon(CmsListInfoBean.StateIcon.secure);
        } else {
            String export = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_EXPORT, true).getValue();
            if (Boolean.parseBoolean(export)) {
                result.setStateIcon(CmsListInfoBean.StateIcon.export);
            } else {
                result.setStateIcon(CmsListInfoBean.StateIcon.standard);
            }
        }
        String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName).getKey();
        Locale currentLocale = cms.getRequestContext().getLocale();
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(currentLocale);
        String resTypeNiceName = messages.key(key);
        result.addAdditionalInfo(messages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TYPE_0), resTypeNiceName);
        result.setResourceType(resTypeName);
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
        String path = resolver.resolveMacros(pathWithMacros).replaceAll("/+", "/");
        return path;
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
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
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
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getAvailabilityInfo(org.opencms.util.CmsUUID)
     */
    public CmsAvailabilityInfoBean getAvailabilityInfo(CmsUUID structureId) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return getAvailabilityInfo(res);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getAvailabilityInfo(java.lang.String)
     */
    public CmsAvailabilityInfoBean getAvailabilityInfo(String vfsPath) throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(vfsPath, CmsResourceFilter.IGNORE_EXPIRATION);
            return getAvailabilityInfo(res);
        } catch (Throwable e) {
            error(e);
            return null; // will never be reached
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
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getDefaultProperties(java.util.List)
     */
    public Map<CmsUUID, Map<String, CmsXmlContentProperty>> getDefaultProperties(List<CmsUUID> structureIds)
    throws CmsRpcException {

        try {
            return internalGetDefaultProperties(structureIds);
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
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getHistoryPreviewInfo(org.opencms.util.CmsUUID, java.lang.String, int)
     */
    public CmsPreviewInfo getHistoryPreviewInfo(CmsUUID structureId, String locale, int version) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource previewResource = (CmsResource)(cms.readResource(structureId, version));
            CmsFile previewFile = cms.readFile(previewResource);

            return getPreviewInfo(previewFile, CmsLocaleManager.getLocale(locale));
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
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
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
            CmsResource res = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
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
            CmsHistoryResourceCollection result = new CmsHistoryResourceCollection();
            CmsObject cms = getCmsObject();
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(resource);
            for (I_CmsHistoryResource historyRes : versions) {
                CmsHistoryResourceBean historyBean = createHistoryResourceBean(cms, historyRes);
                result.add(historyBean);
            }
            CmsListInfoBean info = getPageInfo(structureId);
            result.setContentInfo(info);
            return result;
        } catch (Exception e) {
            error(e);
            return null; // return statement will  never be reached
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getResourceStatus(org.opencms.util.CmsUUID, java.lang.String, boolean, java.util.List)
     */
    public CmsResourceStatusBean getResourceStatus(
        CmsUUID structureId,
        String contentLocale,
        boolean includeTargets,
        List<CmsUUID> additionalTargets) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsDefaultResourceStatusProvider provider = new CmsDefaultResourceStatusProvider();
            return provider.getResourceStatus(cms, structureId, contentLocale, includeTargets, additionalTargets);
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
     * Updates properties for a resource and possibly its detail page.<p>
     *
     * @param cms the CMS context
     * @param ownRes the resource
     * @param propertyModifications the property modifications
     *
     * @throws CmsException if something goes wrong
     */
    public void internalUpdateProperties(
        CmsObject cms,
        CmsResource ownRes,
        List<CmsPropertyModification> propertyModifications) throws CmsException {

        Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(ownRes, false));
        // determine if the title property should be changed in case of a 'NavText' change
        boolean changeOwnTitle = shouldChangeTitle(ownProps);

        String hasNavTextChange = null;
        List<CmsProperty> ownPropertyChanges = new ArrayList<CmsProperty>();
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
                throw new IllegalStateException("Invalid structure id in property changes!");
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
        }
        if (!ownPropertyChanges.isEmpty()) {
            cms.writePropertyObjects(ownRes, ownPropertyChanges);
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#loadPropertyData(org.opencms.util.CmsUUID)
     */
    public CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            return internalLoadPropertyData(cms, id);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#prepareEdit(org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsPrepareEditResponse prepareEdit(CmsUUID currentPageId, String pathWithMacros) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsResource currentPage = cms.readResource(currentPageId, CmsResourceFilter.IGNORE_EXPIRATION);
            String path = prepareFileNameForEditor(cms, currentPage, pathWithMacros);
            CmsResource resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
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

        CmsObject cms = getCmsObject();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        try {
            CmsResource.checkResourceName(newName);
        } catch (CmsIllegalArgumentException e) {
            return e.getLocalizedMessage(locale);
        }
        CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        String oldSitePath = cms.getSitePath(resource);
        String parentPath = CmsResource.getParentFolder(oldSitePath);
        String newSitePath = CmsStringUtil.joinPaths(parentPath, newName);
        try {
            ensureLock(resource);
            cms.moveResource(oldSitePath, newSitePath);
            resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
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
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#saveProperties(org.opencms.gwt.shared.property.CmsPropertyChangeSet)
     */
    public void saveProperties(CmsPropertyChangeSet changes) throws CmsRpcException {

        String origSiteRoot = getCmsObject().getRequestContext().getSiteRoot();
        try {
            getCmsObject().getRequestContext().setSiteRoot("");
            internalSaveProperties(changes);
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
     * Converts CmsProperty objects to CmsClientProperty objects.<p>
     *
     * @param properties a list of server-side properties
     *
     * @return a map of client-side properties
     */
    protected Map<String, CmsClientProperty> convertProperties(List<CmsProperty> properties) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : properties) {
            CmsClientProperty clientProp = new CmsClientProperty(
                prop.getName(),
                prop.getStructureValue(),
                prop.getResourceValue());
            clientProp.setOrigin(prop.getOrigin());
            result.put(clientProp.getName(), clientProp);
        }
        return result;
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
        return new CmsBrokenLinkBean(title, subtitle, typeName);
    }

    /**
     * Helper method to get the default property configuration for the given resource type.<p>
     *
     * @param typeName the name of the resource type
     *
     * @return the default property configuration for the given type
     */
    protected Map<String, CmsXmlContentProperty> getDefaultPropertiesForType(String typeName) {

        Map<String, CmsXmlContentProperty> propertyConfig = new LinkedHashMap<String, CmsXmlContentProperty>();
        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
        if (explorerType != null) {
            List<String> defaultProps = explorerType.getProperties();
            for (String propName : defaultProps) {
                CmsXmlContentProperty property = new CmsXmlContentProperty(
                    propName,
                    "string",
                    "string",
                    "",
                    "",
                    "",
                    "",
                    null,
                    "",
                    "",
                    "false");
                propertyConfig.put(propName, property);
            }
        }
        return propertyConfig;
    }

    /**
     * Internal method for computing the default property configurations for a list of structure ids.<p>
     *
     * @param structureIds the structure ids for which we want the default property configurations
     * @return a map from the given structure ids to their default property configurations
     *
     * @throws CmsException if something goes wrong
     */
    protected Map<CmsUUID, Map<String, CmsXmlContentProperty>> internalGetDefaultProperties(List<CmsUUID> structureIds)
    throws CmsException {

        CmsObject cms = getCmsObject();
        Map<CmsUUID, Map<String, CmsXmlContentProperty>> result = Maps.newHashMap();
        for (CmsUUID structureId : structureIds) {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
            Map<String, CmsXmlContentProperty> propertyConfig = getDefaultPropertiesForType(typeName);
            result.put(structureId, propertyConfig);
        }
        return result;
    }

    /**
     * Saves a set of property changes.<p>
     *
     * @param changes the set of property changes
     * @throws CmsException if something goes wrong
     */
    protected void internalSaveProperties(CmsPropertyChangeSet changes) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource target = cms.readResource(changes.getTargetStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
        CmsLockActionRecord actionRecord = ensureLock(cms.getSitePath(target));
        try {
            internalUpdateProperties(cms, target, changes.getChanges());
        } finally {
            if (actionRecord.getChange() == LockChange.locked) {
                cms.unlockResource(cms.getSitePath(target));
            }
        }

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
     * Creates a bean representing a historical resource version.<p>
     * 
     * @param cms the current CMS context
     * @param historyRes the historical resource
     *  
     * @return the bean representing the historical resource  
     * @throws CmsException if something goes wrong 
     */
    private CmsHistoryResourceBean createHistoryResourceBean(CmsObject cms, I_CmsHistoryResource historyRes)
    throws CmsException {

        CmsHistoryResourceBean result = new CmsHistoryResourceBean();
        List<CmsProperty> historyProperties = cms.readHistoryPropertyObjects(historyRes);
        Map<String, CmsProperty> historyPropertyMap = CmsProperty.toObjectMap(historyProperties);
        CmsProperty titleProp = CmsProperty.wrapIfNull(historyPropertyMap.get(CmsPropertyDefinition.PROPERTY_TITLE));
        CmsProperty descProp = CmsProperty.wrapIfNull(historyPropertyMap.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        result.setStructureId(historyRes.getStructureId());
        result.setTitle(titleProp.getValue());
        result.setDescription(descProp.getValue());
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
        int publishTag = historyRes.getPublishTag();
        CmsHistoryProject project = cms.readHistoryProject(publishTag);
        long publishDate = project.getPublishingDate();
        result.setDatePublished(formatDate(publishDate, locale));

        result.setVersion(historyRes.getVersion());
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

            // check if any detail container page resource exists to this resource
            String detailContainers = CmsJspTagContainer.getDetailOnlyPageName(path);
            if (cms.existsResource(detailContainers, CmsResourceFilter.IGNORE_EXPIRATION)) {
                deleteResource(cms.readResource(detailContainers, CmsResourceFilter.IGNORE_EXPIRATION));
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
     * Returns a bean that contains the infos for the {@link org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog}.<p>
     *
     * @param res the resource to get the availability infos for
     *
     * @return a bean for the {@link org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog}
     *
     * @throws CmsRpcException if something goes wrong
     */
    private CmsAvailabilityInfoBean getAvailabilityInfo(CmsResource res) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            CmsAvailabilityInfoBean result = new CmsAvailabilityInfoBean();

            result.setPageInfo(getPageInfo(res));

            String resourceSitePath = cms.getRequestContext().removeSiteRoot(res.getRootPath());
            result.setVfsPath(resourceSitePath);

            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            result.setResType(type.getTypeName());

            result.setDateReleased(res.getDateReleased());
            result.setDateExpired(res.getDateExpired());

            String notificationInterval = cms.readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                false).getValue();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(notificationInterval)) {
                result.setNotificationInterval(Integer.valueOf(notificationInterval).intValue());
            }

            String notificationEnabled = cms.readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                false).getValue();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(notificationEnabled)) {
                result.setNotificationEnabled(Boolean.valueOf(notificationEnabled).booleanValue());
            }

            result.setHasSiblings(cms.readSiblings(resourceSitePath, CmsResourceFilter.ALL).size() > 1);

            result.setResponsibles(getResponsibles(res.getRootPath()));

            return result;
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached
        }
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
    @SuppressWarnings("unchecked")
    private List<CmsBrokenLinkBean> getBrokenLinkBeans(MultiValueMap linkMap) throws CmsException {

        CmsBrokenLinkRenderer brokenLinkRenderer = new CmsBrokenLinkRenderer(getCmsObject());
        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        for (CmsResource key : (Set<CmsResource>)linkMap.keySet()) {

            CmsBrokenLinkBean parentBean = createSitemapBrokenLinkBean(key);
            result.add(parentBean);
            Collection<CmsResource> values = linkMap.getCollection(key);
            for (CmsResource resource : values) {
                List<CmsBrokenLinkBean> brokenLinkBeans = brokenLinkRenderer.renderBrokenLink(key, resource);
                for (CmsBrokenLinkBean childBean : brokenLinkBeans) {
                    parentBean.addChild(childBean);
                }
            }
        }
        return result;
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
        MultiValueMap linkMap = MultiValueMap.decorate(
            new HashMap<Object, Object>(),
            FactoryUtils.instantiateFactory(HashSet.class));
        for (CmsResource resource : descendants) {
            List<CmsResource> linkSources = getLinkSources(cms, resource, deleteIds);
            for (CmsResource source : linkSources) {
                linkMap.put(resource, source);
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
     * @throws CmsException
     */
    private List<CmsResource> getLinkSources(CmsObject cms, CmsResource resource, HashSet<CmsUUID> deleteIds)
    throws CmsException {

        List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsRelation relation : relations) {
            // only add related resources that are not going to be deleted
            if (!deleteIds.contains(relation.getSourceId())) {
                result.add(relation.getSource(cms, CmsResourceFilter.IGNORE_EXPIRATION));
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
     * @param resource the resource
     * @param locale the requested locale
     *
     * @return the preview info
     */
    private CmsPreviewInfo getPreviewInfo(CmsResource resource, Locale locale) {

        CmsObject cms = getCmsObject();
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
        } else if (CmsResourceTypeImage.getStaticTypeId() == resource.getTypeId()) {
            CmsImageScaler scaler = new CmsImageScaler(cms, resource);
            previewContent = "<img src=\""
                + OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, resource.getRootPath())
                + "\" title=\""
                + title
                + "\" style=\"display:block\" />";
            height = scaler.getHeight();
            width = scaler.getWidth();
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
                    + Messages.get().getBundle().key(Messages.GUI_NO_PREVIEW_CAN_T_READ_CONTENT_0)
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
        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource) || CmsResourceTypeXmlPage.isXmlPage(resource)) {
            CmsPreviewInfo result = new CmsPreviewInfo(null, OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                resource.getRootPath())
                + "?"
                + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
                + "=true"
                + "&__locale="
                + locale.toString(), false, title, cms.getSitePath(resource), locale.toString());
            result.setLocales(locales);
            return result;
        }
        return new CmsPreviewInfo(null, OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            cms,
            resource.getRootPath())
            + "?"
            + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
            + "=true", true, title, cms.getSitePath(resource), locale.toString());
    }

    /**
     * Returns a map of principals of responsible users together with the resource path where the
     * responsibility was found.<p>
     *
     * @param vfsPath the path pointing on the resource to get the responsible users for
     *
     * @return a map of principal beans
     *
     * @throws CmsRpcException if something goes wrong
     */
    private Map<CmsPrincipalBean, String> getResponsibles(String vfsPath) throws CmsRpcException {

        Map<CmsPrincipalBean, String> result = new HashMap<CmsPrincipalBean, String>();
        List<CmsResource> parentResources = new ArrayList<CmsResource>();

        CmsObject cms = getCmsObject();
        String resourceSitePath = cms.getRequestContext().removeSiteRoot(vfsPath);
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(resourceSitePath, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            error(e);
        }

        for (CmsResource resource : parentResources) {
            String storedSiteRoot = cms.getRequestContext().getSiteRoot();
            String sitePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            try {

                cms.getRequestContext().setSiteRoot("/");
                List<CmsAccessControlEntry> entries = cms.getAccessControlEntries(resource.getRootPath(), false);
                for (CmsAccessControlEntry ace : entries) {
                    if (ace.isResponsible()) {
                        I_CmsPrincipal principal = cms.lookupPrincipal(ace.getPrincipal());
                        if (principal != null) {
                            CmsPrincipalBean prinBean = new CmsPrincipalBean(
                                principal.getName(),
                                principal.getDescription(),
                                principal.isGroup());
                            if (!resource.getRootPath().equals(vfsPath)) {
                                if (resource.getRootPath().startsWith(storedSiteRoot)) {
                                    result.put(prinBean, sitePath);
                                } else {
                                    result.put(prinBean, resource.getRootPath());
                                }
                            } else {
                                result.put(prinBean, null);
                            }
                        }
                    }
                }
            } catch (CmsException e) {
                error(e);
            } finally {
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return result;
    }

    /**
     * Loads the data needed for editing the properties of a resource.<p>
     *
     * @param cms the CMS context
     * @param id the structure id of the resource
     * @return the data needed for editing the properties
     *
     * @throws CmsException if something goes wrong
     */
    private CmsPropertiesBean internalLoadPropertyData(CmsObject cms, CmsUUID id) throws CmsException {

        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        CmsPropertiesBean result = new CmsPropertiesBean();
        CmsResource resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
        boolean hasPermissions = cms.hasPermissions(
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            CmsResourceFilter.IGNORE_EXPIRATION);
        CmsLock lock = cms.getLock(resource);
        boolean lockedByOtherUser = !lock.isUnlocked() && !lock.isOwnedBy(cms.getRequestContext().getCurrentUser());
        result.setReadOnly(!hasPermissions || lockedByOtherUser);
        result.setFolder(resource.isFolder());
        result.setContainerPage(CmsResourceTypeXmlContainerPage.isContainerPage(resource));
        String sitePath = cms.getSitePath(resource);
        Map<String, CmsXmlContentProperty> propertyConfig = OpenCms.getADEManager().lookupConfiguration(
            cms,
            resource.getRootPath()).getPropertyConfigurationAsMap();
        Map<String, CmsXmlContentProperty> defaultProperties = internalGetDefaultProperties(
            Collections.singletonList(resource.getStructureId())).get(resource.getStructureId());
        Map<String, CmsXmlContentProperty> mergedConfig = new LinkedHashMap<String, CmsXmlContentProperty>();
        mergedConfig.putAll(defaultProperties);
        mergedConfig.putAll(propertyConfig);
        propertyConfig = mergedConfig;
        result.setPropertyDefinitions(new LinkedHashMap<String, CmsXmlContentProperty>(propertyConfig));
        try {
            cms.getRequestContext().setSiteRoot("");
            String parentPath = CmsResource.getParentFolder(resource.getRootPath());
            CmsResource parent = cms.readResource(parentPath, CmsResourceFilter.IGNORE_EXPIRATION);
            List<CmsProperty> parentProperties = cms.readPropertyObjects(parent, true);
            List<CmsProperty> ownProperties = cms.readPropertyObjects(resource, false);
            result.setOwnProperties(convertProperties(ownProperties));
            result.setInheritedProperties(convertProperties(parentProperties));
            result.setPageInfo(getPageInfo(resource));
            List<CmsPropertyDefinition> propDefs = cms.readAllPropertyDefinitions();
            List<String> propNames = new ArrayList<String>();
            for (CmsPropertyDefinition propDef : propDefs) {
                propNames.add(propDef.getName());
            }
            CmsTemplateFinder templateFinder = new CmsTemplateFinder(cms);
            result.setTemplates(templateFinder.getTemplates());
            result.setAllProperties(propNames);
            result.setStructureId(id);
            result.setSitePath(sitePath);
            return result;
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
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
}
