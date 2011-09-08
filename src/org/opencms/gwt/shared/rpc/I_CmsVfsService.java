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

package org.opencms.gwt.shared.rpc;

import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * A service interface for retrieving information about the VFS tree.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsVfsService extends RemoteService {

    /**
     * Deletes a resource from the VFS.<p>
     * 
     * @param sitePath the site path of the resource to delete
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    void deleteResource(String sitePath) throws CmsRpcException;

    /**
     * Forces a resource to be unlocked. In case the given resource is a folder, all sub-resources are also unlocked.<p>
     * 
     * @param structureId the structure id of the resource to unlock
     * 
     * @throws CmsRpcException if something goes wrong
     */
    void forceUnlock(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns a {@link CmsAvailabilityInfoBean} for a given resource.<p>
     * 
     * @param structureId the structure id to create the {@link CmsAvailabilityInfoBean} for
     * 
     * @return the {@link CmsAvailabilityInfoBean} for a given resource
     * 
     * @throws CmsRpcException if the RPC call goes wrong 
     */
    CmsAvailabilityInfoBean getAvailabilityInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns a {@link CmsAvailabilityInfoBean} for a given resource.<p>
     * 
     * @param vfsPath the vfs path to create the {@link CmsAvailabilityInfoBean} for
     * 
     * @return the {@link CmsAvailabilityInfoBean} for a given resource
     * 
     * @throws CmsRpcException if the RPC call goes wrong
     */
    CmsAvailabilityInfoBean getAvailabilityInfo(String vfsPath) throws CmsRpcException;

    /**
     * Returns a list of potentially broken links, if the given resource was deleted.<p>
     * 
     * @param sitePath the resource site-path
     * 
     * @return a list of potentially broken links
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsDeleteResourceBean getBrokenLinks(String sitePath) throws CmsRpcException;

    /**
     * Fetches the list of children of a path.<p>
     * 
     * @param path the path for which the list of children should be retrieved
     *  
     * @return the children of the path 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    List<CmsVfsEntryBean> getChildren(String path) throws CmsRpcException;

    /**
     * Returns the lock report info.<p>
     * 
     * @param structureId the structure id of the resource to get the report for
     * 
     * @return the lock report info
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsLockReportInfo getLockReportInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns a {@link CmsListInfoBean} for a given resource.<p>
     * 
     * @param structureId the structure id to create the {@link CmsListInfoBean} for
     * 
     * @return the {@link CmsListInfoBean} for a given resource
     * 
     * @throws CmsRpcException if the RPC call goes wrong 
     */
    CmsListInfoBean getPageInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns a {@link CmsListInfoBean} for a given resource.<p>
     * 
     * @param vfsPath the vfs path to create the {@link CmsListInfoBean} for
     * 
     * @return the {@link CmsListInfoBean} for a given resource
     * 
     * @throws CmsRpcException if the RPC call goes wrong 
     */
    CmsListInfoBean getPageInfo(String vfsPath) throws CmsRpcException;

    /**
     * Returns the root entries of the VFS.<p>
     * 
     * @return a list of root entries
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    List<CmsVfsEntryBean> getRootEntries() throws CmsRpcException;

    /**
     * Returns the site-path for the resource with the given id.<p>
     * 
     * @param structureId the structure id
     * 
     * @return the site-path or <code>null</code> if not available
     * 
     * @throws CmsRpcException if something goes wrong
     */
    String getSitePath(CmsUUID structureId) throws CmsRpcException;

    /**
     * Load the data necessary to edit the properties of a resource.<p>
     * 
     * @param id the structure id of a resource
     * @return the property information for that resource 
     * @throws CmsRpcException
     */
    CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsRpcException;

    /**
     * Prepares to edit a file in the XML content editor.<p>
     * 
     * @param currentPage the current page from which the editor should be opened 
     * @param fileNameWithMacros the file name, which may contain macros
     *  
     * @return a bean with more information about the file to edit
     * @throws CmsRpcException  
     */
    CmsPrepareEditResponse prepareEdit(CmsUUID currentPage, String fileNameWithMacros) throws CmsRpcException;

    /**
     * Saves  a set of property changes.<p>
     * 
     * @param changes a set of property changes
     *  
     * @throws CmsRpcException if something goes wrong 
     */
    void saveProperties(CmsPropertyChangeSet changes) throws CmsRpcException;

    /**
     * Returns the absolute link to the given root path.<p>
     * 
     * @param currentSiteRoot the current site
     * @param rootPath the root path
     * 
     * @return the absolute link
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    String substituteLinkForRootPath(String currentSiteRoot, String rootPath) throws CmsRpcException;
}
