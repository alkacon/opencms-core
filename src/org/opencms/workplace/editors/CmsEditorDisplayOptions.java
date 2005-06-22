/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditorDisplayOptions.java,v $
 * Date   : $Date: 2005/06/22 10:38:25 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.LRUMap;
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
 * /system/workplace/editors/configuration/.<br> * 
 * Set navigation position property values on the configuration files
 * and use the permission system to determine which groups and users
 * should use which configuration file.<br>
 * The configuration with the most enabled options should be the first in navigation, 
 * followed by configurations with less enabled options, because 
 * the first file readable for the current user will be used for configuration.<p>
 * 
 * If no configuration file can be found for the current user, 
 * all display options will be disabled by default.<p>
 *
 * @author  Andreas Zahner 
 * @version $Revision: 1.6 $
 * 
 * @since 5.1.14
 */
public class CmsEditorDisplayOptions {
    
    /** The name of the configuration folder.<p> */
    public static final String C_FOLDER_EDITORCONFIGURATION = CmsEditor.C_PATH_EDITORS + "configuration/";
    
    /** Mapping entry name that is used if no mapping is available for the user.<p> */
    public static final String C_NO_MAPPING_FOR_USER = "na";
    
    /** Maximum size of the stored editor configurations.<p> */
    public static final int C_SIZE_CONFIGURATIONFILES = 12;
    
    /** Maximum size of the user editor configuration mappings.<p> */
    public static final int C_SIZE_USERENTRIES = 100;
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorDisplayOptions.class);  
    
    /** Stores all loaded editor configuration options.<p> */
    private Map m_loadedConfigurations;
    
    /** Stores the mappings of users to their configuration options to use.<p> */
    private Map m_userMappings;

    /**
     * Constructor that initializes the editor display options for the workplace.<p>
     */
    public CmsEditorDisplayOptions() {
        // initialize members
        m_userMappings = new LRUMap(C_SIZE_USERENTRIES);
        m_loadedConfigurations = new LRUMap(C_SIZE_CONFIGURATIONFILES);
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
    public synchronized Properties getDisplayOptions(CmsJspActionElement jsp) {

        // get the configuration file name for the current user
        String mappedConfigFile = (String)m_userMappings.get(jsp.getRequestContext().currentUser().getName());
        Properties displayOptions = null;
        if (mappedConfigFile == null) {
            // no configuration file name stored for user, get the navigation items of the configuration folder
            List items = jsp.getNavigation().getNavigationForFolder(C_FOLDER_EDITORCONFIGURATION);
            if (items.size() > 0) {
                // get first found configuration file
                CmsJspNavElement nav = (CmsJspNavElement)items.get(0);
                mappedConfigFile = nav.getFileName();
                displayOptions = (Properties)m_loadedConfigurations.get(nav.getFileName());
                if (displayOptions == null) {
                    // configuration file has not yet been loaded, load it
                    try {
                        // read configuration file
                        CmsFile optionFile = jsp.getCmsObject().readFile(
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
                        mappedConfigFile = C_NO_MAPPING_FOR_USER;
                    } catch (IOException e) {
                        // set configuration to not available
                        if (LOG.isInfoEnabled()) {
                            LOG.info(e);
                        }
                        mappedConfigFile = C_NO_MAPPING_FOR_USER;
                        displayOptions = null;
                    }
                }
            } else {
                // no configuration available for current user, store this in mapping
                mappedConfigFile = C_NO_MAPPING_FOR_USER;
            }
            // store the file name of the configuration file for the current user
            m_userMappings.put(jsp.getRequestContext().currentUser().getName(), mappedConfigFile);
            if (LOG.isDebugEnabled()) {
                // check which mapping has been stored
                LOG.debug(Messages.get().key(
                    Messages.LOG_MAP_CONFIG_FILE_TO_USER_2, mappedConfigFile, jsp.getRequestContext().currentUser().getName()));
            }
        } else {
            // configuration file for current user is known, get options from loaded configurations
            displayOptions = (Properties)m_loadedConfigurations.get(mappedConfigFile);
        }
        // return the editor display options for this user
        return displayOptions;
    }
    
    /**
     * Determines if the given element should be shown in the editor.<p>
     * 
     * @param key the element key name which should be displayed
     * @param displayOptions the display options for the current user
     * @return true if the element should be shown, otherwise false
     */
    public boolean showElement(String key, Properties displayOptions) {

        return (displayOptions != null && Boolean.valueOf(displayOptions.getProperty(key)).booleanValue());
    }

}
