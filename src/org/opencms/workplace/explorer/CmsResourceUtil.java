/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsResourceUtil.java,v $
 * Date   : $Date: 2006/11/16 15:43:35 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsDbUtil;
import org.opencms.file.CmsResource.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.commons.CmsTouch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides {@link CmsResource} utility functions.<p>
 * 
 * This class provides in java all resource information used by the explorer view,
 * mostly generated in javascript (see explorer.js)<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsResourceUtil {

    /**
     * Util class for defining the site modes.<p>
     */
    private static class CmsResourceUtilSiteMode {

        // empty class
    }

    /** Constant that signalizes that all path operations will be based on the current site. */
    public static final CmsResourceUtilSiteMode SITE_MODE_CURRENT = new CmsResourceUtilSiteMode();

    /** Constant that signalizes that all path operations will be based on the best matching site. */
    public static final CmsResourceUtilSiteMode SITE_MODE_MATCHING = new CmsResourceUtilSiteMode();

    /** Constant that signalizes that all path operations will be based on the root path. */
    public static final CmsResourceUtilSiteMode SITE_MODE_ROOT = new CmsResourceUtilSiteMode();

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceUtil.class);

    /** The configured workflow manager, may be <code>null</code>. */
    private static I_CmsWorkflowManager m_wfManager;

    /** The folder size display string constant. */
    private static final String SIZE_DIR = "-";

    /** If greater than zero, the path will be formatted to this number of chars. */
    private int m_abbrevLength;

    /** The current cms context. */
    private CmsObject m_cms;

    /** The current resource lock. */
    private CmsLock m_lock;

    /** The message bundle for formatting dates, depends on the request locale. */
    private CmsMessages m_messages;

    /** Reference project resources cache. */
    private List m_projectResources;

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

    /** Layoutstyle for resources after expire date. */
    public static final int LAYOUTSTYLE_AFTEREXPIRE = 2;

    /** Layoutstyle for resources before release date. */
    public static final int LAYOUTSTYLE_BEFORERELEASE = 1;

    /** Layoutstyle for resources after release date and before expire date. */
    public static final int LAYOUTSTYLE_INRANGE = 0;

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

    static {
        // initialize the workflow manager
        try {
            m_wfManager = OpenCms.getWorkflowManager();
        } catch (CmsRuntimeException e) {
            m_wfManager = null;
        }
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
            return CmsTouch.DEFAULT_DATE_STRING;
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
            return CmsTouch.DEFAULT_DATE_STRING;
        }
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
            if (getLock().isOwnedBy(m_request.currentUser())
                && (getLockedInProjectId() == getReferenceProject().getId())) {
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
        if (getProjectState() == null) {
            iconPath = "none.gif";
        } else if (getProjectState().booleanValue()) {
            iconPath = "this.png";
        } else {
            iconPath = "other.png";
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

        if (!isEditable()) {
            return "filetypes/"
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypePlain.getStaticTypeName()).getIcon();
        }
        return "filetypes/" + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getResourceTypeName()).getIcon();
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
                LOG.error(e);
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
            try {
                lockedBy = getCms().readUser(getLock().getUserId()).getName();
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
    public int getLockedInProjectId() {

        int lockedInProject = CmsDbUtil.UNKNOWN_ID;
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
            int pId = getLockedInProjectId();
            if (pId == CmsDbUtil.UNKNOWN_ID) {
                // the resource is unlocked and unchanged
                return "";
            }
            return getCms().readProject(pId).getName();
        } catch (Throwable e) {
            LOG.error(e);
            return "";
        }
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
     * @return the site relative path
     */
    public String getPath() {

        String path = "";
        if (m_siteMode == SITE_MODE_ROOT || m_cms == null) {
            path = m_resource.getRootPath();
        } else {
            String site = getSite();
            if (m_resource.getRootPath().startsWith(site)) {
                path = m_resource.getRootPath().substring(site.length());
            }
        }
        if (m_relativeTo != null && path.startsWith(m_relativeTo)) {
            path = path.substring(m_relativeTo.length());
            if (path.length() == 0) {
                path = ".";
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
     * Returns the permissions string for the given resource.<p>
     * 
     * @return the permissions string for the given resource
     */
    public String getPermissions() {

        String permissions;
        try {
            permissions = getCms().getPermissions(getCms().getSitePath(getResource())).getPermissionString();
        } catch (Throwable e) {
            getCms().getRequestContext().saveSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot("/");
                permissions = getCms().getPermissions(getResource().getRootPath()).getPermissionString();
            } catch (Throwable e1) {
                permissions = e1.getMessage();
                LOG.error(e1);
            } finally {
                getCms().getRequestContext().restoreSiteRoot();
            }
        }
        return permissions;
    }

    /**
     * Returns the id of the project which the resource belongs to.<p>
     * 
     * @return the id of the project which the resource belongs to
     */
    public int getProjectId() {

        int projectId = m_resource.getProjectLastModified();
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
    public Boolean getProjectState() {

        if (m_resource.getState().isUnchanged() || !isInsideProject()) {
            return null;
        } else {
            return Boolean.valueOf(getLockedInProjectId() == getReferenceProject().getId());
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
                m_referenceProject = m_request.currentProject();
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
            try {
                m_resourceType = OpenCms.getResourceManager().getResourceType(m_resource.getTypeId());
            } catch (Throwable e) {
                try {
                    m_resourceType = OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypePlain.getStaticTypeId());
                } catch (Throwable e1) {
                    // should never happen
                    m_resourceType = null;
                }
            }
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

    /**
     * Returns the site of the current resources,
     * taking into account the set site mode.<p>
     * 
     * @return the site path
     */
    public String getSite() {

        String site = null;
        if (m_siteMode == SITE_MODE_MATCHING || m_cms == null) {
            site = CmsSiteManager.getSiteRoot(m_resource.getRootPath());
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
     * Returns the size of the given resource as a String.<p>
     * 
     * For directories it returns {@link #SIZE_DIR}.<p>
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
            getCms().getRequestContext().saveSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot("/");
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
                getCms().getRequestContext().restoreSiteRoot();
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
            user = getCms().readUser(m_resource.getUserCreated()).getName();
        } catch (Throwable e) {
            LOG.error(e);
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
            user = getCms().readUser(m_resource.getUserLastModified()).getName();
        } catch (Throwable e) {
            LOG.error(e);
        }
        return user;
    }

    /**
     * Returns the workflow project information tooltip for the explorer view.<p>
     * 
     * @return the workflow project information tooltip
     */
    public String getWorkflowProjectInfo() {

        CmsProject wfProject = getWorkflowProject();
        if (wfProject == null) {
            return "";
        }
        try {
            String taskType;
            taskType = m_wfManager.getTaskType(wfProject, getLocale());
            return Messages.get().container(
                Messages.GUI_TOOLTIP_TASK_INFO_5,
                new Object[] {
                    taskType,
                    m_wfManager.getTaskDescription(wfProject),
                    getWorkflowTaskState(),
                    getMessages().getDateTime(m_wfManager.getTaskStartTime(wfProject)),
                    m_wfManager.getTaskOwner(wfProject).getName()}).key();
        } catch (Throwable exc) {
            LOG.error(exc);
            return "";
        }
    }

    /**
     * Returns the workflow task state.<p>
     * 
     * @return the workflow task state
     */
    public String getWorkflowTaskState() {

        String taskState = "";
        CmsProject wfProject = getWorkflowProject();
        if (wfProject != null) {
            try {
                taskState = m_wfManager.getTaskState(wfProject, getLocale());
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return taskState;
    }

    /**
     * Returns <code>true</code> if the given resource is editable by the current user.<p>
     * 
     * Retuns <code>false</code> if no request context is set.<p>
     * 
     * @return <code>true</code> if the given resource  is editable by the current user
     */
    public boolean isEditable() {

        if (m_request == null) {
            return false;
        }
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(getResourceTypeName());
        if (settings != null) {
            return settings.getAccess().getPermissions(getCms()).requiresWritePermission();
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

        if (m_wfManager == null) {
            return CmsProject.isInsideProject(getProjectResources(), m_resource);
        }
        CmsProject wfProject = getWorkflowProject();
        boolean isInsideProject = false;
        if (wfProject != null) {
            if (getWorkflowLock().isWorkflow()) {
                isInsideProject = m_wfManager.isLockableInWorkflow(getCms(), getResource(), false);
            } else {
                isInsideProject = CmsProject.isInsideProject(getProjectResources(), getResource());
                isInsideProject = isInsideProject && m_wfManager.isLockableInWorkflow(getCms(), getResource(), false);
            }
        } else {
            isInsideProject = CmsProject.isInsideProject(getProjectResources(), getResource());
        }
        return isInsideProject;
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
     * Returns the current locale, or the default locale if no locale is set.<p>
     * 
     * @return the current locale
     */
    private Locale getLocale() {

        if (m_request != null) {
            return m_request.getLocale();
        } else {
            return CmsLocaleManager.getDefaultLocale();
        }
    }

    /**
     * Returns the message bundle for formatting dates, depends on the request locale.<p>
     * 
     * @return the message bundle for formatting dates
     */
    private CmsMessages getMessages() {

        if (m_messages == null) {
            if (m_request != null) {
                m_messages = Messages.get().getBundle(m_request.getLocale());
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
    private List getProjectResources() {

        if (m_projectResources == null) {
            try {
                m_projectResources = getCms().readProjectResources(getReferenceProject());
            } catch (Throwable e) {
                LOG.error(e);
                // use an empty list (all resources are "outside")
                m_projectResources = new ArrayList();
            }
        }
        return m_projectResources;
    }

    /**
     * Returns the workflow lock, or {@link CmsLock#getNullLock()} if no workflow is found.<p>
     * 
     * @return the workflow lock
     */
    private CmsLock getWorkflowLock() {

        if (m_wfManager == null) {
            return CmsLock.getNullLock();
        }
        CmsLock lock;
        try {
            lock = getCms().getLockForWorkflow(getResource());
        } catch (CmsException e) {
            lock = CmsLock.getNullLock();
            LOG.error(e);
        }
        return lock;
    }

    /**
     * Returns the workflow project, or <code>null</code> if no workflow is found.<p>
     * 
     * @return the workflow project
     */
    private CmsProject getWorkflowProject() {

        CmsLock lock = getWorkflowLock();
        if (m_wfManager == null || lock.isNullLock()) {
            return null;
        }
        CmsProject project;
        try {
            project = getCms().readProject(lock.getProjectId());
        } catch (CmsException e) {
            LOG.error(e);
            return null;
        }
        if (project.getType() == CmsProject.PROJECT_TYPE_WORKFLOW) {
            try {
                return m_wfManager.getTask(getCms(), getCms().getSitePath(getResource()));
            } catch (CmsException e) {
                LOG.error(e);
                return null;
            }
        } else {
            return null;
        }
    }
}
