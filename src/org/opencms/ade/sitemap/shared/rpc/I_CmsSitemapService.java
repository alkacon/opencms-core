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

package org.opencms.ade.sitemap.shared.rpc;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsLocaleComparePropertyData;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasInitialFetchResult;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationReply;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationRequest;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
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
public interface I_CmsSitemapService extends RemoteService {

    /**
     * Sets the name and title of the given category.<p>
     *
     * @param entryPoint the current entry point
     * @param id the category id
     * @param title the new title
     * @param name the new name
     * @throws CmsRpcException if something goes wrong
     */
    void changeCategory(String entryPoint, CmsUUID id, String title, String name) throws CmsRpcException;

    /**
     * Creates a new category.<p>
     *
     * @param entryPoint the entry point
     * @param id the parent category id
     * @param title the title
     * @param name the category name
     *
     * @throws CmsRpcException if something goes wrong
     */
    void createCategory(String entryPoint, CmsUUID id, String title, String name) throws CmsRpcException;

    /**
     * Creates a new gallery folder.<p>
     *
     * @param parentFolder the parent folder path
     * @param title the title property
     * @param folderTypeId the resource type id
     *
     * @return the new gallery folder data
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryFolderEntry createNewGalleryFolder(String parentFolder, String title, int folderTypeId)
    throws CmsRpcException;

    /**
     * Creates a new model page.<p>
     *
     * @param entryPointUri the uri of the entry point
     * @param title the title for the model page
     * @param description the description for the model page
     * @param copyId the structure id of the resource to copy to create a new model page; if null, the model page is created as an empty container page
     * @param isModelGroup <code>true</code> to create a new model group page
     *
     * @return a bean representing the created model page
     * @throws CmsRpcException if something goes wrong
     */
    CmsModelPageEntry createNewModelPage(
        String entryPointUri,
        String title,
        String description,
        CmsUUID copyId,
        boolean isModelGroup)
    throws CmsRpcException;

    /**
     * Creates a sub-sitemap of the given sitemap starting from the given entry.<p>
     *
     * @param entryId the structure id of the sitemap entry to create a sub sitemap of
     *
     * @return the sub-sitemap creation result
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsSitemapChange createSubSitemap(CmsUUID entryId) throws CmsRpcException;

    /**
     * Sets the disabled flag of a model page within the current sitemap configuration.<p>
     *
     * @param baseUri the base uri for the current sitemap
     * @param modelPageId structure id of the model page to remove
     * @param disabled the disabled flag
     *
     * @throws CmsRpcException if something goes wrong
     */
    void disableModelPage(String baseUri, CmsUUID modelPageId, boolean disabled) throws CmsRpcException;

    /**
     * Gets the alias import results from the server.<p>
     *
     * @param resultKey the key which identifies the alias import results to get
     * @return the list of alias import results
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsAliasImportResult> getAliasImportResult(String resultKey) throws CmsRpcException;

    /**
     * Gets the initial data for the bulk alias editor.<p>
     *
     * @return the initial data for the alias editor
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsAliasInitialFetchResult getAliasTable() throws CmsRpcException;

    /**
     * Gets the category data for the given entry point.<p>
     *
     * @param entryPoint the entry point
     * @return the category data
     *
     * @throws CmsRpcException if something goes wrong
     **/
    CmsSitemapCategoryData getCategoryData(String entryPoint) throws CmsRpcException;

