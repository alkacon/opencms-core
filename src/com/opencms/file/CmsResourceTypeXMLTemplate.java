/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeXMLTemplate.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
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

import java.util.Hashtable;

/**
 * Describes the resource type "XMLTemplate".
 *
 * @version $Revision: 1.6 $
 */
public class CmsResourceTypeXMLTemplate extends CmsResourceTypePlain {

    public static final String C_TYPE_RESOURCE_NAME = "XMLTemplate";

    public CmsResource createResource(CmsObject cms, String newResourceName, Hashtable properties, byte[] contents) throws CmsException{
        CmsResource res = cms.doCreateFile(newResourceName, contents, C_TYPE_RESOURCE_NAME, properties);
        // lock the new file
        cms.lockResource(newResourceName);
        return res;
    }
}