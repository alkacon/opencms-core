package com.opencms.file;
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeCompatiblePlain.java,v $
 * Date   : $Date: 2001/07/12 09:09:52 $
 * Version: $Revision: 1.2 $
 *
 * Copyright (C) 2000  The OpenCms Group
 *
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 *
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.core.*;
import com.opencms.util.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import com.opencms.file.genericSql.*;

/**
 * The resource type compatible plain is a system used resourcetype to show
 * that the resource does not stick to the rules. This only concersns resouorces
 * in the /content path or the /system/modules/... path. These resources can only
 * copied or deleted.
 *
 * @author Hanjo Riege
 * @version $Revision: 1.2 $Date: 2001/07/10 15:44:15 $
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
	* @exception CmsException if the file couldn't be copied, or the user
	* has not the appropriate rights to copy the file.
	*/
	public void copyResource(CmsObject cms, String source, String destination, boolean keepFlags) throws CmsException{
		cms.doCopyFile(source, destination);
        if(!keepFlags) {
            setDefaultFlags(cms, destination);
        }
        cms.doChtype(destination, I_CmsConstants.C_TYPE_PLAIN_NAME);
	}

}