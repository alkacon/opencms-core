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

package org.opencms.widgets;

import org.opencms.ade.contenteditor.shared.CmsComplexWidgetData;
import org.opencms.file.CmsObject;

/**
 * Interface for complex widgets which are used in the Acacia editor to render whole nested contents.<p>
 */
public interface I_CmsComplexWidget {

    /**
     * Returns a copy of this widget which has been configured with the given configuration string.<p>
     *
     * @param configuration the configuration string
     *
     * @return the configured copy of the widget
     */
    I_CmsComplexWidget configure(String configuration);

    /**
     * Gets the name of the complex widget.<p>
     *
     * The string returned by this function should be a valid Javascript identifier.
     *
     * @return the name of the complex widget
     */
    String getName();

    /**
     * Gets the data needed for the editor to render the complex widget.<p>
     *
     * @param cms The CMS object to use for VFS operations
     *
     * @return the data for the complex widget
     */
    CmsComplexWidgetData getWidgetData(CmsObject cms);
}
