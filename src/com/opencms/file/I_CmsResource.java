package com.opencms.file;

import java.util.*;
/**
 * This interface describes a resource in the Cms.
 * This resource can be a I_CmsFile or a I_CmsFolder.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/07 17:25:04 $
 */
public interface I_CmsResource extends I_CmsFlags {
	
	/**
	 * This id will be returned for resources with no id.
	 * (filesystem resources)
	 */
	public final static int C_UNKNOWN_ID		= -1;

	/**
	 * Owner may read this resource
	 */
	public final static int C_OWNER_READ		= 1;

	/**
	 * Owner may write this resource
	 */
	public final static int C_OWNER_WRITE		= 2;
	
	/**
	 * Owner may view this resource
	 */
	public final static int C_OWNER_VISIBLE		= 4;
	
	/**
	 * Group may read this resource
	 */	
	public final static int C_GROUP_READ		= 8;
	
	/**
	 * Group may write this resource
	 */
	public final static int C_GROUP_WRITE		= 16;
	
	/**
	 * Group may view this resource
	 */
	public final static int C_GROUP_VISIBLE		= 32;
	
	/**
	 * Public may read this resource
	 */	
	public final static int C_PUBLIC_READ		= 64;
	
	/**
	 * Public may write this resource
	 */	
	public final static int C_PUBLIC_WRITE		= 128;

	/**
	 * Public may view this resource
	 */
	public final static int C_PUBLIC_VISIBLE	= 256;

	/**
	 * This is an internal resource, it can't be launched directly.
	 */
	public final static int C_INTERNAL_READ		= 512;
	
	/**
	 * All may read this resource.
	 */
	public final static int	C_READ				= C_OWNER_READ + C_GROUP_READ + C_PUBLIC_READ;

	/**
	 * All may write this resource.
	 */
	public final static int	C_WRITE				= C_OWNER_WRITE + C_GROUP_WRITE + C_PUBLIC_WRITE;
	
	/**
	 * All may view this resource.
	 */
	public final static int	C_VISIBLE			= C_OWNER_VISIBLE + C_GROUP_VISIBLE + C_PUBLIC_VISIBLE;
	
	/**
	 * Owner has full access to this resource.
	 */
	public final static int C_OWNER				= C_OWNER_READ + C_OWNER_WRITE + C_OWNER_VISIBLE;

	/**
	 * Group has full access to this resource.
	 */
	public final static int C_GROUP				= C_GROUP_READ + C_GROUP_WRITE + C_GROUP_VISIBLE;

	/**
	 * Public has full access to this resource.
	 */
	public final static int C_PUBLIC			= C_PUBLIC_READ + C_PUBLIC_WRITE + C_PUBLIC_VISIBLE;
	
	/**
	 * The default-flags for a new resource.
	 */
	public final static int C_DEFAULT_FLAGS		= C_OWNER_READ + C_OWNER_WRITE + C_OWNER_VISIBLE + C_GROUP_READ + C_GROUP_WRITE + C_GROUP_VISIBLE + C_PUBLIC_READ + C_PUBLIC_VISIBLE;
	
	/**
	 * Is set, if the resource is unchanged in this project.
	 */
	public static final int C_STATE_UNCHANGED	= 0;

	/**
	 * Is set, if the resource was changed in this project.
	 */
	public static final int C_STATE_CHANGED		= 1;
	
	/**
	 * Is set, if the resource is new in this project.
	 */	
	public static final int C_STATE_NEW			= 2;
	
	/**
	 * Is set, if the resource was deleted in this project.
	 */	
	public static final int C_STATE_DELETED		= 3;
	
	/**
	 * Returns the absolute path for this resource.<BR>
	 * Example: retuns /system/def/language.cms
	 * 
	 * @return the absolute path for this resource.
	 */
    public String getAbsolutePath();
	
	/**
	 * Returns the absolute path of the parent.<BR>
	 * Example: /system/def has the parent /system/<BR>
	 * / has no parent
	 * 
	 * @return the parent absolute path, or null if this is the root-resource.
	 */
    public String getParent();
	
	/**
	 * Returns the path for this resource.<BR>
	 * Example: retuns /system/def/ for the
	 * resource /system/def/language.cms
	 * 
	 * @return the path for this resource.
	 */
	public String getPath();
	
	/**
	 * Returns the name of this resource.<BR>
	 * Example: retuns language.cms for the
	 * resource /system/def/language.cms
	 * 
	 * @return the name of this resource.
	 */
    public String getName();
    	
	/**
	 * Gets the type for this resource.
	 * 
	 * @return the type of this resource.
	 */
	public I_CmsResourceType getType();

	/**
	 * Returns the date of the creation for this resource.
	 * 
	 * @return the date of the creation for this resource.
	 */
	public long getDateCreated();
	
	/**
	 * Returns the date of the last modification for this resource.
	 * 
	 * @return the date of the last modification for this resource.
	 */
    public long getDateLastModified();

	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    public boolean equals(Object obj);

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    public int hashCode();    

	/**
	 * Returns the resource-header without any file or folder specific stuff.
	 * 
	 * @return the resource-header without any file or folder specific stuff.
	 */
	public I_CmsResource getHeader();
    
	/**
	 * Returns the userid of the resource owner.
	 * 
	 * @return the userid of the resource owner.
	 */
	long getOwnerID();
	
	/**
	 * Returns the groupid of this resource.
	 * 
	 * @return the groupid of this resource.
	 */
    long getGroupID();
	
	/**
	 * Returns the accessflags of this resource.
	 * 
	 * @return the accessflags of this resource.
	 */
    public int getFlags();
	
	/**
	 * Returns the id of this resource.<BR>
	 * Some resources have no id, C_UNKNOWN_ID will be returned.
	 * 
	 * @return the id of this resource.
	 */	
    long getID();
    	
	/**
	 * Determines, if this resource is a folder.
	 * 
	 * @return true, if this resource is a folder, else it returns false.
	 */
    public boolean isFolder();

	/**
	 * Determines, if this resource is a file.
	 * 
	 * @return true, if this resource is a file, else it returns false.
	 */
    public boolean isFile();
	
	/**
	 * Returns the state of this resource.<BR>
	 * This may be C_STATE_UNCHANGED, C_STATE_CHANGED, C_STATE_NEW or C_STATE_DELETED.
	 * 
	 * @return the state of this resource.
	 */
	public int getState();
	
	/**
	 * Determines, if this resource is locked by a user.
	 * 
	 * @return true, if this resource is locked by a user, else it returns false.
	 */
	public boolean isLocked();

	/**
	 * Returns the user that locked this resource.
	 * 
	 * @return the user that locked this resource.
	 * If this resource is free it returns null.
	 */
	public I_CmsUser isLockedBy();

	/**
	 * Returns the project for this resource.
	 * 
	 * @return the project for this resource.
	 */
	public I_CmsProject getProject();
	
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
