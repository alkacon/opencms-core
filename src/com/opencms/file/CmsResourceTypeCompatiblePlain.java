/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeCompatiblePlain.java,v $
* Date   : $Date: 2003/07/11 14:00:08 $
* Version: $Revision: 1.7 $
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
import com.opencms.core.I_CmsConstants;

/**
 * The resource type compatible plain is a system used resourcetype to show
 * that the resource does not stick to the rules. This only concerns resources
 * in the /content path or the /system/modules/... path. These resources can only
 * copied or deleted.
 *
 * @author Hanjo Riege
 * @version $Revision: 1.7 $Date: 2003/01/20 23:59:17 $
 *
 */
public class CmsResourceTypeCompatiblePlain extends CmsResourceTypePlain {

    /**
    * Copies a Resource.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfolder.
    * @param keepFlags <code>true</code> if the copy should keep the source file's flags,
    *        <code>false</code> if the copy should get the user's default flags.
    *
    * @throws CmsException if the file couldn't be copied, or the user
    * has not the appropriate rights to copy the file.
    */
    public void copyResource(CmsObject cms, String source, String destination, boolean keepFlags) throws CmsException{
        cms.doCopyFile(source, destination);
        cms.doChtype(destination, I_CmsConstants.C_TYPE_PLAIN_NAME);
    }

}