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

package org.opencms.ui.components.extensions;

import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.shared.components.CmsUploadAreaState;
import org.opencms.ui.shared.rpc.I_CmsUploadRpc;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;

/**
 * Extends the given component to be an upload drop area.<p>
 */
public class CmsUploadAreaExtension extends AbstractExtension implements I_CmsUploadRpc {

    /** The serial version id. */
    private static final long serialVersionUID = 3978957151754705873L;

    /** The registered window close listeners. */
    private List<I_UploadListener> m_listeners;

    /**
     * Constructor.<p>
     *
     * @param component the component to extend
     */
    public CmsUploadAreaExtension(AbstractComponent component) {
        extend(component);
        registerRpc(this);
        m_listeners = new ArrayList<I_UploadListener>();
    }

    /**
     * Adds a window close listener.<p>
     *
     * @param listener the listener to add
     */
    public void addUploadListener(I_UploadListener listener) {

        m_listeners.add(listener);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsUploadRpc#onUploadFinished(java.util.List)
     */
    public void onUploadFinished(List<String> files) {

        for (I_UploadListener listener : m_listeners) {
            listener.onUploadFinished(files);
        }
    }

    /**
     * Removes the given window close listener.<p>
     *
     * @param listener the listener to remove
     */
    public void removeUploadListener(I_UploadListener listener) {

        m_listeners.remove(listener);
    }

    /**
     * Sets the upload target folder.<p>
     *
     * @param targetFolder the folder root path
     */
    public void setTargetFolder(String targetFolder) {

        getState().setTargetFolderRootPath(targetFolder);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsUploadAreaState getState() {

        return (CmsUploadAreaState)super.getState();
    }
}
