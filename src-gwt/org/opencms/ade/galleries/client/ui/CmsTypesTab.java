/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the widget for the types tab.<p>
 * 
 * It displays the available types in the given sort order.
 * 
 * @since 8.0.
 */
public class CmsTypesTab extends A_CmsListTab {

    /** 
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The resource type (name/id?) as id for the selected type. */
        private String m_resourceType;

        /**
         * Constructor.<p>
         * 
         * @param resourceType as id(name) for the selected type
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(String resourceType, CmsCheckBox checkBox) {

            super(checkBox);
            m_resourceType = resourceType;
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (getCheckBox().isChecked()) {
                getTabHandler().selectType(m_resourceType);
            } else {
                getTabHandler().deselectType(m_resourceType);
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_TYPE_TAB = "TypeTab";

    /** The reference to the drag handler for the list elements. */
    private CmsDNDHandler m_dndHandler;

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /** The reference to the handler of this tab. */
    private CmsTypesTabHandler m_tabHandler;

    /** Map of type beans to type name. */
    private Map<String, CmsResourceTypeBean> m_types;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dndHandler the drag and drop handler
     */
    public CmsTypesTab(CmsTypesTabHandler tabHandler, CmsDNDHandler dndHandler) {

        super(GalleryTabId.cms_tab_types);
        m_tabHandler = tabHandler;
        m_dndHandler = dndHandler;
        m_scrollList.truncate(TM_TYPE_TAB, CmsGalleryDialog.DIALOG_WIDTH);
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * @param typeInfos the type info beans 
     * @param selectedTypes the list of types to select
     */
    public void fillContent(List<CmsResourceTypeBean> typeInfos, List<String> selectedTypes) {

        if (m_types == null) {
            m_types = new HashMap<String, CmsResourceTypeBean>();
        }
        m_types.clear();
        for (CmsResourceTypeBean typeBean : typeInfos) {
            m_types.put(typeBean.getType(), typeBean);
            CmsListItemWidget listItemWidget;
            CmsListInfoBean infoBean = new CmsListInfoBean(typeBean.getTitle(), typeBean.getDescription(), null);
            listItemWidget = new CmsListItemWidget(infoBean);
            listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(typeBean.getType(), false));
            CmsCheckBox checkBox = new CmsCheckBox();
            SelectionHandler selectionHendler = new SelectionHandler(typeBean.getType(), checkBox);
            checkBox.addClickHandler(selectionHendler);
            listItemWidget.addDoubleClickHandler(selectionHendler);
            if ((selectedTypes != null) && selectedTypes.contains(typeBean.getType())) {
                checkBox.setChecked(true);
            }
            CmsListItem listItem = new CmsListItem(checkBox, listItemWidget);
            listItem.setId(typeBean.getType());
            if (typeBean.isCreatableType() && (m_dndHandler != null)) {
                listItem.initMoveHandle(m_dndHandler);
            }
            addWidgetToList(listItem);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        if (m_paramPanel == null) {
            m_paramPanel = new CmsSearchParamPanel(Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPES_0), this);
        }
        String content = getTypesParams(searchObj.getTypes());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_paramPanel.setContent(content);
            return m_paramPanel;
        }
        return null;
    }

    /**
     * Returns the content of the types search parameter.<p>
     *  
     * @param selectedTypes the list of selected resource types
     * 
     * @return the selected types
     */
    public String getTypesParams(List<String> selectedTypes) {

        if (CmsCollectionUtil.isEmptyOrNull(selectedTypes)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        for (String type : selectedTypes) {

            CmsResourceTypeBean typeBean = m_types.get(type);
            String title = typeBean.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = typeBean.getType();
            }
            result.append(title).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    /**
     * Deselect the types  in the types list.<p>
     * 
     * @param types the categories to deselect
     */
    public void uncheckTypes(List<String> types) {

        for (String type : types) {
            CmsListItem item = (CmsListItem)m_scrollList.getItem(type);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Updates the types list.<p>
     * 
     * @param types the new types list
     * @param selectedTypes the list of types to select
     */
    public void updateContent(List<CmsResourceTypeBean> types, List<String> selectedTypes) {

        clearList();
        fillContent(types, selectedTypes);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));

        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsTypesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickFilter()
     */
    @Override
    protected boolean hasQuickFilter() {

        // quick filter not available for this tab
        return false;
    }
}