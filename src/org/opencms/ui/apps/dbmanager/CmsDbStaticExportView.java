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
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the static export view.<p>
 */
public class CmsDbStaticExportView extends VerticalLayout {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 6812301161700680358L;

    /**Vaadin component.*/
    private VerticalLayout m_layout;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private Label m_report;

    /**Vaadin component.*/
    private Panel m_reportPanel;

    /**Vaadin component.*/
    private Panel m_startPanel;

    /**
     * public constructor.<p>
     */
    public CmsDbStaticExportView() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_report.setHeight("500px");
        m_layout.setWidth("100%");
        m_report.addStyleName("v-scrollable");
        m_report.addStyleName("o-report");

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 3329214665347113504L;

            public void buttonClick(ClickEvent event) {

                startThread();
            }
        });
        m_reportPanel.setVisible(false);
    }

    /**
     * Start export thread.<p>
     */
    void startThread() {

        m_reportPanel.setVisible(true);
        m_startPanel.setVisible(false);
        m_report.setVisible(false);
        CmsStaticExportThread thread = new CmsStaticExportThread(A_CmsUI.getCmsObject());
        CmsReportWidget report = new CmsReportWidget(thread);
        report.setHeight("500px");
        report.setWidth("100%");
        m_layout.addComponent(report);
        thread.start();
    }
}
