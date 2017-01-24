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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.preview.I_CmsPreviewFactory;
import org.opencms.ade.galleries.client.preview.I_CmsResourcePreview;
import org.opencms.ade.galleries.client.ui.CmsSearchTab.ParamType;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.CmsGalleryTreeEntry;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean.TypeVisibility;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsBinaryPreviewProvider;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.util.CmsClientCollectionUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.gwt.shared.sort.CmsComparatorPath;
import org.opencms.gwt.shared.sort.CmsComparatorTitle;
import org.opencms.gwt.shared.sort.CmsComparatorType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
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

    /** The gallery service instance. */
    private static I_CmsGalleryServiceAsync m_gallerySvc;

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

    /** The gallery configuration. */
    private I_CmsGalleryConfiguration m_configuration;

    /** The current resource preview. */
    private I_CmsResourcePreview<?> m_currentPreview;

    /** Flag to record whether the user changed the gallery selection. */
    private boolean m_galleriesChanged;

    /** Flag which controls whether the galleries in the gallery tab should be selectable. */
    private boolean m_galleriesSelectable;

    /** Flag which indicates whether the site selector should be shown. */
    private boolean m_isShowSiteSelector = true;

    /** Flag which controls whether the results should be selectable. If this is false, the results will not be selectable, but if it is true, the results may still be unselectable for a different reason. */
    private boolean m_resultsSelectable = true;

    /** If <code>true</code> the search object is changed <code>false</code> otherwise.  */
    private boolean m_searchObjectChanged = true;

    /** The search able resource types. */
    private List<CmsResourceTypeBean> m_searchTypes;

    /** The start site to set for the site selector. */
    private String m_startSite;

    /** The configured tabs. */
    private GalleryTabId[] m_tabIds;

    /** The tree token for this gallery instance (determines which tree open state to use). */
    private String m_treeToken;

    /** The vfs service. */
    private I_CmsVfsServiceAsync m_vfsService;

    /**
     * Constructor.<p>
     *
     * @param handler the controller handler
     * @param dialogBean the gallery data
     * @param searchBean the prefetched search
     */
    public CmsGalleryController(
        CmsGalleryControllerHandler handler,
        CmsGalleryDataBean dialogBean,
        CmsGallerySearchBean searchBean) {

        m_handler = handler;
        m_eventBus = new SimpleEventBus();
        addValueChangeHandler(m_handler);
        JavaScriptObject embeddedConfig = CmsJsUtil.getAttribute(CmsJsUtil.getWindow(), "embeddedConfiguration");
        if (embeddedConfig != null) {
            CmsGalleryConfigurationJSO config = embeddedConfig.cast();
            m_handler.m_galleryDialog.setOverrideFormats(true);
            m_handler.m_galleryDialog.setUseFormats(config.isUseFormats());
            m_handler.m_galleryDialog.setImageFormats(config.getImageFormats());
            m_handler.m_galleryDialog.setImageFormatNames(config.getImageFormatNames());
        }
        m_dialogBean = dialogBean;
        m_searchObject = searchBean;
        m_dialogMode = m_dialogBean.getMode();

        if (m_searchObject == null) {
            m_searchObject = new CmsGallerySearchBean();
            m_searchObject.setOriginalGalleryData(dialogBean);
            m_searchObject.setGalleryMode(m_dialogMode);
            m_searchObject.setIgnoreSearchExclude(m_dialogMode != GalleryMode.ade);
            m_searchObject.setLocale(m_dialogBean.getLocale());
            m_searchObject.setScope(m_dialogBean.getScope());
            m_searchObject.setSortOrder(m_dialogBean.getSortOrder().name());
            m_searchObject.setGalleryStoragePrefix(m_dialogBean.getGalleryStoragePrefix());
            if (m_dialogBean.getStartFolderFilter() != null) {
                m_searchObject.setFolders(m_dialogBean.getStartFolderFilter());
            }
        }
        if (m_dialogBean != null) {
            m_tabIds = m_dialogBean.getTabIds();
            m_handler.onInitialSearch(m_searchObject, m_dialogBean, this, true);
        }
    }

    /**
     * Constructor.<p>
     *
     * @param handler the controller handler
     * @param conf the gallery configuration
     */
    public CmsGalleryController(CmsGalleryControllerHandler handler, final I_CmsGalleryConfiguration conf) {

        m_configuration = conf;
        m_resultsSelectable = conf.isResultsSelectable();
        m_galleriesSelectable = conf.isGalleriesSelectable();
        m_handler = handler;
        m_handler.m_galleryDialog.setUseFormats(m_configuration.isUseFormats());
        m_handler.m_galleryDialog.setImageFormats(m_configuration.getImageFormats());
        m_handler.m_galleryDialog.setImageFormatNames(m_configuration.getImageFormatNames());
        m_eventBus = new SimpleEventBus();
        addValueChangeHandler(m_handler);
        m_treeToken = m_configuration.getTreeToken();
        CmsRpcAction<CmsGalleryDataBean> initAction = new CmsRpcAction<CmsGalleryDataBean>() {

            @Override
            public void execute() {

                getGalleryService().getInitialSettings(new CmsGalleryConfiguration(conf), this);
            }

            @Override
            protected void onResponse(CmsGalleryDataBean result) {

                m_dialogBean = result;
                m_dialogMode = m_dialogBean.getMode();
                if (m_dialogBean.getStartTab() != GalleryTabId.cms_tab_results) {
                    List<GalleryTabId> tabs = Arrays.asList(getTabIds());
                    // in case the selected start tab is not present, choose another one
                    if (!tabs.contains(m_dialogBean.getStartTab())) {
                        if ((m_dialogMode == GalleryMode.widget) && tabs.contains(GalleryTabId.cms_tab_vfstree)) {
                            m_dialogBean.setStartTab(GalleryTabId.cms_tab_vfstree);
                        } else {
                            m_dialogBean.setStartTab(tabs.get(0));
                        }
                    }
                }
                initialSearch();
            }
        };
        initAction.execute();
        m_tabIds = m_configuration.getTabConfiguration().getTabs().toArray(new GalleryTabId[] {});
        setShowSiteSelector(m_configuration.isShowSiteSelector());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_configuration.getStartSite())) {
            setStartSite(m_configuration.getStartSite());
        }
    }

    /**
     * Creates a gallery service instance.<p>
     *
     * @return the gallery service instance
     */
    public static I_CmsGalleryServiceAsync createGalleryService() {

        I_CmsGalleryServiceAsync service;
        service = GWT.create(I_CmsGalleryService.class);
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.galleries.CmsGalleryService.gwt");
        ((ServiceDefTarget)service).setServiceEntryPoint(serviceUrl);
        return service;
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
     * Returns the gallery service instance.<p>
     *
     * @return the gallery service instance
     */
    protected static I_CmsGalleryServiceAsync getGalleryService() {

        if (m_gallerySvc == null) {
            I_CmsGalleryServiceAsync service = createGalleryService();
            m_gallerySvc = service;
        }
        return m_gallerySvc;
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
        m_galleriesChanged = true;
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
     *
     * @param searchChanged if true, marks the search parameters as changed
     */
    public void clearFolders(boolean searchChanged) {

        Set<String> selectedFolders = m_searchObject.getFolders();
        if (searchChanged) {
            m_searchObjectChanged = true;
        }
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
     * Gets the available galleries.<p>
     *
     * @return the list of available galleries
     */
    public List<CmsGalleryFolderBean> getAvailableGalleries() {

        return m_dialogBean.getGalleries();
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
     * Gets the default search scope.<p>
     *
     * @return the default search scope
     */
    public CmsGallerySearchScope getDefaultScope() {

        return m_dialogBean.getDefaultScope();
    }

    /**
     * Gets the default site root for the sitemap tab.<p>
     *
     * @return the default site root for the sitemap tab
     */
    public String getDefaultSitemapTabSiteRoot() {

        return getDefaultSiteRoot(m_dialogBean.getSitemapSiteSelectorOptions());
    }

    /**
     * Gets the default site root for the VFS tab.<p>
     *
     * @return the default site root for the VFS tab
     */
    public String getDefaultVfsTabSiteRoot() {

        return getDefaultSiteRoot(m_dialogBean.getVfsSiteSelectorOptions());
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
     * Returns the gallery folder info to the given path.<p>
     *
     * @param galleryPath the gallery folder path
     *
     * @return the gallery folder info
     */
    public CmsGalleryFolderBean getGalleryInfo(String galleryPath) {

        CmsGalleryFolderBean result = null;
        for (CmsGalleryFolderBean folderBean : getAvailableGalleries()) {
            if (folderBean.getPath().equals(galleryPath)) {
                result = folderBean;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the option which should be preselected for the site selector, or null.<p>
     *
     * @param siteRoot the site root
     * @param options the list of options
     *
     * @return the key for the option to preselect
     */
    public String getPreselectOption(String siteRoot, List<CmsSiteSelectorOption> options) {

        if ((siteRoot == null) || options.isEmpty()) {
            return null;
        }
        for (CmsSiteSelectorOption option : options) {
            if (CmsStringUtil.joinPaths(siteRoot, "/").equals(CmsStringUtil.joinPaths(option.getSiteRoot(), "/"))) {
                return option.getSiteRoot();
            }
        }
        return options.get(0).getSiteRoot();
    }

    /**
     * Returns the result view type.<p>
     *
     * @return the result view type
     */
    public String getResultViewType() {

        return m_dialogBean.getResultViewType();
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
     * Returns the searchable resource types.<p>
     *
     * @return the searchable resource types
     */
    public List<CmsResourceTypeBean> getSearchTypes() {

        if (m_searchTypes != null) {
            return m_searchTypes;
        }
        m_searchTypes = Lists.newArrayList();
        for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
            if (type.getVisibility() != TypeVisibility.hidden) {
                m_searchTypes.add(type);
            }
        }
        return m_searchTypes;
    }

    /**
     * Returns the default value for the "show expired" check box.<p>
     *
     * @return the default value for "show expired"
     */
    public boolean getShowExpiredDefault() {

        return m_dialogBean.getIncludeExpiredDefault();
    }

    /**
     * Gets the sitemap site selector options.<p>
     *
     * @return the sitemap site selector options
     */
    public List<CmsSiteSelectorOption> getSitemapSiteSelectorOptions() {

        return m_dialogBean.getSitemapSiteSelectorOptions();
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
     * Gets the start site root.<p>
     *
     * @return the start site root
     */
    public String getStartSiteRoot() {

        return m_startSite;
    }

    /**
     * Loads the sub entries for the given path.<p>
     *
     * @param rootPath the root path
     * @param isRoot <code>true</code> if the requested entry is the root entry
     * @param filter the sitemap filter string
     * @param callback the callback to execute with the result
     */
    public void getSubEntries(
        final String rootPath,
        final boolean isRoot,
        final String filter,
        final I_CmsSimpleCallback<List<CmsSitemapEntryBean>> callback) {

        CmsRpcAction<List<CmsSitemapEntryBean>> action = new CmsRpcAction<List<CmsSitemapEntryBean>>() {

            @Override
            public void execute() {

                start(0, false);
                getGalleryService().getSubEntries(rootPath, isRoot, filter, this);
            }

            @Override
            protected void onResponse(List<CmsSitemapEntryBean> result) {

                stop(false);
                callback.execute(result);
            }

        };
        action.execute();
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
     * Returns the configured tab id's.<p>
     *
     * @return the configured tab id's
     */
    public GalleryTabId[] getTabIds() {

        if (m_tabIds == null) {
            return m_dialogMode.getTabs();
        } else {
            return m_tabIds;
        }
    }

    /**
     * Gets the tree token, which is used to determine which tree state is loaded/saved for the VFS and sitemap tabs.<p>
     *
     * @return the tree token
     */
    public String getTreeToken() {

        return m_treeToken;
    }

    /**
     * Returns the resource type info for the given resource type name.<p>
     *
     * @param typeName the resource type name
     *
     * @return the type info
     */
    public CmsResourceTypeBean getTypeInfo(String typeName) {

        CmsResourceTypeBean result = null;
        for (CmsResourceTypeBean typeBean : m_dialogBean.getTypes()) {
            if (typeBean.getType().equals(typeName)) {
                result = typeBean;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the site selector options.<p>
     *
     * @return the site selector options
     */
    public List<CmsSiteSelectorOption> getVfsSiteSelectorOptions() {

        return m_dialogBean.getVfsSiteSelectorOptions();
    }

    /**
     * Returns true if the galleries in the gallery tab should be selectable.<p>
     *
     * @return true if the galleries should be selectable
     */
    public boolean hasGalleriesSelectable() {

        return m_galleriesSelectable;
    }

    /**
     * Returns if a preview is available for the given resource type.<p>
     *
     * @param resourceType the requested resource type
     *
     * @return <code>true</code> if a preview is available for the given resource type
     */
    public boolean hasPreview(String resourceType) {

        return getProviderName(resourceType) != null;
    }

    /**
     * Returns false if the results in the result tab should not be selectable.<p>
     *
     * @return false if the results in the result tab should not be selectable
     */
    public boolean hasResultsSelectable() {

        return m_resultsSelectable;
    }

    /**
     * Returns if folders should be selectable.<p>
     *
     * @return <code>true</code> if folders should be selectable
     */
    public boolean hasSelectFolder() {

        if (hasSelectResource()) {
            for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                if (type.getType().contains(I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER)) {
                    return true;
                }
            }
        }
        return false;
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
     * Returns if files are included.<p>
     *
     * @return <code>true</code> if files are included
     */
    public boolean isIncludeFiles() {

        return (m_configuration == null) || m_configuration.isIncludeFiles();
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
     * Returns true if the site selector should be shown.<p>
     *
     * @return true if the site selector should be shown
     */
    public boolean isShowSiteSelector() {

        return m_isShowSiteSelector;
    }

    /**
     * Loads the root VFS entry bean for a given site selector option.<p>
     *
     * @param siteRoot the site root for which the VFS entry should be loaded
     * @param filter the search filter
     *
     * @param asyncCallback the callback to call with the result
     */
    public void loadVfsEntryBean(
        final String siteRoot,
        final String filter,

        final AsyncCallback<CmsVfsEntryBean> asyncCallback) {

        CmsRpcAction<CmsVfsEntryBean> action = new CmsRpcAction<CmsVfsEntryBean>() {

            @Override
            public void execute() {

                start(200, false);
                getGalleryService().loadVfsEntryBean(siteRoot, filter, this);
            }

            @Override
            public void onResponse(CmsVfsEntryBean result) {

                stop(false);
                asyncCallback.onSuccess(result);
            }

        };
        action.execute();
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
            m_handler.m_galleryDialog.useMaxDimensions();
            m_currentPreview = m_previewFactoryRegistration.get(provider).getPreview(m_handler.m_galleryDialog);
            m_currentPreview.openPreview(resourcePath, !m_resultsSelectable);
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
     * Removes the category from the search object.<p>
     *
     * @param key the category
     */
    public void removeCategoryParam(String key) {

        if (m_searchObject.getCategories().contains(key)) {
            m_handler.onClearCategories(Collections.singletonList(key));
            m_searchObject.removeCategory(key);
            updateResultsTab(false);
            ValueChangeEvent.fire(this, m_searchObject);
        }
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
     * Removes the folder from the search object.<p>
     *
     * @param key the folder
     */
    public void removeFolderParam(String key) {

        if (m_searchObject.getFolders().contains(key)) {
            m_handler.onClearFolders(Collections.singletonList(key));
            m_searchObject.removeFolder(key);
            updateResultsTab(false);
            ValueChangeEvent.fire(this, m_searchObject);
        }
    }

    /**
     * Remove the gallery from the search object.<p>
     *
     * @param galleryPath the gallery path as id
     */
    public void removeGallery(String galleryPath) {

        m_searchObject.removeGallery(galleryPath);
        m_searchObjectChanged = true;
        m_galleriesChanged = true;
        ValueChangeEvent.fire(this, m_searchObject);
    }

    /**
     * Removes a selected gallery from the search object.<p>
     *
     * @param key the gallery key
     */
    public void removeGalleryParam(String key) {

        if (m_searchObject.getGalleries().contains(key)) {
            m_galleriesChanged = true;
            m_handler.onClearGalleries(Collections.singletonList(key));
            m_searchObject.removeGallery(key);
            updateResultsTab(false);
            ValueChangeEvent.fire(this, m_searchObject);
        }
    }

    /**
     * Removes the given full text search criteria from the search object.<p>
     *
     * @param key the key of the parameter to remove
     */
    public void removeTextSearchParameter(String key) {

        try {
            ParamType type = ParamType.valueOf(key);
            switch (type) {
                case language:
                    m_searchObject.setLocale(getStartLocale());
                    break;
                case text:
                    m_searchObject.setQuery(null);
                    break;
                case expired:
                    m_searchObject.setIncludeExpired(false);
                    break;
                case creation:
                    m_searchObject.setDateCreatedEnd(-1L);
                    m_searchObject.setDateCreatedStart(-1L);
                    break;
                case modification:
                    m_searchObject.setDateModifiedEnd(-1L);
                    m_searchObject.setDateModifiedStart(-1L);
                    break;
                default:
            }
            m_handler.onRemoveSearchParam(type);
            updateResultsTab(false);
            ValueChangeEvent.fire(this, m_searchObject);
        } catch (IllegalArgumentException e) {
            // should not happen
        }
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
     * Removes the type from the search object.<p>
     *
     * @param key the type
     */
    public void removeTypeParam(String key) {

        List<String> selectedTypes = m_searchObject.getTypes();
        if (selectedTypes.contains(key)) {
            m_handler.onClearTypes(Collections.singletonList(key));
            selectedTypes.remove(key);
            updateResultsTab(false);
            ValueChangeEvent.fire(this, m_searchObject);
        }
    }

    /**
     * Saves the tree state for a given tree on the server.<p>
     *
     * @param treeName the tree name
     * @param siteRoot the site root
     * @param openItemIds the structure ids of opened items
     */
    public void saveTreeState(final String treeName, final String siteRoot, final Set<CmsUUID> openItemIds) {

        CmsRpcAction<Void> treeStateAction = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(600, false);
                getGalleryService().saveTreeOpenState(treeName, getTreeToken(), siteRoot, openItemIds, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
            }
        };
        treeStateAction.execute();

    }

    /**
     * Searches for a specific element and opens it's preview if found.<p>
     *
     * @param path the element path
     * @param nextAction the next action to execute after the search data for the element has been loaded into the gallery dialog
     */
    public void searchElement(final String path, final Runnable nextAction) {

        m_dialogBean.setCurrentElement(path);
        m_dialogBean.setStartTab(GalleryTabId.cms_tab_results);
        m_dialogBean.setTreeToken(getTreeToken());

        CmsRpcAction<CmsGallerySearchBean> searchAction = new CmsRpcAction<CmsGallerySearchBean>() {

            @Override
            public void execute() {

                start(200, true);
                getGalleryService().getSearch(m_dialogBean, this);
            }

            @Override
            protected void onResponse(CmsGallerySearchBean result) {

                stop(false);
                m_searchObject = result;
                m_handler.onInitialSearch(result, m_dialogBean, CmsGalleryController.this, false);
                if (nextAction != null) {
                    nextAction.run();
                }
            }

        };
        searchAction.execute();
    }

    /**
     * Selects the given resource and sets its path into the xml-content field or editor link.<p>
     *
     * @param resourcePath the resource path
     * @param structureId the structure id
     * @param title the resource title
     * @param resourceType the resource type
     */
    public void selectResource(String resourcePath, CmsUUID structureId, String title, String resourceType) {

        String provider = getProviderName(resourceType);
        if (provider == null) {
            // use {@link org.opencms.ade.galleries.client.preview.CmsBinaryPreviewProvider} as default to select a resource
            provider = I_CmsBinaryPreviewProvider.PREVIEW_NAME;
        }
        if (m_previewFactoryRegistration.containsKey(provider)) {
            m_previewFactoryRegistration.get(provider).getPreview(m_handler.m_galleryDialog).selectResource(
                resourcePath,
                structureId,
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
     * Sets if the search should include expired or unreleased resources.<p>
     *
     * @param includeExpired if the search should include expired or unreleased resources
     * @param fireEvent true if a change event should be fired after setting the value
     */
    public void setIncludeExpired(boolean includeExpired, boolean fireEvent) {

        m_searchObject.setIncludeExpired(includeExpired);
        m_searchObjectChanged = true;
        if (fireEvent) {
            ValueChangeEvent.fire(this, m_searchObject);
        }

    }

    /**
     * Stores the result view type.<p>
     *
     * @param resultViewType the result view type
     */
    public void setResultViewType(final String resultViewType) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                getGalleryService().saveResultViewType(resultViewType, this);
            }

            @Override
            protected void onResponse(Void result) {

                // nothing to do
            }

        };
        action.execute();
    }

    /**
     * Sets the search object changed flag to <code>true</code>.<p>
     */
    public void setSearchObjectChanged() {

        m_searchObjectChanged = true;
    }

    /**
     * Sets the "Show site selector" option.<p>
     *
     * @param isShowSiteSelector the new value for the option
     */
    public void setShowSiteSelector(boolean isShowSiteSelector) {

        m_isShowSiteSelector = isShowSiteSelector;
    }

    /**
     * Sets the start site.<p>
     *
     * @param startSite the start site
     */
    public void setStartSite(String startSite) {

        m_startSite = startSite;
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

        List<CmsResourceTypeBean> types = getSearchTypes();
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
     * Updates the size of the active tab.<p>
     */
    public void updateActiveTabSize() {

        m_handler.m_galleryDialog.updateSizes();

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
     * Updates the gallery data.<p>
     *
     * @param data the gallery data
     */
    public void updateGalleryData(CmsGalleryDataBean data) {

        m_dialogBean = data;
        m_handler.updateGalleryData(m_searchObject, data, this);
    }

    /**
     * Updates the gallery index and triggers a new search afterwards.<p>
     */
    public void updateIndex() {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getGalleryService().updateIndex(this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                updateResultsTab(false);
                m_handler.hideShowPreviewButton(true);
            }
        };
        action.execute();
    }

    /**
     * Updates the content of the results tab.<p>
     *
     * @param isNextPage signals if the next page should be loaded
     */
    public void updateResultsTab(final boolean isNextPage) {

        // if the RPC call will be sent the search object is in a unchanged state
        m_searchObjectChanged = false;
        if (!m_handler.hasResultsTab()) {
            return;
        }
        if (m_searchObject.isEmpty()) {
            // don't search: notify the user that at least one search criteria should be selected
            if (m_handler.m_galleryDialog.getResultsTab().isSelected()) {
                m_handler.showFirstTab();
            }
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

                    start(0, true);
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

                    stop(false);
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
     * Deletes a resource.<p>
     *
     * @param resourcePath the path of the resource to delete
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
     * Removes a tab id from the internal list of tab ids.<p>
     *
     * @param tabId the id of the tab to remove
     */
    protected void removeTab(GalleryTabId tabId) {

        if (m_tabIds != null) {
            List<GalleryTabId> tabs = new ArrayList<GalleryTabId>(Arrays.asList(m_tabIds));
            if (tabs.contains(tabId)) {
                m_tabIds = new GalleryTabId[tabs.size() - 1];
                tabs.remove(tabId);
                m_tabIds = tabs.toArray(new GalleryTabId[tabs.size()]);
            }
        }
    }

    /**
     * Removes the types tab from the list of configured tabs.<p>
     * This will only take effect when executed before tab initialization.<p>
     */
    protected void removeTypesTab() {

        removeTab(GalleryTabId.cms_tab_types);
    }

    /**
     * Does the initial search if not already pre-fetched.<p>
     */
    void initialSearch() {

        CmsRpcAction<CmsGallerySearchBean> searchAction = new CmsRpcAction<CmsGallerySearchBean>() {

            @Override
            public void execute() {

                start(0, true);
                m_dialogBean.setTreeToken(getTreeToken());
                getGalleryService().getSearch(m_dialogBean, this);
            }

            @Override
            protected void onResponse(CmsGallerySearchBean result) {

                stop(false);
                m_searchObject = result;
                m_handler.onInitialSearch(m_searchObject, m_dialogBean, CmsGalleryController.this, true);
            }
        };
        searchAction.execute();
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
        if (CmsClientCollectionUtil.isEmptyOrNull(m_searchObject.getTypes())) {
            // no galleries is selected, provide all available types
            if (CmsClientCollectionUtil.isEmptyOrNull(m_searchObject.getGalleries())) {
                // additionally provide all available gallery folders if in 'widget' and 'editor' dialog-mode and no folder has been selected
                if (((m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.widget)
                    || (m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.editor))
                    && CmsClientCollectionUtil.isEmptyOrNull(m_searchObject.getFolders())) {
                    ArrayList<String> availableGalleries = new ArrayList<String>();
                    for (CmsGalleryFolderBean galleryPath : m_dialogBean.getGalleries()) {
                        availableGalleries.add(galleryPath.getPath());
                    }
                    preparedSearchObj.setGalleries(availableGalleries);
                }
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                    // exclude deactivated types
                    if (!type.isDeactivated()) {
                        availableTypes.add(type.getType());
                    }
                }
                preparedSearchObj.setServerSearchTypes(availableTypes);
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

                preparedSearchObj.setServerSearchTypes(
                    new ArrayList<String>(CmsClientCollectionUtil.intersection(availableTypes, galleryTypes)));
            }
        } else {
            preparedSearchObj.setServerSearchTypes(m_searchObject.getTypes());
        }
        if (m_galleriesChanged) {
            preparedSearchObj.setGalleriesChanged(true);
            m_galleriesChanged = false;
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
     * Helper method for getting the default (sub)site root for a list of site selector options.<p>
     *
     * @param options the list of options
     *
     * @return the default (sub)site root
     */
    private String getDefaultSiteRoot(List<CmsSiteSelectorOption> options) {

        if (m_startSite != null) {
            return m_startSite;
        } else if ((options != null) && (!options.isEmpty())) {
            String defaultOption = options.get(0).getSiteRoot();
            for (CmsSiteSelectorOption option : options) {
                if (option.getType().equals(CmsSiteSelectorOption.Type.currentSubsite)) {
                    return option.getSiteRoot();
                }
                if (option.isCurrentSite()) {
                    defaultOption = option.getSiteRoot();
                }
            }
            return defaultOption;
        } else {
            return CmsCoreProvider.get().getSiteRoot();
        }
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

                start(200, true);
                CmsCoreProvider.getService().getCategoriesForSitePath(m_dialogBean.getReferenceSitePath(), this);
            }

            @Override
            protected void onResponse(List<CmsCategoryTreeEntry> result) {

                m_dialogBean.setCategories(result);
                m_handler.setCategoriesTabContent(result, new ArrayList<String>());
                m_handler.onCategoriesTabSelection();
                stop(false);
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

                start(200, true);
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
                stop(false);
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
