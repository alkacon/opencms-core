/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/I_CmsResourceType.java,v $
 * Date   : $Date: 2004/06/21 09:55:50 $
 * Version: $Revision: 1.1 $
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

package org.opencms.file.types;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Defines resource type descriptors for all resources in the VFS.<p>
 * 
 * Each file in the VFS must belong to an initialized resource type.
 * The available resource type are read during system startup ftom the configuration 
 * file <code>opencms-vfs.xml</code>.<p>
 * 
 * Certain resource types may require special handling for certain operations.
 * This is usually required for write operations, or other operations that 
 * modify the VFS database.
 * Therefore, the {@link org.opencms.file.CmsObject} defers handling of this 
 * operations to implementations of this interface.<p>
 * 
 * If you implement a new resource type, it's a good idea to extend the  
 * abstract class {@link org.opencms.file.types.A_CmsResourceType}.<p>
 * 
 * Important: The {@link org.opencms.file.CmsObject} passes the {@link org.opencms.db.CmsDriverManager}
 * object to implementations of this class. Using this object correctly is key to the 
 * resource type operations. Mistakes made in the implementation of a resource type
 * can screw up the system security and the database structure, and make you unhappy. 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public interface I_CmsResourceType extends I_CmsConfigurationParameterHandler {

    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * This information is used while publishing. Only resources inside the 
     * project folders that are new/modified/changed <i>and</i> that "belong" 
     * to the project (i.e. have the id of the project set) are published
     * with the project.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to (full path)
     * 
     * @throws CmsException if something goes wrong  
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see CmsDriverManager#changeLastModifiedProjectId(org.opencms.file.CmsRequestContext, String)   
     */
    void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException;

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resource according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image etc.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to (full path)
     * @param newType the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see CmsDriverManager#chtype(org.opencms.file.CmsRequestContext, String, int)
     */
    void chtype(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int newType
    ) throws CmsException;

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param source the name of the resource to copy with complete path
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see CmsDriverManager#copyResource(org.opencms.file.CmsRequestContext, String, String, int)
     */
    void copyResource(
        CmsObject cms,
        CmsDriverManager driverManager,
        String source,
        String destination,
        int siblingMode
    ) throws CmsException;

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see CmsDriverManager#copyResourceToProject(org.opencms.file.CmsRequestContext, String)
     */
    void copyResourceToProject(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException;

    /**
     * Creates a new resource of this resource type
     * with the provided content and properties.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to create (full path)
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see CmsDriverManager#createResource(org.opencms.file.CmsRequestContext, String, int, byte[], List)
     */
    CmsResource createResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        byte[] content, 
        List properties
    ) throws CmsException;

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_IGNORE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see CmsDriverManager#deleteResource(org.opencms.file.CmsRequestContext, String, int)
     */
    void deleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int siblingMode
    ) throws CmsException;

    /**
     * Returns the default for the <code>cache</code> property setting of this resource type.<p>
     * 
     * The <code>cache</code> property is used by the Flex cache implementation 
     * to build the cache key that controls the caching behaviour of a resource.<p>
     * 
     * If <code>null</code> is returnd, this is the same as turning the cache 
     * off by default for this resource type.<p>
     * 
     * @return the default for the <code>cache</code> property setting of this resource type
     * 
     * @see org.opencms.flex.CmsFlexCache
     * @see org.opencms.flex.CmsFlexCacheKey
     */
    String getCachePropertyDefault();

    /**
     * Returns the loader type id of this resource type.<p>
     *
     * @return the loader type id of this resource type
     */
    int getLoaderId();

    /**
     * Returns the type id of this resource type.<p>
     *
     * @return the type id of this resource type
     */
    int getTypeId();

    /**
     * Returns the name of this resource type.<p>
     *
     * @return the name of this resource type
     */
    String getTypeName();

    /**
     * Imports a resource to the OpenCms VFS.<p>
     * 
     * If a resource already exists in the VFS (i.e. has the same name and 
     * same id) it is replaced by the imported resource.<p>
     * 
     * If a resource with the same name but a different id exists, 
     * the imported resource is (usually) moved to the "lost and found" folder.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the target name (with full path) for the resource after import
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveToLostAndFound(CmsObject, CmsDriverManager, String, boolean)
     * @see CmsObject#importResource(String, CmsResource, byte[], List)
     * @see CmsDriverManager#importResource(org.opencms.file.CmsRequestContext, String, CmsResource, byte[], List)
     * @see CmsDriverManager#importResourceUpdate(org.opencms.file.CmsRequestContext, String, byte[], List)
     */
    CmsResource importResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        CmsResource resource, 
        byte[] content, 
        List properties
    ) throws CmsException;

    /**
     * Returns <code>true</code> if a resource type is direct editable.<p>
     * 
     * @return <code>true</code> if a resource type is direct editable
     */
    boolean isDirectEditable();

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_TEMP}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name (with full path) of the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see CmsDriverManager#lockResource(org.opencms.file.CmsRequestContext, String, int)
     */
    void lockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int mode
    ) throws CmsException;

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folder in the offline
     * project, and are unable to undelete them.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param source the name of the resource to apply this operation to
     * @param destination the destination resource name
     *
     * @throws CmsException if something goes wrong
     * @see CmsObject#moveResource(String, String)
     * @see CmsObject#renameResource(String, String)
     * @see CmsDriverManager#copyResource(org.opencms.file.CmsRequestContext, String, String, int)
     * @see CmsDriverManager#deleteResource(org.opencms.file.CmsRequestContext, String, int)
     */
    void moveResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String source, 
        String destination
    ) throws CmsException;

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The "lost and found" folder is a special system folder. 
     * This operation is used e.g. during import of resources
     * when a resource with the same name but a different resource ID
     * already exists in the VFS. In this case the imported resource is 
     * moved to the "lost and found" folder.<p>
     * 
     * The method can also be used to get the potential name of a resource
     * in the "lost and found" folder without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param returnNameOnly if <code>true</code>, only the name of the resource in the "lost and found" 
     *        folder is returned, the move operation is not really performed
     *
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     * @see CmsObject#getLostAndFoundName(String)
     * @see CmsDriverManager#moveToLostAndFound(org.opencms.file.CmsRequestContext, String, boolean)
     */
    String moveToLostAndFound(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean returnNameOnly
    ) throws CmsException;

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     *  
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see CmsDriverManager#replaceResource(org.opencms.file.CmsRequestContext, String, int, byte[], List)
     */
    void replaceResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int type, 
        byte[] content, 
        List properties
    ) throws CmsException;

    /**
     * Restores a file in the current project with a version from the backup.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param tag the tag id to resource form the backup
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResource(String, int)
     * @see CmsDriverManager#restoreResource(org.opencms.file.CmsRequestContext, String, int)
     */
    void restoreResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int tag
    ) throws CmsException;

    /**
     * Change the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expires" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param dateLastModified the new last modified date of the resource
     * @param dateReleased the new release date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param dateExpired the new expire date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param user the user who is inserted as userLastModified 
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#touch(String, long, long, long, CmsUUID, boolean)
     * @see CmsDriverManager#touch(org.opencms.file.CmsRequestContext, String, long, long, long, CmsUUID)
     */
    void touch(
        CmsObject cms,
        CmsDriverManager driverManager,
        String resourcename,
        long dateLastModified,
        long dateReleased,
        long dateExpired,
        CmsUUID user, 
        boolean recursive
  ) throws CmsException;

    /**
     * Undeletes a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undeleteResource(String)
     * @see CmsDriverManager#undeleteResource(org.opencms.file.CmsRequestContext, String)
     */
    void undeleteResource(
        CmsObject cms,
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException;

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see CmsDriverManager#undoChanges(org.opencms.file.CmsRequestContext, String)
     */
    void undoChanges(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean recursive        
    ) throws CmsException;

    /**
     * Unlocks a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to apply this operation to
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String, boolean)
     * @see CmsDriverManager#unlockResource(org.opencms.file.CmsRequestContext, String)
     */
    void unlockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean recursive
    ) throws CmsException;

    /**
     * Writes a resource, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * have a binary content attached.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to apply this operation to
     *
     * @return the written resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see CmsDriverManager#writeFile(org.opencms.file.CmsRequestContext, CmsFile)
     */
    CmsFile writeFile(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsFile resource
    ) throws CmsException;
    
