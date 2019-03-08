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

package org.opencms.ui.apps.modules;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.modules.edit.CmsSiteSelectorNewValueHandler;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.report.CmsReportWidget;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Abstract superclass for the module import forms.<p>
 */
public abstract class A_CmsModuleImportForm extends CssLayout {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsModuleImportForm.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The module manager app instance. */
    protected CmsModuleApp m_app;

    /** A bean representing the module zip file to be imported. */
    protected CmsModuleImportFile m_importFile;

    /**
     * Constructor.<p>
     *
     * @param app the app instance for which this form is opened
     */
    public A_CmsModuleImportForm(
        CmsModuleApp app,
        final VerticalLayout start,
        final VerticalLayout report,
        Runnable run) {

        report.setVisible(false);
        CmsObject cms = A_CmsUI.getCmsObject();
        m_app = app;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        getOkButton().setEnabled(false);

        final IndexedContainer availableSites = CmsVaadinUtils.getAvailableSitesContainer(cms, "name");

        getSiteSelector().setContainerDataSource(availableSites);
        if (availableSites.getItem(cms.getRequestContext().getSiteRoot()) != null) {
            getSiteSelector().setValue(cms.getRequestContext().getSiteRoot());
        }
        getSiteSelector().setNullSelectionAllowed(false);
        getSiteSelector().setItemCaptionPropertyId("name");
        getSiteSelector().setNewValueHandler(new CmsSiteSelectorNewValueHandler("name"));

        getCancelButton().addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsVaadinUtils.getWindow(getCancelButton()).close();

            }
        });

        getOkButton().addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                try {
                    start.setVisible(false);
                    report.setVisible(true);
                    getOkButton().setVisible(false);
                    CmsObject importCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    importCms.getRequestContext().setSiteRoot((String)(getSiteSelector().getValue()));
                    CmsModuleImportThread thread = new CmsModuleImportThread(
                        importCms,
                        m_importFile.getModule(),
                        m_importFile.getPath());

                    CmsReportWidget reportWidget = new CmsReportWidget(thread);
                    reportWidget.setWidth("100%");
                    reportWidget.setHeight("100%");

                    report.addComponent(reportWidget);
                    thread.start();
                    getOkButton().setEnabled(false);
                    getCancelButton().setCaption(CmsVaadinUtils.messageClose());
                    getCancelButton().addClickListener(new ClickListener() {

                        private static final long serialVersionUID = 1L;

                        public void buttonClick(ClickEvent event) {

                            run.run();

                        }
                    });

                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    CmsErrorDialog.showErrorDialog(e);
                }
            }

        });

    }

    /**
     * Gets the list of buttons for the form.<p>
     *
     * @return the buttons
     */
    public List<Button> getButtons() {

        return Arrays.asList(getOkButton(), getCancelButton());
    }

    /**
     * Gets the cancel button.<p>
     *
     * @return the cancel button
     */
    protected abstract Button getCancelButton();

    /**
     * Gets the OK button.<p>
     *
     * @return the OK button
     */
    protected abstract Button getOkButton();

    /**
     * Returns the site selector for the module import.<p>
     *
     * @return the site selector
     **/
    protected abstract CmsAutoItemCreatingComboBox getSiteSelector();

    /**
     * Takes the file name given in the upload and processes it to return the file name under which the upload should be stored in the file system.<p>
     *
     * @param name the upload file name
     * @return the RFS file name
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
     * Validates the module file to be imported.<p>
     */
    protected void validateModuleFile() {

        try {
            m_importFile.loadAndValidate();
            CmsModule importModule = m_importFile.getModule();
            String site = importModule.getSite();
            if (site != null) {
                if (importModule.hasImportSite()) {
                    getSiteSelector().setEnabled(false);
                    getSiteSelector().setValue(site);
                } else {
                    String itemId = CmsVaadinUtils.getPathItemId(getSiteSelector().getContainerDataSource(), site);
                    if (itemId != null) {
                        getSiteSelector().setValue(itemId);
                    }
                }
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
