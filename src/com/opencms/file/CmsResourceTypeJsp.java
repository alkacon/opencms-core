/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeJsp.java,v $
* Date   : $Date: 2002/10/31 11:38:44 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.core.CmsException;
import com.opencms.flex.CmsJspLoader;

import java.util.Hashtable;

/**
 * Describes the resource-type JSP, ensures that some needed VFS
 * file properties are attached to new JSPs.
 * 
 * @param cms the current CmsObject
 * @param newResourceName the name of the new file in the VFS 
 * @param properties property Hashtable
 * @param contents the new file contents
 * @param parameter object that might contain additional inormation for the resource creation
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.6 $
 * 
 * @throws CmsException in case of an exception while manipulating the new file
 */

public class CmsResourceTypeJsp extends com.opencms.file.CmsResourceTypePlain {

    public static final String C_TYPE_RESOURCE_NAME = "jsp";

    public CmsResource createResource(CmsObject cms, String newResourceName, Hashtable properties, byte[] contents, Object parameter) 
    throws CmsException {
        com.opencms.file.CmsResource res = cms.doCreateFile(newResourceName, contents, C_TYPE_RESOURCE_NAME, properties);
        // Lock the new file
        cms.lockResource(newResourceName);
        // Attach default JSP properties
        cms.writeProperty(newResourceName, C_PROPERTY_EXPORT, "false");
        // JSP content encoding default it "ISO-8859-1" by JSP standard
        cms.writeProperty(newResourceName, C_PROPERTY_CONTENT_ENCODING, CmsJspLoader.C_DEFAULT_JSP_ENCODING);
        return res;
    }
}