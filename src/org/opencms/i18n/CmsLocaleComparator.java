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

package org.opencms.i18n;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * Simple comparator implementation for locales, that compares the String value of the locales.<p>
 *
 * @since 6.0.0
 */
public final class CmsLocaleComparator implements Serializable, Comparator<Locale> {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -690619562147670465L;

    /** Static locale comparator. */
    private static final Comparator<Locale> LOCALE_COMPARATOR = new CmsLocaleComparator();

    /**
     * Hides the public constructor.<p>
     */
    private CmsLocaleComparator() {

        // noop
    }

    /**
     * Returns a static instance of the locale comparator.<p>
     *
     * @return a static instance of the locale comparator
     */
    public static Comparator<Locale> getComparator() {

        return LOCALE_COMPARATOR;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Locale o1, Locale o2) {

        return o1.toString().compareTo(o2.toString());
    }
}