/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypePointer.java,v $
 * Date   : $Date: 2005/03/17 10:31:09 $
 * Version: $Revision: 1.3 $
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
import org.opencms.loader.CmsPointerLoader;
import org.opencms.main.OpenCms;

/**
 * Resource type descriptor for the type "pointer".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 */
public class CmsResourceTypePointer extends A_CmsResourceType {

    /** The type id of this resource type. */
    private static final int C_RESOURCE_TYPE_ID = 99;

    /** The name of this resource type. */
    private static final String C_RESOURCE_TYPE_NAME = "pointer";

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypePointer() {

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
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsPointerLoader.C_RESOURCE_LOADER_ID;
    }
    
    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String)
     */
    public void initConfiguration(String name, String id) throws CmsConfigurationException {
        
        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) &&  m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException("Resource type "
                + this.getClass().getName()
                + " with static name='"
                + getStaticTypeName()
                + "' static id='"
                + getStaticTypeId()
                + "' can't be reconfigured");
        }
        
        if (!C_RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException("Resource type "
                + this.getClass().getName()
                + " must be configured with resource type name '"
                + C_RESOURCE_TYPE_NAME
                + "' (not '"
                + name
                + "')");
        }
        
        // freeze the configuration
        m_staticFrozen = true;
        
        super.initConfiguration(C_RESOURCE_TYPE_NAME, id);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
    }
}