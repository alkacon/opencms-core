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
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;

import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Widget to display the job table and additional buttons to perform actions  on the jobs.<p>
 */
public class CmsJobMainView extends VerticalLayout implements I_CmsJobEditHandler {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The app context. */
    private I_CmsAppUIContext m_appContext;

    /** Button to add a new job. */
    private Button m_buttonAddJob;

    /** Table containing the jobs. */
    protected CmsJobTable m_jobTable;

    /** Window used to display the dialog for editing jobs. Using object as the type to prevent declarative widget binding. */
    private Object m_window;

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
                editJob(jobInfo, "Create new job", new FutureCallback<CmsScheduledJobInfo>() {

                    public void onFailure(Throwable t) {
                        // never called

                    }

                    public void onSuccess(CmsScheduledJobInfo result) {

                        try {
                            OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), result);
                            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                            m_jobTable.reloadJobs();
                        } catch (CmsException e) {
                            CmsErrorDialog.showErrorDialog(e);
                        }

                    }
                });

            }
        });

    }

    /**
     * @see org.opencms.ui.apps.scheduler.I_CmsJobEditHandler#editJob(org.opencms.scheduler.CmsScheduledJobInfo, java.lang.String, com.google.common.util.concurrent.FutureCallback)
     */
    public void editJob(CmsScheduledJobInfo job, String caption, final FutureCallback<CmsScheduledJobInfo> callback) {

        CmsBasicDialog bd = new CmsBasicDialog();
        final CmsScheduledJobInfo jobCopy = (CmsScheduledJobInfo)job.clone();
        jobCopy.setActive(job.isActive());
        final CmsJobEditView editPanel = new CmsJobEditView(jobCopy);
        bd.setContent(editPanel);
        Window window = new Window(caption);
        m_window = window;
        window.setWidth("800px");
        window.setContent(bd);
        window.setModal(true);
        editPanel.loadFromBean(job);
        Button saveButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        bd.addButton(saveButton);
        Button cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        bd.addButton(cancelButton);
        cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                getWindow().close();
            }
        });

        saveButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (editPanel.trySaveToBean()) {
                    callback.onSuccess(jobCopy);
                    if (getWindow() != null) {
                        getWindow().close();
                    }
                }
            }
        });
        window.center();
        A_CmsUI.get().addWindow(window);

    }

    /**
     * Gets the currently opened window (may be null).<p>
     *
     * @return the currently opened window
     */
    protected Window getWindow() {

        return (Window)m_window;
    }
}
