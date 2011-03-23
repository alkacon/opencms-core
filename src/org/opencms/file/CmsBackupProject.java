/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsBackupProject.java,v $
 * Date   : $Date: 2011/03/23 14:51:10 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Describes an OpenCms backup project.<p>
 *
 * @author Alexander Kandzior 
 *
 * @version $Revision: 1.20 $
 * 
 * @since 6.0.0 
 * 
 * @deprecated use {@link org.opencms.file.history.CmsHistoryProject}
 */
public class CmsBackupProject extends org.opencms.file.history.CmsHistoryProject {

    private String m_nameOwner;
    private String m_namePublisher;
    private String m_nameGroupUsers;
    private String m_nameGroupManagers;

    /**
     * Creates a new CmsBackupProject.<p>
     * 
     * @param versionId thw version id for this backup project
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
    public CmsBackupProject(
        int versionId,
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

        super(
            versionId,
            projectId,
            name,
            description,
            ownerId,
            groupId,
            managerGroupId,
            dateCreated,
            type,
            datePublished,
            userPublished,
            projectResources);

        m_namePublisher = namePublisher;
        m_nameOwner = nameOwner;
        m_nameGroupUsers = nameGroupUsers;
        m_nameGroupManagers = nameGroupManagers;

    }

    /**
     * @see org.opencms.file.history.CmsHistoryProject#getOwnerName()
     */
    @Override
    public String getOwnerName() {

        return m_nameOwner;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryProject#getPublishedByName()
     */
    @Override
    public String getPublishedByName() {

        return m_namePublisher;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryProject#getGroupName()
     */
    @Override
    public String getGroupName() {

        return m_nameGroupUsers;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryProject#getManagerGroupName()
     */
    @Override
    public String getManagerGroupName() {

        return m_nameGroupManagers;
    }
}
