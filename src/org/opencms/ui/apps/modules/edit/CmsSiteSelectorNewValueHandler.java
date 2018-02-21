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

package org.opencms.ui.apps.modules.edit;

import org.opencms.ui.components.CmsAutoItemCreatingComboBox;
import org.opencms.util.CmsFileUtil;

import java.util.Arrays;

import com.vaadin.v7.data.Container;

/**
 * Helper class used when new options are automatically added to a site selector combo box.<p>
 */
public class CmsSiteSelectorNewValueHandler implements CmsAutoItemCreatingComboBox.I_NewValueHandler {

    /** The property containing the caption. */
    private String m_captionPropertyId;

    /**
     * Creates a new instance.<p>
     *
     * @param captionPropertyId the item property containing the caption
     */
    public CmsSiteSelectorNewValueHandler(String captionPropertyId) {

        m_captionPropertyId = captionPropertyId;
    }

    /**
     * @see org.opencms.ui.components.CmsAutoItemCreatingComboBox.I_NewValueHandler#ensureItem(com.vaadin.v7.data.Container, java.lang.Object)
     */
    public Object ensureItem(Container cnt, Object id) {

        if (id == null) {
            return null;
        }
        String idStr = (String)id;
        for (String path : Arrays.asList(idStr, CmsFileUtil.toggleTrailingSeparator(idStr))) {
            if (cnt.containsId(path)) {
                return path;
            }
        }
        idStr = CmsFileUtil.addTrailingSeparator(idStr);
        cnt.addItem(idStr).getItemProperty(m_captionPropertyId).setValue(idStr);
        return idStr;
    }
}