    /**
     * Returns the sitemap children for the given path.<p>
     *
     * @param entryPointUri the URI of the sitemap entry point
     * @param entryId the entry id
     * @param levels the count of child levels to read
     *
     * @return the sitemap children
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsClientSitemapEntry getChildren(String entryPointUri, CmsUUID entryId, int levels) throws CmsRpcException;

    /**
     * Returns the gallery data to this sub site.<p>
     *
     * @param entryPointUri the sub site folder
     *
     * @return the gallery data to this sub site
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<CmsGalleryType, List<CmsGalleryFolderEntry>> getGalleryData(String entryPointUri) throws CmsRpcException;

    /**
     * Gets the model pages for the given structure id of the sitemap root folder.<p>
     *
     * @param rootId structure id of a folder
     * @return the model pages available in the given folder
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsModelInfo getModelInfos(CmsUUID rootId) throws CmsRpcException;

    /**
     * Loads the model page data for the "add" menu.<p>
     *
     * @param entryPointUri the entry point uri
     * @return the list of resource info beans for the model pages
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsNewResourceInfo> getNewElementInfo(String entryPointUri) throws CmsRpcException;

    /**
     * Loads the data needed by the property editor in the locale comparison view.<p>
     *
     * @param id the id of the resource for which the property editor should be opened
     * @param rootId the structure id of the resource which corresponds to the tree root in locale compare mode
     *
     * @return the property data for locale comparison mode
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsLocaleComparePropertyData loadPropertyDataForLocaleCompareView(CmsUUID id, CmsUUID rootId)
    throws CmsRpcException;

    /**
     * Merges a sub-sitemap into it's parent sitemap.<p>
     *
     * @param entryPoint the sitemap entry point
     * @param subSitemapId the structure id of the sub sitemap folder
     *
     * @return the result of the merge operation
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsSitemapChange mergeSubSitemap(String entryPoint, CmsUUID subSitemapId) throws CmsRpcException;

    /**
     * Returns the initialization data for the given sitemap.<p>
     *
     * @param sitemapUri the site relative path
     *
     * @return the initialization data
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsSitemapData prefetch(String sitemapUri) throws CmsRpcException;

    /**
     * Prepares sitemap reloading for the given sitemap root.<p>
     *
     * This method may change the currently set site root. If the given root id is not in a valid site,
     * null will be returned, otherwise the URL which the client should use to reload the sitemap will be returned.
     *
     * @param rootId the structure id of the new sitemap root
     * @param mode the new editor mode
     *
     * @return the URL which should be used to reload the sitemap
     *
     * @throws CmsRpcException if something goes wrong
     */
    String prepareReloadSitemap(CmsUUID rootId, EditorMode mode) throws CmsRpcException;

    /**
     * Removes a model page from the current sitemap configuration.<p>
     *
     * @param baseUri the base uri for the current sitemap
     * @param modelPageId structure id of the model page to remove
     *
     * @throws CmsRpcException if something goes wrong
     */
    void removeModelPage(String baseUri, CmsUUID modelPageId) throws CmsRpcException;

    /**
     * Saves the change to the given sitemap.<p>
     *
     * @param sitemapUri the sitemap URI
     * @param change the change to save
     *
     * @return the updated change
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsSitemapChange save(String sitemapUri, CmsSitemapChange change) throws CmsRpcException;

    /**
     * Saves the aliases for the bulk alias editor.<p>
     *
     * @param saveRequest the object containing the data to save
     * @return the result of saving the aliases
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsAliasEditValidationReply saveAliases(CmsAliasSaveValidationRequest saveRequest) throws CmsRpcException;

    /**
     * Saves the changed properties in locale comparison mode.<p>
     *
     * @param id the structure id
     * @param newUrlName the new URL name
     * @param propertyChanges the property changes
     * @param editedName true if the name was edited
     *
     * @throws CmsRpcException if something goes wrong
     */
    void savePropertiesForLocaleCompareMode(
        CmsUUID id,
        String newUrlName,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName)
    throws CmsRpcException;

    /**
     * Saves the change to the given sitemap.<p>
     *
     * @param sitemapUri the sitemap URI
     * @param change the change to save
     *
     * @return the updated change
     *
     * @throws CmsRpcException if something goes wrong
     */
    @SynchronizedRpcRequest
    CmsSitemapChange saveSync(String sitemapUri, CmsSitemapChange change) throws CmsRpcException;

    /**
     * Sets the default model page for the given sub-sitemap, returns the updated model info.<p>
     *
     * @param rootId structure id of the sub-sitemap root folder
     * @param modelId the id of the model page to set as default
     *
     * @return the updated model info
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsModelInfo setDefaultModel(CmsUUID rootId, CmsUUID modelId) throws CmsRpcException;

    /**
     * Sets the editor mode in the current user session.<p>
     *
     * @param editorMode the editor mode to set
     *
     * @throws CmsRpcException in case something goes wrong
     */
    void setEditorMode(EditorMode editorMode) throws CmsRpcException;

    /**
     * Updates the alias editor status.<p>
     *
     * This is used to keep two users from editing the alias table for a site root concurrently.<p>
     *
     * @param editing true to indicate that the table is still being edited, false to indicate that the table isn't being edited anymore
     *
     * @throws CmsRpcException if something goes wrong
     */
    void updateAliasEditorStatus(boolean editing) throws CmsRpcException;

    /**
     * Validates the aliases for the bulk alias editor.<p>
     *
     * @param validationRequest an object indicating the type of validation to perform
     * @return the validation result
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsAliasEditValidationReply validateAliases(CmsAliasEditValidationRequest validationRequest) throws CmsRpcException;

    /**
     * Validates rewrite aliases.<p>
     *
     * @param validationRequest the rewrite alias data to validate
     * @return the validation result
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsRewriteAliasValidationReply validateRewriteAliases(CmsRewriteAliasValidationRequest validationRequest)
    throws CmsRpcException;

}
