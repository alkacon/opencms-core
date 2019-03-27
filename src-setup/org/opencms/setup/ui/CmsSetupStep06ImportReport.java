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

package org.opencms.setup.ui;

import org.opencms.setup.CmsVaadinSetupWorkplaceImportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsStreamReportWidget;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

/**
 * Setup step: Module import via CmsShell.
 */
public class CmsSetupStep06ImportReport extends A_CmsSetupStep {

    /** Forward button. */
    private Button m_forwardButton;

    /** The log stream. */
    private OutputStream m_logStream;

    /** The main layout. */
    private VerticalLayout m_mainLayout;

    /**
     * Creates a new instance.
     * @param context the setup context
     */
    public CmsSetupStep06ImportReport(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        m_forwardButton.addClickListener(evt -> forward());
        final CmsStreamReportWidget report = new CmsStreamReportWidget();
        report.setWidth("100%");
        report.setHeight("100%");
        m_forwardButton.setEnabled(false);
        m_mainLayout.addComponent(report);
        try {
            m_logStream = new FileOutputStream(context.getSetupBean().getLogName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        report.setDelegateStream(m_logStream);
        report.addReportFinishedHandler(() -> {
            m_forwardButton.setEnabled(true);
            try {
                m_logStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        Thread thread = new CmsVaadinSetupWorkplaceImportThread(context.getSetupBean(), report);
        thread.start();
    }

    /**
     * Proceed to next step.
     */
    public void forward() {

        m_context.stepForward();

    }

    /**
     * @see org.opencms.setup.ui.A_CmsSetupStep#getTitle()
     */
    @Override
    public String getTitle() {

        return "OpenCms setup - Importing modules";
    }

}
