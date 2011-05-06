/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsContainerPageConfigurationData.java,v $
 * Date   : $Date: 2011/05/06 15:53:57 $
 * Version: $Revision: 1.5 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsLazyFolder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The configuration data class for container pages.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageConfigurationData implements I_CmsMergeable<CmsContainerPageConfigurationData> {

    /** The formatter configuration, mapped by resource type. */
    private Map<String, CmsFormatterConfiguration> m_formatterConfiguration;

    /** A bean which represents the source of the configuration data deepest in the tree. */
    private CmsConfigurationSourceInfo m_lastSource;

    /** The resource type configuration. */
    private Map<String, CmsConfigurationItem> m_typeConfiguration;

    /**
     * Creates an empty configuration data object.<p>
     */
    public CmsContainerPageConfigurationData() {

        this(
            new HashMap<String, CmsConfigurationItem>(),
            new HashMap<String, CmsFormatterConfiguration>(),
            new CmsConfigurationSourceInfo(null, false));
    }

    /**
     * Creates a new configuration data object.<p>
     * 
     * @param typeConfiguration the type configuration 
     * @param formatterConfiguration the formatter configuration 
     * @param lastSource the configuration data source 
     */
    public CmsContainerPageConfigurationData(
        Map<String, CmsConfigurationItem> typeConfiguration,
        Map<String, CmsFormatterConfiguration> formatterConfiguration,
        CmsConfigurationSourceInfo lastSource) {

        m_typeConfiguration = typeConfiguration;
        m_formatterConfiguration = formatterConfiguration;
        m_lastSource = lastSource;
    }

    /**
     * Returns if the given type has a valid configuration to be created by the current user.<p>
     * 
     * @param cms the CMS context
     * @param typeName the resource type name
     * @param item the configuration item
     * 
     * @return <code>true</code> if the type can be created as new
     * 
     * @throws CmsException if something goes wrong
     */
    public static boolean isCreatableType(CmsObject cms, String typeName, CmsConfigurationItem item)
    throws CmsException {

        CmsLazyFolder autoFolder = item.getLazyFolder();
        CmsResource permissionCheckFolder = autoFolder.getPermissionCheckFolder(cms);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
        boolean editable = settings.isEditable(cms, permissionCheckFolder);
        boolean controlPermission = settings.getAccess().getPermissions(cms, permissionCheckFolder).requiresControlPermission();
        return editable && controlPermission;
    }

    /**
     * Gets the formatter configuration.<p>
     * 
     * This is a map from resource type names to formatters.<p>
     * 
     * @return the formatter configuration 
     */
    public Map<String, CmsFormatterConfiguration> getFormatterConfiguration() {

        return Collections.unmodifiableMap(m_formatterConfiguration);
    }

    /**
     * Returns the configuration data source for this object which is deepest in the tree.<p>
     *  
     * @return the deepest configuration data source 
     */
    public CmsConfigurationSourceInfo getLastSource() {

        return m_lastSource;
    }

    /**
     * Returns the searchable resource types.<p>
     * 
     * @param cms the CMS context
     * 
     * @return the searchable resource types
     */
    public Collection<CmsResource> getSearchableElements(CmsObject cms) {

        Set<CmsResource> result = new LinkedHashSet<CmsResource>();
        for (CmsConfigurationItem item : m_typeConfiguration.values()) {
            result.add(item.getSourceFile());
        }
        return result;
    }

    /**
     * Gets the list of 'prototype resources' which are used for creating new content elements.
     * 
     * @param cms the CMS context
     * 
     * @return the resources which are used as prototypes for creating new elements
     * 
     * @throws CmsException if something goes wrong 
     */
    public Collection<CmsResource> getNewElements(CmsObject cms) throws CmsException {

        Set<CmsResource> result = new LinkedHashSet<CmsResource>();
        for (Map.Entry<String, CmsConfigurationItem> entry : m_typeConfiguration.entrySet()) {
            CmsConfigurationItem item = entry.getValue();
            String type = entry.getKey();
            if (isCreatableType(cms, type, item)) {
                result.add(item.getSourceFile());
            }
        }
        return result;
    }

    /**
     * Gets the type configuration.<p>
     * 
     * @return the type configuration 
     */
    public Map<String, CmsConfigurationItem> getTypeConfiguration() {

        return Collections.unmodifiableMap(m_typeConfiguration);
    }

    /**
     * @see org.opencms.ade.config.I_CmsMergeable#merge(java.lang.Object)
     */
    public CmsContainerPageConfigurationData merge(CmsContainerPageConfigurationData data) {

        Map<String, CmsConfigurationItem> newTypeConfiguration = new LinkedHashMap<String, CmsConfigurationItem>();
        newTypeConfiguration.putAll(m_typeConfiguration);
        newTypeConfiguration.putAll(data.m_typeConfiguration);

        Map<String, CmsFormatterConfiguration> newFormatterConfiguration = new LinkedHashMap<String, CmsFormatterConfiguration>();
        newFormatterConfiguration.putAll(m_formatterConfiguration);
        newFormatterConfiguration.putAll(data.m_formatterConfiguration);

        CmsConfigurationSourceInfo lastSource = m_lastSource.merge(data.m_lastSource);

        return new CmsContainerPageConfigurationData(newTypeConfiguration, newFormatterConfiguration, lastSource);
    }
}
