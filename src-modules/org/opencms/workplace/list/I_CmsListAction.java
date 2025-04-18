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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.tools.I_CmsHtmlIconButton;

/**
 * Interface for list actions.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsListAction extends I_CmsHtmlIconButton {

    /**
     * The same using {@link #getWp()}.<p>
     *
     * @return the buttons html code
     *
     * @see I_CmsHtmlIconButton#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    String buttonHtml();

    /**
     * Returns an optional confirmation message for the action.<p>
     *
     * It will be escaped for usage in java script code.<p>
     *
     * @return confirmation message
     */
    CmsMessageContainer getConfirmationMessage();

    /**
     * Returns the id of the associated list.<p>
     *
     * @return the id of the associated list
     */
    String getListId();

    /**
     * Returns the related workplace dialog object, to be able to access dynamic data.<p>
     *
     * @return the related workplace dialog object
     */
    A_CmsListDialog getWp();

    /**
     * Sets the confirmation message.<p>
     *
     * @param confirmationMsg the confirmation message to set
     */
    void setConfirmationMessage(CmsMessageContainer confirmationMsg);

    /**
     * Sets the list id.<p>
     *
     * @param listId the id of the list to set
     */
    void setListId(String listId);

    /**
     * Sets the workplace dialog object.<p>
     *
     * @param wp the workplace dialog object
     */
    void setWp(A_CmsListDialog wp);
}