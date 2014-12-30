/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client;

import com.google.gwt.user.client.Command;

/**
 * Handles updates on the HTML required due to entity data changes during inline editing.<p>
 */
public interface I_CmsInlineHtmlUpdateHandler {

    /**
     * Reinitializes the editing widget inside the form parent.<p>
     * 
     * @param formParent the form parent
     */
    void reinitWidgets(I_CmsInlineFormParent formParent);

    /**
     * Updates the HTML inside the form parent according to the currently edited entiy data.<p>
     * 
     * @param formParent the form parent
     * @param onSuccess the callback to execute on success
     */
    void updateHtml(I_CmsInlineFormParent formParent, Command onSuccess);
}
