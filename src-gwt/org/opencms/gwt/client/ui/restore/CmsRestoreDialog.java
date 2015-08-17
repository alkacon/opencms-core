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

package org.opencms.gwt.client.ui.restore;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.shared.CmsRestoreInfoBean;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * A dialog used for undoing changes to a resource and restoring it to its last published state.<p>
 */
public class CmsRestoreDialog extends CmsPopup {

    /** The content widget for this dialog. */
    protected CmsRestoreView m_restoreView;

    /** The structure id of the resource to undo changes for. */
    protected CmsUUID m_structureId;

    /** The action executed after the changes have been undone. */
    Runnable m_afterRestoreAction;

    /**
     * Creates a new instance of this dialog.<p>
     *
     * @param structureId the structure id of the resource which should be restored
     * @param afterRestoreAction the action which will be executed after the resource has been restored
     */
    public CmsRestoreDialog(CmsUUID structureId, Runnable afterRestoreAction) {

        super(CmsRestoreMessages.messageRestoreDialogTitle());
        setModal(true);
        setGlassEnabled(true);
        m_structureId = structureId;
        m_afterRestoreAction = afterRestoreAction;
    }

    /**
     * Loads the necessary data for the dialog from the server and shows the dialog.<p>
     */
    public void loadAndShow() {

        CmsRpcAction<CmsRestoreInfoBean> action = new CmsRpcAction<CmsRestoreInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getRestoreInfo(m_structureId, this);
            }

            @Override
            protected void onResponse(CmsRestoreInfoBean result) {

                stop(false);
                m_restoreView = new CmsRestoreView(result, m_afterRestoreAction);
                m_restoreView.setPopup(CmsRestoreDialog.this);
                setMainContent(m_restoreView);
                List<CmsPushButton> buttons = m_restoreView.getDialogButtons();
                for (CmsPushButton button : buttons) {
                    addButton(button);
                }
                setWidth(600);
                center();
            }
        };
        action.execute();
    }

}
