/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsModuleADEConfigProvider.java,v $
 * Date   : $Date: 2010/09/22 14:27:47 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.cache.CmsVfsCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.util.CmsStringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * A class for reading ADE configuration data from modules.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsModuleADEConfigProvider extends CmsVfsCache {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleADEConfigProvider.class);

    /** The set of file names of the modules' configuration files. */
    Set<String> m_files = new HashSet<String>();

    /** The offline configuration data. */
    private CmsConfigurationParser m_offlineConfig;

    /** The online configuration data. */
    private CmsConfigurationParser m_onlineConfig;

    /**
     * Constructor.<p>
     * 
     * @param key the name of the module parameter which contains the configuration file name 
     */
    public CmsModuleADEConfigProvider(String key) {

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
     */
    public synchronized CmsConfigurationParser getConfigurationParser(CmsObject cms) {

        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        CmsConfigurationParser result = internalGetConfiguration(online);
        if (result == null) {
            result = readConfiguration(cms);
            internalSetConfiguration(online, result);
        }
        return result;
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected synchronized void flush(boolean online) {

        internalSetConfiguration(online, null);
    }

    /**
     * Gets the online or offline configuration depending on a flag.<p>
     * 
     * @param online if true, returns the online configuration, else the offline configuration 
     * 
     * @return the online or offline configuration 
     */
    protected CmsConfigurationParser internalGetConfiguration(boolean online) {

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
    protected void internalSetConfiguration(boolean online, CmsConfigurationParser config) {

        if (online) {
            m_onlineConfig = config;
        } else {
            m_offlineConfig = config;
        }
    }

    /**
     * Reads the configuration from the configured modules.
     * 
     * @param cms the CMS context
     *  
     * @return the configuration 
     */
    protected CmsConfigurationParser readConfiguration(CmsObject cms) {

        CmsConfigurationParser parser = new CmsConfigurationParser();
        for (String path : m_files) {
            try {
                CmsResource resource = cms.readResource(path);
                CmsConfigurationParser otherParser = CmsConfigurationParser.getParser(cms, resource);
                parser.update(otherParser);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return parser;
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected synchronized void uncacheResource(CmsResource res) {

        if (m_files.contains(res.getRootPath())) {
            m_offlineConfig = null;
        }
    }

}
