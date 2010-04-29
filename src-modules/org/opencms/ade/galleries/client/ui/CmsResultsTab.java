/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/04/29 07:37:51 $
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

import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.CmsResultsListInfoBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.ui.CmsDraggableListItemWidget;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsTab implements ClickHandler {

    /** Button to remove the selected categories. */
    private CmsPushButton m_closeCategoriesBtn;

    /** Button to remove the selected galleries. */
    private CmsPushButton m_closeGalleriesBtn;

    /** Button to remove the full text search. */
    //private CmsImageButton m_closeSearchBtn;

    /** Button to remove the selected types. */
    private CmsPushButton m_closeTypesBtn;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> m_dragHandler;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    /**
     * The constructor with the drag handler.<p>
     *  
     * @param handler the reference to the drag handler
     */
    public CmsResultsTab(I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> handler) {

        super();
        m_dragHandler = handler;
    }

    /**
     * Fill the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param dialogBean the dialog data bean
     */
    public void fillContent(CmsGallerySearchObject searchObj, CmsGalleryDialogBean dialogBean) {

        showParams(searchObj, dialogBean);
        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        addWidgetToOptions(selectBox);

        ArrayList<CmsResultsListInfoBean> list = searchObj.getResults();
        for (CmsResultsListInfoBean resultItem : list) {
            CmsDraggableListItemWidget resultItemWidget;
            if (m_dragHandler != null) {
                resultItemWidget = new CmsDraggableListItemWidget(resultItem, true);
                resultItemWidget.setClientId(resultItem.getClientId());
                m_dragHandler.registerMouseHandler(resultItemWidget);
            } else {
                resultItemWidget = new CmsDraggableListItemWidget(resultItem, false);
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
     * @param dialogBean the dialog data bean
     */
    public void updateContent(CmsGallerySearchObject searchObj, CmsGalleryDialogBean dialogBean) {

        //update the search params
        clearParams();
        showParams(searchObj, dialogBean);
        updateListSize();
        // update the result list
        clearList();
        ArrayList<CmsResultsListInfoBean> list = searchObj.getResults();
        for (CmsResultsListInfoBean resultItem : list) {
            CmsDraggableListItemWidget resultItemWidget;
            if (m_dragHandler != null) {
                resultItemWidget = new CmsDraggableListItemWidget(resultItem, true);
                resultItemWidget.setClientId(resultItem.getClientId());
                m_dragHandler.registerMouseHandler(resultItemWidget);
            } else {
                resultItemWidget = new CmsDraggableListItemWidget(resultItem, false);
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
     * Returns the panel with the content of the categories search parameter.<p>
     *  
     * @param selectedCategories the list of selected categories by the user
     * @param categories the map with categories beans
     * @return the panel showing the selected categories
     */
    private CmsFloatDecoratedPanel getCategoriesParamsPanel(
        ArrayList<String> selectedCategories,
        LinkedHashMap<String, CmsCategoriesListInfoBean> categories) {

        CmsFloatDecoratedPanel categoriesPanel = new CmsFloatDecoratedPanel();
        String panelText = "";
        if (selectedCategories.size() == 1) {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORY_0)).concat(
                "</b> ");
            CmsCategoriesListInfoBean categoryBean = categories.get(selectedCategories.get(0));
            String title = categoryBean.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = categoryBean.getSubTitle();
            }
            panelText = panelText.concat(" ").concat(title);
        } else {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORIES_0)).concat(
                "</b> ");
            for (String categoryPath : selectedCategories) {

                CmsCategoriesListInfoBean categoryBean = categories.get(categoryPath);
                String title = categoryBean.getTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = categoryBean.getSubTitle();
                }
                panelText = panelText.concat(" ").concat(title);
            }
        }
        categoriesPanel.add(new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText));

        return categoriesPanel;
    }

    /**
     * Returns the panel with the content of the galleries search parameter.<p>
     *  
     * @param selectedGalleries the list of selected galleries by the user
     * @param galleries the map with galleries beans
     * @return the panel showing the selected galleries
     */
    private HTMLPanel getGallerisParamsPanel(
        ArrayList<String> selectedGalleries,
        LinkedHashMap<String, CmsGalleriesListInfoBean> galleries) {

        HTMLPanel galleriesPanel;
        String panelText = "";
        if (selectedGalleries.size() == 1) {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERY_0)).concat(
                "</b>");
            CmsGalleriesListInfoBean galleryBean = galleries.get(selectedGalleries.get(0));
            String title = galleryBean.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryBean.getSubTitle();
            }
            panelText = panelText.concat(" ").concat(title);
        } else {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERIES_0)).concat(
                "</b>");
            for (String galleryPath : selectedGalleries) {

                CmsGalleriesListInfoBean galleryBean = galleries.get(galleryPath);
                String title = galleryBean.getTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = galleryBean.getSubTitle();
                }
                panelText = panelText.concat(" ").concat(title);
            }
        }
        galleriesPanel = new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText);

        return galleriesPanel;
    }

    /**
     * Returns the panel with the content of the types search parameter.<p>
     *  
     * @param selectedTypes the list of selected resource types
     * @param types the map with type beans
     * @return the panel showing the selected types
     */
    private CmsFloatDecoratedPanel getTypesParamsPanel(
        List<String> selectedTypes,
        LinkedHashMap<String, CmsTypesListInfoBean> types) {

        CmsFloatDecoratedPanel typesPanel = new CmsFloatDecoratedPanel();
        String panelText = "";
        if (selectedTypes.size() == 1) {
            panelText += CmsDomUtil.enclose(CmsDomUtil.Tag.b, Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPE_0));
            CmsTypesListInfoBean galleryBean = types.get(selectedTypes.get(0));
            String title = galleryBean.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryBean.getSubTitle();
            }
            panelText = panelText + " " + title;
        } else {
            panelText += CmsDomUtil.enclose(CmsDomUtil.Tag.b, Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPES_0));
            for (String galleryPath : selectedTypes) {

                CmsTypesListInfoBean galleryBean = types.get(galleryPath);
                String title = galleryBean.getTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = galleryBean.getSubTitle();
                }
                panelText = panelText + " " + title;
            }
        }
        HTMLPanel test = new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText);
        test.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        typesPanel.add(test);

        return typesPanel;
    }

    /**
     * Displays the selected search parameters above the result list.<p>
     * 
     * @param infoBean the gallery info bean containing the current search parameters
     */
    private void showParams(CmsGallerySearchObject searchObj, CmsGalleryDialogBean dialogBean) {

        if (searchObj.isNotEmpty()) {
            m_params.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            // selected types
            CmsFloatDecoratedPanel typesParams;
            // only show params, if any selected
            if (searchObj.getTypes().size() > 0) {

                typesParams = getTypesParamsPanel(searchObj.getTypes(), dialogBean.getTypes());
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
            HTMLPanel galleriesParams;
            // only show params, if any selected
            if (searchObj.getGalleries().size() > 0) {
                galleriesParams = getGallerisParamsPanel(searchObj.getGalleries(), dialogBean.getGalleries());
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
            CmsFloatDecoratedPanel categoriesParams;
            // only show params, if any selected
            if (searchObj.getCategories().size() > 0) {
                categoriesParams = getCategoriesParamsPanel(searchObj.getCategories(), dialogBean.getCategories());
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