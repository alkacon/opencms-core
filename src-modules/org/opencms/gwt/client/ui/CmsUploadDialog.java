/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsUploadDialog.java,v $
 * Date   : $Date: 2011/02/15 07:33:48 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsUploadFileBean;
import org.opencms.gwt.shared.CmsUploadFileBean.I_CmsUploadConstants;
import org.opencms.gwt.shared.CmsUploadProgessInfo;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;

/**
 * Provides a upload dialog.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsUploadDialog extends CmsPopupDialog {

    /**
     * Implements the submit handler (Used for browsers that don't support file api).<p>
     */
    protected class CmsUploadHandler implements SubmitCompleteHandler, SubmitHandler {

        /**
         * @see com.google.gwt.user.client.ui.FormPanel.SubmitHandler#onSubmit(com.google.gwt.user.client.ui.FormPanel.SubmitEvent)
         */
        public void onSubmit(SubmitEvent event) {

            m_submitEvent = event;
        }

        /**
         * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
         */
        public void onSubmitComplete(SubmitCompleteEvent event) {

            parseResponse(event.getResults());
        }
    }

    /**
     * Provides the upload progress information.<p>
     * 
     * Has a progressbar and a table for showing details.<p>
     */
    private class CmsUploadProgressInfo extends FlowPanel {

        /** The progress bar. */
        private CmsProgressBar m_bar;

        /** The table for showing upload details. */
        private FlexTable m_fileinfo;

        /** A sorted list of the filenames to upload. */
        private List<String> m_orderedFilenamesToUpload;

        /** Signals if the progress was set at least one time. */
        private boolean m_started;

        /**
         * Default constructor.<p>
         */
        public CmsUploadProgressInfo() {

            // get the content length
            m_contentLength = getContentLength();
            // get a ordered list of filenames
            m_orderedFilenamesToUpload = new ArrayList<String>(m_filesToUpload.keySet());
            Collections.sort(m_orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);

            // create the progress bar
            m_bar = new CmsProgressBar();

            // create the file info table
            m_fileinfo = new FlexTable();
            m_fileinfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().fileInfoTable());

            // arrange the progress info
            addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().progressInfo());
            add(m_bar);
            add(m_fileinfo);
        }

        /**
         * Finishes the state of the progress bar.<p>
         */
        public void finish() {

            String length = formatBytes(new Long(m_contentLength).intValue());
            int fileCount = m_orderedFilenamesToUpload.size();
            m_bar.setValue(100);
            m_fileinfo.removeAllRows();
            m_fileinfo.setHTML(0, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_FINISH_UPLOADED_0) + "</b>");
            m_fileinfo.setText(
                0,
                1,
                Messages.get().key(
                    Messages.GUI_UPLOAD_FINISH_UPLOADED_VALUE_4,
                    new Integer(fileCount),
                    new Integer(fileCount),
                    getFileText(),
                    length));
        }

        /**
         * Sets the progress information.<p>
         * 
         * @param info the progress info bean
         */
        public void setProgress(CmsUploadProgessInfo info) {

            int currFile = info.getCurrentFile();

            int currFileIndex = 0;
            if (currFile == 0) {
                // no files read so far 
            } else {
                currFileIndex = currFile - 1;
            }

            String currFilename = m_orderedFilenamesToUpload.get(currFileIndex);
            String contentLength = formatBytes(new Long(m_contentLength).intValue());
            int fileCount = m_orderedFilenamesToUpload.size();
            String readBytes = formatBytes(new Long(getBytesRead(info.getPercent())).intValue());

            m_bar.setValue(info.getPercent());

            if (!m_started) {
                m_started = true;
                m_fileinfo.setHTML(0, 0, "<b>"
                    + Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_CURRENT_FILE_0)
                    + "</b>");
                m_fileinfo.setHTML(1, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_UPLOADING_0) + "</b>");
                m_fileinfo.setHTML(2, 0, "");

                m_fileinfo.setText(0, 1, "");
                m_fileinfo.setText(1, 1, "");
                m_fileinfo.setText(2, 1, "");

                m_fileinfo.getColumnFormatter().setWidth(0, "100px");
                m_fileinfo.getColumnFormatter().setWidth(1, "auto");
            }

            m_fileinfo.setText(0, 1, currFilename);
            m_fileinfo.setText(
                1,
                1,
                Messages.get().key(
                    Messages.GUI_UPLOAD_PROGRESS_CURRENT_VALUE_3,
                    new Integer(currFile),
                    new Integer(fileCount),
                    getFileText()));
            m_fileinfo.setText(
                2,
                1,
                Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_UPLOADING_VALUE_2, readBytes, contentLength));
        }

        /**
         * Returns the bytes that are read so far.<p>
         * 
         * The total request size is larger than the sum of all file sizes that are uploaded.
         * Because boundaries and the target folder or even some other information than only
         * the plain file contents are submited to the server.<p>
         * 
         * This method calculates the bytes that are read with the help of the file sizes.<p>
         *  
         * @param percent the server side determined percentage
         * 
         * @return the bytes that are read so far
         */
        private long getBytesRead(long percent) {

            return percent != 0 ? m_contentLength * percent / 100 : 0;
        }
    }

    /**
     * Enum for the state this dialog.<p>
     */
    private enum DialogState {
        /** Another file upload is currently running. */
        active,
        /** Canceld by the user. */
        canceled,
        /** File check state. (System checks for existing files) */
        check,
        /** Error state (System has encountered an error) */
        error,
        /** File overwrite state. (User selects files to overwrite) */
        overwrite,
        /** File selection state. (User adds files to the list) */
        selection,
        /** File upload state. (System uploads the files of this dialog) */
        upload
    }

    /** The size for kilobytes in bytes. */
    private static final float KILOBYTE = 1024L;

    /** Maximum height for the dialog content. */
    private static final int MAX_HEIGHT = 218;

    /** Minimal height for the dialog content. */
    private static final int MIN_HEIGHT = 110;

    /** The default interval. */
    private static final int UPDATE_PROGRESS_INTERVALL = 1000;

    /** The sum of all file sizes. */
    protected long m_contentLength;

    /** The existing files. */
    protected Map<String, CmsFileInfo> m_existingFiles;

    /** The Map of files to upload. */
    protected Map<String, CmsFileInfo> m_filesToUpload;

    /** Stores the current state of this dialog. */
    protected DialogState m_state;

    /** The submit event to cancel the request. */
    protected SubmitEvent m_submitEvent;

    /** The target folder to upload the selected files. */
    protected String m_targetFolder;

    /** Stores all files that were added. */
    private Map<String, CmsFileInfo> m_allFiles;

    /** Signals that the client currently loading. */
    private boolean m_clientLoading;

    /** A flow panel with a dynamic height. */
    private FlowPanel m_contentWrapper;

    /** The user information text widget. */
    private HTML m_dialogInfo;

    /** The panel that contains all the file input fields. */
    private FlowPanel m_fileInputPanel;

    /** Contains all file input widgets. */
    private List<CmsFileInput> m_fileInputs;

    /** Stores the content height of the selection dialog. */
    private int m_firstContentHeight;

    /** Stores the height of the user information text widget of the selection dialog. */
    private int m_firstInfoHeight;

    /** Stores the height of the summary. */
    private int m_firstSummaryHeight;

    /** The form for the upload. */
    private FormPanel m_form;

    /** The handler for this dialog. */
    private CmsUploadHandler m_formHandler;

    /** Stores the list items of all added files. */
    private Map<String, CmsListItem> m_listItems;

    /** Signals if the dialog has already been loaded. */
    private boolean m_loaded;

    /** A panel for showing client loading. */
    private FlowPanel m_loadingPanel;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /** The progress bar for the upload process. */
    private CmsUploadProgressInfo m_progressInfo;

    /** The user information text widget. */
    private HTML m_selectionSummary;

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

    /**
     * Default constructor.<p>
     */
    public CmsUploadDialog() {

        setText(Messages.get().key(Messages.GUI_UPLOAD_DIALOG_TITLE_0));
        setModal(true);
        setGlassEnabled(true);
        catchNotifications();
        setWidth("439px");
        addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadDialogContent());

        // create a map that stores all files (upload, existing, invalid)
        m_allFiles = new HashMap<String, CmsFileInfo>();
        // create a map the holds all the list items for the selection dialog
        m_listItems = new HashMap<String, CmsListItem>();

        // create the "OK" and "Cancel" buttons
        createButtons();

        // initialize a map that stores all the files that should be uploaded
        m_filesToUpload = new HashMap<String, CmsFileInfo>();

        // set the upload JSP URI as action for the form
        m_form = new FormPanel();
        m_form.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadForm());
        m_form.setAction(CmsCoreProvider.get().getUploadUri());
        // because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        m_form.setEncoding(FormPanel.ENCODING_MULTIPART);
        m_form.setMethod(FormPanel.METHOD_POST);
        // add a submit complete handler to the form that closes the popup
        m_formHandler = new CmsUploadHandler();
        m_form.addSubmitCompleteHandler(m_formHandler);
        // set the panel that holds all the form widgets as widget of the form
        m_fileInputPanel = new FlowPanel();
        m_form.setWidget(m_fileInputPanel);

        // add the form (with the upload button) to the buttons on the bottom
        addButton(m_form);

        // add a hidden field containing the target folder parameter
        m_targetFolder = "/demo_t3/";
        final Hidden targetFolder = new Hidden();
        targetFolder.setName("upload_target_folder");
        targetFolder.setValue(m_targetFolder);
        m_fileInputPanel.add(targetFolder);

        // create the main panel
        FlowPanel mainPanel = new FlowPanel();

        // add the user info to the main panel
        m_dialogInfo = new HTML();
        m_dialogInfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogInfo());
        mainPanel.add(m_dialogInfo);

        // add the content wrapper to the main panel
        m_contentWrapper = new FlowPanel();
        m_contentWrapper.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().mainContentWidget());
        m_contentWrapper.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        mainPanel.add(m_contentWrapper);

        m_selectionSummary = new HTML();
        m_selectionSummary.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().summary());
        mainPanel.add(m_selectionSummary);

        // set the main panel as content of the popup
        setContent(mainPanel);
    }

    /**
     * Adds the given file input field to this dialog.<p>
     * 
     * @param fileInput the file input field to add
     */
    public void addFileInput(CmsFileInput fileInput) {

        // add the files selected by the user to the list of files to upload
        if (fileInput != null) {
            addFiles(fileInput);
        }

        // set the state of this dialog on selection and enable the OK button
        m_state = DialogState.selection;
        m_okButton.enable();

        // set the user info
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_SELECTION_0), false);
        // set the selection summary
        updateSummary();

        // add a new upload button
        CmsUploadButton uploadButton = new CmsUploadButton(this);
        uploadButton.setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_ADD_FILES_0));
        m_fileInputPanel.add(uploadButton);

        // show the popup
        center();

        // style the file input panel
        m_fileInputPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().fileInputPanel());
        m_fileInputPanel.getElement().getStyle().setHeight(uploadButton.getOffsetHeight(), Unit.PX);
        m_fileInputPanel.getElement().getStyle().setWidth(uploadButton.getOffsetWidth(), Unit.PX);

        // set the height of the content
        setHeight();
        center();
    }

    /**
     * Loads and shows this dialog.<p>
     */
    public void loadAndShow() {

        if (m_loaded) {
            throw new UnsupportedOperationException();
        } else {
            addFileInput(null);
            m_loaded = true;
        }
    }

    /**
     * Loads and shows the dialog.<p>
     * 
     * @param fileInput the first file input field 
     */
    public void loadAndShow(CmsFileInput fileInput) {

        if (m_loaded) {
            throw new UnsupportedOperationException();
        } else {
            m_fileInputPanel.getElement().appendChild(fileInput.getElement());
            addFileInput(fileInput);
            m_loaded = true;
        }
    }

    /**
     * Cancels the upload.<p>
     */
    protected void cancelUpload() {

        m_state = DialogState.canceled;
        hide();

        CmsRpcAction<Void> callback = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().cancelUpload(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                // noop
            }
        };
        callback.execute();
    }

    /**
     * Stores the excluded filenames in a hidden input field as pipe separated 
     * String and submits the form afterwards if there are selected files left.<p>
     */
    protected void commit() {

        // submit the form if there are selected files left and hide the dialog
        if (!m_filesToUpload.isEmpty()) {
            m_form.getElement().getStyle().setDisplay(Display.NONE);

            List<String> orderedFilenamesToUpload = new ArrayList<String>(m_filesToUpload.keySet());
            Collections.sort(orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);

            // create a JsArray containing the files to upload
            JsArray<CmsFileInfo> filesToUpload = JavaScriptObject.createArray().cast();
            for (String filename : orderedFilenamesToUpload) {
                filesToUpload.push(m_filesToUpload.get(filename));
            }

            // upload the files
            m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_UPLOADING_0));
            m_state = DialogState.upload;
            showProgress();
            fileUpload(CmsCoreProvider.get().getUploadUri(), m_targetFolder, this, filesToUpload);

            // m_form.submit();
        } else {
            m_okButton.enable();
            CmsNotification.get().send(Type.WARNING, Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_NO_FILES_0));
        }
    }

    /**
     * Checks for existing files and triggers the commit action.<p>
     * 
     * Before the form is effectively submited we have to check 
     * for already existent resources in the VFS.<p>
     * 
     * If there are already existent resources another dialog comes up 
     * that provides the possibility to select resources that should be 
     * overwritten. Otherwise if there are only new resource to upload 
     * the form will be submited.<p>
     * 
     * If the user has not selected any file, a warning comes up.<p>
     */
    protected void commitSelectedFiles() {

        m_okButton.disable(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_OK_DISABLE_CHECKING_0));
        m_state = DialogState.check;

        m_firstContentHeight = CmsDomUtil.getCurrentStyleInt(m_contentWrapper.getElement(), CmsDomUtil.Style.height);
        m_firstInfoHeight = m_dialogInfo.getOffsetHeight();
        m_firstSummaryHeight = m_selectionSummary.getOffsetHeight();

        CmsRpcAction<CmsUploadFileBean> callback = new CmsRpcAction<CmsUploadFileBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().checkUploadFiles(
                    new ArrayList<String>(m_filesToUpload.keySet()),
                    m_targetFolder,
                    this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUploadFileBean result) {

                if (result.isActive()) {
                    m_state = DialogState.active;
                    CmsNotification.get().send(
                        Type.WARNING,
                        Messages.get().key(Messages.GUI_UPLOAD_NOTIFICATION_RUNNING_0));
                } else {
                    m_state = DialogState.selection;
                    if (!result.getExistingResourceNames().isEmpty() || !result.getInvalidFileNames().isEmpty()) {
                        showOverwriteDialog(result);
                    } else {
                        commit();
                    }
                }
            }
        };
        callback.execute();
    }

    /**
     * Formats a given bytes value (file size).<p>
     *  
     * @param filesize the file size to format
     * 
     * @return the formated file size in KB
     */
    protected String formatBytes(int filesize) {

        int roundedKB = new Double(Math.ceil(filesize / KILOBYTE)).intValue();
        String formated = NumberFormat.getDecimalFormat().format(new Double(roundedKB));
        return formated + " KB";
    }

    /**
     * Returns the content length.<p>
     * 
     * @return the content length
     */
    protected int getContentLength() {

        int result = 0;
        for (CmsFileInfo file : m_filesToUpload.values()) {
            result += file.getFileSize();

        }
        return result;
    }

    /**
     * Returns "files" or "file" depending on the files to upload.<p>
     * 
     * @return "files" or "file" depending on the files to upload
     */
    protected String getFileText() {

        if (m_filesToUpload.size() > 1) {
            return Messages.get().key(Messages.GUI_UPLOAD_FILES_PLURAL_0);
        }
        return Messages.get().key(Messages.GUI_UPLOAD_FILES_SINGULAR_0);
    }

    /**
     * Parses the response of the server and decides what to do.<p>
     * 
     * @param results a JSON Object
     */
    protected void parseResponse(String results) {

        m_updateProgressTimer.cancel();
        stopLoadingAnimation();

        if ((m_state != DialogState.canceled) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(results)) {

            JSONValue jsonValue = JSONParser.parseStrict(results);
            JSONObject jsonObject = jsonValue.isObject();

            boolean success = jsonObject.get(I_CmsUploadConstants.KEY_SUCCESS).isBoolean().booleanValue();
            String message = jsonObject.get(I_CmsUploadConstants.KEY_MESSAGE).isString().stringValue();
            String stacktrace = jsonObject.get(I_CmsUploadConstants.KEY_STACKTRACE).isString().stringValue();

            //            String currentFile = jsonObject.get(I_CmsUploadConstants.KEY_CURRENT_FILE).isNumber().toString();
            //            String bytesRead = jsonObject.get(I_CmsUploadConstants.KEY_BYTES_READ).isNumber().toString();
            //            String contentLength = jsonObject.get(I_CmsUploadConstants.KEY_CONTENT_LENGTH).isNumber().toString();
            //            String percent = jsonObject.get(I_CmsUploadConstants.KEY_PERCENT).isNumber().toString();
            //            String running = jsonObject.get(I_CmsUploadConstants.KEY_RUNNING).isBoolean().toString();

            if (success) {
                displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                m_progressInfo.finish();
            } else {
                showErrorReport(message, stacktrace);
                m_state = DialogState.error;
            }
        }
    }

    /**
     * Opens the overwrite dialog.<p>
     * 
     * @param infoBean the info bean containing the existing and invalid file names
     */
    protected void showOverwriteDialog(CmsUploadFileBean infoBean) {

        // update the dialog
        m_state = DialogState.overwrite;
        m_okButton.enable();
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_OVERWRITE_0), true);
        // hide the form with the upload button
        m_form.getElement().getStyle().setDisplay(Display.NONE);

        // handle existing files
        List<CmsFileInfo> existings = getFilesForFilenames(m_filesToUpload, infoBean.getExistingResourceNames());
        Map<String, CmsFileInfo> existingFiles = createFileMapFromList(existings);
        rebuildList(existingFiles);

        // handle the invalid files
        List<String> invalids = new ArrayList<String>(infoBean.getInvalidFileNames());
        Collections.sort(invalids, String.CASE_INSENSITIVE_ORDER);
        Map<String, String> beans = getResourceTypes(invalids);
        for (String filename : invalids) {
            addFileToList(m_filesToUpload.get(filename), beans.get(filename), true);
            m_filesToUpload.remove(filename);
        }
        setHeight();
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

                CmsCoreProvider.getService().getUploadProgressInfo(this);
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

        if (m_state == DialogState.upload) {
            if (info.isRunning()) {
                stopLoadingAnimation();
                m_progressInfo.setProgress(info);
            }
        }
    }

    /**
     * Updates the file summary.<p>
     */
    protected void updateSummary() {

        m_contentLength = getContentLength();
        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<p class=\"").append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessage()).append("\">");
        buffer.append("<b>" + Messages.get().key(Messages.GUI_UPLOAD_SUMMARY_FILES_0) + "</b> ");
        buffer.append(Messages.get().key(
            Messages.GUI_UPLOAD_SUMMARY_FILES_VALUE_3,
            new Integer(m_filesToUpload.size()),
            getFileText(),
            formatBytes(new Long(m_contentLength).intValue())));
        buffer.append("</p>");
        m_selectionSummary.setHTML(buffer.toString());
    }

    /**
     * Adds a click handler for the given checkbox.<p>
     * 
     * @param check the checkbox
     * @param file the file
     */
    private void addClickHandlerToCheckBox(final CmsCheckBox check, final CmsFileInfo file) {

        check.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (check.isChecked()) {
                    m_filesToUpload.put(file.getFileName(), file);
                } else {
                    m_filesToUpload.remove(file.getFileName());
                }
                updateSummary();
            }
        });
    }

    /**
     * Adds the selected files from the given input field to the files to upload.<p>
     * 
     * @param input the file input field
     */
    private void addFiles(CmsFileInput input) {

        if (m_fileInputs == null) {
            m_fileInputs = new ArrayList<CmsFileInput>();
        }
        m_fileInputs.add(input);

        List<CmsFileInfo> fileObjects = Arrays.asList(input.getFiles());
        for (CmsFileInfo file : fileObjects) {
            m_allFiles.put(file.getFileName(), file);
            m_filesToUpload.put(file.getFileName(), file);
            if ((m_listItems.get(file.getFileName()) != null)
                && (m_listItems.get(file.getFileName()).getCheckBox() != null)
                && !m_listItems.get(file.getFileName()).getCheckBox().isChecked()) {
                m_filesToUpload.remove(file.getFileName());
            }
        }
        rebuildList(m_allFiles);
    }

    /**
     * Adds a file to the list.<p>
     * 
     * @param file the file to add
     * @param resType the resource type of the file
     * @param invalid signals if the filename is invalid
     */
    private void addFileToList(CmsFileInfo file, String resType, boolean invalid) {

        CmsListInfoBean infoBean = new CmsListInfoBean(file.getFileName(), formatBytes(file.getFileSize()), null);
        CmsListItemWidget listItemWidget = new CmsListItemWidget(infoBean);
        listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resType, file.getFileName(), false));

        CmsCheckBox check = new CmsCheckBox();
        check.setChecked(false);
        if (!invalid) {
            if (m_filesToUpload.containsKey(file.getFileName())) {
                check.setChecked(true);
            }
            check.setTitle(file.getFileName());
            addClickHandlerToCheckBox(check, file);
        } else {
            check.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
        CmsListItem listItem = new CmsListItem();
        listItem.initContent(check, listItemWidget);
        m_contentWrapper.add(listItem);
        m_listItems.put(file.getFileName(), listItem);
    }

    /**
     * Creates the buttons.<p>
     */
    private void createButtons() {

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setTitle(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
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
        m_okButton.setTitle(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setSize(I_CmsButton.Size.medium);
        m_okButton.setUseMinWidth(true);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                switch (m_state) {
                    case selection:
                        commitSelectedFiles();
                        break;
                    case check:
                        break;
                    case overwrite:
                        commit();
                        break;
                    case upload:
                        break;
                    case active:
                        commit();
                        break;
                    case error:
                        CmsUploadDialog.this.hide();
                        break;
                    default:
                        break;

                }
            }
        });
        addButton(m_okButton);
    }

    /**
     * Creates a Map containing file objects as values and the name of the file as key.<p>
     * 
     * @param files the list of files
     * 
     * @return the map of files
     */
    private Map<String, CmsFileInfo> createFileMapFromList(List<CmsFileInfo> files) {

        Map<String, CmsFileInfo> result = new HashMap<String, CmsFileInfo>();
        for (CmsFileInfo file : files) {
            result.put(file.getFileName(), file);
        }
        return result;
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
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        } else {
            buffer.append("<div class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().warningIcon());
            buffer.append("\"></div>");
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().warningMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        }
        m_dialogInfo.setHTML(buffer.toString());
    }

    /**
     * Sends a post request to the upload JSP.<p>
     * 
     * @param uploadUri the URI of the JSP that performs the upload
     * @param targetFolder the target folder to upload
     * @param dialog this dialog
     * @param filesToUpload the file names to upload
     */
    private native void fileUpload(
        String uploadUri,
        String targetFolder,
        CmsUploadDialog dialog,
        JsArray<CmsFileInfo> filesToUpload) /*-{
		// is executed when there was an error during reading the file
		function errorHandler(evt) {
			alert("Error");
		}

		// is executed when the current file is read completely
		function loaded(evt) {
			// get the current file name and obtain the read file data
			var fileName = file.name;
			var fileData = evt.target.result;
			body += "Content-Disposition: form-data; name=\"fileId\"; filename=\""
					+ fileName + "\"\r\n";
			body += "Content-Type: application/octet-stream\r\n\r\n";
			body += fileData + "\r\n";
			body += "--" + boundary + "\r\n";
			// are there any more files?, continue reading the next file
			if (filesToUpload.length > ++curIndex) {
				file = filesToUpload[curIndex];
				this.readAsBinaryString(file);
			} else {
				// there are no more files left
				// append the target folder to the request 
				appendTargetFolder();
				// create the request and post it
				var xhr = new XMLHttpRequest();
				xhr.open("POST", uri, true);
				xhr.setRequestHeader("Content-Type",
						"multipart/form-data; boundary=" + boundary); // simulate a file MIME POST request.
				xhr.overrideMimeType('text/plain; charset=x-user-defined');
				xhr.onreadystatechange = function() {
					if (xhr.readyState == 4) {
						if (xhr.status == 200) {
							dialog.@org.opencms.gwt.client.ui.CmsUploadDialog::parseResponse(Ljava/lang/String;)(xhr.responseText);
						} else if (xhr.status != 200) {
							alert("Error");
						}
					}
				}
				xhr.sendAsBinary(body);
			}
		}

		// appends the target folder to the request body 
		// should be called at end of creating the body because the boundary is closed here
		function appendTargetFolder() {
			body += "Content-Disposition: form-data; name=upload_target_folder\r\n";
			body += "Content-Type: text/plain\r\n\r\n";
			body += targetFolder + "\r\n";
			body += "--" + boundary + "--";
		}

		// the uri to call
		var uri = uploadUri;
		// the boundary
		var boundary = "26924190726270";
		// the request body with the starting boundary
		var body = "--" + boundary + "\r\n";

		// the main procedure
		if (filesToUpload) {

			var curIndex = 0;
			var file = filesToUpload[curIndex];
			var reader = new FileReader();

			// Handle loaded and errors
			reader.onload = loaded;
			reader.onerror = errorHandler;
			// Read file into memory
			reader.readAsBinaryString(file);
		}
    }-*/;

    /**
     * Searches in the given List of file objects for the file names.<p>
     * 
     * @param files the files 
     * @param names the filenames to search for
     * 
     * @return a list with the found file objects
     */
    private List<CmsFileInfo> getFilesForFilenames(final Map<String, CmsFileInfo> files, final List<String> names) {

        List<CmsFileInfo> result = new ArrayList<CmsFileInfo>();
        for (String filename : names) {
            if (files.containsKey(filename)) {
                result.add(files.get(filename));
            }
        }
        return result;
    }

    /**
     * Returns a map with filename as key and the according resource type as value.<p>
     * 
     * @param filenames the filenames to get the resource types for
     * 
     * @return a map with filename as key and the according resource type as value
     */
    private Map<String, String> getResourceTypes(Collection<String> filenames) {

        Map<String, String> result = new HashMap<String, String>();
        for (String filename : filenames) {
            // TODO: get the right resource type here
            String resType = "image";
            result.put(filename, resType);
        }
        return result;
    }

    /**
     * Adds a the file infos to the table.<p>
     * 
     * @param file the file to add
     */
    private void rebuildList(Map<String, CmsFileInfo> files) {

        removeContent();

        // sort the files for name
        List<String> sortedFileNames = new ArrayList<String>();
        sortedFileNames.addAll(files.keySet());
        Collections.sort(sortedFileNames, String.CASE_INSENSITIVE_ORDER);

        Map<String, String> beans = getResourceTypes(files.keySet());

        for (String filename : sortedFileNames) {
            String resType = beans.get(filename);
            addFileToList(files.get(filename), resType, false);
        }
    }

    /**
     * Removes all widgets from the content wrapper.<p>
     */
    private void removeContent() {

        int widgetCount = m_contentWrapper.getWidgetCount();
        for (int i = 0; i < widgetCount; i++) {
            m_contentWrapper.remove(0);
        }
    }

    /**
     * Sets the height for the content and centers the dialog afterwards.<p>
     */
    private void setHeight() {

        if (m_state == DialogState.selection) {
            DOM.setStyleAttribute(m_contentWrapper.getElement(), "minHeight", MIN_HEIGHT + "px");
            DOM.setStyleAttribute(m_contentWrapper.getElement(), "maxHeight", MAX_HEIGHT + "px");
        } else {
            int infoDiff = m_firstInfoHeight - m_dialogInfo.getOffsetHeight();
            int summaryDiff = m_firstSummaryHeight - m_selectionSummary.getOffsetHeight();
            int height = m_firstContentHeight + infoDiff + summaryDiff;
            m_contentWrapper.getElement().getStyle().setHeight(height, Unit.PX);
            DOM.setStyleAttribute(m_contentWrapper.getElement(), "minHeight", "");
            DOM.setStyleAttribute(m_contentWrapper.getElement(), "maxHeight", "");
        }
    }

    /**
     * Shows the error report.<p>
     * 
     * @param message the message to show
     * @param stacktrace the stacktrace to show
     */
    private void showErrorReport(final String message, final String stacktrace) {

        hide();
        new CmsErrorDialog(message, stacktrace.replaceAll("\n", "<br/>")).center();
    }

    /**
     * Starts the upload progress bar.<p>
     */
    private void showProgress() {

        removeContent();
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_UPLOADING_0), false);
        m_selectionSummary.removeFromParent();
        m_progressInfo = new CmsUploadProgressInfo();
        m_contentWrapper.add(m_progressInfo);
        m_updateProgressTimer.scheduleRepeating(UPDATE_PROGRESS_INTERVALL);
        setHeight();
        startLoadingAnimation(Messages.get().key(Messages.GUI_UPLOAD_CLIENT_LOADING_0));
    }

    /**
     * Starts the loading animation.<p>
     * 
     * Used while client is loading files from hard disk into memory.<p>
     * 
     * @param msg the message that should be displayed below the loading animation (can also be HTML as String)
     */
    private void startLoadingAnimation(String msg) {

        m_clientLoading = true;
        m_loadingPanel = new FlowPanel();
        m_loadingPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingPanel());
        m_loadingPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        HTML animationDiv = new HTML();
        animationDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingAnimation());
        m_loadingPanel.add(animationDiv);

        HTML messageDiv = new HTML();
        messageDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().loadingText());
        messageDiv.setHTML(msg);
        m_loadingPanel.add(messageDiv);

        m_contentWrapper.add(m_loadingPanel);
    }

    /**
     * Stops the client loading animation.<p>
     */
    private void stopLoadingAnimation() {

        if (m_clientLoading) {
            m_contentWrapper.remove(m_loadingPanel);
            m_clientLoading = false;
        }
    }
}
