package com.opencms.file;

import com.opencms.core.*;
import java.io.*;

/**
 * This abstract class describes a mountpoint. A mountpoint defines the type of a filesystem
 * or database containing data used my the Cms and where it is mounted in to the logical
 * filesystem of  the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/23 12:51:45 $
 */

public abstract class A_CmsMountPoint implements Serializable {
    
    	
	/**
	 * Returns the Mountpoint of a CmsMountPoint Object.
	 * @return The mountpoint in the OpenCms filesystem.
	 */
	public abstract String getMountpoint();
	
    
	/**
	 * Returns the mountpath of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The physical location this mountpoint directs to or null.
	 */
	public abstract String getMountPath();
       
    
	/**
	 * Returns the database driver of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_MYSQL.
	 * @return The database driver of this mountpoint or null.
	 */
	public abstract String getDriver() ;

	
     /**
	 * Returns the database connect string of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_MYSQL.
	 * 
	 * @return The database connect string of this mountpoint or null.
	 */
	public abstract String getConnect() ;
	
    
	 /**
	 * Returns the name of a CmsMountPoint Object.
	 * @return The name of this mount point.
	 */
	public abstract String getName();
	
    
	 /**
	 * Returns the default filetype of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default file type that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getType();
	
    
     /**
	 * Returns the default user id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default user id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
    public abstract int getUser() ;
 
    
     /**
	 * Returns the default group id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default user id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getGroup() ;
    
        
     /**
	 * Returns the default project id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default project id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getProject() ;
    
        
    /**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public abstract String toString() ;
    
        /**
	 * Returns the default flags of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default flags that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getFlags();
   
     /**
	 * Returns the default access flags of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default access flags that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getAccessFlags();

     /**
	 * Returns the default launcher id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default launcher id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract int getLauncherId();
    
     /**
	 * Returns the default launcher class name of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default launcher class name that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public abstract String getLauncherClass();
}