/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragMenuElement.java,v $
 * Date   : $Date: 2010/05/05 09:49:43 $
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

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.ade.containerpage.client.ui.CmsDraggableListItemWidget;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Draggable menu element. Needed for favorite list.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsDragMenuElement extends CmsDraggableListItemWidget<I_CmsDragTargetContainer> {

    /** The element delete button. */
    private CmsPushButton m_deleteButton;

    /**
     * Constructor.<p>
     * 
     * @param element the element data
     */
    public CmsDragMenuElement(CmsContainerElement element) {

        super(new CmsListInfoBean(element.getTitle(), element.getFile(), null), true);
        setClientId(element.getClientId());
        m_deleteButton = new CmsPushButton();
        m_deleteButton.setImageClass(I_CmsImageBundle.INSTANCE.style().deleteIcon());
        m_deleteButton.setShowBorder(false);
        m_deleteButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        m_deleteButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                deleteElement();

            }
        });
    }

    /**
     * Removes the element from it's parent widget.<p>
     */
    public void deleteElement() {

        removeFromParent();
    }

    /**
     * Hides the element delete button.<p>
     */
    public void hideDeleteButton() {

        removeButton(m_deleteButton);
    }

    /**
     * Shows the element delete button.<p>
     */
    public void showDeleteButton() {

        addButton(m_deleteButton);
    }

}
