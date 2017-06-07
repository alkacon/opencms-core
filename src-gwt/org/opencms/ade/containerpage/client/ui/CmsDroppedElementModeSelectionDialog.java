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
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Dialog for selecting between copying and reusing an element dropped from the clipboard into the page.<p>
 */
public class CmsDroppedElementModeSelectionDialog extends CmsCreateModeSelectionDialog {

    /**
     * Creates a new instance.<p>
     *
     * @param info the file information
     *
     * @param createModeCallback the callback to call with the result
     */
    public CmsDroppedElementModeSelectionDialog(CmsListInfoBean info, AsyncCallback<String> createModeCallback) {

        super(info, createModeCallback);
    }

    /**
     * Shows the dialog.<p>
     *
     * @param referenceId the  structure id of the resource for which to load the dialog
     * @param createModeCallback the callback to call with the result
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
                (new CmsDroppedElementModeSelectionDialog(result, createModeCallback)).center();
            }
        };
        action.execute();

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog#messageAskMode()
     */
    @Override
    public String messageAskMode() {

        return Messages.get().key(Messages.GUI_SELECT_COPY_OR_REUSE_TEXT_0);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog#messageCaption()
     */
    @Override
    public String messageCaption() {

        return Messages.get().key(Messages.GUI_SELECT_COPY_OR_REUSE_CAPTION_0);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog#messageCopy()
     */
    @Override
    public String messageCopy() {

        return Messages.get().key(Messages.GUI_COPY_ELEMENT_0);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog#messageNew()
     */
    @Override
    public String messageNew() {

        return Messages.get().key(Messages.GUI_REUSE_ELEMENT_0);

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsCreateModeSelectionDialog#addButtons()
     */
    @Override
    protected void addButtons() {

        addButton(createButton(messageNew(), ButtonColor.BLUE, CmsEditorConstants.MODE_REUSE));
        addButton(createButton(messageCopy(), ButtonColor.GREEN, CmsEditorConstants.MODE_COPY));

    }

}
