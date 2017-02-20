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

package org.opencms.ui.apps.cacheadmin;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Class with static methods to create Labels for statistics.<p>
 */
class CmsCacheStatisticElements {

    /**
     * Creates Label used as entry for Statistic Panel.<p>
     *
     * @param title of entry
     * @param value  of entry
     * @param description of entry
     * @return created label
     */
    static Label getStatisticElement(String title, String value, String description) {

        Label ret = new Label();
        ret.setContentMode(ContentMode.HTML);
        ret.setValue("<p>" + title + ": " + value + "</p>");
        ret.setDescription(description);
        return ret;

    }

    /**
     * Creates Label used as title for Statistic Panel.<p>
     *
     * @param title of the panel
     * @return created label
     */
    static Label getTitelElement(String title) {

        Label ret = new Label();
        ret.setContentMode(ContentMode.HTML);
        ret.setValue("<p style='font-size:20px;'>" + title + "</p>");

        return ret;
    }
}
