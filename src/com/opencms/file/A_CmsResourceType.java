package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a resource-type. To determine the special launcher 
 * for a resource this resource-type is needed.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
abstract class A_CmsResourceType {	
	
	/**
	 * Returns all ResourceTypes in a hashtable. The ResourceType-name is the
	 * key into this hashtable.
	 * 
	 * @return a hashtable with all possible ResourceTypes.
	 */
	abstract public Hashtable getResourceTypes();

	/**
	 * Returns the id for this resource-type.
	 * 
	 * @return the id for this resource-type.
	 */
	abstract long getId();
	
	/**
	 * Returns the name for this resource-type.
	 * 
	 * @return the name for this resource-type.
	 */
	abstract public String getName();
	
	/**
	 * Returns the launcher for this resource-type.<BR/>
	 * The launcher will start the resource in its needed environment.
	 * Possibly the resource will be processed bevore delivering.
	 * 
	 * @return the launcher for this resource-type.
	 */
	abstract public Class getLauncher();
}
