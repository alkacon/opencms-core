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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.preview.I_CmsPreviewFactory;
import org.opencms.ade.galleries.client.preview.I_CmsResourcePreview;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.CmsGalleryTreeEntry;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.gwt.shared.sort.CmsComparatorPath;
import org.opencms.gwt.shared.sort.CmsComparatorTitle;
import org.opencms.gwt.shared.sort.CmsComparatorType;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Gallery dialog controller.<p>
 * 
 * This class handles the communication between gallery dialog and the server. 
 * It contains the gallery data, but no references to the gallery dialog widget.
 * 
 * @since 8.0.0
 */
public class CmsGalleryController implements HasValueChangeHandlers<CmsGallerySearchBean> {

    /** The preview factory registration. */
    private static Map<String, I_CmsPreviewFactory> m_previewFactoryRegistration = new HashMap<String, I_CmsPreviewFactory>();

    /** The current load results call id. */
    protected int m_currentCallId;

    /** The gallery dialog bean. */
    protected CmsGalleryDataBean m_dialogBean;

    /** The gallery dialog mode. */
    protected I_CmsGalleryProviderConstants.GalleryMode m_dialogMode;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The gallery controller handler. */
    protected CmsGalleryControllerHandler m_handler;

    /** Flag to indicate that a load results request is currently running. */
    protected boolean m_loading;

    /** The gallery search object. */
    protected CmsGallerySearchBean m_searchObject;

    /** The current resource preview. */
    private I_CmsResourcePreview<?> m_currentPreview;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /** If <code>true</code> the search object is changed <code>false</code> otherwise.  */
    private boolean m_searchObjectChanged = true;

    /** The vfs service. */
    private I_CmsVfsServiceAsync m_vfsService;

    /**
     * Constructor.<p>
     * 
     * @param handler the controller handler 
     */
    public CmsGalleryController(CmsGalleryControllerHandler handler) {

        m_handler = handler;

        // get initial search for gallery
        m_searchObject = (CmsGallerySearchBean)CmsRpcPrefetcher.getSerializedObject(
            getGalleryService(),
            CmsGallerySearchBean.DICT_NAME);
        m_dialogBean = (CmsGalleryDataBean)CmsRpcPrefetcher.getSerializedObject(
            getGalleryService(),
            CmsGalleryDataBean.DICT_NAME);
        m_dialogMode = m_dialogBean.getMode();

        if (m_searchObject == null) {
            m_searchObject = new CmsGallerySearchBean();
            m_searchObject.setLocale(m_dialogBean.getLocale());
            m_searchObject.setScope(m_dialogBean.getScope());
        }
        m_handler.onInitialSearch(m_searchObject, m_dialogBean, this);

        m_eventBus = new SimpleEventBus();
        addValueChangeHandler(handler);
    }

    /**
     * Registers a preview factory for the given name.
     * 
     * @param previewProviderName the preview provider name
     * @param factory the preview factory
     */
    public static void registerPreviewFactory(String previewProviderName, I_CmsPreviewFactory factory) {

        m_previewFactoryRegistration.put(previewProviderName, factory);
    }

