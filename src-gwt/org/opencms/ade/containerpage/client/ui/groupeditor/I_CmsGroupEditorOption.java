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

package org.opencms.ade.containerpage.client.ui.groupeditor;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Common interface for option buttons within the inheritance container editor.<p>
 *
 * @since 8.5.0
 */
public interface I_CmsGroupEditorOption {

    /**
     * Checks if this button should be visible according to the elements state.<p>
     *
     * @return <code>true</code> if the button should be visible
     */
    boolean checkVisibility();

    /**
     * Executed on button click.<p>
     *
     * @param event the click event
     */
    void onClick(ClickEvent event);
}
