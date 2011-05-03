/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/A_CmsModuleConfigProvider.java,v $
 * Date   : $Date: 2011/05/03 10:49:09 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.config;

import org.opencms.cache.CmsVfsCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * An abstract base class used for reading module-specific configuration files which are configured by module
 * parameters.
 * 
 * @param <Config> the type of configuration data 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsModuleConfigProvider<Config extends I_CmsMergeable<Config>> extends CmsVfsCache {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsModuleConfigProvider.class);

    /** The set of file names of the modules' configuration files. */
    Set<String> m_files = new HashSet<String>();

    /** A CMS context with admin privileges. */
    private CmsObject m_adminCms;

    /** The offline configuration data. */
    private Config m_offlineConfig;

    /** The online configuration data. */
    private Config m_onlineConfig;

    /**
     * Constructor.<p>
     * 
     * @param adminCms a CMS context with admin privileges 
     * 
     * @param key the name of the module parameter which contains the configuration file name 
     */
    protected A_CmsModuleConfigProvider(CmsObject adminCms, String key) {

        m_adminCms = adminCms;
        CmsModuleManager manager = OpenCms.getModuleManager();
        List<CmsModule> modules = manager.getAllInstalledModules();
        for (CmsModule module : modules) {
            String config = module.getParameter(key);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(config)) {
                m_files.add(config);
            }
        }
        registerEventListener();
    }

    /**
     * Gets the configuration of the installed modules.<p>
     * 
     * @param cms the CMS context
     *  
     * @return the configuration of the installed modules 
     * 
     * @throws CmsException if something goes wrong 
     */
    public Config getConfiguration(CmsObject cms) throws CmsException {

        synchronized (OpenCms.getADEConfigurationManager()) {
            CmsObject acms = initCmsObject(cms);
            boolean online = acms.getRequestContext().getCurrentProject().isOnlineProject();
            Config result = internalGetConfiguration(online);
            if (result == null) {
                result = readConfiguration(acms);
                internalSetConfiguration(online, result);
            }
            return result;
        }
    }

    /**
     * Creates an empty configuration object.<p>
     * 
     * @return an empty configuration object 
     */
    protected abstract Config createEmptyConfiguration();

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        synchronized (OpenCms.getADEConfigurationManager()) {
            internalSetConfiguration(online, null);
            fireFlush(online);
        }
    }

    /**
     * Gets the online or offline configuration depending on a flag.<p>
     * 
     * @param online if true, returns the online configuration, else the offline configuration 
     * 
     * @return the online or offline configuration 
     */
    protected Config internalGetConfiguration(boolean online) {

        if (online) {
            return m_onlineConfig;
        } else {
            return m_offlineConfig;
        }
    }

    /**
     * Sets the online or offline configuration depending on a flag.<p>
     * 
     * @param online if true, the online configuration will be set, else the offline configuration 
     * @param config the new configuration 
     */
    protected void internalSetConfiguration(boolean online, Config config) {

        if (online) {
            m_onlineConfig = config;
        } else {
            m_offlineConfig = config;
        }
    }

    /**
     * Reads the configuration from the configured modules.
     * 
     * @param adminCms the CMS context
     *  
     * @return the configuration 
     */
    protected Config readConfiguration(CmsObject adminCms) {

        Config result = createEmptyConfiguration();
        for (String path : m_files) {
            try {
                CmsResource resource = adminCms.readResource(path);
                Config config = readSingleConfiguration(adminCms, resource);
                result = result.merge(config);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Reads a single configuration file.<p>
     * 
     * @param adminCms the (admin) CMS context 
     * @param res the configuration file to read
     * 
     * @return the configuration data from the file 
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract Config readSingleConfiguration(CmsObject adminCms, CmsResource res) throws CmsException;

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource res) {

        synchronized (OpenCms.getADEConfigurationManager()) {
            if (m_files.contains(res.getRootPath())) {
                m_offlineConfig = null;
                fireFlush(false);
            }
        }
    }

    /**
     * Initializes an admin CMS context, with the project copied from another CMS context.<p>
     * 
     * @param cms the CMS context whose project should be used
     *  
     * @return the initialized admin CMS context
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_adminCms);
        result.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        return result;
    }

    /** The list of cache flush handlers. */
    private List<I_CmsCacheFlushHandler> m_flushHandlers = new ArrayList<I_CmsCacheFlushHandler>();

    /**
     * Adds another cache flush handler.<p>
     * 
     * @param handler the cache flush handler 
     */
    public void addFlushHandler(I_CmsCacheFlushHandler handler) {

        m_flushHandlers.add(handler);
    }

    /**
     * Notifies all the flush handlers of a cache flush.<p>
     * 
     * @param online true if the online cache was flushed 
     */
    public void fireFlush(boolean online) {

        for (I_CmsCacheFlushHandler handler : m_flushHandlers) {
            handler.onFlushCache(online);
        }
    }
}
