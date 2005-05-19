/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeFolder.java,v $
 * Date   : $Date: 2005/05/19 16:27:33 $
 * Version: $Revision: 1.17 $
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.OpenCms;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.17 $
 */
public class CmsResourceTypeFolder extends A_CmsResourceTypeFolderBase {

    /** The type id of this resource. */
    public static final int C_RESOURCE_TYPE_ID = 0;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "folder";

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeFolder() {

        super();
        m_typeId = C_RESOURCE_TYPE_ID;
        m_typeName = C_RESOURCE_TYPE_NAME;
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

        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String)
     */
    public void initConfiguration(String name, String id) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) &&  m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!C_RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                C_RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(C_RESOURCE_TYPE_NAME, id);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
    }
}