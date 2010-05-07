/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTypesTab.java,v $
 * Date   : $Date: 2010/05/07 08:16:13 $
 * Version: $Revision: 1.8 $
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
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the types tab.<p>
 * 
 * It displays the available types in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.
 */
public class CmsTypesTab extends A_CmsTab implements ValueChangeHandler<String> {

    /** Text metrics key. */
    private static final String TM_TYPE_TAB = "TypeTab";

    /** Text metrics key. */
    private static final String TM_TYPE_SORT = "TypeSort";

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
                m_tabHandler.selectType(m_resourceType);
            } else {
                m_tabHandler.deselectType(m_resourceType);
            }
        }
    }

    /** The reference to the handler of this tab. */
    protected CmsTypesTabHandler m_tabHandler;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The select box to change the sort order. */
    private CmsSelectBox m_sortSelectBox;

    /**
     * Constructor with the drag handler.<p>
     * 
     * @param handler the reference to drag handler
     */
    public CmsTypesTab(I_CmsDragHandler<?, ?> handler) {

        super();
        m_dragHandler = handler;
        m_scrollList.truncate(TM_TYPE_TAB, CmsGalleryDialog.DIALOG_WIDTH);
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the current search parameters
     * @param selectedTypes the list of types to select
     */
    //TODO: add the drag handler and use CmsDraggableListItemWidget instead of CmsListItemWidget
    public void fillContent(CmsGalleryDialogBean dialogBean, List<String> selectedTypes) {

        ArrayList<CmsPair<String, String>> sortList = getSortList();
        m_sortSelectBox = new CmsSelectBox(sortList);
        m_sortSelectBox.addValueChangeHandler(this);
        m_sortSelectBox.addStyleName(DIALOG_CSS.selectboxWidth());
        m_sortSelectBox.truncate(TM_TYPE_SORT, 200);
        addWidgetToOptions(m_sortSelectBox);
        for (CmsTypesListInfoBean typeBean : dialogBean.getTypes()) {
            // TODO: replace with CmsDraggableList Item see: CmsTabResultsPanel
            CmsListItemWidget listItemWidget = new CmsListItemWidget(typeBean);
            Image icon = new Image(typeBean.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(typeBean.getId(), checkBox));
            if (selectedTypes.contains(typeBean.getId())) {
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
     * Returns the tabHandler.<p>
     *
     * @return the tabHandler
     */
    public CmsTypesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Returns the panel with the content of the types search parameter.<p>
     *  
     * @param selectedTypes the list of selected resource types
     * @return the panel showing the selected types
     */
    public CmsFloatDecoratedPanel getTypesParamsPanel(List<String> selectedTypes) {

        CmsFloatDecoratedPanel typesPanel = new CmsFloatDecoratedPanel();
        String panelText = "";
        if (selectedTypes.size() == 1) {
            panelText += CmsDomUtil.enclose(CmsDomUtil.Tag.b, Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPE_0));
            CmsTypeListItem galleryItem = (CmsTypeListItem)m_scrollList.getItem(selectedTypes.get(0));
            String title = galleryItem.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = galleryItem.getSubTitle();
            }
            panelText = panelText + " " + title;
        } else {
            panelText += CmsDomUtil.enclose(CmsDomUtil.Tag.b, Messages.get().key(Messages.GUI_PARAMS_LABEL_TYPES_0));
            for (String galleryPath : selectedTypes) {

                CmsTypeListItem galleryItem = (CmsTypeListItem)m_scrollList.getItem(galleryPath);
                String title = galleryItem.getItemTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = galleryItem.getSubTitle();
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
     * Will be triggered when the tab is selected.<p>
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
            m_tabHandler.onTypesSort(event.getValue());
        }
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

    /**
     * Updates the types list.<p>
     * 
     * @param types the new types list
     * @param selectedTypes the list of types to select
     */
    public void updateContent(ArrayList<CmsTypesListInfoBean> types, List<String> selectedTypes) {

        clearList();
        for (CmsTypesListInfoBean typeBean : types) {
            // TODO: replace with CmsDraggableList Item see: CmsTabResultsPanel
            CmsListItemWidget listItemWidget = new CmsListItemWidget(typeBean);
            Image icon = new Image(typeBean.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(typeBean.getId(), checkBox));
            if (selectedTypes.contains(typeBean.getId())) {
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
     * Returns a list with sort values for this tab.<p>
     * 
     * @return list of sort order value/text pairs
     */
    private ArrayList<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));

        return list;
    }

}