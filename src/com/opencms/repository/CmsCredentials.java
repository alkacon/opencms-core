/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/repository/Attic/CmsCredentials.java,v $
 * Date   : $Date: 2003/05/28 16:46:54 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.repository;

import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsUser;

import javax.jcr.Credentials;

/**
 * Level 1 implementation of JCR credentials. Provides user/group/project credentials to connect
 * to the OpenCms repository.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/28 16:46:54 $
 * @since 5.1.2
 * @see javax.jcr.Credentials
 */
public class CmsCredentials extends Object implements Credentials {

    /**
     * The current project.
     */
    private CmsProject m_project;
    
    /**
     * The current user.
     */
    private CmsUser m_user;
    
    /**
     * The group of the current user.
     */
    private CmsGroup m_group;

    /**
     * Creates new credentials to connect to the OpenCms repository.
     * 
     * @param user the current user
     * @param group the group of the current user
     * @param project the current project
     */
    public CmsCredentials(CmsUser user, CmsGroup group, CmsProject project) {
        m_user = user;
        m_group = group;
        m_project = project;
    }

    /**
     * Returns the group of the current user.
     * 
     * @return the group of the current user
     */
    public CmsGroup getGroup() {
        return m_group;
    }

    /**
     * Returns the current project.
     * 
     * @return the current project
     */
    public CmsProject getProject() {
        return m_project;
    }

    /**
     * Returns the current user.
     * 
     * @return the current user
     */
    public CmsUser getUser() {
        return m_user;
    }

}
