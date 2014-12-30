/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;

import java.util.List;

/**
 * Resource type descriptors for all resources in the VFS.<p>
 * 
 * Each file in the VFS must belong to an initialized resource type.
 * The available resource type are read during system startup from the configuration 
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
 * @since 6.0.0 
 */
public interface I_CmsResourceType extends I_CmsConfigurationParameterHandler {

    /** Resource formatter. */
    enum Formatter {

        /** The gallery list item formatter. */
        ADE_LIST("formatter_ade_list", "/system/workplace/editors/ade/default-list-formatter.jsp"),

        /** The gallery list item formatter. */
        GALLERY_LIST("formatter_gallery_list", "/system/workplace/editors/ade/default-list-formatter.jsp"),

        /** The gallery preview formatter. */
        GALLERY_PREVIEW("formatter_gallery_preview", "/system/workplace/editors/ade/default-preview-formatter.jsp"),

        /** The gallery list item formatter. */
        PUBLISH_LIST("formatter_publish_list", "/system/workplace/editors/ade/default-list-formatter.jsp"),

        /** The sitemap formatter. */
        SITEMAP("formatter_sitemap", "/system/workplace/editors/sitemap/default-formatter.jsp");

        /** Formatter default path. */
        private String m_defaultPath;

        /** Formatter name. */
        private String m_name;

        /** 
         * Constructor.<p>
         * 
         * @param name the formatter name 
         * @param defaultPath the default formatter path 
         */
        private Formatter(String name, String defaultPath) {

            m_name = name;
            m_defaultPath = defaultPath;
        }

        /**
         * Get the default path.<p>
         * 
         * @return the default path
         */
        public String getDefaultPath() {

            return m_defaultPath;
        }

