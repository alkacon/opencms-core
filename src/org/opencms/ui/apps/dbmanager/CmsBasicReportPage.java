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

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.report.CmsReportWidget;

import java.util.Collections;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the vaadin component to show a report of a thread.<p>
 */
public class CmsBasicReportPage extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5954274306940389324L;

    /**Vaadin component. */
    private Button m_ok;

    /**Vaadin component. */
    private Panel m_panel;

    /**Vaadin component. */
    private VerticalLayout m_reportContainer;

    /**
     * Public constructor.<p>
     *
     * @param label text for the caption of the panel
     * @param reportThread thread to start and show report for
     * @param callback runnable which gets called when the ok button is clicked
     */
    public CmsBasicReportPage(String label, final A_CmsReportThread reportThread, final Runnable callback) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_panel.setCaption(label);

        CmsReportWidget reportWidget = new CmsReportWidget(reportThread);
        reportWidget.setSizeFull();
        m_reportContainer.addComponent(reportWidget);
        if (reportThread != null) {
            addAttachListener(new AttachListener() {

                private static final long serialVersionUID = 7904284597826881723L;

                public void attach(AttachEvent event) {

                    if (reportThread.getState() == Thread.State.NEW) {
                        reportThread.start();
                    }
                }
            });
        }
        setSpacing(true);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6078619358416682278L;

            public void buttonClick(ClickEvent event) {

                callback.run();
            }
        });
        setData(Collections.singletonMap(A_CmsAttributeAwareApp.ATTR_MAIN_HEIGHT_FULL, Boolean.TRUE));
    }
}
