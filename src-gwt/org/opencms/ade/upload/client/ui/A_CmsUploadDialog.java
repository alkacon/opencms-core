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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.Messages;
import org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle.I_CmsUploadCss;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsLoadingAnimation;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsVirusReport;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.CmsUploadProgressInfo;
import org.opencms.gwt.client.ui.input.upload.CmsUploader;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog;
import org.opencms.gwt.client.util.CmsChangeHeightAnimation;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsUploadFileBean;
import org.opencms.gwt.shared.CmsUploadProgessInfo;
import org.opencms.gwt.shared.CmsUploadRestrictionInfo;
import org.opencms.gwt.shared.I_CmsUploadConstants;
import org.opencms.gwt.shared.rpc.I_CmsUploadService;
import org.opencms.gwt.shared.rpc.I_CmsUploadServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides an upload dialog.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsUploadDialog extends CmsPopup implements I_CmsUploadDialog {

    /** The minimal height of the content wrapper. */
    private static final int MIN_CONTENT_HEIGHT = 110;

    /** Text metrics key. */
    private static final String TM_FILE_UPLOAD_LIST = "FileUploadList";

    /** The interval for updating the progress information in milliseconds. */
    private static final int UPDATE_PROGRESS_INTERVALL = 1000;

    /** The upload context. */
    protected I_CmsUploadContext m_context;

    /** The drag and drop message. */
    protected HTML m_dragAndDropMessage;

    /** The scroll panel. */
    protected CmsScrollPanel m_scrollPanel;

    /** The uploaded file names. */
    protected List<String> m_uploadedFiles;

    /** Signals that the upload dialog was canceled. */
    boolean m_canceled;

    /** True if the target folder is given as a root path. */
    boolean m_isTargetRootPath;

    /** Stores all files that were added. */
    private Map<String, CmsFileInfo> m_allFiles;

    /** Signals that the client currently loading. */
    private boolean m_clientLoading;

    /** The close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** The sum of all file sizes. */
    private long m_contentLength;

    /** A flow panel with a dynamic height. */
    private FlowPanel m_contentWrapper;

    /** The user information text widget. */
    private HTML m_dialogInfo;

    /** The list of file item widgets. */
    private CmsList<I_CmsListItem> m_fileList;

    /** The list of filenames that should be unziped on the server. */
    private List<String> m_filesToUnzip;

    /** The Map of files to upload. */
    private Map<String, CmsFileInfo> m_filesToUpload;

    /** Stores the content height of the selection dialog. */
    private int m_firstContentHeight;

    /** Stores the height of the user information text widget of the selection dialog. */
    private int m_firstInfoHeight;

    /** Stores the height of the summary. */
    private int m_firstSummaryHeight;

    /** A local reference to the default gwt CSS. */
    private org.opencms.gwt.client.ui.css.I_CmsLayoutBundle m_gwtCss = org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE;

    /** The close handler registration. */
    private HandlerRegistration m_handlerReg;

    /** Stores the list items of all added files. */
    private Map<String, CmsListItem> m_listItems;

    /** A panel for showing client loading. */
    private FlowPanel m_loadingPanel;

    /** A timer to delay the loading animation. */
    private Timer m_loadingTimer;

    /** The main panel. */
    private FlowPanel m_mainPanel;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /** The post-create handler. */
    private String m_postCreateHandler;

    /** The progress bar for the upload process. */
    private CmsUploadProgressInfo m_progressInfo;

    /** The names of restricted upload files. */
    private Set<String> m_restricted = new HashSet<>();

    /** Signals whether the selection is done or not. */
    private boolean m_selectionDone;

    /** The user information text widget. */
    private HTML m_selectionSummary;

    /** The target folder to upload the selected files. */
    private String m_targetFolder;

    /** The timer for updating the progress. */
    private Timer m_updateProgressTimer = new Timer() {

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            updateProgress();
        }
    };

    /** The upload button of this dialog. */
    private I_CmsUploadButton m_uploadButton;

    /** The upload service instance. */
    private I_CmsUploadServiceAsync m_uploadService;

    /**
     * Default constructor.<p>
     */
    public A_CmsUploadDialog() {

        super(Messages.get().key(Messages.GUI_UPLOAD_DIALOG_TITLE_1));

        I_CmsLayoutBundle.INSTANCE.uploadCss().ensureInjected();
        setModal(true);
        setGlassEnabled(true);
        catchNotifications();
        setWidth(CmsPopup.DEFAULT_WIDTH);

        // create a map that stores all files (upload, existing, invalid)
        m_allFiles = new HashMap<String, CmsFileInfo>();
        // create a map the holds all the list items for the selection dialog
        m_listItems = new HashMap<String, CmsListItem>();
        m_filesToUnzip = new ArrayList<String>();
        m_fileList = new CmsList<I_CmsListItem>();
        m_fileList.truncate(TM_FILE_UPLOAD_LIST, CmsPopup.DEFAULT_WIDTH - 50);

        // initialize a map that stores all the files that should be uploaded
        m_filesToUpload = new HashMap<String, CmsFileInfo>();

        // create the main panel
        m_mainPanel = new FlowPanel();

        // add the user info to the main panel
        m_dialogInfo = new HTML();
        m_dialogInfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogInfo());
        m_mainPanel.add(m_dialogInfo);
        m_scrollPanel = GWT.create(CmsScrollPanel.class);
        m_scrollPanel.getElement().getStyle().setPropertyPx("minHeight", MIN_CONTENT_HEIGHT);
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().mainContentWidget());
        m_scrollPanel.addStyleName(m_gwtCss.generalCss().cornerAll());
        m_mainPanel.add(m_scrollPanel);
        // add the content wrapper
        m_contentWrapper = new FlowPanel();
        m_contentWrapper.add(m_fileList);
        m_scrollPanel.add(m_contentWrapper);

        m_selectionSummary = new HTML();
        m_selectionSummary.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().summary());
        m_mainPanel.add(m_selectionSummary);

        // set the main panel as content of the popup
        setMainContent(m_mainPanel);

        addCloseHandler(new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> e) {

                if (m_context != null) {
                    m_context.onUploadFinished(m_uploadedFiles);
                }
            }
        });

        // create and add the "OK", "Cancel" and upload button
        createButtons();
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
     * Creates a bean that can be used for the list item widget.<p>
     *
     * @param file the info to create the bean for
     *
     * @return a list info bean
     */
    public abstract CmsListInfoBean createInfoBean(CmsFileInfo file);

    /**
     * Returns the massage for too large files.<p>
     *
     * @param file the file
     *
     * @return the message
     */
    public abstract String getFileSizeTooLargeMessage(CmsFileInfo file);

    /**
     * Returns <code>true</code> if the file is too large, <code>false</code> otherwise.<p>
     *
     * @param cmsFileInfo the file to check
     *
     * @return <code>true</code> if the file is too large, <code>false</code> otherwise
     */
    public abstract boolean isTooLarge(CmsFileInfo cmsFileInfo);

    /**
     * Loads and shows this dialog.<p>
     */
    public void loadAndShow() {

        // enable or disable the OK button
        if (getFilesToUpload().isEmpty()) {
            disableOKButton(Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_NO_FILES_0));
            setDragAndDropMessage();
        } else {
            enableOKButton();
            removeDragAndDropMessage();
        }
        m_uploadedFiles = null;
        // set the user info
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_SELECTION_0), false);
        // set the selection summary
        updateSummary();

        // add a upload button
        m_uploadButton.createFileInput();

        // show the popup
        if (!isShowing()) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
                 */
                public void execute() {

                    setContentWrapperHeight();
                    center();
                }
            });
        }
        center();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog#parseResponse(java.lang.String)
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
                m_uploadedFiles = new ArrayList<String>();
                List<String> uploadedFileIds = new ArrayList<String>();
                displayDialogInfo(
                    org.opencms.gwt.client.Messages.get().key(
                        org.opencms.gwt.client.Messages.GUI_UPLOAD_INFO_FINISHING_0),
                    false);
                JSONValue uploadedFilesVal = jsonObject.get(I_CmsUploadConstants.KEY_UPLOADED_FILE_NAMES);
                JSONValue uploadHook = jsonObject.get(I_CmsUploadConstants.KEY_UPLOAD_HOOK);
                JSONValue uploadedFileIdsVal = jsonObject.get(I_CmsUploadConstants.KEY_UPLOADED_FILES);
                JSONArray uploadedFileIdsArray = uploadedFileIdsVal.isArray();

                String hookUri = null;
                if ((uploadHook != null) && (uploadHook.isString() != null)) {
                    hookUri = uploadHook.isString().stringValue();
                    if (uploadedFileIdsArray != null) {
                        for (int i = 0; i < uploadedFileIdsArray.size(); i++) {
                            JSONString entry = uploadedFileIdsArray.get(i).isString();
                            if (entry != null) {
                                uploadedFileIds.add(entry.stringValue());
                            }
                        }
                    }
                }
                if (uploadedFileIds.size() == 0) {
                    // no files uploaded, probably because of virus scanner - don't show upload hook dialog
                    hookUri = null;
                }
                JSONArray uploadedFilesArray = uploadedFilesVal.isArray();
                if (uploadedFilesArray != null) {
                    for (int i = 0; i < uploadedFilesArray.size(); i++) {
                        JSONString entry = uploadedFilesArray.get(i).isString();
                        if (entry != null) {
                            m_uploadedFiles.add(entry.stringValue());
                        }
                    }
                }
                Map<String, List<String>> viruses = CmsVirusReport.getVirusWarnings(jsonObject);
                final I_CmsUploadContext context = m_context;
                final String finalHookUri = hookUri;
                m_progressInfo.finish();
                if (!viruses.isEmpty()) {
                    m_context = null;
                    hide();
                    CmsPopup virusPopup = CmsVirusReport.createPopup(viruses, () -> {
                        if (finalHookUri != null) {
                            openHookDialog(uploadedFileIds, finalHookUri, context);
                        } else {
                            context.onUploadFinished(m_uploadedFiles);
                        }
                    });
                    virusPopup.center();
                } else {
                    if (hookUri != null) {
                        // Set the context to be null so that it isn't called when the upload dialog closed;
                        // we want it to be called when the upload property dialog is closed instead.<p>
                        m_context = null;
                        openHookDialog(uploadedFileIds, hookUri, context);
                    }
                    closeOnSuccess();
                }
            } else {
                String message = jsonObject.get(I_CmsUploadConstants.KEY_MESSAGE).isString().stringValue();
                String stacktrace = jsonObject.get(I_CmsUploadConstants.KEY_STACKTRACE).isString().stringValue();
                showErrorReport(message, stacktrace);
            }
        }
    }

    /**
     * Sets the upload context.<p>
     *
     * @param context the new upload context
     */
    public void setContext(I_CmsUploadContext context) {

        if (context != null) {
            m_context = new I_CmsUploadContext() {

                @Override
                public void onUploadFinished(List<String> uploadedFiles) {

                    context.onUploadFinished(uploadedFiles);
                }
            };

        } else {
            m_context = context;
        }
    }

    /**
     * Sets the boolean flag to control whether the target folder is interpreted as a root path.<p>
     *
     * @param isTargetRootPath true if the target folder should be treated as a root path
     */
    public void setIsTargetRootPath(boolean isTargetRootPath) {

        m_isTargetRootPath = isTargetRootPath;
    }

    /**
     * Sets the post-create handler.
     *
     * @param postCreateHandler the post-create handler
     */
    public void setPostCreateHandler(String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /**
     * Sets the target folder.<p>
     *
     * @param target the target folder to set
     */
    public void setTargetFolder(String target) {

        m_targetFolder = target;
        setCaption(Messages.get().key(Messages.GUI_UPLOAD_DIALOG_TITLE_1, m_targetFolder));
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
     * Executes the submit action.<p>
     */
    public void submit() {

        // create a JsArray containing the files to upload
        List<CmsFileInfo> filesToUpload = new ArrayList<CmsFileInfo>(getFilesToUpload().values());
        Collections.sort(filesToUpload, CmsFileInfo.INFO_COMPARATOR);

        CmsUploader uploader = new CmsUploader();
        uploader.uploadFiles(
            getUploadUri(),
            getTargetFolder(),
            m_isTargetRootPath,
            getPostCreateHandler(),
            filesToUpload,
            getFilesToUnzip(false),
            false,
            this);
    }

    /**
     * Updates the button handler.
     */
    public void updateHandler() {

        CmsDialogUploadButtonHandler handler = new CmsDialogUploadButtonHandler(
            () -> m_context,
            m_targetFolder,
            m_isTargetRootPath);
        handler.setUploadDialog(this);
        m_uploadButton.reinitButton(handler);
    }

    /**
     * Updates the file summary.<p>
     */
    public abstract void updateSummary();

    /**
     * Adds the given file input field to this dialog.<p>
     *
     * @param fileInput the file input field to add
     */
    protected void addFileInput(CmsFileInput fileInput) {

        // add the files selected by the user to the list of files to upload
        if (fileInput != null) {
            addFiles(Arrays.asList(fileInput.getFiles()));
        } else {
            loadAndShow();
        }
    }

    /**
     * Adds the given file input field to this dialog.<p>
     *
     * @param fileInfos the file info objects
     */
    protected void addFiles(List<CmsFileInfo> fileInfos) {

        if (fileInfos != null) {
            for (CmsFileInfo file : fileInfos) {

                // store all files
                m_allFiles.put(file.getFileName(), file);
                CmsUploadRestrictionInfo restriction = CmsCoreProvider.get().getUploadRestriction();

                String targetRootPath = getTargetRootPath();
                boolean restricted = !restriction.isUploadEnabled(targetRootPath)
                    || !restriction.checkTypeAllowed(targetRootPath, file.getFileSuffix());
                if (restricted) {
                    m_restricted.add(file.getFileName());
                }

                // add those files to the list of files to upload that potential candidates
                if (!isTooLarge(file) && !restricted) {
                    m_filesToUpload.put(file.getFileName(), file);
                }

                // remove those files from the list to upload that were previously unchecked by the user
                if ((m_listItems.get(file.getFileName()) != null)
                    && (m_listItems.get(file.getFileName()).getCheckBox() != null)
                    && !m_listItems.get(file.getFileName()).getCheckBox().isChecked()) {
                    m_filesToUpload.remove(file.getFileName());
                }
            }

            // now rebuild the list: handle all files
            m_fileList.clearList();
            List<String> sortedFileNames = new ArrayList<String>(m_allFiles.keySet());
            Collections.sort(sortedFileNames, String.CASE_INSENSITIVE_ORDER);
            for (String filename : sortedFileNames) {
                CmsFileInfo file = m_allFiles.get(filename);
                addFileToList(file, false, false, isTooLarge(file), m_restricted.contains(file.getFileName()));
            }
            doResize();
        }
        loadAndShow();
    }

    /**
     * Cancels the upload progress timer.<p>
     */
    protected void cancelUpdateProgress() {

        m_updateProgressTimer.cancel();
    }

    /**
     * Cancels the upload.<p>
     */
    protected void cancelUpload() {

        m_canceled = true;
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
    }

    /**
     * Creates the loading animation HTML and adds is to the content wrapper.<p>
     *
     * @param msg the message to display below the animation
     */
    protected void createLoadingAnimation(String msg) {

        m_clientLoading = true;
        if (m_loadingPanel != null) {
            m_loadingPanel.removeFromParent();
        }
        m_loadingPanel = new FlowPanel();
        m_loadingPanel.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().loadingPanel());
        m_loadingPanel.addStyleName(m_gwtCss.generalCss().cornerAll());

        CmsLoadingAnimation animationDiv = new CmsLoadingAnimation();
        animationDiv.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().loadingAnimation());
        m_loadingPanel.add(animationDiv);

        HTML messageDiv = new HTML();
        messageDiv.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().loadingText());
        messageDiv.setHTML(msg);
        m_loadingPanel.add(messageDiv);

        m_contentWrapper.add(m_loadingPanel);
        doResize();
    }

    /**
     * Disables the OK button.<p>
     *
     * @param disabledReason the reason for disabling the OK button
     */
    protected void disableOKButton(String disabledReason) {

        m_okButton.disable(disabledReason);
    }

    /**
     * Required to be called when the content has changed.<p>
     */
    protected void doResize() {

        m_scrollPanel.onResizeDescendant();
    }

    /**
     * Enables the OK button.<p>
     */
    protected void enableOKButton() {

        m_okButton.enable();
    }

    /**
     * Returns the contentLength.<p>
     *
     * @return the contentLength
     */
    protected long getContentLength() {

        return m_contentLength;
    }

    /**
     * Returns the contentWrapper.<p>
     *
     * @return the contentWrapper
     */
    protected FlowPanel getContentWrapper() {

        return m_contentWrapper;
    }

    /**
     * Returns the list of file names that have to unziped.<p>
     *
     * @param all <code>true</code> if the returned list should contain those filenames that
     * are not inside the map of files to upload. <code>false</code> only those filenames are
     * returned that are also inside the map of files to upload
     *
     * @return the list of file names that have to unziped
     */
    protected List<String> getFilesToUnzip(boolean all) {

        if (!all) {
            List<String> result = new ArrayList<String>();
            for (String fileName : m_filesToUnzip) {
                if (m_filesToUpload.keySet().contains(fileName)) {
                    result.add(fileName);
                }
            }
            return result;
        }
        return m_filesToUnzip;
    }

    /**
     * Returns the filesToUpload.<p>
     *
     * @return the filesToUpload
     */
    protected Map<String, CmsFileInfo> getFilesToUpload() {

        return m_filesToUpload;
    }

    /**
     * Returns "files" or "file" depending on the files to upload.<p>
     *
     * @return "files" or "file" depending on the files to upload
     */
    protected String getFileText() {

        if (m_filesToUpload.size() == 1) {
            return org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_UPLOAD_FILES_SINGULAR_0);
        }
        return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_UPLOAD_FILES_PLURAL_0);
    }

    /**
     * Gets the post-create handler.
     *
     * @return the post-create handler
     */
    protected String getPostCreateHandler() {

        return m_postCreateHandler;
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
     * Returns the targetFolder.<p>
     *
     * @return the targetFolder
     */
    protected String getTargetFolder() {

        return m_targetFolder;
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
     * Inserts a hidden form into.<p>
     *
     * @param form the form to insert
     */
    protected void insertUploadForm(FormPanel form) {

        form.getElement().getStyle().setDisplay(Display.NONE);
        m_contentWrapper.add(form);
    }

    /**
     * The action that is executed if the user clicks on the OK button.<p>
     *
     * If the selection dialog is currently shown the selected files are checked
     * otherwise the upload is triggered.<p>
     */
    protected void onOkClick() {

        if (!m_selectionDone) {
            checkSelection();
        } else {
            commit();
        }
    }

    /**
     * Required to be called when the content has changed.<p>
     */
    protected void onResize() {

        m_scrollPanel.onResize();
    }

    /**
     * Decides how to go on depending on the information of the server response.<p>
     *
     * Shows a warning if there is another upload process active (inside the same session).<p>
     *
     * Otherwise if the list of files to upload contains already existent resources on the VFS or if there
     * are files selected that have invalid file names the overwrite dialog is shown.<p>
     *
     * Only if there is no other upload process running and none of the selected files
     * is already existent on the VFS the upload is triggered.<p>
     *
     * @param result the bean that contains the information to evaluate
     */
    protected void proceedWorkflow(CmsUploadFileBean result) {

        if (result.isActive()) {
            m_okButton.enable();
            CmsNotification.get().send(Type.WARNING, Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_RUNNING_0));
        } else {
            if (!result.getExistingResourceNames().isEmpty()
                || !result.getInvalidFileNames().isEmpty()
                || !result.getExistingDeletedFileNames().isEmpty()) {
                showOverwriteDialog(result);
            } else {
                commit();
            }
        }
    }

    /**
     * Removes the drag and drop message.<p>
     */
    protected void removeDragAndDropMessage() {

        if (m_dragAndDropMessage != null) {
            m_dragAndDropMessage.removeFromParent();
            m_dragAndDropMessage = null;
            getContentWrapper().getElement().getStyle().clearBackgroundColor();
        }
    }

    /**
     * Sets the contentLength.<p>
     *
     * @param contentLength the contentLength to set
     */
    protected void setContentLength(long contentLength) {

        m_contentLength = contentLength;
    }

    /**
     * Execute to set the content wrapper height.<p>
     */
    protected void setContentWrapperHeight() {

        // set the max height of the content panel
        int fixedContent = 0;
        if (m_dialogInfo.isVisible()) {
            fixedContent += m_dialogInfo.getOffsetHeight();
        }
        if (m_selectionSummary.isVisible()) {
            fixedContent += m_selectionSummary.getOffsetHeight();
        }
        m_scrollPanel.getElement().getStyle().setPropertyPx("maxHeight", getAvailableHeight(fixedContent));
        doResize();
    }

    /**
     * Displays the 'use drag and drop' / 'no drag and drop available' message.<p>
     */
    protected void setDragAndDropMessage() {

        if (m_dragAndDropMessage == null) {
            m_dragAndDropMessage = new HTML();
            m_dragAndDropMessage.setStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dragAndDropMessage());
            m_dragAndDropMessage.setText(Messages.get().key(Messages.GUI_UPLOAD_DRAG_AND_DROP_DISABLED_0));
        }
        getContentWrapper().add(m_dragAndDropMessage);
        getContentWrapper().getElement().getStyle().setBackgroundColor(
            I_CmsConstantsBundle.INSTANCE.css().notificationErrorBg());
        doResize();
    }

    /**
     * Sets the HTML of the selection summary.<p>
     *
     * @param html the HTML to set as String
     */
    protected void setSummaryHTML(String html) {

        m_selectionSummary.setHTML(html);
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
                displayDialogInfo(
                    org.opencms.gwt.client.Messages.get().key(
                        org.opencms.gwt.client.Messages.GUI_UPLOAD_INFO_FINISHING_0),
                    false);
                startLoadingAnimation(
                    org.opencms.gwt.client.Messages.get().key(
                        org.opencms.gwt.client.Messages.GUI_UPLOAD_INFO_CREATING_RESOURCES_0),
                    1500);
                break;
            default:
                break;
        }
    }

    /**
     * Adds a click handler for the given check box.<p>
     *
     * @param check the check box
     * @param unzipWidget the un-zip check box
     * @param file the file
     */
    private void addClickHandlerToCheckBox(final CmsCheckBox check, final Widget unzipWidget, final CmsFileInfo file) {

        check.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // add or remove the file from the list of files to upload
                if (check.isChecked()) {
                    getFilesToUpload().put(file.getFileName(), file);
                    if (unzipWidget != null) {
                        enableUnzip(unzipWidget);
                    }
                } else {
                    getFilesToUpload().remove(file.getFileName());
                    if (unzipWidget != null) {
                        disableUnzip(unzipWidget);
                    }
                }

                // disable or enable the OK button
                if (getFilesToUpload().isEmpty()) {
                    disableOKButton(Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_NO_FILES_0));
                } else {
                    enableOKButton();
                }

                // update summary
                updateSummary();

            }

            /**
             * Disables the 'unzip' button
             *
             * @param unzip the unzip button
             */
            private void disableUnzip(Widget unzip) {

                ((CmsToggleButton)unzip).setEnabled(false);
            }

            /**
             * Enables the 'unzip' button
             *
             * @param unzip the unzip button
             */
            private void enableUnzip(Widget unzip) {

                ((CmsToggleButton)unzip).setEnabled(true);
            }
        });
    }

    /**
     * Adds a file to the list.<p>
     *
     * @param file the file to add
     * @param invalid signals if the filename is invalid
     * @param existingDeleted in case of existing files marked as deleted
     * @param isTooLarge signals if the file size limit is exceeded
     * @param forbiddenType true if the file has a forbidden type
     */
    private void addFileToList(
        final CmsFileInfo file,
        boolean invalid,
        boolean existingDeleted,
        boolean isTooLarge,
        boolean forbiddenType) {

        CmsListInfoBean infoBean = createInfoBean(file);
        CmsListItemWidget listItemWidget = new CmsListItemWidget(infoBean);
        listItemWidget.setIcon(CmsCoreProvider.get().getResourceTypeIcon(file));
        CmsCheckBox check = new CmsCheckBox();
        check.setChecked(false);
        if (!invalid && !isTooLarge && !existingDeleted && !forbiddenType) {
            if (file.getFileSize() == 0) {
                check.setChecked(false);
            }
            check.setChecked(m_filesToUpload.containsKey(file.getFileName()));
            check.setTitle(file.getFileName());
            if (!m_selectionDone && file.getFileName().toLowerCase().endsWith(".zip")) {
                final Widget unzip = createUnzipCheckBox(file);
                addClickHandlerToCheckBox(check, unzip, file);
                listItemWidget.addButton(unzip);
            } else {
                addClickHandlerToCheckBox(check, null, file);
            }
        } else if (forbiddenType) {
            String message = Messages.get().key(Messages.GUI_UPLOAD_RESTRICTED_0);
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        } else if (existingDeleted) {
            // is invalid
            String message = Messages.get().key(Messages.GUI_UPLOAD_FILE_EXISTING_DELETED_1, file.getFileName());
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        } else if (isTooLarge) {
            String message = getFileSizeTooLargeMessage(file);
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        } else {
            // is invalid
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_FILE_INVALID_NAME_2,
                file.getFileName(),
                CmsUploadButton.formatBytes(file.getFileSize()));
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        }

        CmsListItem listItem = new CmsListItem(check, listItemWidget);
        m_fileList.addItem(listItem);
        m_listItems.put(file.getFileName(), listItem);
        doResize();
    }

    /**
     * Changes the height of the content wrapper so that the dialog finally has the
     * same height that the dialog has when the min height is set on the selection screen.<p>
     */
    private void changeHeight() {

        int firstHeight = MIN_CONTENT_HEIGHT + m_firstInfoHeight + m_firstSummaryHeight + 2;
        int currentHeight = CmsDomUtil.getCurrentStyleInt(m_mainPanel.getElement(), CmsDomUtil.Style.height);
        int targetHeight = firstHeight - m_dialogInfo.getOffsetHeight() - m_selectionSummary.getOffsetHeight();
        if (currentHeight > firstHeight) {
            CmsChangeHeightAnimation.change(m_scrollPanel.getElement(), targetHeight, new Command() {

                public void execute() {

                    doResize();
                }
            }, 750);
        }
    }

    /**
     * Before the upload data is effectively submited we have to check
     * for already existent resources in the VFS.<p>
     *
     * Executes the RPC call that checks the VFS for existing resources.
     * Passes the response object to a method that evaluates the result.<p>
     */
    private void checkSelection() {

        m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_CHECKING_0));

        if (!m_selectionDone) {
            m_firstContentHeight = CmsDomUtil.getCurrentStyleInt(m_scrollPanel.getElement(), CmsDomUtil.Style.height);
            m_firstInfoHeight = m_dialogInfo.getOffsetHeight();
            m_firstSummaryHeight = m_selectionSummary.getOffsetHeight();
        }

        CmsRpcAction<CmsUploadFileBean> callback = new CmsRpcAction<CmsUploadFileBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                List<String> filesToCheck = new ArrayList<String>(getFilesToUpload().keySet());
                filesToCheck.removeAll(getFilesToUnzip(false));
                getUploadService().checkUploadFiles(filesToCheck, getTargetFolder(), m_isTargetRootPath, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUploadFileBean result) {

                proceedWorkflow(result);
            }
        };
        callback.execute();
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

                A_CmsUploadDialog.this.hide();
            }
        };
        closeTimer.schedule(1500);
    }

    /**
     * Calls the submit action if there are any files selected for upload.<p>
     */
    private void commit() {

        m_selectionDone = true;
        if (!m_filesToUpload.isEmpty()) {
            m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_UPLOADING_0));
            if (m_uploadButton instanceof UIObject) {
                ((UIObject)m_uploadButton).getElement().getStyle().setDisplay(Display.NONE);
            }
            showProgress();
            submit();
        }
    }

    /**
     * Creates the "OK", the "Cancel" and the "Upload" button.<p>
     */
    private void createButtons() {

        addDialogClose(new Command() {

            public void execute() {

                cancelUpload();
            }
        });

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setTitle(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancelButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        cancelButton.setSize(I_CmsButton.Size.medium);
        cancelButton.setUseMinWidth(true);
        cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                cancelUpload();
            }
        });
        addButton(cancelButton);

        m_okButton = new CmsPushButton();
        m_okButton.setTitle(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.setSize(I_CmsButton.Size.medium);
        m_okButton.setUseMinWidth(true);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onOkClick();
            }
        });
        addButton(m_okButton);

        CmsDialogUploadButtonHandler buttonHandler = new CmsDialogUploadButtonHandler(
            new Supplier<I_CmsUploadContext>() {

                public I_CmsUploadContext get() {

                    return m_context;
                }
            });
        buttonHandler.setUploadDialog(this);
        // add a new upload button
        CmsUploadButton uploadButton = new CmsUploadButton(buttonHandler);
        uploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadDialogButton());
        uploadButton.setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_ADD_FILES_0));
        addButton(uploadButton);
        m_uploadButton = uploadButton;
    }

    /**
     * Creates the unzip checkbox.<p>
     *
     * @param file the file to create the checkbox for
     *
     * @return the unzip checkbox
     */
    private Widget createUnzipCheckBox(final CmsFileInfo file) {

        final CmsToggleButton unzip = new CmsToggleButton();
        I_CmsUploadCss uploadCss = I_CmsLayoutBundle.INSTANCE.uploadCss();
        String caption = Messages.get().key(Messages.GUI_UNZIP_BUTTON_TEXT_0);
        unzip.setUpFace(caption, uploadCss.unzipButtonUpFace());
        unzip.setDownFace(caption, uploadCss.unzipButtonDownFace());
        unzip.addStyleName(uploadCss.unzipButton());
        unzip.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        unzip.setDown(getFilesToUnzip(true).contains(file.getFileName()));
        if (!m_filesToUpload.containsKey(file.getFileName())) {
            unzip.disable(Messages.get().key(Messages.GUI_UPLOAD_FILE_NOT_SELECTED_0));
        }
        unzip.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // add or remove the file from the list of files to upload
                if (unzip.isDown()) {
                    getFilesToUnzip(true).add(file.getFileName());
                } else {
                    getFilesToUnzip(true).remove(file.getFileName());
                }
            }
        });
        return unzip;
    }

    /**
     * Sets the user info.<p>
     *
     * @param msg the message to display
     * @param warning signals whether the message should be a warning or nor
     */
    private void displayDialogInfo(String msg, boolean warning) {

        StringBuffer buffer = new StringBuffer(64);
        if (!warning) {
            buffer.append("<p class=\"");
            buffer.append(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().dialogMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        } else {
            buffer.append(FontOpenCms.WARNING.getHtml(32, I_CmsConstantsBundle.INSTANCE.css().colorWarning()));
            buffer.append("<p class=\"");
            buffer.append(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().warningMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        }
        m_dialogInfo.setHTML(buffer.toString());
    }

    /**
     * Gets the target root path.
     *
     * @return the target root path
     */
    private String getTargetRootPath() {

        if (m_isTargetRootPath) {
            return m_targetFolder;
        } else {
            return CmsCoreProvider.get().addSiteRoot(m_targetFolder);
        }
    }

    /**
     * Helper method to upload the hook dialog after an upload.
     *
     * @param uploadedFileIds the uploaded file ids
     * @param hookUri the hook URI
     * @param context the upload context
     */
    private void openHookDialog(List<String> uploadedFileIds, String hookUri, final I_CmsUploadContext context) {

        CloseHandler<PopupPanel> closeHandler;
        closeHandler = new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                if (context != null) {
                    List<CmsUUID> actualIds = uploadedFileIds.stream().map(id -> new CmsUUID(id)).collect(
                        Collectors.toList());
                    // post-upload hook may rename files
                    CmsCoreProvider.getVfsService().getSitePaths(actualIds, new AsyncCallback<List<String>>() {

                        @Override
                        public void onFailure(Throwable caught) {}

                        @Override
                        public void onSuccess(List<String> result) {

                            for (int i = 0; i < result.size(); i++) {
                                String path = result.get(i);
                                if (path.startsWith(m_targetFolder)) {
                                    result.set(i, path.substring(m_targetFolder.length()));
                                }
                            }
                            context.onUploadFinished(result);
                        }
                    });
                }
            }
        };

        String title = Messages.get().key(Messages.GUI_UPLOAD_HOOK_DIALOG_TITLE_0);
        CmsUploadHookDialog.openDialog(title, hookUri, uploadedFileIds, closeHandler);
    }

    /**
     * Removes all widgets from the content wrapper.<p>
     */
    private void removeContent() {

        m_contentWrapper.clear();
        doResize();
    }

    /**
     * Sets the height for the content so that the dialog finally has the same height
     * as the dialog has on the selection screen.<p>
     */
    private void setHeight() {

        int infoDiff = m_firstInfoHeight - m_dialogInfo.getOffsetHeight();
        int summaryDiff = m_firstSummaryHeight - m_selectionSummary.getOffsetHeight();
        int height = m_firstContentHeight + infoDiff + summaryDiff;
        m_scrollPanel.getElement().getStyle().setHeight(height, Unit.PX);
        m_scrollPanel.getElement().getStyle().clearProperty("minHeight");
        m_scrollPanel.getElement().getStyle().clearProperty("maxHeight");
        doResize();
    }

    /**
     * Shows the overwrite dialog.<p>
     *
     * @param infoBean the info bean containing the existing and invalid file names
     */
    private void showOverwriteDialog(CmsUploadFileBean infoBean) {

        // update the dialog
        m_selectionDone = true;
        m_okButton.enable();
        if (infoBean.getInvalidFileNames().isEmpty() && infoBean.getExistingDeletedFileNames().isEmpty()) {
            displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_OVERWRITE_0), true);
        } else {
            displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_INVALID_0), true);
        }
        if (m_uploadButton instanceof UIObject) {
            ((UIObject)m_uploadButton).getElement().getStyle().setDisplay(Display.NONE);
        }
        // clear the list
        m_fileList.clearList();

        // handle existing files
        List<String> existings = new ArrayList<String>(infoBean.getExistingResourceNames());
        Collections.sort(existings, String.CASE_INSENSITIVE_ORDER);
        for (String filename : existings) {
            addFileToList(m_filesToUpload.get(filename), false, false, false, false);
        }

        // handle the invalid files
        List<String> invalids = new ArrayList<String>(infoBean.getInvalidFileNames());
        Collections.sort(invalids, String.CASE_INSENSITIVE_ORDER);
        for (String filename : invalids) {
            addFileToList(m_filesToUpload.get(filename), true, false, false, false);
            m_filesToUpload.remove(filename);
        }

        // handle the invalid files
        List<String> existingDeleted = new ArrayList<String>(infoBean.getExistingDeletedFileNames());
        Collections.sort(existingDeleted, String.CASE_INSENSITIVE_ORDER);
        for (String filename : existingDeleted) {
            addFileToList(m_filesToUpload.get(filename), false, true, false, false);
            m_filesToUpload.remove(filename);
        }

        // set the height of the content
        setHeight();
    }

    /**
     * Starts the upload progress bar.<p>
     */
    private void showProgress() {

        removeContent();
        displayDialogInfo(
            org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_UPLOAD_INFO_UPLOADING_0),
            false);
        m_selectionSummary.removeFromParent();
        List<String> files = new ArrayList<String>(getFilesToUpload().keySet());
        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
        m_progressInfo = new CmsUploadProgressInfo(files);
        m_progressInfo.setContentLength(m_contentLength);
        m_contentWrapper.add(m_progressInfo);
        m_updateProgressTimer.scheduleRepeating(UPDATE_PROGRESS_INTERVALL);
        startLoadingAnimation(Messages.get().key(Messages.GUI_UPLOAD_CLIENT_LOADING_0), 0);
        setHeight();
        changeHeight();
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

                createLoadingAnimation(msg);
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
            m_contentWrapper.remove(m_loadingPanel);
            doResize();
            m_clientLoading = false;
        }
    }
}
