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

package org.opencms.ui.apps.modules;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

/**
 * The module export dialog.<p>
 */
public class CmsModuleExportDialog extends CmsBasicDialog {

    /** Vaadin serial id.*/
    private static final long serialVersionUID = 1L;

    /**
     * public constructor.<p>
     *
     * @param thread to be run
     * @param window holds the dialog
     */
    public CmsModuleExportDialog(A_CmsReportThread thread, final Window window) {

        Button close = createButtonClose();
        close.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }

        });
        addButton(close, true);
        CmsReportWidget report = new CmsReportWidget(thread);
        setHeight("100%");
        report.setWidth("100%");
        report.setHeight("100%");

        setContent(report);
        thread.start();
    }
}
