/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/I_CmsListItemComparator.java,v $
 * Date   : $Date: 2005/06/23 10:47:20 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import java.util.Comparator;
import java.util.Locale;

/**
 * A list item comparator can be set at a column definition to set the sorting method for that column.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsListItemComparator {

    /**
     * Returns a new comparator for comparing list items by the given column,
     * and using the given locale.<p>
     * 
     * @param columnId the id of the column to sort by
     * @param locale the current used locale
     * 
     * @return a new comparator
     */
    Comparator getComparator(final String columnId, final Locale locale);
}