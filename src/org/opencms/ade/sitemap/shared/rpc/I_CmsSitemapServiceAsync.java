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

package org.opencms.ade.sitemap.shared.rpc;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasInitialFetchResult;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationReply;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationRequest;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/** 
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsVfsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public interface I_CmsSitemapServiceAsync {

    /** 
     * Sets the name and title of the given category.<p>
     * 
     * @param entryPoint the current entry point 
     * @param id the category id 
     * @param title the new title 
     * @param name the new name
     * @param callback the callback to call when done  
     */
    void changeCategory(String entryPoint, CmsUUID id, String title, String name, AsyncCallback<Void> callback);

    /**
     * Creates a new category.<p>
     * 
     * @param entryPoint the entry point 
     * @param id the parent category id 
     * @param title the title 
     * @param name the category name
     * @param callback the result callback 
     */
    void createCategory(String entryPoint, CmsUUID id, String title, String name, AsyncCallback<Void> callback);

    /**
     * Creates a new gallery folder.<p>
     * 
     * @param parentFolder the parent folder path
     * @param title the title property
     * @param folderTypeId the resource type id
     * @param callback the async callback
     */
    void createNewGalleryFolder(
        String parentFolder,
        String title,
        int folderTypeId,
        AsyncCallback<CmsGalleryFolderEntry> callback);

    /**
     * Creates a new model page.<p>
     * 
     * @param entryPointUri the uri of the entry point 
     * @param title the title for the model page 
     * @param description the description for the model page 
     * @param copyId the structure id of the resource to copy to create a new model page; if null, the model page is created as an empty container page
     * @param resultCallback the callback for the result 
     */
    void createNewModelPage(
        String entryPointUri,
        String title,
        String description,
        CmsUUID copyId,
        AsyncCallback<CmsModelPageEntry> resultCallback);

    /**
     * Creates a sub-sitemap of the given sitemap starting from the given entry.<p>
     * 
     * @param entryId the structure id of the sitemap entry to create a sub sitemap of
     * @param callback the async callback  
     */
    void createSubSitemap(CmsUUID entryId, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Gets the alias import results from the server.<p>
     * 
     * @param resultKey the key which identifies the alias import results to get 
     * @param asyncCallback the asynchronous callback  
     */
    void getAliasImportResult(String resultKey, AsyncCallback<List<CmsAliasImportResult>> asyncCallback);

    /**
     * Gets the initial data for the bulk alias editor.<p>
     * 
     * @param callback the asynchronous callback  
     */
    void getAliasTable(AsyncCallback<CmsAliasInitialFetchResult> callback);

    /**
     * Gets the category data for the given entry point.<p>
     * 
     * @param entryPoint the entry point
     * @param resultCallback the callback for the result  
     **/
    void getCategoryData(String entryPoint, AsyncCallback<CmsSitemapCategoryData> resultCallback);

    /**
     * Returns the sitemap children for the given path.<p>
     * 
     * @param entryPointUri the URI of the sitemap entry point
     * @param entryId the entry id
     * @param levels the count of child levels to read
     * @param callback the async callback
     */
    void getChildren(String entryPointUri, CmsUUID entryId, int levels, AsyncCallback<CmsClientSitemapEntry> callback);

    /**
     * Returns the gallery data to this sub site.<p>
     * 
     * @param entryPointUri the sub site folder
     * @param callback the async callback
     */
    void getGalleryData(String entryPointUri, AsyncCallback<Map<CmsGalleryType, List<CmsGalleryFolderEntry>>> callback);

    /** 
     * Gets the model pages for the given structure id of the sitemap root folder.<p>
     * 
     * @param id structure id of a folder
     * @param callback the callback for the result  
     */
    void getModelPages(CmsUUID id, AsyncCallback<List<CmsModelPageEntry>> callback);

    /** 
     * Loads the model page data for the "add" menu.<p>
     * 
     * @param entryPointUri the entry point uri
     * @param resultCallback the callback for the result  
     */
    void getNewElementInfo(String entryPointUri, AsyncCallback<List<CmsNewResourceInfo>> resultCallback);

    /**
     * Merges a sub-sitemap into it's parent sitemap.<p>
     * 
     * @param entryPoint the sitemap entry point
     * @param subSitemapId the structure id of the sub sitemap folder
     * 
     * @param callback the async callback
     */
    void mergeSubSitemap(String entryPoint, CmsUUID subSitemapId, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Returns the initialization data for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     * @param callback the async callback
     */
    void prefetch(String sitemapUri, AsyncCallback<CmsSitemapData> callback);

    /**
     * Removes a model page from the current sitemap configuration.<p>
     * 
     * @param baseUri the base uri for the current sitemap 
     * @param modelPageId structure id of the model page to remove
     * @param callback the callback
     */
    void removeModelPage(String baseUri, CmsUUID modelPageId, AsyncCallback<Void> callback);

    /**
     * Saves the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * @param callback the async callback
     */
    void save(String sitemapUri, CmsSitemapChange change, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Saves the aliases for the bulk alias editor.<p>
     *  
     * @param saveRequest the object containing the data to save
     * @param callback the asynchronous callback  
     */
    void saveAliases(CmsAliasSaveValidationRequest saveRequest, AsyncCallback<CmsAliasEditValidationReply> callback);

    /**
     * Save the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void saveSync(String sitemapUri, CmsSitemapChange change, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Updates the alias editor status.<p>
     * 
     * This is used to keep two users from editing the alias table for a site root concurrently.<p>
     * 
     * @param editing true to indicate that the table is still being edited, false to indicate that the table isn't being edited anymore
     * @param callback the asynchronous callback 
     */
    void updateAliasEditorStatus(boolean editing, AsyncCallback<Void> callback);

    /**
     * Validates the aliases for the bulk alias editor.<p>
     * 
     * @param validationRequest an object indicating the type of validation to perform 
     * @param callback the asynchronous callback 
     */
    void validateAliases(
        CmsAliasEditValidationRequest validationRequest,
        AsyncCallback<CmsAliasEditValidationReply> callback);

    /**
    * Validates rewrite aliases.<p>
    * 
    * @param validationRequest the rewrite alias data to validate
    *  
    * @param callback the callback for the result 
    */
    void validateRewriteAliases(
        CmsRewriteAliasValidationRequest validationRequest,
        AsyncCallback<CmsRewriteAliasValidationReply> callback);

}
