/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.List;

/**
 * Interface which is used by the {@link CmsObjectWrapper} to create a different view to the
 * resources in the VFS.<p>
 *
 * It is possible to create "new" virtual resource in the view to the clients using the
 * <code>CmsObjectWrapper</code> or to change the existing ones. For example adding the correct
 * file extension for resources, because it is not always given that resources of type jsp have
 * the extension ".jsp". A resource wrapper just could add this extension, so that clients can
 * handle that resource correctly.<p>
 *
 * Each method in the implementing classes first have to check in every method if it is
 * responsible for the action to execute, because the <code>CmsObjectWrapper</code> iterates
 * through all configured resource wrappers and the first which feels responsible wins and the
 * others won't even called.<p>
 *
 * @since 6.2.4
 */
public interface I_CmsResourceWrapper {

    /**
     * Here it is possible to add additional (virtual) child resources to those already existing
     * in the VFS.<p>
     *
     * @see CmsObjectWrapper#getResourcesInFolder(String, CmsResourceFilter)
     *
     * @param cms the current users OpenCms context
     * @param resourcename the full path of the resource where to add the child resources for
     * @param filter the resource filter to use
     *
     * @return a list of all additionaly child <code>{@link CmsResource}</code>s
     *
     * @throws CmsException if something goes wrong
     */
    List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException;

    /**
     * If there is a configuration string for the wrapper, this method will be called with the configuration string
     * before it is used.<p>
     *
     *  Otherwise, it will not be called.
     *
     * @param configuration the configuration string for the wrapper
     */
    void configure(String configuration);

    /**
     * Copies a resource.<p>
     *
     * First should be a check if the source and/or the destination are handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the source or in the destination are virtual paths and
     * so has to be translated into valid paths existing in the VFS to copy the resource.<p>
     *
     * @see CmsObjectWrapper#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     * @see CmsObject#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     *
     * @param cms the initialized CmsObject
     * @param source the name of the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     *
     * @return true if the copy action was handled by this resource wrapper otherwise false
     *
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * @throws CmsException if something goes wrong
     */
    boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to create the resource.<p>
     *
     * @see CmsObjectWrapper#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int, byte[], List)
     *
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     *
     * @return the created resource or null if not handled by this resource wrapper
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is null or of length 0
     */
    CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException;