        /**
         * Get the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

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
    void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, I_CmsResourceType type)
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
     * 
     * @deprecated 
     * Use {@link #chtype(CmsObject, CmsSecurityManager, CmsResource, I_CmsResourceType)} instead.
     * Resource types should always be referenced either by this type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
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
     * @see CmsObject#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     * @see CmsSecurityManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, CmsResource.CmsResourceCopyMode)
     */
    void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode) throws CmsException, CmsIllegalArgumentException;

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
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException;

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @return the new created sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see CmsSecurityManager#createSibling(org.opencms.file.CmsRequestContext, CmsResource, String, List)
     */
    CmsResource createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List<CmsProperty> properties) throws CmsException;

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to delete 
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, CmsResource.CmsResourceDeleteMode)
     * @see CmsSecurityManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResource.CmsResourceDeleteMode siblingMode) throws CmsException;

    /**
     * Gets the folder for which the links should be adjusted after processing the copy resources.<p>
     * 
     * @return the path of the folder for which the links should be adjusted 
     */
    String getAdjustLinksFolder();

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
     * Returns the class name configured for this resource type.<p>
     * 
     * This may be different from the instance class name in case the configured class could not 
     * be instantiated. If the configured class is unavailable, an instance of
     * <code>{@link CmsResourceTypeUnknown}</code> is used. This enables the import of modules that contain their 
     * own resource types classes (which are not available before the module is fully imported).<p>
     * 
     * @return the class name configured for this resource type
     */
    String getClassName();

    /**
     * Returns the configured copy resources for this resource type in an unmodifiable List.<p>
     *
     * @return the configured copy resources for this resource type in an unmodifiable List
     */
    List<CmsConfigurationCopyResource> getConfiguredCopyResources();

    /**
     * Returns the configured default properties for this resource type in an unmodifiable List.<p>
     *
     * @return the configured default properties for this resource type in an unmodifiable List
     */
    List<CmsProperty> getConfiguredDefaultProperties();

    /**
     * Returns the file extensions mappings for this resource type in an unmodifiable List.<p>
     *
     * @return a list of file extensions mappings for this resource type in an unmodifiable List
     */
    List<String> getConfiguredMappings();

    /**
     * Returns the formatter configuration for the given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to get the formatter configuration for
     * 
     * @return the formatter configuration for the given resource
     */
    CmsFormatterConfiguration getFormattersForResource(CmsObject cms, CmsResource resource);

    /**
     * Returns the gallery preview provider class name.<p>
     * 
     * @return the gallery preview provider class name
     */
    String getGalleryPreviewProvider();

    /**
     * Returns the gallery types for this resource type.<p>
     * 
     * @return the gallery types, if no gallery is configured <code>null</code> will be returned
     */
    List<I_CmsResourceType> getGalleryTypes();

    /**
     * Returns the loader type id of this resource type.<p>
     *
     * @return the loader type id of this resource type
     */
    int getLoaderId();

    /**
     * Returns the module name if this is an additional resource type which is defined in a module, or <code>null</code>.<p>
     * 
     * @return the module name if this is an additional resource type which is defined in a module, or <code>null</code>
     */
    String getModuleName();

    /**
     * Returns the type id of this resource type.<p>
     *
     * @return the type id of this resource type
     * 
     * @deprecated 
     * Use this class or {@link #getTypeName()} instead.
     * Resource types should always be referenced either by this type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
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
     * @see CmsSecurityManager#moveToLostAndFound(org.opencms.file.CmsRequestContext, CmsResource, boolean)
     * @see CmsObject#importResource(String, CmsResource, byte[], List)
     * @see CmsSecurityManager#importResource(org.opencms.file.CmsRequestContext, String, CmsResource, byte[], List, boolean)
     */
    CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties) throws CmsException;

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
     * Tests if the given resource type definition is identical to this resource type definition.<p>
     * 
     * Two resource types are considered identical if their names {@link #getTypeName()} 
     * <b>and</b> their ids {@link #getTypeId()} are both the same.<p>
     * 
     * <b>Please note:</b> Two resource type are considered equal in the sense of {@link Object#equals(Object)} if
     * either if their names {@link #getTypeName()} <b>or</b> their ids {@link #getTypeId()} are equal.<p>
     * 
     * @param type another resource type
     * 
     * @return true, if the specified resource type is identical to this resource type
     */
    boolean isIdentical(I_CmsResourceType type);

    /**
     * Locks a resource.<p>
     *
     * The <code>type</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLockType#EXCLUSIVE}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#TEMPORARY}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to lock
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     * @see CmsSecurityManager#lockResource(org.opencms.file.CmsRequestContext, CmsResource, CmsLockType)
     */
    void lockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, CmsLockType type)
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
     * @see CmsSecurityManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, CmsResource.CmsResourceCopyMode)
     * @see CmsSecurityManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * This is used to reduce the current users project with the
     * specified resource, in case that the resource is already part of the project.
     * The resource is not really removed like in a regular copy operation, 
     * it is in fact only "disabled" in the current users project.<p>   
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
    void removeResourceFromProject(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
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
        I_CmsResourceType type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException;

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
     * 
     * @deprecated
     * Use {@link #replaceResource(CmsObject, CmsSecurityManager, CmsResource, I_CmsResourceType, byte[], List)} instead.
     * Resource types should always be referenced either by this type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
    void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException;

    /**
     * Restores a resource in the current project with a version from the historical archive.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to restore from the archive
     * @param version the version number of the resource to restore
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceVersion(org.opencms.util.CmsUUID, int)
     * @see CmsSecurityManager#restoreResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException;

    /**
     * Sets the additional resource type flag.<p>
     * @param additionalType true or false
     */
    void setAdditionalModuleResourceType(boolean additionalType);

    /**
     * Sets the folder for adjusting links after copying the copy-resources.<p>
     * 
     * @param adjustLinksFolder the folder for which links should be adjusted 
     */
    void setAdjustLinksFolder(String adjustLinksFolder);

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to touch
     * @param dateExpired the new expire date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
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
     * @param recursive if this operation is to be applied recursively to all resources in a folder
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
     * @param recursive if this operation is to be applied recursively to all resources in a folder
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
     * Sets the module name if this is an additional resource type which is defined in a module, or <code>null</code>.<p>
     * 
     * @param moduleName the module name if this is an additional resource type which is defined in a module, or <code>null</code>
     */
    void setModuleName(String moduleName);

    /**
     * Undeletes a resource.<p>
     * 
     * Only resources that have already been published once can be undeleted,
     * if a "new" resource is deleted it can not be undeleted.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to undelete
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undeleteResource(String, boolean)
     * @see CmsSecurityManager#undelete(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void undelete(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException;

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param cms the current cms context
     * @param securityManager the initialized OpenCms security manager
     * @param resource the resource to undo the changes for
     * @param mode the undo mode, one of the <code>{@link org.opencms.file.CmsResource.CmsResourceUndoMode}#UNDO_XXX</code> constants
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsResource#UNDO_CONTENT
     * @see CmsResource#UNDO_CONTENT_RECURSIVE
     * @see CmsResource#UNDO_MOVE_CONTENT
     * @see CmsResource#UNDO_MOVE_CONTENT_RECURSIVE
     * @see CmsObject#undoChanges(String, CmsResource.CmsResourceUndoMode)
     * @see CmsSecurityManager#undoChanges(org.opencms.file.CmsRequestContext, CmsResource, org.opencms.file.CmsResource.CmsResourceUndoMode)
     */
    void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResource.CmsResourceUndoMode mode) throws CmsException;

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
    void writePropertyObjects(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        List<CmsProperty> properties) throws CmsException;
}