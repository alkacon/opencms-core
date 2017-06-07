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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleGroup;

import java.util.Locale;

/**
 * Provides information about the current state of the locale comparison editor.<p>
 */
public interface I_CmsLocaleCompareContext {

    /**
     * Gets the comparison locale.<p>
     *
     * @return the comparison locale
     */
    Locale getComparisonLocale();

    /**
     * Gets the locale group of the root resource.
     *
     * @return the locale group of the root resource
     */
    CmsLocaleGroup getLocaleGroup();

    /**
     * Gets the root resource.<p>
     *
     * @return the root resource
     */
    CmsResource getRoot();

    /**
     * Gets the root locale.<p>
     *
     * @return the root locale
     */
    Locale getRootLocale();

    /**
     * Refreshes everything.
     */
    void refreshAll();

}
