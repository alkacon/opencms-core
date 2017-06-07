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

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server-to-client Interface for the GWT dialog extension.<p>
 */
public interface I_CmsGwtDialogClientRpc extends ClientRpc {

    /**
     * Opens the 'external link' editor dialog for a pointer resource.<p>
     *
     * @param structureId the structure id of the resource
     */
    void editPointer(String structureId);

    /**
     * Tells the client to open the property editing dialog for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource, as a string
     * @param editName if true, makes the file name editable
     */
    void editProperties(String structureId, boolean editName);

    /**
     * Opens the categories dialog.<p>
     *
     * @param structureId the structure id of the resource to open the dialog for
     */
    void openCategoriesDialog(String structureId);

    /**
     * Opens the gallery dialog with the given JSON configuration.<p>
     *
     * @param galleryConfiguration the gallery configuration
     */
    void openGalleryDialog(String galleryConfiguration);

    /**
     * Opens the resource info dialog for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource, as a string
     */
    void openInfoDialog(String structureId);

    /**
     * Opens the lock report dialog.<p>
     *
     * @param dialogTitle the dialog title
     * @param structureId the structure id of the resource to open the dialog for
     */
    void openLockReport(String dialogTitle, String structureId);

    /**
     * Opens the publish dialog with the given publish data.<p>
     *
     * @param serializedPublishData the publish data, an instance of CmsPublishData serialized with the GWT serialization mechanism
     */
    void openPublishDialog(String serializedPublishData);

    /**
     * Opens the 'replace' dialog for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     */
    void openReplaceDialog(String structureId);

    /**
     * Shows the OpenCms about dialog.<p>
     */
    void showAbout();

    /**
     * Shows the preview dialog for resource with the given structure id and version string.<p>
     *
     * The version string format is defined by the toString() method of org.opencms.gwt.shared.CmsHistoryVersion.
     *
     * @param uuid the UUID
     * @param historyVersion the history version string
     */
    void showPreview(String uuid, String historyVersion);

    /**
     * Shows the user preferences.<p>
     */
    void showUserPreferences();

}
