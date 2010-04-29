/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryController.java,v $
 * Date   : $Date: 2010/04/29 08:14:29 $
 * Version: $Revision: 1.3 $
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

import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;

/**
 * Gallery dialog controller.<p>
 * 
 * This class handles the communication between gallery dialog and the server. 
 * It contains the gallery data, but no references to the gallery dialog widget.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryController {

    /** The gallery dialog bean. */
    protected CmsGalleryDialogBean m_dialogBean;

    /** The gallery dialog mode. */
    protected String m_dialogMode;

    /** The gallery controller handler. */
    protected CmsGalleryControllerHandler m_handler;

    /** The gallery search object. */
    protected CmsGallerySearchObject m_searchObject;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /**
     * Constructor.<p>
     * 
     * 
     */
    // TODO: macht es sinn hier schon onGetInitialSearch aufzurufen?
    public CmsGalleryController() {

        // get initial search for gallery
        m_searchObject = new CmsGallerySearchObject();
        m_searchObject.init();

        m_dialogBean = new CmsGalleryDialogBean();
        // set tabs config
        String[] tabs = CmsStringUtil.splitAsArray(CmsGalleryProvider.get().getTabs(), ",");
        final ArrayList<String> tabsConfig = new ArrayList<String>();
        for (int i = 0; tabs.length > i; i++) {
            tabsConfig.add(tabs[i]);
        }
        m_dialogBean.setTabs(tabsConfig);

        m_dialogMode = CmsGalleryProvider.get().getDialogMode();

        // TODO: move a n extra initialoze method
        /** The RPC action to get the initial gallery info object. */
        CmsRpcAction<CmsGalleryInfoBean> initialAction = new CmsRpcAction<CmsGalleryInfoBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // TODO: first search, there are no explicit types set!!! the prepareSearch cannot be call at this moment
                getGalleryService().getInitialSettings(tabsConfig, m_searchObject, m_dialogMode, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGalleryInfoBean infoBean) {

                m_dialogBean = infoBean.getDialogInfo();
                m_searchObject = infoBean.getSearchObject();
                m_handler.onInitialSearch(m_searchObject, m_dialogBean, getGalleryTabIdIndex(), isOpenInResults());
            }
        };
        initialAction.execute();
    }

    /**
     * Add category to search object.<p>
     * 
     * @param categoryPath the id of the category to add
     */
    public void addCategory(String categoryPath) {

        if (!m_searchObject.getCategories().contains(categoryPath)) {
            m_searchObject.getCategories().add(categoryPath);
        }
    }

    /**
     * Add gallery to search object.<p>
     * 
     * @param galleryPath the id of the gallery to add
     */
    public void addGallery(String galleryPath) {

        if (!m_searchObject.getGalleries().contains(galleryPath)) {
            m_searchObject.getGalleries().add(galleryPath);
        }
    }

    /**
     * Add type to search object.<p>
     * 
     * @param resourceType the id(name?) of the resource type to add
     */
    //TODO: is resource type id or name used?
    public void addType(String resourceType) {

        if (!m_searchObject.getTypes().contains(resourceType)) {
            m_searchObject.getTypes().add(resourceType);
        }
    }

    /**
     * Removes all selected categories from the search object.<p>
     */
    public void clearCategories() {

        ArrayList<String> selectedCategories = m_searchObject.getCategories();
        m_handler.onClearCategories(selectedCategories);
        m_searchObject.getCategories().clear();
        updateResultsTab();
    }

    /**
     * Removes all selected galleries from the search object.<p>
     */
    public void clearGalleries() {

        ArrayList<String> selectedGalleries = m_searchObject.getGalleries();
        m_handler.onClearGalleries(selectedGalleries);
        m_searchObject.getGalleries().clear();
        updateResultsTab();
    }

    /**
     * Removes all selected types from the search object.<p>
     */
    public void clearTypes() {

        //TODO: change the type of getTypes to ArrayList<String>
        ArrayList<String> selectedTypes = (ArrayList<String>)m_searchObject.getTypes();
        m_handler.onClearTypes(selectedTypes);
        m_searchObject.getTypes().clear();
        updateResultsTab();
    }

    /**
     * Returns the int value of the tab id.<p> 
     * 
     * @return tab id
     */
    public int getGalleryTabIdIndex() {

        if (I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name().equals(m_searchObject.getTabId())) {
            return m_dialogBean.getTabs().size();
        } else if (m_dialogBean.getTabs().indexOf(m_searchObject.getTabId()) > -1) {
            return m_dialogBean.getTabs().indexOf(m_searchObject.getTabId());
        } else {
            return CmsGallerySearchObject.DEFAULT_TAB_ID;
        }
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
     * Remove the category from the search object.<p>
     * 
     * @param categoryPath the category path as id
     */
    public void removeCategory(String categoryPath) {

        if (m_searchObject.getCategories().contains(categoryPath)) {
            m_searchObject.getCategories().remove(categoryPath);
        }
    }

    /**
     * Remove the gallery from the search object.<p>
     * 
     * @param galleryPath the gallery path as id
     */
    public void removeGallery(String galleryPath) {

        if (m_searchObject.getGalleries().contains(galleryPath)) {
            m_searchObject.getGalleries().remove(galleryPath);
        }
    }

    /**
     * Remove the type from the search object.<p>
     * 
     * @param resourceType the resource type as id
     */
    public void removeType(String resourceType) {

        if (m_searchObject.getTypes().contains(resourceType)) {
            m_searchObject.getTypes().remove(resourceType);
        }
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
     * Updates the content of the categories tab.<p>
     */
    public void updateCategoriesTab() {

        m_handler.onCategoriesTabSelection();
    }

    /**
     * Updates the content of the galleries(folders) tab.<p>
     */
    public void updateGalleriesTab() {

        m_handler.onGalleriesTabSelection();
    }

    /**
     * Updates the content of the results tab.<p>
     */
    public void updateResultsTab() {

        // TODO: perform the search on tab selection and update the result lists
        /** The RPC search action for the gallery dialog. */
        CmsRpcAction<CmsGallerySearchObject> searchAction = new CmsRpcAction<CmsGallerySearchObject>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                CmsGallerySearchObject preparedObject = prepareSearchObject();
                getGalleryService().getSearch(preparedObject, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGallerySearchObject searchObj) {

                m_searchObject.setResults(searchObj.getResults());
                m_searchObject.setResultCount(searchObj.getResultCount());
                m_searchObject.setSortOrder(searchObj.getSortOrder());
                m_searchObject.setPage(searchObj.getPage());

                m_handler.onResultTabSelection(m_searchObject, m_dialogBean);
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
    CmsGallerySearchObject prepareSearchObject() {

        CmsGallerySearchObject searchObj = m_searchObject;
        CmsGallerySearchObject preparedSearchObj = new CmsGallerySearchObject(searchObj);
        // add the available types to the search object used for next search, 
        // if the criteria for types are empty
        if (searchObj.getTypes().isEmpty()) {
            // no galleries is selected, provide all available types
            if (m_searchObject.getGalleries().isEmpty()) {
                // additionally provide all available gallery folders 'widget' and 'editor' dialogmode 
                if (m_dialogMode.equals(I_CmsGalleryProviderConstants.GalleryMode.widget)
                    || m_dialogMode.equals(I_CmsGalleryProviderConstants.GalleryMode.editor)) {
                    ArrayList<String> availableGalleries = new ArrayList<String>();
                    for (String galleryPath : m_dialogBean.getGalleries().keySet()) {
                        availableGalleries.add(galleryPath);
                    }
                    preparedSearchObj.setGalleries(availableGalleries);
                }
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (String type : m_dialogBean.getTypes().keySet()) {
                    availableTypes.add(type);
                }
                preparedSearchObj.setTypes(availableTypes);
                // at least one gallery is selected 
            } else if (searchObj.getGalleries().size() > 0) {

                // get the resource types associated with the selected galleries
                HashSet<String> contentTypes = new HashSet<String>();
                for (Entry<String, CmsGalleriesListInfoBean> gallery : m_dialogBean.getGalleries().entrySet()) {
                    if (searchObj.getGalleries().contains(gallery.getKey())) {
                        contentTypes.addAll(gallery.getValue().getContentTypes());
                    }
                }
                // available types
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (String type : m_dialogBean.getTypes().keySet()) {
                    availableTypes.add(type);
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
}