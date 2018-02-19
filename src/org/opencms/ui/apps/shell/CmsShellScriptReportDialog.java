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

package org.opencms.ui.apps.shell;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsCopyToClipboardButton;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.util.CmsUUID;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

/**
 * Dialog to show shell script output.<p>
 */
public class CmsShellScriptReportDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 4838977228996268018L;

    /**
     * Public constructor.<p>
     *
     * @param thread Thread to be run
     * @param window holding this dialog
     */
    public CmsShellScriptReportDialog(A_CmsReportThread thread, final Window window) {

        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SHELL_SCRIPT_APP_OUTPUT_0));
        window.setHeight("600px");
        window.center();
        setSizeFull();
        CmsReportWidget reportWidget = new CmsReportWidget(thread);
        reportWidget.addStyleName(" o-shell-terminal");
        reportWidget.setHeight("100%");
        String id = "label" + new CmsUUID().getStringValue();
        reportWidget.setId(id);
        reportWidget.setWidth("100%");
        reportWidget.addStyleName("o-sroll-x");
        setContent(reportWidget);
        Button button = createButtonClose();
        addButton(button, true);
        button.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -5567381118325538754L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }
        });

        Button copy = new CmsCopyToClipboardButton(
            CmsVaadinUtils.getMessageText(org.opencms.ui.components.Messages.GUI_COPY_TO_CLIPBOARD_0),
            "#" + id);

        addButton(copy, false);

    }

}
