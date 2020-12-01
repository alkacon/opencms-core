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

package org.opencms.ui.apps.user;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog for reports.<p>
 */
public class CmsShowReportDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1267691928074775090L;

    /**
     * Public constructor.<p>
     *
     * @param thread to be shown
     * @param close Runnable run on close click
     */
    public CmsShowReportDialog(A_CmsReportThread thread, Runnable close) {

        setHeight(CmsImportExportUserDialog.DIALOG_HEIGHT);
        VerticalLayout panel = new VerticalLayout();
        panel.setSizeFull();
        CmsReportWidget widget = new CmsReportWidget(thread);
        widget.setSizeFull();
        panel.addComponent(widget);
        setContent(panel);

        Button closeButton = new Button(CmsVaadinUtils.messageClose());
        closeButton.addClickListener(event -> close.run());
        addButton(closeButton, true);

    }

}
