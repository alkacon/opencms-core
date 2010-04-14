/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTabInnerPanel.java,v $
 * Date   : $Date: 2010/04/14 14:16:20 $
 * Version: $Revision: 1.4 $
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
import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsImageButton.Icon;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the content of a tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.
 */
public class CmsTabInnerPanel extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsTabInnerPanelUiBinder extends UiBinder<Widget, CmsTabInnerPanel> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsTabInnerPanelUiBinder uiBinder = GWT.create(I_CmsTabInnerPanelUiBinder.class);

    /** The categories parameter panel. */
    @UiField
    protected Panel m_categories;

    /** The galleries parameter panel. */
    @UiField
    protected Panel m_galleries;

    /** The borded panel to hold the scrollable list. */
    @UiField
    protected CmsFlowPanel m_list;

    /** The scrollable list panel. */
    @UiField
    protected CmsList m_scrollList;

    /** The option panel. */
    @UiField
    protected Panel m_options;

    /** The option panel. */
    @UiField
    protected Panel m_params;

    /** The option panel. */
    @UiField
    protected HTMLPanel m_tab;

    //    /** The full text search parameter panel. */
    //    @UiField
    //    protected Panel m_text;

    /** The types parameter panel panel. */
    @UiField
    protected Panel m_types;

    /**
     * The default constructor.<p>
     */
    public CmsTabInnerPanel() {

        init();
    }

    /**
     * Add a list item widget to the list panel.<p>
     * 
     * @param listItem the list item to add
     */
    public void addWidgetToList(Widget listItem) {

        m_scrollList.add(listItem);
    }

    /**
     * Add a widget to the option panel.<p>
     * 
     * The option panel should contain drop down boxes or other list options.
     * 
     * @param widget the widget to add
     */
    public void addWidgetToOptions(Widget widget) {

        m_options.add(widget);
    }

    /**
     * Clears the list panel.<p>
     */
    public void clearList() {

        m_scrollList.clearList();
    }

    /**
     * Clears all search parameters.<p>
     */
    public void clearParams() {

        m_types.clear();
        m_galleries.clear();
        m_categories.clear();
        //        m_text.clear();
    }

    /**
     * Displays the selected search parameters above the result list.<p>
     * 
     * @param infoBean the gallery info bean containing the current search parameters
     */
    public void showParams(CmsGalleryInfoBean infoBean) {

        if (infoBean.getSearchObject().isNotEmpty()) {
            m_params.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().marginBottom());
            //m_params.getElement().getStyle().setMarginBottom(5, Unit.PX);
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
                CmsImageButton button = new CmsImageButton(Icon.close, false);
                m_types.add(button);

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
                CmsImageButton button = new CmsImageButton(Icon.close, false);
                m_galleries.add(button);
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
                CmsImageButton button = new CmsImageButton(Icon.close, false);
                m_categories.add(button);
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
    public void updateListSize() {

        int tabHeight = m_tab.getElement().getClientHeight();

        int marginValueParams = 0;
        String marginBottomPrams = CmsDomUtil.getCurrentStyle(m_params.getElement(), CmsDomUtil.Style.marginBottom);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(marginBottomPrams)) {
            marginValueParams = CmsStringUtil.parseInt(marginBottomPrams);
        }
        int paramsHeight = m_params.getOffsetHeight() + marginValueParams;

        int marginValueOptions = 0;
        String marginBottomOptions = CmsDomUtil.getCurrentStyle(m_params.getElement(), CmsDomUtil.Style.marginBottom);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(marginBottomOptions)) {
            marginValueOptions = CmsStringUtil.parseInt(marginBottomOptions);
        }
        int optionsHeight = m_options.getOffsetHeight() + marginValueOptions;

        // 3 is some offset, because of the list border
        int newListSize = tabHeight - paramsHeight - optionsHeight - 4;

        m_list.getElement().getStyle().setHeight(newListSize, Unit.PX);
    }

    /**
     * Updates the layout for all list items in this list.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsList#updateLayout()
     */
    public void updateListLayout() {

        m_scrollList.updateLayout();
    }

    /**
     * Initializes this list item.<p>
     */
    protected void init() {

        CmsGalleriesVfs.initCss();
        uiBinder.createAndBindUi(this);
        initWidget(uiBinder.createAndBindUi(this));
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
        //        CmsImageButton button = new CmsImageButton(ICON.cancel, true);
        //        typesPanel.addToFloat(button);
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
}