    /**
     * Add category to search object.<p>
     * 
     * @param categoryPath the id of the category to add
     */
    public void addCategory(String categoryPath) {

        m_searchObject.addCategory(categoryPath);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the created until date to the search object.<p>
     * 
     * @param end the created until date as long
     */
    public void addDateCreatedEnd(long end) {

        m_searchObject.setDateCreatedEnd(end);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the created since date to the search object.<p>
     * 
     * @param start the created since date as long
     */
    public void addDateCreatedStart(long start) {

        m_searchObject.setDateCreatedStart(start);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the modified until date to the search object.<p>
     * 
     * @param end the modified until date as long
     */
    public void addDateModifiedEnd(long end) {

        m_searchObject.setDateModifiedEnd(end);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the modified since date to the search object.<p>
     * 
     * @param start the modified since date as long
     */
    public void addDateModifiedStart(long start) {

        m_searchObject.setDateModifiedStart(start);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Adds a folder to the current search object.<p>
     * 
     * @param folder the folder to add
     */
    public void addFolder(String folder) {

        m_searchObject.addFolder(folder);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Add gallery to search object.<p>
     * 
     * @param galleryPath the id of the gallery to add
     */
    public void addGallery(String galleryPath) {

        m_searchObject.addGallery(galleryPath);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the locale to the search object.<p>
     * 
     * @param locale the locale to set
     */
    public void addLocale(String locale) {

        m_searchObject.setLocale(locale);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sets the search scope in the search object.<p>
     *
     * @param scope the search scope 
     */
    public void addScope(CmsGallerySearchScope scope) {

        m_searchObject.setScope(scope);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Adds the search query from the search tab.<p>
     * 
     * @param searchQuery the search query
     */
    public void addSearchQuery(String searchQuery) {

        m_searchObject.setQuery(searchQuery);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Add type to search object.<p>
     * 
     * @param resourceType the id(name?) of the resource type to add
     */
    public void addType(String resourceType) {

        m_searchObject.addType(resourceType);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsGallerySearchBean> handler) {

        return m_eventBus.addHandlerToSource(ValueChangeEvent.getType(), this, handler);
    }

    /**
     * Removes all selected categories from the search object.<p>
     */
    public void clearCategories() {

        List<String> selectedCategories = m_searchObject.getCategories();
        m_handler.onClearCategories(selectedCategories);
        m_searchObject.clearCategories();
        updateResultsTab(false);
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes all selected folders from the search object.<p>
     */
    public void clearFolders() {

        Set<String> selectedFolders = m_searchObject.getFolders();
        m_handler.onClearFolders(selectedFolders);
        m_searchObject.clearFolders();
        updateResultsTab(false);
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes all selected galleries from the search object.<p>
     */
    public void clearGalleries() {

        List<String> selectedGalleries = m_searchObject.getGalleries();
        m_handler.onClearGalleries(selectedGalleries);
        m_searchObject.clearGalleries();
        updateResultsTab(false);
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes all full text search criteria from the search object.<p>
     */
    public void clearTextSearch() {

        m_searchObject.clearFullTextSearch();
        m_handler.onClearFullTextSearch();
        updateResultsTab(false);
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes all selected types from the search object.<p>
     */
    public void clearTypes() {

        List<String> selectedTypes = m_searchObject.getTypes();
        m_handler.onClearTypes(selectedTypes);
        m_searchObject.clearTypes();
        updateResultsTab(false);
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Checks for broken links, ask for confirmation and finally deletes the given resource.<p>
     * 
     * @param resourcePath the resource path of the resource to delete
     */
    public void deleteResource(final String resourcePath) {

        CmsDeleteWarningDialog dialog = new CmsDeleteWarningDialog(resourcePath);
        Command callback = new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                updateResultsTab(false);
            }
        };
        dialog.loadAndShow(callback);
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        m_eventBus.fireEventFromSource(event, this);
    }

    /**
     * Returns the available locales.<p>
     * 
     * @return the available locales
     */
    public Map<String, String> getAvailableLocales() {

        return m_dialogBean.getLocales();
    }

    /**
     * Returns the gallery dialog mode.<p>
     *
     * @return the gallery dialog mode
     */
    public I_CmsGalleryProviderConstants.GalleryMode getDialogMode() {

        return m_dialogMode;
    }

    /**
     * Returns the search locale.<p>
     * 
     * @return the search locale
     */
    public String getSearchLocale() {

        return m_searchObject.getLocale();
    }

    /**
     * Returns the gallery search scope.<p>
     * 
     * @return the gallery search scope 
     */
    public CmsGallerySearchScope getSearchScope() {

        return m_dialogBean.getScope();
    }

    /**
     * Returns the start locale.<p>
     * 
     * @return the start locale
     */
    public String getStartLocale() {

        return m_dialogBean.getLocale();
    }

    /**
     * Retrieves the sub-folders of a given folder.<p>
     * 
     * @param folder the folder whose sub-folders should be retrieved 
     * @param callback the callback for processing the sub-folders
     */
    public void getSubFolders(final String folder, final AsyncCallback<List<CmsVfsEntryBean>> callback) {

        CmsRpcAction<List<CmsVfsEntryBean>> action = new CmsRpcAction<List<CmsVfsEntryBean>>() {

            @Override
            public void execute() {

                start(0, false);
                getGalleryService().getSubFolders(folder, this);
            }

            @Override
            protected void onResponse(List<CmsVfsEntryBean> result) {

                stop(false);
                callback.onSuccess(result);
            }

        };
        action.execute();
    }

    /**
     * Returns if resource entries in the search result are selectable.<p>
     * 
     * @return if resource entries in the search result are selectable
     */
    public boolean hasSelectResource() {

        return (m_dialogMode == GalleryMode.editor) || (m_dialogMode == GalleryMode.widget);
    }

    /**
     * Returns if a load results request is currently running.<p>
     * 
     * @return <code>true</code> if a load results request is currently running
     */
    public boolean isLoading() {

        return m_loading;
    }

    /**
     * Checks if the gallery is first opened in results tab.<p> 
     * 
     * @return true if gallery is first opened in results tab, false otherwise
     */
    public boolean isOpenInResults() {

        if (I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name().equals(m_searchObject.getTabId())) {
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code>, if the search object was manipulated by the controller
     * <code>false</code> otherwise.<p>
     *  
     * @return the search object changed flag
     */
    public boolean isSearchObjectChanged() {

        return m_searchObjectChanged;
    }

    /**
     * Checks if any search parameter are selected.<p>
     * 
     * @return <code>false</code> if any search parameter is selected, <code>true</code>
     * if there are no search parameter selected
     */
    public boolean isSearchObjectEmpty() {

        return m_searchObject.isEmpty();
    }

    /**
     * Opens the preview for the given resource by the given resource type.<p>
     * 
     * @param resourcePath the resource path
     * @param resourceType the resource type name
     */
    public void openPreview(String resourcePath, String resourceType) {

        if (m_currentPreview != null) {
            m_currentPreview.removePreview();
        }
        String provider = getProviderName(resourceType);
        if (m_previewFactoryRegistration.containsKey(provider)) {
            m_currentPreview = m_previewFactoryRegistration.get(provider).getPreview(m_handler.m_galleryDialog);
            m_currentPreview.openPreview(resourcePath);
            m_handler.hideShowPreviewButton(false);
        } else {
            CmsDebugLog.getInstance().printLine(
                "Preview provider \"" + provider + "\" has not been registered properly.");
        }
    }

    /**
     * Remove the category from the search object.<p>
     * 
     * @param categoryPath the category path as id
     */
    public void removeCategory(String categoryPath) {

        m_searchObject.removeCategory(categoryPath);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes a folder from the current search object.<p>
     * 
     * @param folder the folder to remove 
     */
    public void removeFolder(String folder) {

        m_searchObject.removeFolder(folder);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Remove the gallery from the search object.<p>
     * 
     * @param galleryPath the gallery path as id
     */
    public void removeGallery(String galleryPath) {

        m_searchObject.removeGallery(galleryPath);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Remove the type from the search object.<p>
     * 
     * @param resourceType the resource type as id
     */
    public void removeType(String resourceType) {

        m_searchObject.removeType(resourceType);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Selects the given resource and sets its path into the xml-content field or editor link.<p>
     * 
     * @param resourcePath the resource path
     * @param title the resource title
     * @param resourceType the resource type
     */
    public void selectResource(String resourcePath, String title, String resourceType) {

        String provider = getProviderName(resourceType);
        if (m_previewFactoryRegistration.containsKey(provider)) {
            m_previewFactoryRegistration.get(provider).getPreview(m_handler.m_galleryDialog).selectResource(
                resourcePath,
                title);

        } else {
            CmsDebugLog.getInstance().printLine("No provider available");
        }
    }

    /**
     * Selects the result tab.<p>
     */
    public void selectResultTab() {

        m_handler.selectResultTab();
    }

    /**
     * Sets the controller handler for gallery dialog.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(CmsGalleryControllerHandler handler) {

        m_handler = handler;
    }

    /**
     * Sets the search object changed flag to <code>true</code>.<p>
     */
    public void setSearchObjectChanged() {

        m_searchObjectChanged = true;
    }

    /**
     * Sets if the search should include expired or unreleased resources.<p>
     * 
     * @param includeExpired if the search should include expired or unreleased resources
     */
    public void setIncludeExpired(boolean includeExpired) {

        m_searchObject.setIncludeExpired(includeExpired);
        m_searchObjectChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Sorts the categories according to given parameters and updates the list.<p>
     * 
     * @param sortParams the sort parameters
     * @param filter the filter to apply before sorting
     */
    public void sortCategories(String sortParams, String filter) {

        List<CmsCategoryBean> categories;
        SortParams sort = SortParams.valueOf(sortParams);
        switch (sort) {
            case tree:
                m_handler.onUpdateCategoriesTree(m_dialogBean.getCategories(), m_searchObject.getCategories());
                break;
            case title_asc:
                categories = getFilteredCategories(filter);
                Collections.sort(categories, new CmsComparatorTitle(true));
                m_handler.onUpdateCategoriesList(categories, m_searchObject.getCategories());
                break;
            case title_desc:
                categories = getFilteredCategories(filter);
                Collections.sort(categories, new CmsComparatorTitle(false));
                m_handler.onUpdateCategoriesList(categories, m_searchObject.getCategories());
                break;
            case type_asc:
            case type_desc:
            case path_asc:
            case path_desc:
            case dateLastModified_asc:
            case dateLastModified_desc:

            default:
        }
    }

    /**
     * Sorts the galleries according to given parameters and updates the list.<p>
     * 
     * @param sortParams the sort parameters
     * @param filter the filter to apply before sorting
     */
    public void sortGalleries(String sortParams, String filter) {

        List<CmsGalleryFolderBean> galleries;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            galleries = new ArrayList<CmsGalleryFolderBean>();
            for (CmsGalleryFolderBean galleryBean : m_dialogBean.getGalleries()) {
                if (galleryBean.matchesFilter(filter)) {
                    galleries.add(galleryBean);
                }
            }
        } else {
            galleries = m_dialogBean.getGalleries();
        }
        SortParams sort = SortParams.valueOf(sortParams);
        switch (sort) {
            case title_asc:
                Collections.sort(galleries, new CmsComparatorTitle(true));
                break;
            case title_desc:
                Collections.sort(galleries, new CmsComparatorTitle(false));
                break;
            case type_asc:
                Collections.sort(galleries, new CmsComparatorType(true));
                break;
            case type_desc:
                Collections.sort(galleries, new CmsComparatorType(false));
                break;
            case path_asc:
                Collections.sort(galleries, new CmsComparatorPath(true));
                break;
            case path_desc:
                Collections.sort(galleries, new CmsComparatorPath(false));
                break;
            case tree:
                m_handler.onUpdateGalleryTree(galleryListToTree(galleries), m_searchObject.getGalleries());
                return;
            case dateLastModified_asc:
            case dateLastModified_desc:
            default:
                // not supported
                return;
        }
        m_handler.onUpdateGalleries(galleries, m_searchObject.getGalleries());
    }

    /**
     * Sorts the results according to given parameters and updates the list.<p>
     * 
     * @param sortParams the sort parameters
     */
    public void sortResults(final String sortParams) {

        m_searchObject.setSortOrder(sortParams);
        updateResultsTab(false);
    }

    /**
     * Sorts the types according to given parameters and updates the list.<p>
     * 
     * @param sortParams the sort parameters
     */
    public void sortTypes(String sortParams) {

        List<CmsResourceTypeBean> types = m_dialogBean.getTypes();
        SortParams sort = SortParams.valueOf(sortParams);
        switch (sort) {
            case title_asc:
                Collections.sort(types, new CmsComparatorTitle(true));
                break;
            case title_desc:
                Collections.sort(types, new CmsComparatorTitle(false));
                break;
            case type_asc:
                Collections.sort(types, new CmsComparatorType(true));
                break;
            case type_desc:
                Collections.sort(types, new CmsComparatorType(false));
                break;
            case dateLastModified_asc:
            case dateLastModified_desc:
            case path_asc:
            case path_desc:
            case tree:
            default:
                // not supported
                return;
        }
        m_handler.onUpdateTypes(types, m_searchObject.getTypes());
    }

    /**
     * Updates the content of the categories tab.<p>
     */
    public void updateCategoriesTab() {

        if (m_dialogBean.getCategories() == null) {
            loadCategories();
        } else {
            m_handler.onCategoriesTabSelection();
        }
    }

    /**
     * Updates the content of the galleries(folders) tab.<p>
     */
    public void updateGalleriesTab() {

        if (m_dialogBean.getGalleries() == null) {
            loadGalleries();
        } else {
            m_handler.onGalleriesTabSelection();
        }
    }

    /**
     * Updates the content of the results tab.<p>
     * 
     * @param isNextPage signals if the next page should be loaded
     */
    public void updateResultsTab(final boolean isNextPage) {

        // if the RPC call will be sent the search object is in a unchanged state
        m_searchObjectChanged = false;

        if (m_searchObject.isEmpty()) {
            // don't search: notify the user that at least one search criteria should be selected
            m_handler.showNoParamsMessage();
        } else {
            // perform the search

            /** The RPC search action for the gallery dialog. */
            CmsRpcAction<CmsGallerySearchBean> searchAction = new CmsRpcAction<CmsGallerySearchBean>() {

                private int m_callId;

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                */
                @Override
                public void execute() {

                    m_currentCallId++;
                    m_callId = m_currentCallId;
                    m_loading = true;
                    CmsGallerySearchBean preparedObject = prepareSearchObject();
                    if (isNextPage) {
                        preparedObject.setPage(preparedObject.getLastPage() + 1);
                    } else {
                        preparedObject.setPage(1);
                    }
                    getGalleryService().getSearch(preparedObject, this);
                }

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                */
                @Override
                public void onResponse(CmsGallerySearchBean searchObj) {

                    if (m_callId != m_currentCallId) {
                        return;
                    }
                    if (!isNextPage) {
                        m_handler.hideShowPreviewButton(true);
                    }
                    m_loading = false;
                    m_searchObject.setResults(searchObj.getResults());
                    m_searchObject.setResultCount(searchObj.getResultCount());
                    m_searchObject.setSortOrder(searchObj.getSortOrder());
                    m_searchObject.setPage(searchObj.getPage());
                    m_searchObject.setLastPage(searchObj.getLastPage());
                    m_handler.onResultTabSelection(m_searchObject);
                }
            };
            searchAction.execute();
        }
    }

    /**
     * Updates the content of the types tab.<p>
     */
    public void updatesTypesTab() {

        m_handler.onTypesTabSelection();
    }

    /**
     * Returns the gallery service instance.<p>
     * 
     * @return the gallery service instance
     */
    protected I_CmsGalleryServiceAsync getGalleryService() {

        if (m_gallerySvc == null) {
            m_gallerySvc = GWT.create(I_CmsGalleryService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.galleries.CmsGalleryService.gwt");
            ((ServiceDefTarget)m_gallerySvc).setServiceEntryPoint(serviceUrl);
        }
        return m_gallerySvc;
    }

    /** 
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    protected I_CmsVfsServiceAsync getVfsService() {

        if (m_vfsService == null) {
            m_vfsService = CmsCoreProvider.getVfsService();
        }
        return m_vfsService;
    }

    /**
     * @param resourcePath
     */
    protected void internalDeleteResource(final String resourcePath) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(0, false);
                getGalleryService().deleteResource(resourcePath, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                updateResultsTab(false);
            }
        };
        action.execute();
    }

    /**
     * Returns a consistent search object to be used for the search.<p>
     * 
     * For the search at least one resource type should be provided.
     * The corresponding resource types will be added to the search object, if no or only gallery folder are selected.
     * 
     * @return the search object
     */
    CmsGallerySearchBean prepareSearchObject() {

        CmsGallerySearchBean preparedSearchObj = new CmsGallerySearchBean(m_searchObject);
        preparedSearchObj.setReferencePath(m_dialogBean.getReferenceSitePath());
        // add the available types to the search object used for next search, 
        // if the criteria for types are empty
        if (CmsCollectionUtil.isEmptyOrNull(m_searchObject.getTypes())) {
            // no galleries is selected, provide all available types
            if (CmsCollectionUtil.isEmptyOrNull(m_searchObject.getGalleries())) {
                // additionally provide all available gallery folders if in 'widget' and 'editor' dialog-mode and no folder has been selected 
                if (((m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.widget) || (m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.editor))
                    && CmsCollectionUtil.isEmptyOrNull(m_searchObject.getFolders())) {
                    ArrayList<String> availableGalleries = new ArrayList<String>();
                    for (CmsGalleryFolderBean galleryPath : m_dialogBean.getGalleries()) {
                        availableGalleries.add(galleryPath.getPath());
                    }
                    preparedSearchObj.setGalleries(availableGalleries);
                }
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                    availableTypes.add(type.getType());
                }
                preparedSearchObj.setTypes(availableTypes);
                // at least one gallery is selected 
            } else {

                // get the resource types associated with the selected galleries
                HashSet<String> galleryTypes = new HashSet<String>();
                for (CmsGalleryFolderBean gallery : m_dialogBean.getGalleries()) {
                    if (m_searchObject.getGalleries().contains(gallery.getPath())) {
                        galleryTypes.addAll(gallery.getContentTypes());
                    }
                }

                HashSet<String> availableTypes = new HashSet<String>();
                for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                    availableTypes.add(type.getType());
                }

                preparedSearchObj.setTypes(new ArrayList<String>(CmsCollectionUtil.intersection(
                    availableTypes,
                    galleryTypes)));
            }
        }
        return preparedSearchObj;

    }

    /**
     * Converts categories tree to a list of info beans.<p>
     * 
     * @param categoryList the category list
     * @param entries the tree entries
     */
    private void categoryTreeToList(List<CmsCategoryBean> categoryList, List<CmsCategoryTreeEntry> entries) {

        if (entries == null) {
            return;
        }
        // skipping the root tree entry where the path property is empty
        for (CmsCategoryTreeEntry entry : entries) {
            CmsCategoryBean bean = new CmsCategoryBean(entry);
            categoryList.add(bean);
            categoryTreeToList(categoryList, entry.getChildren());
        }
    }

    /**
     * Creates a tree structure from the given gallery folder list.<p>
     * The tree may have several entries at root level.<p>
     * 
     * @param galleries the gallery folder list
     * 
     * @return the list of tree entries
     */
    private List<CmsGalleryTreeEntry> galleryListToTree(List<CmsGalleryFolderBean> galleries) {

        List<CmsGalleryTreeEntry> result = new ArrayList<CmsGalleryTreeEntry>();
        Collections.sort(galleries, new CmsComparatorPath(true));
        CmsGalleryTreeEntry previous = null;
        for (CmsGalleryFolderBean folderBean : galleries) {
            CmsGalleryTreeEntry current = new CmsGalleryTreeEntry(folderBean);
            CmsGalleryTreeEntry parent = null;
            if (previous != null) {
                parent = lookForParent(previous, current.getPath());
            }
            if (parent != null) {
                parent.addChild(current);
            } else {
                result.add(current);
            }
            previous = current;
        }
        return result;
    }

    /**
     * Gets the list of categories.<p>
     * 
     * @return a list of category beans 
     */
    private List<CmsCategoryBean> getCategoryList() {

        List<CmsCategoryBean> result = new ArrayList<CmsCategoryBean>();
        categoryTreeToList(result, m_dialogBean.getCategories());
        return result;
    }

    /**
     * Gets the filtered list of categories.<p>
     * 
     * @param filter the search string to use for filtering 
     * 
     * @return the filtered category beans 
     */
    private List<CmsCategoryBean> getFilteredCategories(String filter) {

        List<CmsCategoryBean> result;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            result = new ArrayList<CmsCategoryBean>();
            for (CmsCategoryBean category : getCategoryList()) {
                if (category.matchesFilter(filter)) {
                    result.add(category);
                }
            }
        } else {
            result = getCategoryList();
        }
        return result;
    }

    /**
     * Returns the preview provider name for the given resource type, or <code>null</code> if none available.<p>
     * 
     * @param resourceType the resource type
     * 
     * @return the preview provider name
     */
    private String getProviderName(String resourceType) {

        for (CmsResourceTypeBean typeBean : m_dialogBean.getTypes()) {
            if (typeBean.getType().equals(resourceType)) {
                return typeBean.getPreviewProviderName();
            }
        }
        return null;
    }

    /**
     * Loading all available categories.<p>
     */
    private void loadCategories() {

        CmsRpcAction<List<CmsCategoryTreeEntry>> action = new CmsRpcAction<List<CmsCategoryTreeEntry>>() {

            @Override
            public void execute() {

                CmsCoreProvider.getService().getCategoriesForSitePath(m_dialogBean.getReferenceSitePath(), this);
            }

            @Override
            protected void onResponse(List<CmsCategoryTreeEntry> result) {

                m_dialogBean.setCategories(result);
                m_handler.setCategoriesTabContent(result);
                m_handler.onCategoriesTabSelection();
            }
        };
        action.execute();
    }

    /**
     * Loading all available galleries.<p>
     */
    private void loadGalleries() {

        CmsRpcAction<List<CmsGalleryFolderBean>> action = new CmsRpcAction<List<CmsGalleryFolderBean>>() {

            @Override
            public void execute() {

                List<String> types = new ArrayList<String>();
                for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                    types.add(type.getType());
                }

                getGalleryService().getGalleries(types, this);
            }

            @Override
            protected void onResponse(List<CmsGalleryFolderBean> result) {

                m_dialogBean.setGalleries(result);
                m_handler.setGalleriesTabContent(result, m_searchObject.getGalleries());
                m_handler.onGalleriesTabSelection();
            }
        };
        action.execute();
    }

    /**
     * Looks for an ancestor tree entry for the given path.<p>
     * 
     * @param possibleParent the possible parent entry
     * @param targetPath the target path
     * 
     * @return the parent entry or <code>null</code> if there is none
     */
    private CmsGalleryTreeEntry lookForParent(CmsGalleryTreeEntry possibleParent, String targetPath) {

        if (targetPath.startsWith(possibleParent.getPath())) {
            return possibleParent;
        }
        if (possibleParent.getParent() != null) {
            return lookForParent(possibleParent.getParent(), targetPath);
        }
        return null;
    }
}