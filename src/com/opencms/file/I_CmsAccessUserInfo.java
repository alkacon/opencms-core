/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsAccessUserInfo.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
 * Version: $Revision: 1.4 $
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

package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This interface describes the access to users information in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.4 $ $Date: 2000/02/15 17:44:00 $
 */
interface I_CmsAccessUserInfo {

     /**
	 * Creates a new hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param user The user to create additional infos for.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void addUserInformation(A_CmsUser user)
        throws  CmsException;
    
	/**
	 * Reads a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is read from the database and deserialized.
	 * 
	 * @param user The the user to read the infos from.
	 * 
	 * @return user The user completed with the addinfos.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsUser readUserInformation(A_CmsUser user)
        throws CmsException;

	/**
	 * Writes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is serialized and written into the databse.
	 * 
	 * @param user The user to write the additional infos.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void writeUserInformation(A_CmsUser user)
         throws CmsException;

	/**
	 * Deletes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteUserInformation(int id)
		throws CmsException;
}
