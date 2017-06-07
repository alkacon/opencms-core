/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Draggable menu element. Needed for favorite list.<p>
 *
 * @since 8.0.0
 */
public class CmsMenuListItem extends CmsListItem {

    /** The element edit button. */
    protected CmsPushButton m_editButton;

    /** The edit click handler registration. */
    private HandlerRegistration m_editHandlerRegistration;

    /** The element delete button. */
    private CmsPushButton m_removeButton;

    /**
     * Constructor.<p>
     *
     * @param element the element data
     */
    public CmsMenuListItem(CmsContainerElementData element) {

        super(new CmsListItemWidget(new CmsListInfoBean(element.getTitle(), element.getSitePath(), null)));
        if (!m_listItemWidget.hasAdditionalInfo()) {
            m_listItemWidget.addAdditionalInfo(
                new CmsAdditionalInfoBean("", Messages.get().key(Messages.GUI_NO_SETTINGS_TITLE_0), null));
        }
        setId(element.getClientId());
        getListItemWidget().setIcon(CmsIconUtil.getResourceIconClasses(element.getResourceType(), false));

        m_removeButton = new CmsPushButton();
        m_removeButton.setImageClass(I_CmsButton.CUT_SMALL);
        m_removeButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_removeButton.setTitle(Messages.get().key(Messages.GUI_BUTTON_REMOVE_TEXT_0));
        m_removeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                deleteElement();

            }
        });
        m_editButton = new CmsPushButton();
        m_editButton.setImageClass(I_CmsButton.ButtonData.EDIT.getSmallIconClass());
        m_editButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_editButton.setTitle(
            org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_BUTTON_ELEMENT_EDIT_0));
        m_editButton.setEnabled(false);
        getListItemWidget().addButton(m_editButton);
    }

    /**
     * Removes the element from it's parent widget.<p>
     */
    public void deleteElement() {

        removeFromParent();
    }

    /**
     * Disables the edit button with the given reason.<p>
     *
     * @param reason the disable reason
     * @param locked <code>true</code> if the resource is locked
     */
    public void disableEdit(String reason, boolean locked) {

        m_editButton.disable(reason);
        if (locked) {
            m_editButton.setImageClass("opencms-icon-lock-20");
        }
    }

    /**
     * Enables the edit button with the given click handler.<p>
     *
     * @param editClickHandler the edit click handler
     */
    public void enableEdit(ClickHandler editClickHandler) {

        if (m_editHandlerRegistration != null) {
            m_editHandlerRegistration.removeHandler();
        }
        m_editHandlerRegistration = m_editButton.addClickHandler(editClickHandler);
        m_editButton.enable();
    }

    /**
     * Hides the edit button.<p>
     */
    public void hideEditButton() {

        if (m_editButton != null) {
            getListItemWidget().removeButton(m_editButton);
        }
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
        getElement().getStyle().setOpacity(0.7);
    }

    /**
     * Shows the element edit button.<p>
     */
    public void showEditButton() {

        if (m_editButton != null) {
            getListItemWidget().addButton(m_editButton);
        }
    }

    /**
     * Shows the element delete button.<p>
     */
    public void showRemoveButton() {

        getListItemWidget().addButton(m_removeButton);
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
