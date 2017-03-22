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

package org.opencms.ui.apps.publishqueue;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Vertical Layout showing a publish report of a publish job.<p>
 */

public class CmsPublishReport extends VerticalLayout {

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsPublishReport.class.getName());

    /**vaadin serial id. */
    private static final long serialVersionUID = -1630983150603283505L;

    /**object which calls table.*/
    CmsPublishQueue m_manager;

    /**vaadin component.*/
    private Button m_cancel;

    /**job id.*/
    private CmsUUID m_jobId;

    /**vaadin component.*/
    private VerticalLayout m_panel;

    /**
     * public constructor.<p>
     *
     * @param queue calling object
     * @param jobId of chosen job
     */
    public CmsPublishReport(CmsPublishQueue queue, String jobId) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_manager = queue;
        m_jobId = new CmsUUID(jobId);

        //Obtain job and fill panel with CmsReportWidget
        final CmsPublishJobBase job = OpenCms.getPublishManager().getJobByPublishHistoryId(m_jobId);
        //switch for job type
        if (job instanceof CmsPublishJobRunning) {
            //Running job
            A_CmsReportThread thread = OpenCms.getThreadStore().retrieveThread(
                ((CmsPublishJobRunning)job).getThreadUUID());
            CmsReportWidget report = new CmsReportWidget(thread);

            report.setWidth("100%");
            report.setHeight("700px");
            m_panel.addComponent(report);

        } else {
            //finished job
            String reportHTML = "";
            try {
                reportHTML = new String(OpenCms.getPublishManager().getReportContents((CmsPublishJobFinished)job));
            } catch (CmsException e) {
                LOG.error("Error reading Report content of publish job.", e);
            }
            Label label = new Label();
            label.setValue(reportHTML);
            label.setContentMode(ContentMode.HTML);
            label.setHeight("700px");
            label.addStyleName("v-scrollable");
            label.addStyleName("o-report");
            m_panel.addComponent(label);
        }

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -1921697074171843576L;

            public void buttonClick(ClickEvent event) {

                if (job instanceof CmsPublishJobRunning) {
                    m_manager.openSubView("", true);
                } else {
                    m_manager.openSubView(CmsPublishQueue.PATH_HISTORY, true);
                }
            }
        });
    }
}
