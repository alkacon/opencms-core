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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.Messages;

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
     * Constuctor.<p>
     * To be used in declarative layouts. Make sure to call initContent later on.<p>
     */
    public CmsResourceIcon() {
        setPrimaryStyleName(OpenCmsTheme.RESOURCE_ICON);
        setContentMode(ContentMode.HTML);
    }

    /**
     * Constructor.<p>
     *
     * @param resUtil the resource util
     * @param iconPath the resource icon
     * @param state the resource state
     */
    public CmsResourceIcon(CmsResourceUtil resUtil, String iconPath, CmsResourceState state) {
        this();
        initContent(resUtil, iconPath, state);
    }

    /**
     * Initializes the content.<p>
     *
     * @param resUtil the resource util
     * @param iconPath the resource icon
     * @param state the resource state
     */
    public void initContent(CmsResourceUtil resUtil, String iconPath, CmsResourceState state) {

        String content = "<img src=\"" + iconPath + "\" />";

        if (resUtil != null) {
            String lockIcon;
            switch (resUtil.getLockState()) {
                case 1:
                    lockIcon = OpenCmsTheme.LOCK_OTHER;
                    break;

                case 2:
                    lockIcon = OpenCmsTheme.LOCK_SHARED;
                    break;
                case 3:
                    lockIcon = OpenCmsTheme.LOCK_USER;
                    break;
                default:
                    lockIcon = null;
            }
            if (lockIcon != null) {
                content += getOverlaySpan(
                    lockIcon,
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_EXPLORER_LIST_ACTION_LOCK_NAME_2,
                        resUtil.getLockedByName(),
                        resUtil.getLockedInProjectName()));
            }
        }
        if (state != null) {

            String title = resUtil != null
            ? CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_USER_LAST_MODIFIED_0)
                + " "
                + resUtil.getUserLastModified()
            : null;
            if (state.isChanged() || state.isDeleted()) {
                content += getOverlaySpan(OpenCmsTheme.STATE_CHANGED, title);
            } else if (state.isNew()) {
                content += getOverlaySpan(OpenCmsTheme.STATE_NEW, title);
            }
        }
        if ((resUtil != null) && (resUtil.getLinkType() == 1)) {
            content += getOverlaySpan(OpenCmsTheme.SIBLING, null);
        }
        setValue(content);
    }

    /**
     * Generates an overlay icon span.<p>
     *
     * @param title the span title
     * @param cssClass the CSS class
     *
     * @return the span element string
     */
    private String getOverlaySpan(String cssClass, String title) {

        StringBuffer result = new StringBuffer();
        result.append("<span class=\"").append(cssClass).append("\"");
        if (title != null) {
            result.append(" title=\"").append(title).append("\"");
        }
        result.append("></span>");
        return result.toString();
    }
}
