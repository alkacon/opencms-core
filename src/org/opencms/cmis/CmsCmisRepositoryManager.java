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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;

/**
 * This class creates the OpenCms repository objects for CMIS and provides access to them via the repository ids.<p>
 */
public class CmsCmisRepositoryManager {

    /** The internal map of repositories. */
    private Map<String, CmsCmisRepository> m_repositoryMap = new HashMap<String, CmsCmisRepository>();

    /** The delay between type refreshs. */
    public static final long REFRESH_DELAY = 1000 * 60 * 10;

    /** The logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsCmisRepositoryManager.class);

    /** The type manager instance used to keep track of types available for CMIS. */
    CmsCmisTypeManager m_typeManager;

    /** Timer instance used for refreshing the types periodically. */
    private Timer m_timer = new Timer(true);

    /**
     * Adds a new CMIS repository.<p>
     * 
     * @param repository the repository to add 
     */
    void addRepository(CmsCmisRepository repository) {

        m_repositoryMap.put(repository.getId(), repository);
    }

    /** 
     * Initializes the repositories.<p>
     * 
     * @param adminCms the admin CMS context 
     */
    public void initialize(CmsObject adminCms) {

        try {
            m_typeManager = new CmsCmisTypeManager(adminCms);
            m_timer.purge();
            m_timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    try {
                        m_typeManager.setup();
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

            }, REFRESH_DELAY, REFRESH_DELAY);

            CmsResource root = adminCms.readResource("/");
            CmsCmisRepository onlineRepository = new CmsCmisRepository(m_typeManager, adminCms, root, "online", true);
            CmsObject offlineCms = OpenCms.initCmsObject(adminCms);
            CmsProject offline = adminCms.readProject("Offline");
            offlineCms.getRequestContext().setCurrentProject(offline);
            CmsCmisRepository offlineRepository = new CmsCmisRepository(
                m_typeManager,
                offlineCms,
                root,
                "offline",
                false);
            addRepository(offlineRepository);
            addRepository(onlineRepository);
        } catch (CmsException e) {
            LOG.error("Repository could not be initialized!");
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
