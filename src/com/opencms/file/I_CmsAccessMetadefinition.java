package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes the access to metadefinitions in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/10 11:10:23 $
 */
public interface I_CmsAccessMetadefinition {

	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 */
	 I_CmsMetaDefinition readMetaDefinition(String name, I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param type The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	 Vector readAllMetaDefinitions(I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param type The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	 Vector readAllMetaDefinitions(I_CmsResourceType resourcetype, int type);
	
	/**
	 * Writes the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a metadefinition with the same name for this resource-type exists already.
	 */
	 void writeMetaDefinition(String name, I_CmsResourceType resourcetype, 
									int type)
		throws CmsDuplicateKeyException, CmsException;
	
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 void deleteMetaDefinition(String name, I_CmsResourceType type)
		throws CmsException;

}
