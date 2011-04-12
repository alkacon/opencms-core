/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsSitemapConfigurationData.java,v $
 * Date   : $Date: 2011/04/12 11:59:14 $
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

package org.opencms.ade.config;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsDetailPageInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration data class for the sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapConfigurationData implements I_CmsMergeable<CmsSitemapConfigurationData> {

    /** The detail page configuration. */
    private Map<String, List<CmsDetailPageInfo>> m_detailPageLists;

    /** The configuration source. */
    private CmsConfigurationSourceInfo m_lastSource;

    /** The maximum sitemap depth. */
    private int m_maxDepth;

    /** New elements. */
    private Collection<CmsConfigurationItem> m_newElements = new LinkedHashSet<CmsConfigurationItem>();

    /** The property configuration. */
    private Map<String, CmsXmlContentProperty> m_propertyDefinitions;

    /** The type configuration. */
    private Map<String, CmsConfigurationItem> m_typeConfiguration;

    /**
     * Creates an empty configuration data object.<p>
     */
    public CmsSitemapConfigurationData() {

        this(
            new HashMap<String, CmsConfigurationItem>(),
            new LinkedHashSet<CmsConfigurationItem>(),
            new HashMap<String, CmsXmlContentProperty>(),
            new HashMap<String, List<CmsDetailPageInfo>>(),
            -1,
            new CmsConfigurationSourceInfo(null, false));

    }

    /**
     * Creates a new configuration data object.<p>
     * 
     * @param typeConfiguration the type configuration 
     * @param newElements the new elements configuration
     * @param propertyDefinitions the property configuration 
     * @param detailPageLists the detail page configuration 
     * @param maxDepth the maximum sitemap depth
     * @param lastSource the configuration source  
     */
    public CmsSitemapConfigurationData(
        Map<String, CmsConfigurationItem> typeConfiguration,
        Collection<CmsConfigurationItem> newElements,
        Map<String, CmsXmlContentProperty> propertyDefinitions,
        Map<String, List<CmsDetailPageInfo>> detailPageLists,
        int maxDepth,
        CmsConfigurationSourceInfo lastSource) {

        m_typeConfiguration = typeConfiguration;
        m_newElements = newElements;
        m_propertyDefinitions = propertyDefinitions;
        m_detailPageLists = detailPageLists;
        m_maxDepth = maxDepth;
        m_lastSource = lastSource;
    }

    /**
     * Gets the detail page configuration.<p>
     * 
     * @return the detail page configuration 
     */
    public Map<String, List<CmsDetailPageInfo>> getDetailPageInfo() {

        return Collections.unmodifiableMap(m_detailPageLists);
    }

    /**
     * Gets the configuration source.<p>
     * 
     * @return the configuration source information 
     */
    public CmsConfigurationSourceInfo getLastSource() {

        return m_lastSource;
    }

    /** 
     * Gets the maximum sitemap depth.<p>
     *  
     * @return the maximum sitemap depth 
     */
    public int getMaxDepth() {

        return m_maxDepth;
    }

    /**
     * Returns the newElements.<p>
     *
     * @return the newElements
     */
    public Collection<CmsConfigurationItem> getNewElements() {

        return Collections.unmodifiableCollection(m_newElements);
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
        for (CmsConfigurationItem item : m_newElements) {

            CmsResource source = item.getSourceFile();
            boolean hasView = cms.hasPermissions(source, CmsPermissionSet.ACCESS_VIEW);
            String type = OpenCms.getResourceManager().getResourceType(source.getTypeId()).getTypeName();
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);
            boolean editable = settings.isEditable(cms, source);
            boolean controlPermission = settings.getAccess().getPermissions(cms, source).requiresControlPermission();
            if (editable && controlPermission && hasView) {
                result.add(source);
            }
        }
        return result;
    }

    /**
     * Gets the property configuration.<p>
     * 
     * @return the property configuration 
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfiguration() {

        return Collections.unmodifiableMap(m_propertyDefinitions);
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
    public CmsSitemapConfigurationData merge(CmsSitemapConfigurationData data) {

        Map<String, CmsConfigurationItem> typeConfig = new LinkedHashMap<String, CmsConfigurationItem>();
        typeConfig.putAll(m_typeConfiguration);
        typeConfig.putAll(data.m_typeConfiguration);

        Collection<CmsConfigurationItem> newElements = new LinkedHashSet<CmsConfigurationItem>();
        newElements.addAll(m_newElements);
        newElements.addAll(data.m_newElements);

        Map<String, CmsXmlContentProperty> propertyDefs = new LinkedHashMap<String, CmsXmlContentProperty>();
        propertyDefs.putAll(m_propertyDefinitions);
        propertyDefs.putAll(data.m_propertyDefinitions);

        Map<String, List<CmsDetailPageInfo>> detailPageLists = new LinkedHashMap<String, List<CmsDetailPageInfo>>();
        detailPageLists.putAll(m_detailPageLists);
        detailPageLists.putAll(data.m_detailPageLists);

        int maxDepth = data.m_maxDepth;
        CmsConfigurationSourceInfo lastSource = m_lastSource.merge(data.m_lastSource);

        CmsSitemapConfigurationData result = new CmsSitemapConfigurationData(
            typeConfig,
            newElements,
            propertyDefs,
            detailPageLists,
            maxDepth,
            lastSource);

        return result;
    }

}
