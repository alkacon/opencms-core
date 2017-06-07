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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.wrapper.I_CmsResourceWrapper;
import org.opencms.jlan.CmsJlanRepository;
import org.opencms.jlan.CmsJlanThreadManager;
import org.opencms.jlan.CmsJlanUsers;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * The RepositoryManager keeps a list with all configured {@link I_CmsRepository}
 * and can be used to get a repository by its name.<p>
 *
 * The configuration of the repositories is done in the configuration file
 * <code>opencms-importexport.xml</code>.<p>
 *
 * @since 6.2.4
 */
public class CmsRepositoryManager {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRepositoryManager.class);

    /** Separator between a wrapper class and its parameters. */
    private static final String WRAPPER_CONFIG_SEPARATOR = ":";

    /** Determines if the repository manager was configured or not. */
    private boolean m_configured;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** The JLAN thread manager. */
    private CmsJlanThreadManager m_jlanThreadManager;

    /** The list of repositories. */
    private List<I_CmsRepository> m_repositoryList = new ArrayList<I_CmsRepository>();

    /** All initialized repositories, mapped to their name. */
    private Map<String, I_CmsRepository> m_repositoryMap = new LinkedHashMap<String, I_CmsRepository>();

    /**
     * Creates a new instance for the resource manager,
     * will be called by the vfs configuration manager.<p>
     */
    public CmsRepositoryManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_REPOSITORY_CONFIG_0));
        }
        m_repositoryMap = new HashMap<String, I_CmsRepository>();
        m_jlanThreadManager = new CmsJlanThreadManager();
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
        m_frozen = true;
    }

    /**
     * Creates a list of resource wrappers from a collection of configuration parameters, for use in configuring repositories.<p>
     *
     * @param config the configuration
     * @param paramName the parameter name
     * @param log the logger to use for error messages
     *
     * @return the list of resource wrappers
     *
     * @throws CmsConfigurationException if something goes wrong with reading the configuration
     */
    public static List<I_CmsResourceWrapper> createResourceWrappersFromConfiguration(
        CmsParameterConfiguration config,
        String paramName,
        Log log) throws CmsConfigurationException {

        List<I_CmsResourceWrapper> wrapperObjects = Lists.newArrayList();
        if (config.containsKey(paramName)) {
            List<String> wrappers = config.getList(paramName);
            for (String wrapperString : wrappers) {
                wrapperString = wrapperString.trim();
                String className;
                String configString = null;
                int separatorPos = wrapperString.indexOf(WRAPPER_CONFIG_SEPARATOR);
                if (separatorPos < 0) {
                    className = wrapperString;
                } else {
                    className = wrapperString.substring(0, separatorPos);
                    configString = wrapperString.substring(separatorPos + 1);
                }

                Class<?> nameClazz;

                // init class for wrapper
                try {
                    nameClazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    log.error(Messages.get().getBundle().key(Messages.LOG_WRAPPER_CLASS_NOT_FOUND_1, className), e);
                    wrapperObjects.clear();
                    break;
                }

                I_CmsResourceWrapper wrapper;
                try {
                    wrapper = (I_CmsResourceWrapper)nameClazz.newInstance();
                    if (configString != null) {
                        wrapper.configure(configString);
                    }
                } catch (InstantiationException e) {
                    throw new CmsConfigurationException(
                        Messages.get().container(Messages.ERR_INVALID_WRAPPER_NAME_1, wrapperString));
                } catch (IllegalAccessException e) {
                    throw new CmsConfigurationException(
                        Messages.get().container(Messages.ERR_INVALID_WRAPPER_NAME_1, wrapperString));
                } catch (ClassCastException e) {
                    throw new CmsConfigurationException(
                        Messages.get().container(Messages.ERR_INVALID_WRAPPER_NAME_1, wrapperString));
                }

                wrapperObjects.add(wrapper);

                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.INIT_ADD_WRAPPER_1, wrapper.getClass().getName()));
                }
            }
        }
        return wrapperObjects;
    }

    /**
     * Adds a new configured repository.<p>
     *
     * @param repository the repository to add
     *
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addRepositoryClass(I_CmsRepository repository) throws CmsConfigurationException {

        // check if new repositories can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }
        m_repositoryList.add(repository);
    }

    /**
     * Gets the additional infos for the user who just logged in which is required for the repositories to work.<p>
     *
     * @param userName the name of the logged in user
     * @param password the password of the logged in user
     *
     * @return the additional info entries which should be written to the user
     */
    public Map<String, Object> getAdditionalInfoForLogin(String userName, String password) {

        Map<String, Object> additionalInfos = new HashMap<String, Object>();
        try {
            additionalInfos.put(CmsJlanUsers.JLAN_HASH, CmsJlanUsers.hashPassword(password));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return additionalInfos;
    }

    /**
     * Returns the repositories.<p>
     *
     * @return the repositories
     */
    public List<I_CmsRepository> getRepositories() {

        return new ArrayList<I_CmsRepository>(m_repositoryMap.values());
    }

    /**
     * Gets a list of the repositories for the given superclass.<p>
     *
     * @param cls the superclass
     *
     * @return the repositories for whose classes the given class is a superclass
     */
    @SuppressWarnings("unchecked")
    public <REPO extends I_CmsRepository> List<REPO> getRepositories(Class<REPO> cls) {

        List<REPO> result = new ArrayList<REPO>();
        for (I_CmsRepository repo : m_repositoryMap.values()) {
            if (cls.isInstance(repo)) {
                result.add((REPO)repo);
            }
        }
        return result;
    }

    /**
     * Returns the repository with the given name.<p>
     *
     * @param name the name of the repository
     *
     * @return the repository configured for that name
     */
    public I_CmsRepository getRepository(String name) {

        return m_repositoryMap.get(name);
    }

    /**
     * Gets a repository by name, but only if its class is a subclass of the class passed as a parameter.<p>
     * Otherwise, null will be returned.<p>
     *
     * @param name the repository name
     * @param cls the class used to filter repositories
     *
     * @return the repository with the given name, or null
     */
    @SuppressWarnings("unchecked")
    public <REPO extends I_CmsRepository> REPO getRepository(String name, Class<REPO> cls) {

        I_CmsRepository repo = getRepository(name);
        if (repo == null) {
            return null;
        }
        if (cls.isInstance(repo)) {
            return (REPO)repo;
        } else {
            return null;
        }

    }

    /**
     * Initializes a configuration after all parameters have been added.<p>
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public void initConfiguration() throws CmsConfigurationException {

        for (I_CmsRepository rep : m_repositoryList) {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_ADD_REPOSITORY_2,
                        rep.getClass().getName(),
                        rep.getName()));
            }
            rep.initConfiguration();
            m_repositoryMap.put(rep.getName(), rep);
        }
        m_frozen = true;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_REPOSITORY_CONFIG_FINISHED_0));
        }

    }

    /**
     * Initializes repositories using an admin CMS object.<p>
     *
     * @param cms the CMS object with admin privileges
     */
    public void initializeCms(CmsObject cms) {

        List<String> toRemove = new ArrayList<String>();
        // Repositories which can't be fully initialized need to be removed.
        for (I_CmsRepository repository : m_repositoryMap.values()) {
            String repoName = repository.getName();
            try {
                repository.initializeCms(cms);
            } catch (CmsException e) {
                LOG.warn("Could not fully initialize repository " + repoName, e);
                toRemove.add(repoName);
            }
        }
        for (String removeRepo : toRemove) {
            m_repositoryMap.remove(removeRepo);
        }

        CmsJlanUsers.setAdminCms(cms);
        if (!getRepositories(CmsJlanRepository.class).isEmpty()) {
            m_jlanThreadManager.start();
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

    /**
     * Shuts down the repository manager.<p>
     */
    public void shutDown() {

        if (m_jlanThreadManager != null) {
            m_jlanThreadManager.stop();
        }
    }

}
