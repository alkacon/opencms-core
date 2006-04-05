/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsUserProjectSettings.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.4 $
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

package org.opencms.db;

import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * Bean for holding user specific project settings.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.0.0
 */
public class CmsUserProjectSettings implements Serializable {

    /** use well defined serialVersionUID to avoid issues with serialization. */
    private static final long serialVersionUID = 2836833718390927358L;

    /** delete after publish flag. */
    private boolean m_deleteAfterPublishing;

    /** id of the default manager group. */
    private CmsUUID m_managerGroup;

    /** default mode for the files view. */
    private CmsProjectResourcesDisplayMode m_projectFilesMode;

    /** id of the default user group. */
    private CmsUUID m_userGroup;

    /**
     * Default constructor.<p>
     */
    public CmsUserProjectSettings() {

        //noop
    }

    /**
     * Returns the manager Group id.<p>
     *
     * @return the manager Group id
     */
    public CmsUUID getManagerGroup() {

        return m_managerGroup;
    }

    /**
     * Returns the project Files Mode.<p>
     *
     * @return the project Files Mode
     */
    public CmsProjectResourcesDisplayMode getProjectFilesMode() {

        return m_projectFilesMode;
    }

    /**
     * Returns the user Group id.<p>
     *
     * @return the user Group id
     */
    public CmsUUID getUserGroup() {

        return m_userGroup;
    }

    /**
     * Returns the delete After Publish flag.<p>
     *
     * @return the delete After Publish flag
     */
    public boolean isDeleteAfterPublishing() {

        return m_deleteAfterPublishing;
    }

    /**
     * Sets the delete After Publish flag.<p>
     *
     * @param deleteAfterPublish the delete After Publish flag to set
     */
    public void setDeleteAfterPublishing(boolean deleteAfterPublish) {

        m_deleteAfterPublishing = deleteAfterPublish;
    }

    /**
     * Sets the manager Group id.<p>
     *
     * @param managerGroup the manager Group id to set
     */
    public void setManagerGroup(CmsUUID managerGroup) {

        m_managerGroup = managerGroup;
    }

    /**
     * Sets the project Files Mode.<p>
     *
     * @param projectFilesMode the project Files Mode to set
     */
    public void setProjectFilesMode(CmsProjectResourcesDisplayMode projectFilesMode) {

        m_projectFilesMode = projectFilesMode;
    }

    /**
     * Sets the user Group id.<p>
     *
     * @param userGroup the user Group id to set
     */
    public void setUserGroup(CmsUUID userGroup) {

        m_userGroup = userGroup;
    }
}
