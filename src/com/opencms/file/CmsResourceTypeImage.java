package com.opencms.file;

import com.opencms.core.*;
import java.util.*;

/**
 * This class describes the resource-type image.
 *
 * @author
 * @version 1.0
 */

public class CmsResourceTypeImage extends CmsResourceTypePlain{

    public static final String C_TYPE_RESOURCE_NAME = "image";

	/**
	 * Constructor, creates a new CmsResourceType object.
	 *
	 * @param resourceType The id of the resource type.
	 * @param launcherType The id of the required launcher.
	 * @param resourceTypeName The printable name of the resource type.
	 * @param launcherClass The Java class that should be invoked by the launcher.
	 * This value is <b> null </b> if the default invokation class should be used.
	 * /
	public CmsResourceTypeImage(int resourceType, int launcherType,
						   String resourceTypeName, String launcherClass){

        super(resourceType, launcherType, resourceTypeName, launcherClass);
	}
*/
	public CmsResource createResource(CmsObject cms, String folder, String name, Hashtable properties, byte[] contents) throws CmsException{
		CmsResource res = cms.doCreateFile(folder, name, contents, C_TYPE_RESOURCE_NAME, properties);
		// lock the new file
		cms.lockResource(folder+name);
		return res;
	}
}