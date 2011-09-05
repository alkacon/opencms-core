/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.shared.CmsVfsEntryBean;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler class for the VFS tree tab.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsTabHandler extends A_CmsTabHandler {

    /**
     * Creates a new VFS tab handler.<p>
     * 
     * @param controller the gallery controller
     */
    public CmsVfsTabHandler(CmsGalleryController controller) {

        // TODO: Auto-generated constructor stub
        super(controller);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        m_controller.clearFolders();
    }

    /**
     * Gets the sub-folders of a given folder.<p>
     * 
     * @param path the path of the folder whose subfolders should be retrieved
     * @param callback the callback for processing the subfolders   
     */
    public void getSubFolders(String path, AsyncCallback<List<CmsVfsEntryBean>> callback) {

        m_controller.getSubFolders(path, callback);
    }

    /**
     * This method is called when a folder is selected or deselected in the VFS tab.<p>
     * 
     * @param folder the folder which is selected or deselected 
     * 
     * @param selected true if the folder has been selected, false if it has been deselected 
     */
    public void onSelectFolder(String folder, boolean selected) {

        if (selected) {
            m_controller.addFolder(folder);
        } else {
            m_controller.removeFolder(folder);
        }

    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        // do nothing
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String,java.lang.String)
     */
    @Override
    public void onSort(String sortParams, String filter) {

        // ignore filter, not available for this tab
        m_controller.sortResults(sortParams);
    }

}
