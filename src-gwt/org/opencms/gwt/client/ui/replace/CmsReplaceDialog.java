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

package org.opencms.gwt.client.ui.replace;

import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsVirusReport;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.CmsUploadProgressInfo;
import org.opencms.gwt.client.ui.input.upload.CmsUploader;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsReplaceInfo;
import org.opencms.gwt.shared.CmsUploadProgessInfo;
import org.opencms.gwt.shared.I_CmsUploadConstants;
import org.opencms.gwt.shared.rpc.I_CmsUploadService;
import org.opencms.gwt.shared.rpc.I_CmsUploadServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The replace resource dialog.<p>
 */
public class CmsReplaceDialog extends CmsPopup implements I_CmsUploadDialog {

    /** The main content panel. */
    protected CmsReplaceContentWidget m_mainPanel;

    /** Signals that the upload dialog was canceled. */
    private boolean m_canceled;

    /** Flag indicating the client is waiting for a server response. */
    private boolean m_clientLoading;

    /** The close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** The sum of all file sizes. */
    private long m_contentLength;

    /** The current file input. */
    private CmsFileInput m_fileInput;

    /** The file widget. */
    private CmsListItemWidget m_fileWidget;

    /** The action to execute when the upload dialog is finished. */
    private Runnable m_finishAction;

    /** The dialog handler. */
    private CmsReplaceHandler m_handler;

    /** The close handler registration. */
    private HandlerRegistration m_handlerReg;

    /** The loading timer. */
    private Timer m_loadingTimer;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /** The upload progress widget. */
    private CmsUploadProgressInfo m_progressInfo;

    /** The replace resource information. */
    private CmsReplaceInfo m_replaceInfo;

    /** The progress timer. */
    private Timer m_updateProgressTimer;

    /** The upload button. */
    private CmsUploadButton m_uploadButton;

    /** The upload service. */
    private I_CmsUploadServiceAsync m_uploadService;

