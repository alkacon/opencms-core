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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.Button;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Html5File;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Upload;
import com.vaadin.v7.ui.Upload.ChangeEvent;
import com.vaadin.v7.ui.Upload.ChangeListener;
import com.vaadin.v7.ui.Upload.StartedEvent;
import com.vaadin.v7.ui.Upload.StartedListener;
import com.vaadin.v7.ui.Upload.SucceededEvent;
import com.vaadin.v7.ui.Upload.SucceededListener;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The form for importing modules via HTTP.<p>
 */
public class CmsModuleImportForm extends A_CmsModuleImportForm {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleImportForm.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Drag and drop wrapper. */
    private DragAndDropWrapper m_dnd;

    /** The cancel button. */
    protected Button m_cancel;

    /** The OK button. */
    protected Button m_ok;

    /** The site  selector. */
    protected CmsAutoItemCreatingComboBox m_siteSelect;

    /** The upload widget. */
    protected Upload m_upload;

    /** The label for the upload widget. */
    protected Label m_uploadLabel;

    /**
     * Creates a new instance.<p>
     *
     * @param app the module manager app instance for which this was opened
     */
    @SuppressWarnings("deprecation")
    public CmsModuleImportForm(CmsModuleApp app, VerticalLayout start, VerticalLayout report, Runnable run) {

        super(app, start, report, run);
        m_upload.setImmediate(true);
        m_dnd.setDropHandler(new DropHandler() {

            private static final long serialVersionUID = 1L;

            public void drop(DragAndDropEvent event) {

                WrapperTransferable transfer = (WrapperTransferable)event.getTransferable();
                Html5File[] files = transfer.getFiles();
                if (files.length == 1) {
                    final Html5File file = files[0];
                    m_uploadLabel.setValue(file.getFileName());
                    file.setStreamVariable(new StreamVariable() {

                        private static final long serialVersionUID = 1L;

                        public OutputStream getOutputStream() {

                            String path = CmsStringUtil.joinPaths(
                                OpenCms.getSystemInfo().getWebInfRfsPath(),
                                "packages/modules",
                                processFileName(file.getFileName()));
                            m_importFile = new CmsModuleImportFile(path);
                            try {
                                return new FileOutputStream(m_importFile.getPath());
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e); // shouldn't happen, but if it does, there is no point in continuing
                            }
                        }

                        public boolean isInterrupted() {

                            return false;
                        }

                        public boolean listenProgress() {

                            return false;
                        }

                        public void onProgress(StreamingProgressEvent evt) {
                            // do nothing
                        }

                        @SuppressWarnings("synthetic-access")
                        public void streamingFailed(StreamingErrorEvent evt) {

                            LOG.info("Upload streaming failed: " + evt.getFileName(), evt.getException());
                        }

                        public void streamingFinished(StreamingEndEvent evt) {

                            validateModuleFile();
                        }

                        public void streamingStarted(StreamingStartEvent evt) {

                            m_ok.setEnabled(false);
                            m_siteSelect.setEnabled(true);
                        }
                    });
                }
            }

            public AcceptCriterion getAcceptCriterion() {

                return AcceptAll.get();
            }
        });
        m_upload.addStartedListener(new StartedListener() {

            private static final long serialVersionUID = 1L;

            public void uploadStarted(StartedEvent event) {

                m_ok.setEnabled(false);
                m_siteSelect.setEnabled(true);

                String name = event.getFilename();
                name = processFileName(name);
                m_uploadLabel.setValue(name);

            }

        });

        m_upload.addChangeListener(new ChangeListener() {

            private static final long serialVersionUID = 1L;

            public void filenameChanged(ChangeEvent event) {

                m_ok.setEnabled(false);
                m_siteSelect.setEnabled(true);

                String name = processFileName(event.getFilename());
                m_uploadLabel.setValue(name);
            }
        });

        m_upload.setReceiver(new Upload.Receiver() {

            private static final long serialVersionUID = 1L;

            public OutputStream receiveUpload(String filename, String mimeType) {

                String path = CmsStringUtil.joinPaths(
                    OpenCms.getSystemInfo().getWebInfRfsPath(),
                    "packages/modules",
                    processFileName(filename));
                // make sure parent folders exist
                File rfsFile = new File(path);
                rfsFile.getParentFile().mkdirs();

                m_importFile = new CmsModuleImportFile(path);
                try {
                    return new FileOutputStream(m_importFile.getPath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e); // shouldn't happen, but if it does, there is no point in continuing
                }
            }
        });
        m_upload.addSucceededListener(new SucceededListener() {

            private static final long serialVersionUID = 1L;

            public void uploadSucceeded(SucceededEvent event) {

                validateModuleFile();
            }
        });
    }

    /**
     * @see org.opencms.ui.apps.modules.A_CmsModuleImportForm#getCancelButton()
     */
    @Override
    protected Button getCancelButton() {

        return m_cancel;
    }

    /**
     * @see org.opencms.ui.apps.modules.A_CmsModuleImportForm#getOkButton()
     */
    @Override
    protected Button getOkButton() {

        return m_ok;
    }

    /**
     * @see org.opencms.ui.apps.modules.A_CmsModuleImportForm#getSiteSelector()
     */
    @Override
    protected CmsAutoItemCreatingComboBox getSiteSelector() {

        return m_siteSelect;
    }

}
