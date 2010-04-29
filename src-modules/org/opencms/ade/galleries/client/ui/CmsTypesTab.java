/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTypesTab.java,v $
 * Date   : $Date: 2010/04/29 07:37:51 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsPair;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the types tab.<p>
 * 
 * It displays the available types in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsTypesTab extends A_CmsTab {

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

            //TODO: CmsCheckBox sender = (CmsCheckBox)event.getSource();
            if (m_checkBox.isChecked()) {
                m_tabHandler.selectType(m_resourceType);
            } else {
                m_tabHandler.deselectType(m_resourceType);
            }

        }
    }

    /** The reference to the handler of this tab. */
    protected CmsTypesTabHandler m_tabHandler;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> m_dragHandler;

    /**
     * Constructor with the drag handler.<p>
     * 
     * @param handler the reference to drag handler
     */
    public CmsTypesTab(I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> handler) {

        super();
        m_dragHandler = handler;
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the current search parameters
     */
    //TODO: add the drag handler and use CmsDraggableListItemWidget instead of CmsListItemWidget
    public void fillContent(CmsGalleryDialogBean dialogBean) {

        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsTypesListInfoBean> typeItem : dialogBean.getTypes().entrySet()) {
            // TODO: replace with CmsDraggableList Item see: CmsTabResultsPanel
            CmsListItemWidget listItemWidget = new CmsListItemWidget(typeItem.getValue());
            Image icon = new Image(typeItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(typeItem.getValue().getId(), checkBox));
            CmsTypeListItem listItem = new CmsTypeListItem(checkBox, listItemWidget);
            listItem.setId(typeItem.getKey());
            addWidgetToList(listItem);
        }
    }

    /**
     * Returns the tabHandler.<p>
     *
     * @return the tabHandler
     */
    public CmsTypesTabHandler getTabHandler() {

        return m_tabHandler;
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
     * Returns the tab handler.<p>
     *
     * @param handler the tab handler
     */
    public void setTabHandler(CmsTypesTabHandler handler) {

        m_tabHandler = handler;
    }

    /**
     * Deselect the types  in the types list.<p>
     * 
     * @param types the categories to deselect
     */
    public void uncheckTypes(ArrayList<String> types) {

        for (String type : types) {
            CmsTypeListItem item = (CmsTypeListItem)m_scrollList.getItem(type);
            item.getCheckbox().setChecked(false);
        }
    }
}