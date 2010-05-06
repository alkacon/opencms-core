/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleriesTab.java,v $
 * Date   : $Date: 2010/05/06 10:20:38 $
 * Version: $Revision: 1.6 $
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

import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
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
 * Provides the widget for the galleries(folder) tab.<p>
 * 
 * It displays the available gallery folders in the given order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.
 */
public class CmsGalleriesTab extends A_CmsTab implements ValueChangeHandler<String> {

    /** 
     * Extended ClickHandler class to use with checkboxes in the gallery list.<p>
     *  
     * The checkbox handler saves the id of gallery item, which was selected.  
     */
    private class CheckboxHandler implements ClickHandler {

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /** The gallery path as id for the selected gallery. */
        private String m_galleryPath;

        // TODO: remove the reference to the checkbox when the event source is clicked checkBox and not the toogleButton
        /**
         * Constructor.<p>
         * 
         * @param gallerPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public CheckboxHandler(String gallerPath, CmsCheckBox checkBox) {

            m_galleryPath = gallerPath;
            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            //CmsCheckBox sender = (CmsCheckBox)event.getSource();
            if (m_checkBox.isChecked()) {
                m_tabHandler.onSelectGallery(m_galleryPath);
            } else {
                m_tabHandler.onDeselectGallery(m_galleryPath);
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_GALLERY_SORT = "GallerySort";

    /** Text metrics key. */
    private static final String TM_GALLERY_TAB = "GalleryTab";

    /** The reference to the handler of this tab. */
    protected CmsGalleriesTabHandler m_tabHandler;

    /** The select box to change the sort order. */
    private CmsSelectBox m_sortSelectBox;

    /**
     * Constructor.<p>
     */
    public CmsGalleriesTab() {

        super();
        m_scrollList.truncate(TM_GALLERY_TAB, CmsGalleryDialog.DIALOG_WIDTH);
    }

    /**
     * Fill the content of the galleries tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the current search parameters
     * @param selectedGalleries the list of galleries to select
     */
    public void fillContent(CmsGalleryDialogBean dialogBean, ArrayList<String> selectedGalleries) {

        //TODO: use this code to add labels before select boxes
        //        Label label = new Label();
        //        label.setWidth("30px");
        //        label.setText(Messages.get().key(Messages.GUI_SORT_LABEL_SORT_0));
        //        m_sortLabel.add(label);
        //
        //        ArrayList<CmsPair<String, String>> sortList = getSortList();
        //        m_selectBox = new CmsSelectBox(sortList);
        //        m_selectBox.addValueChangeHandler(this);
        //        // TODO: use the common way to set the width of the select box
        //        m_selectBox.setWidth("200px");
        //        m_sortSelectBox.add(m_selectBox);
        //addWidgetToOptions(m_sortSelectBox);

        ArrayList<CmsPair<String, String>> sortList = getSortList();
        m_sortSelectBox = new CmsSelectBox(sortList);
        m_sortSelectBox.addValueChangeHandler(this);
        // TODO: use the common way to set the width of the select box
        m_sortSelectBox.setWidth("200px");
        m_sortSelectBox.truncate(TM_GALLERY_SORT, 200);
        addWidgetToOptions(m_sortSelectBox);

        for (CmsGalleriesListInfoBean galleryItem : dialogBean.getGalleries()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(galleryItem);
            Image icon = new Image(galleryItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(galleryItem.getId(), checkBox));
            if (selectedGalleries.contains(galleryItem.getId())) {
                checkBox.setChecked(true);
            }
            CmsGalleryListItem listItem = new CmsGalleryListItem(checkBox, listItemWidget);
            listItem.setId(galleryItem.getId());
            listItem.setItemTitle(galleryItem.getTitle());
            listItem.setSubTitle(galleryItem.getSubTitle());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the panel with the content of the galleries search parameter.<p>
     *  
     * @param selectedGalleries the list of selected galleries by the user
     * @return the panel showing the selected galleries
     */
    public HTMLPanel getGallerisParamsPanel(ArrayList<String> selectedGalleries) {

        HTMLPanel galleriesPanel;
        String panelText = "";
        if (selectedGalleries.size() == 1) {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERY_0)).concat(
                "</b>");
            CmsGalleryListItem galleryBean = (CmsGalleryListItem)m_scrollList.getItem(selectedGalleries.get(0));
            String title = galleryBean.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryBean.getSubTitle();
            }
            panelText = panelText.concat(" ").concat(title);
        } else {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERIES_0)).concat(
                "</b>");
            for (String galleryPath : selectedGalleries) {
                CmsGalleryListItem galleryBean = (CmsGalleryListItem)m_scrollList.getItem(galleryPath);
                String title = galleryBean.getItemTitle();
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
     * Returns the tab handler.<p>
     *
     * @return the tab handler
     */
    public CmsGalleriesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Will be triggered when a tab is selected.<p>
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
            m_tabHandler.onGalleriesSort(event.getValue());
        }
    }

    /**
     * Sets the tab handler.<p>
     *
     * @param tabHandler the tab handler to set
     */
    public void setTabHandler(CmsGalleriesTabHandler tabHandler) {

        m_tabHandler = tabHandler;
    }

    /**
    * Deselect the galleries  in the galleries list.<p>
    * 
    * @param galleries the galleries to deselect
    */
    public void uncheckGalleries(ArrayList<String> galleries) {

        for (String gallery : galleries) {
            CmsGalleryListItem item = (CmsGalleryListItem)m_scrollList.getItem(gallery);
            item.getCheckbox().setChecked(false);
        }
    }

    /**
     * Update the galleries list.<p>
     * 
     * @param galleries the new gallery list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateGalleries(ArrayList<CmsGalleriesListInfoBean> galleries, ArrayList<String> selectedGalleries) {

        clearList();
        for (CmsGalleriesListInfoBean galleryItem : galleries) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(galleryItem);
            Image icon = new Image(galleryItem.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(galleryItem.getId(), checkBox));
            if (selectedGalleries.contains(galleryItem.getId())) {
                checkBox.setChecked(true);
            }
            CmsGalleryListItem listItem = new CmsGalleryListItem(checkBox, listItemWidget);
            listItem.setId(galleryItem.getId());
            listItem.setItemTitle(galleryItem.getTitle());
            listItem.setSubTitle(galleryItem.getSubTitle());
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
        //TODO: move constants to the I_CmsGalleryProviderConstants
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));
        list.add(new CmsPair<String, String>(SortParams.type_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.type_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_DESC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_DESC_0)));

        return list;
    }
}