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

package org.opencms.ui.components;

import org.opencms.db.CmsResourceState;
import org.opencms.workplace.CmsWorkplace;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Displays the resource icon and state and lock info.<p>
 * Important: To avoid issues with click event propagation within tables, we are required to extent the Label component.
 */
public class CmsResourceIcon extends Label {

    /** The serial version id. */
    private static final long serialVersionUID = 5031544534869165777L;

    /**
     * Constructor.<p>
     *
     * @param icon the resource icon
     * @param lockState the lock state
     * @param state the resource state
     */
    @SuppressWarnings("incomplete-switch")
    public CmsResourceIcon(String icon, int lockState, CmsResourceState state) {
        setPrimaryStyleName(OpenCmsTheme.RESOURCE_ICON);
        setContentMode(ContentMode.HTML);
        String content = "<img src=\"" + CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + icon) + "\" />";

        String lockIcon = null;
        switch (lockState) {
            case 1:
                lockIcon = OpenCmsTheme.LOCK_OTHER;
                break;

            case 2:
                lockIcon = OpenCmsTheme.LOCK_SHARED;
                break;
            case 3:
                lockIcon = OpenCmsTheme.LOCK_USER;
                break;
        }
        if (lockIcon != null) {
            content += "<span class=\"" + lockIcon + "\"></span>";
        }

        if (state.isChanged()) {
            content += "<span class=\"" + OpenCmsTheme.STATE_CHANGED + "\"></span>";
        } else if (state.isNew()) {
            content += "<span class=\"" + OpenCmsTheme.STATE_NEW + "\"></span>";
        }
        setValue(content);
    }

}
