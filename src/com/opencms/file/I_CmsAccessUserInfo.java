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
 * @version $Revision: 1.3 $ $Date: 2000/01/28 17:42:31 $
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
