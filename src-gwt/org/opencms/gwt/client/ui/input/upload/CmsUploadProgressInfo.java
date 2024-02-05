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

package org.opencms.gwt.client.ui.input.upload;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsProgressBar;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsUploadProgessInfo;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Provides the upload progress information.<p>
 *
 * Has a progressbar and a table for showing details.<p>
 */
public class CmsUploadProgressInfo extends FlowPanel {

    /** The progress bar. */
    private CmsProgressBar m_bar;

    /** The table for showing upload details. */
    private FlexTable m_fileinfo;

    /** A sorted list of the filenames to upload. */
    private List<String> m_orderedFilenamesToUpload;

    /** Signals if the progress was set at least one time. */
    private boolean m_started;

    /** The upload content length. */
    private long m_contentLength;

    /**
     * Default constructor.<p>
     *
     * @param orderedFilenamesToUpload the files
     */
    public CmsUploadProgressInfo(List<String> orderedFilenamesToUpload) {

        // get a ordered list of filenames
        m_orderedFilenamesToUpload = orderedFilenamesToUpload;

        // create the progress bar
        m_bar = new CmsProgressBar();

        // create the file info table
        m_fileinfo = new FlexTable();
        m_fileinfo.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().fileInfoTable());

        // arrange the progress info
        addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().progressInfo());
        add(m_bar);
        add(m_fileinfo);
    }

    /**
     * Sets the upload content length.<p>
     *
     * @param contentLength the upload content length
     */
    public void setContentLength(long contentLength) {

        m_contentLength = contentLength;
    }

    /**
     * Finishes the state of the progress bar.<p>
     */
    public void finish() {

        String length = CmsUploadButton.formatBytes(m_contentLength);
        int fileCount = m_orderedFilenamesToUpload.size();
        m_bar.setValue(100);
        m_fileinfo.removeAllRows();
        m_fileinfo.setHTML(0, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_FINISH_UPLOADED_0) + "</b>");
        m_fileinfo.setText(
            0,
            1,
            Messages.get().key(
                Messages.GUI_UPLOAD_FINISH_UPLOADED_VALUE_4,
                Integer.valueOf(fileCount),
                Integer.valueOf(fileCount),
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

        if (m_contentLength == 0) {
            m_contentLength = info.getContentLength();
        }

        String currFilename = m_orderedFilenamesToUpload.get(currFileIndex);
        String contentLength = CmsUploadButton.formatBytes(m_contentLength);
        int fileCount = m_orderedFilenamesToUpload.size();
        String readBytes = CmsUploadButton.formatBytes(getBytesRead(info.getPercent()));

        m_bar.setValue(info.getPercent());

        if (!m_started) {
            m_started = true;
            m_fileinfo.setHTML(0, 0, "<b>" + Messages.get().key(Messages.GUI_UPLOAD_PROGRESS_CURRENT_FILE_0) + "</b>");
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
                Integer.valueOf(currFileIndex + 1),
                Integer.valueOf(fileCount),
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

        return percent != 0 ? (m_contentLength * percent) / 100 : 0;
    }

    /**
     * Returns the file text.<p>
     *
     * @return the file text
     */
    private String getFileText() {

        if (m_orderedFilenamesToUpload.size() > 1) {
            return Messages.get().key(Messages.GUI_UPLOAD_FILES_PLURAL_0);
        } else {
            return Messages.get().key(Messages.GUI_UPLOAD_FILES_SINGULAR_0);
        }
    }
}