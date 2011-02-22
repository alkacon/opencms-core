/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/A_CmsUploadDialog.java,v $
 * Date   : $Date: 2011/02/22 16:34:06 $
 * Version: $Revision: 1.1 $
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
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
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

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Provides an upload dialog.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsUploadDialog extends CmsPopupDialog {

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

            // get a ordered list of filenames
            m_orderedFilenamesToUpload = new ArrayList<String>(getFilesToUpload().keySet());
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

            String length = formatBytes(new Long(getContentLength()).intValue());
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
                if (currFileIndex >= m_orderedFilenamesToUpload.size()) {
                    currFileIndex = m_orderedFilenamesToUpload.size() - 1;
                }
            }

            if (getContentLength() == 0) {
                setContentLength(info.getContentLength());
            }

            String currFilename = m_orderedFilenamesToUpload.get(currFileIndex);
            String contentLength = formatBytes(new Long(getContentLength()).intValue());
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
            }

            m_fileinfo.setText(0, 1, currFilename);
            m_fileinfo.setText(
                1,
                1,
                Messages.get().key(
                    Messages.GUI_UPLOAD_PROGRESS_CURRENT_VALUE_3,
                    new Integer(currFileIndex + 1),
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

            return percent != 0 ? getContentLength() * percent / 100 : 0;
        }
    }

    /** The size for kilobytes in bytes. */
    private static final float KILOBYTE = 1024L;

    /** Maximum width for the file item widget list. */
    private static final int MAX_LIST_WIDTH = 600;

    /** Text metrics key. */
    private static final String TM_FILE_UPLOAD_LIST = "FileUploadList";

    /** The interval for updating the progress information in milliseconds. */
    private static final int UPDATE_PROGRESS_INTERVALL = 1000;

    /** Stores all files that were added. */
    private Map<String, CmsFileInfo> m_allFiles;

    private boolean m_canceled;

    /** Signals that the client currently loading. */
    private boolean m_clientLoading;

    /** The sum of all file sizes. */
    private long m_contentLength;

    /** A flow panel with a dynamic height. */
    private FlowPanel m_contentWrapper;

    /** The user information text widget. */
    private HTML m_dialogInfo;

    /** The list of file item widgets. */
    private CmsList<I_CmsListItem> m_fileList;

    /** The Map of files to upload. */
    private Map<String, CmsFileInfo> m_filesToUpload;

    /** Stores the content height of the selection dialog. */
    private int m_firstContentHeight;

    /** Stores the height of the user information text widget of the selection dialog. */
    private int m_firstInfoHeight;

    /** Stores the height of the summary. */
    private int m_firstSummaryHeight;

    /** The input file input fields. */
    private Map<String, CmsFileInput> m_inputsToUpload;

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
    private CmsUploadButton m_uploadButton;

    /**
     * Default constructor.<p>
     */
    public A_CmsUploadDialog() {

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
        m_inputsToUpload = new HashMap<String, CmsFileInput>();
        m_fileList = new CmsList<I_CmsListItem>();
        m_fileList.truncate(TM_FILE_UPLOAD_LIST, MAX_LIST_WIDTH);

        // initialize a map that stores all the files that should be uploaded
        m_filesToUpload = new HashMap<String, CmsFileInfo>();

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
        m_contentWrapper.getElement().getStyle().setPropertyPx("maxHeight", Window.getClientHeight() - 300);
        mainPanel.add(m_contentWrapper);

        m_selectionSummary = new HTML();
        m_selectionSummary.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().summary());
        mainPanel.add(m_selectionSummary);

        // set the main panel as content of the popup
        setContent(mainPanel);

        // create the "OK" and "Cancel" buttons and add the upload button
        createButtons();
        addUploadButton();
    }

    /**
     * Adds the given file input field to this dialog.<p>
     * 
     * @param fileInput the file input field to add
     */
    public void addFileInput(CmsFileInput fileInput) {

        m_okButton.enable();

        // add the files selected by the user to the list of files to upload
        if (fileInput != null) {
            List<CmsFileInfo> fileObjects = Arrays.asList(fileInput.getFiles());
            for (CmsFileInfo file : fileObjects) {
                m_allFiles.put(file.getFileName(), file);
                if (!isTooLarge(file)) {
                    m_inputsToUpload.put(file.getFileName(), fileInput);
                    m_filesToUpload.put(file.getFileName(), file);
                }
                if ((m_listItems.get(file.getFileName()) != null)
                    && (m_listItems.get(file.getFileName()).getCheckBox() != null)
                    && !m_listItems.get(file.getFileName()).getCheckBox().isChecked()) {
                    m_filesToUpload.remove(file.getFileName());
                }
            }
            rebuildList(m_allFiles);
        }

        // set the user info
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_SELECTION_0), false);
        // set the selection summary
        updateSummary();
        // add a upload button
        addUploadButton();

        // show the popup
        center();

        getDialog().getElement().getStyle().setDisplay(Display.NONE);
        getDialog().getElement().getStyle().setDisplay(Display.BLOCK);
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
     * Returns the targetFolder.<p>
     *
     * @return the targetFolder
     */
    public String getTargetFolder() {

        return m_targetFolder;
    }

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
            addFileInput(fileInput);
            m_loaded = true;
        }
    }

    /**
     * Sets the target folder.<p>
     * 
     * @param target the target folder to set 
     */
    public void setTargetFolder(String target) {

        m_targetFolder = target;
    }

    /**
     * Executes the submit action.<p>
     */
    public abstract void submit();

    /**
     * Updates the file summary.<p>
     */
    public abstract void updateSummary();

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
     * Disables the OK button.<p>
     * 
     * @param disabledReason the reason for disabling the OK button
     */
    protected void disableOKButton(String disabledReason) {

        m_okButton.disable(disabledReason);
    }

    /**
     * Enables the OK button.<p>
     */
    protected void enableOKButton() {

        m_okButton.enable();
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
     * Returns the contentLength.<p>
     *
     * @return the contentLength
     */
    protected long getContentLength() {

        return m_contentLength;
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
            return Messages.get().key(Messages.GUI_UPLOAD_FILES_SINGULAR_0);
        }
        return Messages.get().key(Messages.GUI_UPLOAD_FILES_PLURAL_0);
    }

    /**
     * Returns the inputsToUpload.<p>
     *
     * @return the inputsToUpload
     */
    protected Collection<CmsFileInput> getInputsToUpload() {

        return Collections.unmodifiableCollection(m_inputsToUpload.values());
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
     * Parses the upload response of the server and decides what to do.<p>
     * 
     * @param results a JSON Object
     */
    protected void parseResponse(String results) {

        cancelUpdateProgress();
        stopLoadingAnimation();

        if ((!m_canceled) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(results)) {
            JSONObject jsonObject = JSONParser.parseStrict(results).isObject();

            boolean success = jsonObject.get(I_CmsUploadConstants.KEY_SUCCESS).isBoolean().booleanValue();
            String message = jsonObject.get(I_CmsUploadConstants.KEY_MESSAGE).isString().stringValue();
            String stacktrace = jsonObject.get(I_CmsUploadConstants.KEY_STACKTRACE).isString().stringValue();

            // If the upload is done so fast that we did not receive any progress information, then
            // the content length is unknown. For that reason take the request size to show how 
            // much bytes were uploaded.
            double size = jsonObject.get(I_CmsUploadConstants.KEY_REQUEST_SIZE).isNumber().doubleValue();
            long requestSize = new Double(size).longValue();
            if (m_contentLength == 0) {
                m_contentLength = requestSize;
            }

            if (success) {
                displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_FINISHING_0), false);
                m_progressInfo.finish();
                closeOnSuccess();
            } else {
                showErrorReport(message, stacktrace);
            }
        }
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
            if (!result.getExistingResourceNames().isEmpty() || !result.getInvalidFileNames().isEmpty()) {
                showOverwriteDialog(result);
            } else {
                commit();
            }
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
     * Sets the HTML of the selection summary.<p>
     * 
     * @param html the HTML to set as String 
     */
    protected void setSummaryHTML(String html) {

        m_selectionSummary.setHTML(html);
    }

    /**
     * Shows the error report.<p>
     * 
     * @param message the message to show
     * @param stacktrace the stacktrace to show
     */
    protected void showErrorReport(final String message, final String stacktrace) {

        hide();
        new CmsErrorDialog(message, stacktrace).center();
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

        if (info.isRunning()) {
            stopLoadingAnimation();
            m_progressInfo.setProgress(info);
        }
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

                // add or remove the file from the list of files to upload
                if (check.isChecked()) {
                    getFilesToUpload().put(file.getFileName(), file);
                } else {
                    getFilesToUpload().remove(file.getFileName());
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
        });
    }

    /**
     * Adds a file to the list.<p>
     * 
     * @param file the file to add
     * @param icon the resource type icon
     * @param invalid signals if the filename is invalid
     */
    private void addFileToList(CmsFileInfo file, String icon, boolean invalid, boolean isTooLarge) {

        CmsListInfoBean infoBean = createInfoBean(file);
        CmsListItemWidget listItemWidget = new CmsListItemWidget(infoBean);
        listItemWidget.setIcon(icon);

        CmsCheckBox check = new CmsCheckBox();
        check.setChecked(false);
        if (!invalid && !isTooLarge) {
            if (m_filesToUpload.containsKey(file.getFileName())) {
                check.setChecked(true);
            }
            check.setTitle(file.getFileName());
            addClickHandlerToCheckBox(check, file);
        } else if (isTooLarge) {
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_FILE_TOO_LARGE_2,
                formatBytes(file.getFileSize()),
                formatBytes(new Long(CmsCoreProvider.get().getUploadFileSizeLimit()).intValue()));
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        } else {
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_FILE_INVALID_NAME_2,
                file.getFileName(),
                formatBytes(file.getFileSize()));
            check.disable(message);
            listItemWidget.setBackground(Background.RED);
            listItemWidget.setSubtitleLabel(message);
        }
        CmsListItem listItem = new CmsListItem();
        listItem.initContent(check, listItemWidget);
        m_fileList.addItem(listItem);
        m_listItems.put(file.getFileName(), listItem);
    }

    /**
     * Removes the current upload button and adds a new one.<p>
     */
    private void addUploadButton() {

        if (m_uploadButton != null) {
            removeButton(m_uploadButton);
        }
        // add a new upload button
        m_uploadButton = new CmsUploadButton(this);
        m_uploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadDialogButton());
        m_uploadButton.setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_ADD_FILES_0));
        addButton(m_uploadButton);
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

        int firstContentWidth = CmsDomUtil.getCurrentStyleInt(m_contentWrapper.getElement(), CmsDomUtil.Style.width);
        m_contentWrapper.getElement().getStyle().setWidth(firstContentWidth, Unit.PX);

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
                    new ArrayList<String>(getFilesToUpload().keySet()),
                    getTargetFolder(),
                    this);
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
            m_uploadButton.getElement().getStyle().setDisplay(Display.NONE);
            showProgress();
            submit();
        }
    }

    /**
     * Creates the OK and the Cancel button.<p>
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

                onOkClick();
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
     * Searches in the map of files to upload for the filenames in the given list and
     * returns a list with the found file objects.<p>
     * 
     * @param names the list of filenames to search for
     * 
     * @return a list with the found file objects in the files to upload
     */
    private List<CmsFileInfo> getFilesForFilenames(final List<String> names) {

        List<CmsFileInfo> result = new ArrayList<CmsFileInfo>();
        for (String filename : names) {
            if (m_filesToUpload.containsKey(filename)) {
                result.add(m_filesToUpload.get(filename));
            }
        }
        return result;
    }

    /**
     * Returns a map with filename as key and the according icon classes as value.<p>
     * 
     * @param filenames the filenames to get the resource type icons for
     * 
     * @return a map with filename as key and the according icon classes as value
     */
    private Map<String, String> getResourceIcons(Collection<String> filenames) {

        Map<String, String> result = new HashMap<String, String>();
        for (String filename : filenames) {
            String icon = CmsIconUtil.getResourceTypeIconClasses(filename, false);
            result.put(filename, icon);
        }
        return result;
    }

    /**
     * Rebuilds the list of files to upload.<p>
     * 
     * @param files the map of the files to put in the list
     */
    private void rebuildList(Map<String, CmsFileInfo> files) {

        removeContent();
        m_fileList.clearList();

        // sort the files for name
        List<String> sortedFileNames = new ArrayList<String>();
        sortedFileNames.addAll(files.keySet());
        Collections.sort(sortedFileNames, String.CASE_INSENSITIVE_ORDER);

        Map<String, String> icons = getResourceIcons(files.keySet());

        for (String filename : sortedFileNames) {
            addFileToList(files.get(filename), icons.get(filename), false, isTooLarge(files.get(filename)));
        }
        m_contentWrapper.add(m_fileList);
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

        int infoDiff = m_firstInfoHeight - m_dialogInfo.getOffsetHeight();
        int summaryDiff = m_firstSummaryHeight - m_selectionSummary.getOffsetHeight();
        int height = m_firstContentHeight + infoDiff + summaryDiff;
        m_contentWrapper.getElement().getStyle().setHeight(height, Unit.PX);
        m_contentWrapper.getElement().getStyle().clearProperty("minHeight");
        m_contentWrapper.getElement().getStyle().clearProperty("maxHeight");
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
        displayDialogInfo(Messages.get().key(Messages.GUI_UPLOAD_INFO_OVERWRITE_0), true);
        // hide the upload button
        m_uploadButton.getElement().getStyle().setDisplay(Display.NONE);
        // handle existing files
        List<CmsFileInfo> existings = getFilesForFilenames(infoBean.getExistingResourceNames());
        Map<String, CmsFileInfo> existingFiles = createFileMapFromList(existings);
        rebuildList(existingFiles);

        // handle the invalid files
        List<String> invalids = new ArrayList<String>(infoBean.getInvalidFileNames());
        Collections.sort(invalids, String.CASE_INSENSITIVE_ORDER);
        Map<String, String> icons = getResourceIcons(invalids);
        for (String filename : invalids) {
            addFileToList(m_filesToUpload.get(filename), icons.get(filename), true, false);
            m_filesToUpload.remove(filename);
        }
        setHeight();
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
