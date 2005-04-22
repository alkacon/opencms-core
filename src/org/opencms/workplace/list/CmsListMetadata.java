/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListMetadata.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
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

import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is class contains all the information for defining a whole html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsListMetadata {

    /** List of independent actions. */
    private List m_indepActions = new ArrayList();

    /** List of column definitions. */
    private List m_columnList = new ArrayList();

    /** Map for column definitions. */
    private Map m_columnMap = new HashMap();

    /** Associated list. */
    private CmsHtmlList m_list;

    /** Maximum number of items per page. */
    private int m_maxItemsPerPage = 20;

    /** List of multi actions. */
    private List m_multiActions = new ArrayList();

    /** Comment for <code>m_searchAction</code>. */
    private CmsSearchAction m_searchAction;

    /**
     * Adds a new column definition at the end.<p>
     * 
     * @param listColumn the column definition
     * 
     * @see List#add(Object)
     */
    //throws IllegalArgumentException if the column name is already present
    public void addColumn(CmsListColumnDefinition listColumn) {

        if (m_columnMap.containsKey(listColumn.getId())) {
            throw new IllegalArgumentException(Messages.get().key(Messages.ERR_LIST_COLUMN_EXISTS_1, listColumn));
        }
        m_columnList.add(listColumn);
        m_columnMap.put(listColumn.getId(), listColumn);
    }

    /**
     * Adds a new column definition at the given position.<p>
     * 
     * @param listColumn the column definition
     * @param position the position
     * 
     * @see List#add(int, Object)
     */
    //throws IllegalArgumentException if the column name is already present
    public void addColumn(CmsListColumnDefinition listColumn, int position) {

        if (m_columnMap.containsKey(listColumn.getId())) {
            throw new IllegalArgumentException(Messages.get().key(Messages.ERR_LIST_COLUMN_EXISTS_1, listColumn));
        }
        m_columnList.add(position, listColumn);
        m_columnMap.put(listColumn.getId(), listColumn);
    }

    /**
     * Adds an action applicable to more than one list item at once.<p>
     * 
     * It will be executed iterating over the selected list items.<p>
     * 
     * @param directAction the action
     */
    public void addDirectMultiAction(I_CmsListDirectAction directAction) {

        m_multiActions.add(directAction);
    }

    /**
     * Adds a list item independent action.<p>
     * 
     * @param action the action
     */
    public void addIndependentAction(I_CmsListAction action) {

        m_indepActions.add(action);
    }

    /**
     * Adds an action applicable to more than one list item at once.<p>
     * 
     * It will be executed with a list of <code>{@link CmsListItem}</code>s.<p> 
     *  
     * @param multiAction the action
     */
    public void addMultiAction(CmsListMultiAction multiAction) {

        m_multiActions.add(multiAction);
    }

    /**
     * Returns a column definition object for a given column name.<p>
     * 
     * @param columnName the column name
     * 
     * @return the column definition, or <code>null</code> if not present
     */
    public CmsListColumnDefinition getColumnDefinition(String columnName) {

        return (CmsListColumnDefinition)m_columnMap.get(columnName);
    }

    /**
     * Returns all columns definitions.<p>
     * 
     * @return a list of <code>{@link CmsListColumnDefinition}</code>s.
     */
    public List getListColumns() {

        return Collections.unmodifiableList(m_columnList);
    }

    /**
     * Returns the maximum number of items per page.<p>
     *
     * @return the maximum number of items per page
     */
    public int getMaxItemsPerPage() {

        return m_maxItemsPerPage;
    }

    /**
     * Returns the search action.<p>
     *
     * @return the search action
     */
    public CmsSearchAction getSearchAction() {

        return m_searchAction;
    }

    /**
     * Returns <code>true</code> if the list definition contains an action.<p>
     * 
     * @return <code>true</code> if the list definition contains an action
     */
    public boolean hasActions() {

        return !m_indepActions.isEmpty();
    }

    /**
     * Returns <code>true</code> if the list definition contains a multi action.<p>
     * 
     * @return <code>true</code> if the list definition contains a multi action
     */
    public boolean hasMultiActions() {

        return !m_multiActions.isEmpty();
    }

    /**
     * Returns <code>true</code> if any column definition contains a single action.<p>
     * 
     * @return <code>true</code> if any column definition contains a single action
     */
    public boolean hasSingleActions() {

        Iterator itCols = m_columnList.iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (col.getDefaultAction() != null || !col.getDirectActions().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the html code for the action bar.<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    public String htmlActionBar(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='misc'>\n");
        html.append("\t<div>\n");
        Iterator itActions = m_indepActions.iterator();
        while (itActions.hasNext()) {
            I_CmsListAction indepAction = (I_CmsListAction)itActions.next();
            html.append("\t\t");
            html.append(indepAction.buttonHtml(wp));
            html.append("\n");
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Generates the hml code for an empty table.<p>
     * 
     * @return html code
     */
    public String htmlEmptyTable() {

        StringBuffer html = new StringBuffer(512);
        html.append("<tr class='oddrowbg'>\n");
        html.append("\t<td align='center' colspan='");
        html.append(m_columnList.size()+(m_multiActions.isEmpty()?0:1));
        html.append("'>\n");
        html.append("${key.");
        html.append(Messages.GUI_LIST_EMPTY_0);
        html.append("}");
        html.append("\t</td>\n");
        html.append("</tr>\n");
        return html.toString();
    }

    /**
     * Returns the html code for the header of the list.<p>
     * 
     * @return html code
     */
    public String htmlHeader() {

        StringBuffer html = new StringBuffer(1024);
        html.append("<tr>\n");
        Iterator itCols = m_columnList.iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            html.append(col.htmlHeader(m_list));
        }
        if (!m_multiActions.isEmpty()) {
            html.append("\t<th width='0' class='select'>\n");
            html.append("\t\t<input type='checkbox' class='checkbox' name='");
            html.append(m_list.getId());
            html.append("ListSelectAll' value='true' onClick='");
            html.append(m_list.getId());
            html.append("ListSelect()'\n>");
            html.append("\t</th>\n");
        }
        html.append("</tr>\n");
        return html.toString();
    }

    /**
     * Returns the html code for a list item.<p>
     * 
     * @param item the list item to render
     * @param wp the workplace context
     * @param odd if the position is odd or even
     * 
     * @return html code
     */
    public String htmlItem(CmsListItem item, CmsWorkplace wp, boolean odd) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<tr class='");
        html.append(odd ? "oddrowbg" : "evenrowbg");
        html.append("'>\n");
        Iterator itCols = m_columnList.iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            html.append(col.htmlCell(item, wp));
            html.append("\n");
        }
        if (!m_multiActions.isEmpty()) {
            html.append("\t<td class='select' align='center' >\n");
            html.append("\t\t<input type='checkbox' class='checkbox' name='");
            html.append(m_list.getId());
            html.append("MultiAction' value='");
            html.append(item.getId());
            html.append("'>\n");
            html.append("\t</td>\n");
        }
        html.append("</tr>\n");
        return html.toString();
    }

    /**
     * Returns the html code for the multi action bar.<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    public String htmlMultiActionBar(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='misc'>\n");
        html.append("\t<div>\n");
        Iterator itActions = m_multiActions.iterator();
        while (itActions.hasNext()) {
            I_CmsListAction multiAction = (I_CmsListAction)itActions.next();
            if (multiAction instanceof CmsListDirectAction) {
                ((CmsListDirectAction)multiAction).setItem(null);
            }
            html.append(multiAction.buttonHtml(wp));
            html.append("\n");
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Generates the html code for the search bar.<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    public String htmlSearchBar(CmsWorkplace wp) {

        if (!isSearchable()) {
            return "";
        }
        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='main'>\n");
        html.append("\t<div>\n");
        html.append("\t\t<input type='text' name='");
        html.append(m_list.getId());
        html.append("Filter' id='searchInput' value='' size='20' maxlength='245'>\n");
        html.append(m_searchAction.buttonHtml(wp));
        I_CmsListAction showAllAction = m_searchAction.getShowAllAction();
        if (showAllAction != null) {
            html.append(showAllAction.buttonHtml(wp));
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Returns <code>true</code> if the list is searchable.<p>
     * 
     * @return  <code>true</code> if the list is searchable
     */
    public boolean isSearchable() {

        return m_searchAction != null; // && getColumnDefinition(m_searchByColumnName).isVisible()
    }

    /**
     * Returns <code>true</code> if any column is sorteable.<p>
     * 
     * @return <code>true</code> if any column is sorteable
     */
    public boolean isSorteable() {

        Iterator itCols = m_columnList.iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (col.isSorteable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method asociates this metadata object to a list.<p>
     * 
     * Once this object is asociated to a list, it can not be asociated to another list, 
     * this method can be called only once.<p> 
     * 
     * @param list the list to asociate with
     */
    //throws IllegalArgumentException if the column name is already present
    public void setList(CmsHtmlList list) {

        if (m_list != null) {
            throw new IllegalStateException(Messages.get().key(Messages.ERR_LIST_METADATA_EXISTS_0));
        }
        m_list = list;
    }

    /**
     * Sets the maximum number of items per page.<p>
     *
     * @param maxItemsPerPage the maximum number of items per page to set
     */
    public void setMaxItemsPerPage(int maxItemsPerPage) {

        this.m_maxItemsPerPage = maxItemsPerPage;
    }

    /**
     * Sets the search action.<p>
     *
     * @param searchAction the search action to set
     */
    public void setSearchAction(CmsSearchAction searchAction) {

        m_searchAction = searchAction;
    }

}