    /**
     * Constructor.<p>
     *
     * @param handler the dialog handler
     */
    public CmsReplaceDialog(CmsReplaceHandler handler) {

        super(Messages.get().key(Messages.GUI_REPLACE_TITLE_0));
        m_handler = handler;
        setModal(true);
        setGlassEnabled(true);
        catchNotifications();
        // create the main panel
        m_mainPanel = new CmsReplaceContentWidget();
        // set the main panel as content of the popup
        setMainContent(m_mainPanel);
        addDialogClose(new Command() {

            public void execute() {

                cancelReplace();
            }
        });

        CmsPushButton cancelButton = createButton(
            org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                cancelReplace();
            }
        });
        addButton(cancelButton);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#addCloseHandler(com.google.gwt.event.logical.shared.CloseHandler)
     */
    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {

        m_closeHandler = handler;
        m_handlerReg = super.addCloseHandler(handler);
        return m_handlerReg;
    }

    /**
     * Returns the action which should be executed when the upload dialog is finished.<p>
     *
     * @return an action to run when the upload dialog is finished
     */
    public Runnable getFinishAction() {

        return m_finishAction;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#hide()
     */
    @Override
    public void hide() {

        if (m_fileInput != null) {
            m_fileInput.removeFromParent();
        }
        super.hide();
    }

    /**
     * Parses the upload response of the server and decides what to do.<p>
     *
     * @param results a JSON Object
     */
    public void parseResponse(String results) {

        cancelUpdateProgress();
        stopLoadingAnimation();

        if ((!m_canceled) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(results)) {
            JSONObject jsonObject = JSONParser.parseStrict(results).isObject();
            boolean success = jsonObject.get(I_CmsUploadConstants.KEY_SUCCESS).isBoolean().booleanValue();
            // If the upload is done so fast that we did not receive any progress information, then
            // the content length is unknown. For that reason take the request size to show how
            // much bytes were uploaded.
            double size = jsonObject.get(I_CmsUploadConstants.KEY_REQUEST_SIZE).isNumber().doubleValue();
            long requestSize = Double.valueOf(size).longValue();
            if (m_contentLength == 0) {
                m_contentLength = requestSize;
            }
            if (success) {
                m_progressInfo.finish();
                m_mainPanel.displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                Map<String, List<String>> viruses = CmsVirusReport.getVirusWarnings(jsonObject);
                if (viruses.isEmpty()) {
                    closeOnSuccess();
                } else {
                    if (m_handlerReg != null) {
                        m_handlerReg.removeHandler();
                    }
                    CmsPopup popup = CmsVirusReport.createPopup(viruses, () -> {
                        // do nothing
                    });
                    if (m_closeHandler != null) {
                        popup.addCloseHandler(m_closeHandler);
                    }
                    hide();
                    popup.center();
                }
            } else {
                String message = jsonObject.get(I_CmsUploadConstants.KEY_MESSAGE).isString().stringValue();
                String stacktrace = jsonObject.get(I_CmsUploadConstants.KEY_STACKTRACE).isString().stringValue();
                showErrorReport(message, stacktrace);
            }
        }
    }

    /**
     * Sets an action that should be executed if the upload dialog is finished.<p>
     *
     * @param action the action to execute when finished
     */
    public void setFinishAction(Runnable action) {

        m_finishAction = action;
    }

    /**
     * Shows the error report.<p>
     *
     * @param message the message to show
     * @param stacktrace the stacktrace to show
     */
    public void showErrorReport(final String message, final String stacktrace) {

        if (!m_canceled) {
            CmsErrorDialog errDialog = new CmsErrorDialog(message, stacktrace);
            if (m_handlerReg != null) {
                m_handlerReg.removeHandler();
            }
            if (m_closeHandler != null) {
                errDialog.addCloseHandler(m_closeHandler);
            }
            hide();
            errDialog.center();
        }
    }

    /**
     * Cancels the replace process.<p>
     */
    protected void cancelReplace() {

        m_canceled = true;
        if (m_progressInfo != null) {
            cancelUpdateProgress();
            CmsRpcAction<Boolean> callback = new CmsRpcAction<Boolean>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getUploadService().cancelUpload(this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Boolean result) {

                    hide();
                }
            };
            callback.execute();
        } else {
            hide();
        }
    }

    /**
     * Cancels the upload progress timer.<p>
     */
    protected void cancelUpdateProgress() {

        if (m_updateProgressTimer != null) {
            m_updateProgressTimer.cancel();
        }
    }

    /**
     * Returns the resource type name for a given filename.<p>
     *
     * @param file the file info
     *
     * @return the resource type name
     */
    protected String getResourceType(CmsFileInfo file) {

        return CmsCoreProvider.get().getResourceType(file);
    }

    /**
     * Returns the upload service instance.<p>
     *
     * @return the upload service instance
     */
    protected I_CmsUploadServiceAsync getUploadService() {

        if (m_uploadService == null) {
            m_uploadService = GWT.create(I_CmsUploadService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.upload.CmsUploadService.gwt");
            ((ServiceDefTarget)m_uploadService).setServiceEntryPoint(serviceUrl);
        }
        return m_uploadService;
    }

    /**
     * Returns the upload JSP uri.<p>
     *
     * @return the upload JSP uri
     */
    protected String getUploadUri() {

        return CmsCoreProvider.get().link(I_CmsUploadConstants.UPLOAD_ACTION_JSP_URI);
    }

    /**
     * Initializes the dialog content with the replace information.<p>
     *
     * @param replaceInfo the replace information
     */
    protected void initContent(CmsReplaceInfo replaceInfo) {

        m_replaceInfo = replaceInfo;
        CmsListItemWidget fileInfoItem = new CmsListItemWidget(m_replaceInfo.getFileInfo());
        m_mainPanel.setReplaceInfo(fileInfoItem);
        if (!m_replaceInfo.isLockable()) {
            m_mainPanel.displayDialogInfo(Messages.get().key(Messages.GUI_REPLACE_LOCKED_RESOURCE_0), true);
        } else {
            m_mainPanel.displayDialogInfo(Messages.get().key(Messages.GUI_REPLACE_INFO_0), false);
            checkFileType();
            createButtons();
        }
    }

    /**
     * Initializes the dialog content.<p>
     *
     * @param structureId the structure id of the file to replace
     */
    protected void initContent(final CmsUUID structureId) {

        CmsRpcAction<CmsReplaceInfo> action = new CmsRpcAction<CmsReplaceInfo>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getFileReplaceInfo(structureId, this);
            }

            @Override
            protected void onResponse(CmsReplaceInfo result) {

                initContent(result);
                stop(false);
            }
        };
        action.execute();
    }

    /**
     * Sets the file input.<p>
     *
     * @param fileInput the file input
     */
    protected void setFileInput(CmsFileInput fileInput) {

        // only accept file inputs with a single selected file
        if (fileInput.getFiles().length == 1) {
            if (m_okButton != null) {
                m_okButton.enable();
            }
            if (m_fileInput != null) {
                m_fileInput.removeFromParent();
            }
            m_fileInput = fileInput;
            RootPanel.get().add(m_fileInput);
            m_mainPanel.setContainerWidget(createFileWidget(m_fileInput.getFiles()[0]));
        }
    }

    /**
     * Retrieves the progress information from the server.<p>
     */
    protected void updateProgress() {

        CmsRpcAction<CmsUploadProgessInfo> callback = new CmsRpcAction<CmsUploadProgessInfo>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getUploadService().getUploadProgressInfo(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onFailure(java.lang.Throwable)
             */
            @Override
            public void onFailure(Throwable t) {

                super.onFailure(t);
                cancelUpdateProgress();
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUploadProgessInfo result) {

                updateProgressBar(result);
            }
        };
        callback.execute();
    }

    /**
     * Updates the progress bar.<p>
     *
     * @param info the progress info
     */
    protected void updateProgressBar(CmsUploadProgessInfo info) {

        switch (info.getState()) {
            case notStarted:
                break;
            case running:
                m_progressInfo.setProgress(info);
                stopLoadingAnimation();
                break;
            case finished:
                m_progressInfo.finish();
                m_mainPanel.displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                startLoadingAnimation(Messages.get().key(Messages.GUI_UPLOAD_INFO_CREATING_RESOURCES_0), 1500);
                break;
            default:
                break;
        }
    }

    /**
     * Uploads the selected file.<p>
     */
    protected void uploadFile() {

        hideOkAndUploadButtons();
        CmsUploader uploader = new CmsUploader();
        CmsFileInfo info = m_fileInput.getFiles()[0];
        info.setOverrideFileName(CmsResource.getName(m_replaceInfo.getSitepath()));
        uploader.uploadFiles(
            CmsCoreProvider.get().link(I_CmsUploadConstants.UPLOAD_ACTION_JSP_URI),
            CmsResource.getFolderPath(m_replaceInfo.getSitepath()),
            false,
            null,
            Collections.singletonList(info),
            Collections.<String> emptyList(),
            true,
            this);
        showProgress();
    }

    /**
     * Checks if the file suffix of the selected file matches the suffix of the resource to replace.<p>
     */
    private void checkFileType() {

        if ((m_fileInput != null) && (m_replaceInfo != null) && (m_fileWidget != null)) {
            CmsFileInfo file = m_fileInput.getFiles()[0];
            if (!m_replaceInfo.getSitepath().endsWith(file.getFileSuffix())) {
                Widget warningImage = FontOpenCms.WARNING.getWidget(
                    20,
                    I_CmsConstantsBundle.INSTANCE.css().colorWarning());
                warningImage.setTitle(Messages.get().key(Messages.GUI_REPLACE_WRONG_FILE_EXTENSION_0));
                warningImage.addStyleName(
                    org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
                m_fileWidget.addButton(warningImage);
            }
        }
    }

    /**
     * Closes the dialog after a delay.<p>
     */
    private void closeOnSuccess() {

        Timer closeTimer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                CmsReplaceDialog.this.hide();
            }
        };
        closeTimer.schedule(1000);
    }

    /**
     * Creates a dialog text button.<p>
     *
     * @param buttonText the button text
     *
     * @return the button
     */
    private CmsPushButton createButton(String buttonText) {

        CmsPushButton button = new CmsPushButton();
        button.setTitle(buttonText);
        button.setText(buttonText);
        button.setSize(I_CmsButton.Size.medium);
        button.setUseMinWidth(true);
        return button;
    }

    /**
     * Creates the "OK", the "Cancel" and the "Change file" button.<p>
     */
    private void createButtons() {

        m_okButton = createButton(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                uploadFile();
            }
        });
        if (m_fileInput == null) {
            m_okButton.disable(Messages.get().key(Messages.GUI_REPLACE_NO_FILE_SELECTED_0));
        }
        addButton(m_okButton);

        // add a new upload button
        m_uploadButton = new CmsUploadButton(m_handler);
        m_uploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().uploadDialogButton());
        m_uploadButton.setText(Messages.get().key(Messages.GUI_REPLACE_CHANGE_FILE_0));
        addButton(m_uploadButton);
        m_handler.setButton(m_uploadButton);
    }

    /**
     * Creates the widget to display the selected file information.<p>
     *
     * @param file the file info
     *
     * @return the widget
     */
    private CmsListItemWidget createFileWidget(CmsFileInfo file) {

        String subTitle;
        String resourceType = getResourceType(file);
        if (file.getFileSize() > 0) {
            subTitle = CmsUploadButton.formatBytes(file.getFileSize()) + " (" + getResourceType(file) + ")";
        } else {
            subTitle = resourceType;
        }
        CmsListInfoBean infoBean = new CmsListInfoBean(file.getFileName(), subTitle, null);
        m_fileWidget = new CmsListItemWidget(infoBean);
        m_fileWidget.setIcon(CmsCoreProvider.get().getResourceTypeIcon(file));
        checkFileType();
        return m_fileWidget;
    }

    /**
     * Hides the OK and upload button while processing the upload.<p>
     */
    private void hideOkAndUploadButtons() {

        m_uploadButton.setVisible(false);
        m_okButton.setVisible(false);
    }

    /**
     * Starts the upload progress bar.<p>
     */
    private void showProgress() {

        CmsFileInfo fileInfo = m_fileInput.getFiles()[0];
        m_progressInfo = new CmsUploadProgressInfo(Collections.singletonList(fileInfo.getFileName()));
        m_progressInfo.setContentLength(fileInfo.getFileSize());
        m_mainPanel.setContainerWidget(m_progressInfo);
        m_updateProgressTimer = new Timer() {

            @Override
            public void run() {

                updateProgress();
            }
        };
        m_updateProgressTimer.scheduleRepeating(1000);

    }

    /**
     * Starts the loading animation.<p>
     *
     * Used while client is loading files from hard disk into memory.<p>
     *
     * @param msg the message that should be displayed below the loading animation (can also be HTML as String)
     * @param delayMillis the delay to start the animation with
     */
    private void startLoadingAnimation(final String msg, int delayMillis) {

        m_loadingTimer = new Timer() {

            @Override
            public void run() {

                m_mainPanel.showLoadingAnimation(msg);
            }
        };
        if (delayMillis > 0) {
            m_loadingTimer.schedule(delayMillis);
        } else {
            m_loadingTimer.run();
        }
    }

    /**
     * Stops the client loading animation.<p>
     */
    private void stopLoadingAnimation() {

        if (m_loadingTimer != null) {
            m_loadingTimer.cancel();
        }
        if (m_clientLoading) {
            m_mainPanel.removeLoadingAnimation();
            m_clientLoading = false;
        }
    }
}
