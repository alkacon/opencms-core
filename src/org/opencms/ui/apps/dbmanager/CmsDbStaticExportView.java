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

package org.opencms.ui.apps.dbmanager;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.report.CmsReportDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the static export view.<p>
 */
public class CmsDbStaticExportView extends VerticalLayout {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 6812301161700680358L;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private Panel m_startPanel;

    /**
     * public constructor.<p>
     */
    public CmsDbStaticExportView() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 3329214665347113504L;

            public void buttonClick(ClickEvent event) {

                startThread();
            }
        });
    }

    /**
     * Start export thread.<p>
     */
    void startThread() {

        m_startPanel.setVisible(false);
        CmsStaticExportThread thread = new CmsStaticExportThread(A_CmsUI.getCmsObject());
        String title = CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATEXP_ADMIN_TOOL_NAME_0);
        CmsReportDialog.showReportDialog(title, thread);
    }
}
