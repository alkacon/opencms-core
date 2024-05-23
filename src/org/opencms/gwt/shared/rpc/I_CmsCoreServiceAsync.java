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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Provides general core services.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public interface I_CmsCoreServiceAsync {

    /**
    * Changes the password of the current user.<p>
    *
    * @param oldPassword the old password
    * @param newPassword the value entered for the new password
    * @param newPasswordConfirm the value entered for the confirmation of the new password
    *
    * @param callback the callback for the result
    */
    void changePassword(
        String oldPassword,
        String newPassword,
        String newPasswordConfirm,
        AsyncCallback<String> callback);

    /**
     * Creates a new UUID.<p>
     *
     * @param callback the async callback
     */
    void createUUID(AsyncCallback<CmsUUID> callback);

    /**
     * Returns the latest messages for the current user.<p>
     *
     * @param callback the async callback
     */
    void getBroadcast(AsyncCallback<List<CmsBroadcastMessage>> callback);

    /**
     * Returns the categories for the given search parameters.<p>
     *
     * @param fromCatPath the category path to start with, can be <code>null</code> or empty to use the root
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param refVfsPath the reference path (site-relative path according to which the available category repositories are determined),
     *        can be <code>null</code> to only use the system repository
     * @param withRepositories flag, indicating if also the category repositories should be returned as category
     * @param selected a set of paths of currently selected categories (which should be included in the result even if they are marked as hidden)
     * @param callback the async callback
     */
    void getCategories(
        String fromCatPath,
        boolean includeSubCats,
        String refVfsPath,
        boolean withRepositories,
        Set<String> selected,
        AsyncCallback<List<CmsCategoryTreeEntry>> callback);

    /**
     * Returns the categories for the given reference site-path.<p>
     *
     * @param sitePath the reference site-path
     * @param callback the async callback
     */
    void getCategoriesForSitePath(String sitePath, AsyncCallback<List<CmsCategoryTreeEntry>> callback);

    /**
     * Returns the category information for the given resource.<p>
     *
     * @param structureId the resource structure id
     * @param callback the callback which receives the result
     */
    void getCategoryInfo(CmsUUID structureId, AsyncCallback<CmsResourceCategoryInfo> callback);

    /**
     * Returns a list of menu entry beans for the context menu.<p>
     *
     * @param structureId the structure id of the resource for which to get the context menu
     * @param context the ade context (sitemap or containerpage)
     * @param callback the asynchronous callback
     */
    void getContextMenuEntries(
        CmsUUID structureId,
        AdeContext context,
        AsyncCallback<List<CmsContextMenuEntryBean>> callback);

    /**
     * Returns a list of menu entry beans for the context menu.<p>
     *
     * @param structureId the structure id of the resource for which to get the context menu
     * @param context the ade context (sitemap or containerpage)
     * @param params additional context information that the server side can use to decide menu item availability
     * @param callback the asynchronous callback
     */
    void getContextMenuEntries(
        CmsUUID structureId,
        AdeContext context,
        Map<String, String> params,
        AsyncCallback<List<CmsContextMenuEntryBean>> callback);

    /**
     * Given a return code, returns the link to the page which corresponds to the return code.<p>
     *
     * @param returnCode the return code
     * @param callback the asynchronous callback
     */
    void getLinkForReturnCode(String returnCode, AsyncCallback<CmsReturnLinkInfo> callback);

    /**
     * Gets the resource state of a resource.<p>
     *
     * @param structureId the structure id of the resource
     * @param callback the callback which receives the result
     */
    void getResourceState(CmsUUID structureId, AsyncCallback<CmsResourceState> callback);

    /**
     * Returns a unique filename for the given base name and the parent folder.<p>
     *
     * This is executed in a synchronized request.<p>
     *
     * @param parentFolder the parent folder of the file
     * @param baseName the proposed file name
     * @param callback the callback which receives the result
     */
    void getUniqueFileName(String parentFolder, String baseName, AsyncCallback<String> callback);

    /**
     * Returns the user info.<p>
     *
     * @param callback the callback
     */
    void getUserInfo(AsyncCallback<UserInfo> callback);

    /**
     * Returns a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.<p>
     *
     * @param structureId the structure id of the resource for which to open the workplace
     * @param callback the callback which receives the result
     */
    void getWorkplaceLink(CmsUUID structureId, AsyncCallback<String> callback);

    /**
     * Gets the workplace link for the given path.
     *
     * @param path the path
     * @param action the callback for the result
     */
    void getWorkplaceLinkForPath(String path, AsyncCallback<String> action);

    /**
     * Loads the user settings for the current user.<p>
     *
     * @param callback the callback to call with the result
     */
    void loadUserSettings(AsyncCallback<CmsUserSettingsBean> callback);

    /**
     * Locks the given resource with a temporary lock if it exists.<p>
     * If the resource does not exist yet, the closest existing ancestor folder will check if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param callback the async callback
     */
    void lockIfExists(String sitePath, AsyncCallback<String> callback);

    /**
     * Locks the given resource with a temporary lock if it exists.<p>
     * If the resource does not exist yet, the closest existing ancestor folder will check if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param loadTime the time when the requested resource was loaded
     * @param callback the async callback
     */
    void lockIfExists(String sitePath, long loadTime, AsyncCallback<String> callback);

    /**
     * Locks the given resource with a temporary lock.<p>
     *
     * @param structureId the resource structure id
     * @param callback the async callback
     */
    void lockTemp(CmsUUID structureId, AsyncCallback<String> callback);

    /**
     * Locks the given resource with a temporary lock.<p>
     * Locking will fail in case the requested resource has been changed since the given load time.<p>
     *
     * @param structureId the resource structure id
     * @param loadTime the time when the requested resource was loaded
     * @param callback the async callback
     */
    void lockTemp(CmsUUID structureId, long loadTime, AsyncCallback<String> callback);

    /**
     * Generates core data for prefetching in the host page.<p>
     *
     * @param callback the async callback
     */
    void prefetch(AsyncCallback<CmsCoreData> callback);

    /**
     * Saves the user settings for the current user.<p>
     *
     * @param userSettings the new values for the user settings
     * @param edited the keys of the user settings which were actually edited
     * @param resultCallback the callback to call when the operation has finished
     */
    void saveUserSettings(Map<String, String> userSettings, Set<String> edited, AsyncCallback<Void> resultCallback);

    /**
     * Sets the categories of the given resource. Will remove all other categories.<p>
     *
     * @param structureId the resource structure id
     * @param categories the categories to set
     * @param callback the callback which receives the result
     */
    void setResourceCategories(CmsUUID structureId, List<String> categories, AsyncCallback<Void> callback);

    /**
     * Sets the show editor help flag.<p>
     *
     * @param showHelp the show help flag
     * @param callback the asynchronous callback
     */
    void setShowEditorHelp(boolean showHelp, AsyncCallback<Void> callback);

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     *
     * @param visible <code>true</code> if the tool-bar is visible
     * @param callback the call-back executed on response
     */
    void setToolbarVisible(boolean visible, AsyncCallback<Void> callback);

    /**
     * Unlocks the given resource.<p>
     *
     * @param structureId the resource structure id
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void unlock(CmsUUID structureId, AsyncCallback<String> callback);

    /**
     * Unlocks the given resource.<p>
     *
     * @param rootPath the resource root path
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void unlock(String rootPath, AsyncCallback<String> callback);

    /**
     * Performs a batch of validations and returns the results.<p>
     *
     * @param validationQueries a map from field names to validation queries
     * @param callback the asynchronous callback
     */
    void validate(
        Map<String, CmsValidationQuery> validationQueries,
        AsyncCallback<Map<String, CmsValidationResult>> callback);

    /**
     * Performs a batch of validations using a custom form validator class.<p>
     *
     * @param formValidatorClass the class name of the form validator
     * @param validationQueries a map from field names to validation queries
     * @param values the map of all field values
     * @param config the form validator configuration string
     * @param callback the asynchronous callback
     */
    void validate(
        String formValidatorClass,
        Map<String, CmsValidationQuery> validationQueries,
        Map<String, String> values,
        String config,
        AsyncCallback<Map<String, CmsValidationResult>> callback);

}