    /**
     * Deletes a resource given its name.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to delete the resource.<p>
     *
     * @see CmsObjectWrapper#deleteResource(String, CmsResource.CmsResourceDeleteMode)
     * @see CmsObject#deleteResource(String, CmsResource.CmsResourceDeleteMode)
     *
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource to delete
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @return true if the delete action was handled by this resource wrapper otherwise false
     *
     * @throws CmsException if something goes wrong
     */
    boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode) throws CmsException;

    /**
     * Returns the lock for the resource.<p>
     *
     * First should be a check if the resource is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resource is a virtual path and
     * so has to be translated into a valid path existing in the VFS to
     * get the lock for the resource.<p>
     *
     * @see CmsObjectWrapper#getLock(CmsResource)
     * @see CmsObject#getLock(CmsResource)
     *
     * @param cms the initialized CmsObject
     * @param resource the resource to check the lock for
     *
     * @return the lock state of the resource or null if the action couldn't be handled by this resource wrapper
     *
     * @throws CmsException if something goes wrong
     */
    CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Is called to check if the given resource is handled by this wrapper.<p>
     *
     * @see CmsObjectWrapper#getResourcesInFolder(String, CmsResourceFilter)
     *
     * @param cms the initialized CmsObject
     * @param res the resource to check
     *
     * @return true if the resource will be handled by the wrapper otherwise false
     */
    boolean isWrappedResource(CmsObject cms, CmsResource res);

    /**
     * Locks a resource.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to lock the resource.<p>
     *
     * @see CmsObjectWrapper#lockResource(String)
     * @see CmsObject#lockResource(String)
     *
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to lock (full path)
     * @param temporary true if the resource should only be locked temporarily
     *
     * @return true if this request could be handled by this wrapper or false if not
     *
     * @throws CmsException if something goes wrong
     */
    boolean lockResource(CmsObject cms, String resourcename, boolean temporary) throws CmsException;

    /**
     * Moves a resource to the given destination.<p>
     *
     * First should be a check if the source and/or the destination are handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the source or in the destination are virtual paths and
     * so has to be translated into valid paths existing in the VFS to move the resource.<p>
     *
     * @see CmsObjectWrapper#moveResource(String, String)
     * @see CmsObject#moveResource(String, String)
     *
     * @param cms the current cms context
     * @param source the name of the resource to move
     * @param destination the destination resource name
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is null or of length 0
     *
     * @return true if the move action was handled by this resource wrapper otherwise false
     */
    boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to read the resource.<p>
     *
     * @see CmsObjectWrapper#readFile(String, CmsResourceFilter)
     * @see CmsObject#readFile(String, CmsResourceFilter)
     *
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the file resource that was read or null if it could not be handled by this resource wrapper
     *
     * @throws CmsException if the file resource could not be read for any reason
     */
    CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException;

    /**
     * Reads a resource from the VFS, using the specified resource filter.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to read the resource.<p>
     *
     * @see CmsObjectWrapper#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String, CmsResourceFilter)
     *
     * @param cms the current users OpenCms context
     * @param resourcename The name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read or null if it could not be handled by this resource wrapper
     *
     * @throws CmsException if the resource could not be read for any reason
     */
    CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException;

    /**
     * Returns the link to a existing resource in the VFS for the uri.<p>
     *
     * Turns the virtual path into a real path to a resource in the VFS.<p>
     *
     * @see #rewriteLink(CmsObject, CmsResource)
     * @see CmsObjectWrapper#restoreLink(String)
     *
     * @param cms the initialized CmsObject
     * @param uri the (virtual) uri to be restored
     *
     * @return the uri where to find the resource in the VFS or null if the wrapper is not responsible
     */
    String restoreLink(CmsObject cms, String uri);

    /**
     * Returns the link how it is for the resource after using this resource wrapper.<p>
     *
     * For example: in the VFS: "/sites/default/index.html" and this resource wrapper
     * adds the extension "jsp" then link returned should be "/sites/default/index.html.jsp".<p>
     *
     * @see #restoreLink(CmsObject, String)
     * @see CmsObjectWrapper#rewriteLink(String)
     *
     * @param cms the initialized CmsObject
     * @param res the resource where to rewrite the path
     *
     * @return the rewritten path or null if the wrapper is not responsible
     */
    String rewriteLink(CmsObject cms, CmsResource res);

    /**
     * Unlocks a resource.<p>
     *
     * First should be a check if the resourcename is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resourcename is a virtual path and
     * so has to be translated into a valid path existing in the VFS to unlock the resource.<p>
     *
     * @see CmsObjectWrapper#unlockResource(String)
     * @see CmsObject#unlockResource(String)
     *
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to unlock (full path)
     *
     * @return true if this request could be handled by this wrapper of false if not
     *
     * @throws CmsException if something goes wrong
     */
    boolean unlockResource(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Changes the given resource based on the implementation of this wrapper.<p>
     *
     * For example, some resource types like {@link org.opencms.file.types.CmsResourceTypeJsp}
     * just get a <code>.jsp</code> extension, while other types like
     * {@link org.opencms.file.types.CmsResourceTypeXmlPage} are "exploded" into a folder /
     * file structure.<p>
     *
     * @see CmsObjectWrapper#getResourcesInFolder(String, CmsResourceFilter)
     *
     * @param cms the current users OpenCms context
     * @param resource the resource to wrap
     *
     * @return a wrapped {@link CmsResource} object or <code>null</code> to not change the resource
     */
    CmsResource wrapResource(CmsObject cms, CmsResource resource);

    /**
     * Writes a resource, including it's content.<p>
     *
     * First should be a check if the resource is handled by this
     * resource wrapper.<p>
     *
     * It is possible that the path in the resource is a virtual path and
     * so has to be translated into a valid path existing in the VFS to write the resource.<p>
     *
     * @see CmsObjectWrapper#writeFile(CmsFile)
     * @see CmsObject#writeFile(CmsFile)
     *
     * @param cms the current cms context
     * @param resource the resource to apply this operation to
     *
     * @return the written resource or null if the action could not be handled by this resource wrapper
     *
     * @throws CmsException if something goes wrong
     */
    CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException;
}