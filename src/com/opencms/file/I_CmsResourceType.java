package com.opencms.file;

import java.util.*;

/**
 * This interface describes a resource-type. To determine the special launcher 
 * for a resource this resource-type is needed.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 1999/12/06 09:39:22 $
 */
public interface I_CmsResourceType {	

	/**
	 * The resource type-id for a folder.
	 */
	public final static int C_FOLDER		= 0;
	
	/**
	 * The resource type-id for a plain text file.
	 */
	public final static int C_TEXTPLAIN		= 1;
	
	/**
	 * The resource type-id for a binary file.
	 */
	public final static int C_BINARY		= 2;
	
	/**
	 * The resource type-id for a xml base file.
	 */
	public final static int C_XMLBASE		= 3;

	/**
	 * The resource type-id for a xml templatefile.
	 */
	public final static int C_XMLTEMPLATE	= 4;
	
	/**
	 * The resource type-id for a docloader file.
	 */
	public final static int C_DOCLOADER	= 5;
	
	/**
	 * Returns all ResourceTypes in a hashtable. The ResourceType-name is the
	 * key into this hashtable.
	 * 
	 * @return a hashtable with all possible ResourceTypes.
	 */
	public Hashtable getResourceTypes();

	/**
	 * Returns the id for this resource-type.
	 * 
	 * @return the id for this resource-type.
	 */
	long getId();
	
	/**
	 * Returns the name for this resource-type.
	 * 
	 * @return the name for this resource-type.
	 */
	public String getName();
	
	/**
	 * Returns the launcher for this resource-type.<BR>
	 * The launcher will start the resource in its needed environment.
	 * Possibly the resource will be processed bevore delivering.
	 * 
	 * @return the launcher for this resource-type.
	 */
	public Class getLauncher();
}
