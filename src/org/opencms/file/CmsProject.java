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

package org.opencms.file;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.A_CmsModeIntEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.List;

/**
 * Describes an OpenCms project,
 * which contains a set of VFS resources that are being worked on at the same time.<p>
 *
 * @since 6.0.0
 */
public class CmsProject implements Cloneable, Comparable<CmsProject>, Serializable {

    /**
     *  Enumeration class for project types.<p>
     */
    public static final class CmsProjectType extends A_CmsModeIntEnumeration {

        /** Project type normal. */
        protected static final CmsProjectType MODE_PROJECT_NORMAL = new CmsProjectType(0);

        /** Project type temporary. */
        protected static final CmsProjectType MODE_PROJECT_TEMPORARY = new CmsProjectType(1);

        /** Project type 'workflow'. */
        protected static final CmsProjectType MODE_PROJECT_WORKFLOW = new CmsProjectType(2);

        /** Serializable version id. */
        private static final long serialVersionUID = -8701314451776599534L;

        /**
         * Private constructor.<p>
         *
         * @param mode the copy mode integer representation
         */
        private CmsProjectType(int mode) {

            super(mode);
        }

        /**
         * Returns the copy mode object from the old copy mode integer.<p>
         *
         * @param mode the old copy mode integer
         *
         * @return the copy mode object
         */
        public static CmsProjectType valueOf(int mode) {

            switch (mode) {
                case 0:
                    return CmsProjectType.MODE_PROJECT_NORMAL;
                case 1:
                    return CmsProjectType.MODE_PROJECT_TEMPORARY;
                case 2:
                    return CmsProjectType.MODE_PROJECT_WORKFLOW;
                default:
                    return CmsProjectType.MODE_PROJECT_NORMAL;
            }
        }

