/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.util;

import org.opencms.db.CmsDbUtil;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.List;

/**
 * Provides {@link CmsResource} utility functions.<p>
 * 
 * This class provides in java all resource information used by the explorer view,
 * mostly generated in javascript (see explorer.js)<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision$ 
 * 
 * @since 6.0.0 
 */
public final class CmsResourceUtil {

    /** Resource states abbreviations table. */
    private static final char[] RESOURCE_STATE = new char[] {'U', 'C', 'N', 'D', '_'};

    /** The folder size display string constant. */
    private static final String SIZE_DIR = "-";

    /** The current cms context. */
    private CmsObject m_cms;

    /** The current resource lock. */
    private CmsLock m_lock;

    /** Reference project resources cache. */
    private List m_projectResources;

    /** The project to use to check project state, if <code>null</code> the current project will be used. */
    private CmsProject m_referenceProject;

    /** The current request context. */
    private CmsRequestContext m_request;

    /** The current resource. */
    private CmsResource m_resource;

    /** The current resource type. */
    private I_CmsResourceType m_resourceType;

    // TODO: Remove this class, maybe refactor to org.opencms.workplace.list package
    // TODO: Check if CmsResource should be extended by this class
    private int todo = 0;

    /**
     * Creates a new {@link CmsRequestUtil} object.<p> 
     * 
     * @param cms the cms context
     */
    public CmsResourceUtil(CmsObject cms) {

        setCms(cms);
    }

    /**
     * Creates a new {@link CmsRequestUtil} object.<p> 
     * 
     * @param cms the cms context
     * @param resource the resource
     */
    public CmsResourceUtil(CmsObject cms, CmsResource resource) {

        setCms(cms);
        setResource(resource);
    }

    /**
     * Creates a new {@link CmsRequestUtil} object.<p> 
     * 
     * @param resource the resource
     */
    public CmsResourceUtil(CmsResource resource) {

        setResource(resource);
    }

    /**
     * Returns resource state abbreviation.<p>
     * 
     * @param state the resource state 
     * 
     * @return resource state abbreviation
     */
    public static char getStateAbbreviation(int state) {

        if (state >= 0 && state <= 3) {
            return RESOURCE_STATE[state];
        } else {
            return RESOURCE_STATE[4];
        }
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

        int lockId = getLock().getType();
        String iconPath = null;
        if (lockId != CmsLock.TYPE_UNLOCKED && m_request != null && isInsideProject()) {
            if (getLock().getUserId().equals(m_request.currentUser().getId())
                && getLockedInProjectId() == getReferenceProject().getId()) {
                if (lockId == CmsLock.TYPE_SHARED_EXCLUSIVE || lockId == CmsLock.TYPE_SHARED_INHERITED) {
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
            iconPath = "none";
        } else if (getProjectState().booleanValue()) {
            iconPath = "this";
        } else {
            iconPath = "other";
        }
        return "explorer/project_" + iconPath + ".gif";
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
        if (getLock().isNullLock() && getResource().getState() != CmsResource.STATE_UNCHANGED) {
            // resource is unlocked and modified
            lockedInProject = getResource().getProjectLastModified();
        } else {
            if (getResource().getState() != CmsResource.STATE_UNCHANGED) {
                // resource is locked and modified
                lockedInProject = getProjectId();
            } else {
                // resource is locked and unchanged
                lockedInProject = getLock().getProjectId();
            }
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
            return e.getMessage();
        }
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
            try {
                permissions = getCms().getPermissions(getResource().getRootPath()).getPermissionString();
            } catch (Throwable e1) {
                permissions = e1.getMessage();
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
        if (!getLock().isNullLock()
            && getLock().getType() != CmsLock.TYPE_INHERITED
            && getLock().getType() != CmsLock.TYPE_SHARED_INHERITED) {
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

        if (m_resource.getState() == CmsResource.STATE_UNCHANGED || !isInsideProject()) {
            return null;
        } else {
            return new Boolean(getLockedInProjectId() == getReferenceProject().getId());
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

        int state = getResource().getState();
        if (state >= 0 && state <= 3) {
            return RESOURCE_STATE[state];
        } else {
            return RESOURCE_STATE[4];
        }
    }

    /**
     * Returns the state name for a resource.<p>
     * 
     * Uses default locale if request context is <code>null</code>.<p>
     * 
     * @return the state name for that resource
     */
    public String getStateName() {

        int state = m_resource.getState();
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
            switch (m_resource.getState()) {
                case CmsResource.STATE_CHANGED:
                    return "fc";
                case CmsResource.STATE_NEW:
                    return "fn";
                case CmsResource.STATE_DELETED:
                    return "fd";
                case CmsResource.STATE_UNCHANGED:
                default:
                    return "nf";
            }
        }
        return "fp";
    }

    /**
     * Returns additional style sheets depending on publication constraints.<p>
     * 
     * That is, depending on {@link CmsResource#getDateReleased()} and 
     * {@link CmsResource#getDateExpired()}.<p>
     * 
     * @return additional style sheets depending on publication constraints
     */
    public String getStyleRange() {

        return isInRange() ? "" : "font-style:italic;";
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
     * Returns the title of a resource.<p>
     * 
     * @return the title for that resource
     */
    public String getTitle() {

        String title;
        try {
            title = getCms().readPropertyObject(
                getCms().getSitePath(m_resource),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false).getValue();
        } catch (Throwable e) {
            title = e.getMessage();
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
            // ignore
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
            // ignore
        }
        return user;
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
     * Returns <code>true</code> if the given resource has expired.<p>
     * 
     * Retuns <code>true</code> if no request context is set.<p>
     * 
     * @return <code>true</code> if the given resource has expired
     */
    public boolean isExpired() {

        if (m_request == null) {
            return m_resource.getDateExpired() < System.currentTimeMillis();
        }
        return m_resource.getDateExpired() < m_request.getRequestTime();
    }

    /**
     * Returns <code>true</code> if the given resource has been released and has not expired.<p>
     * 
     * Retuns <code>false</code> if no request context is set.<p>
     * 
     * @return <code>true</code> if the given resource has been released and has not expired
     */
    public boolean isInRange() {

        return isReleased() && !isExpired();
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

        if (m_projectResources == null) {
            try {
                m_projectResources = getCms().readProjectResources(getReferenceProject());
            } catch (Throwable e) {
                return false;
            }
        }
        return CmsProject.isInsideProject(m_projectResources, m_resource);
    }

    /**
     * Returns <code>true</code> if the given resource has been released.<p>
     * 
     * @return <code>true</code> if the given resource has been released
     */
    public boolean isReleased() {

        if (m_request == null) {
            return m_resource.getDateReleased() < System.currentTimeMillis();
        }
        return m_resource.getDateReleased() < m_request.getRequestTime();
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
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
        m_lock = null;
        m_resourceType = null;
    }
}