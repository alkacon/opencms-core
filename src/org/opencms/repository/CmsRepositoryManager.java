/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/CmsRepositoryManager.java,v $
 * Date   : $Date: 2011/03/23 14:50:52 $
 * Version: $Revision: 1.9 $
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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The RepositoryManager keeps a list with all configured {@link I_CmsRepository}
 * and can be used to get a repository by its name.<p>
 *
 * The configuration of the repositories is done in the configuration file
 * <code>opencms-importexport.xml</code>.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.2.4
 */
public class CmsRepositoryManager {

    /** Determines if the repository manager was configured or not. */
    private boolean m_configured;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** A list with all configured repositories. */
    private List m_repositoryList;

    /** All initialized repositories, mapped to their name. */
    private Map m_repositoryMap;

    /**
     * Creates a new instance for the resource manager, 
     * will be called by the vfs configuration manager.<p>
     */
    public CmsRepositoryManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_REPOSITORY_CONFIG_0));
        }

        m_repositoryList = new ArrayList();
        m_repositoryMap = new HashMap();
        m_frozen = false;
        m_configured = true;
    }

    /**
     * Creates a new unconfigured instance of the repository manager.<p>
     * 
     * Is used if there are no repositories configured.<p>
     * 
     * @param configured determines if the repository manager was configured
     */
    public CmsRepositoryManager(boolean configured) {

        this();
        m_configured = configured;

        m_repositoryList = Collections.unmodifiableList(m_repositoryList);
        m_frozen = true;
    }

    /**
     * Adds a new configured repository.<p>
     * 
     * @param rep the repository to add
     * 
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addRepositoryClass(I_CmsRepository rep) throws CmsConfigurationException {

        // check if new repositories can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        m_repositoryList.add(rep);
    }

    /**
     * Returns the repositories.<p>
     *
     * @return the repositories
     */
    public List getRepositories() {

        return m_repositoryList;
    }

    /**
     * Returns the repository with the given name.<p>
     * 
     * @param name the name of the repository
     * 
     * @return the repository configured for that name
     */
    public I_CmsRepository getRepository(String name) {

        return (I_CmsRepository)m_repositoryMap.get(name);
    }

    /**
     * Initializes a configuration after all parameters have been added.<p>
     * 
     * @throws CmsConfigurationException if something goes wrong
     */
    public void initConfiguration() throws CmsConfigurationException {

        m_repositoryList = Collections.unmodifiableList(m_repositoryList);

        Iterator iter = m_repositoryList.iterator();
        while (iter.hasNext()) {
            A_CmsRepository rep = (A_CmsRepository)iter.next();
            m_repositoryMap.put(rep.getName(), rep);

            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_ADD_REPOSITORY_2,
                    rep.getClass().getName(),
                    rep.getName()));
            }
            rep.initConfiguration();
        }

        m_frozen = true;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_REPOSITORY_CONFIG_FINISHED_0));
        }

    }

    /**
     * Returns the configured.<p>
     *
     * @return the configured
     */
    public boolean isConfigured() {

        return m_configured;
    }

}
