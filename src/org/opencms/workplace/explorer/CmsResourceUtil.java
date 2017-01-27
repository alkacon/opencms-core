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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.A_CmsModeIntEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Provides {@link CmsResource} utility functions.<p>
 *
 * This class provides in java all resource information used by the explorer view,
 * mostly generated in javascript (see explorer.js)<p>
 *
 * @since 6.0.0
 */
public final class CmsResourceUtil {

    /**
     * Enumeration class for defining the resource project state.<p>
     */
    public static class CmsResourceProjectState extends A_CmsModeIntEnumeration {

        /** Constant for the project state unlocked. */
        protected static final CmsResourceProjectState CLEAN = new CmsResourceProjectState(0);

        /** Constant for the project state locked for publishing. */
        protected static final CmsResourceProjectState LOCKED_FOR_PUBLISHING = new CmsResourceProjectState(5);

        /** Constant for the project state locked in current project. */
        protected static final CmsResourceProjectState MODIFIED_IN_CURRENT_PROJECT = new CmsResourceProjectState(1);

        /** Constant for the project state locked in other project. */
        protected static final CmsResourceProjectState MODIFIED_IN_OTHER_PROJECT = new CmsResourceProjectState(2);

        /** serial version UID. */
        private static final long serialVersionUID = 4580450220255428716L;

        /**
         * Default constructor.<p>
         *
         * @param mode the mode descriptor
         */
        protected CmsResourceProjectState(int mode) {

            super(mode);
        }

        /**
         * Checks if this is a {@link #LOCKED_FOR_PUBLISHING} state.<p>
         *
         * @return <code>true</code> if this is a {@link #LOCKED_FOR_PUBLISHING} state
         */
        public boolean isLockedForPublishing() {

            return (this == LOCKED_FOR_PUBLISHING);
        }

        /**
         * Checks if this is a {@link #MODIFIED_IN_CURRENT_PROJECT} state.<p>
         *
         * @return <code>true</code> if this is a {@link #MODIFIED_IN_CURRENT_PROJECT} state
         */
        public boolean isModifiedInCurrentProject() {

            return (this == MODIFIED_IN_CURRENT_PROJECT);
        }

        /**
         * Checks if this is a {@link #MODIFIED_IN_OTHER_PROJECT} state.<p>
         *
         * @return <code>true</code> if this is a {@link #MODIFIED_IN_OTHER_PROJECT} state
         */
        public boolean isModifiedInOtherProject() {

            return (this == MODIFIED_IN_OTHER_PROJECT);
        }

        /**
         * Checks if this is a {@link #CLEAN} state.<p>
         *
         * @return <code>true</code> if this is a {@link #CLEAN} state
         */
        public boolean isUnlocked() {

            return (this == CLEAN);
        }
    }

    /**
     * Enumeration class for defining the site modes.<p>
     */
    private static class CmsResourceUtilSiteMode {

        /**
         * Default constructor.<p>
         */
        protected CmsResourceUtilSiteMode() {

            // noop
        }
    }

    /** Layout style for resources after expire date. */
    public static final int LAYOUTSTYLE_AFTEREXPIRE = 2;

    /** Layout style for resources before release date. */
    public static final int LAYOUTSTYLE_BEFORERELEASE = 1;

    /** Layout style for resources after release date and before expire date. */
    public static final int LAYOUTSTYLE_INRANGE = 0;

    /** Constant that signalizes that all path operations will be based on the current site. */
    public static final CmsResourceUtilSiteMode SITE_MODE_CURRENT = new CmsResourceUtilSiteMode();

    /** Constant that signalizes that all path operations will be based on the best matching site. */
    public static final CmsResourceUtilSiteMode SITE_MODE_MATCHING = new CmsResourceUtilSiteMode();

    /** Constant that signalizes that all path operations will be based on the root path. */
    public static final CmsResourceUtilSiteMode SITE_MODE_ROOT = new CmsResourceUtilSiteMode();

    /** Constant for the project state locked for publishing. */
    public static final CmsResourceProjectState STATE_LOCKED_FOR_PUBLISHING = CmsResourceProjectState.LOCKED_FOR_PUBLISHING;

    /** Constant for the project state locked in current project. */
    public static final CmsResourceProjectState STATE_MODIFIED_IN_CURRENT_PROJECT = CmsResourceProjectState.MODIFIED_IN_CURRENT_PROJECT;

