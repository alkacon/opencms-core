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

package org.opencms.ui.report;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsBasicDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Simple dialog for displaying a report.
 */
public class CmsReportDialog extends CmsBasicDialog {

    /** Vaadin serial id.*/
    private static final long serialVersionUID = 1L;

    /**
     * Public constructor.<p>
     *
     * @param thread to be run
     * @param window holds the dialog
     */
    public CmsReportDialog(A_CmsReportThread thread, final Window window) {

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
        VerticalLayout container = new VerticalLayout();
        container.setHeight("100%");
        container.addComponent(report);
        setContent(container);
    }

    /**
     * Creates a new window and displays the given report thread's output in it.
     *
     * <p>Does <i>not</i> start the thread.
     *
     * @param title the title for the window
     * @param thread the thread whose report should be displayed
     */
    public static void showReportDialog(String title, A_CmsReportThread thread) {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        window.setCaption(title);
        window.setHeight("500px");
        window.setContent(new CmsReportDialog(thread, window));
        A_CmsUI.get().addWindow(window);
        thread.start();
    }

}
