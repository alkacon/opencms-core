package com.opencms.file;

import java.io.*;

import com.opencms.core.*;

/**
 * This interface describes the access to propertys in the Cms.<BR/>
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/21 15:08:47 $
 */
interface I_CmsAccessProperty {

     /**
	 * Creates a serializable object in the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable addProperty(String name, Serializable object)
		throws CmsException;
    
	/**
	 * Reads a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable readProperty(String name)
		throws CmsException;

	/**
	 * Writes or updates a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable writeProperty(String name, Serializable object)
		throws  CmsException;

	/**
	 * Deletes a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteProperty(String name)
		throws CmsException;
}
