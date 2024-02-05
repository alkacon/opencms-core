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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypeFolder extends A_CmsResourceTypeFolderBase {

    /** Configuration key for the optional list of resource types to show as available index page types. */
    public static final String CONFIGURATION_INDEX_PAGE_TYPE = "restypes.indexpage";

    /** The type id of this resource. */
    public static final int RESOURCE_TYPE_ID = 0;

    /** The name of this resource type. */
    public static final String RESOURCE_TYPE_NAME = "folder";

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The serial version id. */
    private static final long serialVersionUID = -6627448037959709694L;

    /** The configured list of resource types to show as available index page types. */
    private String m_indexPageTypes;

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeFolder() {

        super();
        m_typeId = RESOURCE_TYPE_ID;
        m_typeName = RESOURCE_TYPE_NAME;
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     *
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     *
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        super.addConfigurationParameter(paramName, paramValue);
        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_INDEX_PAGE_TYPE.equalsIgnoreCase(paramName)) {
                m_indexPageTypes = paramValue.trim();
            }
        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        CmsParameterConfiguration additional = super.getConfiguration();
        if ((additional != null) && (additional.size() > 0)) {
            result.putAll(additional);
        }
        if (CmsStringUtil.isNotEmpty(getIndexPageTypes())) {
            result.put(CONFIGURATION_INDEX_PAGE_TYPE, getIndexPageTypes());
        }
        return result;
    }

    /**
     * Returns the indexPageTypes.<p>
     *
     * @return the indexPageTypes
     */
    public String getIndexPageTypes() {

        return m_indexPageTypes;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_CONFIG_FROZEN_3,
                    this.getClass().getName(),
                    getStaticTypeName(),
                    Integer.valueOf(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                    this.getClass().getName(),
                    RESOURCE_TYPE_NAME,
                    name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration
        m_staticTypeId = m_typeId;
    }
}