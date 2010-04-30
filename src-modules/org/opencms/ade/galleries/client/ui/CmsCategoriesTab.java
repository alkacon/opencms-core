/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsCategoriesTab.java,v $
 * Date   : $Date: 2010/04/30 10:17:38 $
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

import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the categories tab.<p>
 * 
 * It displays the available categories in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsCategoriesTab extends A_CmsTab implements ValueChangeHandler<String> {

    /** 
     * Extended ClickHandler class to use with checkboxes in the category list.<p>
     *  
     * The checkbox handler saves the id of category item, which was selected.  
     */
    private class CheckboxHandler implements ClickHandler {

        /** The category path as id for the selected category. */
        private String m_categoryPath;

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        // TODO: remove the reference to the checkbox when the event source is clicked checkBox and not the toogleButton
        /**
         * Constructor.<p>
         * 
         * @param categoryPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public CheckboxHandler(String categoryPath, CmsCheckBox checkBox) {

            m_categoryPath = categoryPath;
            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            //TODO: CmsCheckBox sender = (CmsCheckBox)event.getSource();
            if (m_checkBox.isChecked()) {
                m_tabHandler.onSelectCategory(m_categoryPath);
            } else {
                m_tabHandler.onDeselectCategory(m_categoryPath);
            }

        }
    }

    /** The reference to the handler of this tab. */
    protected CmsCategoriesTabHandler m_tabHandler;

    /** The select box to change the sort order. */
    private CmsSelectBox m_sortSelectBox;

    /**
     * Constructor.<p>
     */
    public CmsCategoriesTab() {

        super();
    }

    /**
     * Fill the content of the categories tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the available categories
     */
    public void fillContent(CmsGalleryDialogBean dialogBean) {

        ArrayList<CmsPair<String, String>> sortList = getSortList();
        m_sortSelectBox = new CmsSelectBox(sortList);
        m_sortSelectBox.addValueChangeHandler(this);
        // TODO: use the common way to set the width of the select box
        m_sortSelectBox.setWidth("200px");
        addWidgetToOptions(m_sortSelectBox);
        for (CmsCategoriesListInfoBean categoryItem : dialogBean.getCategories()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryItem);
            Image icon = new Image(categoryItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(categoryItem.getId(), checkBox));
            CmsCategoryListItem listItem = new CmsCategoryListItem(checkBox, listItemWidget);
            listItem.setId(categoryItem.getId());
            listItem.setItemTitle(categoryItem.getTitle());
            listItem.setSubTitle(categoryItem.getSubTitle());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the panel with the content of the categories search parameter.<p>
     *  
     * @param selectedCategories the list of selected categories by the user
     * @return the panel showing the selected categories
     */
    public CmsFloatDecoratedPanel getCategoriesParamsPanel(ArrayList<String> selectedCategories) {

        CmsFloatDecoratedPanel categoriesPanel = new CmsFloatDecoratedPanel();
        String panelText = "";
        if (selectedCategories.size() == 1) {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORY_0)).concat(
                "</b> ");
            CmsCategoryListItem categoryItem = (CmsCategoryListItem)m_scrollList.getItem(selectedCategories.get(0));
            String title = categoryItem.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = categoryItem.getSubTitle();
            }
            panelText = panelText.concat(" ").concat(title);
        } else {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORIES_0)).concat(
                "</b> ");
            for (String categoryPath : selectedCategories) {

                CmsCategoryListItem categoryItem = (CmsCategoryListItem)m_scrollList.getItem(categoryPath);
                String title = categoryItem.getItemTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = categoryItem.getSubTitle();
                }
                panelText = panelText.concat(" ").concat(title);
            }
        }
        categoriesPanel.add(new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText));

        return categoriesPanel;
    }

    /**
     * Will be triggered when a tab is selected.<p>
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
     * Sets the tab handler.<p>
     *
     * @param tabHandler the tab handler to set
     */
    public void setTabHandler(CmsCategoriesTabHandler tabHandler) {

        m_tabHandler = tabHandler;
    }

    /**
     * Deselect the categories  in the category list.<p>
     * 
     * @param categories the categories to deselect
     */
    public void uncheckCategories(ArrayList<String> categories) {

        for (String category : categories) {
            CmsCategoryListItem item = (CmsCategoryListItem)m_scrollList.getItem(category);
            item.getCheckbox().setChecked(false);
        }
    }

    /**
     * Returns a list with sort values for this tab.<p>
     * 
     * @return list of sort order value/text pairs
     */
    private ArrayList<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.tree.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_HIERARCHIC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));

        return list;
    }
}