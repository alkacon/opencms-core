/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsResourceType.java,v $
 * Date   : $Date: 2003/08/14 12:50:53 $
 * Version: $Revision: 1.41 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package com.opencms.file;

import com.opencms.core.CmsException;
import com.opencms.flex.util.CmsUUID;

import java.util.Map;

/**
 * Defines of all methods that a specific resource type has to implement.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public interface I_CmsResourceType {
    
     /**
      * Returns the loader type id needed for this resource type.<p>
      *
      * @return the loader type id for this resource type
      */
     int getLoaderId();

    /**
     * Returns the name for this resource type.<p>
     *
     * @return the name for this resource type
     */
     String getResourceTypeName();

    /**
     * Returns the type id of this resource type.<p>
     *
     * @return the type id of this resource type
     */
    int getResourceType();

    /**
     * Creates a new resource of this resource type.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the file to create
     * @param contents the contents of the new file
     * @param properties the properties of the new file
     * @param parameter additional parameters for the create operation
     * @return file a CmsResource representing the newly created file
     * @throws CmsException if something goes wrong
     */
    CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException;
        
    /**
     * Locks a resource.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param force if true, locking is forced even if resource is already locked by someone else
     * @throws CmsException if something goes wrong
     */
    void lockResource(CmsObject cms, String resourcename, boolean force) throws CmsException;

    /**
     * Unlocks a resource.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * @throws CmsException if something goes wrong
     */
    void unlockResource(CmsObject cms, String resourcename, boolean recursive) throws CmsException;
    
    /**
     * Copies a Resource.
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param destination the complete path of the destinationfolder
     * @param keeppermissions <code>true</code> if the copy should keep the source file's permissions,
     *        <code>false</code> if the copy should get the user's default flags
     * @param copyMode mode of the copy operation, described how to handle linked resourced during copy.
     * Possible values are: 
     * <ul>
     * <li>C_COPY_AS_NEW</li>
     * <li>C_COPY_AS_LINK</li>
     * <li>C_COPY_PRESERVE_LINK</li>
     * </ul>
     * @throws CmsException if something goes wrong
     */
    void copyResource(CmsObject cms, String resourcename, String destination, boolean keeppermissions, boolean lockCopy, int copyMode) throws CmsException;

    /**
     * Moves a file to the given destination.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param destination the destination resource name
     * @throws CmsException if something goes wrong
     */
    void moveResource(CmsObject cms, String resourcename, String destination) throws CmsException;

    /**
     * Moves a resource to the lost and found folder
     *
     * @param cms the current cms object
     * @param resourcename the complete path of the sourcefile
     * @param copyResource true, if the resource should be copied to its destination inside the lost+found folder
     * @return string with the name of moved resource inside the lost & found folder
     * @throws  CmsException if something goes wrong
     */
    String copyToLostAndFound(CmsObject cms, String resourcename, boolean copyResource) throws CmsException;

    /**
     * Renames a file to a new name.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param destination the destination resource name
     * @throws CmsException if something goes wrong
     */
    void renameResource(CmsObject cms, String resourcename, String destination) throws CmsException;

    /**
     * Deletes a resource.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong
     */
    void deleteResource(CmsObject cms, String resourcename, int deleteOption) throws CmsException;

    /**
     * Undeletes a resource.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong
     */
    void undeleteResource(CmsObject cms, String resourcename) throws CmsException;
            
    /**
     * Change the timestamp of a resource.<p>
     * 
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param timestamp the new timestamp of the changed resource
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * @param user the user who is inserted as userladtmodified 
     * @throws CmsException if something goes wrong
     */
    void touch(CmsObject cms, String resourcename, long timestamp, boolean recursive, CmsUUID user) throws CmsException;

    /**
     * Changes the resource type of a resource.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param newtype the name of the new resource type for this resource
     * @throws CmsException if something goes wrong
     */
    void chtype(CmsObject cms, String resourcename, int newtype) throws CmsException;
    
    /**
     * Replaces the content and properties of a resource.<p>
     * 
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @param properties the new properties of the resource
     * @param content the new content of the resource
     * @param type the new type of the resource
     * @throws CmsException if something goes wrong
     */
    // TODO: Allow null valued for properties / content / type
    void replaceResource(CmsObject cms, String resourcename, Map properties, byte[] content, int type) throws CmsException;

    /**
     * Undo all changes in the resource, restoring the online version.<p>
     *
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong
     */
    void undoChanges(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Restores a file in the current project with a version from the backup.<p>
     *
     * @param cms the current cms context
     * @param version the version id to resource form the backup
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong
     */
    void restoreResource(CmsObject cms, int version, String resourcename) throws CmsException;

    /**
     * Copies a resource to the currently selected project.<p>
     * 
     * @param cms the current cms context
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong
     */
    void copyResourceToProject(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Changes the project id of the resource to the new project.<p>
     *
     * @param cms the current cms context
     * @param project the project id to change the status of the resource to
     * @param resourcename the name of the resource to apply this operation to
     * @throws CmsException if something goes wrong     
     */
    void changeLockedInProject(CmsObject cms, int project, String resourcename) throws CmsException;

    /**
     * Does the Linkmanagement when a resource is exported.<p>
     * 
     * When a resource is exported, the ID´s inside the
     * link tags have to be changed to the corresponding URL´s.<p>
     *
     * @param cms the current cms context
     * @param file the file to apply this operation to
     * @throws CmsException if something goes wrong
     * @return CmsFile
     */
    // TODO: Check where this method is called
    CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException;

    /**
     * Imports a resource to the cms.<p>
     *
     * @param the current cms object
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * @param destination the name of the resource destinaition
     * @return the imported CmsResource
     * @throws CmsException if operation was not successful
     */
    CmsResource importResource(CmsObject cms, CmsResource resource, byte[] content, Map properties, String destinition) throws CmsException;
}
