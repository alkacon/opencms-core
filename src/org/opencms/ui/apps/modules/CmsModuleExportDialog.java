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

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The module export dialog.<p>
 */
public class CmsModuleExportDialog extends CmsBasicDialog {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleExportDialog.class);

    /** Vaadin serial id.*/
    private static final long serialVersionUID = 1L;

    private CmsModuleImportExportHandler m_exportHandler;

    /**
     * public constructor.<p>
     *
     * @param thread to be run
     * @param window holds the dialog
     */
    public CmsModuleExportDialog(
        CmsModuleImportExportHandler exportHandler,
        A_CmsReportThread thread,
        final Window window) {

        m_exportHandler = exportHandler;
        Button close = createButtonClose();
        Button download = new Button(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_BUTTON_DOWNLOAD_0));
        close.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }

        });

        download.setEnabled(false);
        addButton(download, true);
        addButton(close, true);

        final CmsReportWidget report = new CmsReportWidget(thread);
        report.addReportFinishedHandler(() -> {
            if (thread.getErrors().size() == 0) {
                FileDownloader downloadContext = new FileDownloader(getDownloadResource());
                downloadContext.extend(download);
                download.setEnabled(true);
            }
        });
        setHeight("100%");
        report.setWidth("100%");
        report.setHeight("100%");
        VerticalLayout container = new VerticalLayout();
        container.setHeight("100%");
        container.addComponent(report);
        setContent(container);
        thread.start();
    }

    /**
     * Gets the download resource.
     *
     * @return the download resource
     */
    private Resource getDownloadResource() {

        return new StreamResource(() -> {
            try {
                InputStream stream = new FileInputStream(m_exportHandler.getFileName() + ".zip");
                return stream;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
        }, CmsResource.getName(m_exportHandler.getFileName() + ".zip"));

    }
}
