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

package org.opencms.gwt.client.ui.contenteditor;

import org.opencms.util.CmsUUID;

/**
 * Handler for the XML content editor.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsContentEditorHandler {

    /**
     * Executed by the XML content editor dialog on close.<p>
     *
     * @param sitePath the sitepath of the edited resource
     * @param structureId the structure id of the edited resource
     * @param isNew <code>true</code> if the resource was newly created
     * @param hasChangedSettings <code>true</code> in case container page element settings where changed during edit
     * @param usedPublishDialog true if the editor was closed by using the publish button
     */
    void onClose(
        String sitePath,
        CmsUUID structureId,
        boolean isNew,
        boolean hasChangedSettings,
        boolean usedPublishDialog);
}
