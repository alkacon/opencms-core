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
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsExternalLinkInfoBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsHistoryVersion;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.gwt.shared.CmsQuickLaunchParams;
import org.opencms.gwt.shared.CmsRenameInfoBean;
import org.opencms.gwt.shared.CmsReplaceInfo;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsRestoreInfoBean;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * A service interface for retrieving information about the VFS tree.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsVfsService extends RemoteService {

    /**
     * Creates a new external link resource.<p>
     *
     * @param title the title
     * @param link the link
     * @param resourceName the name of the link resource to create
     * @param parentFolderPath the parent folder site path
     *
     * @throws CmsRpcException if something goes wrong
     */
    void createNewExternalLink(String title, String link, String resourceName, String parentFolderPath)
    throws CmsRpcException;

    /**
     * Creates a new property definition.<p>
     *
     * @param propDef the name of the property
     *
     * @throws CmsRpcException if something goes wrong
     */
    void createPropertyDefinition(String propDef) throws CmsRpcException;

    /**
     * Deletes a resource from the VFS.<p>
     *
     * @param structureId the structure id of the resource to delete
     *
     * @throws CmsRpcException if something goes wrong
     */
    void deleteResource(CmsUUID structureId) throws CmsRpcException;

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
     * Fetches the aliases for a given page.<p>
     *
     * @param uuid the structure id of the page
     *
     * @return the lists of aliases for the page
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsAliasBean> getAliasesForPage(CmsUUID uuid) throws CmsRpcException;

    /**
     * Returns a list of potentially broken links, if the given resource was deleted.<p>
     *
     * @param structureId the resource structure id
     *
     * @return a list of potentially broken links
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsDeleteResourceBean getBrokenLinks(CmsUUID structureId) throws CmsRpcException;

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
     * Loads a thumbnail for a dataview record.<p>
     *
     * @param config the dataview configuration string
     * @param id the record id
     *
     * @return the URL of the thumbnail
     * @throws CmsRpcException if something goes wrong
     */
    String getDataViewThumbnail(String config, String id) throws CmsRpcException;

    /**
     * Gets the default property configurations for the given structure ids.<p>
     *
     * @param structureIds the structure ids for which the property configurations should be fetched
     * @return a map from the given structure ids to their default property configurations
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<CmsUUID, Map<String, CmsXmlContentProperty>> getDefaultProperties(List<CmsUUID> structureIds)
    throws CmsRpcException;

    /**
     * Gets the names of defined properties.<p>
     *
     * @return the list of names for all defined properties
     *
     * @throws CmsRpcException if something goes wrong
     */
    ArrayList<String> getDefinedProperties() throws CmsRpcException;

    /**
     * Gets the detail name for the given structure id.
     *
     * @param id the structure id of a content
     * @param locale the locale to use
     * @return the detail name for the structure id
     *
     * @throws CmsRpcException if something goes wrong
     */
    String getDetailName(CmsUUID id, String locale) throws CmsRpcException;

    /**
     * Returns the file replace info.<p>
     *
     * @param structureId the structure id of the file to replace
     *
     * @return the file replace info
     *
     * @throws CmsRpcException if the RPC call goes wrong
     */
    CmsReplaceInfo getFileReplaceInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Gets the preview information for a historic version.<p>
     *
     * @param structureId the structure id of the resource
     * @param locale the locale
     * @param version the version number
     *
     * @return the preview information for the historic resource version
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsPreviewInfo getHistoryPreviewInfo(CmsUUID structureId, String locale, CmsHistoryVersion version)
    throws CmsRpcException;

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
     * Returns the preview info for the given resource.<p>
     *
     * @param structureId the resource structure id
     * @param locale the requested locale
     *
     * @return the preview info
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsPreviewInfo getPreviewInfo(CmsUUID structureId, String locale) throws CmsRpcException;

    /**
     * Returns the preview info for the given resource.<p>
     *
     * @param sitePath the resource site path
     * @param locale the requested locale
     *
     * @return the preview info
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsPreviewInfo getPreviewInfo(String sitePath, String locale) throws CmsRpcException;

    /***
     * Gets the information necessary for the rename dialog.<p>
     *
     * @param structureId the structure id of the resource to rename
     *
     * @return the information needed for the rename dialog
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsRenameInfoBean getRenameInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Gets the resource history for a given structure id.<p>
     *
     * @param structureId the structure id of a resource
     * @return the history for the given resource
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsHistoryResourceCollection getResourceHistory(CmsUUID structureId) throws CmsRpcException;

    /**
     * Gets a bean containing status information for a given resource.<p>
     *
     * @param structureId the structure id of a resource
     * @param locale the locale for which we want the resource information
     * @param includeTargets true if relation targets should also be fetched
     * @param detailContentId the structure id of the detail content if present
     * @param context additional context-dependent parameters used for providing additional information
     *
     * @return the resource status
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsResourceStatusBean getResourceStatus(
        CmsUUID structureId,
        String locale,
        boolean includeTargets,
        CmsUUID detailContentId,
        Map<String, String> context)
    throws CmsRpcException;

    /**
     * Gets the information which is necessary for opening the 'Restore' dialog for a resource.<p>
     *
     * @param structureId the structure id of the resource
     * @return the information for the resource
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsRestoreInfoBean getRestoreInfo(CmsUUID structureId) throws CmsRpcException;

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
     * Gets the site paths corresponding to a list of structure ids.
     *
     * <p>If for any of the structure ids in the input list the corresponding resource can not be read, it will be skipped.
     *
     * @param ids a list of structure ids
     * @return the list of paths corresponding to the structure ids
     * @throws CmsRpcException
     */
    List<String> getSitePaths(List<CmsUUID> ids) throws CmsRpcException;

    /**
     * Gets the structure id for a given site path.
     *
     * @param vfsPath the site path
     * @return the structure id
     * @throws CmsRpcException if something goes wrong
     */
    CmsUUID getStructureId(String vfsPath) throws CmsRpcException;

    /**
     * Gets the resource info to display for an upload folder.
     *
     * @param path the folder path
     * @return the info to display
     * @throws CmsRpcException if something goes wrong
     */
    CmsListInfoBean getUploadFolderInfo(String path) throws CmsRpcException;

    /**
     * Loads the external link info.<p>
     *
     * @param structureId the external link structure id
     *
     * @return the external link info
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsExternalLinkInfoBean loadLinkInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Load the data necessary to edit the properties of a resource.<p>
     *
     * @param id the structure id of a resource
     * @return the property information for that resource
     * @throws CmsRpcException if something goes wrong
     */
    CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsRpcException;

    /**
     * Loads the items for the quick launch menu.<p>
     *
     * @param params the quick launch parameters
     *
     * @return the list of quick launch items
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsQuickLaunchData> loadQuickLaunchItems(CmsQuickLaunchParams params) throws CmsRpcException;

    /**
     * Prepares to edit a file in the XML content editor.<p>
     *
     * @param currentPage the current page from which the editor should be opened
     * @param fileNameWithMacros the file name, which may contain macros
     *
     * @return a bean with more information about the file to edit
     * @throws CmsRpcException if something goes wrong
     */
    CmsPrepareEditResponse prepareEdit(CmsUUID currentPage, String fileNameWithMacros) throws CmsRpcException;

    /**
     * Renames a resource.<p>
     *
     * @param structureId the structure id of the resource to rename
     * @param newName the new resource name
     *
     * @return null or an error message
     *
     * @throws CmsRpcException if something goes wrong
     */
    String renameResource(CmsUUID structureId, String newName) throws CmsRpcException;

    /**
     * Reverts a resource to a previous historic version.<p>
     *
     * @param structureId the structure id of the resource to revert
     * @param version the version to which the resource should be reverted
     *
     * @throws CmsRpcException  if something goes wrong
     */
    void restoreResource(CmsUUID structureId, int version) throws CmsRpcException;

    /**
     * Saves aliases for a page.<p>
     *
     * @param structureId the structure id of the page
     *
     * @param aliases the aliases which should be saved for the page
     * @throws CmsRpcException if something goes wrong
     */
    void saveAliases(CmsUUID structureId, List<CmsAliasBean> aliases) throws CmsRpcException;

    /**
     * Saves the external link.<p>
     *
     * @param structureId the link structure id
     * @param title the link title
     * @param link the link
     * @param fileName the file name
     *
     * @throws CmsRpcException if something goes wrong
     */
    void saveExternalLink(CmsUUID structureId, String title, String link, String fileName) throws CmsRpcException;

    /**
     * Saves  a set of property changes.<p>
     *
     * @param changes a set of property changes
     * @param updateIndex true if the index should be updated after saving the property changes
     *
     * @throws CmsRpcException if something goes wrong
     */
    void saveProperties(CmsPropertyChangeSet changes, boolean updateIndex) throws CmsRpcException;

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

    /**
     * Deletes a resource from the VFS.<p>
     *
     * @param structureId the structure id of the resource to delete
     *
     * @throws CmsRpcException if something goes wrong
     */
    void syncDeleteResource(CmsUUID structureId) throws CmsRpcException;

    /**
     * Undeletes a resource.<p>
     *
     * @param structureId the structure id of the resource to undelete
     *
     * @throws CmsRpcException if something goes wrong
     */
    void undelete(CmsUUID structureId) throws CmsRpcException;

    /**
     * Undoes the changes to a given resource, i.e. restores its online content to its offline version.<p>
     *
     * @param structureId the structure id of the resource to undo
     * @param undoMove true if move operations should be undone
     *
     * @throws CmsRpcException if something goes wrong
     */
    void undoChanges(CmsUUID structureId, boolean undoMove) throws CmsRpcException;

    /**
     * Validates alias paths for a page.<p>
     *
     * @param uuid the structure id of the page
     * @param aliasPaths a map from (arbitrary) id strings to alias paths
     *
     * @return a map which maps the same id strings to validation results
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<String, String> validateAliases(CmsUUID uuid, Map<String, String> aliasPaths) throws CmsRpcException;

}
