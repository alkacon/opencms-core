/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsAccessUser.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
 * Version: $Revision: 1.6 $
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
 * This interface describes the access to users in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/02/15 17:44:00 $
 */
interface I_CmsAccessUser {

	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(String username)
		throws CmsException;
    
     /**
	 * Returns a user object.<P/>
	 * 
	 * @param userid The id of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(int id)
		throws CmsException;
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	public A_CmsUser readUser(String username, String password)
		throws CmsException;

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param description The description for the user.
	 * 
	 * @return user The added user will be returned.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public A_CmsUser createUser(String name, String password, 
				               String description)
		throws CmsException;

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(String username)
		throws CmsException;

	/**
	 * Writes the user information in the Cms.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param user The Cms useer to be updated
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeUser(A_CmsUser user)
		throws CmsException;

	/**
	 * Returns all users<P/>
	 * 
	 * @return users A Vector of all existing users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsers()
        throws CmsException;


	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(String username, String newPassword)
		throws CmsException;
}
