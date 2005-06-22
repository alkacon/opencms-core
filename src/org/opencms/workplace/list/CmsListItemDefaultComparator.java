/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListItemDefaultComparator.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.list;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Default comparator for case sensitive column sorting with string localization.<p>
 * 
 * If both list items column values are Strings then a localized collector is used for sorting; 
 * if not, the <code>{@link Comparable}</code> interface is used.<p>
 * 
 * @author Michael Moossen  
 * @version $Revision: 1.4 $
 * @since 5.7.3
 * 
 * @see org.opencms.workplace.list.CmsListColumnDefinition
 */
public class CmsListItemDefaultComparator implements I_CmsListItemComparator {

    /**
     * Default Constructor.<p>
     */
    public CmsListItemDefaultComparator() {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
     */
    public Comparator getComparator(final String columnId, final Locale locale) {

        final Collator collator = Collator.getInstance(locale);
        return new Comparator() {

            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare(Object o1, Object o2) {

                Comparable c1 = (Comparable)((CmsListItem)o1).get(columnId);
                Comparable c2 = (Comparable)((CmsListItem)o2).get(columnId);
                if (c1 instanceof String && c2 instanceof String) {
                    return collator.compare(c1, c2);
                } else if (c1 != null) {
                    if (c2 == null) {
                        return 1;
                    }
                    return c1.compareTo(c2);
                } else if (c2 != null) {
                    return -1;
                }
                return 0;
            }
        };
    }
}