package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to metadefinitions in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/17 14:35:31 $
 */
abstract class A_CmsAccessMetadefinition {

	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract A_CmsMetadefinition readMetadefinition(String name, int type)
		throws CmsException;
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	abstract Vector readAllMetadefinitions(int resourcetype)
		throws CmsException;
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	abstract Vector readAllMetadefinitions(int resourcetype, int type)
		throws CmsException;

	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a metadefinition with the same name for this resource-type exists already.
	 */
	abstract A_CmsMetadefinition createMetadefinition(String name, int resourcetype, 
													  int type)
		throws CmsDuplicateKeyException, CmsException;
		
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void deleteMetadefinition(String name, int type)
		throws CmsException;
}
