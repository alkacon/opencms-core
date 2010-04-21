/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryDialog.java,v $
 * Date   : $Date: 2010/04/21 15:43:31 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleriesVfs;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.I_CmsGalleryDialogCss;
import org.opencms.ade.galleries.client.util.CmsGalleryProvider;
import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.CmsResultsListInfoBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the method for the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer> {

    /** The css bundle used for this widget. */
    private static final I_CmsGalleryDialogCss DIALOG_CSS = I_CmsLayoutBundle.INSTANCE.galleryDialogCss();

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

            initTabs(infoBean);
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

        CmsGalleriesVfs.initCss();
        m_tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);

    }

    /**
     * The constructor of the gallery dialog.<p>
     * 
     * @param infoBean the gallery info bean
     */
    public CmsGalleryDialog(CmsGalleryInfoBean infoBean) {

        CmsGalleriesVfs.initCss();
        m_infoBean = infoBean;
        m_tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            CmsTabInnerPanel tabContent = buildTypesTabPanel(infoBean.getDialogInfo().getTypes());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            CmsTabInnerPanel tabContent = buildGalleriesTabPanel(infoBean.getDialogInfo().getGalleries());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            CmsTabInnerPanel tabContent = buildCategoriesTabPanel(infoBean.getDialogInfo().getCategories());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
            m_tabbedPanel.add(new Label("Hallo"), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // implement  
        }
        m_tabbedPanel.addWithLeftMargin(buildResultsTabPanel(), Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        // TODO: select the tab from the configuration
        m_tabbedPanel.selectTab(m_infoBean.getGalleryTabIdIndex());

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);

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
     * Fill the tabs with the content provided from the info bean. <p>
     * 
     * @param infoBean the bean with the tab content
     */
    public void initTabs(CmsGalleryInfoBean infoBean) {

        m_infoBean = infoBean;

        ArrayList<String> tabs = infoBean.getDialogInfo().getTabs();

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            CmsTabInnerPanel tabContent = buildTypesTabPanel(infoBean.getDialogInfo().getTypes());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            CmsTabInnerPanel tabContent = buildGalleriesTabPanel(infoBean.getDialogInfo().getGalleries());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

        }

        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            CmsTabInnerPanel tabContent = buildCategoriesTabPanel(infoBean.getDialogInfo().getCategories());
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
            m_tabbedPanel.add(new Label("Hallo"), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
        }
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // implement  
        }
        // add left margin to the result button
        m_tabbedPanel.addWithLeftMargin(buildResultsTabPanel(), Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        m_tabbedPanel.selectTab(m_infoBean.getGalleryTabIdIndex());
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

        // TODO: provide a convinient way to get the tab id 
        int tabIndex = getInfoBean().getDialogInfo().getTabs().size();
        CmsTabInnerPanel tabPanel = (CmsTabInnerPanel)m_tabbedPanel.getWidget(tabIndex);
        //update the search params
        tabPanel.clearParams();
        tabPanel.showParams(getInfoBean());
        tabPanel.updateListSize();
        // update the result list
        tabPanel.clearList();
        ArrayList<CmsResultsListInfoBean> list = infoBean.getSearchObject().getResults();
        getInfoBean().getSearchObject().setResults(infoBean.getSearchObject().getResults());
        getInfoBean().getSearchObject().setResultCount(infoBean.getSearchObject().getResultCount());
        getInfoBean().getSearchObject().setSortOrder(infoBean.getSearchObject().getSortOrder());
        getInfoBean().getSearchObject().setPage(infoBean.getSearchObject().getPage());
        for (CmsResultsListInfoBean resultItem : list) {
            CmsListItemWidget resultItemWidget = new CmsListItemWidget(resultItem);
            Image icon = new Image(resultItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            resultItemWidget.setIcon(icon);
            CmsResultListItem listItem = new CmsResultListItem(getInfoBean(), resultItemWidget);
            listItem.setId(resultItem.getId());
            tabPanel.addWidgetToList(listItem);
        }
    }

    /**
     * Creates a 'div' tag containing the categories tab content.<p>
     * 
     * @param contentList the list with the content of the given tab
     * 
     * @return the widget containing the tab content
     */
    private CmsTabInnerPanel buildCategoriesTabPanel(LinkedHashMap<String, CmsCategoriesListInfoBean> contentList) {

        CmsTabInnerPanel tabInner = new CmsTabInnerPanel();
        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        tabInner.addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsCategoriesListInfoBean> categoryItem : contentList.entrySet()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryItem.getValue());
            Image icon = new Image(categoryItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCategoryListItem listItem = new CmsCategoryListItem(getInfoBean(), new CmsCheckBox(), listItemWidget);
            listItem.setId(categoryItem.getKey());
            tabInner.addWidgetToList(listItem);
        }
        return tabInner;
    }

    /**
     * Creates a 'div' tag containing the galleries tab content.<p>
     * 
     * @param contentList the list with the content of the given tab
     * 
     * @return the widget containing the tab content
     */
    private CmsTabInnerPanel buildGalleriesTabPanel(LinkedHashMap<String, CmsGalleriesListInfoBean> contentList) {

        CmsTabInnerPanel tabInner = new CmsTabInnerPanel();
        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        tabInner.addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsGalleriesListInfoBean> galleryItem : contentList.entrySet()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(galleryItem.getValue());
            Image icon = new Image(galleryItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsGalleryListItem listItem = new CmsGalleryListItem(getInfoBean(), new CmsCheckBox(), listItemWidget);
            listItem.setId(galleryItem.getKey());
            tabInner.addWidgetToList(listItem);
        }
        return tabInner;
    }

    /**
     * Creates a 'div' tag containing the results tab content.<p>
     * 
     * @param contentList the list with the content of the given tab
     * 
     * @return the widget containing the tab content
     */
    private CmsTabInnerPanel buildResultsTabPanel() {

        CmsTabInnerPanel tabInner = new CmsTabInnerPanel();
        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        tabInner.addWidgetToOptions(selectBox);
        if (!getInfoBean().getSearchObject().getResults().isEmpty()) {
            ArrayList<CmsResultsListInfoBean> list = getInfoBean().getSearchObject().getResults();
            for (CmsResultsListInfoBean resultItem : list) {
                CmsListItemWidget resultItemWidget = new CmsListItemWidget(resultItem);
                Image icon = new Image(resultItem.getIconResource());
                icon.setStyleName(DIALOG_CSS.listIcon());
                resultItemWidget.setIcon(icon);
                CmsResultListItem listItem = new CmsResultListItem(getInfoBean(), resultItemWidget);
                listItem.setId(resultItem.getId());
                tabInner.addWidgetToList(listItem);
            }
        }
        return tabInner;
    }

    /**
     * Creates a 'div' tag containing the types tab content.<p>
     * 
     * @param contentList the list with the content of the given tab
     * 
     * @return the widget containing the tab content
     */
    private CmsTabInnerPanel buildTypesTabPanel(LinkedHashMap<String, CmsTypesListInfoBean> contentList) {

        CmsTabInnerPanel tabInner = new CmsTabInnerPanel();
        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        tabInner.addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsTypesListInfoBean> typeItem : contentList.entrySet()) {
            final CmsListItemWidget listItemWidget = new CmsListItemWidget(typeItem.getValue());
            Image icon = new Image(typeItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsTypeListItem listItem = new CmsTypeListItem(getInfoBean(), new CmsCheckBox(), listItemWidget);
            listItem.setId(typeItem.getKey());
            tabInner.addWidgetToList(listItem);
        }
        return tabInner;
    }
}