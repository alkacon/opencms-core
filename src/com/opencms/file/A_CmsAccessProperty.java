package com.opencms.file;

import java.io.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to propertys in the Cms.<BR/>
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/16 18:13:09 $
 */
abstract class A_CmsAccessProperty {
	
	/**
	 * Reads a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract Serializable readProperty(String name)
		throws CmsException;

	/**
	 * Writes or updates a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void writeProperty(String name, Serializable object)
		throws  CmsException;

	/**
	 * Deletes a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void deleteProperty(String name)
		throws CmsException;
}
