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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * Resources for the custom cell table style.<p>
 */
public interface I_CmsCellTableResources extends CellTable.Resources {

    /** The cell table resource instance. */
    I_CmsCellTableResources INSTANCE = GWT.create(I_CmsCellTableResources.class);

    /**
     * @see com.google.gwt.user.cellview.client.CellTable.Resources#cellTableStyle()
     */
    @Source("org/opencms/gwt/client/ui/css/CellTable.css")
    I_CmsCellTableCss cellTableStyle();

    /**
     * Common CSS constants accessor.<p>
     *
     * @return the bundle of constants
     */
    I_CmsConstantsBundle constants();
}
