package com.opencms.file;

import com.opencms.core.*;

/**
 * This class describes a mountpoint. A mountpoint defines the type of a filesystem
 * or database containing data used my the Cms and where it is mounted in to the logical
 * filesystem of  the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/01/04 13:46:02 $
 */

public class CmsMountPoint extends A_CmsMountPoint implements I_CmsConstants {
    /**
     * Definition of the location of this mountpoint in the logical Cms fielsystem.
     */
	private String m_mountpoint=null;
    
     /**
     * The type of this mountpoint
     */

    private int m_mountpointType=C_UNKNOWN_ID;
    
    /**
     * The the mountpath of a mountpoint in a real filesystem.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
	private String m_mountpath=null;
    
    /** 
    * The connect string to the database used by this mountpoint.
    * Only used if for mountpoints of the type C_MOUTNTPOINT_MYSQL.
    */
    private String m_connect=null;
    
    /** 
    * The JDBC driver for the database used by this mountpoint.
    * Only used if for mountpoints of the type C_MOUTNTPOINT_MYSQL.
    */
    private String m_driver=null;
    
    /**
     * The individual name of this mountpoint.
     */
    private String m_system=null;
    
    /**
     * The default resource type for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
	private int m_resourceType=C_UNKNOWN_ID;
    
     /**
     * The default user for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_user=C_UNKNOWN_ID;
    
     /**
     * The default group for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_group=C_UNKNOWN_ID;
    
     /**
     * The default project for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_project=C_UNKNOWN_ID;

     /**
     * The default resource flags for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_flags=C_UNKNOWN_ID;
   
     /**
     * The default access flags for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_accessFlags=C_UNKNOWN_ID;

     /**
     * The default launcher id for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private int m_launcherId=C_UNKNOWN_ID;

     /**
     * The default launcher class name for all resources of this mountpoint.
     * Only used if for mountpoints of the type C_MOUTNTPOINT_FILESYSTEM.
     */
    private String m_launcherClass=null;
    

    
	 /**
	 * Constructs a new CmsMountPoint. 
	 * This constructor creates a new mountpoint for a  disc filesystem
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param mountpath The physical location this mount point directs to. 
	 * @param name The name of this mountpoint.
	 * @param user The default user for this mountpoint.
	 * @param group The default group for this mountpoint.
	 * @param project The default project for this mountpoint.
	 * @param type The default resourcetype for this mountpoint.
	 */
	public CmsMountPoint(String mountpoint, String mountpath, String name,
                         A_CmsUser user, A_CmsGroup group, A_CmsProject project,
                         int type, int flags, int accessFlags,
                         int launcherId, String launcherClass) {
		m_mountpoint = mountpoint;
		m_mountpath = mountpath;
		m_system=name;
		m_resourceType=type;
        m_user=user.getId();
        m_group=group.getId();
        m_project=project.getId();
        m_mountpointType=C_MOUNTPOINT_FILESYSTEM;
        m_flags=flags;
        m_accessFlags=accessFlags;
        m_launcherId=launcherId;
        m_launcherClass=launcherClass;
	}
    
     /**
	 * Constructs a new CmsMountPoint. 
	 * This constructor creates a new mountpoint for a  disc filesystem
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param mountpath The physical location this mount point directs to. 
	 * @param name The name of this mountpoint.
	 * @param user The default user for this mountpoint.
	 * @param group The default group for this mountpoint.
	 * @param project The default project for this mountpoint.
	 * @param type The default resourcetype for this mountpoint.
	 */
	public CmsMountPoint(String mountpoint, String driver, String connect,
                         String name){
        
		m_mountpoint = mountpoint;
		m_driver = driver;
        m_connect = connect;
		m_system=name;
	    m_mountpointType=C_MOUNTPOINT_MYSQL;
	}
	
	/**
	 * Returns the Mountpoint of a CmsMountPoint Object.
	 * @return The mountpoint in the OpenCms filesystem.
	 */
	public String getMountpoint() {
	  return m_mountpoint;
	}
	
	/**
	 * Returns the mountpath of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The physical location this mountpoint directs to or null.
	 */
	public String getMountPath() {
		return m_mountpath;
	}
    
    	
	/**
	 * Returns the database driver of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_MYSQL.
	 * @return The database driver of this mountpoint or null.
	 */
	public String getDriver() {
		return m_driver;
	}
	
     /**
	 * Returns the database connect string of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_MYSQL.
	 * 
	 * @return The database connect string of this mountpoint or null.
	 */
	public String getConnect() {
		return m_connect;
	}
	
	 /**
	 * Returns the name of a CmsMountPoint Object.
	 * @return The name of this mount point.
	 */
	public String getName()	{
	  return m_system;
	}
	
	 /**
	 * Returns the default filetype of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default file type that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getType(){
	  return m_resourceType;
	}
	
     /**
	 * Returns the default user id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default user id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
    public int getUser() {
	  return m_user;
	}
 
     /**
	 * Returns the default group id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default user id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getGroup() {
	  return m_group;
	}
    
     /**
	 * Returns the default project id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default project id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getProject() {
	  return m_project;
	}
    
        
     /**
	 * Returns the default flags of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default flags that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getFlags() {
	  return m_flags;
	}
   
     /**
	 * Returns the default access flags of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default access flags that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getAccessFlags() {
	  return m_accessFlags;
	}

     /**
	 * Returns the default launcher id of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default launcher id that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public int getLauncherId() {
	  return m_launcherId;
	}
    
     /**
	 * Returns the default launcher class name of a CmsMountPoint Object.
	 * A value is only returned for mountpoints of thetype C_MOUNTPOUINT_FILESYSTEM.
	 * 
	 * @return The default launcher class name that is returned for all files at this mount point
	 * or C_UNKNOWN_ID.
	 */
	public String getLauncherClass() {
	  return m_launcherClass;
	}
    
     /**
	 * Returns the type of this mountpoint.
	 * 
	 * @return The type of the mountpoint.
	 */
    public int getMountpointType() {
        return m_mountpointType;
    }
    
    
    /**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[MountPoint]:");
        output.append(m_mountpoint);
        output.append(" , Mountpath=");
        output.append(m_mountpath);
        output.append(" , Driver=");
        output.append(m_driver);
        output.append(" , Connect=");
        output.append(m_connect);
        return output.toString();
	}
	
}