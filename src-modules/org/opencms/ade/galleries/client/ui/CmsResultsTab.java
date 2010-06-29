/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/06/29 09:38:46 $
 * Version: $Revision: 1.18 $
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

import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Provides the widget for the results tab.<p>
 * 
 * It displays the selected search parameter, the sort order and
 * the search results for the current search.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsListTab {

    /**
     * Special click handler to use with push button.<p>
     */
    private class CmsPreviewButtonHandler implements ClickHandler {

        /** The id of the selected item. */
        private String m_resourcePath;

        /** The resource type of the selected item. */
        private String m_resourceType;

        /**
         * Constructor.<p>
         * 
         * @param resourcePath the item resource path 
         * @param resourceType the item resource type
         */
        public CmsPreviewButtonHandler(String resourcePath, String resourceType) {

            m_resourcePath = resourcePath;
            m_resourceType = resourceType;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().openPreview(m_resourcePath, m_resourceType);

        }
    }

    /** Text metrics key. */
    private static final String TM_RESULT_TAB = "ResultTab";

    /** The categories parameter panel. */
    private CmsSearchParamPanel m_categories;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The VFS folders parameter panel. */
    private CmsSearchParamPanel m_folders;

    /** The galleries parameter panel. */
    private CmsSearchParamPanel m_galleries;

    /** The panel showing the search parameters. */
    private FlowPanel m_params;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    /** The types parameter panel panel. */
    private CmsSearchParamPanel m_types;

    /**
     * The constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dragHandler the drag handler
     */
    public CmsResultsTab(CmsResultsTabHandler tabHandler, I_CmsDragHandler<?, ?> dragHandler) {

        super(GalleryTabId.cms_tab_results);
        m_dragHandler = dragHandler;
        m_tabHandler = tabHandler;
        m_scrollList.truncate(TM_RESULT_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_params = new FlowPanel();
        m_params.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().tabOptions());
        m_categories = new CmsSearchParamPanel(GalleryTabId.cms_tab_categories, Messages.get().key(
            Messages.GUI_PARAMS_LABEL_CATEGORIES_0), this);
        m_galleries = new CmsSearchParamPanel(GalleryTabId.cms_tab_galleries, Messages.get().key(
            Messages.GUI_PARAMS_LABEL_GALLERIES_0), this);
        m_types = new CmsSearchParamPanel(GalleryTabId.cms_tab_types, Messages.get().key(
            Messages.GUI_PARAMS_LABEL_TYPES_0), this);
        m_folders = new CmsSearchParamPanel(GalleryTabId.cms_tab_vfstree, Messages.get().key(
            Messages.GUI_PARAMS_LABEL_VFS_0), this);
        m_tab.insert(m_params, 0);
    }

    /**
     * Fill the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the selected types as a user-readable string
     * @param galleriesParams the selected galleries as a user-readable string  
     * @param foldersParams the selected VFS folders as a user-readable string 
     * @param categoriesParams the selected categories as a user-readable string 
     */
    public void fillContent(
        CmsGallerySearchBean searchObj,
        String typesParams,
        String galleriesParams,
        String foldersParams,
        String categoriesParams) {

        showParams(searchObj, typesParams, galleriesParams, foldersParams, categoriesParams);

        List<CmsResultItemBean> list = searchObj.getResults();
        for (CmsResultItemBean resultItem : list) {

            CmsListItemWidget resultItemWidget;
            CmsListInfoBean infoBean = new CmsListInfoBean(resultItem.getTitle(), resultItem.getDescription(), null);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resultItem.getExcerpt())) {
                infoBean.addAdditionalInfo(
                    Messages.get().key(Messages.GUI_RESULT_LABEL_EXCERPT_0),
                    resultItem.getExcerpt());
            }
            if (m_dragHandler != null) {
                resultItemWidget = m_dragHandler.createDraggableListItemWidget(infoBean, resultItem.getClientId());
            } else {
                resultItemWidget = new CmsListItemWidget(infoBean);
            }
            // add  preview button
            CmsPushButton previewButton = new CmsPushButton();
            previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().magnifierIcon());
            previewButton.setShowBorder(false);
            previewButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            previewButton.addClickHandler(new CmsPreviewButtonHandler(resultItem.getPath(), resultItem.getType()));
            resultItemWidget.addButton(previewButton);
            // add file icon
            resultItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resultItem.getType(), resultItem.getPath()));
            CmsResultListItem listItem = new CmsResultListItem(resultItemWidget);
            listItem.setId(resultItem.getPath());
            addWidgetToList(listItem);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#onSelection()
     */
    @Override
    public void onSelection() {

        super.onSelection();
        updateListSize();
    }

    /**
     * Removes the categories parameter display button.<p>
     */
    public void removeCategories() {

        m_categories.removeFromParent();
    }

    /**
     * Removes the galleries parameter display button.<p>
     */
    public void removeGalleries() {

        m_galleries.removeFromParent();
    }

    /**
     * Removes the search parameters associated with the given tab id.<p>
     * 
     * @param tabId the tab id
     */
    public void removeParams(GalleryTabId tabId) {

        switch (tabId) {
            case cms_tab_categories:
                m_tabHandler.onRemoveCategories();
                break;
            case cms_tab_containerpage:
                break;
            case cms_tab_galleries:
                m_tabHandler.onRemoveGalleries();
                break;
            case cms_tab_results:
                break;
            case cms_tab_search:
                m_tabHandler.onRemoveTextSearch();
                break;
            case cms_tab_sitemap:
                break;
            case cms_tab_types:
                m_tabHandler.onRemoveTypes();
                break;
            case cms_tab_vfstree:
                m_tabHandler.onRemoveFolders();
                break;
            default:
                break;
        }
        updateListSize();
    }

    /**
     * Removes the types parameter display button.<p>
     */
    public void removeTypes() {

        m_types.removeFromParent();
    }

    /**
     * Updates the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the selected types as a user-readable string
     * @param galleriesParams the selected galleries as a user-readable string 
     * @param foldersParams the  selected VFS folders as a user-readable string
     * @param categoriesParams the selected categories as a user-readable string 
     */
    public void updateContent(
        CmsGallerySearchBean searchObj,
        String typesParams,
        String galleriesParams,
        String foldersParams,
        String categoriesParams) {

        clearList();
        fillContent(searchObj, typesParams, galleriesParams, foldersParams, categoriesParams);
    }

    /**
     * Clears all search parameters.<p>
     */
    protected void clearParams() {

        m_params.clear();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected ArrayList<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));
        list.add(new CmsPair<String, String>(SortParams.type_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.type_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_DESC_0)));
        list.add(new CmsPair<String, String>(SortParams.dateLastModified_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_DATELASTMODIFIED_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.dateLastModified_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_DATELASTMODIFIED_DESC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_DESC_0)));

        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsResultsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        updateListSize();
    }

    /**
     * Displays the selected search parameters in the result tab.<p>
     * 
     * @param searchObj the bean containing the search parameters 
     * @param typesParams a user-readable string containing the selected types
     * @param galleriesParams a user-readable string containing the selected galleries
     * @param foldersParams a user-readable string containing
     * @param categoriesParams
     */
    private void showParams(
        CmsGallerySearchBean searchObj,
        String typesParams,
        String galleriesParams,
        String foldersParams,
        String categoriesParams) {

        m_params.clear();
        if (searchObj.isEmpty()) {
            m_params.setVisible(false);
            return;
        }
        m_params.setVisible(true);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typesParams)) {
            m_types.setContent(typesParams);
            m_params.add(m_types);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(galleriesParams)) {
            m_galleries.setContent(galleriesParams);
            m_params.add(m_galleries);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoriesParams)) {
            m_categories.setContent(categoriesParams);
            m_params.add(m_categories);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(foldersParams)) {
            m_folders.setContent(foldersParams);
            m_params.add(m_folders);
        }
    }

    /**
     * Updates the height (with border) of the params 'div' panel.<p>    
     */
    private void updateListSize() {

        int tabHeight = m_tab.getElement().getClientHeight();
        CmsDebugLog.getInstance().printLine("updating size, tabHeight: " + tabHeight);
        // sanity check on tab height
        tabHeight = tabHeight > 0 ? tabHeight : 450;

        int marginValueParams = 0;
        String marginBottomPrams = CmsDomUtil.getCurrentStyle(m_params.getElement(), CmsDomUtil.Style.marginBottom);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(marginBottomPrams)) {
            marginValueParams = CmsClientStringUtil.parseInt(marginBottomPrams);
        }
        int paramsHeight = m_params.getOffsetHeight() + marginValueParams;

        int marginValueOptions = 0;
        String marginBottomOptions = CmsDomUtil.getCurrentStyle(m_params.getElement(), CmsDomUtil.Style.marginBottom);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(marginBottomOptions)) {
            marginValueOptions = CmsClientStringUtil.parseInt(marginBottomOptions);
        }
        int optionsHeight = m_options.getOffsetHeight() + marginValueOptions;

        // 3 is some offset, because of the list border
        int newListSize = tabHeight - paramsHeight - optionsHeight - 4;
        CmsDebugLog.getInstance().printLine(" paramsHeight: " + paramsHeight + " optionsHeight: " + optionsHeight);
        // another sanity check, don't set any negative height 
        if (newListSize > 0) {
            m_list.getElement().getStyle().setHeight(newListSize, Unit.PX);
        }
    }
}