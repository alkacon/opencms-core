package com.opencms.file;

import java.util.*;

/**
 * This interface describes a resource in the Cms.
 * This resource can be a I_CmsFile or a I_CmsFolder.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
abstract class A_CmsResource {
	/**
	 * Returns the absolute path for this resource.<BR/>
	 * Example: retuns /system/def/language.cms
	 * 
	 * @return the absolute path for this resource.
	 */
    abstract public String getAbsolutePath();
	
	/**
	 * Returns the absolute path of the parent.<BR/>
	 * Example: /system/def has the parent /system/<BR/>
	 * / has no parent
	 * 
	 * @return the parent absolute path, or null if this is the root-resource.
	 */
    abstract public String getParent();
	
	/**
	 * Returns the path for this resource.<BR/>
	 * Example: retuns /system/def/ for the
	 * resource /system/def/language.cms
	 * 
	 * @return the path for this resource.
	 */
	abstract public String getPath();
	
	/**
	 * Returns the name of this resource.<BR/>
	 * Example: retuns language.cms for the
	 * resource /system/def/language.cms
	 * 
	 * @return the name of this resource.
	 */
    abstract public String getName();
    	
	/**
	 * Gets the type for this resource.
	 * 
	 * @return the type of this resource.
	 */
	abstract public I_CmsResourceType getType();

	/**
	 * Returns the date of the creation for this resource.
	 * 
	 * @return the date of the creation for this resource.
	 */
	abstract public long getDateCreated();
	
	/**
	 * Returns the date of the last modification for this resource.
	 * 
	 * @return the date of the last modification for this resource.
	 */
    abstract public long getDateLastModified();

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
	 * Returns the resource-header without any file or folder specific stuff.
	 * 
	 * @return the resource-header without any file or folder specific stuff.
	 */
	abstract public I_CmsResource getHeader();
    
	/**
	 * Returns the userid of the resource owner.
	 * 
	 * @return the userid of the resource owner.
	 */
	abstract long getOwnerID();
	
	/**
	 * Returns the groupid of this resource.
	 * 
	 * @return the groupid of this resource.
	 */
    abstract long getGroupID();
	
	/**
	 * Returns the accessflags of this resource.
	 * 
	 * @return the accessflags of this resource.
	 */
    abstract public int getFlags();
	
	/**
	 * Returns the id of this resource.<BR/>
	 * Some resources have no id, C_UNKNOWN_ID will be returned.
	 * 
	 * @return the id of this resource.
	 */	
    abstract long getID();
    	
	/**
	 * Determines, if this resource is a folder.
	 * 
	 * @return true, if this resource is a folder, else it returns false.
	 */
    abstract public boolean isFolder();

	/**
	 * Determines, if this resource is a file.
	 * 
	 * @return true, if this resource is a file, else it returns false.
	 */
    abstract public boolean isFile();
	
	/**
	 * Returns the state of this resource.<BR/>
	 * This may be C_STATE_UNCHANGED, C_STATE_CHANGED, C_STATE_NEW or C_STATE_DELETED.
	 * 
	 * @return the state of this resource.
	 */
	abstract public int getState();
	
	/**
	 * Determines, if this resource is locked by a user.
	 * 
	 * @return true, if this resource is locked by a user, else it returns false.
	 */
	abstract public boolean isLocked();

	/**
	 * Returns the user that locked this resource.
	 * 
	 * @return the user that locked this resource.
	 * If this resource is free it returns null.
	 */
	abstract public I_CmsUser isLockedBy();

	/**
	 * Returns the project for this resource.
	 * 
	 * @return the project for this resource.
	 */
	abstract public I_CmsProject getProject();
	
	// the following methods are not used, because the functionality is handled by
	// a I_CmsObjectBase:
	/*
	public boolean canRead();
    public boolean canWrite();
    public boolean accessPublic(int flags);
    public boolean accessPublicRead();
    public boolean accessPublicWrite();
    public boolean accessPublicVisible();
    public boolean accessInternalRead();
    public boolean accessUser(I_CmsUser user, int flags);
    public boolean accessUserRead(I_CmsUser user);
    public boolean accessUserWrite(I_CmsUser user);
    public boolean accessUserVisible(I_CmsUser user);
    public boolean accessGroupRead(I_CmsGroup group);
    public boolean accessGroupWrite(I_CmsGroup group);
    public boolean accessGroupVisible(I_CmsGroup group);
    public boolean isOwnedBy(I_CmsUser user);
    public boolean isOwnedBy(int id);
    public boolean isOfGroup(I_CmsUser user);
    public boolean isOfGroup(I_CmsGroup group);
    public boolean isOfGroup(int id);   
	*/
}
