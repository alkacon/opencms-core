package com.opencms.file;

import java.util.*;
import com.opencms.core.*;

/**
 * This class describes a resource-type. To determine the special launcher 
 * for a resource this resource-type is needed.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/21 14:23:14 $
 */
 class CmsResourceType extends A_CmsResourceType implements I_CmsConstants {	
	
     /**
      * The id of resource type.
      */
 	private int m_resourceType;
    
    /**
     * The id of the launcher used by this resource.
     */
    private int m_launcherType;
    
    /**
     * The resource type name.
     */
    private String m_resourceTypeName;
    
    /**
     * The class name of the Java class launched by the launcher.
     */
    private String m_launcherClass;
     
    
    /**
     * Constructor, creates a new CmsResourceType object.
     * 
     * @param resourceType The id of the resource type.
     * @param launcherType The id of the required launcher.
     * @param resourceTypeName The printable name of the resource type.
     * @param launcherClass The Java class that should be invoked by the launcher. 
     * This value is <b> null </b> if the default invokation class should be used.
     */
    public CmsResourceType(int resourceType, int launcherType,
                           String resourceTypeName, String launcherClass){
        
        m_resourceType=resourceType;
        m_launcherType=launcherType;
        m_resourceTypeName=resourceTypeName;
        m_launcherClass=launcherClass;
    }
    
	/**
	 * Returns the type of this resource-type.
	 * 
	 * @return the type of this resource-type.
	 */
     int getResourceType() {
         return m_resourceType;
     }
    
     /**
	 * Returns the launcher type needed for this resource-type.
	 * 
	 * @return the launcher type for this resource-type.
	 */
     int getLauncherType() {
         return m_launcherType;
     }
	
	/**
	 * Returns the name for this resource-type.
	 * 
	 * @return the name for this resource-type.
	 */
     public String getResourceName() {
         return m_resourceTypeName;
     }
    
     /**
	 * Returns the name of the Java class loaded by the launcher.
	 * This method returns <b>null</b> if the default class for this type is used.
	 * 
	 * @return the name of the Java class.
	 */
     public String getLauncherClass() {
         return m_launcherClass;
     }

}
