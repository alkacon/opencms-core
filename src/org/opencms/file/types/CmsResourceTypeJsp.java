/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2004/08/11 16:55:58 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Resource type descriptor for the type "jsp".<p>
 * 
 * Ensures that some required file properties are attached to new JSPs.<p>
 * 
 * The value for the encoding properies of a new JSP usually is the
 * system default encoding, but this can be overwritten by 
 * a configuration parameters set in <code>opencms-vfs.xml</code>.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 */
public class CmsResourceTypeJsp extends A_CmsResourceType {

    /** The configuration parameter for "default JSP encoding". */
    public static final String C_CONFIGURATION_JSP_ENCODING = "default.encoding";

    /** The type id of this resource type. */
    public static final int C_RESOURCE_TYPE_ID = 8;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "jsp";

    /** The default encoding to use when creating new JSP pages. */
    private String m_defaultEncoding;

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (C_CONFIGURATION_JSP_ENCODING.equalsIgnoreCase(paramName)) {
            m_defaultEncoding = CmsEncoder.lookupEncoding(paramValue.trim(), OpenCms.getSystemInfo()
                .getDefaultEncoding());
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, java.lang.String, byte[], java.util.List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsDriverManager driverManager,
        String resourcename, 
        byte[] content,
        List properties
    ) throws CmsException {
        
        List newProperties;       
        if (properties == null) {
            newProperties = new ArrayList(); 
        } else {
            newProperties = new ArrayList(properties);
        }
        newProperties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORT, null, "false"));
        newProperties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, null, m_defaultEncoding));

        return super.createResource(cms, driverManager, resourcename, content, newProperties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        ExtendedProperties result = new ExtendedProperties();
        if (!OpenCms.getSystemInfo().getDefaultEncoding().equals(m_defaultEncoding)) {
            // only write encoding back to configuration if different from default
            result.put(C_CONFIGURATION_JSP_ENCODING, m_defaultEncoding);
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsJspLoader.C_RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {

        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getTypeName()
     */
    public String getTypeName() {

        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {
        super.initConfiguration();
        // ensure default content encoding is set
        if (m_defaultEncoding == null) {
            m_defaultEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }
        m_defaultEncoding = m_defaultEncoding.intern();
    }
}