/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.components;

import org.opencms.ui.shared.components.CmsUploadState;
import org.opencms.ui.shared.rpc.I_CmsUploadRpc;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

/**
 * The upload button.<p>
 */
public class CmsUploadButton extends Button implements I_CmsUploadRpc {

    /**
     * Upload listener interface.<p>
     */
    public interface I_UploadListener {

        /**
         * Called once the upload is finished.<p>
         *
         * @param uploadedFiles the uploaded files root paths
         */
        void onUploadFinished(List<String> uploadedFiles);
    }

    /** Serial version id. */
    private static final long serialVersionUID = -8591991683786743571L;

    /** The upoad listeners. */
    private List<I_UploadListener> m_uploadListener;

    /**
     * Constructor.<p>
     *
     * @param icon the button icon
     * @param targetFolderRootPath the target folder path
     */
    public CmsUploadButton(Resource icon, String targetFolderRootPath) {

        this(targetFolderRootPath);
        setIcon(icon);
    }

    /**
     * Constructor.<p>
     *
     * @param targetFolderRootPath the upload target folder root path
     */
    public CmsUploadButton(String targetFolderRootPath) {

        super();
        registerRpc(this);
        m_uploadListener = new ArrayList<I_UploadListener>();
        getState().setTargetFolderRootPath(targetFolderRootPath);
    }

    /**
     * Adds an upload listener.<p>
     *
     * @param listener the listener instance
     */
    public void addUploadListener(I_UploadListener listener) {

        m_uploadListener.add(listener);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsUploadRpc#onUploadFinished(java.util.List)
     */
    public void onUploadFinished(List<String> uploadedFiles) {

        for (I_UploadListener listener : m_uploadListener) {
            listener.onUploadFinished(uploadedFiles);
        }
    }

    /**
     * Removes the given upload listener.<p>
     *
     * @param listener the listener to remove
     */
    public void removeUploadListener(I_UploadListener listener) {

        m_uploadListener.remove(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
    }

    /**
     * Sets the upload target folder.<p>
     *
     * @param targetFolder the upload target
     */
    public void setTargetFolder(String targetFolder) {

        getState().setTargetFolderRootPath(targetFolder);
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    protected CmsUploadState getState() {

        return (CmsUploadState)super.getState();
    }
}