//    /**
//     * Changes the lock of a resource.<p>
//     * 
//     * @param resourcename name of the resource
//     * @throws CmsException if something goes wrong
//     */
//    public void changeLock(String resourcename) throws CmsException {
//        m_driverManager.changeLock(m_context, addSiteRoot(resourcename));
//    }
//    
//    /**
//     * Creates a new sibling of the target resource.<p>
//     * 
//     * @param siblingName name of the new link
//     * @param targetName name of the target
//     * @param siblingProperties additional properties of the link resource
//     * @return the new link resource
//     * @throws CmsException if something goes wrong
//     */
//    public CmsResource createSibling(String siblingName, String targetName, List siblingProperties) throws CmsException {
//        return m_driverManager.createSibling(m_context, addSiteRoot(siblingName), addSiteRoot(targetName), siblingProperties, true);
//    }
//    
//    /**
//     * Deletes all property values of a file or folder.<p>
//     * 
//     * You may specify which whether just structure or resource property values should
//     * be deleted, or both of them.<p>
//     *
//     * @param resourcename the name of the resource for which all properties should be deleted.
//     * @param deleteOption determines which property values should be deleted
//     * @throws CmsException if operation was not successful
//     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES
//     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_VALUES
//     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_RESOURCE_VALUES
//     */
//    public void deleteAllProperties(String resourcename, int deleteOption) throws CmsException {
//        m_driverManager.deleteAllProperties(m_context, addSiteRoot(resourcename), deleteOption);
//    }
//    
//    /**
//     * Deletes a property for a file or folder.<p>
//     *
//     * @param resourcename the name of a resource for which the property should be deleted
//     * @param key the name of the property
//     * @throws CmsException if something goes wrong
//     * @deprecated use {@link #writePropertyObject(String, CmsProperty)} instead
//     */
//    public void deleteProperty(String resourcename, String key) throws CmsException {
//        CmsProperty property = new CmsProperty();
//        property.setKey(key);
//        property.setStructureValue(CmsProperty.C_DELETE_VALUE);
//        
//        m_driverManager.writePropertyObject(m_context, addSiteRoot(resourcename), property);        
//    }
//    
//    /**
//     * Recovers a resource from the online project back to the 
//     * offline project as an unchanged resource.<p>
//     * 
//     * @param resourcename the name of the resource which is recovered
//     * @return the recovered resource in the offline project
//     * @throws CmsException if somethong goes wrong
//     */
//    public CmsResource recoverResource(String resourcename) throws CmsException {
//        return m_driverManager.recoverResource(m_context, m_context.addSiteRoot(resourcename));        
//    }
//    
//    /**
//     * Writes a file-header to the Cms.
//     *
//     * @param file the file to write.
//     *
//     * @throws CmsException if resourcetype is set to folder. The CmsException will also be thrown,
//     * if the user has not the rights to write the file header..
//     */
//    public void writeFileHeader(CmsFile file) throws CmsException {
//        m_driverManager.writeFileHeader(m_context, file);
//    }
//    
//    /**
//     * Writes a property object to the database mapped to a specified resource.<p>
//     * 
//     * @param resourceName the name of resource where the property is mapped to
//     * @param property a CmsProperty object containing a structure and/or resource value
//     * @throws CmsException if something goes wrong
//     */    
//    public void writePropertyObject(String resourceName, CmsProperty property) throws CmsException {
//        m_driverManager.writePropertyObject(m_context, addSiteRoot(resourceName), property);
//    }
//    
//    /**
//     * Writes a list of property objects to the database mapped to a specified resource.<p>
//     * 
//     * Code calling this method has to ensure that the properties in the specified list are
//     * disjunctive.<p>
//     * 
//     * @param resourceName the name of resource where the property is mapped to
//     * @param properties a list of CmsPropertys object containing a structure and/or resource value
//     * @throws CmsException if something goes wrong
//     */    
//    public void writePropertyObjects(String resourceName, List properties) throws CmsException {
//        m_driverManager.writePropertyObjects(m_context, addSiteRoot(resourceName), properties);
//    }  
}