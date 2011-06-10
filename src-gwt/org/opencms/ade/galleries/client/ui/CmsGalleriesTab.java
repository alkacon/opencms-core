/*
 * File   : $Source: /alkacon/cvs/opencms/src-gwt/org/opencms/ade/galleries/client/ui/CmsGalleriesTab.java,v $
 * Date   : $Date: 2011/06/10 06:57:13 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.galleries.shared.CmsGalleryTreeEntry;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the widget for the galleries(folder) tab.<p>
 * 
 * It displays the available gallery folders in the given order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsGalleriesTab extends A_CmsListTab {

    /** 
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The gallery path as id for the selected gallery. */
        private String m_galleryPath;

        /**
         * Constructor.<p>
         * 
         * @param gallerPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(String gallerPath, CmsCheckBox checkBox) {

            super(checkBox);
            m_galleryPath = gallerPath;
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (getCheckBox().isChecked()) {
                getTabHandler().onSelectGallery(m_galleryPath);
            } else {
                getTabHandler().onDeselectGallery(m_galleryPath);
            }

        }
    }

    /** Text metrics key. */
    private static final String TM_GALLERY_TAB = "GalleryTab";

    /** Map of gallery folders by path. */
    private Map<String, CmsGalleryFolderBean> m_galleries;

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

        if (m_galleries == null) {
            m_galleries = new HashMap<String, CmsGalleryFolderBean>();
        }
        m_galleries.clear();
        for (CmsGalleryFolderBean galleryItem : galleryInfos) {
            addWidgetToList(createTreeItem(galleryItem, selectedGalleries, false));
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
            CmsGalleryFolderBean galleryBean = m_galleries.get(galleryPath);
            String title = galleryBean.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryBean.getPath();
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
    * De-selects the galleries in the galleries list.<p>
    * 
    * @param galleries the galleries to deselect
    */
    public void uncheckGalleries(List<String> galleries) {

        for (String gallery : galleries) {
            CmsListItem item = searchTreeItem(m_scrollList, gallery);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Update the galleries list.<p>
     * 
     * @param galleries the new gallery list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateListContent(List<CmsGalleryFolderBean> galleries, List<String> selectedGalleries) {

        clearList();
        fillContent(galleries, selectedGalleries);
    }

    /**
     * Update the galleries tree.<p>
     * 
     * @param galleryTreeEntries the new gallery tree list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateTreeContent(List<CmsGalleryTreeEntry> galleryTreeEntries, List<String> selectedGalleries) {

        clearList();
        if (m_galleries == null) {
            m_galleries = new HashMap<String, CmsGalleryFolderBean>();
        }
        if (galleryTreeEntries != null) {
            // add the first level and children
            for (CmsGalleryTreeEntry galleryBean : galleryTreeEntries) {
                // set the category tree item and add to list 
                CmsTreeItem treeItem = createTreeItem(galleryBean, selectedGalleries, true);
                addChildren(treeItem, galleryBean.getChildren(), selectedGalleries);
                addWidgetToList(treeItem);
                treeItem.setOpen(true);
            }
        }
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
        list.add(new CmsPair<String, String>(SortParams.tree.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_HIERARCHIC_0)));
        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsGalleriesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Adds children to the gallery tree and select the galleries.<p>
     * 
     * @param parent the parent item 
     * @param children the list of children
     * @param selectedGalleries the list of galleries to select
     */
    private void addChildren(CmsTreeItem parent, List<CmsGalleryTreeEntry> children, List<String> selectedGalleries) {

        if (children != null) {
            for (CmsGalleryTreeEntry child : children) {
                // set the category tree item and add to parent tree item
                CmsTreeItem treeItem = createTreeItem(child, selectedGalleries, true);
                if ((selectedGalleries != null) && selectedGalleries.contains(child.getPath())) {
                    parent.setOpen(true);
                    openParents(parent);
                }
                parent.addChild(treeItem);
                addChildren(treeItem, child.getChildren(), selectedGalleries);
            }
        }
    }

    /**
     * Creates a tree item widget used in list and tree view of this tab.<p>
     * 
     * @param galleryInfo the gallery folder bean
     * @param selectedGalleries the selected galleries
     * @param forTree <code>true</code> if the item is used within tree view
     * 
     * @return the tree item
     */
    private CmsTreeItem createTreeItem(CmsGalleryFolderBean galleryInfo, List<String> selectedGalleries, boolean forTree) {

        CmsListItemWidget listItemWidget = new CmsListItemWidget(new CmsListInfoBean(
            galleryInfo.getTitle(),
            galleryInfo.getPath(),
            null));
        m_galleries.put(galleryInfo.getPath(), galleryInfo);
        listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(galleryInfo.getType(), false));
        CmsCheckBox checkBox = new CmsCheckBox();
        SelectionHandler selectionHandler = new SelectionHandler(galleryInfo.getPath(), checkBox);
        checkBox.addClickHandler(selectionHandler);
        listItemWidget.addDoubleClickHandler(selectionHandler);
        if ((selectedGalleries != null) && selectedGalleries.contains(galleryInfo.getPath())) {
            checkBox.setChecked(true);
        }
        if (galleryInfo.isEditable()) {
            listItemWidget.addButton(createUploadButtonForTarget(galleryInfo.getPath()));
        }
        CmsTreeItem treeItem = new CmsTreeItem(forTree, checkBox, listItemWidget);
        treeItem.setId(galleryInfo.getPath());
        return treeItem;
    }

    /**
     * Goes up the tree and opens the parents of the item.<p>
     * 
     * @param item the child item to start from
     */
    private void openParents(CmsTreeItem item) {

        if (item != null) {
            item.setOpen(true);
            openParents(item.getParentItem());
        }
    }
}