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
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CmsDbSynchDialog extends CmsBasicDialog {

    Runnable m_closeRunnable;

    Button m_okButton;

    Label m_icon;

    Button m_cancelButton;

    HorizontalLayout m_confirm;
    VerticalLayout m_report;

    public CmsDbSynchDialog(Runnable closeRunnable) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_report.setVisible(false);
        m_closeRunnable = closeRunnable;

        //Setup icon
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        m_okButton.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {

                showReport();

            }
        });

        m_cancelButton.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_closeRunnable.run();

            }
        });
    }

    protected void showReport() {

        m_confirm.setVisible(false);
        m_report.setVisible(true);
        CmsSynchronizeThread thread = new CmsSynchronizeThread(A_CmsUI.getCmsObject());
        thread.start();
        CmsReportWidget widget = new CmsReportWidget(thread);
        widget.setHeight("500px");
        widget.setWidth("100%");
        m_report.addComponent(widget);
        m_okButton.setEnabled(false);
    }
}
