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

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsBroadcastMessage;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsCoreData.UserInfo;
import org.opencms.gwt.shared.CmsResourceCategoryInfo;
import org.opencms.gwt.shared.CmsReturnLinkInfo;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Provides general core services.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public interface I_CmsCoreService extends RemoteService {

    /**
     * Changes the password of the current user.<p>
     *
     * @param oldPassword the old password
     * @param newPassword the value entered for the new password
     * @param newPasswordConfirm the value entered for the confirmation of the new password
     *
     * @return an error message if an error occurred, or null if the password was successfully changed
     *
     * @throws CmsRpcException if something goes wrong
     */
    String changePassword(String oldPassword, String newPassword, String newPasswordConfirm) throws CmsRpcException;

    /**
    * Creates a new UUID.<p>
    *
    * @return the created UUID
    *
    * @throws CmsRpcException if something goes wrong
    */
    CmsUUID createUUID() throws CmsRpcException;

    /**
     * Returns the latest messages for the current user.<p>
     *
     * @return the messages
     *
     * @throws CmsRpcException if anything goes wrong
     */
    List<CmsBroadcastMessage> getBroadcast() throws CmsRpcException;

    /**
     * Returns the categories for the given search parameters.<p>
     *
     * @param fromCatPath the category path to start with, can be <code>null</code> or empty to use the root
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param refVfsPath the reference path (site-relative path according to which the available category repositories are determined),
     *        can be <code>null</code> to only use the system repository
     * @param withRepositories flag, indicating if also the category repositories should be returned as category
     * @param selected a set of paths of currently selected categories (which should be included in the result even if they are marked as hidden)
     *
     * @return the resource categories
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsCategoryTreeEntry> getCategories(
        String fromCatPath,
        boolean includeSubCats,
        String refVfsPath,
        boolean withRepositories,
        Set<String> selected)
    throws CmsRpcException;

    /**
     * Returns the categories for the given reference site-path.<p>
     *
     * @param sitePath the reference site-path
     *
     * @return the categories for the given reference site-path
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsCategoryTreeEntry> getCategoriesForSitePath(String sitePath) throws CmsRpcException;

    /**
     * Returns the category information for the given resource.<p>
     *
     * @param structureId the resource structure id
     *
     * @return the category information
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsResourceCategoryInfo getCategoryInfo(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns the context menu entries for the given URI.<p>
     *
     * @param structureId the currently requested structure id
     * @param context the ade context (sitemap or containerpage)
     *
     * @return the context menu entries
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsContextMenuEntryBean> getContextMenuEntries(CmsUUID structureId, AdeContext context) throws CmsRpcException;

    /**
     * Returns the context menu entries for the given URI.<p>
     *
     * @param structureId the currently requested structure id
     * @param context the ade context (sitemap or containerpage)
     * @param params additional context information the server side can use to determine menu item availability
     *
     * @return the context menu entries
     *
     * @throws CmsRpcException if something goes wrong
     */

    List<CmsContextMenuEntryBean> getContextMenuEntries(
        CmsUUID structureId,
        AdeContext context,
        Map<String, String> params)
    throws CmsRpcException;

    /**
     * Given a return code, returns the link to the page which corresponds to the return code.<p>
     *
     * @param returnCode the return code
     *
     * @return the link for the return code
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsReturnLinkInfo getLinkForReturnCode(String returnCode) throws CmsRpcException;

    /**
     * Gets the resource state for a resource with a given path.<p>
     *
     * @param structureId the resource structure id
     *
     * @return the resource state of the resource
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsResourceState getResourceState(CmsUUID structureId) throws CmsRpcException;

    /**
     * Returns a unique filename for the given base name and the parent folder.<p>
     *
     * @param parentFolder the parent folder of the file
     * @param baseName the proposed file name
     *
     * @return the unique file name
     *
     * @throws CmsRpcException if something goes wrong
     */
    String getUniqueFileName(String parentFolder, String baseName) throws CmsRpcException;

    /**
     * Returns the user info.<p>
     *
     * @return the user info
     *
     * @throws CmsRpcException in case something goes wrong
     */
    UserInfo getUserInfo() throws CmsRpcException;

    /**
     * Returns a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.<p>
     *
     * @param structureId the structure id of the resource for which to open the workplace
     *
     * @return a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     *         site of the given explorerRootPath and show the folder given in the explorerRootPath.
     *
     * @throws CmsRpcException if something goes wrong
     */
    String getWorkplaceLink(CmsUUID structureId) throws CmsRpcException;

    /**
     * Gets the workplace link for the given path.
     *
     * @param path the path
     * @return the workplace link for the path
     * @throws CmsRpcException if something goes wrong
     */
    String getWorkplaceLinkForPath(String path) throws CmsRpcException;

    /**
     * Loads the user settings for the current user.<p>
     *
     * @return the user settings for the current user
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsUserSettingsBean loadUserSettings() throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock if it exists.<p>
     * If the resource does not exist yet, the closest existing ancestor folder will check if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String lockIfExists(String sitePath) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock if it exists.<p>
     * If the resource does not exist yet, the closest existing ancestor folder will check if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param loadTime the time when the requested resource was loaded
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String lockIfExists(String sitePath, long loadTime) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock.<p>
     *
     * @param structureId the structure id of the resource to lock
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String lockTemp(CmsUUID structureId) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock.<p>
     * Locking will fail in case the requested resource has been changed since the given load time.<p>
     *
     * @param structureId the resource structure id
     * @param loadTime the time when the requested resource was loaded
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String lockTemp(CmsUUID structureId, long loadTime) throws CmsRpcException;

    /**
     * Generates core data for prefetching in the host page.<p>
     *
     * @return the core data
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsCoreData prefetch() throws CmsRpcException;

    /**
     * Saves the user settings for the current user.<p>
     *
     * @param userSettings the new values for the user settings
     * @param edited the keys of the user settings which were actually edited
     *
     * @throws CmsRpcException if something goes wrong
     */
    void saveUserSettings(Map<String, String> userSettings, Set<String> edited) throws CmsRpcException;

    /**
     * Sets the categories of the given resource. Will remove all other categories.<p>
     *
     * @param structureId the resource structure id
     * @param categories the categories to set
     *
     * @throws CmsRpcException if something goes wrong
     */
    void setResourceCategories(CmsUUID structureId, List<String> categories) throws CmsRpcException;

    /**
     * Sets the show editor help flag.<p>
     *
     * @param showHelp the show help flag
     *
     * @throws CmsRpcException if something goes wrong
     */
    void setShowEditorHelp(boolean showHelp) throws CmsRpcException;

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     *
     * @param visible <code>true</code> if the tool-bar is visible
     *
     * @throws CmsRpcException if something goes wrong
     */
    void setToolbarVisible(boolean visible) throws CmsRpcException;

    /**
     * Unlocks the given resource.<p>
     *
     * @param structureId the structure id of the resource to unlock
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String unlock(CmsUUID structureId) throws CmsRpcException;

    /**
     * Unlocks the given resource.<p>
     *
     * @param rootPath the root path of the resource to unlock
     *
     * @return <code>null</code> if successful, an error message if not
     *
     * @throws CmsRpcException if something goes wrong
     */
    String unlock(String rootPath) throws CmsRpcException;

    /**
     * Performs a batch of validations and returns the results.<p>
     *
     * @param validationQueries a map from field names to validation queries
     *
     * @return a map from field names to validation results
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<String, CmsValidationResult> validate(Map<String, CmsValidationQuery> validationQueries) throws CmsRpcException;

    /**
     * Performs a batch of validations using a custom form validator class.<p>
     *
     * @param formValidatorClass the class name of the form validator
     * @param validationQueries a map from field names to validation queries
     * @param values the map of all field values
     * @param config the form validator configuration string
     *
     * @return a map from field names to validation results
     *
     * @throws CmsRpcException if the RPC call goes wrong
     */
    Map<String, CmsValidationResult> validate(
        String formValidatorClass,
        Map<String, CmsValidationQuery> validationQueries,
        Map<String, String> values,
        String config)
    throws CmsRpcException;

}
