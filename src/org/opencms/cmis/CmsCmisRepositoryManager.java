/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cmis;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class creates the OpenCms repository objects for CMIS and provides access to them via the repository ids.<p>
 */
public class CmsCmisRepositoryManager {

    /** The internal map of repositories. */
    private Map<String, CmsCmisRepository> m_repositoryMap = new HashMap<String, CmsCmisRepository>();

    /** 
     * Initializes the repositories.<p>
     * 
     * @param adminCms the admin CMS context 
     */
    public void initialize(CmsObject adminCms) {

        try {
            CmsResource root = adminCms.readResource("/");
            CmsCmisRepository onlineRepository = new CmsCmisRepository(adminCms, root, "online");
            CmsObject offlineCms = OpenCms.initCmsObject(adminCms);
            CmsProject offline = adminCms.readProject("Offline");
            offlineCms.getRequestContext().setCurrentProject(offline);
            CmsCmisRepository offlineRepository = new CmsCmisRepository(offlineCms, root, "offline");
            m_repositoryMap.put("offline", offlineRepository);
            m_repositoryMap.put("online", onlineRepository);
        } catch (CmsException e) {
            System.out.println("Repository could not be initialized!");
        }
    }

    /**
     * Gets a collection of all repositories.<p>
     * 
     * @return the repositories provided by this object 
     */
    public Collection<CmsCmisRepository> getRepositories() {

        return m_repositoryMap.values();
    }

    /**
     * Gets a repository with a given id.<p>
     * 
     * @param repositoryId the repository id 
     * @return the repository with the given id 
     */
    public CmsCmisRepository getRepository(String repositoryId) {

        return m_repositoryMap.get(repositoryId);
    }
}
