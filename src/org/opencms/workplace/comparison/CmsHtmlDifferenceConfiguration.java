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

package org.opencms.workplace.comparison;

import com.alkacon.diff.DiffConfiguration;
import com.alkacon.diff.HtmlDiffConfiguration;

import java.util.Locale;

/**
 * OpenCms Html Diff operation configuration class.<p>
 *
 * @since 6.0.2
 */
public class CmsHtmlDifferenceConfiguration extends HtmlDiffConfiguration {

    /**
     * Creates a new configuration object.<p>
     *
     * @param lines the lines to show before skipping
     * @param locale the locale to use
     */
    public CmsHtmlDifferenceConfiguration(int lines, Locale locale) {

        super(new DiffConfiguration(lines, Messages.get().getBundleName(), Messages.GUI_DIFF_SKIP_LINES_1, locale));
        setDivStyleNames("df-unc", "df-add", "df-rem", "df-skp");
        setSpanStyleNames("df-unc", "df-add", "df-rem");
    }
}