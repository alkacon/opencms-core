/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A cache class for storing inherited container configurations.<p>
 */
public class CmsContainerConfigurationCacheState {

    /** The standard file name for inherited container configurations. */
    public static final String INHERITANCE_CONFIG_FILE_NAME = ".inherited";

    /** The logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsContainerConfigurationCacheState.class);

    /** Inheritance configurations by structure id. */
    private Map<CmsUUID, CmsContainerConfigurationGroup> m_configurationsById = new HashMap<CmsUUID, CmsContainerConfigurationGroup>();

    /** The map of cached configurations, with the base paths as keys. */
    private Map<String, CmsContainerConfigurationGroup> m_configurationsByPath = new HashMap<String, CmsContainerConfigurationGroup>();

    /**
     * Creates a new cache state.<p>
     *
     * @param groups the inheritance group configurations.<p>
     */
    public CmsContainerConfigurationCacheState(Collection<CmsContainerConfigurationGroup> groups) {
        for (CmsContainerConfigurationGroup group : groups) {
            if (group != null) {
                m_configurationsByPath.put(getBasePath(group.getRootPath()), group);
                m_configurationsById.put(group.getStructureId(), group);
            }
        }
    }

    /**
     * Gets the container configuration for a given root path, name and locale.<p>
     *
     * @param rootPath the root path
     * @param name the configuration name
     *
     * @return the container configuration for the given combination of parameters
     */
    public synchronized CmsContainerConfiguration getContainerConfiguration(String rootPath, String name) {

        String key = getCacheKey(rootPath);
        if (m_configurationsByPath.containsKey(key)) {
            CmsContainerConfigurationGroup group = m_configurationsByPath.get(key);
            CmsContainerConfiguration result = group.getConfiguration(name);
            return result;
        }
        return null;
    }

    /**
     * Creates a new inheritance container cache state, which is based on this instance, but with some changed configurations.<p>
     *
     * @param updateGroups a map of the updated configurations, with the structure ids as keys
     *
     * @return the new cache state
     */
    public CmsContainerConfigurationCacheState updateWithChangedGroups(
        Map<CmsUUID, CmsContainerConfigurationGroup> updateGroups) {

        Map<CmsUUID, CmsContainerConfigurationGroup> newGroups = Maps.newHashMap(m_configurationsById);

        for (Map.Entry<CmsUUID, CmsContainerConfigurationGroup> entry : updateGroups.entrySet()) {
            CmsUUID key = entry.getKey();
            CmsContainerConfigurationGroup group = entry.getValue();
            if (group == null) {
                newGroups.remove(key);
            } else {
                newGroups.put(key, group);
            }
        }
        return new CmsContainerConfigurationCacheState(newGroups.values());
    }

    /**
     * Returns the base path for a given configuration file.
     *
     * E.g. the result for the input '/sites/default/.container-config' will be '/sites/default'.<p>
     *
     * @param rootPath the root path of the configuration file
     *
     * @return the base path for the configuration file
     */
    protected String getBasePath(String rootPath) {

        if (rootPath.endsWith(INHERITANCE_CONFIG_FILE_NAME)) {
            return rootPath.substring(0, rootPath.length() - INHERITANCE_CONFIG_FILE_NAME.length());
        }
        return rootPath;
    }

    /**
     * Gets the cache key for a given base path.<p>
     *
     * @param basePath the base path
     *
     * @return the cache key for the base path
     */
    protected String getCacheKey(String basePath) {

        assert !basePath.endsWith(INHERITANCE_CONFIG_FILE_NAME);
        return CmsFileUtil.addTrailingSeparator(basePath);
    }

}
