/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/I_CmsResourceWrapper.java,v $
 * Date   : $Date: 2007/02/22 12:35:51 $
 * Version: $Revision: 1.1.4.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.List;

/**
 * Interface to make it possible to work with the CmsObjectWrapper to handle resources
 * of the configured type different.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.3 $
 * 
 * since 6.5.6
 */
public interface I_CmsResourceWrapper {

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
     * @param source the name of the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @return true if the resource was copied successfully otherwise false
     * 
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, CmsResourceCopyMode)
     */
    boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
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
     */
    CmsResource createResource(CmsObject cms, String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException;

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
     * @param resourcename the name of the resource to delete 
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @return true if the resource was deleted successfully otherwise false
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, CmsResourceDeleteMode)
     */
    boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode) throws CmsException;

    /**
     * Returns the lock for the resource.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resource the resource to check the lock for
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#getLock(CmsResource)
     */
    CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#getResourcesInFolder(String, CmsResourceFilter)
     */
    List getResourcesInFolder(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException;

    /**
     * Returns the system lock for the resource.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resource the resource to check the lock for
     * 
     * @return the system lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#getSystemLock(CmsResource)
     */
    CmsLock getSystemLock(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Is called to check if the given resource is handled by this wrapper.<p>
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
     * This will be an exclusive, persistant lock that is removed only if the user unlocks it.<p>
     *
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to lock (full path)
     * 
     * @return true if this request could be handled by this wrapper of false if not
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     */
    boolean lockResource(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param cms the current cms context
     * @param source the name of the resource to move
     * @param destination the destination resource name
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is null or of length 0
     * 
     * @return true if the resource was moved successfully otherwise false
     * 
     * @see CmsObject#moveResource(String, String)
     * @see CmsObject#renameResource(String, String)
     */
    boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException;

    /**
     * Reads a file which was not found in the VFS and this resource
     * type wrapper is responsible for.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     * 
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see CmsObject#readFile(String, CmsResourceFilter)
     */
    CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException;

    /**
     * Reads a resource which was not found in the VFS and this resource
     * type wrapper is responsible for using the <code>{@link CmsResourceFilter#DEFAULT}</code> 
     * filter.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param resourcename The name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     * 
     * @return the resource that was read
     * 
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsObject#readResource(String)
     */
    CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException;

    /**
     * Returns the link to a existing resource in the VFS for the uri.<p>
     * 
     * @param cms the initialized CmsObject
     * @param uri the (virutal) uri to be restored
     * 
     * @return the uri where to find the resource in the VFS or null if the wrapper is not responsible
     */
    String restoreLink(CmsObject cms, String uri);

    /**
     * Change the link to the resource existing in the VFS.<p>
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
     * @param cms the current users OpenCms context
     * @param resourcename the name of the resource to unlock (full path)
     * 
     * @return true if this request could be handled by this wrapper of false if not
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     */
    boolean unlockResource(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Changes the given resource based on the implementation of this wrapper.<p>
     * 
     * For example, some resource types like {@link org.opencms.file.types.CmsResourceTypeJsp} just get a <code>.jsp</code> extension,
     * while other types like {@link org.opencms.file.types.CmsResourceTypeXmlPage} are "exploded" into a folder / file structure.<p>
     * 
     * The wrapper will be found depending on the configuration and the resource type id.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to wrap
     * 
     * @return a wrapped {@link CmsResource} object
     */
    CmsResource wrapResource(CmsObject cms, CmsResource resource);

    /**
     * Writes a resource, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * that have a binary content attached.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to apply this operation to
     *
     * @return the written resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     */
    CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException;
}