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

package org.opencms.ui.components;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.Messages;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.PopupDateField;

/**
 * Convenience subclass of PopupDateField which comes preconfigured with a resolution and validation error message.<p>
 */
public class CmsDateField extends PopupDateField {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p<
     */
    public CmsDateField() {
        super();
        setResolution(Resolution.MINUTE);
        String parseError = Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_INVALID_DATE_FORMAT_0);
        setParseErrorMessage(parseError);
    }

}
