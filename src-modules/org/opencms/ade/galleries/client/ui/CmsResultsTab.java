/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/05/27 09:42:23 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the widget for the results tab.<p>
 * 
 * It displays the selected search parameter, the sort order and
 * the search results for the current search.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsListTab implements ClickHandler {

    /**
     * Special click handler to use with push button.<p>
     */
    private class CmsPushButtonHandler implements ClickHandler {

        /** The id of the selected item. */
        private String m_id;

        /**
         * Constructor.<p>
         * 
         * @param id
         */
        public CmsPushButtonHandler(String id) {

            m_id = id;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().onClick(m_id);

        }
    }

    /** Text metrics key. */
    private static final String TM_RESULT_TAB = "ResultTab";

    /** The categories parameter panel. */
    private CmsFlowPanel m_categories;

    /** Button to remove the selected categories. */
    private CmsPushButton m_closeCategoriesBtn;

    /** Button to remove the selected galleries. */
    private CmsPushButton m_closeGalleriesBtn;

    /** Button to remove the selected types. */
    private CmsPushButton m_closeTypesBtn;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The galleries parameter panel. */
    private CmsFlowPanel m_galleries;

    /** Button to remove the full text search. */
    //private CmsImageButton m_closeSearchBtn;

    /** The panel showing the search parameters. */
    private FlowPanel m_params;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    /** The types parameter panel panel. */
    private CmsFlowPanel m_types;

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
        m_categories = new CmsFlowPanel(CmsDomUtil.Tag.span.name());
        m_categories.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_galleries = new CmsFlowPanel(CmsDomUtil.Tag.span.name());
        m_galleries.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_types = new CmsFlowPanel(CmsDomUtil.Tag.span.name());
        m_types.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_params.add(m_types);
        m_params.add(m_galleries);
        m_params.add(m_categories);
        m_tab.insert(m_params, 0);
    }

    /**
     * Fill the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the widget to display the selected types
     * @param galleriesParams the widget to display the selected galleries 
     * @param categoriesParams the widget to display the selected categories
     */
    public void fillContent(
        CmsGallerySearchBean searchObj,
        Widget typesParams,
        Widget galleriesParams,
        Widget categoriesParams) {

        showParams(searchObj, typesParams, galleriesParams, categoriesParams);
        //updateListSize();
        List<CmsResultItemBean> list = searchObj.getResults();
        for (CmsResultItemBean resultItem : list) {

            CmsListItemWidget resultItemWidget;
            CmsListInfoBean infoBean = new CmsListInfoBean(resultItem.getTitle(), resultItem.getDescription(), null);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resultItem.getExcerpt())) {
                //TODO: add localization
                infoBean.addAdditionalInfo("Excerpt", resultItem.getExcerpt());
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
            previewButton.addClickHandler(new CmsPushButtonHandler(resultItem.getPath()));
            resultItemWidget.addButton(previewButton);
            // add file icon
            resultItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resultItem.getType(), resultItem.getPath()));
            CmsResultListItem listItem = new CmsResultListItem(resultItemWidget);
            listItem.setId(resultItem.getPath());
            addWidgetToList(listItem);
        }
    }

    /**
     * Callback to handle click events on the close button of the selected parameters.<p>
     * 
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        if (event.getSource() == m_closeTypesBtn) {
            m_tabHandler.onRemoveTypes();
        } else if (event.getSource() == m_closeGalleriesBtn) {
            m_tabHandler.onRemoveGalleries();
        } else if (event.getSource() == m_closeCategoriesBtn) {
            m_tabHandler.onRemoveCategories();
        } // TODO: add search params panel
    }

    /**
     * Removes the categories parameter display button.<p>
     */
    public void removeCategories() {

        m_categories.clear();
        m_categories.removeStyleName(org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
    }

    /**
     * Removes the galleries parameter display button.<p>
     */
    public void removeGalleries() {

        m_galleries.clear();
        m_galleries.removeStyleName(org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
    }

    /**
     * Removes the types parameter display button.<p>
     */
    public void removeTypes() {

        m_types.clear();
        m_types.removeStyleName(org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
    }

    /**
     * Updates the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the widget to display the selected types
     * @param galleriesParams the widget to display the selected galleries 
     * @param categoriesParams the widget to display the selected categories
     */
    public void updateContent(
        CmsGallerySearchBean searchObj,
        Widget typesParams,
        Widget galleriesParams,
        Widget categoriesParams) {

        //update the search params
        clearParams();
        clearList();
        fillContent(searchObj, typesParams, galleriesParams, categoriesParams);
    }

    /**
     * Clears all search parameters.<p>
     */
    protected void clearParams() {

        m_types.clear();
        m_galleries.clear();
        m_categories.clear();
        //        m_text.clear();
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
     * Displays the selected search parameters above the result list.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the widget to display the selected types
     * @param galleriesParams the widget to display the selected galleries 
     * @param categoriesParams the widget to display the selected categories
     */
    private void showParams(
        CmsGallerySearchBean searchObj,
        Widget typesParams,
        Widget galleriesParams,
        Widget categoriesParams) {

        if (searchObj.isNotEmpty()) {
            m_params.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            // selected types           
            // only show params, if any selected
            if (typesParams != null) {
                typesParams.getElement().getStyle().setDisplay(Display.INLINE);
                m_types.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_types.add(typesParams);
                typesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeTypesBtn = new CmsPushButton(I_CmsButton.UiIcon.close);
                m_closeTypesBtn.setShowBorder(false);
                m_types.add(m_closeTypesBtn);
                m_closeTypesBtn.addClickHandler(this);

                // otherwise remove border
            } else {
                m_types.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            }

            // selected galleries
            // only show params, if any selected
            if (galleriesParams != null) {
                m_galleries.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_galleries.add(galleriesParams);
                galleriesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeGalleriesBtn = new CmsPushButton(I_CmsButton.UiIcon.close);
                m_closeGalleriesBtn.setShowBorder(false);
                m_closeGalleriesBtn.addClickHandler(this);
                m_galleries.add(m_closeGalleriesBtn);
                // otherwise remove border
            } else {
                m_galleries.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            }

            // selected categories        
            // only show params, if any selected
            if (categoriesParams != null) {
                m_categories.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_categories.add(categoriesParams);
                categoriesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeCategoriesBtn = new CmsPushButton(I_CmsButton.UiIcon.close);
                m_closeCategoriesBtn.setShowBorder(false);
                m_closeCategoriesBtn.addClickHandler(this);
                m_categories.add(m_closeCategoriesBtn);
                // otherwise remove border
            } else {
                m_categories.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            }

            // TODO: full text search
        } else {
            // remove margin and border
            m_params.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            m_types.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            m_galleries.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            m_categories.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
        }
    }

    /**
     * Updates the height (with border) of the params 'div' panel.<p>    
     */
    private void updateListSize() {

        int tabHeight = m_tab.getElement().getClientHeight();

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

        m_list.getElement().getStyle().setHeight(newListSize, Unit.PX);
    }
}