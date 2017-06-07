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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Default implementation of a list item selection action.<p>
 *
 * @since 6.0.0
 */
public class CmsListItemSelectionAction extends CmsListDirectAction {

    /** The id of the related multi action. */
    private final String m_multiActionId;

    /** The id of the selected item. */
    private String m_selectedItemId;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param multiActionId the id of the related multi Action
     */
    public CmsListItemSelectionAction(String id, String multiActionId) {

        super(id);
        m_multiActionId = multiActionId;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        String html = "<input type='radio' value='" + getItem().getId() + "' name='" + getListId() + getId() + "'";
        if (!isEnabled()) {
            html += " disabled";
        }
        if (getItem().getId().equals(getSelectedItemId())) {
            html += " checked";
        }
        html += ">\n";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            html,
            getHelpText().key(wp.getLocale()),
            true,
            null,
            null,
            null);
    }

    /**
     * Returns the id of the related multi Action.<p>
     *
     * @return the id of the related multi Action
     */
    public String getMultiActionId() {

        return m_multiActionId;
    }

    /**
     * Returns the selected item Id.<p>
     *
     * @return the selected item Id
     */
    public String getSelectedItemId() {

        return m_selectedItemId;
    }

    /**
     * Sets the selected item Id.<p>
     *
     * @param selectedItemId the selected item Id to set
     */
    public void setSelectedItemId(String selectedItemId) {

        m_selectedItemId = selectedItemId;
    }
}