    /** Constant for the project state locked in other project. */
    public static final CmsResourceProjectState STATE_MODIFIED_IN_OTHER_PROJECT = CmsResourceProjectState.MODIFIED_IN_OTHER_PROJECT;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceUtil.class);

    /** The folder size display string constant. */
    private static final String SIZE_DIR = "-";

    /** Constant for the project state unlocked. */
    private static final CmsResourceProjectState STATE_CLEAN = CmsResourceProjectState.CLEAN;

    /** If greater than zero, the path will be formatted to this number of chars. */
    private int m_abbrevLength;

    /** The current cms context. */
    private CmsObject m_cms;

    /** The current resource lock. */
    private CmsLock m_lock;

    /** The message bundle for formatting dates, depends on the request locale. */
    private CmsMessages m_messages;

    /** Reference project resources cache. */
    private List<String> m_projectResources;

    /** The project to use to check project state, if <code>null</code> the current project will be used. */
    private CmsProject m_referenceProject;

    /** The 'relative to' path. */
    private String m_relativeTo;

    /** The current request context. */
    private CmsRequestContext m_request;

    /** The current resource. */
    private CmsResource m_resource;

    /** The current resource type. */
    private I_CmsResourceType m_resourceType;

    /** The current site mode. */
    private CmsResourceUtilSiteMode m_siteMode = SITE_MODE_CURRENT;

    private Map<Locale, CmsGallerySearchResult> m_searchResultMap = Maps.newHashMap();

    /**
     * Creates a new {@link CmsResourceUtil} object.<p>
     *
     * @param cms the cms context
     */
    public CmsResourceUtil(CmsObject cms) {

        setCms(cms);
    }

    /**
     * Creates a new {@link CmsResourceUtil} object.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     */
    public CmsResourceUtil(CmsObject cms, CmsResource resource) {

        setCms(cms);
        setResource(resource);
    }

    /**
     * Creates a new {@link CmsResourceUtil} object.<p>
     *
     * @param resource the resource
     */
    public CmsResourceUtil(CmsResource resource) {

        setResource(resource);
    }

    /**
     * Returns the path abbreviation length.<p>
     *
     * If greater than zero, the path will be formatted to this number of chars.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @return the path abbreviation Length
     */
    public int getAbbrevLength() {

        return m_abbrevLength;
    }

    /**
     * Gets the full path for the big icon.<p>
     *
     * @return the full path for the big icon
     */
    public String getBigIconPath() {

        if ((m_cms != null) && CmsJspNavBuilder.isNavLevelFolder(m_cms, m_resource)) {
            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + CmsIconUtil.ICON_NAV_LEVEL_BIG);
        }
        if ((m_cms != null) && CmsResourceTypeXmlContainerPage.isModelReuseGroup(m_cms, m_resource)) {
            return CmsWorkplace.getResourceUri(
                CmsWorkplace.RES_PATH_FILETYPES + CmsIconUtil.ICON_MODEL_GROUP_REUSE_BIG);
        }
        I_CmsResourceType type = getResourceType();
        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        String typeIcon = "";
        if (explorerType != null) {
            typeIcon = CmsWorkplace.getResourceUri(
                CmsWorkplace.RES_PATH_FILETYPES + explorerType.getBigIconIfAvailable());
        }
        return typeIcon;

    }

    /**
     * Returns the cms context.<p>
     *
     * @return the cms context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the formatted date of expiration.<p>
     *
     * @return the formatted date of expiration
     */
    public String getDateExpired() {

        long release = m_resource.getDateExpired();
        if (release != CmsResource.DATE_EXPIRED_DEFAULT) {
            return getMessages().getDateTime(release);
        } else {
            return CmsWorkplace.DEFAULT_DATE_STRING;
        }
    }

    /**
     * Returns the formatted date of release.<p>
     *
     * @return the formatted date of release
     */
    public String getDateReleased() {

        long release = m_resource.getDateReleased();
        if (release != CmsResource.DATE_RELEASED_DEFAULT) {
            return getMessages().getDateTime(release);
        } else {
            return CmsWorkplace.DEFAULT_DATE_STRING;
        }
    }

    /**
     * Returns the path of the current resource, taking into account just the site mode.<p>
     *
     * @return the full path
     */
    public String getFullPath() {

        String path = m_resource.getRootPath();
        if ((m_siteMode != SITE_MODE_ROOT) && (m_cms != null)) {
            String site = getSite();
            if (path.startsWith(site)) {
                path = path.substring(site.length());
            }
        }
        return path;
    }

    public String getGalleryDescription(Locale locale) {

        return getSearchResult(locale).getDescription();
    }

    public String getGalleryTitle(Locale locale) {

        return getSearchResult(locale).getTitle();
    }

    /**
     * Returns the resource icon path displayed in the explorer view for the given resource.<p>
     *
     * Relative to <code>/system/workplace/resources/</code>.<p>
     *
     * If the resource has no sibling it is the same as {@link #getIconPathResourceType()}.<p>
     *
     * @return the resource icon path displayed in the explorer view for the given resource
     *
     * @see #getStyleSiblings()
     */
    public String getIconPathExplorer() {

        if (m_resource.getSiblingCount() > 1) {
            // links are present
            if (m_resource.isLabeled()) {
                // there is at least one link in a marked site
                return "explorer/link_labeled.gif";
            } else {
                // common links are present
                return "explorer/link.gif";
            }
        } else {
            return getIconPathResourceType();
        }
    }

    /**
     * Returns the lock icon path for the given resource.<p>
     *
     * Relative to <code>/system/workplace/resources/</code>.<p>
     *
     * Returns <code>explorer/project_none.gif</code> if request context is <code>null</code>.<p>
     *
     * @return the lock icon path for the given resource
     */
    public String getIconPathLock() {

        CmsLock lock = getLock();
        String iconPath = null;
        if (!lock.isUnlocked() && (m_request != null) && isInsideProject()) {
            if (getLock().isOwnedBy(m_request.getCurrentUser())
                && (getLockedInProjectId().equals(getReferenceProject().getUuid()))) {
                if (lock.isShared()) {
                    iconPath = "shared";
                } else {
                    iconPath = "user";
                }
            } else {
                iconPath = "other";
            }
        }
        if (iconPath == null) {
            iconPath = "project_none";
        } else {
            iconPath = "lock_" + iconPath;
        }
        return "explorer/" + iconPath + ".gif";
    }

    /**
     * Returns the project state icon path for the given resource.<p>
     *
     * Relative to <code>/system/workplace/resources/</code>.<p>
     *
     * @return the project state icon path for the given resource
     */
    public String getIconPathProjectState() {

        String iconPath;
        if (getProjectState() == STATE_MODIFIED_IN_CURRENT_PROJECT) {
            iconPath = "this.png";
        } else if (getProjectState() == STATE_MODIFIED_IN_OTHER_PROJECT) {
            iconPath = "other.png";
        } else if (getProjectState() == STATE_LOCKED_FOR_PUBLISHING) {
            iconPath = "publish.png";
        } else {
            // STATE_UNLOCKED
            iconPath = "none.gif";
        }
        return "explorer/project_" + iconPath;
    }

    /**
     * Returns the resource type icon path for the given resource.<p>
     *
     * Relative to <code>/system/workplace/resources/</code>.<p>
     *
     * @return the resource type icon path for the given resource
     */
    public String getIconPathResourceType() {

        if ((m_cms != null) && CmsJspNavBuilder.isNavLevelFolder(m_cms, m_resource)) {
            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + CmsIconUtil.ICON_NAV_LEVEL_SMALL);
        }
        if ((m_cms != null) && CmsResourceTypeXmlContainerPage.isModelReuseGroup(m_cms, m_resource)) {
            return CmsWorkplace.getResourceUri(
                CmsWorkplace.RES_PATH_FILETYPES + CmsIconUtil.ICON_MODEL_GROUP_REUSE_SMALL);
        }
        if (!isEditable()) {
            return CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    CmsResourceTypePlain.getStaticTypeName()).getIcon();
        }
        return CmsWorkplace.RES_PATH_FILETYPES
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getResourceTypeName()).getIcon();
    }

    /**
     * Returns an integer representation for the link type.<p>
     *
     * <ul>
     *   <li><code>0</code>: No sibling
     *   <li><code>1</code>: Sibling
     *   <li><code>2</code>: Labeled sibling
     * </ul>
     *
     * @return an integer representation for the link type
     */
    public int getLinkType() {

        if (m_resource.getSiblingCount() > 1) {
            // links are present
            if (m_resource.isLabeled()) {
                // there is at least one link in a marked site
                return 2;
            } else {
                // common links are present
                return 1;
            }
        } else {
            // no links to the resource are in the VFS
            return 0;
        }
    }

    /**
     * Returns the the lock for the given resource.<p>
     *
     * @return the lock the given resource
     */
    public CmsLock getLock() {

        if (m_lock == null) {
            try {
                m_lock = getCms().getLock(m_resource);
            } catch (Throwable e) {
                m_lock = CmsLock.getNullLock();
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_lock;
    }

    /**
     * Returns the user name who owns the lock for the given resource.<p>
     *
     * @return the user name who owns the lock for the given resource
     */
    public String getLockedByName() {

        String lockedBy = "";
        if (!getLock().isNullLock()) {
            // user
            lockedBy = getLock().getUserId().toString();
            try {
                lockedBy = getCurrentOuRelativeName(
                    CmsPrincipal.readPrincipalIncludingHistory(getCms(), getLock().getUserId()).getName());
            } catch (Throwable e) {
                lockedBy = e.getMessage();
            }
        }
        return lockedBy;
    }

    /**
     * Returns the id of the project in which the given resource is locked.<p>
     *
     * @return the id of the project in which the given resource is locked
     */
    public CmsUUID getLockedInProjectId() {

        CmsUUID lockedInProject = null;
        if (getLock().isNullLock() && !getResource().getState().isUnchanged()) {
            // resource is unlocked and modified
            lockedInProject = getResource().getProjectLastModified();
        } else if (!getResource().getState().isUnchanged()) {
            // resource is locked and modified
            lockedInProject = getProjectId();
        } else if (!getLock().isNullLock()) {
            // resource is locked and unchanged
            lockedInProject = getLock().getProjectId();
        }
        return lockedInProject;
    }

    /**
     * Returns the project name that locked the current resource's.<p>
     *
     * @return the the project name that locked the current resource's
     */
    public String getLockedInProjectName() {

        try {
            CmsUUID pId = getLockedInProjectId();
            if ((pId == null) || pId.isNullUUID()) {
                // the resource is unlocked and unchanged
                return "";
            }
            try {
                return getCurrentOuRelativeName(getCms().readProject(pId).getName());
            } catch (CmsDbEntryNotFoundException e) {
                return getCurrentOuRelativeName(getCms().readHistoryProject(pId).getName());
            }
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            return "";
        }
    }

    /**
     * Returns the lock state of the current resource.<p>
     *
     * @return the lock state of the current resource
     */
    public int getLockState() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getLockedByName())) {
            // unlocked
            return 0;
        }
        if (!getLockedByName().equals(getCurrentOuRelativeName(m_request.getCurrentUser().getName()))
            || !getLockedInProjectId().equals(m_request.getCurrentProject().getUuid())) {
            // locked by other user and/or project
            return 1;
        }
        if (getLock().getType().isShared()) {
            // shared lock
            return 2;
        }
        // exclusive lock
        return 3;
    }

    /**
     * Returns the navtext of a resource.<p>
     *
     * @return the navtext for that resource
     */
    public String getNavText() {

        String navText = "";
        try {
            navText = getCms().readPropertyObject(
                getCms().getSitePath(m_resource),
                CmsPropertyDefinition.PROPERTY_NAVTEXT,
                false).getValue();
        } catch (Throwable e) {
            String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot("");
                navText = getCms().readPropertyObject(
                    m_resource.getRootPath(),
                    CmsPropertyDefinition.PROPERTY_NAVTEXT,
                    false).getValue();
            } catch (Exception e1) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            } finally {
                getCms().getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        if (navText == null) {
            navText = "";
        }
        return navText;
    }

    /**
     * Checks is the current resource can be edited by the current user.<p>
     *
     * @param locale the locale to use for the messages
     *
     * @return an empty string if editable, or a localized string with the reason
     *
     * @throws CmsException if something goes wrong
     */
    public String getNoEditReason(Locale locale) throws CmsException {

        return getNoEditReason(locale, false);
    }

    /**
     * Checks is the current resource can be edited by the current user.<p>
     *
     * @param locale the locale to use for the messages
     * @param ignoreExpiration <code>true</code> to ignore resource release and expiration date
     *
     * @return an empty string if editable, or a localized string with the reason
     *
     * @throws CmsException if something goes wrong
     */
    public String getNoEditReason(Locale locale, boolean ignoreExpiration) throws CmsException {

        String reason = "";
        if (m_resource instanceof I_CmsHistoryResource) {
            reason = Messages.get().getBundle(locale).key(Messages.GUI_NO_EDIT_REASON_HISTORY_0);
        } else if (!m_cms.hasPermissions(
            m_resource,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            ignoreExpiration ? CmsResourceFilter.IGNORE_EXPIRATION : CmsResourceFilter.DEFAULT) || !isEditable()) {
            reason = Messages.get().getBundle(locale).key(Messages.GUI_NO_EDIT_REASON_PERMISSION_0);
        } else if (!getLock().isLockableBy(m_cms.getRequestContext().getCurrentUser())) {
            if (getLock().getSystemLock().isPublish()) {
                reason = Messages.get().getBundle(locale).key(Messages.GUI_PUBLISH_TOOLTIP_0);
            } else {
                reason = Messages.get().getBundle(locale).key(Messages.GUI_NO_EDIT_REASON_LOCK_1, getLockedByName());
            }
        }
        return reason;
    }

    /**
     * Returns the path of the current resource.<p>
     *
     * Taking into account following settings:<br>
     * <ul>
     *    <li>site mode
     *    <li>abbreviation length
     *    <li>relative to
     * </ul>
     *
     * @return the path
     */
    public String getPath() {

        String path = getFullPath();
        if (m_relativeTo != null) {
            path = getResource().getRootPath();
            if (path.startsWith(m_relativeTo)) {
                path = path.substring(m_relativeTo.length());
                if (path.length() == 0) {
                    path = ".";
                }
            } else {
                String site = getSite();
                if (path.startsWith(site + "/") || path.equals(site)) {
                    path = path.substring(site.length());
                }
            }
        }
        if (m_abbrevLength > 0) {
            boolean absolute = path.startsWith("/");
            path = CmsStringUtil.formatResourceName(path, m_abbrevLength);
            if (!absolute && path.startsWith("/")) {
                // remove leading '/'
                path = path.substring(1);
            }
        }
        return path;
    }

    /**
     * Returns the permission set for the given resource.<p>
     *
     * @return the permission set for the given resource
     */
    public CmsPermissionSet getPermissionSet() {

        CmsPermissionSetCustom pset = new CmsPermissionSetCustom();
        CmsResource resource = getResource();
        try {
            if (getCms().hasPermissions(resource, CmsPermissionSet.ACCESS_CONTROL, false, CmsResourceFilter.ALL)) {
                pset.grantPermissions(CmsPermissionSet.PERMISSION_CONTROL);
            } else {
                pset.denyPermissions(CmsPermissionSet.PERMISSION_CONTROL);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage());
        }
        try {
            if (getCms().hasPermissions(
                resource,
                CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                false,
                CmsResourceFilter.ALL)) {
                pset.grantPermissions(CmsPermissionSet.PERMISSION_DIRECT_PUBLISH);
            } else {
                pset.denyPermissions(CmsPermissionSet.PERMISSION_DIRECT_PUBLISH);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage());
        }
        try {
            if (getCms().hasPermissions(resource, CmsPermissionSet.ACCESS_READ, false, CmsResourceFilter.ALL)) {
                pset.grantPermissions(CmsPermissionSet.PERMISSION_READ);
            } else {
                pset.denyPermissions(CmsPermissionSet.PERMISSION_READ);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage());
        }
        try {
            if (getCms().hasPermissions(resource, CmsPermissionSet.ACCESS_VIEW, false, CmsResourceFilter.ALL)) {
                pset.grantPermissions(CmsPermissionSet.PERMISSION_VIEW);
            } else {
                pset.denyPermissions(CmsPermissionSet.PERMISSION_VIEW);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage());
        }
        try {
            if (getCms().hasPermissions(resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL)) {
                pset.grantPermissions(CmsPermissionSet.PERMISSION_WRITE);
            } else {
                pset.denyPermissions(CmsPermissionSet.PERMISSION_WRITE);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage());
        }

        return pset;
    }

    /**
     * Returns the permissions string for the given resource.<p>
     *
     * @return the permissions string for the given resource
     */
    public String getPermissionString() {

        return getPermissionSet().getPermissionString();
    }

    /**
     * Returns the id of the project which the resource belongs to.<p>
     *
     * @return the id of the project which the resource belongs to
     */
    public CmsUUID getProjectId() {

        CmsUUID projectId = m_resource.getProjectLastModified();
        if (!getLock().isUnlocked() && !getLock().isInherited()) {
            // use lock project ID only if lock is not inherited
            projectId = getLock().getProjectId();
        }
        return projectId;
    }

    /**
     * Returns the project state of the given resource.<p>
     *
     * <ul>
     *   <li>null: unchanged.</li>
     *   <li>true: locked in current project.</li>
     *   <li>false: not locked in current project.</li>
     * </ul>
     *
     * @return the project state of the given resource
     */
    public CmsResourceProjectState getProjectState() {

        if (getResource().getState().isUnchanged()) {
            return STATE_CLEAN; // STATE_CLEAN
        } else if (getLock().getSystemLock().isPublish()) {
            return STATE_LOCKED_FOR_PUBLISHING;
        } else if (getResource().getProjectLastModified().equals(getReferenceProject().getUuid())) {
            return STATE_MODIFIED_IN_CURRENT_PROJECT; // STATE_MODIFIED_CURRENT_PROJECT
        } else {
            return STATE_MODIFIED_IN_OTHER_PROJECT; // STATE_MODIFIED_OTHER_PROJECT
        }
    }

    /**
     * Returns the project to use to check project state.<p>
     *
     * @return the project to use to check project state
     */
    public CmsProject getReferenceProject() {

        if (m_referenceProject == null) {
            if (m_request != null) {
                m_referenceProject = m_request.getCurrentProject();
            }
        }
        return m_referenceProject;
    }

    /**
     * Returns the 'relative to' path.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @return the 'relative to' path
     */
    public String getRelativeTo() {

        return m_relativeTo;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the resource type for the given resource.<p>
     *
     * @return the resource type for the given resource
     */
    public I_CmsResourceType getResourceType() {

        if (m_resourceType == null) {
            m_resourceType = OpenCms.getResourceManager().getResourceType(m_resource);
        }
        return m_resourceType;
    }

    /**
     * Returns the resource type id for the given resource.<p>
     *
     * @return the resource type id for the given resource
     */
    public int getResourceTypeId() {

        return getResourceType().getTypeId();
    }

    /**
     * Returns the resource type name for the given resource.<p>
     *
     * @return the resource type name for the given resource
     */
    public String getResourceTypeName() {

        return getResourceType().getTypeName();
    }

    public CmsGallerySearchResult getSearchResult(Locale locale) {

        if (!m_searchResultMap.containsKey(locale)) {
            CmsGallerySearchResult sResult;
            try {
                sResult = CmsGallerySearch.searchById(m_cms, m_resource.getStructureId(), locale);
                m_searchResultMap.put(locale, sResult);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_searchResultMap.get(locale);
    }

    /**
     * Returns the site of the current resources,
     * taking into account the set site mode.<p>
     *
     * @return the site path
     */
    public String getSite() {

        String site = null;
        if ((m_siteMode == SITE_MODE_MATCHING) || (m_cms == null)) {
            site = OpenCms.getSiteManager().getSiteRoot(m_resource.getRootPath());
        } else if (m_siteMode == SITE_MODE_CURRENT) {
            site = m_cms.getRequestContext().getSiteRoot();
        } else if (m_siteMode == SITE_MODE_ROOT) {
            site = "";
        }
        return (site == null ? "" : site);
    }

    /**
     * Returns the site mode.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @return the site mode
     */
    public CmsResourceUtilSiteMode getSiteMode() {

        return m_siteMode;
    }

    /**
     * Returns the title of the site.<p>
     *
     * @return the title of the site
     */
    public String getSiteTitle() {

        String title = getSite();
        String rootSite = getCms().getRequestContext().getSiteRoot();
        try {
            getCms().getRequestContext().setSiteRoot("");
            title = getCms().readPropertyObject(title, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(title);
        } catch (CmsException e) {
            // ignore
        } finally {
            getCms().getRequestContext().setSiteRoot(rootSite);
        }
        return title;
    }

    /**
     * Returns the size of the given resource as a String.<p>
     *
     * For directories it returns <code>SIZE_DIR</code>.<p>
     *
     * @return the size of the given resource as a String
     */
    public String getSizeString() {

        return m_resource.getLength() == -1 ? SIZE_DIR : "" + m_resource.getLength();
    }

    /**
     * Returns resource state abbreviation.<p>
     *
     * @return resource state abbreviation
     */
    public char getStateAbbreviation() {

        return getResource().getState().getAbbreviation();
    }

    /**
     * Returns the state name for a resource.<p>
     *
     * Uses default locale if request context is <code>null</code>.<p>
     *
     * @return the state name for that resource
     */
    public String getStateName() {

        CmsResourceState state = m_resource.getState();
        String name;
        if (m_request == null) {
            name = org.opencms.workplace.explorer.Messages.get().getBundle().key(
                org.opencms.workplace.explorer.Messages.getStateKey(state));
        } else {
            name = org.opencms.workplace.explorer.Messages.get().getBundle(m_request.getLocale()).key(
                org.opencms.workplace.explorer.Messages.getStateKey(state));
        }
        return name;
    }

    /**
     * Returns the style class to use for the given resource.<p>
     *
     * @return style class name
     *
     * @see org.opencms.workplace.list.CmsListExplorerColumn#getExplorerStyleDef()
     */
    public String getStyleClassName() {

        if (isInsideProject() && isEditable()) {
            if (m_resource.getState().isChanged()) {
                return "fc";
            } else if (m_resource.getState().isNew()) {
                return "fn";
            } else if (m_resource.getState().isDeleted()) {
                return "fd";
            } else {
                return "nf";
            }
        }
        return "fp";
    }

    /**
     * Returns additional style sheets for the resource type icon depending on siblings.<p>
     *
     * That is, depending on {@link CmsResource#getSiblingCount()}
     *
     * Use it with the {@link #getIconPathExplorer} method.<p>
     *
     * @return additional style sheets depending on siblings
     */
    public String getStyleSiblings() {

        StringBuffer style = new StringBuffer(128);
        if (m_resource.getSiblingCount() > 1) {
            style.append("background-image:url(");
            style.append(CmsWorkplace.getSkinUri());
            style.append(getIconPathResourceType());
            style.append("); background-position: 0px 0px; background-repeat: no-repeat; ");
        }
        return style.toString();
    }

    /**
     * Returns the system lock information tooltip for the explorer view.<p>
     *
     * @param forExplorer if the tool tip should be generated for the explorer view
     *
     * @return the system lock information tooltip
     */
    public String getSystemLockInfo(boolean forExplorer) {

        if (getLock().getSystemLock().isPublish()) {
            if (!forExplorer) {
                return getMessages().key(Messages.GUI_PUBLISH_TOOLTIP_0);
            } else {
                // see explorer.js(sysLockInfo) and top_js.jsp(publishlock)
                return "p"; // should have length == 1
            }
        }
        return "";
    }

    /**
     * Returns additional style sheets depending on publication constraints.<p>
     *
     * That is, depending on {@link CmsResource#getDateReleased()} and
     * {@link CmsResource#getDateExpired()}.<p>
     *
     * @return additional style sheets depending on publication constraints
     *
     * @see #getTimeWindowLayoutType()
     */
    public String getTimeWindowLayoutStyle() {

        return getTimeWindowLayoutType() == CmsResourceUtil.LAYOUTSTYLE_INRANGE ? "" : "font-style:italic;";
    }

    /**
     * Returns the layout style for the current time window state.<p>
     *
     * <ul>
     *   <li><code>{@link CmsResourceUtil#LAYOUTSTYLE_INRANGE}</code>: The time window is in range
     *   <li><code>{@link CmsResourceUtil#LAYOUTSTYLE_BEFORERELEASE}</code>: The resource is not yet released
     *   <li><code>{@link CmsResourceUtil#LAYOUTSTYLE_AFTEREXPIRE}</code>: The resource has already expired
     * </ul>
     *
     * @return the layout style for the current time window state
     *
     * @see #getTimeWindowLayoutStyle()
     */
    public int getTimeWindowLayoutType() {

        int layoutstyle = CmsResourceUtil.LAYOUTSTYLE_INRANGE;
        if (!m_resource.isReleased(getCms().getRequestContext().getRequestTime())) {
            layoutstyle = CmsResourceUtil.LAYOUTSTYLE_BEFORERELEASE;
        } else if (m_resource.isExpired(getCms().getRequestContext().getRequestTime())) {
            layoutstyle = CmsResourceUtil.LAYOUTSTYLE_AFTEREXPIRE;
        }
        return layoutstyle;
    }

    /**
     * Returns the title of a resource.<p>
     *
     * @return the title for that resource
     */
    public String getTitle() {

        String title = "";
        try {
            title = getCms().readPropertyObject(
                getCms().getSitePath(m_resource),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false).getValue();
        } catch (Throwable e) {
            String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot("");
                title = getCms().readPropertyObject(
                    m_resource.getRootPath(),
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
            } catch (Exception e1) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            } finally {
                getCms().getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        if (title == null) {
            title = "";
        }
        return title;
    }

    /**
     * Returns the name of the user who created the given resource.<p>
     *
     * @return the name of the user who created the given resource
     */
    public String getUserCreated() {

        String user = m_resource.getUserCreated().toString();
        try {
            user = getCurrentOuRelativeName(
                CmsPrincipal.readPrincipalIncludingHistory(getCms(), m_resource.getUserCreated()).getName());
        } catch (Throwable e) {
            LOG.info(e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * Returns the name of the user who last modified the given resource.<p>
     *
     * @return the name of the user who last modified the given resource
     */
    public String getUserLastModified() {

        String user = m_resource.getUserLastModified().toString();
        try {
            user = getCurrentOuRelativeName(
                CmsPrincipal.readPrincipalIncludingHistory(getCms(), m_resource.getUserLastModified()).getName());
        } catch (Throwable e) {
            LOG.info(e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * Returns <code>true</code> if the given resource is editable by the current user.<p>
     *
     * Returns <code>false</code> if no request context is set.<p>
     *
     * @return <code>true</code> if the given resource is editable by the current user
     */
    public boolean isEditable() {

        if (m_request == null) {
            return false;
        }
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(getResourceTypeName());
        if (settings != null) {
            String rightSite = OpenCms.getSiteManager().getSiteRoot(getResource().getRootPath());
            if (rightSite == null) {
                rightSite = "";
            }
            String currentSite = getCms().getRequestContext().getSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot(rightSite);
                return settings.isEditable(getCms(), getResource());
            } finally {
                getCms().getRequestContext().setSiteRoot(currentSite);
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given resource is in the reference project.<p>
     *
     * Returns <code>false</code> if the request context is <code>null</code>.<p>
     *
     * @return <code>true</code> if the given resource is in the reference project
     *
     * @see #getReferenceProject()
     */
    public boolean isInsideProject() {

        return CmsProject.isInsideProject(getProjectResources(), getResource());
    }

    /**
     * Returns <code>true</code> if the stored resource has been released and has not expired.<p>
     *
     * If no request context is available, the current time is used for the validation check.<p>
     *
     * @return <code>true</code> if the stored resource has been released and has not expired
     *
     * @see CmsResource#isReleasedAndNotExpired(long)
     */
    public boolean isReleasedAndNotExpired() {

        long requestTime;
        if (m_request == null) {
            requestTime = System.currentTimeMillis();
        } else {
            requestTime = m_request.getRequestTime();
        }
        return m_resource.isReleasedAndNotExpired(requestTime);
    }

    /**
     * Sets the path abbreviation length.<p>
     *
     * If greater than zero, the path will be formatted to this number of chars.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @param abbrevLength the path abbreviation length to set
     */
    public void setAbbrevLength(int abbrevLength) {

        m_abbrevLength = abbrevLength;
    }

    /**
     * Sets the cms context.<p>
     *
     * @param cms the cms context to set
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
        m_request = cms.getRequestContext();
        m_referenceProject = null;
        m_projectResources = null;
        m_messages = null;
    }

    /**
     * Sets the project to use to check project state.<p>
     *
     * @param project the project to set
     */
    public void setReferenceProject(CmsProject project) {

        m_referenceProject = project;
        m_projectResources = null;
    }

    /**
     * Sets the 'relative to' path.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @param relativeTo the 'relative to' path to set
     */
    public void setRelativeTo(String relativeTo) {

        m_relativeTo = relativeTo;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_relativeTo)) {
            m_relativeTo = null;
        } else {
            if (!m_relativeTo.startsWith("/")) {
                m_relativeTo = "/" + m_relativeTo;
            }
            if (!m_relativeTo.endsWith("/")) {
                m_relativeTo += "/";
            }
        }
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
        m_lock = null;
        m_resourceType = null;
    }

    /**
     * Sets the site mode.<p>
     *
     * This only affects the generation of the path for the current resource.<p>
     *
     * @param siteMode the site mode to set
     */
    public void setSiteMode(CmsResourceUtilSiteMode siteMode) {

        m_siteMode = siteMode;
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     *
     * @param name the fully qualified name to check
     *
     * @return the simple name if the ou is the same as the current user's ou
     */
    private String getCurrentOuRelativeName(String name) {

        if (m_request == null) {
            return CmsOrganizationalUnit.SEPARATOR + name;
        }
        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(m_request.getCurrentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * Returns the message bundle for formatting dates, depends on the request locale.<p>
     *
     * @return the message bundle for formatting dates
     */
    private CmsMessages getMessages() {

        if (m_messages == null) {
            if (m_cms != null) {
                m_messages = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms));
            } else {
                m_messages = Messages.get().getBundle();
            }
        }
        return m_messages;
    }

    /**
     * Returns the reference project resources.<p>
     *
     * @return the reference project resources
     */
    private List<String> getProjectResources() {

        if (m_projectResources == null) {
            try {
                m_projectResources = getCms().readProjectResources(getReferenceProject());
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
                // use an empty list (all resources are "outside")
                m_projectResources = new ArrayList<String>();
            }
        }
        return m_projectResources;
    }
}