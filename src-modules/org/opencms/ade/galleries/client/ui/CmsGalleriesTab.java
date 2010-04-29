/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleriesTab.java,v $
 * Date   : $Date: 2010/04/29 07:37:52 $
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

import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
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
 * Provides the widget for the galleries(folder) tab.<p>
 * 
 * It displays the available gallery folders in the given order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsGalleriesTab extends A_CmsTab {

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

    /** The reference to the handler of this tab. */
    protected CmsGalleriesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     */
    public CmsGalleriesTab() {

        super();
    }

    /**
     * Fill the content of the galleries tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the current search parameters
     */
    public void fillContent(CmsGalleryDialogBean dialogBean) {

        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsGalleriesListInfoBean> galleryItem : dialogBean.getGalleries().entrySet()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(galleryItem.getValue());
            Image icon = new Image(galleryItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsCheckBox checkBox = new CmsCheckBox();
            checkBox.addClickHandler(new CheckboxHandler(galleryItem.getValue().getId(), checkBox));
            CmsGalleryListItem listItem = new CmsGalleryListItem(checkBox, listItemWidget);
            listItem.setId(galleryItem.getKey());
            addWidgetToList(listItem);
        }
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
}