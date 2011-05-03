/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/rpc/Attic/I_CmsCoreService.java,v $
 * Date   : $Date: 2011/05/03 10:17:09 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Provides general core services.<p>
 * 
 * @author Michael Moossen
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public interface I_CmsCoreService extends RemoteService {

    /** A constant that signals that we are in the container page context. */
    String CONTEXT_CONTAINERPAGE = "containerpage";

    /** A constant that signals that we are in the sitemap context. */
    String CONTEXT_SITEMAP = "sitemap";

    /**
    * Creates a new UUID.<p>
    * 
    * @return the created UUID
    * 
    * @throws CmsRpcException if something goes wrong 
    */
    CmsUUID createUUID() throws CmsRpcException;

    /**
     * Returns the categories for the given search parameters.<p>
     * 
     * @param fromCatPath the category path to start with, can be <code>null</code> or empty to use the root
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param refVfsPaths the reference paths, can be <code>null</code> to only use the system repository
     * 
     * @return the resource categories
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCategoryTreeEntry getCategories(String fromCatPath, boolean includeSubCats, List<String> refVfsPaths)
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
    CmsCategoryTreeEntry getCategoriesForSitePath(String sitePath) throws CmsRpcException;

    /**
     * Returns the context menu entries for the given URI.<p>
     * 
     * @param uri the current requested URI
     * @param context the ade context (sitemap or containerpage)
     * 
     * @return the context menu entries 
     * 
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsContextMenuEntryBean> getContextMenuEntries(String uri, AdeContext context) throws CmsRpcException;

    /**
     * Gets the resource state for a resource with a given path.<p>
     * 
     * @param path the resource path
     *  
     * @return the resource state of the resource 
     * @throws CmsRpcException
     */
    CmsResourceState getResourceState(String path) throws CmsRpcException;

    /**
     * Locks the given resource.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String lock(String uri) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String lockTemp(String uri) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock additionally checking that 
     * the given resource has not been modified after the given timestamp.<p>
     * 
     * @param uri the resource URI 
     * @param modification the timestamp to check
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String lockTempAndCheckModification(String uri, long modification) throws CmsRpcException;

    /**
     * Generates core data for prefetching in the host page.<p>
     * 
     * @return the core data
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCoreData prefetch() throws CmsRpcException;

    /**
     * Applies the changes stored in the info bean to the vfs of OpenCms.<p>
     * 
     * @param structureId the structure id of the modified resource
     * @param bean the bean with the information of the dialog
     * 
     * @throws CmsRpcException if the RPC call goes wrong 
     */
    void setAvailabilityInfo(CmsUUID structureId, CmsAvailabilityInfoBean bean) throws CmsRpcException;

    /**
     * Applies the changes stored in the info bean to the vfs of OpenCms.<p>
     * 
     * @param vfsPath the vfs path of the modified resource
     * @param bean the bean with the information of the dialog
     * 
     * @throws CmsRpcException if the RPC call goes wrong 
     */
    void setAvailabilityInfo(String vfsPath, CmsAvailabilityInfoBean bean) throws CmsRpcException;

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     * 
     * @param visible <code>true</code> if the tool-bar is visible
     * 
     * @throws CmsRpcException
     */
    void setToolbarVisible(boolean visible) throws CmsRpcException;

    /**
     * Translates an URL name of a sitemap entry to a valid form containing no illegal characters.<p>
     * 
     * @param urlName the url name to be translated
     *  
     * @return the translated URL name
     *  
     * @throws CmsRpcException if something goes wrong 
     */
    String translateUrlName(String urlName) throws CmsRpcException;

    /**
     * Unlocks the given resource.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String unlock(String uri) throws CmsRpcException;

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
        String config) throws CmsRpcException;
}
