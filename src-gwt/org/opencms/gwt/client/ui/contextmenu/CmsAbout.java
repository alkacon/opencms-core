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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;

/**
 * Dialog used to display the "About" information in an iframe.<p>
 */
public class CmsAbout implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /**
     * Creates a new context menu command.<p>
     *
     * @return the context menu command created
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsAbout();
    }

    /**
     * Shows the OpenCms about dialog.<p>
     */
    public static void showAbout() {

        final CmsPopup popup = new CmsPopup(Messages.get().key(Messages.GUI_ABOUT_DIALOG_TITLE_0));
        FlowPanel container = new FlowPanel();
        int height = 350;
        if (Window.getClientHeight() < height) {
            height = Math.max(300, Window.getClientHeight() - 50);
        }
        container.setHeight(height + "px");
        Frame frame = new Frame();

        frame.setWidth("100%");
        frame.setHeight("100%");
        frame.setUrl(CmsCoreProvider.get().getAboutLink());
        container.add(frame);
        popup.setMainContent(container);
        popup.center();
        popup.addDialogClose(null);
        CmsPushButton closeButton = new CmsPushButton();
        closeButton.setText(Messages.get().key(Messages.GUI_CLOSE_0));
        closeButton.setUseMinWidth(true);

        closeButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                popup.hide();
            }
        });
        popup.addButton(closeButton);
        popup.addDialogClose(null);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

        showAbout();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getItemWidget(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public A_CmsContextMenuItem getItemWidget(
        CmsUUID structureId,
        I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#hasItemWidget()
     */
    public boolean hasItemWidget() {

        return false;
    }

}
