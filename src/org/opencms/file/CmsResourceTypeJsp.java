/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2004/06/14 14:25:57 $
 * Version: $Revision: 1.6 $
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

package org.opencms.file;

import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Describes the resource type "JSP", ensures that some needed VFS
 * file properties are attached to new JSPs.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.6 $
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
     * @see org.opencms.file.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {        
        
        if (C_CONFIGURATION_JSP_ENCODING.equalsIgnoreCase(paramName)) {
            m_defaultEncoding = CmsEncoder.lookupEncoding(paramValue.trim(), OpenCms.getSystemInfo().getDefaultEncoding());
        }
    }    
        
    /**
     * Creates a new JSP resource.<p>
     * 
     * @param cms the current CmsObject
     * @param newResourceName the name of the new file in the VFS 
     * @param properties property Hashtable
     * @param contents the new file contents
     * @param parameter object that might contain additional inormation for the resource creation
     * @return the created resource 
     * @throws CmsException in case of an exception while manipulating the new file
     */
    public CmsResource createResource(CmsObject cms, String newResourceName, List properties, byte[] contents, Object parameter) throws CmsException {
        
        CmsResource res = cms.doCreateFile(newResourceName, contents, getResourceTypeName(), properties);
        contents = null;
        // lock the new file
        cms.lockResource(newResourceName);
        // set export to false      
        cms.writePropertyObject(newResourceName, new CmsProperty(I_CmsConstants.C_PROPERTY_EXPORT, null, "false"));
        // set JSP content encoding
        cms.writePropertyObject(newResourceName, new CmsProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, null, m_defaultEncoding));
        return res;
    }
    
    /**
     * @see org.opencms.file.A_CmsResourceType#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {
        
        ExtendedProperties result = new ExtendedProperties();
        if (! OpenCms.getSystemInfo().getDefaultEncoding().equals(m_defaultEncoding)) {
            // only write encoding back to configuration if different from default
            result.put(C_CONFIGURATION_JSP_ENCODING, m_defaultEncoding);
        }
        return result;
    }   
    
    /**
     * @see org.opencms.file.I_CmsResourceType#initConfiguration()
     */
    public void initConfiguration() {

        // ensure default content encoding is set
        if (m_defaultEncoding == null) {
            m_defaultEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }
        m_defaultEncoding = m_defaultEncoding.intern();
    }     

    /**
     * @see org.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsJspLoader.C_RESOURCE_LOADER_ID;       
    } 
    
    /**
     * @see org.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }
}