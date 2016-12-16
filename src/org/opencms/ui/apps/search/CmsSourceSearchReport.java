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

package org.opencms.ui.apps.search;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;

/**
 * The source search report.<p>
 */
public class CmsSourceSearchReport extends VerticalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 5867419212983288182L;

    /** The report widget. */
    private CmsReportWidget m_report;

    /** The show files button. */
    Button m_showFiles;

    /**
     * Constructor.<p>
     *
     * @param app the source search app
     * @param thread the report thread
     */
    public CmsSourceSearchReport(final CmsSourceSearchApp app, A_CmsReportThread thread) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_report.setReportThread(thread);
        m_report.addReportFinishedHandler(new Runnable() {

            public void run() {

                m_showFiles.setEnabled(true);
            }
        });
        //   m_reportPanel.setContent(report);
        m_showFiles.setEnabled(false);
        m_showFiles.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                app.displayResult();
            }
        });

    }

}
