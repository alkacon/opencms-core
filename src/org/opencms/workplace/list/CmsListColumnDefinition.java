/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListColumnDefinition.java,v $
 * Date   : $Date: 2005/09/16 13:11:12 $
 * Version: $Revision: 1.21.2.1 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Html list column definition.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.21.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListColumnDefinition {

    /** Standard list button location. */
    public static final String ICON_DOWN = "list/arrow_down.png";

    /** Standard list button location. */
    public static final String ICON_UP = "list/arrow_up.png";

    /** Column alignment. */
    private CmsListColumnAlignEnum m_align = CmsListColumnAlignEnum.ALIGN_LEFT;

    /** Comparator for sorting. */
    private I_CmsListItemComparator m_comparator = new CmsListItemDefaultComparator();

    /** Default action. */
    private List m_defaultActions = new ArrayList();

    /** List of actions. */
    private List m_directActions = new ArrayList();

    /** Data formatter. */
    private I_CmsListFormatter m_formatter;

    /** Customized help text. */
    private CmsMessageContainer m_helpText;

    /** Unique id. */
    private final String m_id;

    /** List id. */
    private String m_listId;

    /** Display name. */
    private CmsMessageContainer m_name;

    /** Flag for text wrapping. */
    private boolean m_textWrapping = false;

    /** Visible Flag. */
    private boolean m_visible = true;

    /** Column width. */
    private String m_width;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     */
    public CmsListColumnDefinition(String id) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LIST_INVALID_NULL_ARG_1, "id"));
        }
        m_id = id;
    }

    /**
     * Adds a default Action.<p>
     *
     * A column could have more than one default action if the visibilities are complementary.<p>
     * 
     * @param defaultAction the default Action to add
     */
    public void addDefaultAction(CmsListDefaultAction defaultAction) {

        if (m_listId != null) {
            // set the list id
            defaultAction.setListId(m_listId);
        }
        // set the column id
        defaultAction.setColumnForLink(getId());

        m_defaultActions.add(defaultAction);
    }

    /**
     * Adds a new action to the column.<p>
     * 
     * @param listAction the action to add
     */
    public void addDirectAction(I_CmsListDirectAction listAction) {

        if (m_listId != null) {
            listAction.setListId(m_listId);
        }        
        m_directActions.add(listAction);
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
     * Returns a default action by id.<p>
     * 
     * @param actionId the id of the action
     * 
     * @return the action if found or null
     */
    public CmsListDefaultAction getDefaultAction(String actionId) {

        Iterator it = m_directActions.iterator();
        while (it.hasNext()) {
            CmsListDefaultAction action = (CmsListDefaultAction)it.next();
            if (action.getId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the default Action Ids list.<p>
     *
     * @return the default Action Ids list
     */
    public List getDefaultActionIds() {

        List ids = new ArrayList();
        Iterator itDefActions = m_defaultActions.iterator();
        while (itDefActions.hasNext()) {
            I_CmsListDirectAction action = (I_CmsListDirectAction)itDefActions.next();
            ids.add(action.getId());
        }        
        return Collections.unmodifiableList(ids);
    }

    /**
     * Returns the default Actions list.<p>
     *
     * @return the default Actions list
     */
    public List getDefaultActions() {

        return Collections.unmodifiableList(m_defaultActions);
    }

    /**
     * Returns a direct action by id.<p>
     * 
     * @param actionId the id of the action
     * 
     * @return the action if found or null
     */
    public I_CmsListDirectAction getDirectAction(String actionId) {

        Iterator it = m_directActions.iterator();
        while (it.hasNext()) {
            I_CmsListDirectAction action = (I_CmsListDirectAction)it.next();
            if (action.getId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the direct Action Ids list.<p>
     *
     * @return the direct Action Ids list
     */
    public List getDirectActionIds() {

        List ids = new ArrayList();
        Iterator itDirActions = m_directActions.iterator();
        while (itDirActions.hasNext()) {
            I_CmsListDirectAction action = (I_CmsListDirectAction)itDirActions.next();
            ids.add(action.getId());
        }        
        return Collections.unmodifiableList(ids);
    }

    /**
     * Returns all direct actions.<p>
     * 
     * @return a list of <code>{@link I_CmsListDirectAction}</code>s.
     */
    public List getDirectActions() {

        return Collections.unmodifiableList(m_directActions);
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
     * Returns the customized help Text.<p>
     * 
     * if <code>null</code> a default help text indicating the sort actions is used.<p>
     *
     * @return the customized help Text
     */
    public CmsMessageContainer getHelpText() {

        return m_helpText;
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
        Iterator itActions = m_directActions.iterator();
        while (itActions.hasNext()) {
            I_CmsListDirectAction action = (I_CmsListDirectAction)itActions.next();
            action.setItem(item);
            html.append(action.buttonHtml(wp));
        }
        if (!m_defaultActions.isEmpty()) {
            Iterator itDefaultActions = m_defaultActions.iterator();
            while (itDefaultActions.hasNext()) {
                I_CmsListDirectAction defAction = (I_CmsListDirectAction)itDefaultActions.next();
                defAction.setItem(item);
                html.append(defAction.buttonHtml(wp));
            }
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
     * @param wp the workplace instance
     * 
     * @return html code
     */
    public String htmlHeader(CmsHtmlList list, CmsWorkplace wp) {

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
        if (!isTextWrapping()) {
            html.append(" style='white-space: nowrap;'");
        }
        html.append(">\n");

        boolean isSorted = getId().equals(sortedCol);
        CmsListOrderEnum nextOrder = CmsListOrderEnum.ORDER_ASCENDING;
        if (isSorted && order == CmsListOrderEnum.ORDER_ASCENDING) {
            nextOrder = CmsListOrderEnum.ORDER_DESCENDING;
        }
        // button
        String id = listId + getId() + "Sort";
        String onClic = "listSort('" + listId + "', '" + getId() + "');";
        String helpText = null;
        if (m_helpText != null) {
            helpText = new MessageFormat(m_helpText.key(wp.getLocale()), wp.getLocale()).format(new Object[] {getName().key(
                wp.getLocale())});
        } else {
            if (isSorteable()) {
                if (nextOrder.equals(CmsListOrderEnum.ORDER_ASCENDING)) {
                    helpText = Messages.get().key(
                        wp.getLocale(),
                        Messages.GUI_LIST_COLUMN_ASC_SORT_1,
                        new Object[] {getName().key(wp.getLocale())});
                } else {
                    helpText = Messages.get().key(
                        wp.getLocale(),
                        Messages.GUI_LIST_COLUMN_DESC_SORT_1,
                        new Object[] {getName().key(wp.getLocale())});
                }
            } else {
                helpText = Messages.get().key(
                    wp.getLocale(),
                    Messages.GUI_LIST_COLUMN_NO_SORT_1,
                    new Object[] {getName().key(wp.getLocale())});
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth()) && getWidth().indexOf('%')<0) {
            html.append("\t<div style='display:block; width: ");
            html.append(getWidth());
            html.append("px;'>\n");
        }
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            wp.getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            id,
            getName().key(wp.getLocale()),
            helpText,
            isSorteable(),
            null,
            onClic));
        // sort order marker
        if (isSorted) {
            if (nextOrder == CmsListOrderEnum.ORDER_ASCENDING) {
                html.append("\t\t<img src='");
                html.append(CmsWorkplace.getSkinUri());
                html.append(ICON_UP);
                html.append("' alt=''>\n");
            } else {
                html.append("\t\t<img src='");
                html.append(CmsWorkplace.getSkinUri());
                html.append(ICON_DOWN);
                html.append("' alt=''>\n");
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth()) && getWidth().indexOf('%')<0) {
            html.append("\t</div>\n");
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

        return getListItemComparator() != null;
    }

    /**
     * Returns the text Wrapping flag.<p>
     *
     * @return the text Wrapping flag
     */
    public boolean isTextWrapping() {

        return m_textWrapping;
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
     * Sets the data formatter.<p>
     *
     * @param formatter the data formatter to set
     */
    public void setFormatter(I_CmsListFormatter formatter) {

        m_formatter = formatter;
    }

    /**
     * Sets the customized help Text.<p>
     *
     * if <code>null</code> a default help text indicating the sort actions is used.<p>
     *
     * @param helpText the customized help Text to set
     */
    public void setHelpText(CmsMessageContainer helpText) {

        m_helpText = helpText;
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
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(CmsMessageContainer name) {

        m_name = name;
    }

    /**
     * Indicates if the current column is sorteable or not.<p>
     * 
     * if <code>true</code> a default list item comparator is used.<p>
     * 
     * if <code>false</code> any previously set list item comparator is removed.<p>
     * 
     * @param sorteable the sorteable flag
     */
    public void setSorteable(boolean sorteable) {

        if (sorteable) {
            setListItemComparator(new CmsListItemDefaultComparator());
        } else {
            setListItemComparator(null);
        }
    }

    /**
     * Sets the text Wrapping flag.<p>
     *
     * @param textWrapping the text Wrapping flag to set
     */
    public void setTextWrapping(boolean textWrapping) {

        m_textWrapping = textWrapping;
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

    /**
     * Sets the id of the list.<p>
     * 
     * @param listId the id of the list
     */
    /*package*/void setListId(String listId) {

        m_listId = listId;
    }
}
