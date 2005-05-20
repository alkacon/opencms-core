/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListSearchAction.java,v $
 * Date   : $Date: 2005/05/20 15:11:42 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation for a seach action in an html list.<p>
 * 
 * It allows to search in several columns, including item details.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsListSearchAction extends A_CmsListSearchAction implements I_CmsSearchAction {

    /** Ids of Columns to search into. */
    private final List m_columns = new ArrayList();

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param column the column to search into
     */
    public CmsListSearchAction(String listId, CmsListColumnDefinition column) {

        super(listId);
        useDefaultShowAllAction();
        m_columns.add(column);
    }
    
    /**
     * Adds a column to search into.<p>
     * 
     * @param column the additional column to search into
     */
    public void addColumn(CmsListColumnDefinition column) {

        m_columns.add(column);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListSearchAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (getHelpText() == C_EMPTY_MESSAGE) {
            String columns = "";
            Iterator it = m_columns.iterator();
            while (it.hasNext()) {
                CmsListColumnDefinition col = (CmsListColumnDefinition)it.next();
                columns += "${key." + col.getName().getKey() + "}";
                if (it.hasNext()) {
                    columns += ", ";
                }
            }
            setHelpText(new CmsMessageContainer(
                Messages.get(),
                Messages.GUI_LIST_ACTION_SEARCH_HELP_1,
                new Object[] {columns}));
        }
        return super.buttonHtml(wp);
    }


    /**
     * @see org.opencms.workplace.list.I_CmsSearchAction#filter(java.util.List, java.lang.String)
     */
    public List filter(List items, String filter) {
        
        List res = new ArrayList();
        Iterator itItems = items.iterator();
        while (itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            if (res.contains(item)) {
                continue;
            }
            Iterator itCols = m_columns.iterator();
            while (itCols.hasNext()) {
                CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
                if (item.get(col.getId()).toString().indexOf(filter) > -1) {
                    res.add(item);
                    break;
                }
            }
        }
        return res;
    }
}