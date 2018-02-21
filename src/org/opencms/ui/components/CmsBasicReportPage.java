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

package org.opencms.ui.components;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.report.CmsReportWidget;

import java.util.Collections;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Page to display a report.<p>
 */
public class CmsBasicReportPage extends VerticalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The OK button. */
    private Button m_ok;

    /** The panel for the report. */
    private Panel m_panel;

    /** The immediate parent layout of the report. */
    private VerticalLayout m_reportContainer;

    /**
     * Creates a new instance.<p>
     *
     * @param label the caption for the panel
     * @param reportThread the report thread whose output should be displayed
     * @param callback the callback to call when the user clicks OK
     */
    public CmsBasicReportPage(String label, final A_CmsReportThread reportThread, final Runnable callback) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_panel.setCaption(label);
        CmsReportWidget reportWidget = new CmsReportWidget(reportThread);
        reportWidget.setSizeFull();
        m_reportContainer.addComponent(reportWidget);
        if (reportThread != null) {
            addAttachListener(new AttachListener() {

                private static final long serialVersionUID = 1L;

                public void attach(AttachEvent event) {

                    if (reportThread.getState() == Thread.State.NEW) {
                        reportThread.start();
                    }
                }
            });
        }
        setSpacing(true);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                callback.run();
            }

        });
        setData(Collections.singletonMap(A_CmsAttributeAwareApp.ATTR_MAIN_HEIGHT_FULL, Boolean.TRUE));
    }

}
