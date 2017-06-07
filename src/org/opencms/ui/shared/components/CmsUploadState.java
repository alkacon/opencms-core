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

package org.opencms.ui.shared.components;

import org.opencms.util.CmsUUID;

import com.vaadin.shared.ui.button.ButtonState;

/**
 * The upload button state.<p>
 */
public class CmsUploadState extends ButtonState {

    /** The upload types. */
    public enum UploadType {
        /** Multi file upload. */
        multifile,

        /** Single file upload. */
        singlefile
    }

    /** Serial version id. */
    private static final long serialVersionUID = -763395505975462778L;

    /** The structure id of the file to replace. */
    private CmsUUID m_replaceId;

    /** The target file name. */
    private String m_targetFileName;

    /** The target file name prefix. */
    private String m_targetFileNamePrefix;

    /** The upload target folder root path. */
    private String m_targetFolderRootPath;

    /** The upload type. */
    private UploadType m_uploadType;

    /** The dialog title. */
    private String m_dialogTitle;

    /**
     * Constructor.<p>
     */
    public CmsUploadState() {
        m_uploadType = UploadType.multifile;
    }

    /**
     * Returns the dialog title.<p>
     *
     * @return the dialog title
     */
    public String getDialogTitle() {

        return m_dialogTitle;
    }

    /**
     * Returns the structure id of the file to replace.<p>
     *
     * @return the structure id of the file to replace
     */
    public CmsUUID getReplaceId() {

        return m_replaceId;
    }

    /**
     * Returns the target file name.<p>
     *
     * @return the target file name
     */
    public String getTargetFileName() {

        return m_targetFileName;
    }

    /**
     * Returns the target file name prefix.<p>
     *
     * @return the target file name prefix
     */
    public String getTargetFileNamePrefix() {

        return m_targetFileNamePrefix;
    }

    /**
     * Returns the targetFolderRootPath.<p>
     *
     * @return the targetFolderRootPath
     */
    public String getTargetFolderRootPath() {

        return m_targetFolderRootPath;
    }

    /**
     * Returns the upload type.<p>
     *
     * @return the upload type
     */
    public UploadType getUploadType() {

        return m_uploadType;
    }

    /**
     * Sets the dialog title.<p>
     *
     * @param dialogTitle the dialog title to set
     */
    public void setDialogTitle(String dialogTitle) {

        m_dialogTitle = dialogTitle;
    }

    /**
     * Sets the structure id of the file to replace.<p>
     *
     * @param replaceId the structure id of the file to replace
     */
    public void setReplaceId(CmsUUID replaceId) {

        m_replaceId = replaceId;
    }

    /**
     * Sets the target file name.<p>
     *
     * @param targetFileName the target file name to set
     */
    public void setTargetFileName(String targetFileName) {

        m_targetFileName = targetFileName;
    }

    /**
     * Sets the target file name prefix.<p>
     *
     * @param targetFileNamePrefix the target file name prefix to set
     */
    public void setTargetFileNamePrefix(String targetFileNamePrefix) {

        m_targetFileNamePrefix = targetFileNamePrefix;
    }

    /**
     * Sets the targetFolderRootPath.<p>
     *
     * @param targetFolderRootPath the targetFolderRootPath to set
     */
    public void setTargetFolderRootPath(String targetFolderRootPath) {

        m_targetFolderRootPath = targetFolderRootPath;
    }

    /**
     * Sets the upload type.<p>
     *
     * @param uploadType the upload type to set
     */
    public void setUploadType(UploadType uploadType) {

        m_uploadType = uploadType;
    }

}
