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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Draggable menu element. Needed for favorite list.<p>
 * 
 * @since 8.0.0
 */
public class CmsMenuListItem extends CmsListItem {

    /** The element delete button. */
    private CmsPushButton m_removeButton;

    /**
     * Constructor.<p>
     * 
     * @param element the element data
     */
    public CmsMenuListItem(CmsContainerElementData element) {

        super(new CmsListItemWidget(new CmsListInfoBean(
            element.getTitle(),
            element.getSitePath(),
            element.getFormatedIndividualSettings())));
        if (!m_listItemWidget.hasAdditionalInfo()) {
            m_listItemWidget.addAdditionalInfo(new CmsAdditionalInfoBean("", Messages.get().key(
                Messages.GUI_NO_SETTINGS_TITLE_0), null));
        }
        setId(element.getClientId());
        getListItemWidget().setIcon(CmsIconUtil.getResourceIconClasses(element.getResourceType(), false));
        m_removeButton = new CmsPushButton();
        m_removeButton.setImageClass(I_CmsImageBundle.INSTANCE.style().removeIcon());
        m_removeButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_removeButton.setTitle(Messages.get().key(Messages.GUI_BUTTON_REMOVE_TEXT_0));
        m_removeButton.addClickHandler(new ClickHandler() {

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
    public void hideRemoveButton() {

        getListItemWidget().removeButton(m_removeButton);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    @Override
    public void onDragCancel() {

        super.onDragCancel();
        clearDrag();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onDrop(I_CmsDropTarget target) {

        super.onDrop(target);
        clearDrag();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void onStartDrag(I_CmsDropTarget target) {

        super.onStartDrag(target);
        getElement().getStyle().setOpacity(0.5);
    }

    /**
     * Shows the element delete button.<p>
     */
    public void showRemoveButton() {

        getListItemWidget().addButton(m_removeButton);
    }

    /**
     * Sets the icon style.<p>
     * 
     * @param imageClass the image class to set
     * @param title the title (tool-tip) to set
     */
    protected void setMoveIconStyle(String imageClass, String title) {

        if (getMoveHandle() instanceof CmsPushButton) {
            CmsPushButton button = (CmsPushButton)getMoveHandle();
            button.setImageClass(imageClass);
            button.setTitle(title);
        }
    }

    /**
     * Removes all styling done during drag and drop.<p>
     */
    private void clearDrag() {

        // using own implementation as GWT won't do it properly on IE7-8
        CmsDomUtil.clearOpacity(getElement());

        getElement().getStyle().clearDisplay();
    }
}
