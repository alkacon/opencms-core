/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/05/05 09:20:00 $
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

import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.CmsResultsListInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the results tab.<p>
 * 
 * It displays the selected search parameter, the sort order and
 * the search results for the current search.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsTab implements ClickHandler, ValueChangeHandler<String> {

    /** Button to remove the selected categories. */
    private CmsPushButton m_closeCategoriesBtn;

    /** Button to remove the selected galleries. */
    private CmsPushButton m_closeGalleriesBtn;

    /** Button to remove the full text search. */
    //private CmsImageButton m_closeSearchBtn;

    /** Button to remove the selected types. */
    private CmsPushButton m_closeTypesBtn;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The select box to change the sort order. */
    private CmsSelectBox m_sortSelectBox;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    /**
     * The constructor with the drag handler.<p>
     *  
     * @param handler the reference to the drag handler
     */
    public CmsResultsTab(I_CmsDragHandler<?, ?> handler) {

        super();
        m_dragHandler = handler;
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
        CmsGallerySearchObject searchObj,
        CmsFloatDecoratedPanel typesParams,
        HTMLPanel galleriesParams,
        CmsFloatDecoratedPanel categoriesParams) {

        showParams(searchObj, typesParams, galleriesParams, categoriesParams);
        ArrayList<CmsPair<String, String>> sortList = getSortList();
        m_sortSelectBox = new CmsSelectBox(sortList);
        m_sortSelectBox.addValueChangeHandler(this);
        // TODO: use the common way to set the width of the select box
        m_sortSelectBox.setWidth("200px");
        addWidgetToOptions(m_sortSelectBox);

        ArrayList<CmsResultsListInfoBean> list = searchObj.getResults();
        for (CmsResultsListInfoBean resultItem : list) {
            CmsListItemWidget resultItemWidget;
            if (m_dragHandler != null) {
                resultItemWidget = m_dragHandler.createDraggableListItemWidget(resultItem, resultItem.getClientId());
            } else {
                resultItemWidget = new CmsListItemWidget(resultItem);
            }

            Image icon = new Image(resultItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            resultItemWidget.setIcon(icon);
            CmsResultListItem listItem = new CmsResultListItem(resultItemWidget);
            listItem.setId(resultItem.getId());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the tabHandler.<p>
     *
     * @return the tabHandler
     */
    public CmsResultsTabHandler getTabHandler() {

        return m_tabHandler;
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
        }
        // TODO: add search params panel

    }

    /**
     * Will be triggered when the tab is selected.<p>
     *
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#onSelection()
     */
    @Override
    public void onSelection() {

        m_tabHandler.onSelection();
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        if (event.getSource() == m_sortSelectBox) {
            // TODO: implement
            event.getValue();
        }
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
     * Returns the tab handler.<p>
     *
     * @param handler the tab handler
     */
    public void setHandler(CmsResultsTabHandler handler) {

        m_tabHandler = handler;
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
        CmsGallerySearchObject searchObj,
        CmsFloatDecoratedPanel typesParams,
        HTMLPanel galleriesParams,
        CmsFloatDecoratedPanel categoriesParams) {

        //update the search params
        clearParams();
        showParams(searchObj, typesParams, galleriesParams, categoriesParams);
        updateListSize();
        // update the result list
        clearList();
        ArrayList<CmsResultsListInfoBean> list = searchObj.getResults();
        for (CmsResultsListInfoBean resultItem : list) {
            CmsListItemWidget resultItemWidget;
            if (m_dragHandler != null) {
                resultItemWidget = m_dragHandler.createDraggableListItemWidget(resultItem, resultItem.getClientId());
            } else {
                resultItemWidget = new CmsListItemWidget(resultItem);
            }

            Image icon = new Image(resultItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            resultItemWidget.setIcon(icon);
            CmsResultListItem listItem = new CmsResultListItem(resultItemWidget);
            listItem.setId(resultItem.getId());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns a list with sort values for this tab.<p>
     * 
     * @return list of sort order value/text pairs
     */
    private ArrayList<CmsPair<String, String>> getSortList() {

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
     * Displays the selected search parameters above the result list.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param typesParams the widget to display the selected types
     * @param galleriesParams the widget to display the selected galleries 
     * @param categoriesParams the widget to display the selected categories
     */
    private void showParams(
        CmsGallerySearchObject searchObj,
        CmsFloatDecoratedPanel typesParams,
        HTMLPanel galleriesParams,
        CmsFloatDecoratedPanel categoriesParams) {

        if (searchObj.isNotEmpty()) {
            m_params.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            // selected types           
            // only show params, if any selected
            if (searchObj.getTypes().size() > 0) {
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
            if (searchObj.getGalleries().size() > 0) {
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
            if (searchObj.getCategories().size() > 0) {
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