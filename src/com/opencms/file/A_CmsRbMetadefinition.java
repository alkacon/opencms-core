package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes a resource broker for metadefinitions in 
 * the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: I_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
abstract class A_CmsRbMetadefinition {
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * <B>Security:</B>
	 * All users are granted
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 */
	abstract I_CmsMetaDefinition readMetaDefinition(I_CmsUser callingUser, String name, I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * <B>Security:</B>
	 * All users are granted
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param type The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	abstract Vector readAllMetaDefinitions(I_CmsUser callingUser, I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * <B>Security:</B>
	 * All users are granted
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param type The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (I_CmsUser callingUser, normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	abstract Vector readAllMetaDefinitions(I_CmsUser callingUser, I_CmsResourceType resourcetype, int type);
	
	/**
	 * Writes the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * <B>Security:</B>
	 * Only users in the admin group are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (I_CmsUser callingUser, normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a metadefinition with the same name for this resource-type exists already.
	 */
	abstract void writeMetaDefinition(I_CmsUser callingUser, String name, I_CmsResourceType resourcetype, 
									int type)
		throws CmsDuplicateKeyException, CmsException;
	
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * <B>Security:</B>
	 * Only users in the admin group are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void deleteMetaDefinition(I_CmsUser callingUser, String name, I_CmsResourceType type)
		throws CmsException;

}