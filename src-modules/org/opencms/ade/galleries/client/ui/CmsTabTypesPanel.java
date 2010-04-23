/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsTabTypesPanel.java,v $
 * Date   : $Date: 2010/04/23 10:08:25 $
 * Version: $Revision: 1.1 $
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

import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
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

import com.google.gwt.user.client.ui.Image;

/**
 * Provides a widget for the content of the types tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
//TODO: extends this panel to drag the tlist items
public class CmsTabTypesPanel extends CmsTabInnerPanel {

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> m_dragHandler;

    /**
     * The default constructor with drag handler.<p>
     * 
     * @param infoBean the reference to the gallery info bean 
     * @param handler the reference to drag handler
     */
    public CmsTabTypesPanel(CmsGalleryInfoBean infoBean, I_CmsDragHandler<I_CmsDragElement, I_CmsDragTarget> handler) {

        super();
        m_dragHandler = handler;
        fillContent(infoBean);
    }

    /**
     * Fill the content of the types tab panel.<p>
     * 
     * @param infoBean the gallery info bean containing the current search parameters
     */
    public void fillContent(CmsGalleryInfoBean infoBean) {

        // TODO: replace the dummy select box
        ArrayList<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        pairs.add(new CmsPair<String, String>("test1", "value1"));
        pairs.add(new CmsPair<String, String>("test2", "value2"));
        CmsSelectBox selectBox = new CmsSelectBox(pairs);
        // TODO: use the common way to set the width of the select box
        selectBox.setWidth("100px");
        addWidgetToOptions(selectBox);
        for (Map.Entry<String, CmsTypesListInfoBean> typeItem : infoBean.getDialogInfo().getTypes().entrySet()) {
            // TODO: replace with CmsDraggableList Item see: CmsTabResultsPanel
            CmsListItemWidget listItemWidget = new CmsListItemWidget(typeItem.getValue());
            Image icon = new Image(typeItem.getValue().getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            CmsTypeListItem listItem = new CmsTypeListItem(infoBean, new CmsCheckBox(), listItemWidget);
            listItem.setId(typeItem.getKey());
            addWidgetToList(listItem);
        }
    }
}