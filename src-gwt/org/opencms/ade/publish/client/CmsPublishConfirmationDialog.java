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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.client.CmsPublishDialog.State;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * A confirmation message dialog which can be displayed after the publish dialog has been closed.<p>
 */
public class CmsPublishConfirmationDialog extends CmsPopup {

    /** The link which is opened after the dialog is finished. */
    private String m_closeLink;

    /** Flag indicating whether current user is a workplace user. */
    private boolean m_isWorkplaceUser;

    /** The panel which contains the dialog contents. */
    private Panel m_panel = new HorizontalPanel();

    /**
     * Creates a new publish confirmation dialog.<p>
     *
     * @param dialog the publish dialog instance
     * @param closeLink the link to open after the dialog is finished
     */
    public CmsPublishConfirmationDialog(CmsPublishDialog dialog, String closeLink) {

        super();
        setModal(true);
        setGlassEnabled(true);
        CmsPublishDialog.State state = dialog.getState();
        m_closeLink = closeLink;
        m_isWorkplaceUser = CmsCoreProvider.get().getUserInfo().isWorkplaceUser();
        String message = "-";
        if (state == State.success) {
            CmsWorkflowAction lastAction = dialog.getLastAction();
            if (lastAction.isPublish()) {
                message = getMessage(
                    m_isWorkplaceUser ? Messages.GUI_CONFIRMATION_PUBLISH_0 : Messages.GUI_CONFIRMATION_PUBLISH_NOWP_0);
            } else {
                message = getMessage(
                    m_isWorkplaceUser
                    ? Messages.GUI_CONFIRMATION_WORKFLOW_1
                    : Messages.GUI_CONFIRMATION_WORKFLOW_NOWP_1,
                    lastAction.getLabel());
            }
        } else if (state == State.failure) {
            message = dialog.getFailureMessage();
        }
        Label label = new Label(message);
        label.getElement().getStyle().setPaddingLeft(10, Unit.PX);
        m_panel.getElement().getStyle().setPadding(10, Unit.PX);
        m_panel.getElement().getStyle().setHeight(150, Unit.PX);
        Label checkmark = new Label();
        checkmark.addStyleName(I_CmsPublishLayoutBundle.INSTANCE.publishCss().checkmark());
        m_panel.add(checkmark);
        m_panel.add(label);

        setMainContent(m_panel);
        setCaption(getMessage(Messages.GUI_CONFIRMATION_CAPTION_0));
        for (CmsPushButton button : getButtons()) {
            addButton(button);
        }
    }

    /**
     * Produces the buttons for this dialog.<p>
     *
     * @return a list of buttons that should be displayed
     */
    protected List<CmsPushButton> getButtons() {

        CmsPushButton workplaceButton = new CmsPushButton();
        workplaceButton.setText(getMessage(Messages.GUI_CONFIRMATION_WORKPLACE_BUTTON_0));
        workplaceButton.addClickHandler(new ClickHandler() {

            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent e) {

                Window.Location.assign(m_closeLink);
            }
        });
        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        if (m_isWorkplaceUser) {
            result.add(workplaceButton);
        }
        return result;
    }

    /**
     * Helper method to get a localized message.<p>
     *
     * @param key the message key
     * @param args the message parameters
     *
     * @return the localized message
     */
    protected String getMessage(String key, Object... args) {

        return Messages.get().getBundle().key(key, args);
    }

}
