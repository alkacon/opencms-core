/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsConfigurationSourceInfo.java,v $
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

import org.opencms.file.CmsResource;

/**
 * A bean which contains information about where a configuration is coming from.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsConfigurationSourceInfo implements I_CmsMergeable<CmsConfigurationSourceInfo> {

    /** True if this is a configuration from a module. */
    private boolean m_isModuleConfiguration;

    /** The resource from which the configuration was read. */
    private CmsResource m_resource;

    /** 
     * Creates a new instance.<p>
     * 
     * @param resource the resource from which the configuration was read
     * @param isModuleConfiguration true if the configuration is a module configuration 
     */
    public CmsConfigurationSourceInfo(CmsResource resource, boolean isModuleConfiguration) {

        m_resource = resource;
        m_isModuleConfiguration = isModuleConfiguration;
    }

    /**
     * Gets the resource from which the configuration was read.<p>
     * 
     * @return the resource from which the configuration was read 
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns true if the configuration comes from a module.<p>
     * 
     * @return true if the configuration comes from a module.<p>
     */
    public boolean isModuleConfiguration() {

        return m_isModuleConfiguration;
    }

    /**
     * @see org.opencms.ade.config.I_CmsMergeable#merge(java.lang.Object)
     */
    public CmsConfigurationSourceInfo merge(CmsConfigurationSourceInfo other) {

        if (other.m_resource == null) {
            return this;
        }
        return other;
    }

}
