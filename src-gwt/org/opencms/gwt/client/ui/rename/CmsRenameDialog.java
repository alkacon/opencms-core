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

package org.opencms.gwt.client.ui.rename;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.shared.CmsRenameInfoBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The dialog for renaming a given resource.<p>
 */
public class CmsRenameDialog extends CmsPopup {

    /** The handler which should be called when the resource has been successfully renamed. */
    AsyncCallback<String> m_renameHandler;

    /** The structure id of the resource to be renamed. */
    CmsUUID m_structureId;

    /**
     * Creates a new instance.<p>
     *
     * @param structureId the structure id of the resource to be renamed
     * @param renameHandler the handler which should be called when the resource has been renamed
     */
    public CmsRenameDialog(CmsUUID structureId, AsyncCallback<String> renameHandler) {

        super(CmsRenameMessages.messageDialogTitle());
        setModal(true);
        setGlassEnabled(true);
        m_structureId = structureId;
        m_renameHandler = renameHandler;
    }

    /**
     * Loads the necessary data for the rename dialog from the server and then displays the rename dialog.<p>
     */
    public void loadAndShow() {

        CmsRpcAction<CmsRenameInfoBean> infoAction = new CmsRpcAction<CmsRenameInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getRenameInfo(m_structureId, this);
            }

            @Override
            protected void onResponse(CmsRenameInfoBean renameInfo) {

                stop(false);
                CmsRenameView view = new CmsRenameView(renameInfo, m_renameHandler);
                for (CmsPushButton button : view.getDialogButtons()) {
                    addButton(button);
                }
                setMainContent(view);
                view.setDialog(CmsRenameDialog.this);
                center();
            }
        };
        infoAction.execute();
    }
}
