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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * An asynchronous service interface for retrieving information about the VFS tree.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsVfsServiceAsync {

    /**
     * Creates a new external link resource.<p>
     *
     * @param title the title
     * @param link the link
     * @param resourceName the name of the link resource to create
     * @param parentFolderPath the parent folder site path
     * @param callback the async callback
     */
    void createNewExternalLink(
        String title,
        String link,
        String resourceName,
        String parentFolderPath,
        AsyncCallback<Void> callback);

    /**
     * Creates a property definition.<p>
     *
     * @param propertyName the new property name
     *
     * @param callback the callback
     */
    void createPropertyDefinition(String propertyName, AsyncCallback<Void> callback);

    /**
     * Deletes a resource from the VFS.<p>
     *
     * @param structureId the structure id of the resource to delete
     * @param callback the callback
     */
    void deleteResource(CmsUUID structureId, AsyncCallback<Void> callback);

    /**
     * Deletes a resource from the VFS.<p>
     *
     * @param sitePath the site path of the resource to delete
     * @param callback the callback
     */
    void deleteResource(String sitePath, AsyncCallback<Void> callback);

    /**
     * Forces a resource to be unlocked. In case the given resource is a folder, all sub-resources are also unlocked.<p>
     *
     * @param structureId the structure id of the resource to unlock
     * @param callback the callback
     */
    void forceUnlock(CmsUUID structureId, AsyncCallback<Void> callback);

    /**
     * Fetches the aliases for a given page.<p>
     *
     * @param structureId the structure id of the page
     * @param callback the async callback
     *
     */
    void getAliasesForPage(CmsUUID structureId, AsyncCallback<List<CmsAliasBean>> callback);

    /**
     * Returns a list of potentially broken links, if the given resource was deleted.<p>
     *
     * @param structureId the resource structure id
     * @param callback the callback
     */
    void getBrokenLinks(CmsUUID structureId, AsyncCallback<CmsDeleteResourceBean> callback);

    /**
     * Returns a list of potentially broken links, if the given resource was deleted.<p>
     *
     * @param sitePath the resource site-path
     * @param callback the callback
     */
    void getBrokenLinks(String sitePath, AsyncCallback<CmsDeleteResourceBean> callback);

    /**
     * Fetches the list of children of a path.<p>
     *
     * @param path the path for which the list of children should be retrieved
     * @param callback the asynchronous callback
     */
    void getChildren(String path, AsyncCallback<List<CmsVfsEntryBean>> callback);

    /**
     * Loads a thumbnail for the given dataview configuration and id.<p>
     *
     * @param config the dataview configuration
     * @param id the data id
     * @param imageCallback the callback to be called with the result URL
     */
    void getDataViewThumbnail(String config, String id, AsyncCallback<String> imageCallback);

    /**
     * Gets the default property configurations for a list of structure ids.<p>
     *
     * @param structureIds the structure ids for which to fetch the default property configurations
     *
     * @param callback the callback for the result
     */
    void getDefaultProperties(
        List<CmsUUID> structureIds,
        AsyncCallback<Map<CmsUUID, Map<String, CmsXmlContentProperty>>> callback);

    /**
     * Gets the names of defined properties.<p>
     *
     * @param callback the callback for the results
     */
    void getDefinedProperties(AsyncCallback<ArrayList<String>> callback);

    /**
     * Gets the detail name for the given structure id.
     *
     * @param id a structure id
     * @param localeStr the locale as a string
     * @param callback the callback for the result
     */
    void getDetailName(CmsUUID id, String localeStr, AsyncCallback<String> callback);

    /**
     * Returns the file replace info.<p>
     *
     * @param structureId the structure id of the file to replace
     * @param callback the asynchronous callback
     */
    void getFileReplaceInfo(CmsUUID structureId, AsyncCallback<CmsReplaceInfo> callback);

    /**
     * Gets th historical preview information for the given resource.<p>
     *
     * @param structureId the structure id of the resource
     * @param locale the locale for which to get the preview info
     * @param version thee version for which to get the preview information
     *
     * @param resultCallback if something goe
     */
    void getHistoryPreviewInfo(
        CmsUUID structureId,
        String locale,
        CmsHistoryVersion version,
        AsyncCallback<CmsPreviewInfo> resultCallback);

    /**
     * Returns the lock report info.<p>
     *
     * @param structureId the structure id of the resource to get the report for
     * @param callback the callback
     */
    void getLockReportInfo(CmsUUID structureId, AsyncCallback<CmsLockReportInfo> callback);

    /**
     * Gets a {@link CmsListInfoBean} for a given resource.<p>
     *
     * @param structureId the structure id to create the {@link CmsListInfoBean} for
     * @param callback the asynchronous callback
     */
    void getPageInfo(CmsUUID structureId, AsyncCallback<CmsListInfoBean> callback);

    /**
     * Gets a {@link CmsListInfoBean} for a given resource.<p>
     *
     * @param vfsPath the vfs path to create the {@link CmsListInfoBean} for
     * @param callback the asynchronous callback
     */
    void getPageInfo(String vfsPath, AsyncCallback<CmsListInfoBean> callback);

    /**
     * Returns the preview info for the given resource.<p>
     *
     * @param structureId the resource structure id
     * @param locale the requested locale
     * @param callback the call back
     */
    void getPreviewInfo(CmsUUID structureId, String locale, AsyncCallback<CmsPreviewInfo> callback);

    /**
     * Returns the preview info for the given resource.<p>
     *
     * @param sitePath the resource site path
     * @param locale the requested locale
     * @param callback the call back
     */
    void getPreviewInfo(String sitePath, String locale, AsyncCallback<CmsPreviewInfo> callback);

    /**
     * Gets the information needed for the Rename dialog.<p>
     *
     * @param structureId the structure id of the resource to rename
     * @param callback the callback for the result
     */
    void getRenameInfo(CmsUUID structureId, AsyncCallback<CmsRenameInfoBean> callback);

    /**
     * Gets the history of a resource.<p>
     *
     * @param structureId the structure id of the resource
     * @param resultCallback the callback to call with the result
     */
    void getResourceHistory(CmsUUID structureId, AsyncCallback<CmsHistoryResourceCollection> resultCallback);

    /**
     * Gets status information for a single resource.<p>
     *
     * @param structureId the structure id of the resource
     * @param locale the locale for which we want the resource information
     * @param includeTargets flag to control whether relation targets should also be fetched
     * @param detailContentId the structure id of the detail content if present
     * @param context a map of context-dependent parameters used to provide additional information
     * @param callback the callback for the results
     */
    void getResourceStatus(
        CmsUUID structureId,
        String locale,
        boolean includeTargets,
        CmsUUID detailContentId,
        Map<String, String> context,
        AsyncCallback<CmsResourceStatusBean> callback);

    /**
     * Gets the information which is necessary for opening the 'Restore' dialog for a resource.<p>
     *
     * @param structureId the structure id of the resource
     * @param resultCallback the callback for the result
     */
    void getRestoreInfo(CmsUUID structureId, AsyncCallback<CmsRestoreInfoBean> resultCallback);

    /**
     * Returns the root entries of the VFS.<p>
     *
     * @param callback the asynchronous callback
     */
    void getRootEntries(AsyncCallback<List<CmsVfsEntryBean>> callback);

    /**
     * Returns the site-path for the resource with the given id.<p>
     *
     * @param structureId the structure id
     * @param callback the asynchronous callback
     */
    void getSitePath(CmsUUID structureId, AsyncCallback<String> callback);

    /**
     * Gets the site paths corresponding to a list of structure ids.
     *
     * <p>If for any of the structure ids in the input list the corresponding resource can not be read, it will be skipped.
     *
     * @param ids a list of structure ids
     * @param callback the result callback
     */
    void getSitePaths(List<CmsUUID> ids, AsyncCallback<List<String>> callback);

    /**
     * Gets the structure id for the given site path.
     *
     * @param vfsPath a site path
     * @param callback the callback for the result
     */
    void getStructureId(String vfsPath, AsyncCallback<CmsUUID> callback);

    /**
     * Gets the resource info to display for an upload folder.
     *
     * @param path the folder path
     * @param callback the callback to call with the result
     */
    void getUploadFolderInfo(String path, AsyncCallback<CmsListInfoBean> callback);

    /**
     * Loads the external link info.<p>
     *
     * @param structureId the external link structure id
     * @param callback the callback
     */
    void loadLinkInfo(CmsUUID structureId, AsyncCallback<CmsExternalLinkInfoBean> callback);

    /**
     * Load the data necessary to edit the properties of a resource.<p>
     *
     * @param id the structure id of a resource
     * @param callback the asynchronous callback
     */
    void loadPropertyData(CmsUUID id, AsyncCallback<CmsPropertiesBean> callback);

    /**
     * Loads the items for the quick launch menu.<p>
     *
     * @param params the quick launch parameters
     *
     * @param resultCallback the callback for the result
     */
    void loadQuickLaunchItems(CmsQuickLaunchParams params, AsyncCallback<List<CmsQuickLaunchData>> resultCallback);

    /**
     * Prepares to edit a file in the XML content editor.<p>
     *
     * @param currentPage the current page from which the editor should be opened
     * @param fileNameWithMacros the file name, which may contain macros
     *
     * @param callback the asynchronous callback
     */
    void prepareEdit(CmsUUID currentPage, String fileNameWithMacros, AsyncCallback<CmsPrepareEditResponse> callback);

    /**
     * Renames a resource.<p>
     *
     * @param structureId the structure id of the resource to rename
     * @param newName the new resource name
     *
     * @param callback the asynchronous callback for the result
     */
    void renameResource(CmsUUID structureId, String newName, AsyncCallback<String> callback);

    /**
     * Restores a previous version of the resource.<p>
     *
     * @param structureId the structure id of the version
     * @param version the number of the version to which  the resource should be reverted
     * @param callback the callback to call with the results
     */
    void restoreResource(CmsUUID structureId, int version, AsyncCallback<Void> callback);

    /**
     * Saves aliases for a page.<p>
     *
     * @param structureId the structure id of the page
     * @param aliases the aliases which should be saved for the page
     * @param callback the async callback
     */
    void saveAliases(CmsUUID structureId, List<CmsAliasBean> aliases, AsyncCallback<Void> callback);

    /**
     * Saves the external link.<p>
     *
     * @param structureId the link structure id
     * @param title the link title
     * @param link the link
     * @param fileName the file name
     * @param callback the asynchronous callback
     */
    void saveExternalLink(
        CmsUUID structureId,
        String title,
        String link,
        String fileName,
        AsyncCallback<Void> callback);

    /**
     * Saves a set of property changes.<p>
     *
     * @param changes the property changes
     * @param updateIndex true if the index should be updated after saving the property changes
     * @param callback the asynchronous callback
     */
    void saveProperties(CmsPropertyChangeSet changes, boolean updateIndex, AsyncCallback<Void> callback);

    /**
     * Returns the absolute link to the given root path.<p>
     *
     * @param currentSiteRoot the current site
     * @param rootPath the root path
     * @param callback the asynchronous callback
     */
    void substituteLinkForRootPath(String currentSiteRoot, String rootPath, AsyncCallback<String> callback);

    /**
     * Deletes a resource from the VFS.<p>
     *
     * @param structureId the structure id of the resource to delete
     * @param callback the callback
     */
    @SynchronizedRpcRequest
    void syncDeleteResource(CmsUUID structureId, AsyncCallback<Void> callback);

    /**
     * Undeletes a resource.<p>
     *
     * @param structureId the structure id of the resource
     *
     * @param callback the result callback
     */
    void undelete(CmsUUID structureId, AsyncCallback<Void> callback);

    /**
     * Undoes the changes to a given resource, i.e. restores its online content to its offline version.<p>
     *
     * @param structureId the structure id of the resource to undo
     * @param undoMove true if move operations should be undone
     * @param callback the callback for the result
     */
    void undoChanges(CmsUUID structureId, boolean undoMove, AsyncCallback<Void> callback);

    /**
     * Validates alias paths for a page.<p>
     *
     * @param structureId the structure id of the page
     * @param aliasPaths a map from (arbitrary) id strings to alias paths
     * @param callback the async callback
     */
    void validateAliases(
        CmsUUID structureId,
        Map<String, String> aliasPaths,
        AsyncCallback<Map<String, String>> callback);

}
