/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/I_CmsResourceType.java,v $
 * Date   : $Date: 2005/10/13 12:09:14 $
 * Version: $Revision: 1.25.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;

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
 * Important: The {@link org.opencms.file.CmsObject} passes the {@link org.opencms.db.CmsSecurityManager}
 * object to implementations of this class. Using this object correctly is key to the 
 * resource type operations. Mistakes made in the implementation of a resource type
 * can screw up the system security and the database structure, and make you unhappy. <p> 
 * 
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.25.2.4 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsResourceType extends I_CmsConfigurationParameterHandler {

    /** The name of the addMapping() method. */
    String ADD_MAPPING_METHOD = "addMappingType";

    /** Name of the addResourceType() method to add a resource type from the configuration. */
    String ADD_RESOURCE_TYPE_METHOD = "addResourceType";

    /** Configuration key prefix for properties that are attached when creating a new resource. */
    String CONFIGURATION_PROPERTY_CREATE = "property.create.";

    /** Configuration key for the resource type id. */
    String CONFIGURATION_RESOURCE_TYPE_ID = "resource.type.id";

    /** Configuration key for the resource type name. */
    String CONFIGURATION_RESOURCE_TYPE_NAME = "resource.type.name";

    /** Store the property on resource record. */
    String PROPERTY_ON_RESOURCE = "resource";

    /** Store the property on structure record. */
    String PROPERTY_ON_STRUCTURE = "structure";

    /**
     * Maps a file extension to a resource type.<p>
     * 
     * When uploading files into OpenCms, they must be mapped to the different
     * OpenCms resource types. The configuration, to map which extension to which
     * resouce type is done in the OpenCms VFS configuration.
     * 
     * @param mapping the file extension mapped to the resource type
     */
    void addMappingType(String mapping);

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
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong  
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see CmsSecurityManager#changeLastModifiedProjectId(org.opencms.file.CmsRequestContext, CmsResource)   
     */
    void changeLastModifiedProjectId(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException;

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the name of the resource to change the lock with complete path
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLock(String)
     * @see CmsSecurityManager#changeLock(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void changeLock(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource) throws CmsException;

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     *
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chflags(String, int)
     * @see CmsSecurityManager#chflags(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void chflags(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int flags)
    throws CmsException;

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resources according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image, etc.<p> 
     *
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see CmsSecurityManager#chtype(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type) throws CmsException;

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see CmsSecurityManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, int)
     */
    void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        int siblingMode) throws CmsException, CmsIllegalArgumentException;

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that the resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resource</code> argument is null or of length 0
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see CmsSecurityManager#copyResourceToProject(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void copyResourceToProject(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resourcename the name of the resource to create (full path)
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is null or of length 0
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see CmsSecurityManager#createResource(org.opencms.file.CmsRequestContext, String, int, byte[], List)
     */
    CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException, CmsIllegalArgumentException;

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see CmsSecurityManager#createSibling(org.opencms.file.CmsRequestContext, CmsResource, String, List)
     */
    void createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List properties) throws CmsException;

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.file.CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to delete 
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see CmsSecurityManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void deleteResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int siblingMode)
    throws CmsException;

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
     * Returns the class name configured for this resouce type.<p>
     * 
     * This may be different from the instance class name in case the configured class could not 
     * be instanciated. If the configured class is unavailable, an instance of
     * <code>{@link CmsResourceTypeUnknown}</code> is used. This enables the import of modules that contain their 
     * own resource types classes (which are not available before the module is fully imnported).<p>
     * 
     * @return the class name configured for this resouce type
     */
    String getClassName();

    /**
     * Returns the configured copy resources for this resource type in an unmodifiable List.<p>
     *
     * @return the configured copy resources for this resource type in an unmodifiable List
     */
    List getConfiguredCopyResources();

    /**
     * Returns the configured default properties for this resource type in an unmodifiable List.<p>
     *
     * @return the configured default properties for this resource type in an unmodifiable List
     */
    List getConfiguredDefaultProperties();

    /**
     * Returns the file extensions mappings for this resource type in an unmodifiable List.<p>
     *
     * @return a list of file extensions mappings for this resource type in an unmodifiable List
     */
    List getConfiguredMappings();

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
     * @param securityManager the initialized OpenCms security manager
     * @param resourcename the target name (with full path) for the resource after import
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#moveToLostAndFound(org.opencms.file.CmsRequestContext, String, boolean)
     * @see CmsObject#importResource(String, CmsResource, byte[], List)
     * @see CmsSecurityManager#importResource(org.opencms.file.CmsRequestContext, String, CmsResource, byte[], List, boolean)
     */
    CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List properties) throws CmsException;

    /**
     * Special version of the configuration initialization used with resource types
     * to set resource type, id and class name, required for the XML configuration.<p>
     * 
     * <i>Please note:</i> Many resource types defined in the core have in fact
     * a fixed resource type and a fixed id. Configurable name and id is used only
     * for certain types.<p>
     * 
     * The provided named class must implement this interface (<code>{@link I_CmsResourceType}</code>).
     * Usually the provided class name should be the class name of the resource type instance,
     * but this may be different in special cases or configuration errors.
     * 
     * For example, if a module is imported that contains it's own resource type class files, 
     * the included class file are usually not be available until the server is restarted.
     * If the named class given in the XML configuration (or module manifest.xml) is not available, 
     * or not implementing <code>{@link I_CmsResourceType}</code>, 
     * then <code>{@link CmsResourceTypeUnknown}</code> is used for the resource type instance.<p>
     *
     * @param name the resource type name
     * @param id the resource type id
     * @param className the class name of the resource type (read from the XML configuration)
     * 
     * @throws CmsConfigurationException if the configuration is invalid
     */
    void initConfiguration(String name, String id, String className) throws CmsConfigurationException;

    /**
     * Initializes this resource type.<p>
     * 
     * This method will be called once during the OpenCms 
     * initialization processs. The VFS will already be available 
     * at the time the method is called.<p>
     * 
     * @param cms a OpenCms context initialized with "Admin" permissions
     */
    void initialize(CmsObject cms);

    /**
     * Indicates that this is an additional resource type which is defined in a module.<p>
     * @return true or false
     */
    boolean isAdditionalModuleResourceType();

    /**
     * Returns <code>true</code> if this resource type is direct editable.<p>
     * 
     * @return <code>true</code> if this resource type is direct editable
     */
    boolean isDirectEditable();

    /**
     * Returns <code>true</code> if this resource type is a folder.<p>
     * 
     * @return <code>true</code> if this resource type is a folder
     */
    boolean isFolder();

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#TEMPORARY}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see CmsSecurityManager#lockResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void lockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int mode)
    throws CmsException;

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to move
     * @param destination the destination resource name
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is null or of length 0
     * 
     * 
     * @see CmsObject#moveResource(String, String)
     * @see CmsObject#renameResource(String, String)
     * @see CmsSecurityManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, int)
     * @see CmsSecurityManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the name of the resource to replace
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     *  
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see CmsSecurityManager#replaceResource(org.opencms.file.CmsRequestContext, CmsResource, int, byte[], List)
     */
    void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException;

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see CmsSecurityManager#restoreResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void restoreResourceBackup(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int tag)
    throws CmsException;

    /**
     * Sets the additional resource type flag.<p>
     * @param additionalType true or false
     */
    void setAdditionalModuleResourceType(boolean additionalType);

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to touch
     * @param dateExpired the new expire date of the changed resource
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#setDateExpired(String, long, boolean)
     * @see CmsSecurityManager#setDateExpired(org.opencms.file.CmsRequestContext, CmsResource, long)
     */
    void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive) throws CmsException;

    /**
     * Changes the "last modified" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to touch
     * @param dateLastModified timestamp the new timestamp of the changed resource
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#setDateLastModified(String, long, boolean)
     * @see CmsSecurityManager#setDateLastModified(org.opencms.file.CmsRequestContext, CmsResource, long)
     */
    void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException;

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to touch
     * @param dateReleased the new release date of the changed resource
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#setDateReleased(String, long, boolean)
     * @see CmsSecurityManager#setDateReleased(org.opencms.file.CmsRequestContext, CmsResource, long)
     */
    void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateReleased,
        boolean recursive) throws CmsException;

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * This is also used when doing an "undelete" operation.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to undo the changes for
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see CmsSecurityManager#undoChanges(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void undoChanges(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException;

    /**
     * Unlocks a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to unlock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see CmsSecurityManager#unlockResource(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void unlockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource) throws CmsException;

    /**
     * Writes a resource, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * that have a binary content attached.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to apply this operation to
     *
     * @return the written resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see CmsSecurityManager#writeFile(org.opencms.file.CmsRequestContext, CmsFile)
     */
    CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException;

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see CmsSecurityManager#writePropertyObject(org.opencms.file.CmsRequestContext, CmsResource, CmsProperty)
     */
    void writePropertyObject(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsProperty property) throws CmsException;

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see CmsSecurityManager#writePropertyObjects(org.opencms.file.CmsRequestContext, CmsResource, List)
     */
    void writePropertyObjects(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, List properties)
    throws CmsException;
}