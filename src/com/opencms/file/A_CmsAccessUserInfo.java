package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to users information in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/20 17:19:47 $
 */
abstract class A_CmsAccessUserInfo {

     /**
	 * Creates a new hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * @param object The hashtable including the user information
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void addUserInformation(int id, Hashtable object)
		throws  CmsException;
    

    
	/**
	 * Reads a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * 
	 * @return object The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract Hashtable readUserInformation(int id)
		throws CmsException;

	/**
	 * Writes or updates a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * @param infos The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	  */
	abstract void writeUserInformation(int id , Hashtable infos)
		throws  CmsException;

	/**
	 * Deletes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void deleteUserInformation(int id)
		throws CmsException;
}
