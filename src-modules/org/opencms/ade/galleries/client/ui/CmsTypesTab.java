/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTypesTab.java,v $
 * Date   : $Date: 2010/05/14 13:34:53 $
 * Version: $Revision: 1.10 $
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

import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the types tab.<p>
 * 
 * It displays the available types in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 8.0.
 */
public class CmsTypesTab extends A_CmsListTab {

    /** 
     * Extended ClickHandler class to use with checkboxes in the types list.<p>
     *  
     * The checkbox handler saves the id(name?) of resource type item, which was selected.  
     */
    private class CheckboxHandler implements ClickHandler {

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /** The resource type (name/id?) as id for the selected type. */
        private String m_resourceType;

        // TODO: remove when the event source is clicked checkBox and not the toogleButton
        /**
         * Constructor.<p>
         * 
         * @param resourceType as id(name) for the selected type
         * @param checkBox the reference to the checkbox
         */
        public CheckboxHandler(String resourceType, CmsCheckBox checkBox) {

            m_resourceType = resourceType;
            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (m_checkBox.isChecked()) {
                getTabHandler().selectType(m_resourceType);
            } else {
                getTabHandler().deselectType(m_resourceType);
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_TYPE_TAB = "TypeTab";

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The reference to the handler of this tab. */
    private CmsTypesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dragHandler the drag handler
     */
    public CmsTypesTab(CmsTypesTabHandler tabHandler, I_CmsDragHandler<?, ?> dragHandler) {

        super(GalleryTabId.cms_tab_types);
        m_tabHandler = tabHandler;
        m_dragHandler = dragHandler;
        m_scrollList.truncate(TM_TYPE_TAB, CmsGalleryDialog.DIALOG_WIDTH);
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * @param typeInfos the type info beans 
     * @param selectedTypes the list of types to select
     */
    public void fillContent(List<CmsTypesListInfoBean> typeInfos, List<String> selectedTypes) {

        for (CmsTypesListInfoBean typeBean : typeInfos) {
            CmsListItemWidget listItemWidget;
            if (m_dragHandler != null) {
                // TODO: check if this is working
                listItemWidget = m_dragHandler.createDraggableListItemWidget(typeBean, typeBean.getId());
            } else {
                listItemWidget = new CmsListItemWidget(typeBean);
            }
            Image icon = new Image(typeBean.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(typeBean.getId(), checkBox));
            if ((selectedTypes != null) && selectedTypes.contains(typeBean.getId())) {
                checkBox.setChecked(true);
            }
            CmsTypeListItem listItem = new CmsTypeListItem(checkBox, listItemWidget);
            listItem.setId(typeBean.getId());
            listItem.setItemTitle(typeBean.getTitle());
            listItem.setSubTitle(typeBean.getSubTitle());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the panel with the content of the types search parameter.<p>
     *  
     * @param selectedTypes the list of selected resource types
     * @return the panel showing the selected types
     */
    public CmsFloatDecoratedPanel getTypesParamsPanel(List<String> selectedTypes) {

        if ((selectedTypes == null) || (selectedTypes.size() == 0)) {
            return null;
        }
        CmsFloatDecoratedPanel typesPanel = new CmsFloatDecoratedPanel();
        String panelText = CmsDomUtil.enclose(CmsDomUtil.Tag.b, Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPES_0));
        for (String type : selectedTypes) {

            CmsTypeListItem galleryItem = (CmsTypeListItem)m_scrollList.getItem(type);
            String title = galleryItem.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryItem.getSubTitle();
            }
            panelText += " " + title + ",";

        }
        panelText = panelText.substring(0, panelText.length() - 1);
        HTMLPanel test = new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText);
        test.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        typesPanel.add(test);

        return typesPanel;
    }

    /**
     * Deselect the types  in the types list.<p>
     * 
     * @param types the categories to deselect
     */
    public void uncheckTypes(List<String> types) {

        for (String type : types) {
            CmsTypeListItem item = (CmsTypeListItem)m_scrollList.getItem(type);
            item.getCheckbox().setChecked(false);
        }
    }

    /**
     * Updates the types list.<p>
     * 
     * @param types the new types list
     * @param selectedTypes the list of types to select
     */
    public void updateContent(List<CmsTypesListInfoBean> types, List<String> selectedTypes) {

        clearList();
        for (CmsTypesListInfoBean typeBean : types) {
            // TODO: replace with CmsDraggableList Item see: CmsTabResultsPanel
            CmsListItemWidget listItemWidget = new CmsListItemWidget(typeBean);
            Image icon = new Image(typeBean.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(typeBean.getId(), checkBox));
            if ((selectedTypes != null) && selectedTypes.contains(typeBean.getId())) {
                checkBox.setChecked(true);
            }
            CmsTypeListItem listItem = new CmsTypeListItem(checkBox, listItemWidget);
            listItem.setId(typeBean.getId());
            listItem.setItemTitle(typeBean.getTitle());
            listItem.setSubTitle(typeBean.getSubTitle());
            addWidgetToList(listItem);
        }
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

}