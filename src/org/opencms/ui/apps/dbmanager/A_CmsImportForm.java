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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Abstract class for a form to import a file.<p>
 */
public abstract class A_CmsImportForm extends VerticalLayout {

    /**Log object for class.*/
    private static final Log LOG = CmsLog.getLog(A_CmsImportForm.class);

    /**vaadin serial id.*/
    private static final long serialVersionUID = -4074089542763339000L;

    /**App which uses the form.*/
    protected I_CmsReportApp m_app;

    /**Import file object.*/
    protected CmsImportFile m_importFile;

    /**
     * public constructor.<p>
     *
     * @param app calling instance of app
     * */
    public A_CmsImportForm(I_CmsReportApp app) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_app = app;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        getOkButton().setEnabled(false);

        final IndexedContainer availableSites = CmsVaadinUtils.getAvailableSitesContainer(cms, "caption");

        getSiteSelector().setContainerDataSource(availableSites);
        if (availableSites.getItem(cms.getRequestContext().getSiteRoot()) != null) {
            getSiteSelector().setValue(cms.getRequestContext().getSiteRoot());
        }
        getSiteSelector().setNullSelectionAllowed(false);
        getSiteSelector().setItemCaptionPropertyId("caption");

        getProjectSelector().setContainerDataSource(
            CmsVaadinUtils.getProjectsContainer(A_CmsUI.getCmsObject(), "caption"));
        getProjectSelector().setItemCaptionPropertyId("caption");
        getProjectSelector().select(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid());
        getProjectSelector().setNewItemsAllowed(false);
        getProjectSelector().setNullSelectionAllowed(false);
        getProjectSelector().setTextInputAllowed(false);

        if (getCancelButton() != null) {
            getCancelButton().addClickListener(new ClickListener() {

                private static final long serialVersionUID = -3475214711731413636L;

                public void buttonClick(ClickEvent event) {

                    m_app.goToMainView();
                }
            });
        }
        getOkButton().addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5651452508587710734L;

            public void buttonClick(ClickEvent event) {

                m_app.openReport(getReportPath(), getThread(), getTitle());
            }
        });

    }

    /**
     * Gets a button for a cancel function.<p>
     *
     * @return a vaadin button
     */
    protected abstract Button getCancelButton();

    /**
     * Returns a cms object set to the site corresponding to siteselector.<p>
     *
     * @return a cms object
     */
    protected CmsObject getCmsObject() {

        try {
            CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            cms.getRequestContext().setSiteRoot((String)getSiteSelector().getValue());
            cms.getRequestContext().setCurrentProject(cms.readProject((CmsUUID)getProjectSelector().getValue()));
            return cms;
        } catch (CmsException e) {
            LOG.error("Unable to get CmsObject", e);
        }
        return null;

    }

    /**
     * Gets a button for a ok function.<p>
     *
     * @return a vaadin button
     */
    protected abstract Button getOkButton();

    /**
     * Gets a combobox used for the site selector.<p>
     *
     * @return a vaadin combobox
     */
    protected abstract ComboBox getProjectSelector();

    /**
     * Get the path (state) for the app to show the report for the import thread.<p>
     *
     * @return path to be called for showing report of thread
     */
    protected abstract String getReportPath();

    /**
     * Gets a combobox used for the site selector.<p>
     *
     * @return a vaadin combobox
     */
    protected abstract ComboBox getSiteSelector();

    /**
     * Gets the thread which gets started by clicking the ok button.<p>
     *
     * @return a thread
     */
    protected abstract A_CmsReportThread getThread();

    /**
     * Gets the title of the report to show.<p>
     *
     * @return title name
     */
    protected abstract String getTitle();

    /**
     * Processes a filename.<p>
     *
     * @param name to be processed
     * @return a valid file name
     */
    protected String processFileName(String name) {

        int pos = name.lastIndexOf("/");
        if (pos >= 0) {
            name = name.substring(pos + 1);
        }
        pos = name.lastIndexOf("\\");
        if (pos >= 0) {
            name = name.substring(pos + 1);
        }
        return name;
    }

    /**
     * Validates a file for module import.<p>
     */
    protected void validateModuleFile() {

        try {
            m_importFile.loadAndValidate();
            String importSite = m_importFile.getModule().getImportSite();
            if (importSite != null) {
                getSiteSelector().setEnabled(false);
                getSiteSelector().setValue(importSite);
            }
            getOkButton().setEnabled(true);
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            m_importFile = null;
            getOkButton().setEnabled(false);
            CmsErrorDialog.showErrorDialog(e);

        }
    }
}
