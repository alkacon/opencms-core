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

package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

/**
 * Provides methods to determine the display options of a workplace editor for the current user.<p>
 *
 * On the editor JSP, do the following:
 * <ul>
 * <li>get the object instance with <code>OpenCms.getWorkplaceManager().getEditorDisplayOptions()</code>.</li>
 * <li>get the Properties for the current user with <code>getDisplayOptions(CmsJspActionElement)</code>.</li>
 * <li>use <code>showElement(key, Properties)</code> to determine if an element is shown.</li>
 * </ul>
 *
 * Define your editor display options in property files located in the VFS folder
 * <code>/system/workplace/editors/configuration/</code>.<p>
 *
 * Set navigation position property values on the configuration files
 * and use the permission system to determine which groups and users
 * should use which configuration file.<p>
 *
 * The configuration with the most enabled options should be the first in navigation,
 * followed by configurations with less enabled options, because
 * the first file readable for the current user will be used for configuration.<p>
 *
 * If no configuration file can be found for the current user,
 * all display options will be disabled by default.<p>
 *
 * @since 6.0.0
 */
public class CmsEditorDisplayOptions {

    /** The name of the configuration folder.<p> */
    public static final String FOLDER_EDITORCONFIGURATION = CmsEditor.PATH_EDITORS + "configuration/";

    /** Mapping entry name that is used if no mapping is available for the user.<p> */
    public static final String NO_MAPPING_FOR_USER = "na";

    /** Maximum size of the stored editor configurations.<p> */
    public static final int SIZE_CONFIGURATIONFILES = 12;

    /** Maximum size of the user editor configuration mappings.<p> */
    public static final int SIZE_USERENTRIES = 100;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorDisplayOptions.class);

    /** Stores all loaded editor configuration options.<p> */
    private Map<Object, Object> m_loadedConfigurations;

    /** Stores the mappings of users to their configuration options to use.<p> */
    private Map<Object, Object> m_userMappings;

    /**
     * Constructor that initializes the editor display options for the workplace.<p>
     */
    public CmsEditorDisplayOptions() {

        // initialize members
        m_userMappings = CmsCollectionsGenericWrapper.createLRUMap(SIZE_USERENTRIES);
        m_loadedConfigurations = CmsCollectionsGenericWrapper.createLRUMap(SIZE_CONFIGURATIONFILES);
    }

    /**
     * Clears the cached user configuration data, casing a reload off all configurations.<p>
     */
    public synchronized void clearCache() {

        m_userMappings.clear();
        m_loadedConfigurations.clear();
    }

    /**
     * Reads the editor configuration file valid for the current user and caches the result in a Map.<p>
     *
     * The configuration settings of the found file are stored in a Map holding the loaded configuration
     * with the configuration file name as key.<p>
     *
     * The configuration file name to use for the current user is stored in another Map with the user name
     * as key.<p>
     *
     * @param jsp the JSP action element to access the VFS and current user information
     * @return the display options to use for the current user or null if no display options were found
     */
    public Properties getDisplayOptions(CmsJspActionElement jsp) {

        return getDisplayOptions(jsp.getCmsObject());
    }

    /**
     * Reads the editor configuration file valid for the current user and caches the result in a Map.<p>
     *
     * The configuration settings of the found file are stored in a Map holding the loaded configuration
     * with the configuration file name as key.<p>
     *
     * The configuration file name to use for the current user is stored in another Map with the user name
     * as key.<p>
     *
     * @param cms the CmsObject to access the VFS and current user information
     * @return the display options to use for the current user or null if no display options were found
     */
    public Properties getDisplayOptions(CmsObject cms) {

        // get the configuration file name for the current user
        String mappedConfigFile = (String)m_userMappings.get(cms.getRequestContext().getCurrentUser().getName());
        Properties displayOptions;
        if (mappedConfigFile == null) {
            // no configuration file name stored for user, get the navigation items of the configuration folder
            List<CmsJspNavElement> items = new CmsJspNavBuilder(cms).getNavigationForFolder(FOLDER_EDITORCONFIGURATION);
            if (items.size() > 0) {
                // get first found configuration file
                CmsJspNavElement nav = items.get(0);
                mappedConfigFile = nav.getFileName();
                synchronized (m_loadedConfigurations) {
                    // must sync read/write access to shared map
                    displayOptions = (Properties)m_loadedConfigurations.get(nav.getFileName());
                    if (displayOptions == null) {
                        // configuration file has not yet been loaded, load it
                        try {
                            // read configuration file
                            CmsFile optionFile = cms.readFile(
                                nav.getResourceName(),
                                CmsResourceFilter.IGNORE_EXPIRATION);
                            InputStream in = new ByteArrayInputStream(optionFile.getContents());
                            displayOptions = new Properties();
                            displayOptions.load(in);
                            // store loaded options
                            m_loadedConfigurations.put(nav.getFileName(), displayOptions);
                        } catch (CmsException e) {
                            // set configuration to not available
                            if (LOG.isInfoEnabled()) {
                                LOG.info(e);
                            }
                            mappedConfigFile = NO_MAPPING_FOR_USER;
                        } catch (IOException e) {
                            // set configuration to not available
                            if (LOG.isInfoEnabled()) {
                                LOG.info(e);
                            }
                            mappedConfigFile = NO_MAPPING_FOR_USER;
                            displayOptions = null;
                        }
                    }
                }
            } else {
                // no configuration available for current user, store this in mapping
                mappedConfigFile = NO_MAPPING_FOR_USER;
                displayOptions = null;
            }
            if (LOG.isDebugEnabled()) {
                // check which mapping has been stored
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_MAP_CONFIG_FILE_TO_USER_2,
                        mappedConfigFile,
                        cms.getRequestContext().getCurrentUser().getName()));
            }
            // store the file name of the configuration file for the current user
            m_userMappings.put(cms.getRequestContext().getCurrentUser().getName(), mappedConfigFile);
        } else {
            // configuration file for current user is known, get options from loaded configurations
            displayOptions = (Properties)m_loadedConfigurations.get(mappedConfigFile);
        }
        // return the editor display options for this user
        return displayOptions;
    }

    /**
     * Returns the value for the given key from the display options.<p>
     *
     * @param key he element key name which should be read
     * @param defaultValue the default value to use in case the property is not found
     * @param displayOptions the display options for the current user
     *
     * @return the value for the given key from the display options
     */
    public String getOptionValue(String key, String defaultValue, Properties displayOptions) {

        if (displayOptions == null) {
            return defaultValue;
        }
        return displayOptions.getProperty(key, defaultValue);
    }

    /**
     * Determines if the given element should be shown in the editor.<p>
     *
     * @param key the element key name which should be displayed
     * @param displayOptions the display options for the current user
     *
     * @return true if the element should be shown, otherwise false
     */
    public boolean showElement(String key, Properties displayOptions) {

        return showElement(key, null, displayOptions);
    }

    /**
     * Determines if the given element should be shown in the editor.<p>
     *
     * @param key the element key name which should be displayed
     * @param defaultValue the default value to use in case the property is not found, should be a boolean value as String
     * @param displayOptions the display options for the current user
     *
     * @return true if the element should be shown, otherwise false
     */
    public boolean showElement(String key, String defaultValue, Properties displayOptions) {

        if (defaultValue == null) {
            return ((displayOptions != null) && Boolean.valueOf(displayOptions.getProperty(key)).booleanValue());
        }
        if (displayOptions == null) {
            return Boolean.valueOf(defaultValue).booleanValue();
        }
        return Boolean.valueOf(displayOptions.getProperty(key, defaultValue)).booleanValue();
    }
}