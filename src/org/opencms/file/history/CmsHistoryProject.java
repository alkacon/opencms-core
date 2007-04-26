/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/history/CmsHistoryProject.java,v $
 * Date   : $Date: 2007/04/26 14:31:12 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.file.history;

import org.opencms.file.CmsProject;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Describes an OpenCms historical project entry.<p>
 *
 * @author Alexander Kandzior 
 *
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.9.1
 */
public class CmsHistoryProject extends CmsProject implements Cloneable {

    /** The publishing date of this project. */
    private long m_datePublished;

    /** The name of the manager group. */
    private String m_nameGroupManagers;

    /** The name of the user group. */
    private String m_nameGroupUsers;

    /** The name, firstname and lastname of the project owner. */
    private String m_nameOwner;

    /** The name, firstname and lastname of the user who has published the project. */
    private String m_namePublisher;

    /** The resources belonging to the project. */
    private List m_projectResources;

    /** The publish tag of the published project. */
    private int m_publishTag;

    /** The user id of the publisher. */
    private CmsUUID m_userPublished;

    /**
     * Creates a new CmsHistoryProject.<p>
     * 
     * @param publishTag the version id for this historical project
     * @param projectId the id to use for this project
     * @param name the name for this project
     * @param description the description for this project
     * @param ownerId the owner id for this project
     * @param groupId the group id for this project
     * @param managerGroupId the manager group id for this project
     * @param dateCreated the creation date of this project
     * @param type the type of this project
     * @param datePublished the date this backup project was published
     * @param userPublished the id of the user who published
     * @param namePublisher the name of the user who published
     * @param nameOwner the name of the project owner
     * @param nameGroupUsers the name of the project user group
     * @param nameGroupManagers the name of the project manager group
     * @param projectResources a list of resources that are the project "view"
     */
    public CmsHistoryProject(
        int publishTag,
        CmsUUID projectId,
        String name,
        String description,
        CmsUUID ownerId,
        CmsUUID groupId,
        CmsUUID managerGroupId,
        long dateCreated,
        CmsProjectType type,
        long datePublished,
        CmsUUID userPublished,
        String namePublisher,
        String nameOwner,
        String nameGroupUsers,
        String nameGroupManagers,
        List projectResources) {

        super(projectId, name, description, ownerId, groupId, managerGroupId, 0, dateCreated, type);

        m_publishTag = publishTag;
        m_datePublished = datePublished;
        m_userPublished = userPublished;
        m_namePublisher = namePublisher;
        m_nameOwner = nameOwner;
        m_nameGroupUsers = nameGroupUsers;
        m_nameGroupManagers = nameGroupManagers;
        m_projectResources = projectResources;
    }

    /**
     * @see org.opencms.file.CmsProject#clone()
     */
    public Object clone() {

        return new CmsHistoryProject(
            m_publishTag,
            getUuid(),
            getName(),
            getDescription(),
            getOwnerId(),
            getGroupId(),
            getManagerGroupId(),
            getDateCreated(),
            getType(),
            m_datePublished,
            m_userPublished,
            m_namePublisher,
            m_nameOwner,
            m_nameGroupUsers,
            m_nameGroupManagers,
            m_projectResources);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsHistoryProject) {
            return ((CmsHistoryProject)obj).getUuid().equals(getUuid());
        }
        return false;
    }

    /**
     * Returns the projects user group name.<p>
     *
     * @return the projects user group name
     */
    public String getGroupName() {

        return m_nameGroupUsers;
    }

    /**
     * Gets the project manager grou pname.<p>
     *
     * @return the projects manager group name
     */
    public String getManagerGroupName() {

        return m_nameGroupManagers;
    }

    /**
     * Gets the ownername.
     *
     * @return the ownername
     */
    public String getOwnerName() {

        return m_nameOwner;
    }

    /**
     * Returns the project resources (i.e. the "view" of the project).<p>
     * 
     * @return the project resources 
     */
    public List getProjectResources() {

        return m_projectResources;
    }

    /**
     * Gets the published-by value.
     *
     * @return the published-by value
     */
    public CmsUUID getPublishedBy() {

        return m_userPublished;
    }

    /**
     * Gets the publishers name.
     *
     * @return the publishers name
     */
    public String getPublishedByName() {

        return m_namePublisher;
    }

    /**
     * Returns the publishing date of this project.
     *
     * @return the publishing date of this project
     */
    public long getPublishingDate() {

        return m_datePublished;
    }

    /**
     * Returns the publish tag.
     *
     * @return the publish tag
     */
    public int getPublishTag() {

        return m_publishTag;
    }

    /**
     * Gets the versionId.
     *
     * @return the versionId
     * 
     * @deprecated Use {@link #getPublishTag()} instead
     */
    public int getVersionId() {

        return getPublishTag();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return (new Long(m_datePublished)).hashCode();
    }
}