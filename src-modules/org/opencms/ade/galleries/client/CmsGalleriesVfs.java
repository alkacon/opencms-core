/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleriesVfs.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
 * Version: $Revision: 1.1 $
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

import org.opencms.ade.galleries.client.util.CmsGalleryProvider;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.util.CmsStringUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Gallery Dialog entry class to be open from the vfs tree.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
// TODO: internatiolisation
// TODO: extract tab panel code to build gallery dialog to an class 
public class CmsGalleriesVfs extends A_CmsEntryPoint {

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

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

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        final CmsTabbedPanel tabbedPanel = new CmsTabbedPanel();

        // TODO: test data for initial search object
        final CmsGallerySearchObject searchObj = new CmsGallerySearchObject();
        // TODO: set the initial search bean:
        // if gallery is selected write the gallery path to the gallery list
        ArrayList<String> galleries = new ArrayList<String>();
        // TODO: replace string params through JSON string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(CmsGalleryProvider.get().getGalleryPath())) {
            galleries.add(CmsGalleryProvider.get().getGalleryPath());

        }
        searchObj.setGalleries(galleries);
        //TODO: replace dummy data with data openvfsgallery.jsp

        searchObj.setSortOrder("");
        searchObj.setTabId(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name());
        searchObj.setMachesPerPage(8);

        // set tabs config
        String[] tabs = CmsStringUtil.splitAsArray(CmsGalleryProvider.get().getTabs(), ",");
        final ArrayList<String> tabsConfig = new ArrayList<String>();
        for (int i = 0; tabs.length > i; i++) {
            tabsConfig.add(tabs[i]);
        }

        CmsRpcAction<CmsGalleryInfoBean> getInitialAction = new CmsRpcAction<CmsGalleryInfoBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // TODO: call an inteface function to load all possible lists                
                getGalleryService().getInitialSettings(tabsConfig, searchObj, this);

            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGalleryInfoBean infoBean) {

                //TODO: use enum for tabs names!!!!                
                ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

                if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
                    CmsList galleriesList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getDialogInfo().getTypes();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget typesItem = new CmsListItemWidget(item);
                        galleriesList.addItem(new CmsListItem(typesItem));
                    }

                    tabbedPanel.add(galleriesList, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));

                }

                if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
                    CmsList galleriesList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getDialogInfo().getGalleries();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget galleryItem = new CmsListItemWidget(item);
                        galleriesList.addItem(new CmsListItem(galleryItem));
                    }

                    tabbedPanel.add(galleriesList, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

                }

                if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
                    CmsList categoriesList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getDialogInfo().getCategories();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget categoryItem = new CmsListItemWidget(item);
                        categoriesList.addItem(new CmsListItem(categoryItem));
                    }

                    tabbedPanel.add(categoriesList, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

                }
                if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
                    tabbedPanel.add(new Label("Hallo"), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
                }
                if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
                    // TODO: implement  
                }
                if (!infoBean.getSearchObject().getResults().isEmpty()) {
                    CmsList resultList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getSearchObject().getResults();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget resultItem = new CmsListItemWidget(item);
                        resultList.addItem(new CmsListItem(resultItem));
                    }
                    tabbedPanel.insert(resultList, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0), 0);
                }
                RootPanel.get().add(tabbedPanel);
            }

        };
        getInitialAction.execute();

        CmsRpcAction<CmsGalleryInfoBean> getTabsAction = new CmsRpcAction<CmsGalleryInfoBean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // TODO: call an inteface function to load all possible lists                
                getGalleryService().getCriteriaLists(tabsConfig, this);

            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsGalleryInfoBean infoBean) {

                //TODO: use enum for tabs names!!!!
                ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

                if (tabs.contains("cms_tabs_galleries")) {
                    CmsList galleriesList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getDialogInfo().getGalleries();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget galleryItem = new CmsListItemWidget(item);
                        galleriesList.addItem(new CmsListItem(galleryItem));
                    }

                    tabbedPanel.add(galleriesList, "Galleries");

                }
                if (tabs.contains("cms_tabs_categories")) {
                    CmsList categoriesList = new CmsList();
                    List<CmsListInfoBean> list = infoBean.getDialogInfo().getCategories();
                    for (CmsListInfoBean item : list) {
                        CmsListItemWidget categoryItem = new CmsListItemWidget(item);
                        categoriesList.addItem(new CmsListItem(categoryItem));
                    }

                    tabbedPanel.add(categoriesList, "Categories");

                }
                if (tabs.contains("cms_tabs_query")) {
                    tabbedPanel.add(new Label("Hallo"), "Search Query");
                }
                if (tabs.contains("cms_tabs_sitemap")) {
                    // TODO: implement  
                }

                RootPanel.get().add(tabbedPanel);
            }

        };
        // getTabsAction.execute();
    }
}
