/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.ade.contenteditor.shared.CmsComplexWidgetData;
import org.opencms.file.CmsObject;

/**
 * Dummy widget class that causes the Acacia editor to render a nested content normally without a special widget.<p>
 * This could be used to override a default widget defined for a nested content in its XSD on a per-attribute basis.
 */
public class CmsDefaultComplexWidget implements I_CmsComplexWidget {

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#configure(java.lang.String)
     */
    public I_CmsComplexWidget configure(String configuration) {

        return this;
    }

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#getName()
     */
    public String getName() {

        return "default";
    }

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#getWidgetData(org.opencms.file.CmsObject)
     */
    public CmsComplexWidgetData getWidgetData(CmsObject cms) {

        CmsComplexWidgetData result = new CmsComplexWidgetData("default", "", null);
        return result;
    }

}
