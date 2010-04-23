/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryDialog.java,v $
 * Date   : $Date: 2010/04/23 08:02:03 $
 * Version: $Revision: 1.5 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.util.CmsGalleryProvider;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the method for the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer> {

    /** The RPC action to get the initial gallery info object. */
    CmsRpcAction<CmsGalleryInfoBean> m_initialAction = new CmsRpcAction<CmsGalleryInfoBean>() {

        /**
        * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
        */
        @Override
        public void execute() {

            // initial search obj
            CmsGallerySearchObject searchObj = new CmsGallerySearchObject();
            searchObj.init();

            // set tabs config
            String[] tabs = CmsStringUtil.splitAsArray(CmsGalleryProvider.get().getTabs(), ",");
            final ArrayList<String> tabsConfig = new ArrayList<String>();
            for (int i = 0; tabs.length > i; i++) {
                tabsConfig.add(tabs[i]);
            }

            String dialogMode = CmsGalleryProvider.get().getDialogMode();

            getGalleryService().getInitialSettings(tabsConfig, searchObj, dialogMode, this);
        }

        /**
        * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
        */
        @Override
        public void onResponse(CmsGalleryInfoBean infoBean) {

            fillTabsContent(infoBean);
        }
    };

    /** The RPC search action for the gallery dialog. */
    CmsRpcAction<CmsGalleryInfoBean> m_search = new CmsRpcAction<CmsGalleryInfoBean>() {

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
        public void onResponse(CmsGalleryInfoBean infoBean) {

            updateResultTab(infoBean);
        }

    };

    private I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> m_dragHandler;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /** The gallery info bean. */
    private CmsGalleryInfoBean m_infoBean;

    /** The tabbed panel. */
    private CmsTabbedPanel m_tabbedPanel;

    /**
     * The default constructor for the gallery dialog.<p> 
     */
    public CmsGalleryDialog() {

        initCss();
        m_dragHandler = null;
        m_tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);

    }

    // TODO: remove, if do not used any more
    /**
     * The constructor of the gallery dialog.<p>
     * 
     * @param infoBean the gallery info bean
     * @param handler the reference to the drag handler
     */
    public CmsGalleryDialog(CmsGalleryInfoBean infoBean, I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> handler) {

        initCss();
        m_dragHandler = handler;
        m_tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillTypesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillGalleriesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillCategoriesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
            // TODO: fill with content
            m_tabbedPanel.add(new Label("Hallo"), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // implement  
        }
        // add left margin to the result button
        CmsTabInnerPanel resultTab = new CmsTabInnerPanel(m_dragHandler);
        resultTab.fillResultsTabPanel();
        m_tabbedPanel.addWithLeftMargin(resultTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        m_tabbedPanel.selectTab(m_infoBean.getGalleryTabIdIndex());

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);

    }

    /**
     * The default constructor for the gallery dialog.<p> 
     * 
     * @param handler the reference to the drag handler
     */
    public CmsGalleryDialog(I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> handler) {

        initCss();
        m_dragHandler = handler;
        m_tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);

    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public static void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
    }

    /**
     * Fill the tabs with the content provided from the info bean. <p>
     * 
     * @param infoBean the bean with the tab content
     */
    public void fillTabsContent(CmsGalleryInfoBean infoBean) {

        m_infoBean = infoBean;

        ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillTypesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillGalleriesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            CmsTabInnerPanel tabContent = new CmsTabInnerPanel(m_dragHandler);
            tabContent.fillCategoriesTabPanel(infoBean);
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
            // TODO: fill with content
            m_tabbedPanel.add(new Label("Hallo"), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // implement  
        }
        // add left margin to the result button
        CmsTabInnerPanel resultTab = new CmsTabInnerPanel(m_dragHandler);
        resultTab.fillResultsTabPanel();
        m_tabbedPanel.addWithLeftMargin(resultTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        m_tabbedPanel.selectTab(m_infoBean.getGalleryTabIdIndex());
    }

    /**
     * Returns the gallery info bean.<p>
     *
     * @return the infoBean
     */
    public CmsGalleryInfoBean getInfoBean() {

        return m_infoBean;
    }

    /**
     * Executes the RPC call to get the initial gallery setting and fill gallery with content.<p>
     */
    public void init() {

        m_initialAction.execute();
    }

    /**
     * @see com.google.gwt.event.logical.shared.BeforeSelectionHandler#onBeforeSelection(com.google.gwt.event.logical.shared.BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

        // implement        
    }

    /**
     * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();
        // TODO: change the condition to make more generic
        // update the list layout with checkbox on selection
        if ((selectedIndex != 3) && (selectedIndex != 4)) {
            CmsTabInnerPanel panel = (CmsTabInnerPanel)m_tabbedPanel.getWidget(selectedIndex);
            panel.updateListLayout();
        }

        // get the id of the result tab
        // TODO: provide a convinient way to get the tab id 
        int resultTabIndex = getInfoBean().getDialogInfo().getTabs().size();
        if (selectedIndex == resultTabIndex) {
            m_search.execute();
        }

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
     * Returns a consistent search object for the search.<p>
     * 
     * For the search at least one resource type should be provided.
     * The corresponding resource types will be added to the search object, if no or only gallery folder are selected.
     * 
     * @return the search object
     */
    CmsGallerySearchObject prepareSearchObject() {

        CmsGallerySearchObject searchObj = getInfoBean().getSearchObject();
        CmsGallerySearchObject preparedSearchObj = new CmsGallerySearchObject(searchObj);
        // add the available types to the search object used for next search, 
        // if the criteria for types are empty
        if (searchObj.getTypes().isEmpty()) {
            // no galleries is selected, provide all available types
            if (getInfoBean().getSearchObject().getGalleries().isEmpty()) {
                // TODO:  change when dialogmode is moved to infobea
                // additionally provide all available gallery folders 'widget' and 'editor' dialogmode 
                if (getInfoBean().getDialogMode().equals(I_CmsGalleryProviderConstants.GalleryMode.widget)
                    || getInfoBean().getDialogMode().equals(I_CmsGalleryProviderConstants.GalleryMode.editor)) {
                    ArrayList<String> availableGalleries = new ArrayList<String>();
                    for (String galleryPath : getInfoBean().getDialogInfo().getGalleries().keySet()) {
                        availableGalleries.add(galleryPath);
                    }
                    preparedSearchObj.setGalleries(availableGalleries);
                }
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (String type : getInfoBean().getDialogInfo().getTypes().keySet()) {
                    availableTypes.add(type);
                }
                preparedSearchObj.setTypes(availableTypes);
                // at least one gallery is selected 
            } else if (searchObj.getGalleries().size() > 0) {

                // get the resource types associated with the selected galleries
                HashSet<String> contentTypes = new HashSet<String>();
                for (Entry<String, CmsGalleriesListInfoBean> gallery : getInfoBean().getDialogInfo().getGalleries().entrySet()) {
                    if (searchObj.getGalleries().contains(gallery.getKey())) {
                        contentTypes.addAll(gallery.getValue().getContentTypes());
                    }
                }
                // available types
                ArrayList<String> availableTypes = new ArrayList<String>();
                for (String type : getInfoBean().getDialogInfo().getTypes().keySet()) {
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

    /**
     * Updates the list of the results after the search.<p>
     * 
     * @param infoBean the gallery info bean containing the new results
     */
    /* default */void updateResultTab(CmsGalleryInfoBean infoBean) {

        // update search object
        getInfoBean().getSearchObject().setResults(infoBean.getSearchObject().getResults());
        getInfoBean().getSearchObject().setResultCount(infoBean.getSearchObject().getResultCount());
        getInfoBean().getSearchObject().setSortOrder(infoBean.getSearchObject().getSortOrder());
        getInfoBean().getSearchObject().setPage(infoBean.getSearchObject().getPage());

        // TODO: provide a convinient way to get the tab id 
        int tabIndex = getInfoBean().getDialogInfo().getTabs().size();
        CmsTabInnerPanel tabPanel = (CmsTabInnerPanel)m_tabbedPanel.getWidget(tabIndex);
        tabPanel.updateResultTab(getInfoBean());
    }
}