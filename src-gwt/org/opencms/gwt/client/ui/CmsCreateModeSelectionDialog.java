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

package org.opencms.gwt.client.ui;

import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A  dialog used to select the create mode for new contents created from a collector list.<p>
 */
public class CmsCreateModeSelectionDialog extends CmsPopup {

    /** The callback which is called with the create mode selected by the user. */
    AsyncCallback<String> m_callback;

    /**
     * Creates a new dialog instance.<p>
     *
     * @param info the resource information for the selected collector list entry
     * @param createModeCallback the callback to call with the result
     */
    public CmsCreateModeSelectionDialog(CmsListInfoBean info, final AsyncCallback<String> createModeCallback) {

        super("");
        setCaption(messageCaption());
        m_callback = createModeCallback;
        setModal(true);
        setGlassEnabled(true);
        CmsCreateModeSelectionView main = new CmsCreateModeSelectionView();
        main.getLabel().setText(messageAskMode());

        CmsListItemWidget item = new CmsListItemWidget(info);
        main.getInfoBox().add(item);
        setMainContent(main);
        addButtons();
        Command closeCommand = new Command() {

            public void execute() {

                createModeCallback.onFailure(null);
            }
        };
        addDialogClose(closeCommand);
    }

    /**
     * Shows the dialog for the given collector list entry.<p>
     *
     * @param referenceId the structure id of the collector list entry
     * @param createModeCallback the callback which should be called with the selected create mode
     */
    public static void showDialog(final CmsUUID referenceId, final AsyncCallback<String> createModeCallback) {

        CmsRpcAction<CmsListInfoBean> action = new CmsRpcAction<CmsListInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getPageInfo(referenceId, this);

            }

            @Override
            protected void onResponse(CmsListInfoBean result) {

                stop(false);
                Boolean isFolder = result.getIsFolder();
                if ((isFolder != null) && isFolder.booleanValue()) {
                    createModeCallback.onSuccess(null);
                } else {
                    (new CmsCreateModeSelectionDialog(result, createModeCallback)).center();
                }

            }
        };
        action.execute();

    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public String messageAskMode() {

        return Messages.get().key(Messages.GUI_CREATE_MODE_ASK_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public String messageCaption() {

        return Messages.get().key(Messages.GUI_CREATE_MODE_CAPTION_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public String messageCopy() {

        return Messages.get().key(Messages.GUI_CREATE_MODE_BUTTON_COPY_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public String messageNew() {

        return Messages.get().key(Messages.GUI_CREATE_MODE_BUTTON_NEW_0);
    }

    /**
     * Adds the dialog buttons.<p>
     */
    protected void addButtons() {

        addButton(createButton(messageNew(), ButtonColor.BLUE, null));
        addButton(createButton(messageCopy(), ButtonColor.GREEN, CmsEditorConstants.MODE_COPY));

    }

    /**
     * Creates a button used to select a create mode.<p>
     *
     * @param text the button text
     * @param color the button color
     * @param result the create mode selected by the button
     *
     * @return the newly created button
     */
    protected CmsPushButton createButton(String text, ButtonColor color, final String result) {

        CmsPushButton button = new CmsPushButton();
        button = new CmsPushButton();
        button.setText(text);
        button.setUseMinWidth(true);
        button.setButtonStyle(ButtonStyle.TEXT, color);
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                CmsCreateModeSelectionDialog.this.hide();
                m_callback.onSuccess(result);
            }
        });
        return button;

    }

}
