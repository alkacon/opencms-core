/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListColumnDefinition.java,v $
 * Date   : $Date: 2005/05/18 13:19:27 $
 * Version: $Revision: 1.8 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Html list column definition.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.8 $
 * @since 5.7.3
 */
public class CmsListColumnDefinition {

    /** List of actions. */
    private List m_actionList = new ArrayList();

    /** Column alignment. */
    private CmsListColumnAlignEnum m_align;

    /** Comparator for sorting. */
    private I_CmsListItemComparator m_comparator = null;

    /** Default action. */
    private CmsListDefaultAction m_defaultAction;

    /** Unique id. */
    private String m_id;

    /** Display name. */
    private CmsMessageContainer m_name;

    /** Visible Flag. */
    private boolean m_visible = true;

    /** Column width. */
    private String m_width;

    /** the data formatter. */
    private I_CmsListFormatter m_formatter;

    /**
     * Default Constructor.<p>
     *  
     * @param id the id
     */
    public CmsListColumnDefinition(String id) {

        m_id = id;
    }

    /**
     * Full Constructor.<p>
     *  
     * @param id the id
     * @param name the name
     * @param width the width
     * @param align the alignment
     */
    public CmsListColumnDefinition(String id, CmsMessageContainer name, String width, CmsListColumnAlignEnum align) {

        this(id);
        setName(name);
        setWidth(width);
        setAlign(align);
    }

    /**
     * Returns the data formatter.<p>
     *
     * @return the data formatter
     */
    public I_CmsListFormatter getFormatter() {

        return m_formatter;
    }

    /**
     * Sets the data formatter.<p>
     *
     * @param formatter the data formatter to set
     */
    public void setFormatter(I_CmsListFormatter formatter) {

        m_formatter = formatter;
    }

    /**
     * Adds a new action to the column.<p>
     * 
     * @param listAction the action to add
     */
    public void addDirectAction(I_CmsListDirectAction listAction) {

        m_actionList.add(listAction);
    }

    /**
     * Returns the align.<p>
     *
     * @return the align
     */
    public CmsListColumnAlignEnum getAlign() {

        return m_align;
    }

    /**
     * Returns the comparator, used for sorting.<p>
     *
     * if no comparator was set, the default list item comparator is used.<p>
     * 
     * @return the comparator
     * 
     * @see CmsListItemDefaultComparator
     */
    public I_CmsListItemComparator getListItemComparator() {

        return m_comparator;
    }

    /**
     * Returns the default Action.<p>
     *
     * @return the default Action
     */
    public I_CmsListDirectAction getDefaultAction() {

        return m_defaultAction;
    }

    /**
     * Returns all direct actions.<p>
     * 
     * @return a list of <code>{@link I_CmsListDirectAction}</code>s.
     */
    public List getDirectActions() {

        return Collections.unmodifiableList(m_actionList);
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public CmsMessageContainer getName() {

        return m_name;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public String getWidth() {

        return m_width;
    }

    /**
     * returns the html for a cell.<p>
     * 
     * @param item the item to render the cell for
     * @param wp the workplace context
     * 
     * @return html code
     */
    public String htmlCell(CmsListItem item, CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        Iterator itActions = m_actionList.iterator();
        while (itActions.hasNext()) {
            I_CmsListDirectAction action = (I_CmsListDirectAction)itActions.next();
            action.setItem(item);
            html.append(action.buttonHtml(wp));
        }
        if (m_defaultAction != null) {
            m_defaultAction.setItem(item);
            html.append(m_defaultAction.buttonHtml(wp));
        } else {
            if (m_formatter == null) {
                // unformatted output
                if (item.get(m_id) != null) {
                    // null values are not showed by default
                    html.append(item.get(m_id).toString());
                }
            } else {
                // formatted output
                html.append(m_formatter.format(item.get(m_id), wp.getLocale()));
            }
        }
        html.append("\n");
        return html.toString();
    }

    /**
     * Returns the html code for a column header.<p>
     * 
     * @param list the list to generate the header code for
     * @param locale the locale for localized messages
     * 
     * @return html code
     */
    public String htmlHeader(CmsHtmlList list, Locale locale) {

        if (!isVisible()) {
            return "";
        }
        
        String listId = list.getId();
        String sortedCol = list.getSortedColumn();
        CmsListOrderEnum order = list.getCurrentSortOrder();
        
        StringBuffer html = new StringBuffer(512);
        html.append("<th");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth())) {
            html.append(" width='");
            html.append(getWidth());
            html.append("'");
        }
        html.append(">\n");

        boolean isSorted = getId().equals(sortedCol);
        CmsListOrderEnum nextOrder = CmsListOrderEnum.ORDER_ASCENDING;
        if (isSorted && order == CmsListOrderEnum.ORDER_ASCENDING) {
            nextOrder = CmsListOrderEnum.ORDER_DESCENDING;
        }
        // button
        String id = listId + getId() + "Sort";
        String onClic = listId + "ListSort('" + getId() + "');";
        String helpText = null;
        if (isSorteable()) {
            if (nextOrder.equals(CmsListOrderEnum.ORDER_ASCENDING)) {
                helpText = Messages.get().key(
                    locale,
                    Messages.GUI_LIST_COLUMN_ASC_SORT_1,
                    new Object[] {getName().key(locale)});
            } else {
                helpText = Messages.get().key(
                    locale,
                    Messages.GUI_LIST_COLUMN_DESC_SORT_1,
                    new Object[] {getName().key(locale)});
            }
        } else {
            helpText = Messages.get().key(
                locale,
                Messages.GUI_LIST_COLUMN_NO_SORT_1,
                new Object[] {getName().key(locale)});
        }
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            id,
            getName().key(locale),
            helpText,
            isSorteable(),
            null,
            onClic));
        // sort order marker
        if (isSorted) {
            if (nextOrder == CmsListOrderEnum.ORDER_ASCENDING) {
                html.append("\t<img src='");
                html.append(CmsWorkplace.getSkinUri());
                html.append("list/arrow_up.gif");
                html.append("'>\n");
            } else {
                html.append("\t<img src='");
                html.append(CmsWorkplace.getSkinUri());
                html.append("list/arrow_down.gif");
                html.append("'>\n");
            }
        }
        html.append("</th>\n");
        return html.toString();
    }

    /**
     * Returns the sorteable.<p>
     *
     * @return the sorteable
     */
    public boolean isSorteable() {

        return getListItemComparator()!=null;
    }

    /**
     * Returns the visible.<p>
     *
     * @return the visible
     */
    public boolean isVisible() {

        return m_visible;
    }

    /**
     * Sets the align.<p>
     *
     * @param align the align to set
     */
    public void setAlign(CmsListColumnAlignEnum align) {

        m_align = align;
    }

    /**
     * Sets the comparator, used for sorting.<p>
     *
     * @param comparator the comparator to set
     */
    public void setListItemComparator(I_CmsListItemComparator comparator) {

        m_comparator = comparator;
    }

    /**
     * Sets the default Action.<p>
     *
     * @param defaultAction the default Action to set
     */
    public void setDefaultAction(CmsListDefaultAction defaultAction) {

        m_defaultAction = defaultAction;
        // set the column id
        m_defaultAction.setColumn(getId());
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(CmsMessageContainer name) {

        m_name = name;
    }

    /**
     * Sets the visible.<p>
     *
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {

        m_visible = visible;
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(String width) {

        m_width = width;
    }

}

