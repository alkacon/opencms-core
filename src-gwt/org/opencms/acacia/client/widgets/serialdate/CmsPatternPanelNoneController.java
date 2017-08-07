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

package org.opencms.acacia.client.widgets.serialdate;

import com.google.gwt.user.client.ui.Widget;

/** Dummy controller for the pattern of a single event. */
public class CmsPatternPanelNoneController implements I_CmsSerialDatePatternController {

    /** Dummy pattern view. */
    private static final I_CmsSerialDatePatternView DUMMY_VIEW = new I_CmsSerialDatePatternView() {

        public Widget asWidget() {

            return null;
        }

        public void onValueChange() {
            // Do nothing
        }
    };

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDatePatternController#getView()
     */
    public I_CmsSerialDatePatternView getView() {

        return DUMMY_VIEW;
    }

}
