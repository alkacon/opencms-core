package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This public class describes the access to users information in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/21 15:08:47 $
 */
interface I_CmsAccessUserInfo {

     /**
	 * Creates a new hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * @param object The hashtable including the user information
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void addUserInformation(int id, Hashtable object)
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
	public Hashtable readUserInformation(int id)
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
	public void writeUserInformation(int id , Hashtable infos)
		throws  CmsException;

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
