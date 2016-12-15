/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Dialog used for confirming the removal of an element in the container page editor.<p>
 */
public class CmsConfirmRemoveDialog extends CmsPopup {

    /** The widget with the dialog contents. */
    private CmsConfirmRemoveWidget m_widget;

    /**
     * Creates a new instance.<p>
     *
     * @param elementInfo the data for the resource info box
     * @param deleteCheckbox true if the checkbox for deleting the resource should be displayed
     * @param deleteMode the element delete mode
     * @param removeCallback the callback which should be called when the user has confirmed or cancelled the element removal
     */
    public CmsConfirmRemoveDialog(
        CmsListInfoBean elementInfo,
        boolean deleteCheckbox,
        ElementDeleteMode deleteMode,
        AsyncCallback<Boolean> removeCallback) {

        m_widget = new CmsConfirmRemoveWidget(elementInfo, deleteCheckbox, removeCallback);
        m_widget.setPopup(this);
        switch (deleteMode) {
            default:
            case askDelete:
                break;
            case askKeep:
                m_widget.m_checkBox.setChecked(false);
                m_widget.m_deleteContent = false;
                break;
            case alwaysKeep:
                m_widget.m_checkBox.setChecked(false);
                m_widget.m_deleteContent = false;
                //$FALL-THROUGH$
            case alwaysDelete:
                m_widget.m_checkBox.setVisible(false);
                m_widget.m_noReferenceLabel.setVisible(false);
                break;
        }

        setMainContent(m_widget);
        setModal(true);
        setGlassEnabled(true);
        setCaption(CmsConfirmRemoveWidget.MessageStrings.caption());
        for (CmsPushButton button : m_widget.getButtons()) {
            addButton(button);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {

        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYPRESS:
                switch (event.getNativeEvent().getKeyCode()) {
                    case KeyCodes.KEY_ENTER:
                        event.cancel();
                        m_widget.onClickCancel(null);
                        break;
                    case KeyCodes.KEY_ESCAPE:
                        event.cancel();
                        m_widget.onClickOk(null);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

}
