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

package org.opencms.gwt.client.ui.resourcehelp;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.shared.CmsResourceHelpDialogType;
import org.opencms.gwt.shared.CmsResourceTypeHelpBean;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * the dialog with help information
 */
public class CmsResourceHelpDialog extends CmsPopup {

    /** The content widget for this dialog. */
    protected CmsResourceHelpView m_helpView;
    /** The structure id of the resource to undo changes for. */
    protected CmsUUID m_structureId;

    /** The dialog type */
    protected CmsResourceHelpDialogType m_dialogType;

    /**
     * Creates the dialog for the given resource help information.<p>
     * @param structureId the structureId of resource
     * @param dialogType the dialog type
     *
     */
    public CmsResourceHelpDialog(final CmsUUID structureId, final CmsResourceHelpDialogType dialogType) {
        super();
        m_structureId = structureId;
        m_dialogType = dialogType;
        setModal(true);
        setGlassEnabled(true);
        addDialogClose(null);
        setWidth(-1);
    }

    /**
     * Loads the resource help information for a resource and displays it in a dialog.<p>
     *
     */
    public void loadAndShow() {

        CmsRpcAction<List<CmsResourceTypeHelpBean>> action = new CmsRpcAction<List<CmsResourceTypeHelpBean>>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getResourceTypeHelp(m_structureId, m_dialogType, this);
            }

            @Override
            protected void onResponse(List<CmsResourceTypeHelpBean> result) {

                stop(false);
                if ((null == result) || result.isEmpty()) {
                    return;
                }
                m_helpView = new CmsResourceHelpView(result);
                m_helpView.setPopup(CmsResourceHelpDialog.this);
                if (!m_dialogType.equals(CmsResourceHelpDialogType.START)) {
                    m_helpView.getCheckboxNotShowOnStart().setVisible(false);
                }
                m_helpView.changeContent();
                setMainContent(m_helpView);
                List<CmsPushButton> buttons = m_helpView.getDialogButtons();
                for (CmsPushButton button : buttons) {
                    addButton(button);
                }
                center();
            }
        };
        action.execute();
    }

}
