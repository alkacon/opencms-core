/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryController.java,v $
 * Date   : $Date: 2010/05/28 09:31:39 $
 * Version: $Revision: 1.14 $
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.shared.CmsCategoryBean;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.sort.CmsComparatorPath;
import org.opencms.gwt.shared.sort.CmsComparatorTitle;
import org.opencms.gwt.shared.sort.CmsComparatorType;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.client.GWT;

/**
 * Gallery dialog controller.<p>
 * 
 * This class handles the communication between gallery dialog and the server. 
 * It contains the gallery data, but no references to the gallery dialog widget.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryController {

    /** The gallery dialog bean. */
    protected CmsGalleryDataBean m_dialogBean;

    /** The gallery dialog mode. */
    protected I_CmsGalleryProviderConstants.GalleryMode m_dialogMode;

    /** The gallery controller handler. */
    protected CmsGalleryControllerHandler m_handler;

    /** The gallery search object. */
    protected CmsGallerySearchBean m_searchObject;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

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
        //        m_searchObject = null;
        m_dialogBean = (CmsGalleryDataBean)CmsRpcPrefetcher.getSerializedObject(
            getGalleryService(),
            CmsGalleryDataBean.DICT_NAME);
        //        m_dialogBean = new CmsGalleryDataBean();
        //        m_dialogBean.setMode(GalleryMode.view);

        // set tabs config
        //        String[] tabs = CmsStringUtil.splitAsArray(CmsGalleryProvider.get().getTabs(), ",");
        //        final ArrayList<String> tabsConfig = new ArrayList<String>();
        //        for (int i = 0; tabs.length > i; i++) {
        //            tabsConfig.add(tabs[i]);
        //        }
        //        m_dialogBean.setTabs(tabsConfig);

        m_dialogMode = GalleryMode.view;
        // m_dialogBean.getMode();

        if (m_searchObject == null) {
            m_searchObject = new CmsGallerySearchBean();
        }
        m_handler.onInitialSearch(m_searchObject, m_dialogBean, this);

        // TODO: move to an extra initialize method
        /** The RPC action to get the initial gallery info object. */
        //        CmsRpcAction<CmsGalleryInfoBean> initialAction = new CmsRpcAction<CmsGalleryInfoBean>() {
        //
        //            /**
        //            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
        //            */
        //            @Override
        //            public void execute() {
        //
        //                // TODO: first search, there are no explicit types set!!! the prepareSearch cannot be call at this moment
        //                getGalleryService().getInitialSettings(tabsConfig, m_searchObject, this);
        //            }
        //
        //            /**
        //            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
        //            */
        //            @Override
        //            public void onResponse(CmsGalleryInfoBean infoBean) {
        //
        //                m_dialogBean = infoBean.getDialogInfo();
        //                m_searchObject = infoBean.getSearchObject();
        //                m_handler.onInitialSearch(m_searchObject, m_dialogBean, getGalleryTabIdIndex(), isOpenInResults());
        //            }
        //        };
        //        initialAction.execute();

    }

    /**
     * Opens the resource preview for the given resource.<p>
     * 
     * @param previewName the name of the preview provider
     * @param galleryMode the gallery mode
     * @param resourcePath the resource path
     * @param parentElement the dialog element to insert the preview into
     * 
     * @return debug message
     */
    private static native String openPreview(
        String previewName,
        String galleryMode,
        String resourcePath,
        String parentElementId)/*-{
        var providerList=$wnd[@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_PREVIEW_PROVIDER_LIST];
        if (providerList){
        var provider=providerList[previewName];
        if (provider){
        var openPreview=provider[@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_OPEN_PREVIEW_FUNCTION];
        if (openPreview){
        try{
        openPreview(galleryMode, resourcePath, parentElementId);
        }catch(err){
        return err.description;
        }
        return "Opened preview";
        }else{
        return "Open function not available";
        }
        }else{
        return "Provider "+previewName+" not available";
        }
        }else{
        return "Provider list not available";
        }
    }-*/;

    /**
     * Add category to search object.<p>
     * 
     * @param categoryPath the id of the category to add
     */
    public void addCategory(String categoryPath) {

        m_searchObject.addCategory(categoryPath);
    }

    /**
     * Add gallery to search object.<p>
     * 
     * @param galleryPath the id of the gallery to add
     */
    public void addGallery(String galleryPath) {

        m_searchObject.addGallery(galleryPath);
    }

    /**
     * Add type to search object.<p>
     * 
     * @param resourceType the id(name?) of the resource type to add
     */
    //TODO: is resource type id or name used?
    public void addType(String resourceType) {

        m_searchObject.addType(resourceType);
    }

    /**
     * Removes all selected categories from the search object.<p>
     */
    public void clearCategories() {

        List<String> selectedCategories = m_searchObject.getCategories();
        m_handler.onClearCategories(selectedCategories);
        m_searchObject.clearCategories();
        updateResultsTab();
    }

    /**
     * Removes all selected galleries from the search object.<p>
     */
    public void clearGalleries() {

        List<String> selectedGalleries = m_searchObject.getGalleries();
        m_handler.onClearGalleries(selectedGalleries);
        m_searchObject.clearGalleries();
        updateResultsTab();
    }

    /**
     * Removes all selected types from the search object.<p>
     */
    public void clearTypes() {

        List<String> selectedTypes = m_searchObject.getTypes();
        m_handler.onClearTypes(selectedTypes);
        m_searchObject.clearTypes();
        updateResultsTab();
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
     * Opens the preview for the selected item.<p>
     * 
     * @param id the item to select
     */
    //TODO: remove function
    //    public void openPreview(final String id) {
    //
    //        // TODO: replace dummy with data from rpc call
    //        CmsPreviewInfoBean dummyBean = new CmsPreviewInfoBean();
    //        dummyBean.setPreviewHtml(new String("/opencms/opencms/demo_t3/images/Strelitzie.JPG"));
    //        Map<String, String> dummyProps = new LinkedHashMap<String, String>();
    //        dummyProps.put("Title", "Mein Title");
    //        dummyProps.put("Description", "Mein Title");
    //        dummyProps.put("Groesse", "Mein Title");
    //        dummyProps.put("Groesse und sehr lang und so", "Mein Title");
    //        dummyProps.put("Groesse", "Mein Title und hier auch etwas länger");
    //        dummyProps.put("Groesse", "Mein Title");
    //        dummyProps.put("Groesse und alles durcheinander", "Mein Title und auch enen langen Text");
    //        dummyProps.put("Groesse", "Mein Title");
    //        dummyProps.put("Groesse", "Mein Title");
    //        dummyProps.put("Groesse", "Mein Title udn am Ende auch");
    //        dummyBean.setPropeties(dummyProps);
    //
    //        Map<String, String> dummyInfos = new LinkedHashMap<String, String>();
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.title.name(), "");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_name.name(), "");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_size.name(), "");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_type.name(), "Mein Title");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.height.name(), "");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.width.name(), "");
    //        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.path.name(), "");
    //        dummyBean.setImageInfos(dummyInfos);
    //        m_handler.onOpenPreview(m_dialogMode, dummyBean);
    //        //TODO: call the rpc action and open the preview dialog in the callback
    //    }

    /**
     * Opens the preview for the given resource by the given resource type.<p>
     * 
     * @param resourcePath the resource path
     * @param resourceType the resource type name
     */
    public void openPreview(String resourcePath, String resourceType) {

        CmsDebugLog.getInstance().printLine("Opening preview for path: " + resourcePath + " type: " + resourceType);
        for (CmsResourceTypeBean typeBean : m_dialogBean.getTypes()) {
            CmsDebugLog.getInstance().printLine("Checking type: " + typeBean.getType());
            if (typeBean.getType().equals(resourceType)) {
                CmsDebugLog.getInstance().printLine(
                    openPreview(
                        typeBean.getPreviewProviderName(),
                        m_dialogMode.name(),
                        resourcePath,
                        m_handler.getDialogElementId()));
                return;
            }
        }
        CmsDebugLog.getInstance().printLine("Could not open preview.");
    }

    /**
     * Remove the category from the search object.<p>
     * 
     * @param categoryPath the category path as id
     */
    public void removeCategory(String categoryPath) {

        m_searchObject.removeCategory(categoryPath);
    }

    /**
     * Remove the gallery from the search object.<p>
     * 
     * @param galleryPath the gallery path as id
     */
    public void removeGallery(String galleryPath) {

        m_searchObject.removeGallery(galleryPath);
    }

    /**
     * Remove the type from the search object.<p>
     * 
     * @param resourceType the resource type as id
     */
    public void removeType(String resourceType) {

        m_searchObject.removeType(resourceType);
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
     * Sorts the categories according to given parameters and updates the list.<p>
     * 
     * @param sortParams the sort parameters
     */
    public void sortCategories(String sortParams) {

        List<CmsCategoryBean> categories;
        SortParams sort = SortParams.valueOf(sortParams);
        switch (sort) {
            case tree:
                m_handler.onUpdateCategories(m_dialogBean.getCategories(), m_searchObject.getCategories());
                return;
            case title_asc:
                categories = new ArrayList<CmsCategoryBean>();
                categoryTreeToList(categories, m_dialogBean.getCategories());
                Collections.sort(categories, new CmsComparatorTitle(true));
                m_handler.onUpdateCategories(categories, m_searchObject.getCategories());
                break;
            case title_desc:
                categories = new ArrayList<CmsCategoryBean>();
                categoryTreeToList(categories, m_dialogBean.getCategories());
                Collections.sort(categories, new CmsComparatorTitle(false));
                m_handler.onUpdateCategories(categories, m_searchObject.getCategories());
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
     */
    public void sortGalleries(String sortParams) {

        List<CmsGalleryFolderBean> galleries = m_dialogBean.getGalleries();
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
            case dateLastModified_asc:
            case dateLastModified_desc:
            case tree:
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

        /** The RPC search action for the gallery dialog. */
        CmsRpcAction<CmsGallerySearchBean> sortAction = new CmsRpcAction<CmsGallerySearchBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                m_searchObject.setSortOrder(sortParams);
                CmsGallerySearchBean preparedObject = prepareSearchObject();
                getGalleryService().getSearch(preparedObject, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGallerySearchBean searchObj) {

                m_searchObject.setResults(searchObj.getResults());
                m_searchObject.setResultCount(searchObj.getResultCount());
                m_searchObject.setSortOrder(searchObj.getSortOrder());
                m_searchObject.setPage(searchObj.getPage());

                m_handler.onResultTabSelection(m_searchObject);
            }

        };
        sortAction.execute();
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
     */
    public void updateResultsTab() {

        /** The RPC search action for the gallery dialog. */
        CmsRpcAction<CmsGallerySearchBean> searchAction = new CmsRpcAction<CmsGallerySearchBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                CmsGallerySearchBean preparedObject = prepareSearchObject();
                getGalleryService().getSearch(preparedObject, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGallerySearchBean searchObj) {

                m_searchObject.setResults(searchObj.getResults());
                m_searchObject.setResultCount(searchObj.getResultCount());
                m_searchObject.setSortOrder(searchObj.getSortOrder());
                m_searchObject.setPage(searchObj.getPage());

                m_handler.onResultTabSelection(m_searchObject);
            }

        };
        searchAction.execute();
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
        }
        return m_gallerySvc;
    }

    //TODO: which modifier to use
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
        // add the available types to the search object used for next search, 
        // if the criteria for types are empty
        if ((m_searchObject.getTypes() == null) || m_searchObject.getTypes().isEmpty()) {
            // no galleries is selected, provide all available types
            if ((m_searchObject.getGalleries() == null) || m_searchObject.getGalleries().isEmpty()) {
                // additionally provide all available gallery folders 'widget' and 'editor' dialogmode 
                if ((m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.widget)
                    || (m_dialogMode == I_CmsGalleryProviderConstants.GalleryMode.editor)) {
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
                HashSet<String> contentTypes = new HashSet<String>();
                for (CmsGalleryFolderBean gallery : m_dialogBean.getGalleries()) {
                    if (m_searchObject.getGalleries().contains(gallery.getPath())) {
                        contentTypes.addAll(gallery.getContentTypes());
                    }
                }
                // available types
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                    availableTypes.add(type.getType());
                }
                // check if the associated type is also an available type
                ArrayList<String> checkedTypes = new ArrayList<String>();
                for (String type : contentTypes) {
                    if (availableTypes.contains(type) && !checkedTypes.contains(type)) {
                        checkedTypes.add(type);
                    }
                }
                preparedSearchObj.setTypes(checkedTypes);
            }
            return preparedSearchObj;
            // just use the unchanged search object 
        } else {
            return preparedSearchObj;
        }
    }

    /**
     * Converts categories tree to a list of info beans.<p>
     * 
     * @param categoryList the category list
     * @param entries the tree entries
     */
    private void categoryTreeToList(List<CmsCategoryBean> categoryList, CmsCategoryTreeEntry treeEntry) {

        if (treeEntry == null) {
            return;
        }
        // skipping the root tree entry where the path property is empty
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(treeEntry.getPath())) {
            CmsCategoryBean bean = new CmsCategoryBean(treeEntry.getTitle(), treeEntry.getPath(), treeEntry.getPath());
            categoryList.add(bean);
        }
        if (treeEntry.getChildren() != null) {
            for (CmsCategoryTreeEntry entry : treeEntry.getChildren()) {
                categoryTreeToList(categoryList, entry);
            }
        }
    }

    /**
     * Loading all available categories.<p>
     */
    private void loadCategories() {

        CmsRpcAction<CmsCategoryTreeEntry> action = new CmsRpcAction<CmsCategoryTreeEntry>() {

            @Override
            public void execute() {

                if (m_dialogBean.getGalleries() == null) {
                    List<String> types = new ArrayList<String>();
                    for (CmsResourceTypeBean type : m_dialogBean.getTypes()) {
                        types.add(type.getType());
                    }
                    getGalleryService().getCategoryTreeTypes(types, this);
                } else {
                    List<String> galleries = new ArrayList<String>();
                    for (CmsGalleryFolderBean info : m_dialogBean.getGalleries()) {
                        galleries.add(info.getPath());
                    }
                    getGalleryService().getCategoryTreeGalleries(galleries, this);
                }
            }

            @Override
            protected void onResponse(CmsCategoryTreeEntry result) {

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
}