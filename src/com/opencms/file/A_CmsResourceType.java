package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a resource-type. To determine the special launcher 
 * for a resource this resource-type is needed.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 2000/01/05 17:03:09 $
 */
abstract public class A_CmsResourceType {	
	
	/**
	 * Returns the type of this resource-type.
	 * 
	 * @return the type of this resource-type.
	 */
	abstract int getResourceType();
    
     /**
	 * Returns the launcher type needed for this resource-type.
	 * 
	 * @return the launcher type for this resource-type.
	 */
	abstract int getLauncherType();
	
	/**
	 * Returns the name for this resource-type.
	 * 
	 * @return the name for this resource-type.
	 */
	abstract public String getResourceName();
    
     /**
	 * Returns the name of the Java class loaded by the launcher.
	 * This method returns <b>null</b> if the default class for this type is used.
	 * 
	 * @return the name of the Java class.
	 */
	abstract public String getLauncherClass();

}
