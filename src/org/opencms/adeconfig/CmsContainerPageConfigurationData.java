/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/adeconfig/Attic/CmsContainerPageConfigurationData.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
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

package org.opencms.adeconfig;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.CmsConfigurationItem;
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
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageConfigurationData implements I_CmsMergeable<CmsContainerPageConfigurationData> {

    /** The formatter configuration. */
    private Map<String, CmsTypeFormatterConfiguration> m_formatterConfiguration;

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
            new HashMap<String, CmsTypeFormatterConfiguration>(),
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
        Map<String, CmsTypeFormatterConfiguration> formatterConfiguration,
        CmsConfigurationSourceInfo lastSource) {

        m_typeConfiguration = typeConfiguration;
        m_formatterConfiguration = formatterConfiguration;
        m_lastSource = lastSource;
    }

    /**
     * Gets the formatter configuration.<p>
     * 
     * @return the formatter configuration 
     */
    public Map<String, CmsTypeFormatterConfiguration> getFormatterConfiguration() {

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
            CmsResource source = item.getSourceFile();
            CmsLazyFolder autoFolder = item.getLazyFolder();
            CmsResource permissionCheckFolder = autoFolder.getPermissionCheckFolder(cms);
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);
            boolean editable = settings.isEditable(cms, permissionCheckFolder);
            boolean controlPermission = settings.getAccess().getPermissions(cms, permissionCheckFolder).requiresControlPermission();
            if (editable && controlPermission) {
                result.add(source);
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
     * @see org.opencms.adeconfig.I_CmsMergeable#merge(java.lang.Object)
     */
    public CmsContainerPageConfigurationData merge(CmsContainerPageConfigurationData data) {

        Map<String, CmsConfigurationItem> newTypeConfiguration = new LinkedHashMap<String, CmsConfigurationItem>();
        newTypeConfiguration.putAll(m_typeConfiguration);
        newTypeConfiguration.putAll(data.m_typeConfiguration);

        Map<String, CmsTypeFormatterConfiguration> newFormatterConfiguration = new LinkedHashMap<String, CmsTypeFormatterConfiguration>();
        newFormatterConfiguration.putAll(m_formatterConfiguration);
        newFormatterConfiguration.putAll(data.m_formatterConfiguration);

        CmsConfigurationSourceInfo lastSource = m_lastSource.merge(data.m_lastSource);

        return new CmsContainerPageConfigurationData(newTypeConfiguration, newFormatterConfiguration, lastSource);
    }

}
