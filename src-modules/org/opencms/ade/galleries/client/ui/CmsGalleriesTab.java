/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleriesTab.java,v $
 * Date   : $Date: 2011/02/17 08:54:05 $
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

import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsUploadButton;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Provides the widget for the galleries(folder) tab.<p>
 * 
 * It displays the available gallery folders in the given order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 8.0.
 */
public class CmsGalleriesTab extends A_CmsListTab {

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
                getTabHandler().onSelectGallery(m_galleryPath);
            } else {
                getTabHandler().onDeselectGallery(m_galleryPath);
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_GALLERY_TAB = "GalleryTab";

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /** The tab handler. */
    private CmsGalleriesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     */
    public CmsGalleriesTab(CmsGalleriesTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_galleries);
        m_scrollList.truncate(TM_GALLERY_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_tabHandler = tabHandler;
    }

    /**
     * Fill the content of the galleries tab panel.<p>
     * 
     * @param galleryInfos the gallery info beans 
     * @param selectedGalleries the list of galleries to select
     */
    public void fillContent(List<CmsGalleryFolderBean> galleryInfos, List<String> selectedGalleries) {

        for (CmsGalleryFolderBean galleryItem : galleryInfos) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(new CmsListInfoBean(
                galleryItem.getTitle(),
                galleryItem.getPath(),
                null));
            listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(galleryItem.getType(), false));
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(galleryItem.getPath(), checkBox));
            if ((selectedGalleries != null) && selectedGalleries.contains(galleryItem.getPath())) {
                checkBox.setChecked(true);
            }
            if (galleryItem.isEditable()) {
                CmsUploadButton uploadButton = new CmsUploadButton();
                // uploadButton.setShowBorder(false);
                listItemWidget.addButton(uploadButton);
            }
            CmsGalleryListItem listItem = new CmsGalleryListItem(checkBox, listItemWidget);
            listItem.setId(galleryItem.getPath());
            listItem.setItemTitle(galleryItem.getTitle());
            listItem.setSubTitle(galleryItem.getPath());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the content of the galleries search parameter.<p>
     *  
     * @param selectedGalleries the list of selected galleries by the user
     * 
     * @return the selected galleries
     */
    public String getGalleriesParams(List<String> selectedGalleries) {

        if ((selectedGalleries == null) || (selectedGalleries.size() == 0)) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        for (String galleryPath : selectedGalleries) {
            CmsGalleryListItem galleryBean = (CmsGalleryListItem)m_scrollList.getItem(galleryPath);
            String title = galleryBean.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryBean.getSubTitle();
            }
            result.append(title).append(", ");
        }
        result.delete(result.length() - 2, result.length());

        return result.toString();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        if (m_paramPanel == null) {
            m_paramPanel = new CmsSearchParamPanel(Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERIES_0), this);
        }
        String content = getGalleriesParams(searchObj.getGalleries());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_paramPanel.setContent(content);
            return m_paramPanel;
        }
        return null;
    }

    /**
    * Deselect the galleries  in the galleries list.<p>
    * 
    * @param galleries the galleries to deselect
    */
    public void uncheckGalleries(List<String> galleries) {

        for (String gallery : galleries) {
            CmsGalleryListItem item = (CmsGalleryListItem)m_scrollList.getItem(gallery);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Update the galleries list.<p>
     * 
     * @param galleries the new gallery list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateContent(List<CmsGalleryFolderBean> galleries, List<String> selectedGalleries) {

        clearList();
        fillContent(galleries, selectedGalleries);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        List<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
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

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsGalleriesTabHandler getTabHandler() {

        return m_tabHandler;
    }
}