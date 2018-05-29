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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Upload;
import com.vaadin.v7.ui.Upload.ChangeEvent;
import com.vaadin.v7.ui.Upload.ChangeListener;
import com.vaadin.v7.ui.Upload.StartedEvent;
import com.vaadin.v7.ui.Upload.StartedListener;
import com.vaadin.v7.ui.Upload.SucceededEvent;
import com.vaadin.v7.ui.Upload.SucceededListener;

/**
 *Abstract class for HTTP imports.<p>
 */
public abstract class A_CmsHTTPImportForm extends A_CmsImportForm {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 8268966029442189695L;

    /**
     * public constructor.<p>
     *
     * @param app calling app instance
     * @param pathToServer path to server to save uploaded file
     * @param validate indicates if import should be validated (only possible for modules)
     */
    @SuppressWarnings("deprecation")
    public A_CmsHTTPImportForm(I_CmsReportApp app, final String pathToServer, final boolean validate) {

        super(app);

        getUpload().setImmediate(true);
        getUpload().addStartedListener(new StartedListener() {

            private static final long serialVersionUID = -1167851886739855757L;

            public void uploadStarted(StartedEvent event) {

                getOkButton().setEnabled(false);
                getSiteSelector().setEnabled(true);

                String name = event.getFilename();
                name = processFileName(name);
                getUploadLabel().setValue(name);
            }
        });

        getUpload().addChangeListener(new ChangeListener() {

            private static final long serialVersionUID = -8531203923548531981L;

            public void filenameChanged(ChangeEvent event) {

                getOkButton().setEnabled(false);
                getSiteSelector().setEnabled(true);

                String name = processFileName(event.getFilename());
                getUploadLabel().setValue(name);
            }
        });

        getUpload().setReceiver(new Upload.Receiver() {

            private static final long serialVersionUID = 5860617055589937645L;

            public OutputStream receiveUpload(String filename, String mimeType) {

                String path = CmsStringUtil.joinPaths(
                    OpenCms.getSystemInfo().getWebInfRfsPath(),
                    pathToServer,
                    processFileName(filename));
                // make sure parent folders exist
                File rfsFile = new File(path);
                rfsFile.getParentFile().mkdirs();
                m_importFile = new CmsImportFile(path);
                try {
                    return new FileOutputStream(m_importFile.getPath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e); // shouldn't happen, but if it does, there is no point in continuing
                }
            }
        });
        getUpload().addSucceededListener(new SucceededListener() {

            private static final long serialVersionUID = 3430913281578577509L;

            public void uploadSucceeded(SucceededEvent event) {

                if (validate) {
                    validateModuleFile();
                } else {
                    getOkButton().setEnabled(true);
                }
            }
        });
    }

    /**
     *Gets the upload button.<p>
     *
     * @return a vaadin upload button
     */
    @SuppressWarnings("deprecation")
    protected abstract Upload getUpload();

    /**
     * Gets the upload label, which shows the name of the uploaded file.<p>
     *
     * @return a vaadin label
     */
    @SuppressWarnings("deprecation")
    protected abstract Label getUploadLabel();
}
