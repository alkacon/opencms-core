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

package org.opencms.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Increases the visibility of some key methods of a {@link ResourceBundle}.<p>
 *
 * This interface is required because the methods {@link #setParent(ResourceBundle)} and
 * {@link #setLocale(Locale)} are not visible in the standard implementation. However,
 * access to these methods is required by the {@link org.opencms.i18n.CmsResourceBundleLoader}.<p>
 *
 * @since 8.0.1
 *
 * @see org.opencms.i18n.CmsResourceBundleLoader
 */
public interface I_CmsResourceBundle {

    /**
     * Creates a clone of the resource bundle.<p>
     *
     * (This may not actually clone the resource bundle if it is immutable).
     *
     * @return a clone of the resource bundle
     */
    I_CmsResourceBundle getClone();

    /**
     * Sets the locale used for this resource bundle.<p>
     *
     * @param l the locale to set
     */
    void setLocale(Locale l);

    /**
     * Sets the parent bundle.<p>
     *
     * @param parent the parent bundle to set
     */
    void setParent(ResourceBundle parent);
}