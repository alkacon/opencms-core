/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListItemActionIconComparator.java,v $
 * Date   : $Date: 2005/09/16 13:11:12 $
 * Version: $Revision: 1.6.2.1 $
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.Iterator;
import java.util.Locale;

/**
 * Comparator for column sorting by first direct action icon names.<p>
 * 
 * If the list items column definition has at least one direct action, 
 * the icon of the first visible action is used for sorting 
 * (using the <code>{@link I_CmsListDirectAction#setItem(CmsListItem)}</code> method); 
 * if not, the <code>{@link Comparable}</code> interface is used.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.6.2.1 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.workplace.list.CmsListColumnDefinition
 */
public class CmsListItemActionIconComparator implements I_CmsListItemComparator {

    /**
     * Default Constructor.<p>
     */
    public CmsListItemActionIconComparator() {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
     */
    public Comparator getComparator(final String columnId, final Locale locale) {

        return new Comparator() {

            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare(Object o1, Object o2) {

                CmsListItem li1 = (CmsListItem)o1;                
                CmsListItem li2 = (CmsListItem)o2;
                CmsListColumnDefinition col = li1.getMetadata().getColumnDefinition(columnId);
                if (col.getDirectActions().size() > 0) {                   
                    String i1 = null;
                    String i2 = null;
                    Iterator it = col.getDirectActions().iterator();
                    while (it.hasNext()) {
                        I_CmsListDirectAction action = (I_CmsListDirectAction)it.next();                    
                        CmsListItem tmp = action.getItem();
                        action.setItem(li1);
                        if (action.isVisible()) {
                            i1 = action.getIconPath();
                        }
                        action.setItem(li2);
                        if (action.isVisible()) {
                           i2 = action.getIconPath();
                        }
                        action.setItem(tmp);
                    }
                    if (i1 != null) {
                        if (i2 == null) {
                            return 1;
                        }
                        return i1.compareTo(i2);
                    } else if (i2 != null) {
                        return -1;
                    }
                    return 0;
                } else {
                    Comparable c1 = (Comparable)((CmsListItem)o1).get(columnId);
                    Comparable c2 = (Comparable)((CmsListItem)o2).get(columnId);
                    if (c1 != null) {
                        if (c2 == null) {
                            return 1;
                        }
                        return c1.compareTo(c2);
                    } else if (c2 != null) {
                        return -1;
                    }
                    return 0;
                }
            }
        };
    }
}