        /**
         * Returns the default flags which should be set when a new project of this type is created.<p>
         *
         * @return the default flags for the project type
         */
        public int getDefaultFlags() {

            //            if (getMode() == CmsProjectType.MODE_PROJECT_WORKFLOW.getMode()) {
            //                return PROJECT_FLAG_HIDDEN;
            //            }
            return PROJECT_FLAG_NONE;
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -4552095577282894706L;

    /** The name of the online project. */
    public static final String ONLINE_PROJECT_NAME = "Online";

    /** The id of the online project. */
    public static final CmsUUID ONLINE_PROJECT_ID = CmsUUID.getConstantUUID(ONLINE_PROJECT_NAME);

    /** Indicates that a project is invisible in the workplace. */
    public static final int PROJECT_FLAG_HIDDEN = 4;

    /** Indicates that a normal project. */
    public static final int PROJECT_FLAG_NONE = 0;

    /** Indicates that a project should be hidden from the workplace project selector, but should otherwise behave the same as normal projects. */
    public static final int PROJECT_HIDDEN_IN_SELECTOR = 8;

    /** Indicates a normal project. */
    public static final CmsProjectType PROJECT_TYPE_NORMAL = CmsProjectType.MODE_PROJECT_NORMAL;

    /** The project type for a workflow project. */
    public static final CmsProjectType PROJECT_TYPE_WORKFLOW = CmsProjectType.MODE_PROJECT_WORKFLOW;

    /** Indicates a temporary project that is deleted after it is published. */
    public static final CmsProjectType PROJECT_TYPE_TEMPORARY = CmsProjectType.MODE_PROJECT_TEMPORARY;

    /** The creation date of this project. */
    private long m_dateCreated;

    /** The description of this project. */
    private String m_description;

    /** The state of this project. */
    private int m_flags;

    /** The manager group id of this project. */
    private CmsUUID m_groupManagersId;

    /** The id of the user group of this project. */
    private CmsUUID m_groupUsersId;

    /** The id of this project. */
    private CmsUUID m_id;

    /** The name of this project. */
    private String m_name;

    /** The id of this projects owner. */
    private CmsUUID m_ownerId;

    /** The type of this project. */
    private CmsProjectType m_type;

    /**
     * Default constructor for gui usage.<p>
     */
    public CmsProject() {

        // empty
    }

    /**
     * Creates a new CmsProject.<p>
     *
     * @param projectId the id to use for this project
     * @param projectFqn the name for this project
     * @param description the description for this project
     * @param ownerId the owner id for this project
     * @param groupId the group id for this project
     * @param managerGroupId the manager group id for this project
     * @param flags the flags for this project
     * @param dateCreated the creation date of this project
     * @param type the type of this project
     */
    public CmsProject(
        CmsUUID projectId,
        String projectFqn,
        String description,
        CmsUUID ownerId,
        CmsUUID groupId,
        CmsUUID managerGroupId,
        int flags,
        long dateCreated,
        CmsProjectType type) {

        m_id = projectId;
        m_name = projectFqn;
        m_description = description;
        m_ownerId = ownerId;
        m_groupUsersId = groupId;
        m_groupManagersId = managerGroupId;
        m_flags = flags;
        m_type = type;
        m_dateCreated = dateCreated;
    }

    /**
     * Throws a runtime exception if name is empty.<p>
     *
     * @param name the project name to check
     */
    public static void checkProjectName(String name) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_PROJECTNAME_VALIDATION_0));
        }
    }

    /**
     * Checks if the full resource name (including the site root) of a resource matches
     * any of the project resources of a project.<p>
     *
     * @param projectResources a List of project resources as Strings
     * @param resource the resource to check
     * @return true, if the resource is "inside" the project resources
     */
    public static boolean isInsideProject(List<String> projectResources, CmsResource resource) {

        String resourcename = resource.getRootPath();
        return isInsideProject(projectResources, resourcename);
    }

    /**
     * Checks if the full resource name (including the site root) of a resource matches
     * any of the project resources of a project.<p>
     *
     * @param projectResources a List of project resources as Strings
     * @param resourcename the resource to check
     * @return true, if the resource is "inside" the project resources
     */
    public static boolean isInsideProject(List<String> projectResources, String resourcename) {

        for (int i = (projectResources.size() - 1); i >= 0; i--) {
            String projectResource = projectResources.get(i);
            if (CmsResource.isFolder(projectResource)) {
                if (resourcename.startsWith(projectResource)) {
                    // folder - check only the prefix
                    return true;
                }
            } else {
                if (resourcename.equals(projectResource)) {
                    // file - check the full path
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given project id is the online project id.<p>
     *
     * @param projectId the project id to check
     * @return true if the given project id is the online project id
     */
    public static boolean isOnlineProject(CmsUUID projectId) {

        return projectId.equals(CmsProject.ONLINE_PROJECT_ID);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        return new CmsProject(
            m_id,
            m_name,
            m_description,
            m_ownerId,
            m_groupUsersId,
            m_groupManagersId,
            m_flags,
            m_dateCreated,
            m_type);
    }

    /**
     * Compares this instance to another given object instance of this class .<p>
     *
     * @param o the other given object instance to compare with
     * @return integer value for sorting the objects
     */
    public int compareTo(CmsProject o) {

        if (o == this) {
            return 0;
        }

        // compare the names
        return m_name.compareTo(o.getName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsProject) {
            return ((CmsProject)obj).m_id.equals(m_id);
        }
        return false;
    }

    /**
     * Returns the creation date of this project.<p>
     *
     * @return the creation date of this project
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the description of this project.
     *
     * @return the description of this project
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the state of this project.<p>
     *
     * @return the state of this project
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the user group id of this project.<p>
     *
     * @return the user group id of this project
     */
    public CmsUUID getGroupId() {

        return m_groupUsersId;
    }

    /**
     * Returns the manager group id of this project.<p>
     *
     * @return the manager group id of this project
     */
    public CmsUUID getManagerGroupId() {

        return m_groupManagersId;
    }

    /**
     * Returns the name of this project.<p>
     *
     * @return the name of this project
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the fully qualified name of the associated organizational unit.<p>
     *
     * @return the fully qualified name of the associated organizational unit
     */
    public String getOuFqn() {

        return CmsOrganizationalUnit.getParentFqn(m_name);
    }

    /**
     * Returns the user id of the project owner.<p>
     *
     * @return the user id of the project owner
     */
    public CmsUUID getOwnerId() {

        return m_ownerId;
    }

    /**
     * Returns the simple name of this organizational unit.
     *
     * @return the simple name of this organizational unit.
     */
    public String getSimpleName() {

        return CmsOrganizationalUnit.getSimpleName(m_name);
    }

    /**
     * Returns the type of this project.<p>
     *
     * @return the type of this project
     */
    public CmsProjectType getType() {

        return m_type;
    }

    /**
     * Returns the id of this project.<p>
     *
     * @return the id of this project
     */
    public CmsUUID getUuid() {

        return m_id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_name != null) {
            return m_name.hashCode();
        }
        return 0;
    }

    /**
     * Returns the delete After Publishing flag.<p>
     *
     * @return the delete After Publishing flag
     *
     * @see #getType()
     */
    public boolean isDeleteAfterPublishing() {

        return (m_type == CmsProject.PROJECT_TYPE_TEMPORARY);
    }

    /**
     * Returns the 'hidden' flag.<p>
     *
     * @return the 'hidden' flag
     *
     * @see #getFlags()
     */
    public boolean isHidden() {

        return (getFlags() & PROJECT_FLAG_HIDDEN) == PROJECT_FLAG_HIDDEN;
    }

    /**
     * Checks if the project should be hidden from the project selector in the workplace.<p>
     *
     * @return true if the project should not appear in the workplace's project selector
     */
    public boolean isHiddenFromSelector() {

        return isWorkflowProject() || (0 != (getFlags() & PROJECT_HIDDEN_IN_SELECTOR));
    }

    /**
     * Returns <code>true</code> if this project is the Online project.<p>
     *
     * @return <code>true</code> if this project is the Online project
     */
    public boolean isOnlineProject() {

        return isOnlineProject(m_id);
    }

    /**
     * Returns true if this is a workflow project.<p>
     *
     * @return true if this is a workflow project
     */
    public boolean isWorkflowProject() {

        return getType().getMode() == PROJECT_TYPE_WORKFLOW.getMode();
    }

    /**
     * Sets the delete After Publishing flag.<p>
     *
     * @param deleteAfterPublishing the delete After Publishing flag to set
     */
    public void setDeleteAfterPublishing(boolean deleteAfterPublishing) {

        m_type = deleteAfterPublishing ? CmsProject.PROJECT_TYPE_TEMPORARY : CmsProject.PROJECT_TYPE_NORMAL;
    }

    /**
     * Sets the description of this project.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the flags of this project.<p>
     *
     * @param flags the flag to set
     */
    public void setFlags(int flags) {

        m_flags = flags;
    }

    /**
     * Sets the user group id of this project.<p>
     *
     * @param id the user group id of this project
     */
    public void setGroupId(CmsUUID id) {

        CmsUUID.checkId(id, false);
        m_groupUsersId = id;
    }

    /**
     * Sets the 'hidden' flag.<p>
     *
     * @param value the value to set
     */
    public void setHidden(boolean value) {

        if (isHidden() != value) {
            setFlags(getFlags() ^ PROJECT_FLAG_HIDDEN);
        }
    }

    /**
     * Sets the manager group id of this project.<p>
     *
     * @param id the manager group id of this project
     */
    public void setManagerGroupId(CmsUUID id) {

        CmsUUID.checkId(id, false);
        m_groupManagersId = id;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        checkProjectName(name);
        m_name = name;
    }

    /**
     * Sets the owner id of this project.<p>
     *
     * @param id the id of the new owner
     */
    public void setOwnerId(CmsUUID id) {

        CmsUUID.checkId(id, false);
        m_ownerId = id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Project]:");
        result.append(m_name);
        result.append(" , Id=");
        result.append(m_id);
        result.append(", Desc=");
        result.append(m_description);
        return result.toString();
    }

    /**
     * Sets the type of this project.<p>
     *
     * @param type the type to set
     */
    void setType(CmsProjectType type) {

        m_type = type;
    }
}
