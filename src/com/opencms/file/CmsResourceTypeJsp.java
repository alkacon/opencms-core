/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2004/01/06 09:46:26 $
 * Version: $Revision: 1.13 $
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

package com.opencms.file;

import org.opencms.loader.CmsJspLoader;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;

import java.util.Map;

/**
 * Describes the resource type "JSP", ensures that some needed VFS
 * file properties are attached to new JSPs.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.13 $
 */
public class CmsResourceTypeJsp extends A_CmsResourceType {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 8;
    
    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "jsp";
    
    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see com.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsJspLoader.C_RESOURCE_LOADER_ID;       
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
    public CmsResource createResource(CmsObject cms, String newResourceName, Map properties, byte[] contents, Object parameter) throws CmsException {
        com.opencms.file.CmsResource res = cms.doCreateFile(newResourceName, contents, getResourceTypeName(), properties);
        contents = null;
        // Lock the new file
        cms.lockResource(newResourceName);
        // Attach default JSP properties
        cms.writeProperty(newResourceName, I_CmsConstants.C_PROPERTY_EXPORT, "false");
        // JSP content encoding default it "ISO-8859-1" by JSP standard
        cms.writeProperty(newResourceName, I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, CmsJspLoader.C_DEFAULT_JSP_ENCODING);
        return res;
    }
}