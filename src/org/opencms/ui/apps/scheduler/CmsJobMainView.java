/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps.scheduler;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsErrorDialog;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Widget to display the job table and additional buttons to perform actions  on the jobs.<p>
 */
public class CmsJobMainView extends VerticalLayout implements I_CmsJobEditHandler {

    /** Log instance for this  class. */
    private static final Log LOG = CmsLog.getLog(CmsJobMainView.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The app context. */
    private I_CmsAppUIContext m_appContext;

    /** Button to add a new job. */
    private Button m_buttonAddJob;

    /** Table containing the jobs. */
    protected CmsJobTable m_jobTable;

    /**
     * Creates a new instance.<p>
     *
     * @param context the app context.
     */
    public CmsJobMainView(I_CmsAppUIContext context) {

        m_appContext = context;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_jobTable.setJobEditHandler(this);
        m_buttonAddJob.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        m_buttonAddJob.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        m_buttonAddJob.setIcon(CmsVaadinUtils.getWorkplaceResource("tools/scheduler/icons/big/scheduler_new.png"));
        m_buttonAddJob.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
                jobInfo.setContextInfo(new CmsContextInfo());
                editJob(jobInfo, "Create new job");

            }
        });

    }

    /**
     * @see org.opencms.ui.apps.scheduler.I_CmsJobEditHandler#editJob(org.opencms.scheduler.CmsScheduledJobInfo, java.lang.String)
     */
    public void editJob(CmsScheduledJobInfo job, String caption) {

        final CmsScheduledJobInfo jobCopy = (CmsScheduledJobInfo)job.clone();
        jobCopy.setActive(job.isActive());
        final CmsJobEditView editPanel = new CmsJobEditView(jobCopy);
        editPanel.setTitle(caption);
        editPanel.loadFromBean(jobCopy);

        Button saveButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));

        Button cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        editPanel.setButtons(saveButton, cancelButton);
        m_appContext.setAppContent(editPanel);
        CmsAppWorkplaceUi.get().changeCurrentAppState("edit");
        saveButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings({"synthetic-access"})
            public void buttonClick(ClickEvent event) {

                m_appContext.setAppContent(new CmsJobMainView(m_appContext));
                try {
                    if (editPanel.trySaveToBean()) {
                        OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), jobCopy);
                        OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                        restoreMainView();
                    }

                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    CmsErrorDialog.showErrorDialog(e, new Runnable() {

                        public void run() {

                            restoreMainView();

                        }
                    });
                }

            }

        });

        cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                restoreMainView();

            }
        });
    }

    /**
     * Restores the main view after leaving the editing mode.<p>
     */
    public void restoreMainView() {

        m_appContext.setAppContent(CmsJobMainView.this);
        m_jobTable.reloadJobs();
        CmsAppWorkplaceUi.get().changeCurrentAppState("");
    }

}
