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

package org.opencms.setup.updater.dialogs;

import org.opencms.setup.CmsUpdateUI;
import org.opencms.setup.db.CmsVaadinUpdateDBThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsStreamReportWidget;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Panel;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * DB Update thread dialog.<p>
 */
public class CmsUpdateStep03DBThreadDialog extends A_CmsUpdateDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**Vaadin component. */
    private Panel m_reportPanel;

    /**Vaadin component. */
    private Label m_icon;

    /**Vaadin component. */
    private Label m_iconFin;

    /**Vaadin component. */
    private HorizontalLayout m_running;

    /**Vaadin component. */
    private HorizontalLayout m_finished;

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#init(org.opencms.setup.CmsUpdateUI)
     */
    @Override
    public boolean init(CmsUpdateUI ui) {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        super.init(ui, false, true);

        setCaption("OpenCms Update-Wizard - Update database");
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontAwesome.CLOCK_O.getHtml());
        m_iconFin.setContentMode(ContentMode.HTML);
        m_iconFin.setValue(FontAwesome.CHECK_CIRCLE_O.getHtml());
        m_finished.setVisible(false);
        m_reportPanel.setContent(getReportContent());
        return true;
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getNextDialog()
     */
    @Override
    A_CmsUpdateDialog getNextDialog() {

        return new CmsUpdateStep04SettingsDialog();
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getPreviousDialog()
     */
    @Override
    A_CmsUpdateDialog getPreviousDialog() {

        return null;
    }

    /**
     * Gets the content.<p>
     *
     * @return VerticalLayout
     */
    private VerticalLayout getReportContent() {

        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("100%");
        final CmsStreamReportWidget report = new CmsStreamReportWidget();
        report.setWidth("100%");
        report.setHeight("100%");
        enableOK(false);
        Thread thread = new CmsVaadinUpdateDBThread(m_ui.getUpdateBean(), report);
        thread.start();
        try {
            OutputStream logStream = new FileOutputStream(m_ui.getUpdateBean().getLogName());
            report.setDelegateStream(logStream);
            report.addReportFinishedHandler(() -> {

                try {
                    logStream.close();
                    report.getStream().close();
                    enableOK(true);
                    m_finished.setVisible(true);
                    m_running.setVisible(false);
                } catch (IOException e) {
                    //
                }
            });

        } catch (FileNotFoundException e1) {
            //
        }
        layout.addComponent(report);
        return layout;
    }

}
