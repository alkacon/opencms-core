package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a user.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
public abstract class A_CmsUser {
	
	/**
	 * Gets the login-name of the user.
	 * 
	 * @return the login-name of the user.
	 */
	abstract public String getName();
	
	/**
	 * Gets the id of this user.
	 * 
	 * @return the id of this user.
	 */
	abstract long getID();
	
	/**
	 * Gets the description of this user.
	 * 
	 * @return the description of this user.
	 */
	abstract public String getDescription();
	
    /**
     * Desides, if this user is disabled.
     * 
     * @return USER_FLAGS == C_FLAG_DISABLED
     */
    abstract public boolean getDisabled();
	
	/**
	 * Returns the USER_FLAGS.
	 * 
	 * @return the USER_FLAGS.
	 */
	abstract public int getFlags();
	
	/**
	 * Returns the last login date.
	 * 
	 * @return the last login date.
	 */	 
	abstract public long getLastLoginDate();
	
	/**
	 * Returns the default group for this user.
	 * 
	 * @return the default group for this user.
	 */
	abstract public I_CmsGroup getDefaultGroup();

	/**
	 * Returns the current group for this user.
	 * 
	 * @return the current group for this user.
	 */
	abstract public I_CmsGroup getCurrentGroup();

	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    abstract public boolean equals(Object obj);

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    abstract public int hashCode();
	
	/**
	 * Returns additional information about the user. <BR/>
	 * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
	 * 
	 * The additional infos must be requested via the CmsObject.
	 * 
	 * 
	 * @param key the key to the additional information.
	 * 
	 * Returns additional information about the user. If the additional info
	 * does not exists, it returns null.
	 * 
	 */
	abstract public String getAdditionalInfo(String key);
	
	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_EMAIL);</pre>
	 * 
	 * @return the USER_EMAIL, or null.
	 */
	abstract public String getAdditionalInfoEmail();

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_FIRSTNAME);</pre>
	 * 
	 * @return the USER_FIRSTNAME, or null.
	 */
	abstract public String getAdditionalInfoFirstname();

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_SURNAME);</pre>
	 * 
	 * @return the USER_SURNAME, or null.
	 */
	abstract public String getAdditionalInfoSurname();
	
	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_ADDRESS);</pre>
	 * 
	 * @return the USER_ADDRESS, or null.
	 */
	abstract public String getAdditionalInfoAddress();

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_SECTION);</pre>
	 * 
	 * @return the USER_SECTION, or null.
	 */
	abstract public String getAdditionalInfoSection();

	// the following methods are not used, because the functionality is handled by
	// a I_CmsObjectBase:
	/*
	public boolean isAdminUser();
    public Vector getGroups();
    public boolean isMemberOf(I_CmsGroup group);
	*/
}
