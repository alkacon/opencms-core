/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeJsp.java,v $
* Date   : $Date: 2002/09/19 06:04:09 $
* Version: $Revision: 1.4 $
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

import com.opencms.flex.CmsJspLoader;

/**
 * This class describes the resource-type JSP.
 * It was needed because JSP should have property "export" set to "false" by default.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 */

public class CmsResourceTypeJsp extends com.opencms.file.CmsResourceTypePlain {

    public static final String C_TYPE_RESOURCE_NAME = "jsp";

    public com.opencms.file.CmsResource createResource(com.opencms.file.CmsObject cms, String folder, String name, java.util.Hashtable properties, byte[] contents) throws com.opencms.core.CmsException{
        com.opencms.file.CmsResource res = cms.doCreateFile(folder, name, contents, C_TYPE_RESOURCE_NAME, properties);
        String filename = folder + name;
        // Lock the new file
        cms.lockResource(filename);
        // Attach default JSP properties
        cms.writeProperty(filename, C_PROPERTY_EXPORT, "false");
        // JSP content encoding default it "ISO-8859-1" by JSP standard
        cms.writeProperty(filename, C_PROPERTY_CONTENT_ENCODING, CmsJspLoader.C_DEFAULT_JSP_ENCODING);
        return res;
    }
}