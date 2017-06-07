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

/**
 * Interface for list direct action, that is an action that may be applied
 * directly on a list item.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsListDirectAction extends I_CmsListAction {

    /**
     * Generates a confirmation text div tag, to use in lists where all items use
     * the same confirmation text.<p>
     *
     * @return html code
     */
    String confirmationTextHtml();

    /**
     * Returns the id of the column to use as parameter for the helptext and confirmation message.<p>
     *
     * @return the id of the column to use
     */
    String getColumnForTexts();

    /**
     * Returns the current item.<p>
     *
     * @return the current item
     */
    CmsListItem getItem();

    /**
     * Generates a help text div tag, to use in lists where all items use the same help text.<p>
     *
     * @return html code
     */
    String helpTextHtml();

    /**
     * Sets the id of the column to use as parameter for the helptext and confirmation message.<p>
     *
     * @param columnId the id of the column to use
     */
    void setColumnForTexts(String columnId);

    /**
     * Sets the current item, should be called before the <code>{@link #buttonHtml(org.opencms.workplace.CmsWorkplace)}</code> method.<p>
     *
     * @param item the item
     */
    void setItem(CmsListItem item);

}