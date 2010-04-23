/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTabResultsPanel.java,v $
 * Date   : $Date: 2010/04/23 10:08:25 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsResultsListInfoBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.ui.CmsDraggableListItemWidget;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsImageButton.Icon;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

/**
 * Provides a widget for the content of a results tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsTabResultsPanel extends CmsTabInnerPanel {

    //TODO: comment
    private CmsImageButton m_closeCategoriesBtn;

    private CmsImageButton m_closeGalleriesBtn;

    private CmsImageButton m_closeSearchBtn;

    private CmsImageButton m_closeTypesBtn;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> m_dragHandler;

    /**
     * The constructor with the drag handler.<p>
     *  
     * @param handler the reference to drag handler
     */
    public CmsTabResultsPanel(I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> handler) {

        super();
        m_dragHandler = handler;
        fillContent();

    }

    /**
     * Callback to handle click events on the close button of the selected parameters.<p>
     * 
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @Override
    public void onClick(ClickEvent event) {

        if (event.getSource() == m_closeTypesBtn) {
            removeParams(m_types);
        } else if (event.getSource() == m_closeGalleriesBtn) {
            removeParams(m_galleries);
        } else if (event.getSource() == m_closeCategoriesBtn) {
            removeParams(m_categories);
        }
        // TODO: add search params panel

    }

    /**
     * Displays the selected search parameters above the result list.<p>
     * 
     * @param infoBean the gallery info bean containing the current search parameters
     */
    private void showParams(CmsGalleryInfoBean infoBean) {

        if (infoBean.getSearchObject().isNotEmpty()) {
            m_params.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            // selected types
            CmsFloatDecoratedPanel typesParams;
            // only show params, if any selected
            if (infoBean.getSearchObject().getTypes().size() > 0) {

                typesParams = getTypesParamsPanel(
                    infoBean.getSearchObject().getTypes(),
                    infoBean.getDialogInfo().getTypes());
                typesParams.getElement().getStyle().setDisplay(Display.INLINE);
                m_types.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_types.add(typesParams);
                typesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeTypesBtn = new CmsImageButton(Icon.close, false);
                m_closeTypesBtn.addClickHandler(this);
                m_types.add(m_closeTypesBtn);

                // otherwise remove border
            } else {
                m_types.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            }

            // selected galleries
            HTMLPanel galleriesParams;
            // only show params, if any selected
            if (infoBean.getSearchObject().getGalleries().size() > 0) {
                galleriesParams = getGallerisParamsPanel(
                    infoBean.getSearchObject().getGalleries(),
                    infoBean.getDialogInfo().getGalleries());
                m_galleries.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_galleries.add(galleriesParams);
                galleriesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeGalleriesBtn = new CmsImageButton(Icon.close, false);
                m_closeGalleriesBtn.addClickHandler(this);
                m_galleries.add(m_closeGalleriesBtn);
                // otherwise remove border
            } else {
                m_galleries.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
            }

            // selected categories
            CmsFloatDecoratedPanel categoriesParams;
            // only show params, if any selected
            if (infoBean.getSearchObject().getCategories().size() > 0) {
                categoriesParams = getCategoriesParamsPanel(
                    infoBean.getSearchObject().getCategories(),
                    infoBean.getDialogInfo().getCategories());
                m_categories.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
                m_categories.add(categoriesParams);
                categoriesParams.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().paramsText());
                m_closeCategoriesBtn = new CmsImageButton(Icon.close, false);
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
     * Update the content of the result tab.<p>
     * 
     * @param infoBean the gallery info bean containing the current search parameters
     */
    public void updateResultTab(CmsGalleryInfoBean infoBean) {

        //update the search params
        clearParams();
        showParams(infoBean);
        updateListSize();
        // update the result list
        clearList();
        ArrayList<CmsResultsListInfoBean> list = infoBean.getSearchObject().getResults();
        infoBean.getSearchObject().setResults(infoBean.getSearchObject().getResults());
        infoBean.getSearchObject().setResultCount(infoBean.getSearchObject().getResultCount());
        infoBean.getSearchObject().setSortOrder(infoBean.getSearchObject().getSortOrder());
        infoBean.getSearchObject().setPage(infoBean.getSearchObject().getPage());
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
            CmsResultListItem listItem = new CmsResultListItem(infoBean, resultItemWidget);
            listItem.setId(resultItem.getId());
            addWidgetToList(listItem);
        }
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * The result list is not filled.
     */
    private void fillContent() {

        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        addWidgetToOptions(selectBox);
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
     * Deletes the html content of the panel and removes the style.<p>
     * 
     * @param panel the panel to be cleared
     */
    private void removeParams(Panel panel) {

        panel.clear();
        panel.removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showParams());
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
