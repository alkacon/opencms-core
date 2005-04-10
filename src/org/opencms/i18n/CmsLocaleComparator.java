/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsLocaleComparator.java,v $
 * Date   : $Date: 2005/04/10 11:00:14 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import java.util.Comparator;
import java.util.Locale;

/**
 * Simple comparator implementation for locales, that compares the String value of the locales.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 */
public final class CmsLocaleComparator implements Comparator {

    /** Static locale comparator. */
    private static final Comparator LOCALE_COMPARATOR = new CmsLocaleComparator();

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
    public static Comparator getComparator() {

        return LOCALE_COMPARATOR;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {

        if ((o1 instanceof Locale) && (o2 instanceof Locale)) {
            return o1.toString().compareTo(o2.toString());
        }
        return 0;
    }
}