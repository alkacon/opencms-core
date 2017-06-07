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

package org.opencms.ui.contextmenu;

import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.controlpermission;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.defaultfile;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.deleted;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.file;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.folder;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.haseditor;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.hassourcecodeeditor;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.inproject;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.mylock;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.noinheritedlock;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.nootherlock;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.notdeleted;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.notinproject;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.notnew;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.notonline;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.notunchangedfile;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.otherlock;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.pagefolder;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.pointer;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.publishpermission;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.replacable;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.roleeditor;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.rolewpuser;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.unlocked;
import static org.opencms.ui.contextmenu.CmsVisibilityCheckFlag.writepermisssion;
import static org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
import static org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditorTypes.BundleType;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.workplace.explorer.menu.Messages;

import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Standard visibility check implementation.<p>
 *
 * Instances of this class are configured with a set of flags, each of which corresponds to a check to perform which
 * may cause the context menu item to be hidden or deactivated.<p>
 */
public final class CmsStandardVisibilityCheck extends A_CmsSimpleVisibilityCheck {

    /** Default visibility check for 'edit-like' operations on folders. */
    public static final CmsStandardVisibilityCheck COPY_PAGE = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        notdeleted,
        pagefolder);

    /** Default visibility check for 'edit-like' operations on resources. */
    public static final CmsStandardVisibilityCheck DEFAULT = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject);

    /**
     * Check for operations which need a default file.<p>
     */
    public static final I_CmsHasMenuItemVisibility DEFAULT_DEFAULTFILE = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject,
        defaultfile);

    /** Default visibility check for 'edit-like' operations on folders. */
    public static final CmsStandardVisibilityCheck DEFAULT_FOLDERS = new CmsStandardVisibilityCheck(
        folder,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject);

    /** Like DEFAULT, but only active for files. */
    public static final CmsStandardVisibilityCheck EDIT = new CmsStandardVisibilityCheck(
        file,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject,
        haseditor);

    /** Like DEFAULT, but only active for files. */
    public static final CmsStandardVisibilityCheck EDIT_CODE = new CmsStandardVisibilityCheck(
        file,
        hassourcecodeeditor,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject,
        haseditor);

    /** Visibility check for editing external links (pointers). */
    public static final I_CmsHasMenuItemVisibility EDIT_POINTER = new CmsStandardVisibilityCheck(
        file,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject,
        pointer);

    /** Check for locking resources. */
    public static final CmsStandardVisibilityCheck LOCK = new CmsStandardVisibilityCheck(
        unlocked,
        roleeditor,
        notonline,
        notdeleted,
        inproject);

    /** Visibility check used for copy to project dialog. */
    public static final CmsStandardVisibilityCheck OTHER_PROJECT = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        notdeleted,
        notinproject);

    /** Visibility check for the permissions dialog. */
    public static final I_CmsHasMenuItemVisibility PERMISSIONS = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        controlpermission,
        inproject);

    /** Visibility check for publish option. */
    public static final CmsStandardVisibilityCheck PUBLISH = new CmsStandardVisibilityCheck(
        notunchangedfile,
        publishpermission,
        notonline,
        inproject);

    /** Check for the 'replace' operation. */
    public static final CmsStandardVisibilityCheck REPLACE = new CmsStandardVisibilityCheck(
        replacable,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject);

    /** Default check for 'locked resources' action. */
    public static final CmsStandardVisibilityCheck SHOW_LOCKS = new CmsStandardVisibilityCheck(
        notonline,
        inproject,
        folder);

    /** Permission check for stealing locks. */
    public static final I_CmsHasMenuItemVisibility STEAL_LOCK = new CmsStandardVisibilityCheck(
        otherlock,
        noinheritedlock,
        inproject);

    /** Visibility check for undelete option. */
    public static final CmsStandardVisibilityCheck UNDELETE = new CmsStandardVisibilityCheck(
        roleeditor,
        notonline,
        deleted,
        writepermisssion,
        inproject);

    /** Visibility check for the undo function. */
    public static final CmsStandardVisibilityCheck UNDO = new CmsStandardVisibilityCheck(
        notunchangedfile,
        notnew,
        roleeditor,
        notonline,
        notdeleted,
        writepermisssion,
        inproject);

    /** Visibility check for the undo function. */
    public static final CmsStandardVisibilityCheck UNLOCK = new CmsStandardVisibilityCheck(
        mylock,
        noinheritedlock,
        inproject);

    /** Default visibility check for view operations on resources. */
    public static final CmsStandardVisibilityCheck VIEW = new CmsStandardVisibilityCheck(roleeditor, notdeleted);

    /** Always active. */
    public static final I_CmsHasMenuItemVisibility VISIBLE = new CmsStandardVisibilityCheck();

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStandardVisibilityCheck.class);

    /** The set of flags. */
    private Set<CmsVisibilityCheckFlag> m_flags = Sets.newHashSet();

    /**
     * Creates a new instance using the given flags.<p>
     *
     * Note that the order of the flags does not matter; the checks corresponding to the flags are performed in a fixed order.
     *
     * @param flags the flags indicating which checks to perform
     */
    public CmsStandardVisibilityCheck(CmsVisibilityCheckFlag... flags) {
        for (CmsVisibilityCheckFlag flag : flags) {
            m_flags.add(flag);
        }
    }

    /**
     * Helper method to make checking for a flag very short (character count).<p>
     *
     * @param flag the flag to check
     *
     * @return true if this instance was configured with the given flag
     */
    public boolean flag(CmsVisibilityCheckFlag flag) {

        return m_flags.contains(flag);
    }

    /**
     * @see org.opencms.ui.contextmenu.A_CmsSimpleVisibilityCheck#getSingleVisibility(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsMenuItemVisibilityMode getSingleVisibility(CmsObject cms, CmsResource resource) {

        boolean prioritize = false;
        String inActiveKey = null;
        if (resource != null) {
            if (flag(roleeditor)
                && !OpenCms.getRoleManager().hasRoleForResource(cms, CmsRole.EDITOR, cms.getSitePath(resource))) {
                return VISIBILITY_INVISIBLE;
            }
            if (flag(rolewpuser)
                && !OpenCms.getRoleManager().hasRoleForResource(
                    cms,
                    CmsRole.WORKPLACE_USER,
                    cms.getSitePath(resource))) {
                return VISIBILITY_INVISIBLE;
            }
        } else {
            if (flag(roleeditor) && !OpenCms.getRoleManager().hasRole(cms, CmsRole.EDITOR)) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(rolewpuser) && !OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_USER)) {
                return VISIBILITY_INVISIBLE;
            }
        }
        if (flag(notonline) && cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return VISIBILITY_INVISIBLE;
        }

        if ((resource != null)) {
            CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
            if (flag(file) && !resource.isFile()) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(defaultfile)) {
                if (!resource.isFile()) {
                    return VISIBILITY_INVISIBLE;
                }
                try {
                    CmsResource parentFolder = cms.readParentFolder(resource.getStructureId());
                    CmsResource defaultFile = cms.readDefaultFile(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    if ((defaultFile == null) || !(defaultFile.getStructureId().equals(resource.getStructureId()))) {
                        return VISIBILITY_INVISIBLE;
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    return VISIBILITY_INVISIBLE;
                }
            }

            if (flag(folder) && resource.isFile()) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(pagefolder)) {
                if (!resource.isFolder()) {
                    return VISIBILITY_INVISIBLE;
                }
                try {
                    CmsResource defaultFile;
                    defaultFile = cms.readDefaultFile("" + resource.getStructureId());
                    if ((defaultFile == null) || !CmsResourceTypeXmlContainerPage.isContainerPage(defaultFile)) {
                        return VISIBILITY_INVISIBLE;
                    }
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return VISIBILITY_INVISIBLE;
                }
            }

            if (flag(pointer)
                && !OpenCms.getResourceManager().matchResourceType(
                    CmsResourceTypePointer.getStaticTypeName(),
                    resource.getTypeId())) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(replacable)) {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
                boolean usesDumpLoader = type.getLoaderId() == CmsDumpLoader.RESOURCE_LOADER_ID;
                if (!usesDumpLoader && !(type instanceof CmsResourceTypeImage)) {
                    return VISIBILITY_INVISIBLE;
                }

            }

            if (flag(hassourcecodeeditor)) {
                I_CmsResourceType type = resUtil.getResourceType();
                boolean hasSourcecodeEditor = (type instanceof CmsResourceTypeXmlContent)
                    || (type instanceof CmsResourceTypeXmlPage)
                    || OpenCms.getResourceManager().matchResourceType(
                        BundleType.PROPERTY.toString(),
                        resource.getTypeId());
                if (!hasSourcecodeEditor) {
                    return VISIBILITY_INVISIBLE;
                }
            }

            if (flag(unlocked)) {
                CmsLock lock = resUtil.getLock();
                if (!lock.isUnlocked()) {
                    return VISIBILITY_INVISIBLE;
                }
                prioritize = true;
            }

            if (flag(otherlock)) {
                CmsLock lock = resUtil.getLock();
                if (lock.isUnlocked() || lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                    return VISIBILITY_INVISIBLE;
                }
                prioritize = true;
            }

            if (flag(nootherlock)) {
                CmsLock lock = resUtil.getLock();
                if (!lock.isUnlocked() && !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                    return VISIBILITY_INVISIBLE;
                }
            }

            if (flag(mylock)) {
                CmsLock lock = resUtil.getLock();
                if (!lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                    return VISIBILITY_INVISIBLE;
                }
                prioritize = true;
            }

            if (flag(noinheritedlock)) {
                CmsLock lock = resUtil.getLock();
                if (lock.isInherited()) {
                    return VISIBILITY_INVISIBLE;
                }
            }

            if (flag(notunchangedfile) && resource.isFile() && resUtil.getResource().getState().isUnchanged()) {
                inActiveKey = Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_UNCHANGED_0;
            }

            if (flag(notnew) && (inActiveKey == null) && resource.getState().isNew()) {
                inActiveKey = Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_NEW_UNCHANGED_0;
            }

            if (flag(haseditor)
                && !OpenCms.getWorkplaceManager().getWorkplaceEditorManager().isEditorAvailableForResource(resource)) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(inproject) && (!resUtil.isInsideProject() || resUtil.getProjectState().isLockedForPublishing())) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(notinproject)
                && (resUtil.isInsideProject() || resUtil.getProjectState().isLockedForPublishing())) {
                return VISIBILITY_INVISIBLE;
            }

            if (flag(publishpermission)) {
                try {
                    if (!cms.hasPermissions(
                        resource,
                        CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                        false,
                        CmsResourceFilter.ALL)) {
                        return VISIBILITY_INVISIBLE;
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }

            if (flag(controlpermission)) {
                try {
                    if (!cms.hasPermissions(
                        resource,
                        CmsPermissionSet.ACCESS_CONTROL,
                        false,
                        CmsResourceFilter.IGNORE_EXPIRATION)) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                    }
                } catch (CmsException e) {
                    LOG.warn("Error checking context menu entry permissions", e);
                    return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                }
            }

            if (flag(writepermisssion)) {
                try {
                    if (!resUtil.getLock().isLockableBy(cms.getRequestContext().getCurrentUser())) {
                        // set invisible if not lockable
                        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                    }
                    if (!resUtil.isEditable()
                        || !cms.hasPermissions(
                            resUtil.getResource(),
                            CmsPermissionSet.ACCESS_WRITE,
                            false,
                            CmsResourceFilter.ALL)) {
                        inActiveKey = Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0;
                    }
                } catch (CmsException e) {
                    LOG.debug("Error checking context menu entry permissions.", e);
                    return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                }
            }

            if (flag(notdeleted) && (inActiveKey == null) && resUtil.getResource().getState().isDeleted()) {
                inActiveKey = Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_DELETED_0;
            }

            if (flag(deleted) && !resource.getState().isDeleted()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

        } else {
            return VISIBILITY_INVISIBLE;
        }
        if (inActiveKey != null) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(inActiveKey).prioritize(prioritize);
        }
        return VISIBILITY_ACTIVE.prioritize(prioritize);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        return getVisibility(context.getCms(), context.getResources());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "visibility[" + m_flags + "]";
    }
}
